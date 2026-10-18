package com.shnewbs.deepprimal;

import com.shnewbs.deepprimal.core.DistancePolicy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Tracking graphs are sized once per server session, so ordinary distances retain native cost. */
public final class DistanceRuntime {
    private static volatile int clientRequest = 12;
    private static volatile int trackingCapacity = 32;
    public static void clientRequest(int distance) { clientRequest = DistancePolicy.clampRender(distance); }
    public static int trackingCapacity() { return trackingCapacity; }
    @SubscribeEvent public void starting(ServerAboutToStartEvent event) {
        int requested = event.getServer().isSingleplayer() ? clientRequest : event.getServer().getPlayerList().getViewDistance();
        RuntimeMetrics.reset();
        trackingCapacity = Math.max(32, DistancePolicy.clampRender(requested));
        DeepPrimal.LOGGER.info("Render tracking capacity for this session: {} chunks", trackingCapacity);
    }
    @SubscribeEvent public void stopped(ServerStoppedEvent event) { trackingCapacity = 32; }
}
