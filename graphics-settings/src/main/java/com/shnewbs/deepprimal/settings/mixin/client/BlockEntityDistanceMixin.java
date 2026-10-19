package com.shnewbs.deepprimal.settings.mixin.client;

import com.shnewbs.deepprimal.settings.ClientPerformanceConfig;
import com.shnewbs.deepprimal.settings.VisualPerformance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityDistanceMixin {
  @Shadow private Vec3 cameraPos;

  @Shadow
  public abstract <E extends BlockEntity, S extends BlockEntityRenderState>
      BlockEntityRenderer<E, S> getRenderer(E entity);

  @Inject(
      method =
          "tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;Lnet/minecraft/client/renderer/culling/Frustum;)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;",
      at = @At("HEAD"),
      cancellable = true)
  private void deepprimal$visualRange(
      BlockEntity entity,
      float delta,
      ModelFeatureRenderer.CrumblingOverlay breaking,
      Frustum frustum,
      CallbackInfoReturnable<BlockEntityRenderState> callback) {
    int radius = ClientPerformanceConfig.BLOCK_ENTITY_LIMIT.get();
    if (radius == 0 || cameraPos == null) return;
    var renderer = getRenderer(entity);
    if (renderer == null || renderer.shouldRenderOffScreen()) return;
    if (VisualPerformance.outside(
        renderer.getRenderBoundingBox(entity), cameraPos.x, cameraPos.y, cameraPos.z, radius)) {
      VisualPerformance.blockEntitiesSkipped++;
      callback.setReturnValue(null);
    }
  }
}
