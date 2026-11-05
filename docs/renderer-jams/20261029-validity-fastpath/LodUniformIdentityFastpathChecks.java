package com.shnewbs.deepprimal.lod.client;

import java.util.Arrays;
import java.util.Random;
import net.minecraft.world.phys.AABB;

/** Actual source versus pinned pre-jam source; no Minecraft/GPU claim. */
public final class LodUniformIdentityFastpathChecks {
  private static long checks;
  private LodUniformIdentityFastpathChecks() {}

  private static void check(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError("immutable-bounds parity #" + checks + ": " + message);
  }

  private static AABB aabb(double x, double y, double z, double xx, double yy, double zz) {
    return new AABB(x, y, z, xx, yy, zz);
  }

  private static void compareBox(AABB expected, AABB actual, AABB first) {
    check((expected == null) == (actual == null), "page nullability");
    if (expected == null) return;
    check((expected == first) == (actual == first), "uniform identity marker");
    for (int d = 0; d < 6; d++) {
      double before = switch (d) {
        case 0 -> expected.minX; case 1 -> expected.minY; case 2 -> expected.minZ;
        case 3 -> expected.maxX; case 4 -> expected.maxY; default -> expected.maxZ;
      };
      double after = switch (d) {
        case 0 -> actual.minX; case 1 -> actual.minY; case 2 -> actual.minZ;
        case 3 -> actual.maxX; case 4 -> actual.maxY; default -> actual.maxZ;
      };
      check(Double.doubleToRawLongBits(before) == Double.doubleToRawLongBits(after),
          "union coordinate exact bits d=" + d);
    }
  }

  private static void compare(AABB[][] levels) {
    AABB[] old = new AABB[levels.length], fresh = new AABB[levels.length];
    long[][] oldAdj = LodPageFrustumBounds.ENABLED ? new long[levels.length][] : new long[0][];
    long[][] newAdj = LodPageFrustumBounds.ENABLED ? new long[levels.length][] : new long[0][];
    long[][] oldTwo = LodPageFrustumBounds.ENABLED ? new long[levels.length][] : new long[0][];
    long[][] newTwo = LodPageFrustumBounds.ENABLED ? new long[levels.length][] : new long[0][];
    LodPublishedBoundsJam43.populate(levels, old, oldAdj, oldTwo);
    LodPublishedBounds.populate(levels, fresh, newAdj, newTwo);
    for (int i = 0; i < levels.length; i++) {
      compareBox(old[i], fresh[i], levels[i].length == 0 ? null : levels[i][0]);
      if (LodPageFrustumBounds.ENABLED) {
        check(Arrays.equals(oldAdj[i], newAdj[i]), "adjacent mask " + i);
        check(Arrays.equals(oldTwo[i], newTwo[i]), "two-back mask " + i);
      }
    }
  }

  private static AABB randomPart(Random random, AABB[] parts, int index) {
    int choice = random.nextInt(32);
    if (choice < 8 && index > 0) return parts[index - 1];
    if (choice < 11 && index > 1) return parts[index - 2];
    if (choice == 11) return null;
    if (choice == 12) return aabb(Double.NaN, 0, 0, 1, 1, 1);
    if (choice == 13) return aabb(1, 1, 1, 0, 2, 2);
    if (choice == 14) return aabb(1, 0, 0, Double.POSITIVE_INFINITY, 1, 1);
    if (choice == 15) return aabb(-0.0, 0.0, -0.0, 0.0, 1, 0.0);
    if (choice == 16) return aabb(0.0, -0.0, 0.0, 0.0, 1, -0.0);
    int x = random.nextInt(60000001) - 30000000;
    int y = random.nextInt(1600) - 800;
    int z = random.nextInt(60000001) - 30000000;
    return aabb(x, y, z, x + 16, y + 16, z + 16);
  }

  private static void stress(Random random, int trial) {
    int n = switch (trial % 9) {
      case 0 -> 0; case 1 -> 1; case 2 -> 2; case 3 -> 63;
      case 4 -> 64; case 5 -> 65; case 6 -> 128;
      case 7 -> 257; default -> random.nextInt(260);
    };
    AABB[][] levels = {new AABB[n], new AABB[n / 3], new AABB[0]};
    for (int d = 0; d < 2; d++) {
      AABB[] parts = levels[d];
      AABB uniform = aabb(-16.0, -0.0, -4.0, 0.0, 20.0, 8.0);
      AABB alternating = aabb(-8.0, 0.0, -8.0, 8.0, 32.0, 8.0);
      for (int i = 0; i < parts.length; i++) {
        switch (trial % 7) {
          case 0 -> parts[i] = uniform;
          case 1 -> parts[i] = aabb(uniform.minX, uniform.minY, uniform.minZ,
              uniform.maxX, uniform.maxY, uniform.maxZ);
          case 2 -> parts[i] = (i & 1) == 0 ? uniform : alternating;
          case 3 -> parts[i] = i == parts.length - 1 ? alternating : uniform;
          case 4 -> parts[i] = i < parts.length / 2 ? uniform : randomPart(random, parts, i);
          case 5 -> parts[i] = randomPart(random, parts, i);
          default -> parts[i] = (i & 1) == 0
              ? aabb(-0.0, 0, 0, 1, 2, 3)
              : aabb(0.0, 0, 0, 1, 2, 3);
        }
      }
    }
    compare(levels);
  }

  public static void main(String[] args) {
    Random random = new Random(2026102943L);
    compare(new AABB[][] {});
    compare(new AABB[][] {new AABB[] {null}});
    compare(new AABB[][] {new AABB[] {aabb(-0.0, 0, 0, 1, 1, 1),
        aabb(0.0, -0.0, 0, 1, 1, 1), aabb(-10, -10, -10, 10, 10, 10)}});
    // Exact reference repetition matters even when an invalid AABB occurs.
    AABB invalidOrder = aabb(8, 1, 1, -8, 3, 3);
    AABB invalidNaN = aabb(Double.NaN, 0, 0, 1, 1, 1);
    AABB valid = aabb(-30_000_000, -64, -30_000_000, 1, 320, 1);
    compare(new AABB[][] {{invalidOrder, invalidOrder, invalidOrder}});
    compare(new AABB[][] {{valid, invalidOrder, valid, invalidOrder}});
    compare(new AABB[][] {{valid, invalidNaN, valid, invalidNaN}});
    compare(new AABB[][] {{null, valid, null, valid}});
    compare(new AABB[][] {{valid, null, null, valid, valid}});
    compare(new AABB[][] {{valid, valid, invalidNaN, invalidNaN, valid}});
    // Explicit multi-word run masks and late single-identity divergence.
    for (int n : new int[] {2, 3, 4, 63, 64, 65, 128, 129, 257, 1024, 1025}) {
      AABB good = aabb(-0.0, -128.0, -16.0, 0.0, 320.0, 16.0);
      AABB bad = aabb(Double.POSITIVE_INFINITY, 0, 0, 2, 2, 2);
      for (AABB shared : new AABB[] {good, bad, null}) {
        AABB[] all = new AABB[n];
        Arrays.fill(all, shared);
        compare(new AABB[][] {all});
        AABB[] late = all.clone();
        late[n - 1] = shared == good ? bad : good;
        compare(new AABB[][] {late});
      }
    }
    for (int trial = 0; trial < 12000; trial++) stress(random, trial);
    System.out.println("LOD_UNIFORM_IDENTITY_FASTPATH_PARITY_PASS assertions=" + checks
        + " randomizedPages=12000 broadphase=" + LodPageFrustumBounds.ENABLED
        + " twoBack=" + LodPublishedBounds.TWO_BACK_ENABLED);
  }
}
