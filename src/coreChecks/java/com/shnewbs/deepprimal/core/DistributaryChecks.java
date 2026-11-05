package com.shnewbs.deepprimal.core;

/**
 * Channel revision 5: distributary discharge budgets. Each fan splits its trunk discharge into
 * conserved branch shares and widens every branch by sqrt(share). Revisions 3 and 4 must replay
 * exactly, and a budget may only change water inside the fan footprint.
 */
final class DistributaryChecks {
  private DistributaryChecks() {}

  /** Frozen fingerprints for channel revisions 3 and 4 captured before revision-5 work began. */
  private static final long[][] FROZEN = {
    {3L, 0L, 0xb36cbb0e0952981fL},
    {3L, 73L, 0xae3c3d455f133775L},
    {3L, 20261020L, 0xe87ab03e934ed8daL},
    {4L, 0L, 0x92066ccbe2e6047dL},
    {4L, 73L, 0x5a93f362020c1e9fL},
    {4L, 20261020L, 0xd91bf3554721eec4L}
  };

  static void run() {
    frozenRevisions();
    budget();
    System.out.println("DISTRIBUTARY_CORE_PASS revisions3And4Replay=true sharesConserved=true");
  }

  /** Revisions 3 and 4 must not move when revision 5 is added. */
  private static void frozenRevisions() {
    for (long[] expected : FROZEN) {
      long hash = digest(expected[1], (int) expected[0]);
      if (hash != expected[2])
        throw new AssertionError(
            "Channel revision "
                + expected[0]
                + " changed for seed "
                + expected[1]
                + ": "
                + Long.toHexString(hash));
    }
  }

  private static long digest(long seed, int channelRevision) {
    var t = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, channelRevision);
    long hash = 0xcbf29ce484222325L;
    int cols = 0;
    for (int z = -3000; z <= 3000; z += 193)
      for (int x = -3000; x <= 3000; x += 193) {
        hash = column(t, x, z, hash);
        cols++;
      }
    if (cols != 1024) throw new AssertionError("Revision fingerprint grid");
    for (int cz = -1; cz <= 1; cz++)
      for (int cx = -1; cx <= 1; cx++) {
        var region = t.waterways(cx * 8192, cz * 8192);
        for (var d : region.deltaProbes()) {
          hash = mix(hash, Double.doubleToLongBits(d.mouthX()));
          hash = mix(hash, Double.doubleToLongBits(d.mouthZ()));
          hash = mix(hash, Double.doubleToLongBits(d.reach()));
          hash = mix(hash, Double.doubleToLongBits(d.halfWidth()));
          hash = mix(hash, Double.doubleToLongBits(d.trunkRadius()));
        }
        for (var p : region.deltaCenterline()) hash = column(t, p.x(), p.z(), hash);
        for (var o : region.oxbowProbes()) hash = column(t, o.x(), o.z(), hash);
        for (var p : region.lakeProbes()) hash = column(t, p.x(), p.z(), hash);
        hash = mix(hash, region.statistics().segments());
      }
    hash = mix(hash, t.waterways(0, 0).statistics().segments());
    return hash;
  }

  private static long column(TerrainEngine t, double x, double z, long hash) {
    var s = t.column(x, z);
    hash = mix(hash, Double.doubleToLongBits(s.ground()));
    hash = mix(hash, Double.doubleToLongBits(s.water()));
    hash = mix(hash, Double.doubleToLongBits(s.sediment()));
    hash = mix(hash, Double.doubleToLongBits(s.flowX()));
    hash = mix(hash, Double.doubleToLongBits(s.flowZ()));
    hash = mix(hash, Double.doubleToLongBits(s.speed()));
    return mix(hash, Double.doubleToLongBits(t.raw(x, z)));
  }

  private static void budget() {
    int fans = 0, branches = 0, wetSamples = 0, changedOutside = 0;
    double minShare = 1, maxShare = 0, widestRatio = Double.MAX_VALUE;
    for (long seed : new long[] {0, 73, 20261020}) {
      var previous = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 4);
      var budgeted = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 5);
      var replay = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT, 5);
      if (!budgeted.column(-17, 31).equals(replay.column(-17, 31)))
        throw new AssertionError("Distributary budget replay");
      for (int cz = -1; cz <= 1; cz++)
        for (int cx = -1; cx <= 1; cx++) {
          var region = budgeted.waterways(cx * 8192, cz * 8192);
          var probes = region.distributaryProbes();
          if (!probes.equals(replay.waterways(cx * 8192, cz * 8192).distributaryProbes()))
            throw new AssertionError("Distributary probe replay");
          var fans4 = previous.waterways(cx * 8192, cz * 8192).distributaryProbes();
          if (fans4.size() != probes.size())
            throw new AssertionError("Revision 4 branch count " + fans4.size());
          // Revision 4 is capacity-fair: equal shares and equal widths, no budget.
          for (int fan = 0; fan < fans4.size() / 3; fan++) {
            double share = fans4.get(fan * 3).share(), width = fans4.get(fan * 3).startRadius();
            for (int b = 0; b < 3; b++) {
              var p = fans4.get(fan * 3 + b);
              if (p.budgeted() || Math.abs(p.share() - 1 / 3.) > 1e-12)
                throw new AssertionError("Revision 4 took a discharge budget " + p);
              if (Math.abs(p.share() - share) > 1e-12
                  || Math.abs(p.startRadius() - width) > 1e-12)
                throw new AssertionError("Revision 4 branch widths diverged " + p);
            }
          }
          int expected = region.deltaProbes().size() * 3;
          if (probes.size() != expected)
            throw new AssertionError("Distributary probes " + probes.size() + "/" + expected);
          for (int fan = 0; fan < region.deltaProbes().size(); fan++) {
            fans++;
            var d = region.deltaProbes().get(fan);
            var group = new java.util.ArrayList<WatershedRegion.DistributaryProbe>();
            for (var p : probes) if (p.fan() == fan) group.add(p);
            if (group.size() != 3) throw new AssertionError("Fan branch count " + group.size());
            double sum = 0, widest = 0, narrowest = Double.MAX_VALUE;
            int largest = 0;
            for (int b = 0; b < group.size(); b++) {
              var p = group.get(b);
              branches++;
              if (!p.budgeted()) throw new AssertionError("Revision 5 branch is unbudgeted");
              if (p.share() < .12 || p.share() > .60)
                throw new AssertionError("Distributary share outside design " + p.share());
              sum += p.share();
              minShare = Math.min(minShare, p.share());
              maxShare = Math.max(maxShare, p.share());
              if (p.share() > group.get(largest).share()) largest = b;
              widest = Math.max(widest, p.startRadius());
              narrowest = Math.min(narrowest, p.startRadius());
            }
            if (Math.abs(sum - 1) > 1e-9)
              throw new AssertionError("Distributary shares do not close: " + sum);
            // The dominant branch carries the most water and is the widest.
            for (int b = 0; b < group.size(); b++)
              if (b != largest && group.get(b).share() >= group.get(largest).share())
                throw new AssertionError("Distributary share is not unique");
            if (Math.abs(group.get(largest).startRadius() - widest) > 1e-12)
              throw new AssertionError("Widest branch is not the dominant branch");
            double widestPlanned = Math.sqrt(group.get(largest).share() / minShareOf(group));
            widestRatio = Math.min(widestRatio, widest / narrowest);
            if (widest / narrowest < widestPlanned * .95)
              throw new AssertionError("Branch widths do not follow sqrt(share)");
            // The planned widths must order exactly with the shares.
            for (var a : group)
              for (var b : group)
                if (a.share() < b.share() && a.startRadius() >= b.startRadius())
                  throw new AssertionError("Branch width order disagrees with share");
            wetSamples += sampleFan(budgeted, group);
            // A budget may widen distributaries, never change land outside the fan.
            double outside = Math.max(d.reach(), d.halfWidth()) * 1.5 + 40;
            for (int n = 0; n < 8; n++) {
              double angle = n * Math.PI / 4;
              double x = d.mouthX() + Math.cos(angle) * outside;
              double z = d.mouthZ() + Math.sin(angle) * outside;
              if (!budgeted.column(x, z).equals(previous.column(x, z))) changedOutside++;
            }
            if (!budgeted.column(d.mouthX(), d.mouthZ()).wet())
              throw new AssertionError("Budgeted fan closed its trunk mouth " + d);
          }
        }
    }
    if (fans < 6) throw new AssertionError("Too few budgeted fans: " + fans);
    if (widestRatio < 1.05)
      throw new AssertionError("Discharge budget widened no branch: " + widestRatio);
    if (changedOutside != 0)
      throw new AssertionError("Distributary budget changed land outside fans: " + changedOutside);
    try {
      new TerrainEngine(7, 3, TerrainOptions.DEFAULT, 5);
      throw new AssertionError("Distributary budget accepted non-V7 geography");
    } catch (IllegalArgumentException expected) {
      // Revision 5 remains V7-only.
    }
    System.out.printf(
        "DISTRIBUTARY_PASS fans=%d branches=%d wetSamples=%d minShare=%.3f maxShare=%.3f"
            + " widestRatio=%.2f floodEdges=0 outsideChanges=0%n",
        fans, branches, wetSamples, minShare, maxShare, widestRatio);
  }

  /** Exactly one branch must take the largest share; ties are only legal at equal shares. */
  private static double minShareOf(java.util.List<WatershedRegion.DistributaryProbe> group) {
    double min = Double.MAX_VALUE;
    for (var p : group) min = Math.min(min, p.share());
    return min;
  }

  /**
   * Each branch must be a live channel at its midpoint and mouth: wet and no deeper than its
   * design, with seaward flow at the mouth where the branch is clear of its siblings.
   */
  private static int sampleFan(
      TerrainEngine t, java.util.List<WatershedRegion.DistributaryProbe> group) {
    int wet = 0;
    for (var p : group) {
      for (double[] point : new double[][] {{p.midX(), p.midZ()}, {p.endX(), p.endZ()}}) {
        var c = t.column(point[0], point[1]);
        if (!c.wet())
          throw new AssertionError("Dry distributary at " + point[0] + "," + point[1]);
        double depth = c.water() - c.ground();
        if (depth < .4 || depth > 8.5)
          throw new AssertionError("Distributary depth outside design: " + depth);
        for (int dz = -1; dz <= 1; dz++)
          for (int dx = -1; dx <= 1; dx++) {
            var n = t.column(point[0] + dx, point[1] + dz);
            if (!n.wet() && n.ground() < Math.ceil(c.water()) - .001 - 1e-9)
              throw new AssertionError(
                  "Distributary spills at " + (point[0] + dx) + "," + (point[1] + dz));
          }
        wet++;
      }
      double dx = p.endX() - p.midX(), dz = p.endZ() - p.midZ(), len = Math.hypot(dx, dz);
      if (len < 1) throw new AssertionError("Distributary reached zero length");
      var mouth = t.column(p.endX(), p.endZ());
      double seaward = (mouth.flowX() * dx + mouth.flowZ() * dz) / len;
      if (seaward < .5)
        throw new AssertionError("Distributary flow turns away from its mouth: " + seaward);
      if (mouth.speed() < .05) throw new AssertionError("Stagnant distributary " + mouth.speed());
    }
    return wet;
  }

  private static long mix(long hash, long value) {
    hash ^= value;
    hash *= 0x100000001b3L;
    return hash ^ (hash >>> 29);
  }
}
