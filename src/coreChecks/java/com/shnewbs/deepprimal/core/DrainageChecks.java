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
    System.out.println(
        "DRAINAGE_CORE_PASS channels="
            + channels
            + " elevated="
            + high
            + " gorges="
            + gorges
            + " random=20000 acyclic=true downhill=true cacheEviction=true");
  }

  private static void check(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }
}
