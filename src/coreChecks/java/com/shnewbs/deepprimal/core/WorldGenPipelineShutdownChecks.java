package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.ChunkMaterializer.ChunkKey;
import com.shnewbs.deepprimal.core.ChunkMaterializer.MaterializedChunk;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Shutdown must settle all in-flight worldgen listeners and prevent cache resurrection. */
public final class WorldGenPipelineShutdownChecks {
  private static long checks;
  private static void need(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message + " check=" + checks);
  }
  private static void cancelled(CompletableFuture<?> future) {
    try {
      future.get(2, TimeUnit.SECONDS);
      throw new AssertionError("Query succeeded after shutdown");
    } catch (ExecutionException | CancellationException expected) {
      Throwable failure = expected instanceof ExecutionException ? expected.getCause() : expected;
      need(failure instanceof CancellationException, "expected cancellation: " + failure);
    } catch (java.util.concurrent.TimeoutException timeout) {
      throw new AssertionError("Query remained pending after shutdown", timeout);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new AssertionError(interrupted);
    }
  }
  public static void main(String[] args) {
    for (int i = 0; i < 10_000; i++) {
      ChunkKey key = new ChunkKey(i, -i, "dimension-" + (i & 3));
      CompletableFuture<MaterializedChunk> supplied = new CompletableFuture<>();
      AtomicInteger calls = new AtomicInteger();
      ChunkCache cache = new ChunkCache(1, 64);
      WorldGenPipeline pipeline = new WorldGenPipeline((k, cancelled) -> {
        calls.incrementAndGet();
        return supplied;
      }, cache);
      CompletableFuture<MaterializedChunk> first = pipeline.generateChunk(key);
      CompletableFuture<MaterializedChunk> second = pipeline.generateChunk(key);
      need(calls.get() == 1 && pipeline.pendingCount() == 1, "coalescing before shutdown");
      pipeline.shutdown();
      cancelled(first);
      cancelled(second);
      need(pipeline.pendingCount() == 0 && cache.size() == 0, "shutdown must clear pending/cache");
      supplied.complete(PipelineTestFixtures.chunk(key));
      need(cache.size() == 0, "late completion must not restore cache");
      try {
        pipeline.generateChunk(key).join();
        throw new AssertionError("stopped pipeline accepted a new query");
      } catch (CompletionException expected) {
        need(expected.getCause() instanceof IllegalStateException, "stopped pipeline rejection");
      }
    }
    System.out.println("PIPELINE_SHUTDOWN_PASS assertions=" + checks
        + " trials=10000 coalescedCancellation=true lateCompletion=true noCacheResurrection=true");
  }
}
