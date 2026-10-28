package com.shnewbs.deepprimal.lod.client;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CancellationException;

/**
 * Finds the closest persisted LOD pages without treating directory enumeration order
 * as spatial priority. Scans metadata on the LOD worker, never the render thread;
 * retains at most {@value #MAX_KEYS} candidates. The filesystem scan itself is
 * still O(number of files) and must not be called in a render callback.
 */
final class LodDiskNearestScan {
  static final int MAX_KEYS = 8192;
  private static final String EXTENSION = ".plod";

  private LodDiskNearestScan() {}

  static List<Long> scan(Path folder, int cameraX, int cameraZ, int maxKeys)
      throws IOException {
    if (maxKeys < 1 || maxKeys > MAX_KEYS)
      throw new IllegalArgumentException("invalid disk-index capacity");
    if (Thread.currentThread().isInterrupted()) throw new CancellationException();
    if (!Files.isDirectory(folder)) return List.of();

    // Farthest at the head so a late, nearer file can replace any directory-prefix
    // candidate. Ordered tie breaks make the resulting set independent of iteration order.
    PriorityQueue<RankedKey> farthest = new PriorityQueue<>(maxKeys, Comparator.reverseOrder());
    try (DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.plod")) {
      for (Path file : files) {
        if (Thread.currentThread().isInterrupted()) throw new CancellationException();
        long[] coordinates = parse(file.getFileName().toString());
        if (coordinates == null) continue;
        RankedKey candidate = new RankedKey((int) coordinates[0], (int) coordinates[1], cameraX, cameraZ);
        if (farthest.size() < maxKeys) farthest.add(candidate);
        else if (candidate.compareTo(farthest.peek()) < 0) {
          farthest.remove();
          farthest.add(candidate);
        }
      }
    }
    ArrayList<RankedKey> nearest = new ArrayList<>(farthest);
    nearest.sort(Comparator.naturalOrder());
    ArrayList<Long> result = new ArrayList<>(nearest.size());
    for (RankedKey ranked : nearest)
      result.add(((long) ranked.x << 32) ^ (ranked.z & 0xffffffffL));
    return List.copyOf(result);
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
      // Reject ambiguous spellings that could alias one coordinate/key to multiple files.
      if (!base.equals(x + "_" + z)) return null;
      return new long[] {x, z};
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  /** Exact unsigned 128-bit squared distance: int-to-int deltas can exceed 32 bits. */
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
