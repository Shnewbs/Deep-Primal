package com.shnewbs.deepprimal.core.lod;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Worker-owned, entry-bounded nearest-key enumeration; does not read geometry or load chunks. */
public final class LodDiskIndex implements AutoCloseable {
  public static final int MAX_KEYS = 8192, MAX_ENTRIES_PER_TURN = 256;
  private record Candidate(int x, int z, long low, int high) {
    long key() { return ((long) x << 32) ^ (z & 0xffffffffL); }
  }
  private static int compare(Candidate a, Candidate b) {
    int c = Integer.compare(a.high, b.high);
    if (c == 0) c = Long.compareUnsigned(a.low, b.low);
    if (c == 0) c = Integer.compare(a.x, b.x);
    return c != 0 ? c : Integer.compare(a.z, b.z);
  }
  private final int cameraX, cameraZ, limit;
  private final PriorityQueue<Candidate> nearest;
  private DirectoryStream<Path> directory;
  private Iterator<Path> iterator;
  private long visited;
  private boolean complete, closed;

  public LodDiskIndex(Path root, int cameraX, int cameraZ, int limit) throws IOException {
    if (limit < 1 || limit > MAX_KEYS) throw new IllegalArgumentException("LOD index key bound");
    this.cameraX = cameraX; this.cameraZ = cameraZ; this.limit = limit;
    nearest = new PriorityQueue<>(limit, (a, b) -> compare(b, a));
    if (!Files.isDirectory(root)) { complete = true; return; }
    directory = Files.newDirectoryStream(root); // Count all directory entries, including invalid names.
    try { iterator = directory.iterator(); }
    catch (RuntimeException | Error failure) {
      try { close(); } catch (IOException close) { failure.addSuppressed(close); }
      throw failure;
    }
  }

  /** At most budget directory entries per call; filesystem latency itself is not time-bounded. */
  public boolean advance(int budget) throws IOException {
    if (budget < 1 || budget > MAX_ENTRIES_PER_TURN)
      throw new IllegalArgumentException("LOD index turn bound");
    if (complete) return true;
    if (closed) throw new IllegalStateException("LOD index closed before completion");
    try {
      for (int n = 0; n < budget; n++) {
        if (!iterator.hasNext()) { complete = true; closeDirectory(); return true; }
        Path path = iterator.next(); visited++;
        Candidate c = parse(path.getFileName().toString());
        if (c == null) continue;
        if (nearest.size() < limit) nearest.add(c);
        else if (compare(c, nearest.peek()) < 0) { nearest.remove(); nearest.add(c); }
      }
      return false;
    } catch (DirectoryIteratorException failure) {
      complete = false;
      IOException cause = failure.getCause();
      try { close(); } catch (IOException close) { cause.addSuppressed(close); }
      throw cause;
    } catch (IOException | RuntimeException | Error failure) {
      complete = false;
      try { close(); } catch (IOException close) { failure.addSuppressed(close); }
      throw failure;
    }
  }

  private Candidate parse(String name) {
    if (!name.endsWith(".plod")) return null;
    String stem = name.substring(0, name.length() - 5);
    int split = stem.indexOf('_');
    if (split < 1 || split != stem.lastIndexOf('_')) return null;
    try {
      int x = Integer.parseInt(stem.substring(0, split));
      int z = Integer.parseInt(stem.substring(split + 1));
      if (!name.equals(x + "_" + z + ".plod")) return null; // Match LodStore's canonical path.
      long dx = (long) x - cameraX, dz = (long) z - cameraZ;
      long a = dx * dx, b = dz * dz, low = a + b;
      // Each square fits unsigned64; their sum needs one carry bit for full int coordinates.
      int high = Long.compareUnsigned(low, a) < 0 ? 1 : 0;
      return new Candidate(x, z, low, high);
    } catch (NumberFormatException ignored) { return null; }
  }

  public long visitedEntries() { return visited; }
  public int retainedKeys() { return nearest.size(); }
  public boolean isComplete() { return complete; }
  public boolean isOpen() { return directory != null; }

  /** Complete global result for the fixed camera; bounded sorting still needs worker scheduling. */
  public List<Long> keys() {
    if (!complete || closed) throw new IllegalStateException("LOD index not completed successfully");
    var sorted = new ArrayList<>(nearest); sorted.sort(LodDiskIndex::compare);
    var keys = new ArrayList<Long>(sorted.size());
    for (var c : sorted) keys.add(c.key());
    return List.copyOf(keys);
  }

  private void closeDirectory() throws IOException {
    DirectoryStream<Path> stream = directory; directory = null; iterator = null;
    if (stream != null) stream.close();
  }
  @Override public void close() throws IOException {
    if (!complete) { closed = true; nearest.clear(); }
    closeDirectory();
  }
}
