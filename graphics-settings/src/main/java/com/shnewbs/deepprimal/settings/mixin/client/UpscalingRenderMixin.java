package com.shnewbs.deepprimal.settings.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.shnewbs.deepprimal.settings.UpscalingRuntime;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import net.minecraft.client.renderer.state.GameRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class UpscalingRenderMixin {
  @Shadow @Final private Minecraft minecraft;
  @Shadow @Final private GameRenderState gameRenderState;
  @Shadow @Final private GlobalSettingsUniform globalSettingsUniform;

  @WrapMethod(method = "render")
  private void deepprimal$guardFrame(DeltaTracker tracker, boolean renderLevel,
      Operation<Void> original) {
    UpscalingRuntime.beginFrame(minecraft);
    try {
      original.call(tracker, renderLevel);
    } finally {
      UpscalingRuntime.restoreTarget(minecraft);
    }
  }

  @Inject(method = "render", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V"))
  private void deepprimal$beginWorld(DeltaTracker tracker, boolean renderLevel, CallbackInfo ci) {
    UpscalingRuntime.beginWorld(minecraft, gameRenderState, globalSettingsUniform, tracker);
  }

  @Inject(method = "render", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/client/renderer/fog/FogRenderer;endFrame()V"))
  private void deepprimal$finishWorld(DeltaTracker tracker, boolean renderLevel, CallbackInfo ci) {
    UpscalingRuntime.finishWorld(minecraft);
  }

  @Inject(method = "resize", at = @At("TAIL"))
  private void deepprimal$resized(int width, int height, CallbackInfo ci) {
    UpscalingRuntime.resized();
  }

  @Inject(method = "close", at = @At("HEAD"))
  private void deepprimal$close(CallbackInfo ci) { UpscalingRuntime.close(); }
}
