package com.shnewbs.deepprimal.lod.client;

import java.util.Arrays;
import java.util.Random;
import net.minecraft.world.phys.AABB;

/** Production-helper parity check using the jam36 test-only Minecraft AABB stub. */
public final class LodPublishedBoundsCommittedSmoke {
  private static long assertions;
  private LodPublishedBoundsCommittedSmoke() {}

  private static void require(boolean condition, String label) {
    assertions++;
    if (!condition) throw new AssertionError("check " + assertions + ": " + label);
  }

  public static void main(String[] args) {
    Random random = new Random(2026102937L);
    for (int page = 0; page < 2400; page++) {
      AABB[][] frozen = new AABB[4][];
      for (int d = 0; d < frozen.length; d++) {
        int count = page % 5 == 0 ? random.nextInt(5) : random.nextInt(130);
        AABB[] parts = new AABB[count];
        for (int i = 0; i < count; i++) {
          int variant = random.nextInt(17);
          if (i > 0 && variant < 6) parts[i] = parts[i - 1];
          else if (variant == 7) parts[i] = null;
          else if (variant == 8)
            parts[i] = new AABB(Double.NaN, 0, 0, 1, 1, 1);
          else if (variant == 9)
            parts[i] = new AABB(2, 0, 0, 1, 1, 1);
          else {
            int x = random.nextInt(2000) - 1000;
            parts[i] = new AABB(x, -40, -x, x + 16, 80, 16 - x);
          }
        }
        frozen[d] = parts;
      }
      AABB[] expected = LodPageFrustumBounds.fromSnapshot(frozen);
      long[][] expectedMasks = LodPageFrustumBounds.ENABLED
          ? LodPartFrustumRuns.fromSnapshot(frozen) : new long[0][];
      AABB[] actual = new AABB[frozen.length];
      long[][] masks = LodPageFrustumBounds.ENABLED
          ? new long[frozen.length][] : new long[0][];
      LodPublishedBounds.populate(frozen, actual, masks);
      require(masks.length == expectedMasks.length, "mask detail count");
      for (int d = 0; d < frozen.length; d++) {
        require((actual[d] == null) == (expected[d] == null), "same coarse fallback");
        if (expected[d] != null) {
          require((expected[d] == frozen[d][0]) == (actual[d] == frozen[d][0]),
              "uniform marker identity");
          require(Arrays.equals(coords(expected[d]), coords(actual[d])),
              "bit-exact conservative union");
        }
        if (LodPageFrustumBounds.ENABLED)
          require(Arrays.equals(expectedMasks[d], masks[d]), "bit-exact adjacent runs");
      }
    }
    AABB good = new AABB(-2, -3, -4, 2, 3, 4);
    AABB[][] invalidFront = {{null, good, good}};
    AABB[] coarse = new AABB[1];
    long[][] repeated = LodPageFrustumBounds.ENABLED ? new long[1][] : new long[0][];
    LodPublishedBounds.populate(invalidFront, coarse, repeated);
    require(coarse[0] == null, "invalid page is never coarsely rejected");
    if (LodPageFrustumBounds.ENABLED)
      require(LodPartFrustumRuns.repeatsPrevious(repeated[0], 2),
          "run following invalid first part still reused");
    System.out.println("FUSED_COMMITTED_PASS enabled=" + LodPageFrustumBounds.ENABLED
        + " assertions=" + assertions);
  }

  private static long[] coords(AABB b) {
    return new long[] {Double.doubleToRawLongBits(b.minX), Double.doubleToRawLongBits(b.minY),
        Double.doubleToRawLongBits(b.minZ), Double.doubleToRawLongBits(b.maxX),
        Double.doubleToRawLongBits(b.maxY), Double.doubleToRawLongBits(b.maxZ)};
  }
}
