package com.shnewbs.deepprimal.water;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.shnewbs.deepprimal.DeepPrimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Rain visits native sampled columns; soil storage persists in the chunk without a global scan. */
public final class WeatherHydrology {
  public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
      DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, DeepPrimal.ID);
  public static final DeferredHolder<AttachmentType<?>, AttachmentType<List<Integer>>> SOIL =
      ATTACHMENTS.register(
          "soil_water",
          () ->
              AttachmentType.<List<Integer>>builder(() -> Collections.nCopies(256, 0))
                  .serialize(
                      Codec.intRange(0, 8)
                          .listOf()
                          .validate(
                              values ->
                                  values.size() == 256
                                      ? DataResult.success(List.copyOf(values))
                                      : DataResult.error(() -> "Expected 256 soil water columns"))
                          .fieldOf("columns"))
                  .build());

  public static void tick(ServerLevel level, BlockPos top, Biome.Precipitation kind) {
    if (!FiniteWater.enabled() || !RiverConfig.RAIN_RUNOFF.get() || !level.hasChunkAt(top)) return;
    var p = top.below();
    var block = level.getBlockState(p);
    var chunk = level.getChunkAt(p);
    int index = (p.getZ() & 15) * 16 + (p.getX() & 15);
    var stored = chunk.getData(SOIL);
    int moisture = stored.get(index);
    if (level.isRaining() && kind == Biome.Precipitation.RAIN) {
      double intensity =
          level.getRainLevel(1)
              * (.65
                  + .35
                      * Math.sin(
                          p.getX() * .0013 + p.getZ() * .0017 + level.getGameTime() * .00008));
      if (level.getRandom().nextDouble() > intensity) return;
      if (FiniteWater.units(block) > 0) {
        FiniteWater.exchange(level, p, 1, false);
        return;
      }
      boolean soil = block.is(BlockTags.DIRT) || block.is(Blocks.FARMLAND);
      if (soil && moisture < 8) {
        set(chunk, stored, index, moisture + 1);
        if (block.is(Blocks.FARMLAND))
          level.setBlockAndUpdate(p, block.setValue(FarmlandBlock.MOISTURE, 7));
      } else if ((soil || block.is(BlockTags.BASE_STONE_OVERWORLD))
          && level.getBlockState(top).isAir()) {
        FiniteWater.exchange(level, top, 1, false);
      }
    } else if (!level.isRaining()
        && RiverConfig.EVAPORATION.get()
        && level.dimensionType().hasSkyLight()
        && kind != Biome.Precipitation.SNOW
        && level.getRandom().nextInt(16) == 0) {
      if (FiniteWater.units(block) > 0) FiniteWater.exchange(level, p, -1, false);
      else if (moisture > 0) set(chunk, stored, index, moisture - 1);
    }
  }

  private static void set(
      net.minecraft.world.level.chunk.LevelChunk chunk, List<Integer> old, int index, int amount) {
    var next = new ArrayList<>(old);
    next.set(index, amount);
    chunk.setData(SOIL, List.copyOf(next));
  }

  private WeatherHydrology() {}
}
