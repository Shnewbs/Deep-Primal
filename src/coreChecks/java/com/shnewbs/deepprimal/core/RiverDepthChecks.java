package com.shnewbs.deepprimal.core;

/** Channel revision 6 deepens main-river beds without changing route planning or lake geometry. */
final class RiverDepthChecks {
  private RiverDepthChecks() {}

  static void run() {
    long sampled = 0, deepened = 0, unchangedExterior = 0, unchangedLakes = 0;
    double deepestGain = 0;
    for (long seed : new long[] {0, 73, 20261020}) {
      var previous = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 5);
      var deep = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 6);
      var replay = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 6);
      if (!deep.column(-17, 31).equals(replay.column(-17, 31)))
        throw new AssertionError("Deep-channel revision replay");
      for (int cz = -1; cz <= 1; cz++)
        for (int cx = -1; cx <= 1; cx++) {
          var oldRegion = previous.waterways(cx * 8192, cz * 8192);
          var newRegion = deep.waterways(cx * 8192, cz * 8192);
          var repeatRegion = replay.waterways(cx * 8192, cz * 8192);
          if (!oldRegion.centerline().equals(newRegion.centerline())
              || !oldRegion.lakeProbes().equals(newRegion.lakeProbes())
              || !oldRegion.oxbowProbes().equals(newRegion.oxbowProbes())
              || !oldRegion.deltaProbes().equals(newRegion.deltaProbes())
              || !oldRegion.distributaryProbes().equals(newRegion.distributaryProbes())
              || oldRegion.statistics().segments() != newRegion.statistics().segments())
            throw new AssertionError("Deep-channel revision changed route/lake planning");
          if (!newRegion.centerline().equals(repeatRegion.centerline())
              || !newRegion.deltaProbes().equals(repeatRegion.deltaProbes()))
            throw new AssertionError("Deep-channel planning replay");

          for (int i = 0; i + 1 < newRegion.centerline().size(); i += 8) {
            var p = newRegion.centerline().get(i);
            var oldColumn = previous.column(p.x(), p.z());
            var newColumn = deep.column(p.x(), p.z());
            if (oldColumn.wet() != newColumn.wet())
              throw new AssertionError("Deep-channel revision changed water coverage");
            if (oldColumn.wet()) {
              if (oldColumn.water() != newColumn.water())
                throw new AssertionError("Deep-channel revision changed water stage");
              sampled++;
              double gain = (newColumn.water() - newColumn.ground())
                  - (oldColumn.water() - oldColumn.ground());
              if (p.radius() >= 5 && gain > .75) deepened++;
              deepestGain = Math.max(deepestGain, gain);

              var next = newRegion.centerline().get(i + 1);
              double dx = next.x() - p.x(), dz = next.z() - p.z(), length = Math.hypot(dx, dz);
              double outside = p.radius() + 40;
              double outsideX = p.x() - dz / length * outside;
              double outsideZ = p.z() + dx / length * outside;
              var oldBank = previous.column(outsideX, outsideZ);
              var newBank = deep.column(outsideX, outsideZ);
              if (oldBank.wet() != newBank.wet()
                  || !oldBank.wet() && oldBank.ground() != newBank.ground()
                  || oldBank.wet() && oldBank.water() != newBank.water())
                throw new AssertionError("Deep-channel revision changed exterior terrain at "
                    + outsideX + "," + outsideZ);
              if (!oldBank.wet()) unchangedExterior++;
            }
          }
          for (var lake : newRegion.lakeProbes()) {
            var oldColumn = previous.column(lake.x(), lake.z());
            var newColumn = deep.column(lake.x(), lake.z());
            if (!oldColumn.equals(newColumn))
              throw new AssertionError("Deep-channel revision changed lake surface");
            unchangedLakes++;
          }
          for (var oxbow : newRegion.oxbowProbes()) {
            var oldColumn = previous.column(oxbow.x(), oxbow.z());
            var newColumn = deep.column(oxbow.x(), oxbow.z());
            if (!oldColumn.equals(newColumn))
              throw new AssertionError("Deep-channel revision changed oxbow surface");
            unchangedLakes++;
          }
        }
    }
    if (sampled < 20 || deepened < 10 || deepestGain < 1 || unchangedExterior < 20)
      throw new AssertionError("Deep-channel cross-sections missing: wet=" + sampled
          + " deeper=" + deepened + " maxGain=" + deepestGain
          + " unchangedExterior=" + unchangedExterior);
    System.out.println("RIVER_DEPTH_REVISION_PASS wetCentres=" + sampled + " deepenedCentres="
        + deepened + " maxDepthGain=" + deepestGain + " unchangedExterior="
        + unchangedExterior + " unchangedLakeSamples=" + unchangedLakes);
  }
}
