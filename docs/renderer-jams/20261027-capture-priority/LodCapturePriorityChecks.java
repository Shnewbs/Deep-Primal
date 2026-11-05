package com.shnewbs.deepprimal.lod.client;

import java.util.LinkedHashMap;
import java.util.Random;

/** Dependency-free checks of the production renderer capture-selection helper. */
public final class LodCapturePriorityChecks {
  private static long checks;
  private LodCapturePriorityChecks() {}

  private static void equal(long actual, long expected, String message) {
    checks++;
    if (actual != expected) throw new AssertionError(message + ": " + actual + " != " + expected);
  }

  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private static long expected(LinkedHashMap<Long, Long> dirty, int cx, int cz,
      boolean oldestOnly) {
    long selected = 0;
    long bestRing = Long.MAX_VALUE;
    double bestSquared = Double.POSITIVE_INFINITY;
    boolean first = true;
    for (long id : dirty.keySet()) {
      if (first && oldestOnly) return id;
      long dx = (int) (id >> 32) - (long) cx;
      long dz = (int) id - (long) cz;
      long ring = Math.max(Math.abs(dx), Math.abs(dz));
      double squared = (double) dx * dx + (double) dz * dz;
      if (first || ring < bestRing || (ring == bestRing && squared < bestSquared)) {
        selected = id;
        bestRing = ring;
        bestSquared = squared;
      }
      first = false;
    }
    return selected;
  }

  public static void main(String[] args) {
    LinkedHashMap<Long, Long> q = new LinkedHashMap<>();
    try {
      LodCapturePriority.choose(q, 0, 0, false);
      throw new AssertionError("accepted empty queue");
    } catch (IllegalArgumentException correct) { checks++; }
    q.put(key(40, 40), 1L);
    q.put(key(-1, -1), 2L);
    q.put(key(0, 0), 3L);
    equal(LodCapturePriority.choose(q, 0, 0, false), key(0, 0), "camera-near wins");
    equal(LodCapturePriority.choose(q, 0, 0, true), key(40, 40), "fair oldest wins");
    equal(q.size(), 3, "no queue mutation");
    q.clear();
    q.put(key(-9, 2), 1L);
    q.put(key(-11, 2), 2L);
    equal(LodCapturePriority.choose(q, -10, 2, false), key(-9, 2), "stable negative tie");
    q.clear();
    q.put(key(8, 8), 1L);
    q.put(key(8, 0), 2L);
    equal(LodCapturePriority.choose(q, 0, 0, false), key(8, 0), "same-ring near wins");
    q.clear();
    q.put(key(Integer.MAX_VALUE, Integer.MIN_VALUE), 1L);
    q.put(key(Integer.MIN_VALUE, Integer.MAX_VALUE), 2L);
    q.put(key(Integer.MIN_VALUE + 1, Integer.MIN_VALUE), 3L);
    equal(LodCapturePriority.choose(q, Integer.MIN_VALUE, Integer.MIN_VALUE, false),
        key(Integer.MIN_VALUE + 1, Integer.MIN_VALUE), "full int range without overflow");

    Random rng = new Random(0x6fca0b175L);
    for (int iteration = 0; iteration < 2500; iteration++) {
      q.clear();
      int count = 1 + rng.nextInt(513);
      int cx = rng.nextInt(4096) - 2048, cz = rng.nextInt(4096) - 2048;
      for (int j = 0; j < count; j++) {
        int x = j % 59 == 0 ? rng.nextInt() : cx + rng.nextInt(4097) - 2048;
        int z = j % 67 == 0 ? rng.nextInt() : cz + rng.nextInt(4097) - 2048;
        q.put(key(x, z), (long) j);
      }
      boolean oldest = iteration % 8 == 7;
      int priorSize = q.size();
      equal(LodCapturePriority.choose(q, cx, cz, oldest), expected(q, cx, cz, oldest),
          "random camera travel and FIFO fairness " + iteration);
      equal(q.size(), priorSize, "random queue immutable " + iteration);
    }
    // Simulate a 4096-key saturated edit queue. New nearby edits do not wait
    // for the oldest distant entries, but every eighth capture progresses oldest.
    q.clear();
    for (int i = 0; i < 4095; i++) q.put(key(10000 + i, 9000), (long) i);
    q.put(key(-14, 12), 4095L);
    for (int i = 0; i < 256; i++) {
      boolean oldest = i % 8 == 7;
      long selected = LodCapturePriority.choose(q, -14, 12, oldest);
      if (!oldest) equal(selected, key(-14, 12), "saturated near priority");
      else equal(selected, q.keySet().iterator().next(), "saturated bounded fairness");
    }
    equal(q.size(), 4096, "saturated queue unchanged");
    System.out.println("LOD_CAPTURE_PRIORITY_PASS assertions=" + checks
        + " randomQueues=2500 saturatedSelections=256");
  }
}
