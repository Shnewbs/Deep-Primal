package com.shnewbs.deepprimal.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
abstract class PrecipitationMixin {
  @Inject(method = "tickPrecipitation", at = @At("RETURN"))
  private void deepprimal$precipitation(BlockPos column, CallbackInfo ci) {
    com.shnewbs.deepprimal.water.Precipitation.tick((ServerLevel) (Object) this, column);
  }
}
