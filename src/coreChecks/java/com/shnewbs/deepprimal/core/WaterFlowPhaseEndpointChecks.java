package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Atlas-local flow phases are half-open even after double-to-float rounding. */
public final class WaterFlowPhaseEndpointChecks {
  private static final float[] FLOW_X = new float[16], FLOW_Z = new float[16];
  private static long checks, roundedEndpoints;

  static {
    for (int d = 0; d < 16; d++) {
      FLOW_X[d] = (float) Math.cos(d * Math.PI / 8);
      FLOW_Z[d] = (float) Math.sin(d * Math.PI / 8);
    }
  }

  private static void check(boolean ok, String description) {
    checks++;
    if (!ok) throw new AssertionError(description + " at assertion " + checks);
  }

  private static float legacy(int direction, int x, int z, boolean u) {
    if (direction < 0 || direction >= 16) return 0;
    double phase = u
        ? .5 * ((double) FLOW_Z[direction] * x - (double) FLOW_X[direction] * z)
        : .5 * ((double) FLOW_X[direction] * x + (double) FLOW_Z[direction] * z);
    return (float) (phase - Math.floor(phase));
  }

  private static void sample(int direction, int x, int z) {
    for (boolean u : new boolean[] {true, false}) {
      float original = legacy(direction, x, z, u);
      float actual = u ? WaterPatchMesher.flowUOffset(direction, x, z)
          : WaterPatchMesher.flowVOffset(direction, x, z);
      check(Float.isFinite(actual) && actual >= 0 && actual < 1,
          "flow phase escaped half-open atlas interval");
      if (original == 1.0f) {
        roundedEndpoints++;
        check(Float.floatToRawIntBits(actual) == Float.floatToRawIntBits(Math.nextDown(1.0f)),
            "rounded endpoint not corrected to preceding float");
      } else {
        check(Float.floatToRawIntBits(actual) == Float.floatToRawIntBits(original),
            "ordinary flow phase changed its historical bits");
      }
    }
  }

  public static void main(String[] args) {
    // Cardinal directions retain tiny float trig residuals; their negative phases
    // are positive fractions just below one and round to precisely 1.0f.
    check(legacy(4, 0, 1, true) == 1.0f, "U negative-control endpoint missing");
    check(legacy(8, 0, -1, false) == 1.0f, "V negative-control endpoint missing");
    sample(4, 0, 1);
    sample(8, 0, -1);
    for (int direction = 0; direction < 16; direction++)
      for (int x = -32; x <= 32; x++)
        for (int z = -32; z <= 32; z++) sample(direction, x, z);

    int[] extremes = {Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -30_000_000,
        -1, 0, 1, 30_000_000, Integer.MAX_VALUE - 1, Integer.MAX_VALUE};
    for (int direction = 0; direction < 16; direction++)
      for (int x : extremes)
        for (int z : extremes) sample(direction, x, z);

    Random random = new Random(0xD212F10FL);
    for (int i = 0; i < 100_000; i++) {
      int direction = random.nextInt(16);
      int x = random.nextInt(-30_000_000, 30_000_001);
      int z = random.nextInt(-30_000_000, 30_000_001);
      sample(direction, x, z);
    }
    for (int direction : new int[] {-1, 16, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
      check(WaterPatchMesher.flowUOffset(direction, 0, 0) == 0,
          "invalid direction U admission changed");
      check(WaterPatchMesher.flowVOffset(direction, 0, 0) == 0,
          "invalid direction V admission changed");
    }
    check(roundedEndpoints > 0, "negative control did not exercise rounding defect");
    System.out.println("WATER_FLOW_PHASE_ENDPOINT_PASS checks=" + checks
        + " roundedEndpoints=" + roundedEndpoints
        + " grid=67600 randomized=100000 ordinaryBitwiseParity=true");
  }

  private WaterFlowPhaseEndpointChecks() {}
}
