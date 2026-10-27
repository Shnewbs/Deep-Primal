package com.shnewbs.deepprimal.lod.client;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

/** Standalone Java 25 FIFO/budget oracle, excluded from normal mod runtime. */
public final class TransformRetentionBudgetChecks {
  private static long checks;
  private static void ok(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }
  private static void rejects(Runnable call) {
    boolean rejected = false;
    try { call.run(); } catch (IllegalArgumentException | NullPointerException expected) {
      rejected = true;
    }
    ok(rejected, "missing input validation");
  }
  public static void main(String[] args) {
    rejects(() -> new ReusableTransformArrays<String>(0, 9));
    rejects(() -> new ReusableTransformArrays<String>(17, 9));
    rejects(() -> new ReusableTransformArrays<String>(4, 0));
    rejects(() -> new ReusableTransformArrays<String>(4, -1));
    ReusableTransformArrays<String> small = new ReusableTransformArrays<>(3, 9);
    AtomicInteger creates = new AtomicInteger();
    IntFunction<String[]> factory = n -> {
      String[] result = new String[n];
      int generation = creates.incrementAndGet();
      java.util.Arrays.fill(result, "serial=" + generation);
      return result;
    };
    String[] two = small.acquire(2, factory);
    String[] three = small.acquire(3, factory);
    String[] four = small.acquire(4, factory);
    ok(small.retainedElements() == 9 && small.retainedArrays() == 3, "initial budget");
    ok(small.acquire(3, factory) == three, "hit keeps identity");
    String[] five = small.acquire(5, factory);
    ok(small.retainedElements() == 9 && small.retainedArrays() == 2, "multi-eviction budget");
    ok(small.acquire(4, factory) == four, "surviving FIFO entry lost");
    int beforeOversize = creates.get();
    String[] oversized = small.acquire(10, factory);
    ok(small.acquire(10, factory) != oversized, "oversized entry cached");
    ok(creates.get() == beforeOversize + 2, "oversized bypass allocation count");
    ok(small.retainedElements() == 9 && small.acquire(5, factory) == five,
        "oversized request evicted hot entries");
    rejects(() -> small.acquire(1, n -> null));
    rejects(() -> small.acquire(1, n -> new String[2]));
    rejects(() -> small.acquire(-1, factory));
    rejects(() -> small.acquire(1, null));
    ok(small.retainedElements() == 9 && small.acquire(5, factory) == five,
        "invalid request evicted live data");
    small.clear();
    ok(small.retainedArrays() == 0 && small.retainedElements() == 0,
        "world reset still retains entries");
    ok(small.acquire(2, factory) != two, "world reset reused stale array");
    ok(small.acquire(0, factory).length == 0, "zero count rejected");

    // Independent insertion-ordered FIFO oracle. Hits do not promote entries.
    int slots = 4, budget = 31;
    ReusableTransformArrays<String> cache = new ReusableTransformArrays<>(slots, budget);
    LinkedHashMap<Integer, String[]> oracle = new LinkedHashMap<>();
    Random random = new Random(20261027);
    for (int i = 0; i < 150_000; i++) {
      if ((i % 2237) == 0) {
        cache.clear();
        oracle.clear();
      }
      int length = random.nextInt(46);
      String[] prior = oracle.get(length);
      int countBefore = creates.get();
      String[] actual = cache.acquire(length, factory);
      ok(actual.length == length, "exact length at sample " + i);
      ok((prior == null && creates.get() == countBefore + 1) ||
          (prior != null && creates.get() == countBefore), "factory call at sample " + i);
      if (prior != null) {
        ok(actual == prior, "cache hit identity at sample " + i);
      } else if (length <= budget) {
        int retained = 0;
        for (int n : oracle.keySet()) retained += n;
        while (!oracle.isEmpty() && (oracle.size() == slots || retained > budget - length)) {
          Iterator<Integer> it = oracle.keySet().iterator();
          int oldest = it.next();
          it.remove();
          retained -= oldest;
        }
        oracle.put(length, actual);
      }
      int expectedElements = 0;
      for (int n : oracle.keySet()) expectedElements += n;
      ok(cache.retainedElements() == expectedElements, "element budget at sample " + i);
      ok(cache.retainedArrays() == oracle.size(), "slot budget at sample " + i);
    }
    cache.clear();
    ok(cache.retainedElements() == 0 && cache.retainedArrays() == 0, "final reset");

    // Preserve the previous fixed-workload allocation win below the byte proxy.
    ReusableTransformArrays<String> hot = new ReusableTransformArrays<>(4, 8192);
    int hotBefore = creates.get(), singleCountBaseline = 0, previous = -1;
    for (int frame = 0; frame < 20_000; frame++) {
      int count = 200 + (frame & 1);
      if (previous != count) singleCountBaseline++;
      ok(hot.acquire(count, factory).length == count, "hot exact count");
      previous = count;
    }
    ok(creates.get() - hotBefore == 2, "hot arrays reallocated");
    ok(singleCountBaseline == 20_000, "legacy churn control");
    System.out.println("TRANSFORM_RETENTION_BUDGET_PASS checks=" + checks +
        " trackedElementLimit=31 oracleOperations=150000" +
        " warmBaselineArrays=" + singleCountBaseline +
        " warmCandidateArrays=" + (creates.get() - hotBefore) +
        " gpuOrHeapTimingClaim=false");
  }
}
