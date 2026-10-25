package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.performance.DistanceRuntime;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class DeepPrimalClient {
  public DeepPrimalClient(ModContainer container, net.neoforged.bus.api.IEventBus modBus) {
    com.shnewbs.deepprimal.settings.GraphicsActions.dashboard =
        () ->
            net.minecraft.util.Util.getPlatform()
                .openUri(
                    java.net.URI.create(com.shnewbs.deepprimal.diagnostics.LocalDashboard.url()));
    com.shnewbs.deepprimal.settings.GraphicsActions.dashboardAvailable =
        com.shnewbs.deepprimal.diagnostics.LocalDashboard::running;
    container.registerExtensionPoint(
        net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
        (net.neoforged.neoforge.client.gui.IConfigScreenFactory)
            (mod, parent) -> new com.shnewbs.deepprimal.settings.PrimalGraphicsScreen(parent));
    NeoForge.EVENT_BUS.register(this);
    NeoForge.EVENT_BUS.register(new ClientTelemetry());
    modBus.addListener(DeepPrimalClient::packs);
    modBus.addListener(WaterPalettes::register);
    modBus.addListener(GroundMaterialColors::register);
    modBus.addListener(GroundSideSpriteSource::register);
    modBus.addListener(DeepPrimalClient::engineFluid);
    modBus.addListener(DeepPrimalClient::payloads);
    if (com.shnewbs.deepprimal.settings.RendererOwnership.owned()) {
      NeoForge.EVENT_BUS.register(
          com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer.INSTANCE);
      NeoForge.EVENT_BUS.register(com.shnewbs.deepprimal.client.render.WakeRenderer.INSTANCE);
      NeoForge.EVENT_BUS.register(com.shnewbs.deepprimal.client.render.HorizonRenderer.INSTANCE);
      NeoForge.EVENT_BUS.register(
          com.shnewbs.deepprimal.client.render.WaterSurfaceRenderer.INSTANCE);
    }
    com.shnewbs.deepprimal.DeepPrimal.LOGGER.info(
        "Client renderer owner: {}", com.shnewbs.deepprimal.settings.RendererOwnership.owner());
  }

  private static void engineFluid(
      net.neoforged.neoforge.client.event.RegisterFluidModelsEvent event) {
    event.register(
        new net.minecraft.client.renderer.block.FluidModel.Unbaked(
            new net.minecraft.client.resources.model.sprite.Material(
                net.minecraft.resources.Identifier.withDefaultNamespace("block/water_still")),
            new net.minecraft.client.resources.model.sprite.Material(
                net.minecraft.resources.Identifier.withDefaultNamespace("block/water_flow")),
            new net.minecraft.client.resources.model.sprite.Material(
                net.minecraft.resources.Identifier.withDefaultNamespace("block/water_overlay")),
            net.neoforged.neoforge.client.fluid.FluidTintSources.water(),
            new com.shnewbs.deepprimal.client.render.SmoothWaterRenderer()),
        com.shnewbs.deepprimal.engine.EngineRegistry.WATER);
  }

  private static void packs(net.neoforged.neoforge.event.AddPackFindersEvent event) {
    if (event.getPackType() == net.minecraft.server.packs.PackType.CLIENT_RESOURCES
        && com.shnewbs.deepprimal.settings.RendererOwnership.owned())
      event.addPackFinders(
          net.minecraft.resources.Identifier.fromNamespaceAndPath("deepprimal", "renderer/native"),
          net.minecraft.server.packs.PackType.CLIENT_RESOURCES,
          net.minecraft.network.chat.Component.literal("Deep Primal renderer"),
          net.minecraft.server.packs.repository.PackSource.BUILT_IN,
          true,
          net.minecraft.server.packs.repository.Pack.Position.TOP);
  }

  private static void payloads(
      net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent event) {
    event.register(
        com.shnewbs.deepprimal.render.network.HorizonTile.TYPE,
        (p, c) -> {
          if (com.shnewbs.deepprimal.settings.RendererOwnership.owned())
            com.shnewbs.deepprimal.client.render.HorizonRenderer.INSTANCE.accept(p);
        });
  }

  @SubscribeEvent
  public void tick(ClientTickEvent.Post event) {
    WaterEffects.tick();
    SnowAtmosphere.tick();
    com.shnewbs.deepprimal.client.render.WakeRenderer.INSTANCE.tick();
    if (com.shnewbs.deepprimal.settings.RendererOwnership.owned())
      com.shnewbs.deepprimal.client.render.WaterSurfaceRenderer.INSTANCE.tick();
    DistanceRuntime.clientRequest(Minecraft.getInstance().options.renderDistance().get());
  }
}
