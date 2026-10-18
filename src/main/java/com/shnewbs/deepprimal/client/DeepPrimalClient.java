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
    public DeepPrimalClient(ModContainer container, net.neoforged.bus.api.IEventBus modBus) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientPerformanceConfig.SPEC, "deepprimal-client-performance.toml");
        container.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
            (net.neoforged.neoforge.client.gui.IConfigScreenFactory)(mod, parent) -> new DistanceScreen(parent));
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new ClientTelemetry());
        modBus.addListener(DeepPrimalClient::packs);
        modBus.addListener(DeepPrimalClient::payloads);
        if (com.shnewbs.deepprimal.render.RendererOwnership.owned())
            NeoForge.EVENT_BUS.register(com.shnewbs.deepprimal.client.render.HorizonRenderer.INSTANCE);
        com.shnewbs.deepprimal.DeepPrimal.LOGGER.info("Client renderer owner: {}", com.shnewbs.deepprimal.render.RendererOwnership.owner());
    }
    private static void packs(net.neoforged.neoforge.event.AddPackFindersEvent event) {
        if (event.getPackType() == net.minecraft.server.packs.PackType.CLIENT_RESOURCES && com.shnewbs.deepprimal.render.RendererOwnership.owned())
            event.addPackFinders(net.minecraft.resources.Identifier.fromNamespaceAndPath("deepprimal", "renderer/native"),
                net.minecraft.server.packs.PackType.CLIENT_RESOURCES, net.minecraft.network.chat.Component.literal("Deep Primal renderer"),
                net.minecraft.server.packs.repository.PackSource.BUILT_IN, true, net.minecraft.server.packs.repository.Pack.Position.TOP);
    }
    private static void payloads(net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent event) {
        event.register(com.shnewbs.deepprimal.render.network.HorizonTile.TYPE, (p, c) -> {
            if (com.shnewbs.deepprimal.render.RendererOwnership.owned()) com.shnewbs.deepprimal.client.render.HorizonRenderer.INSTANCE.accept(p);
        });
    }
    @SubscribeEvent public void tick(ClientTickEvent.Post event) {
        DistanceRuntime.clientRequest(Minecraft.getInstance().options.renderDistance().get());
    }
}
