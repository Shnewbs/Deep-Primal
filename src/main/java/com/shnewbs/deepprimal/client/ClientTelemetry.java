package com.shnewbs.deepprimal.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.HorizonRenderer;
import com.shnewbs.deepprimal.core.TickWindow;
import com.shnewbs.deepprimal.diagnostics.LocalDashboard;
import com.shnewbs.deepprimal.render.RendererOwnership;
import java.util.LinkedHashMap;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Frame interval includes frame limiting and waits; it is not GPU render time. */
public final class ClientTelemetry {
    private final TickWindow frames = new TickWindow(512);
    private long previous, sampled;
    @SubscribeEvent public void frame(RenderFrameEvent.Post event) {
        if (!LocalDashboard.running()) LocalDashboard.start();
        if (!LocalDashboard.running()) return;
        long now = System.nanoTime(); if (previous != 0) frames.record(now - previous); previous = now;
        if (now - sampled < 1_000_000_000L) return; sampled = now;
        var mc = Minecraft.getInstance(); var data = new LinkedHashMap<String, Object>(); var stats = frames.snapshot();
        data.put("state", mc.level == null ? "Menu" : "In world"); data.put("sampleTime", System.currentTimeMillis());
        data.put("frameMedianMs", stats.medianMs()); data.put("frameP95Ms", stats.p95Ms()); data.put("frameMaxMs", stats.maxMs());
        data.put("fps", mc.getFps()); data.put("rendererOwner", RendererOwnership.owner()); data.put("graphicsApi", RenderSystem.getDevice().getBackendName());
        data.put("gpu", RenderSystem.getDevice().getRenderer()); data.put("renderDistance", mc.options.renderDistance().get()); data.put("simulationDistance", mc.options.simulationDistance().get());
        data.put("verticalRadius", RendererOwnership.owned() ? ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get() : "External");
        data.put("farDistance", RendererOwnership.owned() ? HorizonRenderer.farDistance() : 0);
        if (RendererOwnership.owned()) {
            var renderer = HorizonRenderer.INSTANCE;
            data.put("horizonState", renderer.status); data.put("horizonCached", renderer.cachedTiles); data.put("horizonTarget", renderer.targetTiles); data.put("horizonVisible", renderer.visibleTiles);
            data.put("horizonTrianglesDrawn", renderer.drawnTriangles); data.put("horizonUploadedBytes", renderer.uploadedBytes);
        }
        if (mc.level != null) data.put("dimension", mc.level.dimension().identifier().toString());
        LocalDashboard.client(data);
    }
}
