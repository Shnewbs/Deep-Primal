package com.shnewbs.deepprimal.core;

import java.util.Random;

/** New terrain can cut intersecting valleys without rebuilding rims across another reach. */
final class TerrainRevisionChecks {
  static void run() {
    int changed = 0, rivers = 0, samples = 0;
    for (long seed : new long[] {20261020, -914, Long.MAX_VALUE}) {
      var v1 = new TerrainEngine(seed);
      var v2 = new TerrainEngine(seed, 2);
      var replay = new TerrainEngine(seed, 2);
      Random random = new Random(seed);
      for (int n = 0; n < 4000; n++) {
        int x = random.nextInt(6000) - 3000, z = random.nextInt(6000) - 3000;
        var a = v2.sample(x, z);
        if (!a.equals(replay.sample(x, z))) throw new AssertionError("V2 seed/cache replay");
        if (a.ground() > v2.raw(x, z) + 1e-9)
          throw new AssertionError("River raised a crossing rim");
        if (!Double.isFinite(a.ground()) || (a.wet() && a.water() <= a.ground()))
          throw new AssertionError("Invalid terrain/water surface");
        if (!a.equals(v1.sample(x, z))) changed++;
        if (a.wet() && a.water() > TerrainEngine.SEA + 1) rivers++;
        samples++;
      }
      v2.verify(0, 0);
      var saved = v2.sample(-240, -624);
      for (int region = 0; region < 14; region++) v2.verify(region * 8192., 0);
      if (!v2.sample(-240, -624).equals(saved)) throw new AssertionError("V2 eviction replay");
    }
    if (changed < 100 || rivers < 10) throw new AssertionError("V2 hydrology coverage");
    System.out.println(
        "TERRAIN_V2_CORE_PASS samples="
            + samples
            + " changed="
            + changed
            + " inlandWater="
            + rivers
            + " noRaisedRims=true deterministic=true");
  }
}
