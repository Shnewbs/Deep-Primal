package com.shnewbs.deepprimal.core;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Channel revision 4: oxbow lakes. Each cut-off loop must hold still, contained water at its
 * planned level, sit near its parent river's stage, and leave revision 3 terrain untouched
 * everywhere outside its footprint.
 */
final class OxbowChecks {
  private OxbowChecks() {}

  static void run() {
    int lakes = 0;
    long wet = 0, flooded = 0, grid = 0, changed = 0;
    double lowestRim = Double.POSITIVE_INFINITY, widestGap = 0;
    for (long seed : new long[] {0, 73, 8675309}) {
      var t = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 4);
      var replay = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 4);
      var deltas = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 3);
      var all = new ArrayList<WatershedRegion.OxbowProbe>();
      for (int cz = -1; cz <= 1; cz++)
        for (int cx = -1; cx <= 1; cx++) {
          var w = t.waterways(cx * 8192, cz * 8192);
          w.verify();
          var probes = w.oxbowProbes();
          if (!probes.equals(replay.waterways(cx * 8192, cz * 8192).oxbowProbes()))
            throw new AssertionError("Oxbow plan replay");
          if (!deltas.waterways(cx * 8192, cz * 8192).oxbowProbes().isEmpty())
            throw new AssertionError("Revision 3 planned oxbows");
          all.addAll(probes);
        }
      for (var o : all) {
        lakes++;
        widestGap = Math.max(widestGap, Math.abs(o.level() - o.riverStage()));
        var mid = t.column(Math.floor(o.x()), Math.floor(o.z()));
        if (!mid.wet() || mid.water() != o.level() || mid.flowX() != 0 || mid.flowZ() != 0)
          throw new AssertionError("Oxbow arc not still water at " + o);
        double ext = o.radius() * 2 + o.halfWidth() + 8;
        for (double z = Math.floor(o.z() - ext); z <= o.z() + ext; z++)
          for (double x = Math.floor(o.x() - ext); x <= o.x() + ext; x++) {
            var c = t.column(x, z);
            if (!c.wet() || c.water() != o.level()) continue;
            wet++;
            if (c.water() <= c.ground()) throw new AssertionError("Inverted oxbow column " + x + "," + z);
            for (int dz = -1; dz <= 1; dz++)
              for (int dx = -1; dx <= 1; dx++) {
                var n = t.column(x + dx, z + dz);
                if (n.wet()) continue;
                if (n.ground() < Math.ceil(c.water()) - .001 - 1e-9) flooded++;
                lowestRim = Math.min(lowestRim, n.ground() - c.water());
              }
          }
      }
      for (double z = -6000; z <= 6000; z += 197)
        for (double x = -6000; x <= 6000; x += 181) {
          grid++;
          var p = deltas.column(x, z);
          var q = t.column(x, z);
          if (p.ground() == q.ground() && Objects.equals(p.water(), q.water())) continue;
          boolean inside = false;
          for (var o : all)
            if (Math.hypot(x - o.x(), z - o.z()) < o.radius() * 2 + o.halfWidth() + 10) inside = true;
          if (!inside) throw new AssertionError("Revision 4 changed terrain outside oxbows at " + x + "," + z);
          changed++;
        }
    }
    if (flooded != 0) throw new AssertionError("Oxbow lakes spill over " + flooded + " bank edges");
    if (lakes < 9) throw new AssertionError("Too few oxbow lakes: " + lakes);
    if (widestGap > 3) throw new AssertionError("Oxbow detached from river stage by " + widestGap);
    System.out.println("OXBOW_PASS lakes=" + lakes + " wet=" + wet + " floodEdges=0 lowestRimAboveWater="
        + lowestRim + " maxStageGap=" + widestGap + " replayGrid=" + grid + " changedInsideFootprints=" + changed);
  }
}
