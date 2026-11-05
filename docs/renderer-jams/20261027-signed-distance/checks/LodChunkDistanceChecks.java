package com.shnewbs.deepprimal.lod.client;

import java.math.BigInteger;
import java.util.Random;

/** Independent exact-integer oracle for signed LOD camera/page admission and ordering. */
public final class LodChunkDistanceChecks {
  public static void main(String[] args) {
    int[][] seeds = {
        {0, 0, 0, 0}, {-1, 1, 1, -1},
        {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE},
        {Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE},
        {Integer.MIN_VALUE, 0, Integer.MAX_VALUE, 0},
        {-1_875_000, 1_875_000, 1_875_000, -1_875_000},
        {0, 512, 0, 0}, {512, 512, 0, 0}
    };
    int checks = 0;
    for (int[] v : seeds) { verify(v[0], v[1], v[2], v[3]); checks++; }
    var r = new Random(0x6d68726cL);
    for (int i = 0; i < 100_000; i++) {
      int x = r.nextInt(), z = r.nextInt(), cx = r.nextInt(), cz = r.nextInt();
      verify(x, z, cx, cz);
      checks++;
    }
    // The historical int-before-long subtraction incorrectly sees these as neighbors.
    int wrapped = Math.abs(Integer.MIN_VALUE - Integer.MAX_VALUE);
    if (wrapped != 1 || LodChunkDistance.chebyshev(Integer.MIN_VALUE, 0, Integer.MAX_VALUE, 0)
        != 4_294_967_295L) throw new AssertionError("Far signed wrap admitted");
    if (!(LodChunkDistance.squared(Integer.MIN_VALUE, Integer.MIN_VALUE,
            Integer.MAX_VALUE, Integer.MAX_VALUE)
        > LodChunkDistance.squared(Integer.MAX_VALUE, Integer.MAX_VALUE,
            Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1)))
      throw new AssertionError("Far page sorted ahead of neighbor");
    System.out.println("LOD_SIGNED_DISTANCE_PASS exactOracle=" + checks
        + " fullIntRange=true finiteSquare=true nearRange=true negativeSeams=true");
  }

  private static void verify(int x, int z, int cx, int cz) {
    BigInteger dx = BigInteger.valueOf(x).subtract(BigInteger.valueOf(cx));
    BigInteger dz = BigInteger.valueOf(z).subtract(BigInteger.valueOf(cz));
    long expectedRing = dx.abs().max(dz.abs()).longValueExact();
    BigInteger expectedSquared = dx.multiply(dx).add(dz.multiply(dz));
    long actualRing = LodChunkDistance.chebyshev(x, z, cx, cz);
    double actualSquared = LodChunkDistance.squared(x, z, cx, cz);
    if (actualRing != expectedRing || !Double.isFinite(actualSquared)
        || Math.abs(actualSquared - expectedSquared.doubleValue()) > 2 * Math.ulp(expectedSquared.doubleValue()))
      throw new AssertionError("Signed-distance oracle mismatch at " + x + "," + z
          + " from " + cx + "," + cz);
    if ((actualRing <= 512) != (expectedRing <= 512))
      throw new AssertionError("Radius admission diverged");
    if (actualRing == 0 && actualSquared != 0)
      throw new AssertionError("Origin distance");
  }
}
