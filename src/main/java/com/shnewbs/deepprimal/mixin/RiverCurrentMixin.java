package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.water.RiverWaterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The native fluid renderer and entity physics consume the same stored current. */
@Mixin(FlowingFluid.class)
abstract class RiverCurrentMixin {
  @Inject(method = "getFlow", at = @At("HEAD"), cancellable = true)
  private void deepprimal$current(
      BlockGetter level, BlockPos pos, FluidState fluid, CallbackInfoReturnable<Vec3> ci) {
    if (!fluid.is(net.minecraft.tags.FluidTags.WATER)) return;
    Vec3 current = RiverWaterBlock.current(level.getBlockState(pos));
    if (current != Vec3.ZERO && current.lengthSqr() > 0) ci.setReturnValue(current);
  }
}
