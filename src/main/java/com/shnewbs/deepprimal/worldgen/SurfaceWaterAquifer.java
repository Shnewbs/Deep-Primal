package com.shnewbs.deepprimal.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.jspecify.annotations.Nullable;

/** Surface water for versioned waterways; deep cave fluids retain the native aquifer. */
public final class SurfaceWaterAquifer implements Aquifer {
  private final Aquifer delegate;
  private final NoiseChunk chunk;
  private final int seaLevel;
  private boolean suppliedSurfaceWater;
  private final int[] waterLevels;
  private final int minX, minZ;

  public SurfaceWaterAquifer(Aquifer delegate, NoiseChunk chunk, int seaLevel) {
    this(delegate, chunk, seaLevel, null, 0, 0);
  }

  public SurfaceWaterAquifer(
      Aquifer delegate, NoiseChunk chunk, int seaLevel, WaterwayHeight routed, int minX, int minZ) {
    this.delegate = delegate;
    this.chunk = chunk;
    this.seaLevel = seaLevel;
    this.minX = minX;
    this.minZ = minZ;
    waterLevels = routed == null ? null : new int[256];
    if (routed != null) {
      // Immutable column data: no routing or fresh noise in the per-voxel hot path.
      for (int z = 0; z < 16; z++)
        for (int x = 0; x < 16; x++) {
          var sample = routed.sample(minX + x, minZ + z);
          waterLevels[z * 16 + x] = (int) Math.floor(sample.waterLevel());
        }
    }
  }

  @Override
  public @Nullable BlockState computeSubstance(
      DensityFunction.FunctionContext context, double density) {
    int y = context.blockY();
    int x = context.blockX() - minX, z = context.blockZ() - minZ;
    int surface =
        waterLevels != null && x >= 0 && z >= 0 && x < 16 && z < 16
            ? waterLevels[z * 16 + x]
            : seaLevel;
    // The native preliminary height is cached on its existing 4-block grid.
    // Restrict filling to the surface band; never turn every cave below sea
    // level into water or evaluate fresh noise for every vertical voxel.
    if (density <= 0.0
        && y < surface
        && y >= surface - 16
        && y >= chunk.preliminarySurfaceLevel(context.blockX(), context.blockZ())) {
      suppliedSurfaceWater = true;
      return Blocks.WATER.defaultBlockState();
    }
    suppliedSurfaceWater = false;
    return delegate.computeSubstance(context, density);
  }

  @Override
  public boolean shouldScheduleFluidUpdate() {
    return !suppliedSurfaceWater && delegate.shouldScheduleFluidUpdate();
  }
}
