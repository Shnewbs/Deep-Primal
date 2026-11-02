package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Focused guard against malformed/zero-size view telemetry promoting expensive horizon detail. */
public final class HorizonViewSafetyChecks {
  public static void main(String[] args) {
    int checks = 0;
    for (int height : new int[] {0, -1, Integer.MIN_VALUE}) {
      for (int tile = -3; tile <= 3; tile++) {
        check(HorizonView.create(0, 0, height, 70, 1).detail(tile, tile) == 2,
            "nonpositive viewport must be coarsest");
        checks++;
      }
    }
    for (double invalid : new double[] {0, -1, 180, 181, Double.NaN,
        Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
      check(HorizonView.create(0, 0, 1080, invalid, 1).detail(0, 0) == 2,
          "invalid FOV must be coarsest");
      checks++;
    }
    for (double invalid : new double[] {0, -0.0, -Double.MIN_VALUE, -1, -Double.MAX_VALUE,
        Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
      var view = HorizonView.create(0, 0, 1080, 70, invalid);
      check(view.detail(0, 0) == 2 && view.fineSquared() == 0 && view.mediumSquared() == 0,
          "invalid workload must be coarsest");
      checks++;
    }
    for (double invalid : new double[] {Double.NaN, Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY}) {
      var x = HorizonView.create(invalid, 0, 1080, 70, 1);
      var z = HorizonView.create(0, invalid, 1080, 70, 1);
      check(x.detail(0, 0) == 2 && Double.isFinite(x.x()) && x.fineSquared() == 0,
          "invalid camera X must be coarsest with finite origin");
      check(z.detail(0, 0) == 2 && Double.isFinite(z.z()) && z.mediumSquared() == 0,
          "invalid camera Z must be coarsest with finite origin");
      checks += 2;
    }
    Random random = new Random(20261031L);
    int[] tiers = new int[3];
    for (int i = 0; i < 100_000; i++) {
      double x = random.nextDouble() * 50000 - 25000;
      double z = random.nextDouble() * 50000 - 25000;
      int height = random.nextInt(4000) + 1;
      double fov = 1 + random.nextDouble() * 178;
      double workload = Math.max(Double.MIN_VALUE, random.nextDouble() * 1.5);
      int tx = random.nextInt(200) - 100, tz = random.nextInt(200) - 100;
      HorizonView view = HorizonView.create(x, z, height, fov, workload);
      double projection = Math.clamp(height / 720.0 * Math.tan(Math.toRadians(35))
          / Math.tan(Math.toRadians(fov / 2)), .5, 3);
      double scale = Math.max(.25, projection * projection * Math.clamp(workload, .125, 1));
      double dx = Math.max(0, Math.abs((tx + .5) * FarTerrain.TILE_BLOCKS - x)
          - FarTerrain.TILE_BLOCKS / 2.0);
      double dz = Math.max(0, Math.abs((tz + .5) * FarTerrain.TILE_BLOCKS - z)
          - FarTerrain.TILE_BLOCKS / 2.0);
      double distance = dx * dx + dz * dz;
      int expected = distance < 1024.0 * 1024 * scale ? 0
          : distance < 2560.0 * 2560 * scale ? 1 : 2;
      check(view.detail(tx, tz) == expected, "valid-view detail changed");
      tiers[expected]++;
      checks++;
    }
    check(tiers[0] >= 100 && tiers[1] >= 100 && tiers[2] >= 100,
        "randomized parity must cover every LOD tier");
    checks++;
    System.out.println("HORIZON_VIEW_SAFETY_PASS checks=" + checks
        + " tiers=" + java.util.Arrays.toString(tiers));
  }

  private static void check(boolean condition, String reason) {
    if (!condition) throw new AssertionError(reason);
  }
  private HorizonViewSafetyChecks() {}
}
