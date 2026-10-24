package com.shnewbs.deepprimal.core.lod;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** One cancellable fixed-camera scan; turns and cleanup belong to one existing worker thread. */
public final class LodIndexRequest {
  public enum Status { MORE, COMPLETE, CANCELLED }
  public record Step(Status status, long epoch, int cameraX, int cameraZ,
      long visitedEntries, List<Long> keys) {
    public Step { keys = List.copyOf(keys); }
  }
  private final Path root;
  private final long epoch;
  private final int cameraX, cameraZ, limit, budget;
  private final AtomicBoolean cancelled = new AtomicBoolean();
  private Thread owner;
  private LodDiskIndex cursor;
  private volatile boolean terminal, open;
  private volatile long visited;

  public LodIndexRequest(Path root, long epoch, int cameraX, int cameraZ, int limit, int budget) {
    this.root = Objects.requireNonNull(root);
    if (limit < 1 || limit > LodDiskIndex.MAX_KEYS || budget < 1
        || budget > LodDiskIndex.MAX_ENTRIES_PER_TURN)
      throw new IllegalArgumentException("LOD request bounds");
    this.epoch = epoch; this.cameraX = cameraX; this.cameraZ = cameraZ;
    this.limit = limit; this.budget = budget;
  }
  /** Render-side signal only: never waits for or touches the filesystem. */
  public void cancel() { cancelled.set(true); }
  public boolean isCancelled() { return cancelled.get(); }
  public boolean isTerminal() { return terminal; }
  public boolean isOpen() { return open; }
  public long visitedEntries() { return visited; }

  private void owned() {
    if (owner == null) owner = Thread.currentThread();
    else if (owner != Thread.currentThread())
      throw new IllegalStateException("LOD index cursor changed worker thread");
  }
  private Step step(Status status, List<Long> keys) {
    return new Step(status, epoch, cameraX, cameraZ, visited, keys);
  }
  /** Submit at most once per service turn; this method never resubmits itself. */
  public Step turn() throws IOException {
    owned();
    if (terminal) throw new IllegalStateException("LOD request is terminal");
    try {
      if (cancelled.get()) { closeOnWorker(); return step(Status.CANCELLED, List.of()); }
      if (cursor == null) { cursor = new LodDiskIndex(root, cameraX, cameraZ, limit); open = cursor.isOpen(); }
      boolean done = cursor.advance(budget); visited = cursor.visitedEntries(); open = cursor.isOpen();
      if (cancelled.get()) { closeOnWorker(); return step(Status.CANCELLED, List.of()); }
      if (!done) return step(Status.MORE, List.of());
      List<Long> keys = cursor.keys(); closeCursor(); terminal = true;
      // The consumer must also check isCancelled/current request/session/camera at publication.
      if (cancelled.get()) return step(Status.CANCELLED, List.of());
      return step(Status.COMPLETE, keys);
    } catch (IOException | RuntimeException | Error failure) {
      terminal = true;
      try { closeCursor(); } catch (IOException close) { failure.addSuppressed(close); }
      throw failure;
    }
  }
  /** Queue on the same worker before graceful shutdown; do not discard this cleanup job. */
  public void closeOnWorker() throws IOException {
    owned(); cancelled.set(true); terminal = true; closeCursor();
  }
  private void closeCursor() throws IOException {
    LodDiskIndex old = cursor; cursor = null;
    try { if (old != null) old.close(); }
    finally { open = false; }
  }
}
