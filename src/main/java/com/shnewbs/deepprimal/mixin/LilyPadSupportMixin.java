package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.engine.ShorelineHabitat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LilyPadBlock.class)
abstract class LilyPadSupportMixin {
  @Inject(method = "mayPlaceOn", at = @At("RETURN"), cancellable = true)
  private void deepprimal$freshSurface(BlockState support, BlockGetter level, BlockPos pos,
      CallbackInfoReturnable<Boolean> result) {
    if (!result.getReturnValue() && ShorelineHabitat.supportsLily(support) && level.getFluidState(pos.above()).isEmpty())
      result.setReturnValue(true);
  }
}
