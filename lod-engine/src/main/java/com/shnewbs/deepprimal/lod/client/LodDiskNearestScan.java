package com.shnewbs.deepprimal.lod.client;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Nearest-first persisted LOD discovery. The live renderer uses a separate,
 * low-priority cursor thread: one nonblocking client-tick grant permits at most
 * {@value #ENTRIES_PER_TURN} directory entries, leaving the meshing worker free.
 * The cursor owns and closes its directory iterator, including on cancellation.
 * Only canonical filenames are considered; file contents are not loaded here.
 */
final class LodDiskNearestScan {
  static final int MAX_KEYS = 8192;
  static final int ENTRIES_PER_TURN = 256;
  static final int RETARGET_CHUNKS = 64;
  private static final String EXTENSION = ".plod";

  private LodDiskNearestScan() {}

  /** Synchronous reference for tests and offline tooling; not a render callback. */
  static List<Long> scan(Path folder, int cameraX, int cameraZ, int maxKeys)
      throws IOException {
    checkLimit(maxKeys);
    if (Thread.currentThread().isInterrupted()) throw new CancellationException();
    if (!Files.isDirectory(folder)) return List.of();
    Selection selected = new Selection(cameraX, cameraZ, maxKeys);
    try (DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.plod")) {
      for (Path file : files) {
        if (Thread.currentThread().isInterrupted()) throw new CancellationException();
        selected.accept(file.getFileName().toString());
      }
    }
    return selected.nearest();
  }

  /** Starts a per-world, cancellable metadata cursor without touching the filesystem on this thread. */
  static Cursor start(Path folder, int cameraX, int cameraZ, int maxKeys) {
    return start(folder, cameraX, cameraZ, maxKeys, List.of());
  }

  /** Capture recovery keys on the owner thread; rank them on the cursor thread. */
  static Cursor start(Path folder, int cameraX, int cameraZ, int maxKeys,
      List<Long> queuedRecovery) {
    checkLimit(maxKeys);
    if (queuedRecovery.size() > MAX_KEYS)
      throw new IllegalArgumentException("oversized initial recovery queue");
    return new Cursor(folder, cameraX, cameraZ, maxKeys, List.copyOf(queuedRecovery));
  }

  /**
   * Reconcile a completed nearest-first scan with queued persisted recovery.
   * Both inputs are capped; a full queue of distant recoveries cannot starve
   * closer pages. Dropped keys remain discoverable from their files on a later
   * retarget. The caller retains its existing edit/revision/active-job guards.
   */
  static List<Long> reconcile(LinkedHashSet<Long> existing, List<Long> incoming,
      int cameraX, int cameraZ, int maxKeys) {
    checkLimit(maxKeys);
    if (existing.size() > MAX_KEYS || incoming.size() > MAX_KEYS)
      throw new IllegalArgumentException("oversized disk index input");
    Selection selected = new Selection(cameraX, cameraZ, maxKeys);
    HashSet<Long> seen = new HashSet<>(existing.size() + incoming.size());
    for (long key : existing)
      if (seen.add(key)) selected.accept(key);
    for (long key : incoming)
      if (seen.add(key)) selected.accept(key);
    return selected.nearest();
  }

  private static void checkLimit(int maxKeys) {
    if (maxKeys < 1 || maxKeys > MAX_KEYS)
      throw new IllegalArgumentException("invalid disk-index capacity");
  }

  /** Finished results are immutable. Never call sort() on keys. */
  record Completion(List<Long> keys, List<Long> discardKeys, IOException failure, long examined, long turns) {}

  /**
   * This cursor's only mutator of the DirectoryStream, heap and iterator is its
   * own daemon thread. Client callbacks only grant a capped turn, read a
   * volatile completed result, or signal cancellation without blocking.
   */
  static final class Cursor {
    private final Path folder;
    private final Selection selection;
    private final List<Long> recovery;
    private final Semaphore grants = new Semaphore(0);
    private final AtomicBoolean granted = new AtomicBoolean();
    private final Thread thread;
    private volatile boolean cancelled;
    private volatile boolean missingDirectory;
    private volatile Completion completed;
    private volatile long examined, turns;

    private Cursor(Path folder, int cameraX, int cameraZ, int maxKeys,
        List<Long> recovery) {
      this.folder = folder;
      this.selection = new Selection(cameraX, cameraZ, maxKeys);
      this.recovery = recovery;
      thread = new Thread(this::run, "Deep Primal LOD disk cursor");
      thread.setDaemon(true);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }

    /** At most one queued grant, even if the filesystem blocks for multiple ticks. */
    void advance() {
      if (!cancelled && completed == null && granted.compareAndSet(false, true))
        grants.release();
    }

    Completion completed() { return completed; }
    boolean cancelled() { return cancelled; }
    boolean missingDirectory() { return missingDirectory; }
    boolean stopped() { return !thread.isAlive(); }
    long examined() { return examined; }
    long turns() { return turns; }

    /** Nonblocking: the cursor closes its own directory stream on its worker. */
    void cancel() {
      cancelled = true;
      thread.interrupt();
      grants.release();
    }

    // Focused-test hook only; never called on a client/render thread.
    boolean awaitStopped(long timeoutMillis) throws InterruptedException {
      thread.join(timeoutMillis);
      return !thread.isAlive();
    }

    private void run() {
      try {
        // The stream, iterator and priority heap never cross into the client thread.
        // Existing recovery pages share the same per-turn metadata-work budget.
        Iterator<Long> recoveries = recovery.iterator();
        if (!Files.isDirectory(folder)) {
          // A world may create its LOD directory after the first client tick.
          // Preserve existing recovery keys, but mark this as retryable.
          missingDirectory = true;
          consumeRecoveryOnly(recoveries);
          return;
        }
        try (DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.plod")) {
          Iterator<Path> iterator = files.iterator();
          while (!cancelled) {
            grants.acquire();
            if (cancelled) break;
            int inspected = 0;
            while (inspected < ENTRIES_PER_TURN && !cancelled) {
              if (recoveries.hasNext()) selection.accept(recoveries.next());
              else if (iterator.hasNext())
                selection.accept(iterator.next().getFileName().toString());
              else break;
              inspected++;
              examined++;
            }
            turns++;
            if (!cancelled && !recoveries.hasNext() && !iterator.hasNext()) {
              publish();
              return;
            }
            granted.set(false);
          }
        }
      } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
        if (!cancelled)
          completed = new Completion(List.of(), List.of(), new IOException("disk scan interrupted", interrupted),
              examined, turns);
      } catch (IOException | DirectoryIteratorException | SecurityException error) {
        if (!cancelled) {
          IOException failure = error instanceof IOException io ? io
              : new IOException("disk directory enumeration failed", error);
          completed = new Completion(List.of(), List.of(), failure, examined, turns);
        }
      } finally {
        granted.set(false);
      }
    }

    private void consumeRecoveryOnly(Iterator<Long> recoveries) throws InterruptedException {
      while (!cancelled) {
        grants.acquire();
        if (cancelled) return;
        int inspected = 0;
        while (inspected < ENTRIES_PER_TURN && !cancelled && recoveries.hasNext()) {
          selection.accept(recoveries.next());
          inspected++;
          examined++;
        }
        turns++;
        if (!cancelled && !recoveries.hasNext()) {
          publish();
          return;
        }
        granted.set(false);
      }
    }

    private void publish() {
      if (cancelled) return;
      List<Long> nearest = selection.nearest();
      // Precompute all expensive membership/sort work on the cursor thread.
      // The client applies at most 256 queue mutations per tick.
      HashSet<Long> chosen = new HashSet<>(nearest);
      ArrayList<Long> discard = new ArrayList<>();
      for (long old : recovery)
        if (!chosen.contains(old)) discard.add(old);
      if (!cancelled)
        completed = new Completion(nearest, List.copyOf(discard), null, examined, turns);
    }
  }

  private static final class Selection {
    private final int cameraX, cameraZ, maxKeys;
    private final PriorityQueue<RankedKey> farthest;
    private final HashSet<Long> retained;

    Selection(int cameraX, int cameraZ, int maxKeys) {
      this.cameraX = cameraX;
      this.cameraZ = cameraZ;
      this.maxKeys = maxKeys;
      farthest = new PriorityQueue<>(maxKeys, Comparator.reverseOrder());
      retained = new HashSet<>(maxKeys);
    }

    void accept(String filename) {
      long[] coordinates = parse(filename);
      if (coordinates != null)
        accept(new RankedKey((int) coordinates[0], (int) coordinates[1], cameraX, cameraZ));
    }

    void accept(long key) {
      accept(new RankedKey((int) (key >> 32), (int) key, cameraX, cameraZ));
    }

    private void accept(RankedKey candidate) {
      long key = ((long) candidate.x << 32) ^ (candidate.z & 0xffffffffL);
      if (retained.contains(key)) return;
      if (farthest.size() < maxKeys) {
        farthest.add(candidate);
        retained.add(key);
      } else if (candidate.compareTo(farthest.peek()) < 0) {
        RankedKey evicted = farthest.remove();
        retained.remove(((long) evicted.x << 32) ^ (evicted.z & 0xffffffffL));
        farthest.add(candidate);
        retained.add(key);
      }
    }

    List<Long> nearest() {
      ArrayList<RankedKey> ranked = new ArrayList<>(farthest);
      ranked.sort(Comparator.naturalOrder());
      ArrayList<Long> result = new ArrayList<>(ranked.size());
      for (RankedKey key : ranked)
        result.add(((long) key.x << 32) ^ (key.z & 0xffffffffL));
      return List.copyOf(result);
    }
  }

  private static long[] parse(String filename) {
    if (!filename.endsWith(EXTENSION) || filename.length() > 30) return null;
    String base = filename.substring(0, filename.length() - EXTENSION.length());
    int separator = base.indexOf('_');
    if (separator <= 0 || separator == base.length() - 1 || base.indexOf('_', separator + 1) != -1)
      return null;
    try {
      int x = Integer.parseInt(base.substring(0, separator));
      int z = Integer.parseInt(base.substring(separator + 1));
      if (!base.equals(x + "_" + z)) return null;
      return new long[] {x, z};
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  /** Exact unsigned 128-bit squared distance: signed int deltas may exceed 32 bits. */
  private static final class RankedKey implements Comparable<RankedKey> {
    final int x, z;
    final long ring, high, low;

    RankedKey(int x, int z, int cx, int cz) {
      this.x = x;
      this.z = z;
      long dx = Math.abs((long) x - cx), dz = Math.abs((long) z - cz);
      ring = Math.max(dx, dz);
      long ax = dx * dx, az = dz * dz;
      low = ax + az;
      high = Math.multiplyHigh(dx, dx) + Math.multiplyHigh(dz, dz)
          + (Long.compareUnsigned(low, ax) < 0 ? 1 : 0);
    }

    @Override
    public int compareTo(RankedKey other) {
      int c = Long.compare(ring, other.ring);
      if (c != 0) return c;
      c = Long.compareUnsigned(high, other.high);
      if (c != 0) return c;
      c = Long.compareUnsigned(low, other.low);
      if (c != 0) return c;
      c = Integer.compare(x, other.x);
      return c != 0 ? c : Integer.compare(z, other.z);
    }
  }
}
