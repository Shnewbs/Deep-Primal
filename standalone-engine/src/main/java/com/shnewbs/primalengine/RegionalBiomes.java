package com.shnewbs.primalengine;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.primalengine.core.ContinentalWorld;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;

/** Biome assets are selected by our own geography/climate, never the multi-noise source. */
public final class RegionalBiomes extends BiomeSource {
  public static final MapCodec<RegionalBiomes> CODEC =
      RecordCodecBuilder.mapCodec(
          i ->
              i.group(
                      Biome.CODEC.fieldOf("plains").forGetter(b -> b.plains),
                      Biome.CODEC.fieldOf("forest").forGetter(b -> b.forest),
                      Biome.CODEC.fieldOf("dry").forGetter(b -> b.dry),
                      Biome.CODEC.fieldOf("ocean").forGetter(b -> b.ocean))
                  .apply(i, RegionalBiomes::new));
  private final Holder<Biome> plains, forest, dry, ocean;
  private volatile ContinentalWorld world = new ContinentalWorld(0);

  public RegionalBiomes(
      Holder<Biome> plains, Holder<Biome> forest, Holder<Biome> dry, Holder<Biome> ocean) {
    this.plains = plains;
    this.forest = forest;
    this.dry = dry;
    this.ocean = ocean;
  }

  void bind(ContinentalWorld world) {
    this.world = world;
  }

  protected MapCodec<? extends BiomeSource> codec() {
    return CODEC;
  }

  protected Stream<Holder<Biome>> collectPossibleBiomes() {
    return Stream.of(plains, forest, dry, ocean);
  }

  public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler ignored) {
    var s = world.sample(x * 4., z * 4.);
    return s.ocean() ? ocean : s.rain() < .4 ? dry : s.rain() > .65 ? forest : plains;
  }
}
