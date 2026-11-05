package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Independent admission checks for native aquifer's exclusive water top Y. */
public final class WaterDynamicsNativeTopChecks {
  private static long assertions;

  private static void require(boolean value, String label) {
    assertions++;
    if (!value) throw new AssertionError(label);
  }

  private static void rejects(double value) {
    boolean rejected = false;
    try { WaterDynamics.surfaceTopY(value); }
    catch (IllegalArgumentException expected) { rejected = true; }
    require(rejected, "native top admitted invalid Y " + value);
  }

  private static void accepts(double value) {
    double target = value - 1.0 / 9;
    // Oracle uses long arithmetic, not a potentially overflowing native int increment.
    long expected = (long) Math.floor(target) + 1L;
    require(expected >= Integer.MIN_VALUE && expected <= Integer.MAX_VALUE, "bad test input");
    require(WaterDynamics.surfaceTopY(value) == expected, "top Y mismatch " + value);
    require(WaterDynamics.surfaceTopY(value) == WaterDynamics.surfaceY(value) + 1L,
        "top disagrees with base Y " + value);
  }

  public static void main(String[] args) {
    for (double invalid : new double[] {
        Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
        Double.MAX_VALUE, -Double.MAX_VALUE,
        (double) Integer.MAX_VALUE + 0.5,
        (double) Integer.MAX_VALUE + 1.0,
        (double) Integer.MAX_VALUE + 1.5,
        (double) Integer.MIN_VALUE,
        (double) Integer.MIN_VALUE - 1.0
    }) rejects(invalid);
    for (double valid : new double[] {
        (double) Integer.MAX_VALUE - 1.0,
        (double) Integer.MAX_VALUE,
        (double) Integer.MIN_VALUE + 1.0,
        (double) Integer.MIN_VALUE + 1.5,
        -1.0, -0.0, 0.0, Math.nextUp(0.0), Math.nextDown(0.0)
    }) accepts(valid);
    Random random = new Random(0xD1445EEDL);
    for (int i = 0; i < 300_000; i++) {
      double value = (random.nextInt(1_000_000) - 500_000) + random.nextDouble();
      accepts(value);
    }
    System.out.println("NATIVE_TOP_PASS assertions=" + assertions + " random=300000 invalid=10");
  }

  private WaterDynamicsNativeTopChecks() {}
}
