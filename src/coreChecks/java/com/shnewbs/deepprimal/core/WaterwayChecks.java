package com.shnewbs.deepprimal.core;

import java.util.Random;

final class WaterwayChecks {
  private static void check(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  static void run() {
    check(
        WaterwayShape.height(150, -0.12, 0.8, -0.9, 0, 0.6, 96) == 150,
        "rough coasts retain their cliff height");
    check(
        WaterwayShape.height(300, 0.7, 0, 0.8, 0, 0, 96) == 300,
        "high inland terrain does not become a sea-level trench");
    check(
        WaterwayShape.height(-50, -0.5, 0, 0, 0, 0, 96) == -50,
        "channels do not raise deep seabeds");
    check(
        WaterwayShape.height(80, -0.15, 0.8, 0.8, 0, 0.6, 96) == 99,
        "gentle coastal shelf has dry beach above sea level");
    check(
        WaterwayShape.height(120, 0.1, 0, 0.8, 0, 0.6, 96) == 89,
        "lowland river has a submerged bed");
    check(
        WaterwayShape.height(100, -0.17, 0, 0.8, 0, 0.6, 96) == 89,
        "river mouth crosses raised coastal shelf without a lip");
    double tributary = 0.11388671875;
    double parent = WaterwayShape.riverHeight(140, 0.1, tributary, 89);
    check(
        parent > 96 && WaterwayShape.height(140, 0.1, tributary, 0, 0, 0, 96) == 89,
        "tributary exists outside the parent river's wet corridor");
    int riverWidth = 0, streamWidth = 0;
    for (int i = -5000; i <= 5000; i++) {
      double ridge = i / 10000.0;
      if (WaterwayShape.riverHeight(140, 0.1, ridge, 89) < 96) riverWidth++;
      if (ridge > 0.075 && WaterwayShape.height(140, 0.1, ridge, 0, 0, 0, 96) < 96) streamWidth++;
    }
    check(streamWidth > 0 && streamWidth < riverWidth / 2, "streams are narrower than rivers");
    Random random = new Random(20261019);
    for (int i = 0; i < 100_000; i++) {
      double base = -120 + random.nextDouble() * 720;
      double c = random.nextDouble() * 2 - 1, r = random.nextDouble() * 2 - 1;
      double e = random.nextDouble() * 2 - 1, d = random.nextDouble() * 4 - 2;
      double branch = random.nextDouble() * 2 - 1;
      double h = WaterwayShape.height(base, c, r, e, d, branch, 96);
      double coast = WaterwayShape.coastHeight(base, c, e, 96);
      check(
          Double.isFinite(h) && h >= Math.min(base, 82) && h <= Math.max(base, 119),
          "bounded height");
      check(h <= coast + 1e-10, "river/stream carving never raises ground");
      check(h == WaterwayShape.height(base, c, -r, e, d, branch, 96), "symmetric tributaries");
      check(h == WaterwayShape.height(base, c, r, e, d, branch, 96), "order-independent sampling");
      double next = WaterwayShape.height(base, c + 1e-8, r + 1e-8, e + 1e-8, d, branch + 1e-8, 96);
      check(Math.abs(next - h) < 0.002, "continuous channel and coast transitions");
    }
    System.out.println(
        "WATERWAY_CORE_PASS randomCases=100000 riverMouth=true dryBeach=true narrowTributary=true"
            + " cliffs=true bounds=true continuity=true");
  }
}
