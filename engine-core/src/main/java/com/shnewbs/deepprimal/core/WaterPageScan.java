package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.lod.DeliveredChunkScan;

/** Nearest-column/nearest-height-first water discovery, bounded and resumable without chunk reads. */
public final class WaterPageScan {
  @FunctionalInterface public interface Visitor { boolean visit(int x, int y, int z); }
  private final DeliveredChunkScan columns;
  private final int centerY, minY, maxY;
  private int vertical;

  public WaterPageScan(int x, int y, int z, int radius, int minY, int maxY) {
    if (minY >= maxY || (long)maxY - minY > 4096)
      throw new IllegalArgumentException("Water discovery height range");
    columns = new DeliveredChunkScan(x, z, radius);
    centerY = Math.clamp(y, minY, maxY - 1);
    this.minY = minY; this.maxY = maxY;
  }

  public boolean done() { return columns.done(); }
  public boolean shouldRetarget(int x, int y, int z, int radius, int minY, int maxY) {
    return this.minY != minY || this.maxY != maxY || centerY != Math.clamp(y, minY, maxY - 1)
        || columns.shouldRetarget(x, z, radius);
  }

  /** A refused page is offered again next turn; time/admission pressure cannot drop a surface. */
  public int drain(int limit, Visitor visitor) {
    if (limit < 0) throw new IllegalArgumentException("Water discovery budget");
    java.util.Objects.requireNonNull(visitor);
    int[] count = {0};
    columns.drain(limit, (x, z) -> {
      int extent = Math.max(centerY - minY, maxY - 1 - centerY);
      while (vertical <= 2 * extent) {
        int offset = vertical == 0 ? 0 : (vertical % 2 == 1 ? -(vertical + 1) / 2 : vertical / 2);
        long candidate = (long) centerY + offset;
        if (candidate < minY || candidate >= maxY) { vertical++; continue; }
        int y = (int)candidate;
        if (count[0] >= limit || !visitor.visit(x, y, z)) return false;
        count[0]++; vertical++;
      }
      vertical = 0;
      return true;
    });
    return count[0];
  }
}
