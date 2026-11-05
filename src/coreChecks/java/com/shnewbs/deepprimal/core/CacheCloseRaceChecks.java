package com.shnewbs.deepprimal.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** A world-scoped cache must not deliver a load after its terminal close. */
public final class CacheCloseRaceChecks {
  private static int assertions;
  private static void check(boolean value, String reason) {
    assertions++;
    if (!value) throw new AssertionError(reason);
  }
  public static void main(String[] args) throws Exception {
    ExecutorService pool = Executors.newSingleThreadExecutor();
    try {
      for (int i = 0; i < 1000; i++) {
        ChunkCache cache = new ChunkCache(1, 4);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger called = new AtomicInteger();
        var load = pool.submit(() -> cache.getOrCompute("chunk", key -> {
          called.incrementAndGet();
          entered.countDown();
          try { release.await(); }
          catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
          return "obsolete";
        }, 1));
        check(entered.await(5, TimeUnit.SECONDS), "load must begin");
        cache.close();
        check(!load.isDone(), "close must not wait for loader");
        release.countDown();
        try {
          load.get(5, TimeUnit.SECONDS);
          throw new AssertionError("closed world returned obsolete value");
        } catch (ExecutionException e) {
          check(e.getCause() instanceof IllegalStateException, "terminal close rejects late completion");
        }
        check(called.get() == 1, "only original loader ran");
        check(cache.size() == 0 && cache.getMemoryUsage() == 0, "no cache admission");
      }
    } finally {
      pool.shutdownNow();
      check(pool.awaitTermination(5, TimeUnit.SECONDS), "worker terminated");
    }
    System.out.println("CACHE_CLOSE_RACE_PASS assertions=" + assertions);
  }
}
