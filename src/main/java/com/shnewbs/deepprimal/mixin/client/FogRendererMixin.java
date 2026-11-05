package com.shnewbs.deepprimal.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.shnewbs.deepprimal.core.VerticalRange;
import com.shnewbs.deepprimal.settings.ClientPerformanceConfig;
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
  @ModifyExpressionValue(
      method = "<clinit>",
      at =
          @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/buffers/Std140SizeCalculator;get()I"))
  private static int deepprimal$extendFogBuffer(int original) {
    return original + 24;
  }

  @Inject(
      method = "updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
      at = @At("RETURN"))
  private void deepprimal$writeVerticalFog(
      ByteBuffer buffer,
      int offset,
      Vector4f color,
      float environmentalStart,
      float environmentalEnd,
      float distanceStart,
      float distanceEnd,
      float skyEnd,
      float cloudsEnd,
      CallbackInfo callback) {
    var mc = Minecraft.getInstance();
    float scale =
        mc == null || mc.level == null
            ? 1.0f
            : VerticalRange.fogScale(
                mc.options.getEffectiveRenderDistance(),
                mc.level.getSectionsCount(),
                ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get());
    buffer.putFloat(offset + 40, scale);
    // Include std140 tail padding in the native constructor's buffer.flip().
    buffer.putFloat(
        offset + 44,
        mc == null ? 0 : com.shnewbs.deepprimal.client.render.HorizonRenderer.nearRadius());
    buffer.putFloat(
        offset + 48,
        mc == null || mc.level == null
            ? 65536
            : ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get() == 0
                ? 65536
                : ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get() * 16);
    var camera =
        mc == null || mc.gameRenderer == null
            ? net.minecraft.world.phys.Vec3.ZERO
            : mc.gameRenderer.getMainCamera().position();
    buffer.putFloat(
        offset + 52,
        (float)
            (com.shnewbs.deepprimal.client.render.HorizonRenderer.nearCenter(camera.x) - camera.x));
    buffer.putFloat(
        offset + 56,
        (float)
            (com.shnewbs.deepprimal.client.render.HorizonRenderer.nearCenter(camera.z) - camera.z));
    buffer.putFloat(offset + 60, 0);
    if ((com.shnewbs.deepprimal.client.render.HorizonRenderer.active()
        || com.shnewbs.deepprimal.settings.LodAccess.active())) {
      float far =
          Math.max(
              distanceEnd,
              Math.max(
                      com.shnewbs.deepprimal.client.render.HorizonRenderer.farDistance(),
                      com.shnewbs.deepprimal.settings.LodAccess.visibleDistance())
                  * 16);
      buffer.putFloat(offset + 24, far * 0.75f);
      buffer.putFloat(offset + 28, far);
      buffer.putFloat(offset + 40, scale * far / Math.max(1, distanceEnd));
      // Extend the ordinary 1024-block atmosphere proportionally; retain weather attenuation
      // and the shorter fog supplied by water, blindness, darkness and custom environments.
      if (environmentalEnd >= 768 && environmentalEnd <= 1024 && far > 1024) {
        buffer.putFloat(offset + 16, environmentalStart * far / 1024);
        buffer.putFloat(offset + 20, environmentalEnd * far / 1024);
      }
    }
    float snow = com.shnewbs.deepprimal.client.SnowAtmosphere.density();
    if (snow > .01f && mc != null && mc.player != null && !mc.player.isUnderWater()) {
      float visibility = 384 - snow * 256;
      buffer.putFloat(offset + 16, Math.min(buffer.getFloat(offset + 16), visibility * .25f));
      buffer.putFloat(offset + 20, Math.min(buffer.getFloat(offset + 20), visibility));
      buffer.putFloat(offset, buffer.getFloat(offset) * (1 - snow * .15f) + .78f * snow * .15f);
      buffer.putFloat(
          offset + 4, buffer.getFloat(offset + 4) * (1 - snow * .15f) + .84f * snow * .15f);
      buffer.putFloat(
          offset + 8, buffer.getFloat(offset + 8) * (1 - snow * .15f) + .9f * snow * .15f);
    }
    buffer.position(offset + FogRenderer.FOG_UBO_SIZE);
  }
}
