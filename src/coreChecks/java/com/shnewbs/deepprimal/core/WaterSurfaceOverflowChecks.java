package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Finite endpoint interpolation must not produce NaN/Infinity in the presentation path. */
public final class WaterSurfaceOverflowChecks {
  private static long checks;

  private static void need(boolean valid, String message) {
    checks++;
    if (!valid) throw new AssertionError(message + " at check " + checks);
  }

  public static void main(String[] args) {
    var plain = new WaterSurfaceTransition(Float.MAX_VALUE, 0);
    plain.target(-Float.MAX_VALUE, 10);
    need(Float.isFinite(plain.at(10)), "finite initial interpolation at t=0");
    need(Float.isFinite(plain.at(60)), "finite initial interpolation at t=.5");
    need(Float.isFinite(plain.at(110)), "finite initial interpolation at t=1");
    plain.target(0f, 60);
    need(Float.isFinite(plain.at(75)), "finite retarget after extreme transition");

    var versioned = new WaterSurfaceTransition(Float.MAX_VALUE, 0);
    versioned.target(-Float.MAX_VALUE, 10, 1);
    need(Float.isFinite(versioned.at(60)), "versioned extreme interpolation");
    versioned.target(0f, 60, 2);
    need(Float.isFinite(versioned.at(75)), "versioned retarget after extreme transition");

    var random = new Random(0x142C0FFEEFL);
    for (int i = 0; i < 100_000; i++) {
      float a = (float) ((double) Float.MAX_VALUE * (random.nextInt(1000) + 1) / 1000d);
      float b = (float) ((double) Float.MAX_VALUE * (random.nextInt(1000) + 1) / 1000d);
      if (random.nextBoolean()) a = -a;
      if (random.nextBoolean()) b = -b;
      var actual = new WaterSurfaceTransition(a, 0);
      if ((i & 1) == 0) actual.target(b, 0, i + 1);
      else actual.target(b, 0);
      for (int j = 0; j <= 10; j++) {
        float t = j / 10f;
        float got = actual.at(j * 10);
        float expected = (float) ((double) a + ((double) b - a) * t);
        need(Float.isFinite(got), "finite endpoint interpolation must remain finite");
        double scale = Math.max(Math.abs((double) a), Math.abs((double) b));
        need(Math.abs((double) got - expected) <= scale * 2e-6,
            "finite endpoint interpolation differs from double oracle");
      }
    }
    System.out.println("WATER_SURFACE_OVERFLOW_PASS assertions=" + checks
        + " randomizedScenarios=100000 randomizedSamples=1100000");
  }

  private WaterSurfaceOverflowChecks() {}
}
