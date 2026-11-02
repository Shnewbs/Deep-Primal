package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.ChunkMaterializer.ChunkKey;
import com.shnewbs.deepprimal.core.ChunkMaterializer.MaterializedChunk;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/** Deterministic interleaving: completion paused after cache admission, before future settlement. */
public final class WorldGenPipelineShutdownRaceChecks {
  private static long checks;
  private static void need(boolean ok, String why) {
    checks++;
    if (!ok) throw new AssertionError(why + " check=" + checks);
  }
  private static void cancelled(CompletableFuture<?> f) throws Exception {
    try {
      f.get(2, TimeUnit.SECONDS);
      throw new AssertionError("In-flight query succeeded after shutdown");
    } catch (ExecutionException | CancellationException expected) {
      Throwable failure = expected instanceof ExecutionException ? expected.getCause() : expected;
      need(failure instanceof CancellationException, "shutdown cancellation must win: " + failure);
    }
  }
  public static void main(String[] args) throws Exception {
    for (int trial = 0; trial < 1000; trial++) {
      ChunkKey key = new ChunkKey(trial, -trial, "race-" + (trial & 7));
      CompletableFuture<MaterializedChunk> supplied = new CompletableFuture<>();
      ChunkCache cache = new ChunkCache();
      WorldGenPipeline pipeline = new WorldGenPipeline(new ChunkMaterializer(k -> supplied), cache);
      CompletableFuture<MaterializedChunk> first = pipeline.generateChunk(key);
      CompletableFuture<MaterializedChunk> coalesced = pipeline.generateChunk(key);
      RaceGate.reset();
      Thread worker = new Thread(() -> supplied.complete(new MaterializedChunk(key, 128)), "materializer-completion");
      worker.start();
      try {
        need(RaceGate.entered.await(2, TimeUnit.SECONDS), "completion never reached barrier");
        need(pipeline.pendingCount() == 1, "pending removed before future settled");
        pipeline.shutdown();
        cancelled(first);
        cancelled(coalesced);
      } finally {
        RaceGate.release.countDown();
        worker.join(2000);
      }
      need(!worker.isAlive(), "completion worker hung");
      need(pipeline.pendingCount() == 0 && cache.size() == 0, "shutdown cleanup failed");
    }
    System.out.println("PIPELINE_SHUTDOWN_RACE_PASS assertions=" + checks
        + " trials=1000 deterministicAdmissionToSettlementBarrier=true");
  }
}
