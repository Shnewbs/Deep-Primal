package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.ChunkMaterializer.ChunkKey;
import com.shnewbs.deepprimal.core.ChunkMaterializer.MaterializedChunk;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

/** A failed query must retire before its synchronous completion listener can retry it. */
public final class WorldGenPipelineReentrantRetryChecks {
  private static long checks;

  private static void need(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message + " check=" + checks);
  }

  public static void main(String[] args) {
    for (int i = 0; i < 10_000; i++) {
      ChunkKey key = new ChunkKey(i, -i, "retry-" + (i & 7));
      MaterializedChunk good = PipelineTestFixtures.chunk(key);
      CompletableFuture<MaterializedChunk> firstSource = new CompletableFuture<>();
      AtomicInteger calls = new AtomicInteger();
      ChunkCache cache = new ChunkCache(1, 64);
      WorldGenPipeline pipeline = new WorldGenPipeline((requested, cancelled) -> {
        need(requested.equals(key), "dispatch key");
        return calls.incrementAndGet() == 1
            ? firstSource : CompletableFuture.completedFuture(good);
      }, cache);
      CompletableFuture<MaterializedChunk> first = pipeline.generateChunk(key);
      CompletableFuture<MaterializedChunk> second = pipeline.generateChunk(key);
      need(calls.get() == 1 && pipeline.pendingCount() == 1, "pending coalescing");
      CompletableFuture<MaterializedChunk> recovered = first.handle((chunk, failure) -> {
        need(failure != null, "first attempt must fail");
        // Runs inside the original future's completion chain, not after settle returns.
        return pipeline.generateChunk(key).join();
      });
      firstSource.completeExceptionally(new IllegalStateException("transient materializer error"));
      try {
        second.join();
        throw new AssertionError("coalesced first attempt unexpectedly succeeded");
      } catch (CompletionException expected) {
        need(expected.getCause() instanceof IllegalStateException, "original error retained");
      }
      need(recovered.join() == good, "synchronous failure listener must retry");
      need(calls.get() == 2 && pipeline.pendingCount() == 0, "retry dispatched once and retired");
      need(cache.get(key) == good, "successful retry cached");
      need(pipeline.generateChunk(key).join() == good && calls.get() == 2, "cache hit after retry");
    }
    System.out.println("PIPELINE_REENTRANT_RETRY_PASS checks=" + checks + " trials=10000");
  }

  private WorldGenPipelineReentrantRetryChecks() {}
}
