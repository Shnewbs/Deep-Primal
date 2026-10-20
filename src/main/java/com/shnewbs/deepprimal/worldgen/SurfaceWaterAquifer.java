package com.shnewbs.deepprimal.worldgen;

import com.shnewbs.deepprimal.water.RiverWater;
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
  private final int[] bedLevels;
  private final BlockState[] waterStates;
  private final int minX, minZ;

  public SurfaceWaterAquifer(Aquifer delegate, NoiseChunk chunk, int seaLevel) {
    this(delegate, chunk, seaLevel, null, 0, 0);
  }

  public SurfaceWaterAquifer(
      Aquifer delegate, NoiseChunk chunk, int seaLevel, WaterwayHeight routed, int minX, int minZ) {
    this(delegate, chunk, seaLevel, routed, minX, minZ, 4);
  }

  public SurfaceWaterAquifer(
      Aquifer delegate,
      NoiseChunk chunk,
      int seaLevel,
      WaterwayHeight routed,
      int minX,
      int minZ,
      int cellWidth) {
    this.delegate = delegate;
    this.chunk = chunk;
    this.seaLevel = seaLevel;
    this.minX = minX;
    this.minZ = minZ;
    waterLevels = routed == null ? null : new int[256];
    boolean contained = routed != null && routed.contained();
    bedLevels = contained ? new int[256] : null;
    waterStates = contained ? new BlockState[256] : null;
    int gridSide = 16 / cellWidth + 1;
    double[] grid = contained ? new double[gridSide * gridSide] : null;
    if (contained) {
      for (int z = 0; z < gridSide; z++)
        for (int x = 0; x < gridSide; x++)
          grid[z * gridSide + x] =
              routed.sample(minX + x * cellWidth, minZ + z * cellWidth).height();
    }
    if (routed != null) {
      // Immutable column data: no routing or fresh noise in the per-voxel hot path.
      for (int z = 0; z < 16; z++)
        for (int x = 0; x < 16; x++) {
          var sample = routed.sample(minX + x, minZ + z);
          waterLevels[z * 16 + x] = (int) Math.floor(sample.waterLevel());
          if (contained) {
            // Match the native density interpolation grid, rather than its
            // rounded preliminary-surface estimate. Fill all the way to the
            // bed so an elevated river cannot become a detached water ceiling.
            int gx = x / cellWidth, gz = z / cellWidth;
            double tx = (x % cellWidth) / (double) cellWidth;
            double tz = (z % cellWidth) / (double) cellWidth;
            double a = grid[gz * gridSide + gx], b = grid[gz * gridSide + gx + 1];
            double c = grid[(gz + 1) * gridSide + gx], d = grid[(gz + 1) * gridSide + gx + 1];
            bedLevels[z * 16 + x] =
                (int) Math.floor((a + tx * (b - a)) * (1 - tz) + (c + tx * (d - c)) * tz);
            waterStates[z * 16 + x] =
                sample.strength() > 0 && (sample.flowX() != 0 || sample.flowZ() != 0)
                    ? RiverWater.BLOCK.get().flowing(sample.flowX(), sample.flowZ(), sample.speed())
                    : Blocks.WATER.defaultBlockState();
          }
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
    if (bedLevels != null && x >= 0 && z >= 0 && x < 16 && z < 16) {
      int index = z * 16 + x, bed = bedLevels[index];
      if (surface > bed + 1) {
        if (density <= 0 && y < surface && y > bed) {
          suppliedSurfaceWater = true;
          return waterStates[index];
        }
        // Native biome carvers query this same aquifer. A thin channel floor
        // keeps their cave entrances from punching holes under a filled reach.
        if (density <= 0 && y <= bed && y >= bed - 2) {
          suppliedSurfaceWater = true;
          return Blocks.STONE.defaultBlockState();
        }
      }
      suppliedSurfaceWater = false;
      return delegate.computeSubstance(context, density);
    }
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
