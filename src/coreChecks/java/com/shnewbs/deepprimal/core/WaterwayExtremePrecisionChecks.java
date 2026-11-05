package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Independent precision regression for full-strength coast, river and stream shaping. */
public final class WaterwayExtremePrecisionChecks {
  private static long assertions;
  private static void check(boolean condition, String message) {
    assertions++;
    if (!condition) throw new AssertionError(message);
  }
  private static double smooth(double value, double low, double high) {
    double t = Math.clamp((value - low) / (high - low), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
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
      double continents = -1 + rng.nextDouble() * 2;
      double ridges = -1 + rng.nextDouble() * 2;
      double erosion = -1 + rng.nextDouble() * 2;
      double detail = -2 + rng.nextDouble() * 4;
      double tributaries = -1 + rng.nextDouble() * 2;
      double expected = baseline(original, continents, ridges, erosion, detail, tributaries, 96);
      double actual = WaterwayShape.height(original, continents, ridges, erosion, detail, tributaries, 96);
      check(Double.doubleToRawLongBits(expected) == Double.doubleToRawLongBits(actual),
          "ordinary finite bit parity " + i);
      check(Double.isFinite(actual), "ordinary finite output " + i);
    }
    double shelf = 96 + 3.0 + 150.0 * (-.20 + .15);
    check(WaterwayShape.coastWeight(-.20, .8) == 1.0, "full coast weight");
    check(WaterwayShape.coastHeight(Double.MAX_VALUE, -.20, .8, 96) == shelf,
        "full coast must hit shelf at extreme finite Y");
    check(WaterwayShape.riverWeight(.1, 0) == 1.0, "full river weight");
    check(WaterwayShape.riverHeight(Double.MAX_VALUE, .1, 0, 89) == 89,
        "full river must hit bed at extreme finite Y");
    double tributaries = .1;
    double ridge = .28 * smooth(tributaries, -.35, .45);
    check(WaterwayShape.streamWeight(.1, ridge, tributaries) == 1.0, "full stream weight");
    check(WaterwayShape.height(Double.MAX_VALUE, -.20, .8, .8, 0, .6, 96) == shelf,
        "coastal height must hit shelf");
    check(WaterwayShape.height(Double.MAX_VALUE, .1, 0, .8, 0, .6, 96) == 89,
        "river height must hit bed");
    check(WaterwayShape.height(Double.MAX_VALUE, .1, ridge, .8, 0, tributaries, 96) == 89,
        "stream height must hit bed");
    System.out.println("WATERWAY_EXTREME_PRECISION_PASS assertions=" + assertions);
  }
}
