package com.shnewbs.deepprimal.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.shnewbs.deepprimal.core.VerticalRange;
import com.shnewbs.deepprimal.settings.ClientPerformanceConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class CameraMixin {
  @ModifyExpressionValue(
      method = "update",
      at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0))
  private float deepprimal$verticalFarPlane(float original) {
    var level = Minecraft.getInstance().level;
    if (level == null) return original;
    int sections =
        VerticalRange.visibilityRadius(
            level.getSectionsCount(), ClientPerformanceConfig.VERTICAL_RENDER_DISTANCE.get());
    return Math.max(
        Math.max(original, (sections + 2) * 16.0f),
        ((com.shnewbs.deepprimal.client.render.HorizonRenderer.active()
                || com.shnewbs.deepprimal.client.render.VoxelLodRenderer.active())
            ? Math.max(
                        com.shnewbs.deepprimal.client.render.HorizonRenderer.farDistance(),
                        com.shnewbs.deepprimal.client.render.VoxelLodRenderer.distance())
                    * 16.0f
                + 64
            : 0));
  }
}
