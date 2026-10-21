package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.core.lod.ColumnarChunk;
import java.util.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;

/** Copies packed containers on the game thread. Workers see copies, never chunks or a world. */
public final class NativeLodCapture {
  private final PalettedContainer<BlockState>[] sections;
  private final Map<BlockState, Integer> ids;
  private final List<String> palette;
  private final int x, z, min, height;
  private final long revision;

  @SuppressWarnings("unchecked")
  public NativeLodCapture(ClientLevel level, LevelChunk chunk, long revision) {
    if (!net.minecraft.client.Minecraft.getInstance().isSameThread())
      throw new IllegalStateException("LOD capture thread");
    x = chunk.getPos().x();
    z = chunk.getPos().z();
    min = chunk.getMinY();
    height = chunk.getHeight();
    this.revision = revision;
    sections = new PalettedContainer[chunk.getSectionsCount()];
    ids = new HashMap<>();
    palette = new ArrayList<>();
    ids.put(Blocks.AIR.defaultBlockState(), 0);
    palette.add("00000000|minecraft:air");
    BlockPos p = new BlockPos(x * 16 + 8, level.getSeaLevel(), z * 16 + 8);
    for (int i = 0; i < sections.length; i++) {
      var section = chunk.getSections()[i];
      if (section.hasOnlyAir()) continue;
      sections[i] = section.getStates().copy();
      sections[i].getAll(
          state -> {
            if (ids.containsKey(state)) return;
            int color;
            if (state.isAir()
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(net.minecraft.tags.BlockTags.FLOWERS)) color = 0;
            else if (!state.getFluidState().isEmpty())
              color =
                  ((224
                              + (int)
                                  Math.clamp(
                                      Math.round(state.getFluidState().getOwnHeight() * 16), 1, 16))
                          << 24)
                      | (net.minecraft.client.renderer.BiomeColors.getAverageWaterColor(level, p)
                          & 0xffffff);
            else if (state.is(net.minecraft.tags.BlockTags.LEAVES))
              color =
                  net.minecraft.util.ARGB.multiply(
                      LodMaterialColors.texture(state),
                      0xff000000
                          | (net.minecraft.client.renderer.BiomeColors.getAverageFoliageColor(
                                  level, p)
                              & 0xffffff));
            else if (state.is(Blocks.GRASS_BLOCK))
              color =
                  net.minecraft.util.ARGB.multiply(
                      0xffadadad,
                      0xff000000
                          | (net.minecraft.client.renderer.BiomeColors.getAverageGrassColor(
                                  level, p)
                              & 0xffffff));
            else color = LodMaterialColors.texture(state);
            ids.put(state, palette.size());
            palette.add(String.format(java.util.Locale.ROOT, "%08x|%s", color, state));
          });
    }
  }

  public ColumnarChunk build() {
    var b = new ColumnarChunk.Builder(x, z, min, height, revision);
    int[] column = new int[height];
    for (int c = 0; c < 256; c++) {
      if (Thread.currentThread().isInterrupted())
        throw new java.util.concurrent.CancellationException();
      Arrays.fill(column, 0);
      for (int s = 0; s < sections.length; s++)
        if (sections[s] != null)
          for (int y = 0; y < 16; y++)
            column[s * 16 + y] = ids.get(sections[s].get(c % 16, y, c / 16));
      b.column(column);
    }
    return b.finish(palette);
  }
}
