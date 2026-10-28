package com.shnewbs.deepprimal.lod.client;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

/** Source-only exact nearest ordering after the full-heap Chebyshev rejection. */
public final class LodNearRingFastRejectChecks {
  private static long assertions;
  private LodNearRingFastRejectChecks() {}

  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private static void check(boolean condition, String what) {
    assertions++;
    if (!condition) throw new AssertionError(what);
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

  public static void main(String[] args) {
    LinkedHashSet<Long> existing = new LinkedHashSet<>();
    ArrayList<Long> incoming = new ArrayList<>();
    // Full queues of far keys followed by near keys, edge-equidistant points,
    // duplicate keys, and extreme signed values stress both reject and tie paths.
    for (int n = 0; n < 4000; n++) existing.add(key(100000 + n, -90000 - n));
    for (int n = 0; n < 4000; n++) incoming.add(key(n % 128 - 64, n / 128 - 16));
    incoming.addAll(List.of(key(10, 9), key(10, 0), key(10, -1),
        key(-10, 0), key(0, 10), key(0, -10), key(10, 0),
        key(Integer.MIN_VALUE, Integer.MAX_VALUE), key(Integer.MAX_VALUE, Integer.MIN_VALUE)));
    Random random = new Random(0x42a2e6fL);
    for (int n = 0; n < 200; n++) incoming.add(key(random.nextInt(), random.nextInt()));
    HashSet<Long> all = new HashSet<>(existing);
    all.addAll(incoming);
    for (int[] camera : new int[][] {{0, 0}, {Integer.MIN_VALUE, Integer.MAX_VALUE},
        {Integer.MAX_VALUE, Integer.MIN_VALUE}, {10, -5}}) {
      ArrayList<Long> expected = new ArrayList<>(all);
      expected.sort(oracle(camera[0], camera[1]));
      for (int capacity : new int[] {1, 7, 256, 512, 8192}) {
        List<Long> actual = LodDiskNearestScan.reconcile(existing, incoming,
            camera[0], camera[1], capacity);
        check(actual.size() == Math.min(capacity, expected.size()), "capacity=" + capacity);
        for (int i = 0; i < actual.size(); i++)
          check(actual.get(i).equals(expected.get(i)), "exact nearest mismatch at " + i);
      }
    }
    // Equal Chebyshev ring but better squared distance MUST win, even if it
    // arrives after a full queue. An incorrect >= early reject loses this key.
    check(LodDiskNearestScan.reconcile(new LinkedHashSet<>(List.of(key(10, 9))),
        List.of(key(10, 0)), 0, 0, 1).getFirst() == key(10, 0),
        "equal-ring candidate was incorrectly rejected");
    System.out.println("LOD_NEAR_RING_FAST_REJECT_PASS assertions=" + assertions
        + " distinct=" + all.size() + " caps=1,7,256,512,8192");
  }
}
