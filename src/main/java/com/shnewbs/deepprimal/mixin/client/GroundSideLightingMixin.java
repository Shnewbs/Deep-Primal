package com.shnewbs.deepprimal.mixin.client;

import com.mojang.blaze3d.vertex.QuadInstance;
import com.shnewbs.deepprimal.client.GroundSideSpriteSource.MaterialContents;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public abstract class GroundSideLightingMixin {
  @Shadow @Final private QuadInstance quadInstance;
  @Unique private final int[] deepprimal$shade = new int[4];

  @Inject(method = "putQuadWithTint", at = @At("HEAD"))
  private void deepprimal$soilShade(BlockQuadOutput output, float x, float y, float z,
      BlockAndTintGetter level, BlockState state, BlockPos pos, BakedQuad quad, CallbackInfo ci) {
    if (!(quad.materialInfo().sprite().contents() instanceof MaterialContents)) return;
    for (int vertex = 0; vertex < 4; vertex++) {
      int color = quadInstance.getColor(vertex);
      deepprimal$shade[vertex] = (color >> 16) & 255;
    }
  }

  @Inject(method = "putQuadWithTint", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/client/renderer/block/BlockQuadOutput;put(FFFLnet/minecraft/client/resources/model/geometry/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadInstance;)V"))
  private void deepprimal$preserveShade(BlockQuadOutput output, float x, float y, float z,
      BlockAndTintGetter level, BlockState state, BlockPos pos, BakedQuad quad, CallbackInfo ci) {
    if (!(quad.materialInfo().sprite().contents() instanceof MaterialContents)) return;
    for (int vertex = 0; vertex < 4; vertex++)
      quadInstance.setColor(vertex, deepprimal$shade[vertex] << 24 | (quadInstance.getColor(vertex) & 0xffffff));
  }
}
