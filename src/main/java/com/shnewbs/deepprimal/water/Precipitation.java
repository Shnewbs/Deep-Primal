package com.shnewbs.deepprimal.water;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Extend native precipitation at its already-selected loaded column. No world-wide weather loop.
 */
public final class Precipitation {
  public static void tick(ServerLevel level, BlockPos column) {
    if (!level.hasChunkAt(column)) return;
    if (level.getChunkSource().getGenerator() instanceof com.shnewbs.deepprimal.engine.EngineChunkGenerator) return;
    BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column);
    var biome = level.getBiome(top).value();
    var kind = biome.getPrecipitationAt(top, level.getSeaLevel());
    if (RiverConfig.GLOBAL_WINTER.get() && biome.hasPrecipitation())
      kind = Biome.Precipitation.SNOW;
    WeatherHydrology.tick(level, top, kind);
    if (!level.isRaining()) return;
    if (kind == Biome.Precipitation.RAIN
        && RiverConfig.RAIN_REFILL.get()
        && !FiniteWater.enabled()) {
      var p = top.below();
      var state = level.getBlockState(p);
      var fluid = state.getFluidState();
      if (state.getBlock() instanceof LiquidBlock
          && fluid.is(FluidTags.WATER)
          && !fluid.isSource()
          && !fluid.getValue(FlowingFluid.FALLING))
        level.setBlockAndUpdate(p, state.setValue(LiquidBlock.LEVEL, 0));
    }
    if (kind != Biome.Precipitation.SNOW || !RiverConfig.SNOW_DRIFTS.get()) return;
    snow(level, top);
    if (level.getBlockState(top.below()).is(BlockTags.LEAVES)) {
      var cursor = top.below().mutable();
      for (int i = 0; i < 12 && cursor.getY() > level.getMinY() + 1; i++) {
        cursor.move(net.minecraft.core.Direction.DOWN);
        var state = level.getBlockState(cursor);
        if (state.isAir() || state.is(BlockTags.LEAVES)) continue;
        snow(level, state.is(Blocks.SNOW) ? cursor.immutable() : cursor.above());
        break;
      }
    }
  }

  public static void snow(ServerLevel level, BlockPos surface) {
    BlockPos pos =
        level.getBlockState(surface).is(Blocks.SNOW)
            ? surface
            : level.getBlockState(surface.below()).is(Blocks.SNOW) ? surface.below() : surface;
    BlockState state = level.getBlockState(pos);
    if (!state.isAir() && !state.is(Blocks.SNOW)) return;
    boolean unlimited = RiverConfig.UNLIMITED_SNOW.get();
    int cap = unlimited ? Integer.MAX_VALUE : RiverConfig.SNOW_CAP.get(), below = 0;
    var cursor = pos.below().mutable();
    while (!unlimited && below <= cap && level.getBlockState(cursor).is(Blocks.SNOW_BLOCK)) {
      below += 8;
      cursor.move(net.minecraft.core.Direction.DOWN);
    }
    int layers = state.is(Blocks.SNOW) ? state.getValue(SnowLayerBlock.LAYERS) : 0;
    int available = Math.max(0, cap - below);
    if (layers > available) {
      level.setBlockAndUpdate(
          pos,
          available == 0
              ? Blocks.AIR.defaultBlockState()
              : Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, available));
      return;
    }
    if (layers == 8 && below + 8 <= cap) {
      BlockState full = Blocks.SNOW_BLOCK.defaultBlockState();
      Block.pushEntitiesUp(state, full, level, pos);
      level.setBlockAndUpdate(pos, full);
      return;
    }
    if (below + layers >= cap) return;
    BlockState next =
        layers >= 8
            ? Blocks.SNOW_BLOCK.defaultBlockState()
            : Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers + 1);
    if (!next.canSurvive(level, pos)
        || level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos) >= 10) return;
    Block.pushEntitiesUp(state, next, level, pos);
    level.setBlockAndUpdate(pos, next);
  }

  private Precipitation() {}
}
