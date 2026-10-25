package com.shnewbs.deepprimal.client;

import com.mojang.serialization.MapCodec;
import com.shnewbs.deepprimal.core.SoilTexture;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/** All soil families use the owner's same luminance/masks with independent mineral tints. */
public record SoilSideSpriteSource() implements SpriteSource {
  public static final MapCodec<SoilSideSpriteSource> CODEC = MapCodec.unit(new SoilSideSpriteSource());

  @Override public MapCodec<SoilSideSpriteSource> codec() { return CODEC; }

  @Override public void run(ResourceManager manager, Output output) {
    for (var soil : SoilTexture.values())
      for (String cover : new String[] {"grass", "podzol"})
        new GroundSideSpriteSource(id("soil/" + soil.id() + "_" + cover + "_side"),
            id("ground/" + cover + "_side"), id("ground/masks/" + cover + "_side_surface"),
            id("ground/masks/" + cover + "_side_soil"), soil.color()).run(manager, output);
  }

  private static Identifier id(String path) {
    return Identifier.fromNamespaceAndPath("deepprimal", "block/" + path);
  }
}
