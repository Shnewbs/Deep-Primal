package com.shnewbs.deepprimal.lod.client;

/**
 * Per-extraction, allocation-free-per-page LOD error selection. Squared range
 * comparisons avoid a hypot/square root for ordinary pages. The legacy division
 * is retained at floating-point boundaries and for degenerate inputs so the
 * hysteresis/near-native handoff cannot silently shift at a threshold.
 */
final class LodScreenErrorSelector {
  private static final double EDGE_RELATIVE_EPSILON = 1.0e-10;
  private final double focal;
  private final double error;
  private final double up0Squared, up1Squared, down1Squared, down2Squared;
  private final boolean finiteThresholds;

  LodScreenErrorSelector(double focal, double error) {
    this.focal = focal;
    this.error = error;
    double upScale = error * .85;
    double downScale = error * 1.15;
    double up0 = 2 * focal / upScale;
    double up1 = 4 * focal / upScale;
    double down1 = 2 * focal / downScale;
    double down2 = 4 * focal / downScale;
    up0Squared = up0 * up0;
    up1Squared = up1 * up1;
    down1Squared = down1 * down1;
    down2Squared = down2 * down2;
    finiteThresholds = focal > 0 && error > 0 && Double.isFinite(focal)
        && Double.isFinite(error) && Double.isFinite(up0Squared)
        && Double.isFinite(up1Squared) && Double.isFinite(down1Squared)
        && Double.isFinite(down2Squared);
  }

  int select(int previous, double dx, double dz) {
    if (!finiteThresholds || previous < 0 || previous > 2
        || !Double.isFinite(dx) || !Double.isFinite(dz))
      return legacy(previous, dx, dz);
    double distanceSquared = Math.max(1, dx * dx + dz * dz);
    if (!Double.isFinite(distanceSquared)
        || near(distanceSquared, up0Squared) || near(distanceSquared, up1Squared)
        || near(distanceSquared, down1Squared) || near(distanceSquared, down2Squared))
      return legacy(previous, dx, dz);
    int detail = previous;
    while (detail < 2 && distanceSquared > (detail == 0 ? up0Squared : up1Squared))
      detail++;
    while (detail > 0 && distanceSquared < (detail == 1 ? down1Squared : down2Squared))
      detail--;
    return detail;
  }

  private static boolean near(double distanceSquared, double boundarySquared) {
    return Math.abs(distanceSquared - boundarySquared)
        <= Math.max(1, boundarySquared) * EDGE_RELATIVE_EPSILON;
  }

  private int legacy(int previous, double dx, double dz) {
    double dist = Math.max(1, Math.hypot(dx, dz));
    int detail = previous;
    while (detail < 2 && (1 << (detail + 1)) * focal / dist < error * .85) detail++;
    while (detail > 0 && (1 << detail) * focal / dist > error * 1.15) detail--;
    return detail;
  }
}
