package com.shnewbs.deepprimal.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class LodDirtyMixin {
  @Inject(method = "sendBlockUpdated", at = @At("RETURN"))
  private void deepprimal$changed(
      BlockPos p, BlockState old, BlockState state, int flags, CallbackInfo ci) {
    com.shnewbs.deepprimal.client.render.VoxelLodRenderer.INSTANCE.dirty(
        Math.floorDiv(p.getX(), 16), Math.floorDiv(p.getZ(), 16));
  }

  @Inject(method = "setBlocksDirty", at = @At("RETURN"))
  private void deepprimal$packet(BlockPos p, BlockState old, BlockState state, CallbackInfo ci) {
    com.shnewbs.deepprimal.client.render.VoxelLodRenderer.INSTANCE.dirty(
        Math.floorDiv(p.getX(), 16), Math.floorDiv(p.getZ(), 16));
  }
}
