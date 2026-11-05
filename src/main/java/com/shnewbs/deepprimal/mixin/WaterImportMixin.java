package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.engine.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Import bucket/third-party native water before native spreading runs in engine worlds. */
@Mixin(FlowingFluid.class)
abstract class WaterImportMixin {
  @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
  private void deepprimal$ownWater(
      ServerLevel level, BlockPos p, BlockState block, FluidState fluid, CallbackInfo ci) {
    if (!fluid.is(FluidTags.WATER)
        || !(level.getChunkSource().getGenerator() instanceof EngineChunkGenerator)) return;
    ci.cancel();
    // Solid waterlogged cells stay sealed until displacement-aware shape storage is implemented.
    if (!block.is(Blocks.WATER)) return;
    int units = Math.clamp(fluid.getAmount() * 2, 1, 16);
    level.setBlock(
        p,
        EngineRegistry.WATER_BLOCK.get().defaultBlockState().setValue(EngineFluid.UNITS, units),
        3);
    WaterRuntime.tick(level, p);
  }
}
