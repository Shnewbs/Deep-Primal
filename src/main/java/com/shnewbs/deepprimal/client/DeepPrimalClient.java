package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.DistanceRuntime;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class DeepPrimalClient {
    public DeepPrimalClient(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientPerformanceConfig.SPEC, "deepprimal-client-performance.toml");
        NeoForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent public void tick(ClientTickEvent.Post event) {
        DistanceRuntime.clientRequest(Minecraft.getInstance().options.renderDistance().get());
    }
}
