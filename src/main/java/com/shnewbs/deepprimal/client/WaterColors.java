package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.core.WaterDynamics;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public final class WaterColors {
  public static int palette(Biome biome) {
    return WaterDynamics.palette(
        biome.getWaterColor(), biome.getBaseTemperature(), biome.hasPrecipitation() ? 1 : 0);
  }

  public static int blended(BlockAndTintGetter level, BlockPos pos) {
    int x = Math.floorDiv(pos.getX(), 16) * 16, z = Math.floorDiv(pos.getZ(), 16) * 16;
    double u = Math.floorMod(pos.getX(), 16) / 16.0, v = Math.floorMod(pos.getZ(), 16) / 16.0;
    int a = level.getBlockTint(new BlockPos(x, pos.getY(), z), BiomeColors.WATER_COLOR_RESOLVER);
    int b =
        level.getBlockTint(new BlockPos(x + 16, pos.getY(), z), BiomeColors.WATER_COLOR_RESOLVER);
    int c =
        level.getBlockTint(new BlockPos(x, pos.getY(), z + 16), BiomeColors.WATER_COLOR_RESOLVER);
    int d =
        level.getBlockTint(
            new BlockPos(x + 16, pos.getY(), z + 16), BiomeColors.WATER_COLOR_RESOLVER);
    int color = WaterDynamics.mix(WaterDynamics.mix(a, b, u), WaterDynamics.mix(c, d, u), v);
    return 0xff000000
        | WaterDynamics.mix(color, 0x63a9b6, Math.clamp((pos.getY() - 140) / 180.0, 0, .7));
  }

  private WaterColors() {}
}
