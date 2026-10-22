package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.baseline.Alpha23TerrainEngine;
import java.util.Random;

/** Compare every tested Y with the published, uncached cave formula. */
final class CaveColumnChecks {
  static void run() {
    var random = new Random(20261022);
    int samples = 0, cavities = 0;
    for (long seed : new long[] {0, 20261020, -914, Long.MIN_VALUE, Long.MAX_VALUE}) {
      var baseline = new Alpha23TerrainEngine(seed);
      var noise = new TerrainNoise(seed);
      for (int n = 0; n < 240; n++) {
        int x = random.nextInt(-30_000_000, 30_000_000),
            z = random.nextInt(-30_000_000, 30_000_000),
            min = n % 2 == 0 ? -128 : -384,
            max = min + 768 - 1;
        double surface = random.nextInt(-100, 420) + random.nextInt(16) / 16.0;
        var cached = new CaveColumn(noise, x, z, surface, min, max);
        for (int y = min - 1; y <= max + 1; y++) {
          boolean expected = y <= max && baseline.cave(x, y, z, surface, min);
          if (cached.contains(y) != expected)
            throw new AssertionError("Cave column changed " + seed + " " + x + "," + y + "," + z);
          if (expected) cavities++;
          samples++;
        }
      }
      var oversized = new CaveColumn(noise, -54, 62, 500, -8192, 1000);
      for (int y = -8192; y <= 200; y++) {
        if (oversized.contains(y) != baseline.cave(-54, y, 62, 500, -8192))
          throw new AssertionError("Bounded fallback changed cave field");
        samples++;
      }
    }
    if (cavities < 1000) throw new AssertionError("Cave differential lacks cavities");
    System.out.println("CAVE_COLUMN_PASS samples=" + samples + " cavities=" + cavities);
  }
}
