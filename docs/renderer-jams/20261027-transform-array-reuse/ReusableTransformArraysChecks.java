package com.shnewbs.deepprimal.lod.client;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/** Standalone exact-array cache regression, no Minecraft or renderer mock artifacts. */
public final class ReusableTransformArraysChecks {
  private static long checks;
  private static void require(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }
  private static void rejects(Runnable call) {
    boolean rejected = false;
    try { call.run(); } catch (IllegalArgumentException | NullPointerException expected) {
      rejected = true;
    }
    require(rejected, "unsafe factory/size accepted");
  }
  public static void main(String[] args) {
    rejects(() -> new ReusableTransformArrays<String>(0));
    rejects(() -> new ReusableTransformArrays<String>(17));
    var cache = new ReusableTransformArrays<String>(4);
    AtomicInteger allocations = new AtomicInteger();
    java.util.function.IntFunction<String[]> factory = n -> {
      int generation = allocations.incrementAndGet();
      String[] result = new String[n];
      java.util.Arrays.fill(result, "generation-" + generation);
      return result;
    };
    String[][] first = new String[4][];
    for (int i = 0; i < 4; i++) first[i] = cache.acquire(37 + i, factory);
    for (int repeat = 0; repeat < 100_000; repeat++) {
      int offset = repeat % 4;
      String[] arr = cache.acquire(37 + offset, factory);
      require(arr == first[offset], "reused transform array identity");
      require(arr.length == 37 + offset, "exact draw count");
      require("generation-" + (offset + 1) == arr[0] ||
          ("generation-" + (offset + 1)).equals(arr[0]), "stable transform instances");
    }
    require(allocations.get() == 4, "repeated fluctuating visibility allocated again");
    String[] evicted = cache.acquire(101, factory);
    require(evicted.length == 101, "new count");
    require(cache.acquire(37, factory) != first[0], "oldest cache entry not retired");
    int before = allocations.get();
    cache.clear();
    require(cache.acquire(38, factory) != first[1], "world reset retains former transforms");
    require(allocations.get() == before + 1, "reset did not free entries");
    require(cache.acquire(0, factory).length == 0, "zero count");
    rejects(() -> cache.acquire(-1, factory));
    rejects(() -> cache.acquire(1, n -> new String[2]));
    rejects(() -> cache.acquire(1, n -> null));
    rejects(() -> cache.acquire(1, null));
    // Compare deterministic work: old one-length cache creates on each change;
    // the four-slot cache creates once per distinct count in a steady scene.
    cache.clear();
    int createdBefore = allocations.get();
    int previousLength = -1, baselineAllocations = 0;
    for (int frame = 0; frame < 20_000; frame++) {
      int count = 200 + ((frame & 1) == 0 ? 0 : 1);
      if (previousLength != count) baselineAllocations++;
      String[] arr = cache.acquire(count, factory);
      require(arr.length == count, "changing visible count");
      previousLength = count;
    }
    require(allocations.get() == createdBefore + 2, "steady toggling cache allocations");
    require(baselineAllocations == 20_000, "baseline single-length churn control");
    System.out.println("REUSABLE_TRANSFORM_ARRAYS_PASS checks=" + checks +
        " baselineTransformArrays=" + baselineAllocations +
        " candidateTransformArrays=" + (allocations.get() - createdBefore) +
        " allocationDelta=" + (baselineAllocations - (allocations.get() - createdBefore)) +
        " gpuTimingClaim=false");
  }
}
