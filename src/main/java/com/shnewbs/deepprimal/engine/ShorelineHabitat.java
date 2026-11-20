package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainEngine;
import com.shnewbs.deepprimal.core.TerrainNoise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/** Bed and bank communities follow depth, current and climate in world coordinates. */
public final class ShorelineHabitat {
  static Block bed(TerrainNoise noise, int x, int z, TerrainEngine.Sample sample, boolean ocean) {
    double depth = sample.water() - sample.ground();
    if (ocean) {
      if (sample.temperature() < .70 || depth < 1.5 || depth > 9 || sample.speed() > .12) return null;
      double colony = noise.noise(x, z, 16, 94601) + .38 * noise.noise(x, z, 5, 94602);
      if (colony < .21 + .035 * Math.abs(depth - 4)) return null;
      double species = noise.noise(x, z, 7, 94603);
      return species < -.25 ? Blocks.TUBE_CORAL_BLOCK : species > .25 ? Blocks.HORN_CORAL_BLOCK : Blocks.BRAIN_CORAL_BLOCK;
    }
    if (sample.speed() < .32 || depth < .6 || depth > 4.5) return null;
    double rib = noise.noise(x, z, 12, 94604)
        + .45 * noise.noise(x + 2 * sample.flowX(), z + 2 * sample.flowZ(), 4.5, 94605);
    if (rib < .16) return null;
    return rib > .62 ? Blocks.ANDESITE
        : sample.rain() > .55 && noise.noise(x, z, 9, 94606) > .13 ? Blocks.MOSSY_COBBLESTONE : Blocks.COBBLESTONE;
  }

  public static boolean supportsLily(BlockState state) {
    return state.is(EngineRegistry.WATER_BLOCK.get()) && !state.getValue(EngineWaterBlock.OCEAN)
        && state.getValue(EngineFluid.UNITS) == 16;
  }

  static void lilies(ChunkAccess chunk, TerrainNoise noise, int x, int z, TerrainEngine.Sample s) {
    double depth = s.water() - s.ground();
    if (depth < 1 || depth > 4.5 || s.speed() > .1 || s.temperature() < .30 || s.rain() < .38
        || noise.noise(x, z, 12, 94607) < .17 || noise.hash(x, z, 94608) > .32) return;
    var pad = new BlockPos(x, (int) Math.ceil(s.water()), z);
    if (pad.getY() <= chunk.getMaxY() && supportsLily(chunk.getBlockState(pad.below()))
        && chunk.getBlockState(pad).isAir()) chunk.setBlockState(pad, Blocks.LILY_PAD.defaultBlockState());
  }

  static void bank(ChunkAccess chunk, TerrainNoise noise, BlockPos plant, TerrainEngine.Sample land,
      TerrainEngine.Sample water, boolean reeds) {
    if (!chunk.getBlockState(plant).isAir()) return;
    int x = plant.getX(), z = plant.getZ(), floor = plant.getY() - 1;
    BlockState soil = chunk.getBlockState(plant.below());
    if (water.speed() >= .32) {
      double patch = noise.noise(x, z, 8, 94609) + .35 * noise.noise(x, z, 3.5, 94610);
      if (patch < .30 || !soil.isFaceSturdy(chunk, plant.below(), Direction.UP)) return;
      Block rock = land.rain() > .55 && patch > .56 ? Blocks.MOSSY_COBBLESTONE : Blocks.ANDESITE;
      chunk.setBlockState(plant, rock.defaultBlockState());
      if (patch > .96 && plant.getY() < chunk.getMaxY() && chunk.getBlockState(plant.above()).isAir())
        chunk.setBlockState(plant.above(), rock.defaultBlockState());
    } else if (reeds && land.temperature() >= .28 && land.rain() >= .38 && land.slope() <= .85
        && floor > Math.floor(water.ground()) && floor < water.water()
        && soil.is(BlockTags.SUPPORTS_SUGAR_CANE) && noise.noise(x, z, 10, 94611) > .10
        && noise.hash(x, z, 94612) < .55) {
      int height = 1 + (int) (noise.hash(x, z, 94613) * 3);
      for (int i = 0; i < height && plant.getY() + i <= chunk.getMaxY(); i++) {
        var segment = plant.above(i);
        if (!chunk.getBlockState(segment).isAir()) break;
        chunk.setBlockState(segment, Blocks.SUGAR_CANE.defaultBlockState());
      }
    }
  }

  private ShorelineHabitat() {}
}
