package com.shnewbs.deepprimal.mixin.client;

import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.settings.WaterVisualConfig;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Water corners and animated-page discovery extend beyond a section's integer Y range. */
@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class WaterSectionBoundsMixin {
  @Unique private AABB deepprimal$originalBounds;
  @Unique private AABB deepprimal$waterBounds;

  @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
  private void deepprimal$waterBounds(CallbackInfoReturnable<AABB> result) {
    if (!AnimatedWaterRenderer.ownsWater() && !WaterVisualConfig.SMOOTH.get()) return;
    AABB original = result.getReturnValue();
    if (original != deepprimal$originalBounds) {
      deepprimal$originalBounds = original;
      deepprimal$waterBounds = original.inflate(0, 1, 0);
    }
    result.setReturnValue(deepprimal$waterBounds);
  }
}
