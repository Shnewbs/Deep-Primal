package com.shnewbs.deepprimal.core;

import java.util.ArrayDeque;

public final class TransitionIntervalChecks {
  public static void main(String[] args) {
    int checks = 0;
    for (int capacity : new int[] {1, 17, 64, 512}) {
      var window = new FrameIntervalWindow(capacity);
      Object world = new Object();
      long now = 0;
      observe(window, now, world);
      if (window.transitionSnapshot().samples() != 0 || window.transitionCount() != 0)
        throw new AssertionError("Invented first transition");
      var oracle = new ArrayDeque<Long>();
      for (int i = 0; i < 256; i++) {
        now += 7;
        observe(window, now, world);
        if (window.snapshot().p99Nanos() != 7) throw new AssertionError("Steady interval");
        long gap = 1000000L + i * 7919L;
        now += gap;
        world = new Object();
        observe(window, now, world);
        oracle.addLast(gap);
        if (oracle.size() > Math.min(capacity, 64)) oracle.removeFirst();
        long[] sorted = oracle.stream().mapToLong(Long::longValue).sorted().toArray();
        var s = window.transitionSnapshot();
        if (window.transitionCount() != i + 1 || s.samples() != sorted.length
            || s.p50Nanos() != percentile(sorted, 50) || s.p95Nanos() != percentile(sorted, 95)
            || s.p99Nanos() != percentile(sorted, 99) || s.maxNanos() != sorted[sorted.length - 1]
            || window.snapshot().samples() != 0) throw new AssertionError("Transition oracle");
        checks++;
      }
      var retained = window.transitionSnapshot();
      now += 7; observe(window, now, world);
      if (!retained.equals(window.transitionSnapshot()) || window.snapshot().p99Nanos() != 7)
        throw new AssertionError("Steady sample polluted transition window");
      observe(window, now - 1, new Object());
      if (!retained.equals(window.transitionSnapshot()) || window.transitionCount() != 256)
        throw new AssertionError("Negative gap admitted");
      window.clear();
      if (window.transitionCount() != 0 || window.transitionSnapshot().samples() != 0)
        throw new AssertionError("Clear retained history");
      observe(window, Long.MAX_VALUE - 2, null);
      observe(window, Long.MIN_VALUE + 2, new Object());
      if (window.transitionCount() != 1 || window.transitionSnapshot().p99Nanos() != 5)
        throw new AssertionError("Crossing clock wrap");
      window.clear();
      observe(window, 100, null); observe(window, 100, new Object());
      if (window.transitionCount() != 1 || window.transitionSnapshot().p99Nanos() != 0)
        throw new AssertionError("Zero gap omitted");
    }
    System.out.println("TRANSITION_INTERVAL_PASS oracleWindows=" + checks
        + " capacities=1,17,64,512 crossingBound=64 noSteadyPollution=true"
        + " clear=true negativeRejected=true wrap=true zero=true");
  }

  private static void observe(FrameIntervalWindow window, long now, Object world) {
    window.observe(now, world, 1920, 1080, true, 120, 32, 16, 192);
  }
  private static long percentile(long[] values, int p) {
    return values[(int) Math.ceil(values.length * p / 100.0) - 1];
  }
}
