package com.shnewbs.deepprimal.core;

/** Integer water volumes: eight 125 mB units fill one native water block. */
public final class FluidVolume {
  public static final int CAPACITY = 8, MILLIBUCKETS = 125;

  /** Native water amounts must be admitted before transfer math can move volume. */
  private static void requireUnits(int source, int target) {
    if (source < 0 || source > CAPACITY || target < 0 || target > CAPACITY)
      throw new IllegalArgumentException("Water units outside 0..8");
  }

  public static int down(int source, int target) {
    requireUnits(source, target);
    return Math.max(0, Math.min(source, CAPACITY - target));
  }

  public static int sideways(int source, int target) {
    requireUnits(source, target);
    return Math.max(0, (source - target) / 2);
  }

  public static int pressure(int sourceY, int source, int targetY, int target) {
    requireUnits(source, target);
    long difference = ((long) sourceY - targetY) * CAPACITY + source - target;
    return (int) Math.max(0, Math.min(Math.min(source, CAPACITY - target), difference / 2));
  }

  public static int nativeLevel(int units) {
    if (units < 1 || units > CAPACITY) throw new IllegalArgumentException("Water units: " + units);
    return CAPACITY - units;
  }

  private FluidVolume() {}
}
