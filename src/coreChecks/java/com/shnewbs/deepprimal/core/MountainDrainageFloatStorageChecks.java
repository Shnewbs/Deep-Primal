package com.shnewbs.deepprimal.core;

import java.util.function.DoubleBinaryOperator;

/** Ensure finite terrain/sea-level inputs cannot overflow the cached float waterline. */
public final class MountainDrainageFloatStorageChecks {
  private static long assertions;

  private static void check(boolean ok, String label) {
    assertions++;
    if (!ok) throw new AssertionError(label);
  }

  private static void rejects(Runnable action, String label) {
    boolean rejected = false;
    try {
      action.run();
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    check(rejected, label);
  }

  public static void run() {
    for (double bad : new double[] {
        Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
        (double) Float.MAX_VALUE * 2, -(double) Float.MAX_VALUE * 2
    }) {
      rejects(() -> new MountainDrainage((x, z) -> 0, bad),
          "Unrepresentable sea level admitted: " + bad);
      rejects(() -> new MountainDrainage((x, z) -> bad, 96).region(0, 0),
          "Unrepresentable terrain admitted: " + bad);
    }

    DoubleBinaryOperator land = (x, z) -> x < 300 && z < 300 ? 84 : 160;
    var network = new MountainDrainage(land, 96, true);
    var region = network.region(0, 0);
    check(region.nodeCount() > 100, "Finite region unexpectedly empty");
    for (int k = 0; k < region.nodeCount(); k++) {
      int n = region.node(k);
      check(Double.isFinite(region.water(n)), "Nonfinite cached waterline");
    }
    for (int x = 0; x <= 256; x += 16) {
      for (int z = 0; z <= 256; z += 16) {
        var sample = network.sample(x, z, land.applyAsDouble(x, z));
        check(Double.isFinite(sample.height()) && Double.isFinite(sample.waterLevel())
            && Double.isFinite(sample.flowX()) && Double.isFinite(sample.flowZ())
            && Double.isFinite(sample.speed()), "Nonfinite finite-land sample");
      }
    }
    var maximumTerrain = new MountainDrainage((x, z) -> Float.MAX_VALUE, 96);
    check(maximumTerrain.region(0, 0).nodeCount() == 0,
        "Representable maximum terrain unexpectedly rejected");
    var maximumSea = new MountainDrainage((x, z) -> 0, Float.MAX_VALUE);
    var seaRegion = maximumSea.region(0, 0);
    check(seaRegion.nodeCount() > 0, "Representable maximum sea level rejected");
    check(Double.isFinite(seaRegion.water(seaRegion.node(0))),
        "Maximum representable sea level stored as infinity");
    System.out.println("DRAINAGE_FLOAT_STORAGE_PASS assertions=" + assertions);
  }

  private MountainDrainageFloatStorageChecks() {}
}
