package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.baseline.Alpha23TerrainEngine;
import java.util.*;
import java.util.function.DoubleBinaryOperator;

/** Independent native-cell topology and frozen-geography checks. */
final class WatershedChecks {
  private static long banks;

  static void run() {
    DoubleBinaryOperator bowl =
        (x, z) -> 150 + x * .014 - 38 * Math.exp(-((x - 300) * (x - 300) + z * z) / (260. * 260));
    var analytic = new WatershedRegion(-4096, -4096, bowl, (x, z) -> .7);
    analytic.verify();
    require(!analytic.lakeProbes().isEmpty(), "Analytical basin needs a lake");
    long samples = inspect(analytic, bowl);
    var inland =
        new WatershedRegion(
            -4096,
            -4096,
            (x, z) -> 130 - 60 * Math.exp(-(x * x + z * z) / (260. * 260)),
            (x, z) -> .7);
    require(
        inland.sample(0, 0, 70).water() > 120,
        "Landlocked depression below sea level became ocean");
    banks = 0;
    int paths = 0, lakes = 0, heads = 0, savedSamples = 0;
    var random = new Random(0x24D2A1);
    for (long seed :
        new long[] {20261020, -914, Long.MAX_VALUE, Long.MIN_VALUE, 0, 1, 42, 1998, 0xD33F0L}) {
      var terrain = new TerrainEngine(seed, 3);
      var saved = new Alpha23TerrainEngine(seed, 2);
      var legacy = new TerrainEngine(seed, 2);
      var region = terrain.waterways(0, 0);
      region.verify();
      samples += inspect(region, terrain::raw);
      paths += flowPaths(region, terrain::raw);
      var stats = region.statistics();
      heads += stats.headwaters();
      lakes += stats.lakes();
      for (int n = 0; n < 2000; n++) {
        int x = random.nextInt(7000) - 3500, z = random.nextInt(7000) - 3500;
        var a = terrain.sample(x, z);
        savedSamples++;
        var bulk = terrain.column(x, z);
        require(
            bulk.slope() == 0 && bulk.withSlope(a.slope()).equals(a), "Bulk column changed fields");
        require(Double.isFinite(a.ground()), "Finite terrain");
        require(
            !a.wet() || (a.ground() < a.water() && Math.floor(a.ground()) + 1 < a.water()),
            "Actual wet voxel");
        var b = legacy.sample(x, z);
        var c = saved.sample(x, z);
        require(
            Arrays.equals(
                new double[] {
                  b.ground(),
                  b.water(),
                  b.rain(),
                  b.temperature(),
                  b.slope(),
                  b.sediment(),
                  b.flowX(),
                  b.flowZ(),
                  b.speed(),
                  b.rock()
                },
                new double[] {
                  c.ground(),
                  c.water(),
                  c.rain(),
                  c.temperature(),
                  c.slope(),
                  c.sediment(),
                  c.flowX(),
                  c.flowZ(),
                  c.speed(),
                  c.rock()
                }),
            "Published V2 changed");
      }
      var retained = terrain.sample(-240, -624);
      for (int r = -3; r <= 3; r++) terrain.verify(r * 8192., 0);
      require(
          terrain.cachedRegions() <= 6 && retained.equals(terrain.sample(-240, -624)),
          "Bounded region replay");
      System.out.println("WATERSHED_SEED seed=" + seed + " " + stats);
    }
    require(heads > 30 && lakes > 0, "Ensemble coverage");
    System.out.println(
        "WATERSHED_CORE_PASS centerlineSamples="
            + samples
            + " headwaters="
            + heads
            + " lakes="
            + lakes
            + " savedV2="
            + savedSamples
            + " faceConnectedFlowReaches="
            + paths
            + " containedBanks="
            + banks);
  }

  private static long inspect(WatershedRegion region, DoubleBinaryOperator raw) {
    var probes = region.centerline();
    long count = 0;
    for (int n = 0; n < probes.size(); n += 2) {
      var a = probes.get(n);
      var b = probes.get(n + 1);
      require(a.stage() + 1e-8 >= b.stage(), "Channel flows uphill");
      int steps = (int) Math.ceil(Math.hypot(b.x() - a.x(), b.z() - a.z()) * 2);
      for (int j = 0; j <= steps; j++) {
        double t = j / (double) steps,
            x = Math.rint(a.x() + (b.x() - a.x()) * t),
            z = Math.rint(a.z() + (b.z() - a.z()) * t);
        var s = region.sample(x, z, raw.applyAsDouble(x, z));
        require(
            Double.isFinite(s.water()) && Math.floor(s.ground()) + 1 < s.water(),
            "Dry centerline " + x + "," + z + " " + s);
        count++;
      }
    }
    return count;
  }

  private static int flowPaths(WatershedRegion region, DoubleBinaryOperator raw) {
    var line = region.centerline();
    int paths = 0;
    for (int n = 0; n < line.size(); n += 16) {
      var a = line.get(n);
      var b = line.get(n + 15);
      if (a.stage() < TerrainEngine.SEA + 1
          || region.sample(a.x(), a.z(), raw.applyAsDouble(a.x(), a.z())).speed() == 0) continue;
      int ax = (int) Math.rint(a.x()),
          az = (int) Math.rint(a.z()),
          bx = (int) Math.rint(b.x()),
          bz = (int) Math.rint(b.z());
      int x0 = Math.min(ax, bx) - 5,
          z0 = Math.min(az, bz) - 5,
          w = Math.abs(ax - bx) + 11,
          h = Math.abs(az - bz) + 11;
      int[] lo = new int[w * h], hi = new int[w * h];
      double[] heads = new double[w * h];
      for (int z = 0; z < h; z++)
        for (int x = 0; x < w; x++) {
          var s = region.sample(x0 + x, z0 + z, raw.applyAsDouble(x0 + x, z0 + z));
          int i = z * w + x;
          heads[i] = s.water();
          lo[i] = (int) Math.floor(s.ground()) + 1;
          hi[i] = Double.isFinite(s.water()) ? (int) Math.ceil(s.water()) - 1 : Integer.MIN_VALUE;
        }
      for (int z = 1; z < h - 1; z++)
        for (int x = 1; x < w - 1; x++) {
          int i = z * w + x;
          if (lo[i] > hi[i] || heads[i] <= TerrainEngine.SEA + 1) continue;
          for (int d : new int[] {-w, w, -1, 1}) {
            int j = i + d;
            if (lo[j] <= hi[j]) continue;
            require(
                lo[j] + 1e-7 >= heads[i],
                "Open bank "
                    + (x0 + x)
                    + ","
                    + (z0 + z)
                    + " head="
                    + heads[i]
                    + " neighbour="
                    + (x0 + j % w)
                    + ","
                    + (z0 + j / w)
                    + " top="
                    + lo[j]);
            banks++;
          }
        }
      int start = (az - z0) * w + ax - x0, end = (bz - z0) * w + bx - x0;
      var q = new ArrayDeque<Integer>();
      var seen = new BitSet(w * h);
      q.add(start);
      seen.set(start);
      while (!q.isEmpty()) {
        int i = q.removeFirst();
        for (int d : new int[] {-w, w, -1, 1}) {
          int j = i + d;
          if (j < 0
              || j >= w * h
              || Math.abs(i % w - j % w) + Math.abs(i / w - j / w) != 1
              || seen.get(j)
              || lo[j] > hi[j]
              || Math.max(lo[i], lo[j]) > Math.min(hi[i], hi[j])) continue;
          seen.set(j);
          q.add(j);
        }
      }
      require(seen.get(end), "Disconnected flow " + ax + "," + az + " -> " + bx + "," + bz);
      paths++;
    }
    return paths;
  }

  private static void require(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }
}
