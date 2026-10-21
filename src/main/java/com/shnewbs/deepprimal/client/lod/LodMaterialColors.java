package com.shnewbs.deepprimal.client.lod;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.world.level.block.state.BlockState;

/** Cache actual resource-pack texture averages; weak keys release old atlases after reload. */
final class LodMaterialColors {
  private static final Map<SpriteContents, Integer> AVERAGES = new WeakHashMap<>();

  static int texture(BlockState state) {
    var sprite =
        Minecraft.getInstance()
            .getModelManager()
            .getBlockStateModelSet()
            .getParticleMaterial(state)
            .sprite()
            .contents();
    return AVERAGES.computeIfAbsent(
        sprite,
        s -> {
          var image =
              ((com.shnewbs.deepprimal.mixin.client.SpritePixelsAccess) s)
                  .deepprimal$originalImage();
          long r = 0, g = 0, b = 0, weight = 0;
          for (int y = 0; y < s.height(); y++)
            for (int x = 0; x < s.width(); x++) {
              int c = image.getPixel(x, y), a = c >>> 24;
              if (a < 32) continue;
              r += ((c >>> 16) & 255) * a;
              g += ((c >>> 8) & 255) * a;
              b += (c & 255) * a;
              weight += a;
            }
          return weight == 0
              ? 0xff777777
              : 0xff000000
                  | ((int) (r / weight) << 16)
                  | ((int) (g / weight) << 8)
                  | (int) (b / weight);
        });
  }

  private LodMaterialColors() {}
}
