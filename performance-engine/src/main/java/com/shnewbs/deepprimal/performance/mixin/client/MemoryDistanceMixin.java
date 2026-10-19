package com.shnewbs.deepprimal.performance.mixin.client;

import com.shnewbs.deepprimal.performance.MemorySafety;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public abstract class MemoryDistanceMixin {
  @Inject(method = "getEffectiveRenderDistance", at = @At("RETURN"), cancellable = true)
  private void deepprimal$memoryLimit(CallbackInfoReturnable<Integer> callback) {
    var level = Minecraft.getInstance().level;
    callback.setReturnValue(
        MemorySafety.limit(
            callback.getReturnValue(), level == null ? 64 : level.getSectionsCount()));
  }
}
