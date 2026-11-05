package com.shnewbs.deepprimal.core;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.shnewbs.deepprimal.core.ChunkMaterializer.*;

/** Reject cross-chunk/dimension materializer completions before cache admission. */
public final class WorldGenPipelineKeyChecks {
  private static long checks;
  private static void need(boolean ok, String why) {
    checks++;
    if (!ok) throw new AssertionError(why + " check=" + checks);
  }
  private static void fails(CompletableFuture<?> future) {
    try {
      future.get(1, TimeUnit.SECONDS);
      throw new AssertionError("Wrong-key materialization succeeded");
    } catch (ExecutionException expected) {
      need(expected.getCause() instanceof IllegalStateException,
          "wrong-key failure type " + expected.getCause());
    } catch (TimeoutException timeout) {
      throw new AssertionError("Wrong-key materialization hung", timeout);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new AssertionError(interrupted);
    }
  }
  public static void main(String[] args) {
    Random random = new Random(0x186A11L);
    for (int trial = 0; trial < 10_000; trial++) {
      ChunkKey wanted = new ChunkKey(random.nextInt(3_000_001) - 1_500_000,
          random.nextInt(3_000_001) - 1_500_000, "world-" + (trial & 7));
      ChunkKey wrong = switch (trial % 4) {
        case 0 -> new ChunkKey(wanted.chunkX() + 1, wanted.chunkZ(), wanted.dimension());
        case 1 -> new ChunkKey(wanted.chunkX(), wanted.chunkZ() - 1, wanted.dimension());
        case 2 -> new ChunkKey(wanted.chunkX(), wanted.chunkZ(), wanted.dimension() + "-other");
        default -> null;
      };
      ChunkCache cache = new ChunkCache(1, 64);
      CompletableFuture<MaterializedChunk> pending = new CompletableFuture<>();
      AtomicInteger calls = new AtomicInteger();
      WorldGenPipeline pipeline = new WorldGenPipeline((key, cancelled) -> {
        calls.incrementAndGet();
        return pending;
      }, cache);
      CompletableFuture<MaterializedChunk> first = pipeline.generateChunk(wanted);
      CompletableFuture<MaterializedChunk> coalesced = pipeline.generateChunk(wanted);
      need(calls.get() == 1 && pipeline.pendingCount() == 1, "coalescing before bad completion");
      pending.complete(PipelineTestFixtures.chunk(wrong));
      fails(first);
      fails(coalesced);
      need(pipeline.pendingCount() == 0, "bad completion leaked pending");
      need(cache.size() == 0 && cache.get(wanted) == null, "wrong chunk cached");
      CompletableFuture<MaterializedChunk> good = pipeline.generateChunk(wanted);
      need(calls.get() == 2, "retry not dispatched");
      // This fixture deliberately reuses an already-completed future. The failed retry
      // must not admit the wrong chunk, even when completion runs inline.
      fails(good);
      need(cache.size() == 0 && pipeline.pendingCount() == 0, "inline retry polluted cache");
      ChunkCache validCache = new ChunkCache(1, 64);
      MaterializedChunk correct = PipelineTestFixtures.chunk(wanted);
      WorldGenPipeline valid = new WorldGenPipeline(
          (key, cancelled) -> CompletableFuture.completedFuture(correct), validCache);
      need(valid.generateChunk(wanted).join() == correct, "valid completion changed");
      need(validCache.get(wanted) == correct && validCache.size() == 1, "valid cache admission changed");
      need(valid.generateChunk(wanted).join() == correct, "cache hit changed");
    }
    System.out.println("PIPELINE_KEY_ADMISSION_PASS assertions=" + checks
        + " trials=10000 mismatch=x,z,dimension,null coalescing=true"
        + " noCachePoison=true retry=true validParity=true");
  }
}
