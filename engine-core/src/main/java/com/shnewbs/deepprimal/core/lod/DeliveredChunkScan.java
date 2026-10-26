package com.shnewbs.deepprimal.core.lod;

import com.shnewbs.deepprimal.core.DistancePolicy;

/** Bounded, resumable, nearest-square-first enumeration; never loads or retains chunks. */
public final class DeliveredChunkScan {
  @FunctionalInterface
  public interface Visitor {
    /** False pauses before this coordinate, so admission pressure cannot silently skip it. */
    boolean visit(int x, int z);
  }
  private final int centerX, centerZ, radius;
  private int ring, edge, visited;

  public DeliveredChunkScan(int x, int z, int radius) {
    if (radius < 0 || radius > DistancePolicy.MAX_RENDER
        || (long)x - radius < Integer.MIN_VALUE || (long)x + radius > Integer.MAX_VALUE
        || (long)z - radius < Integer.MIN_VALUE || (long)z + radius > Integer.MAX_VALUE)
      throw new IllegalArgumentException("Delivered scan bounds");
    centerX = x; centerZ = z; this.radius = radius;
  }

  public int total() { int side = 2 * radius + 1; return side * side; }
  public int visited() { return visited; }
  public boolean done() { return visited == total(); }

  public int drain(int budget, Visitor visitor) {
    if (budget < 0) throw new IllegalArgumentException("Negative scan budget");
    int count = 0;
    while (count < budget && !done()) {
      int dx = 0, dz = 0;
      if (ring > 0) {
        int width = 2 * ring, side = edge / width, along = edge % width;
        switch (side) {
          case 0 -> { dx = -ring + along; dz = -ring; }
          case 1 -> { dx = ring; dz = -ring + along; }
          case 2 -> { dx = ring - along; dz = ring; }
          case 3 -> { dx = -ring; dz = ring - along; }
          default -> throw new AssertionError("Scan edge");
        }
      }
      if (!visitor.visit(centerX + dx, centerZ + dz)) break;
      count++; visited++;
      if (ring == 0) ring = 1;
      else if (++edge == 8 * ring) { ring++; edge = 0; }
    }
    return count;
  }
}
