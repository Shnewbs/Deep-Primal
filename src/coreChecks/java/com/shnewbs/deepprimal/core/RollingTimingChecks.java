package com.shnewbs.deepprimal.core;

import java.util.*;

public final class RollingTimingChecks {
  public static void main(String[] args) throws Exception {
    int checks = 0;
    for (int capacity : new int[] {1, 2, 17, 240, 4096}) {
      var timing = new RollingTiming(capacity);
      var oracle = new ArrayDeque<Long>();
      var random = new Random(73);
      for (int i = 0; i < 6000; i++) {
        long v = random.nextInt(100000);
        timing.record(v); oracle.addLast(v);
        if (oracle.size() > capacity) oracle.removeFirst();
        if (i % 19 != 0) continue;
        long[] sorted = oracle.stream().mapToLong(Long::longValue).sorted().toArray();
        var s = timing.snapshot();
        if (s.samples() != sorted.length || s.p50Nanos() != percentile(sorted, .50)
            || s.p95Nanos() != percentile(sorted, .95) || s.p99Nanos() != percentile(sorted, .99)
            || s.maxNanos() != sorted[sorted.length - 1]) throw new AssertionError(s);
        checks++;
      }
      timing.reset();
      if (timing.snapshot().samples() != 0) throw new AssertionError("Reset");
      timing.record(Long.MAX_VALUE);
      if (timing.snapshot().p99Nanos() != Long.MAX_VALUE) throw new AssertionError("Overflow");
      try { timing.record(-1); throw new AssertionError("Negative accepted"); }
      catch (IllegalArgumentException expected) {}
      if (timing.snapshot().samples() != 1) throw new AssertionError("Rejected sample mutated window");
    }
    for (int capacity : new int[] {0, -1, 4097}) {
      try { new RollingTiming(capacity); throw new AssertionError("Capacity accepted"); }
      catch (IllegalArgumentException expected) {}
    }
    var shared = new RollingTiming(240);
    Thread writer = new Thread(() -> { for (int i = 0; i < 100000; i++) shared.record(i); });
    writer.start();
    while (writer.isAlive()) {
      var s = shared.snapshot();
      if (s.samples() < 0 || s.samples() > 240 || s.p50Nanos() > s.p95Nanos()
          || s.p95Nanos() > s.p99Nanos() || s.p99Nanos() > s.maxNanos())
        throw new AssertionError("Concurrent snapshot");
    }
    writer.join();
    if (shared.snapshot().samples() != 240) throw new AssertionError("Lost samples");
    System.out.println("ROLLING_TIMING_PASS oracleWindows=" + checks + " samples=30000 concurrentWrites=100000 bounded=4096 reset=true overflow=true");
  }
  private static long percentile(long[] values, double fraction) {
    return values[(int)Math.ceil(values.length * fraction) - 1];
  }
}
