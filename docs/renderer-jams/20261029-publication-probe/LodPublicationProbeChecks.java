package com.shnewbs.deepprimal.lod.client;

import java.util.Arrays;
import java.util.Random;

/** Source-only Java21 publication accounting; no GPU/native acceptance. */
public final class LodPublicationProbeChecks {
  private static long assertions;
  private LodPublicationProbeChecks() {}

  private static void check(boolean condition) {
    assertions++;
    if (!condition) throw new AssertionError("publication probe assertion " + assertions);
  }

  private static void expectBad(Runnable r) {
    boolean rejected = false;
    try { r.run(); }
    catch (IllegalArgumentException expected) { rejected = true; }
    check(rejected);
  }

  private static long[] mask(int length, int... bits) {
    long[] result = new long[(length + 63) >>> 6];
    for (int bit : bits) result[bit >>> 6] |= 1L << (bit & 63);
    return result;
  }

  private static long count(long[][] masks) {
    long result = 0;
    for (long[] level : masks) for (int bit = 0; bit < level.length * 64; bit++)
      if ((level[bit >>> 6] & 1L << (bit & 63)) != 0) result++;
    return result;
  }

  public static void main(String[] args) {
    LodPublicationProbe probe = new LodPublicationProbe(240);
    var empty = probe.snapshot();
    check(empty.preparedPages() == 0);
    check(empty.freezeCpuNanos().samples() == 0);
    Object[][] levels = {new Object[130], new Object[3], new Object[0]};
    long[][] adjacent = {mask(130, 1, 63, 64, 129), mask(3, 2), new long[0]};
    long[][] twoBack = {mask(130, 2, 65, 128), mask(3, 1), new long[0]};
    probe.prepared(100, 240, levels, adjacent, twoBack);
    var s = probe.snapshot();
    check(s.preparedPages() == 1);
    check(s.frozenParts() == 133);
    check(s.adjacentAliasBits() == 5);
    check(s.twoBackAliasBits() == 4);
    check(s.freezeCpuNanos().p50Nanos() == 100);
    check(s.metadataCpuNanos().p95Nanos() == 240);
    check(s.totalCpuNanos().p99Nanos() == 340);
    // Broadphase-off legitimately passes empty mask-array sets.
    probe.prepared(200, 0, levels, new long[0][], new long[0][]);
    check(probe.snapshot().preparedPages() == 2);
    check(probe.snapshot().adjacentAliasBits() == 5);
    Random random = new Random(2026102941L);
    long expectedParts = 266, expectedAdjacent = 5, expectedTwoBack = 4;
    for (int i = 0; i < 8000; i++) {
      int n = random.nextInt(260);
      Object[][] page = {new Object[n]};
      long[] adjacentWords = new long[(n + 63) >>> 6];
      long[] twoWords = new long[(n + 63) >>> 6];
      for (int j = 0; j < n; j++) {
        int choice = random.nextInt(5);
        if (choice == 1) adjacentWords[j >>> 6] |= 1L << (j & 63);
        if (choice == 2) twoWords[j >>> 6] |= 1L << (j & 63);
      }
      long[][] a = {adjacentWords}, b = {twoWords};
      long aCount = count(a), bCount = count(b);
      long nanos = random.nextInt(10_000);
      probe.prepared(nanos, 2 * nanos, page, a, b);
      expectedParts += n;
      expectedAdjacent += aCount;
      expectedTwoBack += bCount;
      s = probe.snapshot();
      check(s.preparedPages() == i + 3L);
      check(s.frozenParts() == expectedParts);
      check(s.adjacentAliasBits() == expectedAdjacent);
      check(s.twoBackAliasBits() == expectedTwoBack);
      check(s.totalCpuNanos().samples() == Math.min(i + 3, 240));
      check(s.totalCpuNanos().maxNanos() <= 29997);
    }
    var before = probe.snapshot();
    expectBad(() -> probe.prepared(-1, 0, levels, adjacent, twoBack));
    expectBad(() -> probe.prepared(0, -1, levels, adjacent, twoBack));
    expectBad(() -> probe.prepared(Long.MAX_VALUE, 1, levels, adjacent, twoBack));
    expectBad(() -> probe.prepared(1, 1, null, adjacent, twoBack));
    expectBad(() -> probe.prepared(1, 1, levels, new long[1][], twoBack));
    expectBad(() -> probe.prepared(1, 1, levels, adjacent, new long[1][]));
    expectBad(() -> probe.prepared(1, 1, new Object[][]{null}, new long[0][], new long[0][]));
    expectBad(() -> probe.prepared(1, 1, levels, new long[][]{null, new long[0], new long[0]}, twoBack));
    check(probe.snapshot().equals(before));
    probe.reset();
    s = probe.snapshot();
    check(s.preparedPages() == 0 && s.frozenParts() == 0);
    check(s.adjacentAliasBits() == 0 && s.twoBackAliasBits() == 0);
    check(s.freezeCpuNanos().samples() == 0 && s.totalCpuNanos().samples() == 0);
    System.out.println("LOD_PUBLICATION_PROBE_PASS assertions=" + assertions + " randomPages=8000");
  }
}
