package com.shnewbs.deepprimal.mixin.client;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.shnewbs.deepprimal.client.ClientPerformanceConfig;
import com.shnewbs.deepprimal.core.VisualBudget;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FramerateLimitTracker.class)
public abstract class FrameLimitMixin {
    @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true)
    private void deepprimal$backgroundLimit(CallbackInfoReturnable<Integer> callback) {
        var mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null || !ClientPerformanceConfig.SPEC.isLoaded()) return;
        callback.setReturnValue(VisualBudget.frameLimit(callback.getReturnValue(), mc.isWindowActive(),
            mc.getWindow().isIconified(), ClientPerformanceConfig.UNFOCUSED_FPS.get(), ClientPerformanceConfig.MINIMIZED_FPS.get()));
    }
}
