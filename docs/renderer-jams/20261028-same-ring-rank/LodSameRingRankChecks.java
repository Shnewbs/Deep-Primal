package com.shnewbs.deepprimal.lod.client;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

/** Independent exact-distance oracle for same-ring rank prechecks. */
public final class LodSameRingRankChecks {
  private static long checks;
  private LodSameRingRankChecks() {}
  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }
  private static void check(boolean b, String context) {
    checks++;
    if (!b) throw new AssertionError(context);
  }
  private static Comparator<Long> oracle(int cx, int cz) {
    return Comparator.<Long>comparingLong(k -> Math.max(
        Math.abs((long) (int) (k >> 32) - cx),
        Math.abs((long) (int) (long) k - cz)))
        .thenComparing(k -> {
          BigInteger dx = BigInteger.valueOf((long) (int) (k >> 32) - cx);
          BigInteger dz = BigInteger.valueOf((long) (int) (long) k - cz);
          return dx.multiply(dx).add(dz.multiply(dz));
        })
        .thenComparingInt(k -> (int) (k >> 32))
        .thenComparingInt(k -> (int) (long) k);
  }
  private static void caseCheck(List<Long> input, int cx, int cz, int capacity,
      String scenario) {
    var seen = new HashSet<>(input);
    var expected = new ArrayList<>(seen);
    expected.sort(oracle(cx, cz));
    var actual = LodDiskNearestScan.reconcile(new LinkedHashSet<>(input),
        input, cx, cz, capacity);
    check(actual.size() == Math.min(capacity, seen.size()), scenario + " size");
    for (int i = 0; i < actual.size(); i++)
      check(actual.get(i).equals(expected.get(i)), scenario + " rank " + i);
  }
  public static void main(String[] args) {
    // Populate the same Chebyshev ring with near, farther and equal-square
    // candidates; the key tie-break must choose signed x, then signed z.
    var ring = new ArrayList<Long>();
    for (int a = -1000; a <= 1000; a += 13) {
      ring.add(key(-1000, a));
      ring.add(key(1000, a));
      ring.add(key(a, -1000));
      ring.add(key(a, 1000));
    }
    ring.addAll(List.of(key(1000, 0), key(-1000, 0), key(0, 1000),
        key(0, -1000), key(1000, 0), key(1000, 1), key(1000, -1),
        key(1000, 999), key(1000, -999)));
    Random random = new Random(0x5e738ea09c0L);
    for (int round = 0; round < 40; round++) {
      java.util.Collections.shuffle(ring, random);
      for (int capacity : new int[] {1, 2, 7, 31, 256, 512, 8192})
        caseCheck(ring, 0, 0, capacity, "same-ring-" + round + "-" + capacity);
    }
    // Signed-coordinate extremes: dx and dz exceed signed-int range and
    // dx^2+dz^2 can exceed signed-long range. The oracle uses BigInteger.
    var extremes = new ArrayList<Long>();
    int[] values = {Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -1, 0, 1,
        Integer.MAX_VALUE - 1, Integer.MAX_VALUE};
    for (int x : values) for (int z : values) extremes.add(key(x, z));
    for (int n = 0; n < 1000; n++) extremes.add(key(random.nextInt(), random.nextInt()));
    for (int[] camera : new int[][] {{Integer.MIN_VALUE, Integer.MAX_VALUE},
        {Integer.MAX_VALUE, Integer.MIN_VALUE}, {0, 0}, {-100000, 100000}}) {
      java.util.Collections.shuffle(extremes, random);
      for (int capacity : new int[] {1, 7, 256, 512, 8192})
        caseCheck(extremes, camera[0], camera[1], capacity, "extremes-" + capacity);
    }
    // Exact equal-square tie at full capacity must stay deterministic.
    check(LodDiskNearestScan.reconcile(
        new LinkedHashSet<>(List.of(key(1000, 0))),
        List.of(key(-1000, 0)), 0, 0, 1).getFirst() == key(-1000, 0),
        "signed coordinate tie");
    System.out.println("LOD_SAME_RING_RANK_PASS assertions=" + checks
        + " randomizedRounds=40 bigIntegerOracle=true");
  }
}
