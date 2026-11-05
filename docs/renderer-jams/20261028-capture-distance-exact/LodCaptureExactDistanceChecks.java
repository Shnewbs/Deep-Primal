package com.shnewbs.deepprimal.lod.client;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/** Independent exact-distance oracle for bounded dirty-chunk capture order. */
public final class LodCaptureExactDistanceChecks {
  private static long assertions;
  private LodCaptureExactDistanceChecks() {}

  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private static void check(boolean ok, String detail) {
    assertions++;
    if (!ok) throw new AssertionError(detail);
  }

  private static BigInteger squareDistance(long key, int cameraX, int cameraZ) {
    BigInteger x = BigInteger.valueOf((long) (int) (key >> 32) - cameraX);
    BigInteger z = BigInteger.valueOf((long) (int) key - cameraZ);
    return x.multiply(x).add(z.multiply(z));
  }

  private static long ring(long key, int cameraX, int cameraZ) {
    long x = (long) (int) (key >> 32) - cameraX;
    long z = (long) (int) key - cameraZ;
    return Math.max(Math.abs(x), Math.abs(z));
  }

  private static long exactNearest(LinkedHashMap<Long, Long> queue, int cameraX, int cameraZ) {
    long best = queue.keySet().iterator().next();
    long bestRing = ring(best, cameraX, cameraZ);
    BigInteger bestDistance = squareDistance(best, cameraX, cameraZ);
    for (long candidate : queue.keySet()) {
      long candidateRing = ring(candidate, cameraX, cameraZ);
      if (candidateRing > bestRing) continue;
      BigInteger distance = squareDistance(candidate, cameraX, cameraZ);
      if (candidateRing < bestRing || distance.compareTo(bestDistance) < 0) {
        best = candidate;
        bestRing = candidateRing;
        bestDistance = distance;
      }
    }
    return best;
  }

  private static void verify(LinkedHashMap<Long, Long> queue, int cameraX, int cameraZ) {
    long first = queue.keySet().iterator().next();
    long expected = exactNearest(queue, cameraX, cameraZ);
    int size = queue.size();
    long nearest = LodCapturePriority.choose(queue, cameraX, cameraZ, false);
    check(nearest == expected,
        "near priority mismatch camera=" + cameraX + "," + cameraZ + " expected=" + expected
            + " got=" + nearest);
    check(LodCapturePriority.choose(queue, cameraX, cameraZ, true) == first,
        "oldest-first starvation escape was changed");
    check(queue.size() == size && queue.keySet().iterator().next() == first,
        "capture chooser mutated insertion order");
  }

  public static void main(String[] args) {
    LinkedHashMap<Long, Long> queue = new LinkedHashMap<>();
    boolean rejected = false;
    try {
      LodCapturePriority.choose(queue, 0, 0, false);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    check(rejected, "empty queue accepted");

    queue.put(key(Integer.MAX_VALUE, 2), 1L);
    queue.put(key(Integer.MAX_VALUE, 1), 2L);
    verify(queue, Integer.MIN_VALUE, 0);
    // Both coordinates are on the same enormous Chebyshev ring. The old
    // double-based squared-distance compare rounds the 3-unit difference away.
    check(LodCapturePriority.choose(queue, Integer.MIN_VALUE, 0, false)
        == key(Integer.MAX_VALUE, 1), "lost exact high-range squared-distance tie");
    queue.clear();
    queue.put(key(1, 1), 1L);
    queue.put(key(-1, -1), 2L);
    verify(queue, 0, 0);
    check(LodCapturePriority.choose(queue, 0, 0, false) == key(1, 1),
        "exact equal distance must retain insertion order");

    Random rng = new Random(0x52aca20261028L);
    for (int n = 0; n < 260; n++) {
      queue.clear();
      int centerX = n % 3 == 0 ? Integer.MIN_VALUE
          : n % 3 == 1 ? Integer.MAX_VALUE : rng.nextInt();
      int centerZ = n % 4 == 0 ? Integer.MAX_VALUE
          : n % 4 == 1 ? Integer.MIN_VALUE : rng.nextInt();
      int count = 1 + rng.nextInt(129);
      for (int k = 0; k < count; k++) {
        int x = (k % 3 == 0) ? Integer.MAX_VALUE
            : (k % 3 == 1) ? Integer.MIN_VALUE : rng.nextInt();
        int z = (k % 4 == 0) ? (k % 2) : rng.nextInt();
        queue.put(key(x, z), (long) k);
      }
      verify(queue, centerX, centerZ);
    }
    // Host renderer limits the dirty queue to 4096. Exercise full-size
    // queues with both far extremes and nearby updates, without collecting
    // a sorted copy in the production chooser.
    for (int n = 0; n < 8; n++) {
      queue.clear();
      int cx = n % 2 == 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
      int cz = n % 2 == 1 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
      for (int i = 0; i < 4096; i++)
        queue.put(key(rng.nextInt(), rng.nextInt()), (long) i);
      verify(queue, cx, cz);
    }
    long distinct = queue.keySet().stream().distinct().count();
    check(distinct == queue.size(), "duplicate dirty queue keys");
    for (Map.Entry<Long, Long> entry : queue.entrySet())
      check(entry.getValue() >= 0, "negative work owner");
    System.out.println("LOD_CAPTURE_EXACT_DISTANCE_PASS assertions=" + assertions
        + " maximalDirty=4096 seededOracles=268");
  }
}
