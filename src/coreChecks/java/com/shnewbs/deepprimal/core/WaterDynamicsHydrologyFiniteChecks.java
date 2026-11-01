package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Isolated parity and invalid-input admission for speed and offshore terrain shaping. */
public final class WaterDynamicsHydrologyFiniteChecks {
  private static long checks;
  private static void need(boolean yes, String what) {
    checks++;
    if (!yes) throw new AssertionError(what);
  }
  private static double baselineSpeed(double slope, double catchment, double width) {
    return Math.clamp(.3 + 2.2 * Math.sqrt(Math.max(0, slope) + .0025)
        + .12 * Math.sqrt(Math.max(1, catchment)) / Math.max(4, width * 3), .35, 1.9);
  }
  private static double smooth(double t) { return t * t * (3 - 2 * t); }
  private static double baselineOcean(double base, double continent, double noise, int floor) {
    double offshore = smooth(Math.clamp((-continent - .25) / .35, 0, 1));
    double trench = smooth(Math.clamp((.19 - Math.abs(noise)) / .19, 0, 1));
    return Math.max(floor + 40, base - offshore * (35 + trench * (96 - floor - 80)));
  }
  private static boolean same(double a, double b) {
    return Double.doubleToRawLongBits(a) == Double.doubleToRawLongBits(b);
  }
  public static void main(String[] args) {
    double[] invalid = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
    for (double bad : invalid) {
      need(WaterDynamics.speed(bad, 100, 8) == .35, "invalid slope speed");
      need(WaterDynamics.speed(.2, bad, 8) == .35, "invalid catchment speed");
      need(WaterDynamics.speed(.2, 100, bad) == .35, "invalid width speed");
      need(WaterDynamics.oceanHeight(bad, -.8, .03, -384) == -344, "invalid base fallback");
      need(WaterDynamics.oceanHeight(90, bad, .03, -384) == 90, "invalid continent fallback");
      need(WaterDynamics.oceanHeight(90, -.8, bad, -384) == 90, "invalid noise fallback");
      need(WaterDynamics.oceanHeight(-500, bad, .03, -384) == -344, "minimum relief fallback");
    }
    need(WaterDynamics.oceanHeight(0, -.8, .01, Integer.MAX_VALUE)
        >= (double) Integer.MAX_VALUE + 40, "high floor minimum overflow");
    need(WaterDynamics.oceanHeight(0, -.8, .01, Integer.MIN_VALUE)
        >= (double) Integer.MIN_VALUE + 40, "low floor minimum overflow");
    need(Double.isFinite(WaterDynamics.oceanHeight(100, -.8, .01, Integer.MIN_VALUE)),
        "low floor finite");
    Random random = new Random(0x162_20261101L);
    for (int i = 0; i < 300_000; i++) {
      double slope = random.nextDouble() * 40 - 2;
      double catchment = random.nextDouble() * 1000000 - 2000;
      double width = random.nextDouble() * 500 - 10;
      need(same(WaterDynamics.speed(slope, catchment, width), baselineSpeed(slope, catchment, width)),
          "finite speed parity " + i);
    }
    for (int i = 0; i < 300_000; i++) {
      double base = random.nextDouble() * 2000 - 500;
      double continent = random.nextDouble() * 2 - 1;
      double noise = random.nextDouble() * 2 - 1;
      int floor = -512 + random.nextInt(513);
      need(same(WaterDynamics.oceanHeight(base, continent, noise, floor),
          baselineOcean(base, continent, noise, floor)), "finite ocean parity " + i);
    }
    System.out.println("WATER_HYDROLOGY_FINITE_PASS checks=" + checks
        + " speedParity=300000 oceanParity=300000 invalidCases=21 floorExtremes=3");
  }
  private WaterDynamicsHydrologyFiniteChecks() {}
}
