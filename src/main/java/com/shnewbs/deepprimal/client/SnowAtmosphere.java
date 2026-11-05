package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.water.RiverConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

/** Eight loaded columns per tick; local snow/weather controls a smoothed atmosphere. */
public final class SnowAtmosphere {
  private static net.minecraft.client.multiplayer.ClientLevel level;
  private static float density;

  public static float density() {
    return density;
  }

  public static void tick() {
    var mc = Minecraft.getInstance();
    if (level != mc.level) {
      level = mc.level;
      density = 0;
      WaterPalettes.clear();
    }
    if (level == null || mc.player == null || mc.isPaused()) return;
    var pos = mc.player.blockPosition();
    boolean cold =
        RiverConfig.GLOBAL_WINTER.get()
            || level.getBiome(pos).value().coldEnoughToSnow(pos, level.getSeaLevel());
    float target = 0;
    if (cold && level.canSeeSky(pos)) {
      int covered = 0;
      for (int i = 0; i < 8; i++) {
        var p = pos.offset((i % 3 - 1) * 6, 0, (i / 3 - 1) * 6);
        if (!level.hasChunkAt(p)) continue;
        var top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, p).below();
        var state = level.getBlockState(top);
        if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(BlockTags.SNOW))
          covered++;
      }
      target = Math.clamp(level.getRainLevel(1) * .7f + covered / 8f * .3f, 0, 1);
    }
    density += (target - density) * .05f;
  }

  private SnowAtmosphere() {}
}
