package com.shnewbs.deepprimal.engine;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Last-mile plant admission for NEW, explicitly opted-in V8-r6 chunks only.
 * The saved, signed-world-coordinate placement plan remains authoritative;
 * this guard prevents a later native setter callback from leaving an invalid
 * plant on displaced/non-sturdy/waterlogged ground or inside an occupied cell.
 * It does not generate water, retry skipped plants or mutate any state.
 *
 * <p>Must execute inside the caller's existing prewrite undo/heightmap scope,
 * AFTER topsoil painting and immediately BEFORE each plant write. A false
 * result is an intentional skip, not permission to choose another location.
 * Plant survival after external block updates still belongs to the native game.
 */
public final class V8VegetationNativeAdmission {
  private V8VegetationNativeAdmission() {}

  public static boolean permits(ChunkAccess chunk, BlockPos.MutableBlockPos cursor,
      int worldX, int plantY, int worldZ, BlockState plannedSupport) {
    Objects.requireNonNull(chunk, "V8 destination chunk");
    Objects.requireNonNull(cursor, "V8 write cursor");
    Objects.requireNonNull(plannedSupport, "V8 planned plant support");
    if (plannedSupport.isAir()) return false;
    var below = cursor.set(worldX, plantY - 1, worldZ);
    BlockState actual = Objects.requireNonNull(chunk.getBlockState(below), "V8 actual support");
    if (!plannedSupport.equals(actual) || !actual.getFluidState().isEmpty()
        || !actual.isFaceSturdy(chunk, below, Direction.UP)) return false;
    var destination = cursor.set(worldX, plantY, worldZ);
    BlockState there = Objects.requireNonNull(chunk.getBlockState(destination), "V8 plant destination");
    return there.isAir() && there.getFluidState().isEmpty();
  }
}
