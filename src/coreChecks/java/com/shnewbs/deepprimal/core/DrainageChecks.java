package com.shnewbs.deepprimal.core;

import java.util.Random;

public final class DrainageChecks {
  public static void run() {
    var terrain =
        new MountainDrainage(
            (x, z) ->
                84
                    + x * .065
                    + 100 * Math.exp(-Math.pow((x - 1300) / 180, 2))
                    + 4 * Math.sin(z / 180),
            96);
    var region = terrain.region(0, 0);
    int channels = 0, high = 0, gorges = 0;
    for (int k = 0; k < region.nodeCount(); k++) {
      int n = region.node(k), p = region.parent(n);
      if (region.width(n) <= 0) continue;
      channels++;
      if (region.water(n) > 160) high++;
      if (region.terrain(n) - region.water(n) > 12) gorges++;
      check(p >= 0 && region.water(p) <= region.water(n), "uphill channel");
      int next = n, steps = 0;
      while (region.parent(next) >= 0) {
        next = region.parent(next);
        check(++steps < 5000, "drainage cycle");
      }
      check(region.terrain(next) < 95, "outlet is not wet");
      var s = terrain.sample(region.x(n), region.z(n), region.terrain(n));
      check(s.height() < s.waterLevel(), "channel bed above water");
    }
    check(
        channels > 20 && high > 10 && gorges > 0,
        "missing mountain channels or ridge incision: " + channels + "/" + high + "/" + gorges);
    var random = new Random(4412);
    for (int i = 0; i < 20000; i++) {
      int x = random.nextInt(4096), z = random.nextInt(4096);
      double original =
          84
              + x * .065
              + 100 * Math.exp(-Math.pow((x - 1300) / 180.0, 2))
              + 4 * Math.sin(z / 180.0);
      var a = terrain.sample(x, z, original);
      var b = terrain.sample(x, z, original);
      check(
          a.equals(b) && Double.isFinite(a.height()) && a.height() <= original,
          "non-deterministic/raised terrain");
    }
    var dry = new MountainDrainage((x, z) -> 350 + 20 * Math.sin(x / 180), 96);
    check(dry.region(0, 0).nodeCount() == 0, "invented outlet in closed high terrain");
    check(dry.sample(0, 0, 350).height() == 350, "changed unconnected mountain");
    for (int x : new int[] {-4097, -4096, -1, 0, 4095, 4096}) {
      var a = terrain.sample(x, 64, 250);
      for (int i = 0; i < 70; i++) terrain.region(i, 1);
      check(a.equals(terrain.sample(x, 64, 250)), "cache eviction changed generation");
    }
    var calls = new java.util.concurrent.atomic.AtomicInteger();
    var shared =
        new MountainDrainage(
            (x, z) -> {
              calls.incrementAndGet();
              return 150;
            },
            96);
    for (int z = 0; z < 2; z++) for (int x = 0; x < 2; x++) shared.region(x, z);
    int cold = calls.get();
    for (int i = 0; i < 100; i++)
      for (int z = 0; z < 2; z++) for (int x = 0; x < 2; x++) shared.region(x, z);
    check(calls.get() == cold, "neighboring catchments thrash the routing cache");
    contained();
    natural();
    System.out.println(
        "DRAINAGE_CORE_PASS channels="
            + channels
            + " elevated="
            + high
            + " gorges="
            + gorges
            + " random=20000 acyclic=true downhill=true cacheEviction=true");
  }

  private static void contained() {
    java.util.function.DoubleBinaryOperator land =
        (x, z) ->
            84 + x * .065 + 100 * Math.exp(-Math.pow((x - 1300) / 180, 2)) + 4 * Math.sin(z / 180);
    var network = new MountainDrainage(land, 96, true);
    var region = network.region(0, 0);
    int samples = 0, terraces = 0, descents = 0;
    for (int k = 0; k < region.nodeCount(); k++) {
      int n = region.node(k), p = region.parent(n);
      if (region.width(n) <= 0) continue;
      double last = Double.POSITIVE_INFINITY;
      for (int step = 0; step <= 32; step++) {
        double t = step / 32.0;
        int x = (int) Math.round(region.x(n) + t * (region.x(p) - region.x(n)));
        int z = (int) Math.round(region.z(n) + t * (region.z(p) - region.z(n)));
        var s = network.sample(x, z, land.applyAsDouble(x, z));
        check(
            Double.isFinite(s.waterLevel()) && s.height() <= land.applyAsDouble(x, z),
            "invalid contained profile");
        check(Math.abs(Math.hypot(s.flowX(), s.flowZ()) - 1) < 1e-8, "missing normalized current");
        check(s.waterLevel() <= last + 1.0, "contained course rises");
        if (Double.isFinite(last)) {
          if ((int) last == (int) s.waterLevel()) terraces++;
          else descents++;
        }
        last = s.waterLevel();
        samples++;
      }
    }
    check(terraces > 100 && descents > 100, "lost stepped descent");
    var random = new Random(88213);
    for (int i = 0; i < 20000; i++) {
      int x = random.nextInt(4096), z = random.nextInt(4096);
      double base = land.applyAsDouble(x, z);
      var a = network.sample(x, z, base);
      check(
          a.equals(network.sample(x, z, base)) && a.height() <= base, "unstable contained sample");
    }
    System.out.println(
        "CONTAINED_CORE_PASS courseSamples="
            + samples
            + " terraces="
            + terraces
            + " descents="
            + descents
            + " random=20000 directions=true");
  }

  private static void natural() {
    java.util.function.DoubleBinaryOperator land =
        (x, z) ->
            84 + x * .065 + 100 * Math.exp(-Math.pow((x - 1300) / 180, 2)) + 4 * Math.sin(z / 180);
    var network = new MountainDrainage(land, 96, true, true);
    var region = network.region(0, 0);
    int sources = 0, lakes = 0, partial = 0;
    for (int k = 0; k < region.nodeCount(); k++) {
      int n = region.node(k), parent = region.parent(n);
      if (region.width(n) <= 0) continue;
      check(parent >= 0 && region.water(parent) <= region.water(n), "natural river rises");
      if (region.headwater(n)) sources++;
      if (region.lakeRadius(n) > 0) {
        lakes++;
        check(region.lakeRadius(n) >= 12, "large headwater lacks a body of water");
        var center =
            network.sample(region.x(n), region.z(n), land.applyAsDouble(region.x(n), region.z(n)));
        check(center.height() < center.waterLevel() - 1, "headwater pool has no depth");
      }
      for (int step = 0; step <= 32; step++) {
        double t = step / 32.0;
        int x = (int) Math.round(region.x(n) + t * (region.x(parent) - region.x(n)));
        int z = (int) Math.round(region.z(n) + t * (region.z(parent) - region.z(n)));
        var sample = network.sample(x, z, land.applyAsDouble(x, z));
        int level = WaterDynamics.surfaceLevel(sample.waterLevel());
        double actual = WaterDynamics.surfaceY(sample.waterLevel()) + (8 - level) / 9.0;
        check(
            level >= 0
                && level <= 7
                && Math.abs(actual - (sample.waterLevel() - 1.0 / 9)) <= 1.0 / 9 + 1e-6,
            "invalid partial surface");
        check(sample.height() <= land.applyAsDouble(x, z), "natural river raises land");
        if (level > 0) partial++;
      }
    }
    check(sources > 0 && lakes > 0 && partial > 100, "missing headwaters or partial river levels");
    for (int y = -400; y < 640; y++)
      check(
          WaterDynamics.surfaceY(y) == y - 1 && WaterDynamics.surfaceLevel(y) == 0,
          "integer source height changed");
    System.out.println(
        "NATURAL_RIVER_CORE_PASS sources="
            + sources
            + " lakes="
            + lakes
            + " partial="
            + partial
            + " integerSourceHeights=true");
  }

  private static void check(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }
}
