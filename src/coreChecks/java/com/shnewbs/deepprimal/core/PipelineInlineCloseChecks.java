package com.shnewbs.deepprimal.core;

import java.util.concurrent.*;

/** A legal inline executor must not prevent shutdown while terrain is busy. */
public final class PipelineInlineCloseChecks {
  public static void main(String[] args) throws Exception {
    var entered=new CountDownLatch(1);
    var callers=Executors.newFixedThreadPool(2);
    Executor inline=command->{entered.countDown();command.run();};
    var core=new EngineCore.Builder().revision(1).verticalRange(0,1).workerExecutor(inline).build();
    Future<CompletableFuture<ChunkMaterializer.MaterializedChunk>> submitted=null;
    try {
      core.start();
      synchronized(core.getTerrainEngine()) {
        submitted=callers.submit(()->core.generateChunkAsync(new ChunkMaterializer.ChunkKey(0,0,0)));
        if(!entered.await(5,TimeUnit.SECONDS))throw new AssertionError("executor not invoked");
        callers.submit(core::close).get(2,TimeUnit.SECONDS);
      }
      var result=submitted.get(5,TimeUnit.SECONDS);
      if(!result.isCompletedExceptionally())throw new AssertionError("closed query completed normally");
      if(core.getWorldGenPipeline().pendingCount()!=0 || core.getDensityCache().size()!=0 || core.getMeshCache().size()!=0)
        throw new AssertionError("shutdown left pending work or cached results");
      if(callers.isShutdown())throw new AssertionError("caller executor ownership lost");
    } finally {
      callers.shutdownNow();
      if(!callers.awaitTermination(10,TimeUnit.SECONDS))throw new AssertionError("worker did not terminate");
      core.close();
    }
    System.out.println("PIPELINE_INLINE_CLOSE_PASS no dispatch lock; cancelled result; empty caches and queue");
  }
}
