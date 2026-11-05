package com.shnewbs.deepprimal.core;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;

/** Exact percentile parity and snapshot immutability across partial/full rings. */
public final class TickWindowSnapshotChecks {
  private TickWindowSnapshotChecks() {}
  private static long checks;

  private static void check(boolean condition, String context) {
    checks++;
    if (!condition) throw new AssertionError(context);
  }

  private static void invalid(Runnable call, String context) {
    try { call.run(); }
    catch (IllegalArgumentException expected) { checks++; return; }
    throw new AssertionError(context);
  }

  private static double ms(long nanos) { return nanos / 1_000_000.0; }
  private static double rank(long[] sorted, int percent) {
    return ms(sorted[(int) ((sorted.length * (long) percent + 99) / 100 - 1)]);
  }
  private static void verify(TickWindow ring, ArrayDeque<Long> expected) {
    SampleStats snapshot = ring.snapshot();
    check(snapshot.count() == expected.size(), "count");
    if (expected.isEmpty()) {
      check(snapshot.medianMs() == 0 && snapshot.p95Ms() == 0 && snapshot.p99Ms() == 0
          && snapshot.maxMs() == 0, "empty");
      return;
    }
    long[] sorted = new long[expected.size()];
    int i = 0;
    for (long value : expected) sorted[i++] = value;
    Arrays.sort(sorted);
    check(snapshot.medianMs() == rank(sorted, 50), "median");
    check(snapshot.p95Ms() == rank(sorted, 95), "p95");
    check(snapshot.p99Ms() == rank(sorted, 99), "p99");
    check(snapshot.maxMs() == ms(sorted[sorted.length - 1]), "max");
  }

  public static void main(String[] args) {
    long[] caller = {9000000, 1000000, 5000000, 3000000};
    long[] before = caller.clone();
    SampleStats whole = SampleStats.of(caller);
    check(Arrays.equals(caller, before), "public API mutated input");
    check(whole.count() == 4 && whole.medianMs() == 3 && whole.p95Ms() == 9, "public API");
    check(SampleStats.of(new long[]{4000000, 1000000, -1}, 2).medianMs() == 1,
        "unused ring suffix must not be validated");
    invalid(() -> SampleStats.of(new long[]{100, -1}, 2), "negative initialized value");
    invalid(() -> SampleStats.of(new long[]{100}, 0), "zero initialized count");
    invalid(() -> SampleStats.of(new long[]{100}, 2), "oversized initialized count");
    invalid(() -> SampleStats.of(new long[0]), "empty public API");

    Random rng = new Random(0x202611020700L);
    long operations = 0;
    for (int capacity : new int[]{1, 2, 3, 7, 16, 64, 256}) {
      TickWindow ring = new TickWindow(capacity);
      ArrayDeque<Long> oracle = new ArrayDeque<>();
      for (int step = 0; step < 20000; step++) {
        if (step % 1013 == 0) { ring.clear(); oracle.clear(); }
        long sample = rng.nextInt(16) == 0 ? -1 : (rng.nextLong() & Long.MAX_VALUE);
        ring.record(sample);
        if (sample >= 0) {
          if (oracle.size() == capacity) oracle.removeFirst();
          oracle.addLast(sample);
        }
        operations++;
        if (step % 3 == 0 || step < capacity + 2) {
          verify(ring, oracle);
          // A snapshot must not change subsequent samples, even after wrap.
          verify(ring, oracle);
        }
      }
    }
    System.out.println("TICK_WINDOW_SNAPSHOT_PASS checks=" + checks + " operations=" + operations);
  }
}
