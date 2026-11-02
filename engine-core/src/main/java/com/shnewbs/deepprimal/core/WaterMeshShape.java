package com.shnewbs.deepprimal.core;

/** Small, seamless horizontal displacement shared by neighboring water mesh pages. */
public final class WaterMeshShape {
  private static final double PHASE = Math.PI / 8.0;

  public static float offsetX(double x, double y, double z) {
    // A bad or extreme captured position must not publish NaN displacement or UV phase.
    // Preserve the exact historical trigonometry for ordinary finite phases.
    double a = (x + 2 * z + y) * PHASE;
    double b = (2 * x - z + 3 * y) * PHASE;
    if (!Double.isFinite(a) || !Double.isFinite(b)) return 0;
    return (float) (0.045 * Math.sin(a) + 0.012 * Math.sin(b));
  }

  public static float offsetZ(double x, double y, double z) {
    double a = (x + 2 * z + y) * PHASE;
    double b = (2 * x - z + 3 * y) * PHASE;
    if (!Double.isFinite(a) || !Double.isFinite(b)) return 0;
    return (float) (0.045 * Math.cos(a) - 0.012 * Math.cos(b));
  }

  public static float x(double x, double y, double z) {
    return (float) x + offsetX(x, y, z);
  }

  public static float z(double x, double y, double z) {
    return (float) z + offsetZ(x, y, z);
  }

  public static float flowingV(double x, double y, double z) {
    double phase = 1.0 - y + (x + z) / 16.0;
    if (!Double.isFinite(phase)) return 0;
    return (float) (phase - Math.floor(phase));
  }

  /** Atlas-local vertical coordinate for a single block's falling-water side. */
  public static float sideV(float relativeY) {
    return 1.0f - relativeY;
  }

  private WaterMeshShape() {}
}
