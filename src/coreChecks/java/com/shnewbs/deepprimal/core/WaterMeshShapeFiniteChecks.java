package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Shader-facing water geometry must not receive undefined phases from bad snapshots. */
public final class WaterMeshShapeFiniteChecks {
  private static long checks;

  private static void check(boolean value, String description) {
    checks++;
    if (!value) throw new AssertionError(description + " at assertion " + checks);
  }

  public static void main(String[] args) {
    double[] extreme = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
        Double.MAX_VALUE, -Double.MAX_VALUE, 1.0e308, -1.0e308, 0.0, -0.0, 16.0};
    for (double x : extreme) for (double y : extreme) for (double z : extreme) {
      float dx = WaterMeshShape.offsetX(x, y, z);
      float dz = WaterMeshShape.offsetZ(x, y, z);
      float v = WaterMeshShape.flowingV(x, y, z);
      check(Float.isFinite(dx) && Math.abs(dx) <= 0.057001f, "offset X finite/bounded");
      check(Float.isFinite(dz) && Math.abs(dz) <= 0.057001f, "offset Z finite/bounded");
      check(Float.isFinite(v) && v >= 0 && v < 1, "flow V finite/unit interval");
    }
    // A valid double phase just below one may round to 1.0f, which is
    // outside the half-open texture atlas interval [0, 1).
    for (int exponent = 25; exponent <= 53; exponent++) {
      double y = Math.scalb(1.0, -exponent);
      float v = WaterMeshShape.flowingV(0.0, y, 0.0);
      check(Float.isFinite(v) && v >= 0.0f && v < 1.0f,
          "float-rounded near-unit flow phase must remain wrapped");
    }
    check(WaterMeshShape.flowingV(16.0 - Math.scalb(1.0, -21), 1.0, 0.0) < 1.0f,
        "horizontal near-unit flow phase must remain wrapped");
    var random = new Random(0x210BEEF);
    for (int i = 0; i < 100_000; i++) {
      double x = random.nextDouble(-30_000_000, 30_000_000);
      double y = random.nextDouble(-4096, 4096);
      double z = random.nextDouble(-30_000_000, 30_000_000);
      double a = (x + 2 * z + y) * (Math.PI / 8.0);
      double b = (2 * x - z + 3 * y) * (Math.PI / 8.0);
      float oldX = (float) (0.045 * Math.sin(a) + 0.012 * Math.sin(b));
      float oldZ = (float) (0.045 * Math.cos(a) - 0.012 * Math.cos(b));
      double phase = 1.0 - y + (x + z) / 16.0;
      float oldV = (float) (phase - Math.floor(phase));
      check(Float.floatToRawIntBits(WaterMeshShape.offsetX(x, y, z))
          == Float.floatToRawIntBits(oldX), "legacy X parity");
      check(Float.floatToRawIntBits(WaterMeshShape.offsetZ(x, y, z))
          == Float.floatToRawIntBits(oldZ), "legacy Z parity");
      check(Float.floatToRawIntBits(WaterMeshShape.flowingV(x, y, z))
          == Float.floatToRawIntBits(oldV), "legacy flow phase parity");
    }
    // Deliberate overflow of intermediate phase arithmetic with finite inputs.
    check(WaterMeshShape.offsetX(Double.MAX_VALUE, 0, Double.MAX_VALUE) == 0,
        "extreme phase X must fail closed");
    check(WaterMeshShape.offsetZ(Double.MAX_VALUE, 0, Double.MAX_VALUE) == 0,
        "extreme phase Z must fail closed");
    check(WaterMeshShape.flowingV(Double.MAX_VALUE, 0, Double.MAX_VALUE) == 0,
        "extreme phase V must fail closed");
    System.out.println("WATER_MESH_FINITE_PASS checks=" + checks
        + " randomizedOrdinary=100000 extremeTriples=1000 legacyBitwiseParity=true");
  }

  private WaterMeshShapeFiniteChecks() {}
}
