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

  public SurfaceWaterAquifer(Aquifer delegate, NoiseChunk chunk, int seaLevel) {
    this.delegate = delegate;
    this.chunk = chunk;
    this.seaLevel = seaLevel;
  }

  @Override
  public @Nullable BlockState computeSubstance(
      DensityFunction.FunctionContext context, double density) {
    int y = context.blockY();
    // The native preliminary height is cached on its existing 4-block grid.
    // Restrict filling to the surface band; never turn every cave below sea
    // level into water or evaluate fresh noise for every vertical voxel.
    if (density <= 0.0
        && y < seaLevel
        && y >= seaLevel - 16
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
