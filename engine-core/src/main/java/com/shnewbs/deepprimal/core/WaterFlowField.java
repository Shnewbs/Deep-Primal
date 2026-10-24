package com.shnewbs.deepprimal.core;

/** Canonical visual direction at one global water corner. Never changes quantity. */
public final class WaterFlowField {
  public static int direction(WaterPatchMesher.Snapshot s, int x, int y, int z) {
    if (s == null) return -1;
    double vx = 0, vz = 0; boolean wet = false;
    for (int dz = -1; dz <= 0; dz++) for (int dx = -1; dx <= 0; dx++) {
      if (s.water(x + dx, y, z + dz) <= 0) continue;
      wet = true; int d = s.direction(x + dx, y, z + dz);
      if (d < 16) { vx += WaterPatchMesher.flowX(d); vz += WaterPatchMesher.flowZ(d); }
    }
    if (!wet) return -1;
    if (vx * vx + vz * vz < 1e-12) return 16;
    return (int) Math.round(Math.atan2(vz, vx) * (8 / Math.PI)) & 15;
  }
  /** Four 5-bit IDs plus the low/high four/three bits of the fourth age.
   * The valid ID range leaves every encoded integer strictly below 2^24. */
  public static float targets(int directions, int fourthAge) {
    return -(directions | ((fourthAge & 15) << 20)) - 2f;
  }
  public static float sources(int directions, int fourthAge) {
    return directions | ((fourthAge >>> 4) << 20);
  }
  /** Preserve an active height morph; expired offsets need no more than its full window. */
  public static int heightClock(int offset, int cornerAge) {
    return (Math.clamp(offset, -WaterSurfaceTransition.MILLIS, 0) & 255) | (cornerAge << 8);
  }
  private WaterFlowField() {}
}
