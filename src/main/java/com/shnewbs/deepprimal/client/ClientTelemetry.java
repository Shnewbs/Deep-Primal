package com.shnewbs.deepprimal.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.HorizonRenderer;
import com.shnewbs.deepprimal.core.TickWindow;
import com.shnewbs.deepprimal.diagnostics.LocalDashboard;
import com.shnewbs.deepprimal.settings.ClientPerformanceConfig;
import com.shnewbs.deepprimal.settings.RendererOwnership;
import com.shnewbs.deepprimal.settings.VisualPerformance;
import java.util.LinkedHashMap;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Frame interval includes frame limiting and waits; it is not GPU render time. */
public final class ClientTelemetry {
  private final TickWindow frames = new TickWindow(512);
  private long previous, sampled, lastBuild, lastUpload, lastDraw, lastReceived, lastSync;

  @SubscribeEvent
  public void frame(RenderFrameEvent.Post event) {
    HorizonRenderer.INSTANCE.observeFrame(System.nanoTime());
    if (!LocalDashboard.running()) LocalDashboard.start();
    if (!LocalDashboard.running()) return;
    long now = System.nanoTime();
    if (previous != 0) frames.record(now - previous);
    previous = now;
    if (now - sampled < 1_000_000_000L) return;
    sampled = now;
    var mc = Minecraft.getInstance();
    var data = new LinkedHashMap<String, Object>();
    var stats = frames.snapshot();
    data.put("state", mc.level == null ? "Menu" : "In world");
    data.put("sampleTime", System.currentTimeMillis());
    data.put("frameMedianMs", stats.medianMs());
    data.put("frameP95Ms", stats.p95Ms());
    data.put("frameMaxMs", stats.maxMs());
    data.put("windowFocused", mc.isWindowActive());
    data.put("effectiveFpsLimit", mc.getFramerateLimitTracker().getFramerateLimit());
    data.put("entitiesSkipped", VisualPerformance.entitiesSkipped);
    data.put("blockEntitiesSkipped", VisualPerformance.blockEntitiesSkipped);
    data.put("particlesSkipped", VisualPerformance.particlesSkipped);
    data.put("fps", mc.getFps());
    data.put("rendererOwner", RendererOwnership.owner());
    data.put("graphicsApi", RenderSystem.getDevice().getBackendName());
    data.put("nativeSyncBuilds", VisualPerformance.chunkSyncBuilds);
    data.put("nativeRebuildsDeferred", VisualPerformance.chunkRebuildsDeferred);
    data.put("nativePlayerBuilds", VisualPerformance.chunkPlayerBuilds);
    data.put("nativeSyncBuildMsPerSample", (VisualPerformance.chunkSyncNanos - lastSync) / 1e6);
    lastSync = VisualPerformance.chunkSyncNanos;
    data.put("gpu", RenderSystem.getDevice().getRenderer());
    data.put(
        "wakeMeshBytes",
        com.shnewbs.deepprimal.client.render.WakeRenderer.INSTANCE.residentBytes());
    data.put(
        "oceanMeshBytes",
        com.shnewbs.deepprimal.client.render.WaterSurfaceRenderer.INSTANCE.residentBytes());
    data.put(
        "oceanPendingSamples",
        com.shnewbs.deepprimal.client.render.WaterSurfaceRenderer.INSTANCE.pendingSamples());
    data.put("renderDistance", mc.options.renderDistance().get());
    data.put("effectiveRenderDistance", mc.options.getEffectiveRenderDistance());
    data.put("memoryDistanceGuard", com.shnewbs.deepprimal.performance.MemorySafety.enabled());
    data.put("simulationDistance", mc.options.simulationDistance().get());
    data.put(
        "verticalRadius",
        RendererOwnership.owned()
            ? ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get()
            : "External");
    data.put("farDistance", RendererOwnership.owned() ? HorizonRenderer.farDistance() : 0);
    if (RendererOwnership.owned()) {
      var renderer = HorizonRenderer.INSTANCE;
      data.put("horizonResidentBytes", renderer.residentBytes);
      data.put("horizonTransitionBuilds", renderer.transitionBuilds);
      data.put("horizonWorkFraction", renderer.workFraction());
      data.put("horizonTargetFps", ClientPerformanceConfig.TARGET_FPS.get());
      data.put(
          "horizonMemoryLimitBytes", ClientPerformanceConfig.HORIZON_MEMORY_MIB.get() * 1048576L);
      data.put(
          "horizonBuildMsPerSample",
          (renderer.meshBuildNanos + renderer.meshPackNanos - lastBuild) / 1e6);
      lastBuild = renderer.meshBuildNanos + renderer.meshPackNanos;
      data.put("horizonUploadMsPerSample", (renderer.uploadNanos - lastUpload) / 1e6);
      lastUpload = renderer.uploadNanos;
      data.put("horizonDrawCpuMsPerSample", (renderer.drawCpuNanos - lastDraw) / 1e6);
      lastDraw = renderer.drawCpuNanos;
      data.put("horizonReceivedBytesPerSample", renderer.receivedBytes - lastReceived);
      lastReceived = renderer.receivedBytes;
      data.put("horizonGpuTimingAvailable", renderer.gpuTimingAvailable());
      data.put("horizonGpuMs", renderer.gpuMs());
      data.put("horizonCulledTiles", renderer.culledTiles);
      data.put("horizonBatchedSubmission", ClientPerformanceConfig.BATCHED_DRAWS.get());
      data.put("horizonBackgroundPaused", HorizonRenderer.backgroundWorkPaused());
      data.put("horizonUploadsLastFrame", renderer.uploadsLastFrame);
      data.put("horizonUploadBytesLastFrame", renderer.uploadBytesLastFrame);
      data.put("horizonMaxUploadsPerFrame", renderer.maxUploadsPerFrame);
      data.put("horizonMaxUploadBytesPerFrame", renderer.maxUploadBytesPerFrame);
      data.put("horizonFineVisible", renderer.visibleFine);
      data.put("horizonMediumVisible", renderer.visibleMedium);
      data.put("horizonCoarseVisible", renderer.visibleCoarse);
      data.put("horizonState", renderer.status);
      data.put("horizonCached", renderer.cachedTiles);
      data.put("horizonTarget", renderer.targetTiles);
      data.put("horizonVisible", renderer.visibleTiles);
      data.put("horizonBackgroundDepthBytes", renderer.backgroundDepthBytes());
      data.put("horizonTrianglesDrawn", renderer.drawnTriangles);
      data.put("horizonUploadedBytes", renderer.uploadedBytes);
    }
    var lod = com.shnewbs.deepprimal.client.render.VoxelLodRenderer.INSTANCE;
    data.put("lodState", lod.status);
    data.put("lodCaptured", lod.capturedChunks);
    data.put("lodUploaded", lod.uploadedChunks);
    data.put("lodVisible", lod.visibleChunks);
    data.put("lodVisibleSpan", lod.visibleSpan);
    data.put("lodRequestedRadius", com.shnewbs.deepprimal.settings.LodConfig.RADIUS.get());
    data.put("lodResidentBytes", lod.residentBytes);
    data.put("lodDiskReads", lod.diskReads);
    data.put("lodStaleResults", lod.staleResults);
    data.put("lodQueued", lod.queuedChunks);
    if (mc.level != null) data.put("dimension", mc.level.dimension().identifier().toString());
    LocalDashboard.client(data);
  }
}
