package com.shnewbs.deepprimal.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.shnewbs.deepprimal.client.ClientPerformanceConfig;
import com.shnewbs.deepprimal.core.VerticalRange;
import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE",
        target = "Lcom/mojang/blaze3d/buffers/Std140SizeCalculator;get()I"))
    private static int deepprimal$extendFogBuffer(int original) { return original + 8; }

    @Inject(method = "updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V", at = @At("RETURN"))
    private void deepprimal$writeVerticalFog(ByteBuffer buffer, int offset, Vector4f color,
            float environmentalStart, float environmentalEnd, float distanceStart, float distanceEnd,
            float skyEnd, float cloudsEnd, CallbackInfo callback) {
        var mc = Minecraft.getInstance();
        float scale = mc == null || mc.level == null ? 1.0f : VerticalRange.fogScale(mc.options.getEffectiveRenderDistance(),
            mc.level.getSectionsCount(), ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get());
        buffer.putFloat(offset + 40, scale);
        // Include std140 tail padding in the native constructor's buffer.flip().
        buffer.putFloat(offset + 44, 0.0f);
        buffer.position(offset + FogRenderer.FOG_UBO_SIZE);
    }
}
