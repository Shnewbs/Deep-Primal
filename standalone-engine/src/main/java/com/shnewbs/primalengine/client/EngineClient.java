package com.shnewbs.primalengine.client;

import com.shnewbs.primalengine.PrimalEngine;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;

@Mod(value = PrimalEngine.ID, dist = Dist.CLIENT)
public final class EngineClient {
  public EngineClient(IEventBus bus) {
    bus.addListener(EngineClient::models);
  }

  private static void models(RegisterFluidModelsEvent event) {
    event.register(
        new FluidModel.Unbaked(
            new Material(Identifier.withDefaultNamespace("block/water_still")),
            new Material(Identifier.withDefaultNamespace("block/water_flow")),
            new Material(Identifier.withDefaultNamespace("block/water_overlay")),
            FluidTintSources.water(),
            new SmoothWaterRenderer()),
        PrimalEngine.WATER);
  }
}
