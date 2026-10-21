package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.core.WaterDynamics;
import com.shnewbs.deepprimal.settings.WaterVisualConfig;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/** Decorator preserves every registered source, including constant spruce/birch palettes. */
public record SnowFoliage(BlockTintSource delegate) implements BlockTintSource {
  public int color(BlockState state) {
    return delegate.color(state);
  }

  public Set<Property<?>> relevantProperties() {
    return delegate.relevantProperties();
  }

  public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
    return delegate.colorAsTerrainParticle(state, level, pos);
  }

  public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
    int original = delegate.colorInWorld(state, level, pos);
    if (!(state.getBlock() instanceof LeavesBlock) || !WaterVisualConfig.SNOW.get())
      return original;
    var world = Minecraft.getInstance().level;
    if (world == null
        || !world.hasChunkAt(pos)
        || !(com.shnewbs.deepprimal.water.RiverConfig.GLOBAL_WINTER.get()
            || world.getBiome(pos).value().coldEnoughToSnow(pos, world.getSeaLevel())))
      return original;
    var above = level.getBlockState(pos.above());
    if (!above.is(Blocks.SNOW) && !above.is(Blocks.SNOW_BLOCK) && !world.isRaining())
      return original;
    return 0xff000000 | WaterDynamics.mix(original, 0xe0edf0, .65);
  }
}
