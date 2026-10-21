package com.shnewbs.deepprimal.engine;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.core.TerrainEngine;
import java.util.*;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;

/**
 * Climate/geography selection is ours; existing biome assets are temporary material/content
 * palettes.
 */
public final class RegionBiomeSource extends BiomeSource {
  public static final MapCodec<RegionBiomeSource> CODEC =
      RecordCodecBuilder.mapCodec(
          i ->
              i.group(Biome.CODEC.listOf().fieldOf("palette").forGetter(b -> b.palette))
                  .apply(i, RegionBiomeSource::new));
  private final List<Holder<Biome>> palette;
  private volatile TerrainEngine engine = new TerrainEngine(0);

  public RegionBiomeSource(List<Holder<Biome>> palette) {
    if (palette.size() != 8) throw new IllegalArgumentException("Engine biome palette");
    this.palette = List.copyOf(palette);
  }

  public void bind(TerrainEngine engine) {
    this.engine = engine;
  }

  protected MapCodec<? extends BiomeSource> codec() {
    return CODEC;
  }

  protected Stream<Holder<Biome>> collectPossibleBiomes() {
    return palette.stream();
  }

  public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler ignored) {
    return palette.get(engine.biome(x * 4., z * 4.));
  }
}
