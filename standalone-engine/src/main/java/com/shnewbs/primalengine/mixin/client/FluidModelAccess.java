package com.shnewbs.primalengine.mixin.client;

import net.minecraft.client.renderer.block.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FluidRenderer.class)
public interface FluidModelAccess {
  @Accessor("fluidModels")
  FluidStateModelSet primalengine$models();
}
