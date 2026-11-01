package com.shnewbs.deepprimal.core;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;

/** Negative durations must never fabricate zero-latency ticks or evict valid samples. */
public final class TickWindowDurationChecks {
  private TickWindowDurationChecks() {}

  public static void main(String[] args) {
    long checks = 0;
    for (int capacity : new int[] {1, 2, 3, 16, 32}) {
      TickWindow window = new TickWindow(capacity);
      for (long invalid : new long[] {-1L, Long.MIN_VALUE, -1_000_000L}) {
        window.record(invalid);
        require(window.count() == 0, "invalid empty admission");
        require(window.snapshot().count() == 0, "invalid empty snapshot");
        checks += 2;
      }
      window.record(5_000_000L);
      for (int i = 0; i < 100; i++) window.record(-1L);
      require(window.count() == 1, "invalid sample evicted valid sample");
      require(window.snapshot().medianMs() == 5.0, "negative became fake zero");
      checks += 2;
    }
    Random rng = new Random(0x5EED_2026_1101L);
    long valid = 0, invalid = 0, clears = 0, snapshots = 0;
    for (int trial = 0; trial < 2000; trial++) {
      int capacity = 1 + rng.nextInt(32);
      TickWindow actual = new TickWindow(capacity);
      ArrayDeque<Long> oracle = new ArrayDeque<>();
      for (int step = 0; step < 128; step++) {
        if (step > 0 && step % 97 == 0) {
          actual.clear();
          oracle.clear();
          clears++;
        }
        long value = switch (rng.nextInt(16)) {
          case 0 -> Long.MIN_VALUE;
          case 1, 2, 3 -> -1L - rng.nextInt(100000);
          case 4 -> 0L;
          case 5 -> Long.MAX_VALUE;
          default -> rng.nextLong() & Long.MAX_VALUE;
        };
        actual.record(value);
        if (value >= 0) {
          if (oracle.size() == capacity) oracle.removeFirst();
          oracle.addLast(value);
          valid++;
        } else invalid++;
        require(actual.count() == oracle.size(), "count mismatch trial=" + trial + " step=" + step);
        checks++;
        if (step % 9 == 0 || value < 0) {
          compare(actual.snapshot(), oracle, trial, step);
          snapshots++;
          checks += 5;
        }
      }
    }
    require(valid + invalid == 256000L, "randomized operation coverage");
    require(clears == 2000, "clear coverage");
    checks += 2;
    System.out.println("TICK_WINDOW_DURATION_PASS checks=" + checks
        + " operations=" + (valid + invalid) + " valid=" + valid + " rejected=" + invalid
        + " snapshots=" + snapshots + " clears=" + clears + " capacities=1..32");
  }

  private static void compare(SampleStats actual, ArrayDeque<Long> deque, int trial, int step) {
    int n = deque.size();
    require(actual.count() == n, "snapshot count trial=" + trial + " step=" + step);
    if (n == 0) {
      require(actual.medianMs() == 0.0 && actual.p95Ms() == 0.0
          && actual.p99Ms() == 0.0 && actual.maxMs() == 0.0, "empty snapshot");
      return;
    }
    long[] sorted = new long[n];
    int i = 0;
    for (Long value : deque) sorted[i++] = value;
    Arrays.sort(sorted);
    require(actual.medianMs() == ms(sorted[rank(n, 50)]), "p50 trial=" + trial + " step=" + step);
    require(actual.p95Ms() == ms(sorted[rank(n, 95)]), "p95 trial=" + trial + " step=" + step);
    require(actual.p99Ms() == ms(sorted[rank(n, 99)]), "p99 trial=" + trial + " step=" + step);
    require(actual.maxMs() == ms(sorted[n - 1]), "max trial=" + trial + " step=" + step);
  }

  private static int rank(int n, int pct) { return (n * pct + 99) / 100 - 1; }
  private static double ms(long nanos) { return nanos / 1_000_000.0; }
  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
}
