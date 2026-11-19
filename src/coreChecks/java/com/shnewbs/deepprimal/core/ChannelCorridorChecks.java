package com.shnewbs.deepprimal.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleBinaryOperator;

public final class ChannelCorridorChecks {
  public static void main(String[] args) {
    var straight = new ChannelCorridor.Curve(0, 0, 32. / 3, 0, 64. / 3, 0, 32, 0);
    var flat = ChannelCorridor.fit((x, z) -> 120 - x / 8, straight, 32);
    for (int n = 0; n <= 128; n++) {
      if (flat.offset(n / 128.) != 0) throw new AssertionError("A featureless slope acquired a bend");
    }
    AtomicInteger calls = new AtomicInteger();
    DoubleBinaryOperator valley = (x, z) -> 180 - x / 8 + Math.pow(z - 4 * Math.sin(x * Math.PI / 32), 2);
    var fit = ChannelCorridor.fit((x, z) -> {
      calls.incrementAndGet();
      return valley.applyAsDouble(x, z);
    }, straight, 32);
    if (fit.offset(0) != 0 || fit.offset(1) != 0
        || Math.abs(fit.offset(.00001)) > 1e-6 || Math.abs(fit.offset(.99999)) > 1e-6
        || calls.get() > 12) throw new AssertionError("Refinement moved a knot/tangent or exceeded its sample budget");
    double previous = 0;
    for (int n = 1; n <= 128; n++) {
      double t = n / 128., offset = fit.offset(t);
      if (offset < 0 || offset > fit.middle() + 1e-9 || (n <= 64 ? offset < previous : offset > previous))
        throw new AssertionError("Corridor interpolation overshot measured terrain");
      previous = offset;
    }
    if (valley.applyAsDouble(16, fit.offset(.5)) >= valley.applyAsDouble(16, 0) - 12)
      throw new AssertionError("Measured terrain trough was ignored");

    int reaches = 0, shifted = 0, stations = 0, lowered = 0;
    double savedCut = 0;
    for (long seed : new long[] {0, 1, 73, 20261020, -987654321}) {
      var before = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 11);
      var after = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 12);
      var region = after.waterways(0, 0);
      region.verify();
      var old = before.waterways(0, 0).centerline();
      var line = region.centerline();
      if (line.size() != old.size() || !before.waterways(0, 0).lakeProbes().equals(region.lakeProbes()))
        throw new AssertionError("Sub-grid route changed the drainage graph or basins");
      reaches += line.size() / 16;
      for (int i = 0; i < line.size(); i++) {
        var a = old.get(i);
        var b = line.get(i);
        if (a.stage() != b.stage() || a.radius() != b.radius() || a.area() != b.area() || a.source() != b.source())
          throw new AssertionError("Sub-grid route changed a reach's stage or cross-section");
        double shift = Math.hypot(a.x() - b.x(), a.z() - b.z());
        int station = (i + 1) / 2 % 8;
        if (shift > 6.000001 || (station == 0 && shift != 0))
          throw new AssertionError("Refinement exceeded its corridor or moved a graph knot");
        if ((i & 1) == 0 && shift > .001) {
          shifted++;
          // A roof can hide a real channel from the surface view. Check the bed before roofs.
          var wet = after.hydraulicColumn(b.x(), b.z());
          if (!wet.wet() || wet.ground() > Math.ceil(wet.water()) - 1.001 + 1e-8)
            throw new AssertionError("Refined reach lost a wet center: seed=" + seed + " index=" + i
                + " old=" + a + " refined=" + b + " sampled=" + wet);
          if ((station & 1) == 0) {
            stations++;
            double less = before.raw(a.x(), a.z()) - after.raw(b.x(), b.z());
            if (less > .04) lowered++;
            savedCut += less;
          }
        }
      }
    }
    if (shifted < 100 || stations < 100 || lowered != stations || savedCut < 100)
      throw new AssertionError("Missing measured improvement: shifted=" + shifted + " lowered=" + lowered
          + " stations=" + stations + " heightSaved=" + savedCut);
    System.out.printf(java.util.Locale.ROOT,
        "CHANNEL_CORRIDOR_PASS seeds=5 reaches=%d movedInternalSamples=%d lowerStations=%d meanUncutReduction=%.3f callsPerReach<=12%n",
        reaches, shifted, lowered, savedCut / stations);
  }

  private ChannelCorridorChecks() {}
}
