package com.shnewbs.deepprimal.diagnostics;

import com.shnewbs.deepprimal.core.TickWindow;
import com.shnewbs.deepprimal.performance.DistanceRuntime;
import com.shnewbs.deepprimal.performance.RuntimeMetrics;
import com.shnewbs.deepprimal.render.HorizonService;
import java.util.ArrayList;
import java.util.Map;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Captures scalar world data on the server thread once a second. */
public final class ServerTelemetry {
  private final TickWindow ticks = new TickWindow(256);
  private long before, sampled;

  @SubscribeEvent
  public void started(ServerStartedEvent event) {
    ticks.clear();
    sampled = System.nanoTime();
    LocalDashboard.start();
  }

  @SubscribeEvent
  public void pre(ServerTickEvent.Pre event) {
    before = System.nanoTime();
  }

  @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
  public void post(ServerTickEvent.Post event) {
    if (!LocalDashboard.running()) return;
    long now = System.nanoTime();
    ticks.record(now - before);
    if (event.getServer().getTickCount() % 20 != 0) return;
    var worlds = new ArrayList<Map<String, Object>>();
    for (var level : event.getServer().getAllLevels()) {
      var source = level.getChunkSource();
      var world = new java.util.LinkedHashMap<String, Object>();
      world.put("dimension", level.dimension().identifier().toString());
      world.put("minY", level.getMinY());
      world.put("maxY", level.getMaxY());
      world.put("seaLevel", level.getSeaLevel());
      world.put("gameTime", level.getGameTime());
      world.put("players", level.players().size());
      world.put("chunkHolders", source.getLoadedChunksCount());
      world.put("pendingChunkTasks", source.getPendingTasksCount());
      world.put("blockTicks", level.getBlockTicks().count());
      world.put("fluidTicks", level.getFluidTicks().count());
      world.put(
          "entities",
          ((com.shnewbs.deepprimal.mixin.ServerLevelMetricsAccessor) level)
              .deepprimal$entityManager()
              .count());
      worlds.add(Map.copyOf(world));
    }
    var stats = ticks.snapshot();
    var data = new java.util.LinkedHashMap<String, Object>();
    data.put("state", "Running");
    data.put("sampleTime", System.currentTimeMillis());
    data.put("tickMedianMs", stats.medianMs());
    data.put("tickP95Ms", stats.p95Ms());
    data.put("tickMaxMs", stats.maxMs());
    data.put("tps", Math.min(20, 20_000_000_000.0 / Math.max(1, now - sampled)));
    sampled = now;
    data.put("players", event.getServer().getPlayerCount());
    data.put("trackingCapacity", DistanceRuntime.trackingCapacity());
    data.put("worlds", java.util.List.copyOf(worlds));
    data.put(
        "chunkSaveBackpressure",
        com.shnewbs.deepprimal.performance.RuntimeMetrics.SAVE_BACKPRESSURE.sum());
    data.put(
        "maxPendingChunkWrites",
        com.shnewbs.deepprimal.performance.RuntimeMetrics.MAX_PENDING_WRITES.get());
    data.put("horizonReusedSamples", HorizonService.reusedSamples);
    data.put("horizonCacheBytes", HorizonService.cacheBytes());
    data.put("horizonWorkFraction", HorizonService.workFraction());
    data.put("horizonSharedRequests", HorizonService.sharedRequests);
    data.put("horizonSentBytes", HorizonService.sentBytes);
    data.put("horizonSamplingNanos", HorizonService.samplingNanos);
    data.put("horizonCancelled", HorizonService.cancelled);
    data.put("horizonSamples", HorizonService.samples);
    data.put("horizonCompleted", HorizonService.completed);
    data.put("horizonQueued", HorizonService.pending());
    data.put("horizonCached", HorizonService.cachedTiles());
    data.put("horizonCacheHits", HorizonService.cacheHits());
    data.put("maxChunkSendBatch", RuntimeMetrics.MAX_SEND_BATCH.get());
    data.put("viewColumnsScanned", RuntimeMetrics.VIEW_COLUMNS.sum());
    data.put("engineMetrics", com.shnewbs.deepprimal.core.EngineMetrics.snapshot());
    var core = com.shnewbs.deepprimal.EngineCoreModule.getEngineCore();
    if (core != null) {
      data.put("engineQueryPending", core.getWorldGenPipeline().pendingCount());
      data.put("engineQueryCacheBytes", core.getDensityCache().getMemoryUsage() + core.getMeshCache().getMemoryUsage());
    }
    LocalDashboard.server(data);
  }

  @SubscribeEvent
  public void stopped(ServerStoppedEvent event) {
    LocalDashboard.server(Map.of("state", "Stopped", "sampleTime", System.currentTimeMillis()));
    ticks.clear();
    if (!event.getServer().isSingleplayer()) LocalDashboard.stop();
  }
}
