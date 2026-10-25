package com.shnewbs.deepprimal.core;

/**
 * Channel revision 3: delta fans. Revisions 0/1/2 must replay exactly, and a fan may only raise
 * low coastal ground into a contained distributary network that stays above sea level.
 */
final class DeltaChecks {
  private DeltaChecks() {}

  /** Frozen revision-2 fingerprints captured before delta work began. */
  private static final long[][] REVISION_TWO = {
    {0L, 0xe01bcdaeec5dcbb3L},
    {73L, 0x7d3ee65224b2359aL},
    {20261020L, 0x2bafc04ee1ebe957L}
  };

  static void run() {
    frozenRevisions();
    inventory();
    System.out.println("DELTA_CORE_PASS revision2Replay=true fansBounded=true distributariesContained=true");
  }

  /** Revision 2 must not move when revision 3 is added. */
  private static void frozenRevisions() {
    for (long[] expected : REVISION_TWO) {
      long hash = digest(expected[0]);
      if (hash != expected[1])
        throw new AssertionError(
            "Channel revision 2 changed for seed " + expected[0] + ": " + Long.toHexString(hash));
    }
  }

  private static long digest(long seed) {
    var t = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 2);
    long hash = 0xcbf29ce484222325L, wet = 0, cols = 0;
    for (int z = -3000; z <= 3000; z += 193)
      for (int x = -3000; x <= 3000; x += 193) {
        var s = t.column(x, z);
        hash = mix(hash, Double.doubleToLongBits(s.ground()));
        hash = mix(hash, Double.doubleToLongBits(s.water()));
        hash = mix(hash, Double.doubleToLongBits(s.sediment()));
        hash = mix(hash, Double.doubleToLongBits(s.speed()));
        hash = mix(hash, Double.doubleToLongBits(t.raw(x, z)));
        cols++;
        if (s.wet()) wet++;
      }
    if (cols != 1024 || wet == 0) throw new AssertionError("Revision-2 fingerprint grid");
    return mix(hash, t.waterways(0, 0).statistics().segments());
  }

  private static void inventory() {
    int fans = 0, endpoints = 0, wetEndpoints = 0, deposited = 0, changedOutside = 0;
    double deepest = 0, widest = 0, longest = 0;
    for (long seed : new long[] {0, 73, 20261020}) {
      var earth = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 3);
      var replay = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 3);
      var previous = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 2);
      if (!earth.column(-17, 31).equals(replay.column(-17, 31)))
        throw new AssertionError("Delta seed replay");
      for (int cz = -1; cz <= 1; cz++)
        for (int cx = -1; cx <= 1; cx++) {
          var region = earth.waterways(cx * 8192, cz * 8192);
          var probes = region.deltaProbes();
          if (!probes.equals(replay.waterways(cx * 8192, cz * 8192).deltaProbes()))
            throw new AssertionError("Delta probe replay");
          if (!previous.waterways(cx * 8192, cz * 8192).deltaProbes().isEmpty())
            throw new AssertionError("Channel revision 2 produced a delta fan");
          for (var d : probes) {
            fans++;
            widest = Math.max(widest, d.halfWidth());
            longest = Math.max(longest, d.reach());
            if (d.branches() != 3) throw new AssertionError("Distributary branch count");
            if (d.halfWidth() > 220 || d.reach() > 180 || d.trunkRadius() < 8)
              throw new AssertionError("Unbounded or undersized delta fan " + d);
            if (d.stage() < TerrainEngine.SEA || d.stage() > TerrainEngine.SEA + 6)
              throw new AssertionError("Delta fan not on a low coastal plain " + d);
            // The original five near-trunk probes are all wet in this corpus. Survey
            // the two-dimensional fan as well, including banks beyond the trunk.
            // Keep those original water/ground guards below and the same minimum
            // deposition count; do not mistake trunk coverage for dry-bank coverage.
            for (int along = 1; along <= 9; along++)
              for (int across = -10; across <= 10; across++) {
                double u = along / 10.0, v = across * .09;
                double x = d.mouthX() - d.seawardX() * u * d.reach()
                    - d.seawardZ() * v * d.halfWidth();
                double z = d.mouthZ() - d.seawardZ() * u * d.reach()
                    + d.seawardX() * v * d.halfWidth();
                var now = earth.column(x, z);
                var before = previous.column(x, z);
                if (!now.wet() && now.ground() >= TerrainEngine.SEA + .2
                    && now.ground() > before.ground() + .05) deposited++;
              }
            // The fan is a depositional plain: low ground inside it is raised, never lowered.
            double spread = Math.max(d.reach(), d.halfWidth()) * .5;
            for (int n = -2; n <= 2; n++) {
              double x = d.mouthX() - d.seawardX() * spread * .4 + -d.seawardZ() * n * spread * .3;
              double z = d.mouthZ() - d.seawardZ() * spread * .4 + d.seawardX() * n * spread * .3;
              var now = earth.column(x, z);
              var before = previous.column(x, z);
              if (now.wet() != before.wet() && before.wet())
                throw new AssertionError("Delta fan removed generated water at " + x + "," + z);
              if (!now.wet() && now.ground() < before.ground() - 1e-9)
                throw new AssertionError("Delta fan lowered dry ground at " + x + "," + z);
              if (!now.wet() && now.ground() >= TerrainEngine.SEA + .2
                  && now.ground() > before.ground() + .05) deposited++;
            }
            // Beyond the fan, revision 3 must be identical to revision 2.
            double outside = Math.max(d.reach(), d.halfWidth()) * 1.5 + 40;
            for (int n = 0; n < 8; n++) {
              double angle = n * Math.PI / 4;
              double x = d.mouthX() + Math.cos(angle) * outside;
              double z = d.mouthZ() + Math.sin(angle) * outside;
              if (!earth.column(x, z).equals(previous.column(x, z))) changedOutside++;
            }
          }
          for (var p : region.deltaCenterline()) {
            endpoints++;
            double x = Math.floor(p.x()), z = Math.floor(p.z());
            var c = earth.column(x, z);
            if (!c.wet()) continue;
            wetEndpoints++;
            deepest = Math.max(deepest, c.water() - c.ground());
            if (c.water() < TerrainEngine.SEA - 1e-9)
              throw new AssertionError("Delta channel below sea level at " + x + "," + z);
            if (c.water() - c.ground() > 8.5)
              throw new AssertionError("Delta channel trench " + (c.water() - c.ground()));
            for (int dz = -1; dz <= 1; dz++)
              for (int dx = -1; dx <= 1; dx++) {
                var n = earth.column(x + dx, z + dz);
                if (!n.wet() && n.ground() < Math.ceil(c.water()) - .001 - 1e-9)
                  throw new AssertionError("Delta channel spills at " + (x + dx) + "," + (z + dz));
              }
          }
        }
    }
    if (fans < 6) throw new AssertionError("Too few great-river delta fans: " + fans);
    if (endpoints == 0 || wetEndpoints != endpoints)
      throw new AssertionError("Dry distributary endpoints " + wetEndpoints + "/" + endpoints);
    if (deposited < fans * 4) throw new AssertionError("No depositional delta plain: " + deposited);
    if (changedOutside != 0)
      throw new AssertionError("Delta fan changed land outside its footprint: " + changedOutside);
    try {
      new TerrainEngine(7, 3, TerrainOptions.DEFAULT, 3);
      throw new AssertionError("Delta revision accepted non-V7 geography");
    } catch (IllegalArgumentException expected) {
      // Revision 3 remains V7-only.
    }
    System.out.printf(
        "DELTA_REVISION_PASS fans=%d endpoints=%d depositedColumns=%d widest=%.1f longest=%.1f"
            + " deepest=%.2f outsideChanges=0%n",
        fans, endpoints, deposited, widest, longest, deepest);
  }

  private static long mix(long hash, long value) {
    hash ^= value;
    hash *= 0x100000001b3L;
    return hash ^ (hash >>> 29);
  }
}
