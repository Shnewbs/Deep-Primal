package com.shnewbs.deepprimal.core;

import java.util.Arrays;

/** Channel revision 2: Earth-scaled hydraulic geometry, graded canyons, meanders and braid bars. */
final class EarthRiverChecks {
  private EarthRiverChecks() {}

  static void run() {
    int[] classes = new int[6];
    double widest = 0;
    long transects = 0, wet = 0, flooded = 0, bars = 0, incised = 0, tunnelled = 0, benches = 0;
    double deepestCut = 0, legacyWidest = 0;
    for (long seed : new long[] {0, 73, 8675309}) {
      var t = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 2);
      var replay = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 2);
      var legacy = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 1);
      if (!t.column(-17, 31).equals(replay.column(-17, 31)))
        throw new AssertionError("Earth river seed replay");
      for (int cz = -1; cz <= 1; cz++)
        for (int cx = -1; cx <= 1; cx++) {
          var w = t.waterways(cx * 8192, cz * 8192);
          w.verify();
          var line = w.centerline();
          if (!line.equals(replay.waterways(cx * 8192, cz * 8192).centerline()))
            throw new AssertionError("Earth river centerline replay");
          for (var p : legacy.waterways(cx * 8192, cz * 8192).centerline())
            legacyWidest = Math.max(legacyWidest, p.radius() * 2);
          for (int i = 0; i < line.size(); i++) {
            var p = line.get(i);
            double width = p.radius() * 2;
            classes[width < 4 ? 0 : width < 12 ? 1 : width < 32 ? 2 : width < 64 ? 3 : width < 128 ? 4 : 5]++;
            widest = Math.max(widest, width);
            double x0 = Math.floor(p.x()), z0 = Math.floor(p.z());
            boolean canyon = false;
            if (i % 3 == 0 && p.radius() >= 4) {
              var centre = t.column(x0, z0);
              double cut = centre.wet() ? t.raw(x0, z0) - centre.water() : 0;
              if (cut > 16) {
                canyon = true;
                incised++;
                deepestCut = Math.max(deepestCut, cut);
                if (t.tunnel(x0, z0)) tunnelled++;
              }
            }
            if (!canyon && (i % 61 != 0 || p.radius() < 6)) continue;
            transects++;
            double reach = p.radius() + 60, previous = Double.NaN;
            for (double d = -reach; d <= reach; d++) {
              double x = Math.floor(p.x() + d), z = z0;
              var c = t.column(x, z);
              if (!c.wet()) {
                if (Math.abs(d) < p.radius() * .8 && p.radius() >= 8) bars++;
                if (canyon && Math.abs(c.ground() - previous) < .25 && c.ground() > p.stage() + 4) benches++;
                previous = c.ground();
                continue;
              }
              previous = Double.NaN;
              wet++;
              if (c.water() <= c.ground()) throw new AssertionError("Inverted earth river column " + x + "," + z);
              for (int dz = -1; dz <= 1; dz++)
                for (int dx = -1; dx <= 1; dx++) {
                  var n = t.column(x + dx, z + dz);
                  if (!n.wet() && n.ground() < Math.ceil(c.water()) - .001 - 1e-9) flooded++;
                }
            }
          }
        }
    }
    if (flooded != 0) throw new AssertionError("Earth river spills over " + flooded + " bank edges");
    if (classes[4] + classes[5] == 0 || classes[5] == 0 || widest > 230 || widest < legacyWidest * 4)
      throw new AssertionError("Great rivers missing or unbounded: " + Arrays.toString(classes) + " widest " + widest);
    if (incised < 20 || deepestCut < 32 || tunnelled != 0 || benches < incised)
      throw new AssertionError("Canyon reaches missing or roofed: incised " + incised + " deepest " + deepestCut
          + " tunnelled " + tunnelled + " benches " + benches);
    if (bars == 0) throw new AssertionError("No emergent braid bars");
    System.out.println("EARTH_RIVER_PASS widthClasses[<4,<12,<32,<64,<128,>=128]=" + Arrays.toString(classes)
        + " widest=" + widest + " legacyWidest=" + legacyWidest + " transects=" + transects + " wet=" + wet
        + " floodEdges=0 braidBarColumns=" + bars + " incisedCentres=" + incised + " deepestCut=" + deepestCut
        + " canyonBenches=" + benches + " tunnelledCanyons=0");
  }
}
