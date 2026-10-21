package com.shnewbs.deepprimal.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.biome.Biome;

/**
 * Resource-pack biome water palettes; immutable reload snapshots and a world-scoped lookup cache.
 */
public final class WaterPalettes extends SimpleJsonResourceReloadListener<WaterPalettes.Palette> {
  public record Palette(Optional<Integer> color, Optional<Integer> fogColor, double clarity) {
    static final Codec<Palette> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.intRange(0, 0xffffff)
                            .optionalFieldOf("color")
                            .forGetter(Palette::color),
                        Codec.intRange(0, 0xffffff)
                            .optionalFieldOf("fog_color")
                            .forGetter(Palette::fogColor),
                        Codec.doubleRange(.1, 8)
                            .optionalFieldOf("clarity", 1.0)
                            .forGetter(Palette::clarity))
                    .apply(instance, Palette::new));
  }

  private static final Palette EMPTY = new Palette(Optional.empty(), Optional.empty(), 1);
  public static final WaterPalettes INSTANCE = new WaterPalettes();
  private volatile Map<Identifier, Palette> values = Map.of();
  private final Map<Biome, Palette> resolved = new ConcurrentHashMap<>();

  private WaterPalettes() {
    super(Palette.CODEC, FileToIdConverter.json("deepprimal/water_palette"));
  }

  public static void register(
      net.neoforged.neoforge.client.event.AddClientReloadListenersEvent event) {
    event.addListener(Identifier.fromNamespaceAndPath("deepprimal", "water_palettes"), INSTANCE);
  }

  @Override
  protected void apply(
      Map<Identifier, Palette> values, ResourceManager manager, ProfilerFiller profiler) {
    this.values = Map.copyOf(values);
    clear();
    var mc = Minecraft.getInstance();
    if (mc.level != null) {
      mc.level.clearTintCaches();
      mc.levelRenderer.allChanged();
    }
  }

  public static void clear() {
    INSTANCE.resolved.clear();
  }

  public static Palette get(Biome biome) {
    return INSTANCE.resolved.computeIfAbsent(
        biome,
        b -> {
          var level = Minecraft.getInstance().level;
          if (level == null) return EMPTY;
          var id = level.registryAccess().lookupOrThrow(Registries.BIOME).getKey(b);
          return INSTANCE.values.getOrDefault(id, EMPTY);
        });
  }
}
