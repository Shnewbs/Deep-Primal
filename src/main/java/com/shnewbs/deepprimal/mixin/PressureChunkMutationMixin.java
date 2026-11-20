package com.shnewbs.deepprimal.mixin;

import com.shnewbs.deepprimal.engine.ShorelineHabitat;
import com.shnewbs.deepprimal.engine.WaterRuntime;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
    if (chunk.getLevel() instanceof ServerLevel server && server.getServer().isSameThread()) {
      var before = callback.getReturnValue();
      WaterRuntime.changed(server, pos, before, state);
      // Finite transfers suppress normal shape notifications. Recheck only an actual pad losing support.
      if ((flags & Block.UPDATE_KNOWN_SHAPE) != 0 && before != null && before != state
          && ShorelineHabitat.supportsLily(before) && !ShorelineHabitat.supportsLily(state)) {
        var above = pos.above();
        if (chunk.getBlockState(above).is(Blocks.LILY_PAD))
          server.neighborShapeChanged(Direction.DOWN, above, pos, state, Block.UPDATE_CLIENTS, 1);
      }
    }
  }
}
