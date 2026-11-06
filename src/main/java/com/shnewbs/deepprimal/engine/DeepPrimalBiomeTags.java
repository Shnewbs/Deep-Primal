package com.shnewbs.deepprimal.engine;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/** Deep Primal habitat identity; native tags are only a bridge for older/external worlds. */
public final class DeepPrimalBiomeTags {
  public static final TagKey<Biome> IS_OCEAN = tag("is_ocean");
  public static final TagKey<Biome> IS_RIVER = tag("is_river");

  private static TagKey<Biome> tag(String name) {
    return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("deepprimal", name));
  }

  private static boolean owned(Holder<Biome> biome) {
    return biome.unwrapKey().map(k -> k.identifier().getNamespace().equals("deepprimal")).orElse(false);
  }

  public static boolean isOcean(Holder<Biome> biome) {
    return biome.is(IS_OCEAN) || !owned(biome) && biome.is(BiomeTags.IS_OCEAN);
  }

  public static boolean isRiver(Holder<Biome> biome) {
    return biome.is(IS_RIVER) || !owned(biome) && biome.is(BiomeTags.IS_RIVER);
  }

  private DeepPrimalBiomeTags() {}
}
