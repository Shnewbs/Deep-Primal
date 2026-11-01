package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Standalone, dependency-free finite admission and finite-domain parity regression. */
public final class WaterwayFiniteAdmissionChecks {
  private static long checks;
  private static void need(boolean yes, String why) {
    checks++;
    if (!yes) throw new AssertionError(why);
  }
  private static double smooth(double v, double lo, double hi) {
    double t = Math.clamp((v - lo) / (hi - lo), 0, 1);
    return t * t * (3 - 2 * t);
  }
  private static double baseline(double original, double continents, double ridges,
      double erosion, double detail, double tributaries, int seaLevel) {
    double coastW = smooth(continents, -.26, -.20) * (1 - smooth(continents, -.07, -.02))
        * smooth(erosion, -.45, .25);
    double coast = original + coastW * (seaLevel + 3.0 + 150.0 * (continents + .15) - original);
    double bed = seaLevel - 7.0 + 1.5 * Math.clamp(detail, -1.0, 1.0);
    double lowland = 1 - smooth(continents, .18, .42);
    double riverW = lowland * (1 - smooth(Math.abs(ridges), .018, .22));
    double river = coast - riverW * Math.max(0, coast - bed);
    double offset = .28 * smooth(tributaries, -.35, .45);
    double distance = Math.abs(Math.abs(ridges) - offset);
    double streamW = lowland * (1 - smooth(tributaries, .15, .55))
        * (1 - smooth(distance, .004, .045));
    return river - streamW * Math.max(0, river - bed);
  }
  public static void main(String[] args) {
    Random rng = new Random(20261101L);
    for (int i = 0; i < 300_000; i++) {
      double original = -500 + rng.nextDouble() * 1400;
      double continents = -1.0 + rng.nextDouble() * 2;
      double ridges = -1.0 + rng.nextDouble() * 2;
      double erosion = -1.0 + rng.nextDouble() * 2;
      double detail = -2.0 + rng.nextDouble() * 4;
      double tributaries = -1.0 + rng.nextDouble() * 2;
      double expected = baseline(original, continents, ridges, erosion, detail, tributaries, 96);
      double actual = WaterwayShape.height(original, continents, ridges, erosion, detail, tributaries, 96);
      need(Double.doubleToRawLongBits(actual) == Double.doubleToRawLongBits(expected), "finite-domain parity " + i);
      need(Double.isFinite(actual), "finite-domain output " + i);
    }
    double[] invalid = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
    for (double bad : invalid) {
      need(WaterwayShape.height(bad, 0, 0, 0, 0, 0, 96) == 96, "bad original fallback");
      for (int p = 0; p < 5; p++) {
        double[] s = {-.15, .04, .2, .6, .5};
        s[p] = bad;
        need(WaterwayShape.height(140, s[0], s[1], s[2], s[3], s[4], 96) == 140,
            "bad shaping input " + p);
      }
    }
    // Extreme but finite noise may overflow shelf calculations, without changing
    // ordinary worldgen math or returning nonfinite native height data.
    need(Double.isFinite(WaterwayShape.height(150, Double.MAX_VALUE, 0, 0, 0, 0, 96)), "overflowing shelf");
    need(Double.isFinite(WaterwayShape.height(Double.MAX_VALUE, -.23, 0, 0, 0, 0, 96)), "overflowing original");
    System.out.println("WATERWAY_FINITE_ADMISSION_PASS checks=" + checks);
  }
}
