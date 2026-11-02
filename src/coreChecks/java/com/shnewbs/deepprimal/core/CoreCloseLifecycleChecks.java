package com.shnewbs.deepprimal.core;

import java.util.concurrent.*;

/** A caller-owned worker may remain blocked while world services close. */
public final class CoreCloseLifecycleChecks {
  public static void main(String[] args) throws Exception {
    var worker = Executors.newFixedThreadPool(2);
    var started = new CountDownLatch(1);
    var release = new CountDownLatch(1);
    var core = new EngineCore.Builder().revision(1).verticalRange(0,1).workerExecutor(worker).build();
    try {
      core.start();
      var load = worker.submit(() -> core.getDensityCache().getOrCompute("blocked", k -> {
        started.countDown();
        try { release.await(); } catch (InterruptedException e) { throw new RuntimeException(e); }
        return "computed";
      }, 8));
      if (!started.await(5,TimeUnit.SECONDS)) throw new AssertionError("loader did not start");
      worker.submit(core::close).get(2,TimeUnit.SECONDS);
      if (load.isDone() || worker.isShutdown()) throw new AssertionError("close must preserve caller executor ownership");
      release.countDown();
      try {
        load.get(5,TimeUnit.SECONDS);
        throw new AssertionError("closed world returned obsolete cache result");
      } catch (ExecutionException expected) {
        if (!(expected.getCause() instanceof IllegalStateException))
          throw new AssertionError("terminal close must reject late cache completion", expected);
      }
      if (core.getDensityCache().size()!=0 || core.getDensityCache().getMemoryUsage()!=0 || core.getMeshCache().size()!=0)
        throw new AssertionError("closed caches repopulated");
      if (!core.generateChunkAsync(new ChunkMaterializer.ChunkKey(0,0,0)).isCompletedExceptionally())
        throw new AssertionError("closed engine accepted query");
    } finally {
      release.countDown(); worker.shutdownNow();
      if (!worker.awaitTermination(5,TimeUnit.SECONDS)) throw new AssertionError("worker did not terminate");
      core.close();
    }
    System.out.println("CORE_CLOSE_LIFECYCLE_PASS blocked loader; empty closed caches; caller executor retained");
  }
}
