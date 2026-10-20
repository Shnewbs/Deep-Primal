package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.water.FiniteWater;
import com.shnewbs.deepprimal.water.RiverWaterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowingFluid.class)
abstract class FiniteWaterMixin {
  @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
  private void deepprimal$finiteWater(ServerLevel level, BlockPos p, BlockState block, FluidState fluid, CallbackInfo ci) {
    if (!fluid.is(FluidTags.WATER)) return;
    if (FiniteWater.enabled()) { ci.cancel(); FiniteWater.tick(level, p); }
    else if (block.getBlock() instanceof RiverWaterBlock river && river.stableChannel()) ci.cancel();
  }
}
