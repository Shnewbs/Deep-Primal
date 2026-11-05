package com.shnewbs.deepprimal.lod.client;

import java.util.Arrays;
import java.util.Random;
import net.minecraft.world.phys.AABB;

/** Pin mask/union parity against the published jam38 source; no GPU execution. */
public final class LodAdjacentFirstParityChecks {
  private static long checks;
  private LodAdjacentFirstParityChecks() {}

  private static void yes(boolean condition, String failure) {
    checks++;
    if (!condition) throw new AssertionError("check=" + checks + " " + failure);
  }

  private static boolean same(AABB a, AABB b) {
    return a != null && b != null && a.minX == b.minX && a.minY == b.minY
        && a.minZ == b.minZ && a.maxX == b.maxX
        && a.maxY == b.maxY && a.maxZ == b.maxZ;
  }

  private static void verify(AABB[][] levels) {
    AABB[] oldPages = new AABB[levels.length], newPages = new AABB[levels.length];
    long[][] oldRuns = LodPageFrustumBounds.ENABLED ? new long[levels.length][] : new long[0][];
    long[][] newRuns = LodPageFrustumBounds.ENABLED ? new long[levels.length][] : new long[0][];
    long[][] oldTwo = LodPageFrustumBounds.ENABLED ? new long[levels.length][] : new long[0][];
    long[][] newTwo = LodPageFrustumBounds.ENABLED ? new long[levels.length][] : new long[0][];
    LegacyLodPublishedBounds.populate(levels, oldPages, oldRuns, oldTwo);
    LodPublishedBounds.populate(levels, newPages, newRuns, newTwo);
    for (int d = 0; d < levels.length; d++) {
      yes(oldPages[d] == newPages[d] || same(oldPages[d], newPages[d]), "page union " + d);
      yes(oldPages[d] == null || oldPages[d] != levels[d][0] || newPages[d] == levels[d][0],
          "uniform identity " + d);
      if (LodPageFrustumBounds.ENABLED) {
        yes(Arrays.equals(oldRuns[d], newRuns[d]), "adjacent mask " + d);
        yes(Arrays.equals(oldTwo[d], newTwo[d]), "two-back mask " + d);
        for (int i = 0; i < levels[d].length; i++) {
          boolean a = LodPartFrustumRuns.repeatsPrevious(newRuns[d], i);
          boolean b = LodPartFrustumRuns.repeatsPrevious(newTwo[d], i);
          yes(!(a && b), "disjoint aliases");
        }
      } else {
        yes(oldPages[d] == null && newPages[d] == null, "disabled page union");
      }
    }
  }

  private static AABB pick(Random random, AABB[] parts, int i) {
    int k = random.nextInt(24);
    if (i > 0 && k < 7) return parts[i - 1];
    if (i > 1 && k < 12) return parts[i - 2];
    if (i > 0 && k < 15 && parts[i - 1] != null) {
      AABB x = parts[i - 1];
      return new AABB(x.minX, x.minY, x.minZ, x.maxX, x.maxY, x.maxZ);
    }
    if (k == 15) return null;
    if (k == 16) return new AABB(Double.NaN, 0, 0, 1, 1, 1);
    if (k == 17) return new AABB(Double.POSITIVE_INFINITY, 0, 0, 1, 1, 1);
    if (k == 18) return new AABB(2, 0, 0, 1, 1, 1);
    if (k == 19) return new AABB(-0.0, 0.0, 0.0, 0.0, 1, 1);
    int x = random.nextInt(400) - 200;
    return new AABB(x, -3, -5, x + 1, 7, 8);
  }

  public static void main(String[] args) {
    AABB a = new AABB(-1, 0, -2, 0, 1, 2);
    AABB b = new AABB(3, 0, 5, 4, 1, 6);
    int[] lengths = {0, 1, 2, 3, 4, 5, 63, 64, 65, 127, 128, 129, 130, 192, 256};
    for (int n : lengths) {
      for (int pattern = 0; pattern < 5; pattern++) {
        AABB[][] lv = new AABB[4][];
        for (int d = 0; d < lv.length; d++) {
          AABB[] parts = new AABB[n];
          for (int i = 0; i < n; i++) {
            parts[i] = switch (pattern) {
              case 0 -> a;
              case 1 -> i % 2 == 0 ? a : b;
              case 2 -> i % 3 == 0 ? a : b;
              case 3 -> new AABB(a.minX, a.minY, a.minZ, a.maxX, a.maxY, a.maxZ);
              default -> new AABB(i, 0, i, i + 1, 2, i + 2);
            };
          }
          lv[d] = parts;
        }
        verify(lv);
      }
    }
    Random r = new Random(2026102939L);
    for (int iteration = 0; iteration < 15000; iteration++) {
      AABB[][] lv = new AABB[1 + r.nextInt(5)][];
      for (int d = 0; d < lv.length; d++) {
        lv[d] = new AABB[r.nextInt(257)];
        for (int i = 0; i < lv[d].length; i++) lv[d][i] = pick(r, lv[d], i);
      }
      verify(lv);
    }
    System.out.println("ADJACENT_FIRST_PARITY_PASS broadphase="+LodPageFrustumBounds.ENABLED
        +" checks="+checks);
  }
}
