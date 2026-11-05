package com.shnewbs.deepprimal.lod.client;

import java.util.Random;
import net.minecraft.world.phys.AABB;

/** Java21 exact-helper parity smoke; never a native frustum/GPU acceptance test. */
public final class LodTwoBackCommittedSmoke {
  private static long assertions, saved;
  private LodTwoBackCommittedSmoke() {}

  private static void check(boolean ok) {
    assertions++;
    if (!ok) throw new AssertionError("two-back parity assertion " + assertions);
  }

  private static boolean visible(AABB b) {
    return b != null && Double.isFinite(b.minX) && Math.sin(b.minX) > 0;
  }

  private static void page(AABB[] parts) {
    AABB[] union = new AABB[1];
    long[][] adjacent = LodPageFrustumBounds.ENABLED ? new long[1][] : new long[0][];
    long[][] twoBack = LodPageFrustumBounds.ENABLED ? new long[1][] : new long[0][];
    LodPublishedBounds.populate(new AABB[][] {parts}, union, adjacent, twoBack);
    boolean prior = false, twoPrior = false;
    for (int i = 0; i < parts.length; i++) {
      boolean one = LodPageFrustumBounds.ENABLED
          && LodPartFrustumRuns.repeatsPrevious(adjacent[0], i);
      boolean two = LodPageFrustumBounds.ENABLED && !one
          && LodPartFrustumRuns.repeatsPrevious(twoBack[0], i);
      check(!(one && two));
      boolean result = one ? prior : two ? twoPrior : visible(parts[i]);
      check(result == visible(parts[i]));
      if (two) saved++;
      twoPrior = prior;
      prior = result;
    }
  }

  public static void main(String[] args) {
    AABB a = new AABB(-2, 0, 0, -1, 1, 1);
    AABB b = new AABB(2, 0, 0, 3, 1, 1);
    AABB[] alternating = new AABB[130];
    for (int i = 0; i < alternating.length; i++) alternating[i] = (i & 1) == 0 ? a : b;
    page(alternating);
    check(saved == (LodPageFrustumBounds.ENABLED ? 128 : 0));
    Random rng = new Random(2026102938L);
    for (int trial = 0; trial < 4000; trial++) {
      AABB[] parts = new AABB[rng.nextInt(140)];
      for (int i = 0; i < parts.length; i++) {
        int kind = rng.nextInt(17);
        if (i > 0 && kind < 3) parts[i] = parts[i - 1];
        else if (i > 1 && kind < 9) parts[i] = parts[i - 2];
        else if (kind == 9) parts[i] = null;
        else if (kind == 10) parts[i] = new AABB(Double.NaN, 0, 0, 1, 1, 1);
        else if (kind == 11) parts[i] = new AABB(2, 0, 0, 1, 1, 1);
        else {
          int x = rng.nextInt(101) - 50;
          parts[i] = new AABB(x, 0, 0, x + 1, 1, 1);
        }
      }
      page(parts);
    }
    if (!LodPageFrustumBounds.ENABLED) check(saved == 0);
    else check(saved > 128);
    System.out.println("TWO_BACK_COMMITTED_PASS enabled=" + LodPageFrustumBounds.ENABLED
        + " assertions=" + assertions + " savedFineTests=" + saved);
  }
}
