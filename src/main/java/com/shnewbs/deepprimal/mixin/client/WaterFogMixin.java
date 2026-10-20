package com.shnewbs.deepprimal.mixin.client;

import com.shnewbs.deepprimal.client.WaterColors;
import com.shnewbs.deepprimal.core.WaterDynamics;
import com.shnewbs.deepprimal.settings.WaterVisualConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterFogEnvironment.class)
abstract class WaterFogMixin {
  @Inject(method = "setupFog", at = @At("RETURN"))
  private void deepprimal$clarity(
      FogData fog,
      Camera camera,
      ClientLevel level,
      float range,
      DeltaTracker dt,
      CallbackInfo ci) {
    if (!WaterVisualConfig.FOG.get()) return;
    int color = WaterColors.blended(level, camera.blockPosition());
    double clarity = Math.clamp(1 + ((color & 255) - (color >> 16 & 255)) / 90.0, .45, 2.5);
    fog.environmentalEnd *= (float) clarity;
    fog.skyEnd = fog.cloudEnd = fog.environmentalEnd;
  }

  @Inject(method = "getBaseColor", at = @At("RETURN"), cancellable = true)
  private void deepprimal$color(
      ClientLevel level, Camera camera, int range, float dt, CallbackInfoReturnable<Integer> ci) {
    if (!WaterVisualConfig.FOG.get()) return;
    int color = WaterColors.blended(level, camera.blockPosition());
    double depth = Math.max(0, level.getSeaLevel() - camera.blockPosition().getY());
    ci.setReturnValue(
        0xff000000 | WaterDynamics.mix(color, 0x030c18, Math.clamp(depth / 384.0, .15, .9)));
  }
}
