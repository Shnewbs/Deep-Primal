package com.shnewbs.deepprimal.mixin.client;

import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.engine.EngineRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class WaterPresentationMixin {
  @Inject(
      method =
          "setBlockDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V",
      at = @At("HEAD"),
      cancellable = true)
  private void deepprimal$shape(BlockPos p, BlockState old, BlockState next, CallbackInfo ci) {
    var level = net.minecraft.client.Minecraft.getInstance().level;
    if (level == null || !AnimatedWaterRenderer.ownsWater()) return;
    AnimatedWaterRenderer.INSTANCE.changed(level, p);
    boolean was = old.is(EngineRegistry.WATER_BLOCK.get()),
        now = next.is(EngineRegistry.WATER_BLOCK.get());
    if (old.isAir() && now) AnimatedWaterRenderer.INSTANCE.newlyWet(p);
    if ((was || now) && (was || old.isAir()) && (now || next.isAir())) ci.cancel();
  }

  @Inject(method = "blockChanged", at = @At("HEAD"), cancellable = true)
  private void deepprimal$liquid(
      BlockGetter level, BlockPos p, BlockState old, BlockState next, int flags, CallbackInfo ci) {
    if (!AnimatedWaterRenderer.ownsWater() || !(level instanceof ClientLevel client)) return;
    // Solid/plant edits still rebuild terrain, including edits next to a liquid face.
    AnimatedWaterRenderer.INSTANCE.changed(client, p);
    boolean was = old.is(EngineRegistry.WATER_BLOCK.get()),
        now = next.is(EngineRegistry.WATER_BLOCK.get());
    if (old.isAir() && now) AnimatedWaterRenderer.INSTANCE.newlyWet(p);
    if ((was || now) && (was || old.isAir()) && (now || next.isAir())) {
      AnimatedWaterRenderer.INSTANCE.terrainInvalidationsAvoided++;
      ci.cancel();
    }
  }

  @Inject(method = "allChanged", at = @At("HEAD"))
  private void deepprimal$reload(CallbackInfo ci) {
    AnimatedWaterRenderer.INSTANCE.reset();
  }

  @Inject(method = "setSectionDirty(IIIZ)V", at = @At("HEAD"))
  private void deepprimal$lighting(int x, int y, int z, boolean playerChanged, CallbackInfo ci) {
    AnimatedWaterRenderer.INSTANCE.sectionChanged(x, y, z);
  }
}
