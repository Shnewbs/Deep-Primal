package com.shnewbs.deepprimal.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Transparency;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnewbs.deepprimal.DeepPrimal;
import com.shnewbs.deepprimal.settings.RendererOwnership;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;

/** One opaque material sample: premultiplied soil RGB plus surface luminance in alpha. */
public record GroundSideSpriteSource(
    Identifier sprite, Identifier texture, Identifier surfaceMask, Identifier soilMask, int soilColor)
    implements SpriteSource {
  public static final MapCodec<GroundSideSpriteSource> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      Identifier.CODEC.fieldOf("sprite").forGetter(GroundSideSpriteSource::sprite),
      Identifier.CODEC.fieldOf("texture").forGetter(GroundSideSpriteSource::texture),
      Identifier.CODEC.fieldOf("surface_mask").forGetter(GroundSideSpriteSource::surfaceMask),
      Identifier.CODEC.fieldOf("soil_mask").forGetter(GroundSideSpriteSource::soilMask),
      com.mojang.serialization.Codec.intRange(0, 0xffffff).fieldOf("soil_color")
          .forGetter(GroundSideSpriteSource::soilColor)).apply(i, GroundSideSpriteSource::new));
  public static final int LIGHT_MARKER = 0x4000;

  public static void register(RegisterSpriteSourcesEvent event) {
    event.register(Identifier.fromNamespaceAndPath(DeepPrimal.ID, "ground_side"), CODEC);
    event.register(Identifier.fromNamespaceAndPath(DeepPrimal.ID, "soil_sides"), SoilSideSpriteSource.CODEC);
  }

  @Override
  public MapCodec<GroundSideSpriteSource> codec() {
    return CODEC;
  }

  @Override
  public void run(ResourceManager manager, Output output) {
    output.add(sprite, loader -> load(manager));
  }

  private SpriteContents load(ResourceManager manager) {
    try (var image = read(manager, texture);
        var surface = read(manager, surfaceMask);
        var soil = read(manager, soilMask)) {
      int size = image.getWidth();
      if (size < 16 || size > 256 || (size & (size - 1)) != 0 || image.getHeight() != size
          || surface.getWidth() != size || surface.getHeight() != size
          || soil.getWidth() != size || soil.getHeight() != size)
        throw new IllegalArgumentException(sprite + ": square power-of-two side/masks must match within 16..256");
      boolean owned = RendererOwnership.owned();
      if (!owned)
        DeepPrimal.LOGGER.warn("{} uses default surface color because {} owns the renderer; dynamic side tint requires Deep Primal shaders",
            sprite, RendererOwnership.owner());
      var result = new NativeImage(size, size, false);
      try {
        int fallback = texture.getPath().contains("podzol")
            ? net.minecraft.world.level.DryFoliageColor.FOLIAGE_DRY_DEFAULT
            : net.minecraft.world.level.GrassColor.getDefaultColor();
        for (int y = 0; y < size; y++)
          for (int x = 0; x < size; x++) {
            int luminance = gray(image.getPixel(x, y), texture);
            int weight = gray(surface.getPixel(x, y), surfaceMask);
            int complement = gray(soil.getPixel(x, y), soilMask);
            if (weight + complement != 255)
              throw new IllegalArgumentException(sprite + ": masks do not sum to 255 at " + x + "," + y);
            int pixel = pack(luminance, weight, soilColor);
            if (!owned) pixel = compose(pixel, fallback);
            result.setPixel(x, y, pixel);
          }
        return owned ? new MaterialContents(sprite, result)
            : new SpriteContents(sprite, new FrameSize(size, size), result);
      } catch (RuntimeException | Error failure) {
        result.close();
        throw failure;
      }
    } catch (IOException failure) {
      throw new UncheckedIOException("Unable to load ground material " + sprite, failure);
    }
  }

  private static NativeImage read(ResourceManager manager, Identifier id) throws IOException {
    try (var input = manager.getResourceOrThrow(TEXTURE_ID_CONVERTER.idToFile(id)).open()) {
      return NativeImage.read(input);
    }
  }

  private static int gray(int pixel, Identifier id) {
    int gray = pixel & 255;
    if ((pixel >>> 24) != 255 || ((pixel >> 8) & 255) != gray || ((pixel >> 16) & 255) != gray)
      throw new IllegalArgumentException(id + ": ground luminance and masks must be opaque neutral grayscale");
    return gray;
  }

  public static int pack(int luminance, int surfaceWeight, int soilColor) {
    int soilLuminance = luminance * (255 - surfaceWeight);
    int alpha = (luminance * surfaceWeight + 127) / 255;
    int r = (soilLuminance * ((soilColor >> 16) & 255) + 32512) / 65025;
    int g = (soilLuminance * ((soilColor >> 8) & 255) + 32512) / 65025;
    int b = (soilLuminance * (soilColor & 255) + 32512) / 65025;
    return alpha << 24 | r << 16 | g << 8 | b;
  }

  public static int compose(int packed, int surfaceColor) {
    int alpha = packed >>> 24;
    int r = Math.min(255, ((packed >> 16) & 255) + (alpha * ((surfaceColor >> 16) & 255) + 127) / 255);
    int g = Math.min(255, ((packed >> 8) & 255) + (alpha * ((surfaceColor >> 8) & 255) + 127) / 255);
    int b = Math.min(255, (packed & 255) + (alpha * (surfaceColor & 255) + 127) / 255);
    return 0xff000000 | r << 16 | g << 8 | b;
  }

  /** Alpha stores a coefficient, never coverage; all channels use arithmetic-mean mips. */
  public static final class MaterialContents extends SpriteContents {
    public MaterialContents(Identifier name, NativeImage image) {
      super(name, new FrameSize(image.getWidth(), image.getHeight()), image);
    }

    @Override
    public Transparency transparency() {
      return Transparency.NONE;
    }

    @Override
    public Transparency computeTransparency(float u0, float v0, float u1, float v1) {
      return Transparency.NONE;
    }

    @Override
    public boolean isTransparent(int frame, int x, int y) {
      return false;
    }

    @Override
    public void increaseMipLevel(int level) {
      if (level < byMipLevel.length) return;
      var images = Arrays.copyOf(byMipLevel, level + 1);
      try {
        for (int mip = byMipLevel.length; mip <= level; mip++) {
          var previous = images[mip - 1];
          int width = Math.max(1, previous.getWidth() / 2), height = Math.max(1, previous.getHeight() / 2);
          var next = new NativeImage(width, height, false);
          images[mip] = next;
          for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
              int x1 = Math.min(previous.getWidth() - 1, x * 2 + 1);
              int y1 = Math.min(previous.getHeight() - 1, y * 2 + 1);
              int a = previous.getPixel(x * 2, y * 2), b = previous.getPixel(x1, y * 2);
              int c = previous.getPixel(x * 2, y1), d = previous.getPixel(x1, y1);
              int pixel = 0;
              for (int shift = 0; shift < 32; shift += 8)
                pixel |= ((((a >>> shift) & 255) + ((b >>> shift) & 255)
                    + ((c >>> shift) & 255) + ((d >>> shift) & 255) + 2) / 4) << shift;
              next.setPixel(x, y, pixel);
            }
        }
        byMipLevel = images;
      } catch (RuntimeException | Error failure) {
        for (int mip = byMipLevel.length; mip < images.length; mip++)
          if (images[mip] != null) images[mip].close();
        throw failure;
      }
    }
  }
}
