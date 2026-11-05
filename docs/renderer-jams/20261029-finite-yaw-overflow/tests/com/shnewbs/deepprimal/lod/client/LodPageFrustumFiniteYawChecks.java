package com.shnewbs.deepprimal.lod.client;

import java.util.Random;

/** The exact finite-yaw overflow witness from the independent Dev74 renderer audit. */
public final class LodPageFrustumFiniteYawChecks {
  private static long assertions;

  private static void check(boolean truth, String label) {
    assertions++;
    if (!truth) throw new AssertionError(label + " assertion=" + assertions);
  }

  private static double circularDifference(float a, float b) {
    // Reference computation uses double arithmetic so subtracting finite
    // float headings cannot overflow before the angular modulo is applied.
    double delta = Math.abs(((double) a % 360.0) - ((double) b % 360.0));
    if (delta >= 360.0) delta -= 360.0;
    return Math.min(delta, 360.0 - delta);
  }

  private static boolean forceResample(float earlier, float later) {
    var gate = new LodPageFrustumAdaptiveGate();
    check(gate.shouldTestPages(0, 64, 0, earlier, 0), "initial sample");
    gate.completed(true, 64, 0);
    return gate.shouldTestPages(0, 64, 0, later, 0);
  }

  public static void main(String[] args) {
    boolean enabled = LodPageFrustumAdaptiveGate.ENABLED;
    float max = Float.MAX_VALUE;
    float previous = -Math.nextDown(max);
    check(circularDifference(previous, max) == 104.0, "witness angular distance");
    check(forceResample(previous, max), "positive finite overflow forces sample");
    check(forceResample(max, previous), "negative finite overflow forces sample");
    check(forceResample(max, -3.0e38f), "alternate finite overflow forces sample");
    check(forceResample(-3.0e38f, max), "reverse alternate finite overflow");
    check(forceResample(Float.NaN, 0), "invalid sample does not suppress coarse");
    check(forceResample(0, Float.POSITIVE_INFINITY), "invalid new pose samples");
    check(forceResample(359, -1) == !enabled, "ordinary wrap remains unchanged");
    check(forceResample(0, 8) == !enabled, "ordinary exact threshold unchanged");
    check(forceResample(0, 8.5f), "ordinary beyond threshold still samples");
    // Exact large finite values differing by a full number of turns should
    // still be treated as equivalent, not forced on magnitude alone.
    check(forceResample(max, -max) == !enabled, "equal wrapped extreme headings");

    Random random = new Random(0xC011ED74L);
    int tested = 0, skippedEdges = 0;
    for (int i = 0; i < 40_000; ++i) {
      float earlier = Float.intBitsToFloat(random.nextInt());
      float later = Float.intBitsToFloat(random.nextInt());
      if (!Float.isFinite(earlier) || !Float.isFinite(later)) continue;
      double expected = circularDifference(earlier, later);
      // Avoid microscopic float rounding differences at the heuristic edge:
      // tests are about whether a finite rotation that definitely exceeds
      // eight degrees can accidentally disappear after float overflow.
      if (expected > 7.75 && expected < 8.25) { skippedEdges++; continue; }
      boolean actual = forceResample(earlier, later);
      check(actual == (!enabled || expected > 8.0), "random finite yaw " + i);
      tested++;
    }
    System.out.println("random tested=" + tested + " edgeExcluded=" + skippedEdges);
    check(tested > 38_000, "random coverage sufficient");
    check(skippedEdges < 2_000, "only threshold edge probes excluded");
    System.out.println("FINITE_YAW_PASS adaptive=" + enabled + " random=" + tested
        + " edgeExcluded=" + skippedEdges + " assertions=" + assertions);
  }
}
