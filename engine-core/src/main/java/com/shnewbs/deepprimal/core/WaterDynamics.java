package com.shnewbs.deepprimal.core;

/** Shared deterministic water math. No world scans, platform state or allocations. */
public final class WaterDynamics {
  private static final double[] SPEEDS = {.35, 1, 1.45, 1.9};

  public static double speedForBand(int band) {
    return SPEEDS[band];
  }

  public static double speed(double slope, double catchment, double width) {
    // Invalid hydrology samples must not reach animation/flow-band selection as NaN.
    // Preserve the exact historical calculation for every finite input.
    if (!Double.isFinite(slope) || !Double.isFinite(catchment) || !Double.isFinite(width))
      return SPEEDS[0];
    return Math.clamp(
        .3
            + 2.2 * Math.sqrt(Math.max(0, slope) + .0025)
            + .12 * Math.sqrt(Math.max(1, catchment)) / Math.max(4, width * 3),
        .35,
        1.9);
  }

  public static int band(double speed) {
    int best = 0;
    for (int i = 1; i < SPEEDS.length; i++)
      if (Math.abs(speed - SPEEDS[i]) < Math.abs(speed - SPEEDS[best])) best = i;
    return best;
  }

  public static int mix(int a, int b, double amount) {
    // Invalid climate/opacity samples must not round NaN channels to black.
    // Preserve the original color until a finite blend factor is available.
    if (!Double.isFinite(amount)) return a & 0xFFFFFF;
    double t = Math.clamp(amount, 0, 1);
    int r = (int) Math.round((a >> 16 & 255) * (1 - t) + (b >> 16 & 255) * t);
    int g = (int) Math.round((a >> 8 & 255) * (1 - t) + (b >> 8 & 255) * t);
    int blue = (int) Math.round((a & 255) * (1 - t) + (b & 255) * t);
    return r << 16 | g << 8 | blue;
  }

  public static int palette(int nativeColor, double temperature, double rainfall) {
    // Invalid climate fields cannot be interpreted as a real biome tint.
    if (!Double.isFinite(temperature) || !Double.isFinite(rainfall))
      return nativeColor & 0xFFFFFF;
    int cold = mix(nativeColor, 0x63a9b6, Math.clamp((.5 - temperature) * 1.5, 0, .8));
    return mix(cold, 0x77724a, Math.clamp((temperature - .8) * rainfall * .65, 0, .7));
  }

  public static double wave(double x, double z, double ticks, double storm) {
    // Do not publish NaN displacement to a water vertex buffer.
    if (!Double.isFinite(x) || !Double.isFinite(z)
        || !Double.isFinite(ticks) || !Double.isFinite(storm)) return 0;
    double p = ticks * .045;
    double displacement = (Math.sin(x * .095 + z * .031 - p)
            + .45 * Math.sin(z * .17 - x * .04 - p * 1.41)
            + .2 * Math.sin(x * .29 + z * .12 - p * 1.9)
            + .12 * Math.sin(x * .37 - z * .23 - p * 2.3)
            + .08 * Math.sin(x * .51 + z * .41 - p * 2.7)
            + .05 * Math.sin(z * .67 - x * .32 - p * 3.1)
            + .03 * Math.sin(x * .83 + z * .53 - p * 3.6)
            + .02 * Math.sin(z * 1.07 - x * .73 - p * 4.1))
        * (1.65 / 1.95)
        * (.10 + Math.clamp(storm, 0, 1) * .15);
    // Finite but extreme coordinates can overflow the intermediate phase sums.
    return Double.isFinite(displacement) ? displacement : 0;
  }

  public static double oceanHeight(double base, double continent, double noise, int floor) {
    // A corrupt climate/noise sample cannot become a NaN terrain column.
    // Preserve the supplied finite base when shaping is unavailable.
    double minimum = (double) floor + 40.0;
    if (!Double.isFinite(base)) return minimum;
    if (!Double.isFinite(continent) || !Double.isFinite(noise))
      return Math.max(minimum, base);
    double offshore = smooth(Math.clamp((-continent - .25) / .35, 0, 1));
    double trench = smooth(Math.clamp((.19 - Math.abs(noise)) / .19, 0, 1));
    // Promote before arithmetic: integer floor+40 and 96-floor-80 can overflow.
    double shaped = base - offshore * (35 + trench * (96.0 - floor - 80.0));
    return Double.isFinite(shaped) ? Math.max(minimum, shaped) : Math.max(minimum, base);
  }

  private static double smooth(double t) {
    return t * t * (3 - 2 * t);
  }

  /** A bad sampled waterline must not silently place a source at Y=0 or a saturated int edge. */
  private static double checkedWaterline(double waterline) {
    double target = waterline - 1.0 / 9;
    // floor(target) must fit in a native integer Y. Test the half-open range without
    // narrowing the double first; casting NaN/Infinity silently returns an integer.
    if (!Double.isFinite(target) || target < Integer.MIN_VALUE
        || target >= (double) Integer.MAX_VALUE + 1.0)
      throw new IllegalArgumentException("Nonfinite or unrepresentable waterline: " + waterline);
    return target;
  }

  /** Native water surface quantization; integer waterlines preserve source-water height. */
  public static int surfaceY(double waterline) {
    return (int) Math.floor(checkedWaterline(waterline));
  }

  /** Exclusive native water-top Y; reject overflow before the aquifer caches the column. */
  public static int surfaceTopY(double waterline) {
    int y = surfaceY(waterline);
    if (y == Integer.MAX_VALUE)
      throw new IllegalArgumentException("Native water top exceeds integer Y: " + waterline);
    return y + 1;
  }

  public static int surfaceLevel(double waterline) {
    double target = checkedWaterline(waterline);
    int amount = Math.clamp(Math.round((target - Math.floor(target)) * 9), 1, 8);
    return 8 - amount;
  }

  private WaterDynamics() {}
}
