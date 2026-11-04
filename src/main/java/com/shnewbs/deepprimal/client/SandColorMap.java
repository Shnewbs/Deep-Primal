package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.core.SoilTexture;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Climate tint for sand, sampled from the packaged temperature/downfall colormap. */
public final class SandColorMap implements BlockTintSource {
  static final SandColorMap INSTANCE = new SandColorMap();
  private static final int SIZE = 256;
  private static final int[] COLORS = load();

  private static int[] load() {
    String resource = "/assets/deepprimal/textures/colormap/sand.png";
    try (InputStream stream = SandColorMap.class.getResourceAsStream(resource)) {
      if (stream == null) throw new IllegalStateException("Missing sand colormap: " + resource);
      BufferedImage image = ImageIO.read(stream);
      if (image == null || image.getWidth() != SIZE || image.getHeight() != SIZE)
        throw new IllegalStateException("Sand colormap must be 256x256: " + resource);
      int[] colors = image.getRGB(0, 0, SIZE, SIZE, null, 0, SIZE);
      for (int color : colors)
        if ((color >>> 24) != 255)
          throw new IllegalStateException("Sand colormap must be fully opaque: " + resource);
      return colors;
    } catch (IOException failure) {
      throw new IllegalStateException("Unable to load sand colormap: " + resource, failure);
    }
  }

  public static int sample(double temperature, double downfall) {
    if (!Double.isFinite(temperature) || !Double.isFinite(downfall))
      return SoilTexture.SAND.color();
    double t = Math.clamp(temperature, 0, 1);
    double d = Math.clamp(downfall, 0, 1);
    double x = (1 - t) * (SIZE - 1);
    double y = (1 - t * d) * (SIZE - 1);
    int x0 = (int) x, y0 = (int) y;
    int x1 = Math.min(x0 + 1, SIZE - 1), y1 = Math.min(y0 + 1, SIZE - 1);
    double u = x - x0, v = y - y0;
    int a = COLORS[y0 * SIZE + x0], b = COLORS[y0 * SIZE + x1];
    int c = COLORS[y1 * SIZE + x0], e = COLORS[y1 * SIZE + x1];
    int r = channel(a, b, c, e, 16, u, v);
    int g = channel(a, b, c, e, 8, u, v);
    int bl = channel(a, b, c, e, 0, u, v);
    return (r << 16) | (g << 8) | bl;
  }

  private static int channel(int a, int b, int c, int d, int shift, double u, double v) {
    double top = ((a >>> shift) & 255) * (1 - u) + ((b >>> shift) & 255) * u;
    double bottom = ((c >>> shift) & 255) * (1 - u) + ((d >>> shift) & 255) * u;
    return (int) Math.round(top * (1 - v) + bottom * v);
  }

  @Override
  public int color(BlockState state) {
    return SoilTexture.SAND.color();
  }

  @Override
  public Set<net.minecraft.world.level.block.state.properties.Property<?>> relevantProperties() {
    return Set.of();
  }

  @Override
  public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
    return SoilTexture.SAND.color();
  }

  @Override
  public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
    if (level == null || pos == null) return SoilTexture.SAND.color();
    var clientLevel = Minecraft.getInstance().level;
    if (clientLevel == null) return SoilTexture.SAND.color();
    var climate = clientLevel.getBiome(pos).value().getModifiedClimateSettings();
    return sample(climate.temperature(), climate.downfall());
  }

  private SandColorMap() {}
}
