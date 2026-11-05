package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.engine.WaterRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Observe actual writes even when commands/adapters suppress neighbor notifications. */
@Mixin(LevelChunk.class)
abstract class PressureChunkMutationMixin {
  @Inject(method = "setBlockState", at = @At("RETURN"))
  private void deepprimal$invalidatePressure(BlockPos pos, BlockState state, int flags,
      CallbackInfoReturnable<BlockState> callback) {
    var chunk = (LevelChunk) (Object) this;
    if (chunk.getLevel() instanceof ServerLevel server && server.getServer().isSameThread())
      WaterRuntime.changed(server, pos, callback.getReturnValue(), state);
  }
}
