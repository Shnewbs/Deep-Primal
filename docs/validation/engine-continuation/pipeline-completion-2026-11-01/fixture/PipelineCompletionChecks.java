package com.shnewbs.deepprimal.core;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.shnewbs.deepprimal.core.ChunkMaterializer.*;

/** Deterministic isolated completion/cleanup contract; requires fixture doubles. */
public final class PipelineCompletionChecks {
  private static int checks;
  private static void ok(boolean value, String label) {
    checks++;
    if (!value) throw new AssertionError(label);
  }
  private static void failure(CompletableFuture<?> future, Class<? extends Throwable> kind) {
    try {
      future.get(300, TimeUnit.MILLISECONDS);
      throw new AssertionError("Unexpected success");
    } catch (ExecutionException e) {
      ok(kind.isInstance(e.getCause()), "Wrong failure: " + e.getCause());
    } catch (TimeoutException e) {
      throw new AssertionError("Pending future never completed", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError(e);
    }
  }
  public static void main(String[] args) {
    for (int i = 0; i < 250; i++) {
      ChunkKey key = new ChunkKey(i, 0, "test");
      var pending = new CompletableFuture<MaterializedChunk>();
      var pipeline = new WorldGenPipeline(new ChunkMaterializer(k -> pending), new ChunkCache());
      var returned = pipeline.generateChunk(key);
      pending.complete(null);
      failure(returned, IllegalStateException.class);
      ok(pipeline.pendingCount() == 0, "Null pending leak");

      var cache = new ChunkCache();
      cache.reject = true;
      var done = CompletableFuture.completedFuture(new MaterializedChunk(key, 10));
      var cachePipeline = new WorldGenPipeline(new ChunkMaterializer(k -> done), cache);
      failure(cachePipeline.generateChunk(key), IllegalStateException.class);
      ok(cachePipeline.pendingCount() == 0, "Cache failure pending leak");
      cache.reject = false;
      ok(cachePipeline.generateChunk(key).join().key().equals(key), "Retry after cache recovery");
      ok(cachePipeline.pendingCount() == 0, "Cache hit");

      var delayed = new CompletableFuture<MaterializedChunk>();
      var calls = new AtomicInteger();
      var coalesced = new WorldGenPipeline(new ChunkMaterializer(k -> {
        calls.incrementAndGet();
        return delayed;
      }), new ChunkCache());
      var first = coalesced.generateChunk(key);
      var second = coalesced.generateChunk(key);
      ok(calls.get() == 1 && coalesced.pendingCount() == 1, "Coalesced once");
      var chunk = new MaterializedChunk(key, 100);
      delayed.complete(chunk);
      ok(first.join().equals(chunk) && second.join().equals(chunk), "Coalesced completion");
      ok(coalesced.pendingCount() == 0, "Completed pending cleanup");

      var rejected = new CompletableFuture<MaterializedChunk>();
      var failed = new WorldGenPipeline(new ChunkMaterializer(k -> rejected), new ChunkCache());
      var failedResult = failed.generateChunk(key);
      rejected.completeExceptionally(new IllegalArgumentException("bad query"));
      failure(failedResult, IllegalArgumentException.class);
      ok(failed.pendingCount() == 0, "Exceptional pending cleanup");

      var throwing = new WorldGenPipeline(new ChunkMaterializer(k -> {
        throw new RejectedExecutionException("rejected");
      }), new ChunkCache());
      failure(throwing.generateChunk(key), RejectedExecutionException.class);
      ok(throwing.pendingCount() == 0, "Synchronous dispatch cleanup");

      var shutdownFuture = new CompletableFuture<MaterializedChunk>();
      var shutting = new WorldGenPipeline(new ChunkMaterializer(k -> shutdownFuture), new ChunkCache());
      var shutdownResult = shutting.generateChunk(key);
      shutting.shutdown();
      failure(shutdownResult, CancellationException.class);
      shutdownFuture.complete(chunk);
      ok(shutting.pendingCount() == 0, "Shutdown cleanup");
    }
    System.out.println("PIPELINE_COMPLETION_PASS checks=" + checks
        + " trials=250 nullCompletion=true cacheFailure=true recovery=true"
        + " coalescing=true exceptional=true synchronous=true shutdown=true");
  }
}
