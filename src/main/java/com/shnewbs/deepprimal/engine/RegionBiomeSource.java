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
    if (palette.size() != 8 && palette.size() != 40) throw new IllegalArgumentException("Engine biome palette");
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
    int id=palette.size()==40?engine.biome(x*4.,y*4.,z*4.):engine.biome(x*4.,z*4.);
    if(id>=palette.size())id=id>=29&&id<=35?3:id==36||id==37?0:id==38||id==39?7:id==19||id==18||id==16?5:1;
    return palette.get(id);
  }
}
