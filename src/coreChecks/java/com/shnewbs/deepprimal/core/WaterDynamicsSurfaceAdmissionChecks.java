package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Reject malformed worldgen waterlines without altering any valid saved-world quantization. */
public final class WaterDynamicsSurfaceAdmissionChecks {
  private static int checks;

  private static void require(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message);
  }

  private static void rejected(double waterline) {
    boolean yRejected = false;
    try { WaterDynamics.surfaceY(waterline); }
    catch (IllegalArgumentException expected) { yRejected = true; }
    require(yRejected, "invalid surfaceY admitted waterline=" + waterline);
    boolean levelRejected = false;
    try { WaterDynamics.surfaceLevel(waterline); }
    catch (IllegalArgumentException expected) { levelRejected = true; }
    require(levelRejected, "invalid surfaceLevel admitted waterline=" + waterline);
  }

  private static void unchanged(double waterline) {
    double target = waterline - 1.0 / 9;
    int expectedY = (int) Math.floor(target);
    int expectedLevel = 8 - Math.clamp(Math.round((target - Math.floor(target)) * 9), 1, 8);
    require(WaterDynamics.surfaceY(waterline) == expectedY, "surfaceY parity " + waterline);
    require(WaterDynamics.surfaceLevel(waterline) == expectedLevel, "surfaceLevel parity " + waterline);
    require(expectedLevel >= 0 && expectedLevel <= 7, "native water level bounds");
  }

  public static void main(String[] args) {
    for (double invalid : new double[] {
        Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
        Double.MAX_VALUE, -Double.MAX_VALUE,
        (double) Integer.MAX_VALUE + 2, (double) Integer.MIN_VALUE
    }) rejected(invalid);
    for (int y = -512; y <= 2048; y++) {
      for (int part = 0; part <= 9; part++) unchanged(y + part / 9.0);
    }
    for (double valid : new double[] {
        (double) Integer.MIN_VALUE + 1, (double) Integer.MIN_VALUE + 1.5,
        (double) Integer.MAX_VALUE, (double) Integer.MAX_VALUE + .5,
        (double) Integer.MAX_VALUE + 1,
        -0.0, 0.0, Math.nextUp(0.0), Math.nextDown(0.0)
    }) unchanged(valid);
    var random = new Random(0x5EED144L);
    for (int i = 0; i < 250_000; i++) {
      double value = (random.nextInt(65_536) - 32_768) + random.nextDouble();
      unchanged(value);
    }
    System.out.println("WATERLINE_ADMISSION_PASS checks=" + checks
        + " invalidWaterlines=7 finiteParity=275619");
  }

  private WaterDynamicsSurfaceAdmissionChecks() {}
}
