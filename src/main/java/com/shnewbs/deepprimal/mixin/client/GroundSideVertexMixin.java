package com.shnewbs.deepprimal.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shnewbs.deepprimal.client.GroundSideSpriteSource;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VertexConsumer.class)
public interface GroundSideVertexMixin {
  @ModifyExpressionValue(method = {"putBakedQuad", "putBlockBakedQuad"}, at = @At(value = "INVOKE",
      target = "Lcom/mojang/blaze3d/vertex/QuadInstance;getLightCoordsWithEmission(II)I"))
  default int deepprimal$materialLight(int light, @Local(argsOnly = true) BakedQuad quad) {
    return quad.materialInfo().sprite().contents() instanceof GroundSideSpriteSource.MaterialContents
        ? light | GroundSideSpriteSource.LIGHT_MARKER : light;
  }
}
