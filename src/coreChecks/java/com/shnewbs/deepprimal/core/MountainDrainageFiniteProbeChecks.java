package com.shnewbs.deepprimal.core;

import java.util.function.DoubleBinaryOperator;

/** Regression for nonfinite off-grid terrain samples during contained drainage planning. */
public final class MountainDrainageFiniteProbeChecks {
  private static long assertions;

  private static void check(boolean condition, String label) {
    assertions++;
    if (!condition) throw new AssertionError(label);
  }

  private static void rejects(Runnable action, String label) {
    boolean rejected = false;
    try {
      action.run();
    } catch (IllegalArgumentException exception) {
      rejected = true;
    }
    check(rejected, label);
  }

  public static void run() {
    DoubleBinaryOperator land = (x, z) ->
        84 + x * .065 + 100 * Math.exp(-Math.pow((x - 1300) / 180, 2))
            + 4 * Math.sin(z / 180);
    var valid = new MountainDrainage(land, 96, true);
    var region = valid.region(0, 0);
    check(region.nodeCount() > 100, "Missing contained routing baseline");
    for (int x = 0; x < 256; x += 4) {
      int z = 32 + x;
      double original = land.applyAsDouble(x, z);
      var a = valid.sample(x, z, original);
      check(a.equals(valid.sample(x, z, original)), "Finite drainage replay");
      check(Double.isFinite(a.height()) && Double.isFinite(a.waterLevel()),
          "Finite drainage waterline");
    }

    for (double bad : new double[] {
        Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
    }) {
      // Grid nodes remain valid; fractional bank/profile probes must fail explicitly
      // instead of publishing NaN or infinity in cached river surfaces.
      var offGrid = new MountainDrainage(
          (x, z) -> (x != Math.rint(x) || z != Math.rint(z))
              ? bad : land.applyAsDouble(x, z), 96, true);
      rejects(() -> offGrid.region(0, 0), "Nonfinite off-grid river probe admitted");
      rejects(() -> new MountainDrainage(land, bad),
          "Nonfinite sea level admitted");
    }
    rejects(() -> new MountainDrainage(null, 96), "Null terrain admitted");
    System.out.println("DRAINAGE_FINITE_PROBE_PASS assertions=" + assertions);
  }

  private MountainDrainageFiniteProbeChecks() {}
}
