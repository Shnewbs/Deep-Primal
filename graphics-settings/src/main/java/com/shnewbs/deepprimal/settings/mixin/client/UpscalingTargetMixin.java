package com.shnewbs.deepprimal.settings.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.shnewbs.deepprimal.settings.UpscalingRuntime;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Scoped world target override; the final native main-target field remains untouched. */
@Mixin(Minecraft.class)
public abstract class UpscalingTargetMixin {
  @Inject(method = "getMainRenderTarget", at = @At("HEAD"), cancellable = true)
  private void deepprimal$worldTarget(CallbackInfoReturnable<RenderTarget> cir) {
    var target = UpscalingRuntime.frameTarget();
    if (target != null) cir.setReturnValue(target);
  }
}
