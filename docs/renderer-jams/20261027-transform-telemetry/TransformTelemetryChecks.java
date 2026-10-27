package com.shnewbs.deepprimal.lod.client;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

/** Dependency-free renderer diagnostics oracle; NOT installed in production. */
public final class TransformTelemetryChecks {
  private static long checks;
  private static void ok(boolean condition, String why) {
    checks++;
    if (!condition) throw new AssertionError(why);
  }

  public static void main(String[] args) {
    ReusableTransformArrays<String> c = new ReusableTransformArrays<>(4, 32768);
    AtomicInteger produced = new AtomicInteger();
    IntFunction<String[]> factory = count -> {
      String[] result = new String[count];
      Arrays.fill(result, "generation-" + produced.incrementAndGet());
      return result;
    };
    var initial = c.snapshot();
    ok(initial.requests() == 0 && initial.lastDrawCount() == -1, "initial metrics");
    int[] counts = {0, 512, 513, 2048, 2049, 8192, 8193, 32768, 32769};
    for (int n : counts) ok(c.acquire(n, factory).length == n, "draw exact " + n);
    var first = c.snapshot();
    ok(first.requests() == counts.length && first.allocations() == counts.length,
        "one factory call per completed cold acquire");
    ok(first.uncachedOversized() == 1, "oversize request not counted");
    ok(first.lastDrawCount() == 32769 && first.peakDrawCount() == 32769,
        "visible draw-count peak missing");
    ok(first.count0to512() == 2 && first.count513to2048() == 2
        && first.count2049to8192() == 2 && first.count8193to32768() == 2
        && first.countOver32768() == 1, "histogram boundary mismatch");
    ok(first.retainedElements() <= 32768 && first.retainedArrays() <= 4,
        "cache no longer bounded");
    ok(first.peakRetainedElements() <= 32768, "peak retained exceeded budget");
    var beforeBad = c.snapshot();
    try { c.acquire(19, n -> null); throw new AssertionError("null accepted"); }
    catch (NullPointerException expected) {}
    try { c.acquire(19, n -> new String[20]); throw new AssertionError("nonexact accepted"); }
    catch (IllegalArgumentException expected) {}
    ok(beforeBad.equals(c.snapshot()), "failed construction mutated diagnostics/cache");
    c.clear();
    var cleared = c.snapshot();
    ok(cleared.requests() == 0 && cleared.allocations() == 0 && cleared.hits() == 0
        && cleared.evictions() == 0 && cleared.uncachedOversized() == 0
        && cleared.retainedElements() == 0 && cleared.retainedArrays() == 0
        && cleared.lastDrawCount() == -1 && cleared.peakDrawCount() == 0
        && cleared.count0to512() == 0 && cleared.countOver32768() == 0,
        "world reset did not clear counters");

    ReusableTransformArrays<String> randomized = new ReusableTransformArrays<>(4, 31);
    LinkedHashMap<Integer, String[]> reference = new LinkedHashMap<>();
    Random rng = new Random(20261027L);
    long[] bins = new long[5];
    long hits = 0, allocations = 0, bypass = 0, evictions = 0, requested = 0;
    for (int i = 0; i < 120000; i++) {
      if (i % 3499 == 0) {
        randomized.clear();
        reference.clear();
        hits = allocations = bypass = evictions = requested = 0;
        Arrays.fill(bins, 0);
      }
      int length = rng.nextInt(48);
      String[] prior = reference.get(length);
      String[] actual = randomized.acquire(length, factory);
      ok(actual.length == length, "exact length " + i);
      if (prior == null) {
        allocations++;
        if (length > 31) bypass++;
        else {
          int retained = reference.keySet().stream().mapToInt(Integer::intValue).sum();
          while (!reference.isEmpty() && (reference.size() == 4 || retained > 31 - length)) {
            Iterator<Integer> it = reference.keySet().iterator();
            int old = it.next(); it.remove();
            retained -= old;
            evictions++;
          }
          reference.put(length, actual);
        }
      } else {
        hits++;
        ok(actual == prior, "reused identity changed " + i);
      }
      requested++;
      bins[length <= 512 ? 0 : length <= 2048 ? 1 : length <= 8192 ? 2
          : length <= 32768 ? 3 : 4]++;
      var stats = randomized.snapshot();
      ok(stats.requests() == requested && stats.hits() == hits
          && stats.allocations() == allocations && stats.uncachedOversized() == bypass,
          "hit/miss/oversized counters drift " + i);
      ok(stats.evictions() == evictions, "eviction accounting drift " + i);
      ok(stats.retainedArrays() == reference.size()
          && stats.retainedElements() == reference.keySet().stream().mapToInt(Integer::intValue).sum(),
          "retention accounting drift " + i);
      ok(stats.count0to512() == bins[0] && stats.count513to2048() == bins[1]
          && stats.count2049to8192() == bins[2] && stats.count8193to32768() == bins[3]
          && stats.countOver32768() == bins[4], "histogram drift " + i);
      ok(stats.lastDrawCount() == length && stats.peakRetainedElements() <= 31,
          "last draw count or peak retained drift " + i);
    }
    // A repeated, bounded workload must still reuse both arrays with zero evictions.
    randomized.clear();
    for (int i = 0; i < 10000; i++) randomized.acquire(5 + i % 2, factory);
    var repeated = randomized.snapshot();
    ok(repeated.allocations() == 2 && repeated.hits() == 9998
        && repeated.evictions() == 0 && repeated.retainedElements() == 11,
        "hot-case performance regression");
    System.out.println("TRANSFORM_TELEMETRY_PASS checks=" + checks
        + " randomized=120000 buckets=5 retentionBudget=31"
        + " factoryCalls=2 warmFrames=10000 measuredGpuFps=false");
  }
}
