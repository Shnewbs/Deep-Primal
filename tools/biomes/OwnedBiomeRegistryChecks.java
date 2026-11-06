package com.shnewbs.deepprimal.validation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.shnewbs.deepprimal.core.TerrainEngine;
import com.shnewbs.deepprimal.engine.DeepPrimalBiomeTags;
import com.shnewbs.deepprimal.engine.RegionBiomeSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;

/** Actual 26.1.2 codecs/registries and authored biome source; no loader or client API doubles. */
public final class OwnedBiomeRegistryChecks {
  private static int checks;

  private static void require(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }

  private static JsonObject json(Path path) throws Exception {
    return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
  }

  private static ResourceKey<Biome> key(String id) {
    return ResourceKey.create(Registries.BIOME, Identifier.parse(id));
  }

  public static void main(String[] args) throws Exception {
    Path root = Path.of(args[0]);
    Path resources = root.resolve("src/main/resources");
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
    var vanilla = VanillaRegistries.createLookup();
    var ops = RegistryOps.create(JsonOps.INSTANCE, vanilla);
    var biomes = new MappedRegistry<Biome>(Registries.BIOME, Lifecycle.stable());
    vanilla.lookupOrThrow(Registries.BIOME).listElements()
        .forEach(h -> biomes.register(h.key(), h.value(), RegistrationInfo.BUILT_IN));
    var entries = json(root.resolve("tools/biomes/owned_palette.json")).getAsJsonArray("entries");
    var ids = new ArrayList<String>();
    for (var element : entries) {
      var entry = element.getAsJsonObject();
      String id = entry.get("id").getAsString();
      ids.add(id);
      String name = id.substring("deepprimal:".length());
      var definition = json(resources.resolve("data/deepprimal/worldgen/biome/" + name + ".json"));
      var biome = Biome.DIRECT_CODEC.parse(ops, definition).getOrThrow();
      require(Math.abs(biome.getBaseTemperature() - entry.get("temperature").getAsFloat()) < .0001f,
          "Authored temperature " + id);
      require((biome.getWaterColor() & 0xffffff) == entry.getAsJsonObject("water_palette").get("color").getAsInt(),
          "Authored water color " + id);
      for (var category : MobCategory.values())
        require(biome.getMobSettings().getMobs(category).isEmpty(), "No inherited mob table " + id);
      require(biome.getGenerationSettings().features().stream().allMatch(set -> set.size() == 0),
          "Engine owns decoration " + id);
      var encoded = Biome.DIRECT_CODEC.encodeStart(ops, biome).getOrThrow();
      require(Biome.DIRECT_CODEC.parse(ops, encoded).getOrThrow().getWaterColor() == biome.getWaterColor(),
          "Biome codec round trip " + id);
      biomes.register(key(id), biome, RegistrationInfo.BUILT_IN);
    }
    require(ids.size() == 40 && new HashSet<>(ids).size() == 40, "Complete owned palette");
    var tags = new HashMap<TagKey<Biome>, List<Holder<Biome>>>();
    try (var paths = Files.list(resources.resolve("data/deepprimal/tags/worldgen/biome"))) {
      for (var path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
        var values = json(path).getAsJsonArray("values");
        var members = new ArrayList<Holder<Biome>>();
        for (var value : values) {
          String id = value.getAsString();
          require(ids.contains(id), "Tag resolves only to owned biome: " + id);
          members.add(biomes.getOrThrow(key(id)));
        }
        String name = path.getFileName().toString().replace(".json", "");
        tags.put(TagKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath("deepprimal", name)), members);
      }
    }
    require(tags.size() == 14, "All owned habitat tags registered");
    // Native tags are deliberately NOT populated with our biomes. Exercise the runtime bridge.
    tags.put(BiomeTags.IS_OCEAN, List.of(biomes.getOrThrow(key("minecraft:ocean")),
        biomes.getOrThrow(key("deepprimal:mixed_grass_prairie"))));
    tags.put(BiomeTags.IS_RIVER, List.of(biomes.getOrThrow(key("minecraft:river")),
        biomes.getOrThrow(key("deepprimal:mixed_grass_prairie"))));
    biomes.bindTags(tags);
    biomes.freeze();
    require(DeepPrimalBiomeTags.isOcean(biomes.getOrThrow(key("deepprimal:open_ocean"))),
        "Owned ocean identity works without native tag");
    require(DeepPrimalBiomeTags.isRiver(biomes.getOrThrow(key("deepprimal:lowland_river"))),
        "Owned river identity works without native tag");
    require(!DeepPrimalBiomeTags.isOcean(biomes.getOrThrow(key("deepprimal:mixed_grass_prairie")))
            && !DeepPrimalBiomeTags.isRiver(biomes.getOrThrow(key("deepprimal:mixed_grass_prairie"))),
        "Native tags cannot override owned habitat identity");
    require(DeepPrimalBiomeTags.isOcean(biomes.getOrThrow(key("minecraft:ocean")))
            && DeepPrimalBiomeTags.isRiver(biomes.getOrThrow(key("minecraft:river"))),
        "Older native-biome saves retain water compatibility");
    var lookup = new RegistryAccess.ImmutableRegistryAccess(List.of(biomes)).freeze();
    var paletteOps = RegistryOps.create(JsonOps.INSTANCE, lookup);
    for (String profile : List.of("engine_standard", "engine_deep", "engine_deltas_standard", "engine_deltas_deep")) {
      var preset = json(resources.resolve("worldgen/" + profile + "/data/minecraft/worldgen/world_preset/normal.json"));
      var sourceJson = preset.getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld")
          .getAsJsonObject("generator").getAsJsonObject("biome_source");
      var source = RegionBiomeSource.CODEC.codec().parse(paletteOps, sourceJson).getOrThrow();
      require(source.possibleBiomes().size() == 40, "All owned possible biomes " + profile);
      for (long seed : new long[] {0L, 1L, -17L}) {
        source.bind(new TerrainEngine(seed));
        for (int z = -512; z <= 512; z += 64)
          for (int x = -512; x <= 512; x += 64)
            require(source.getNoiseBiome(x, 24, z, null).unwrapKey().orElseThrow()
                    .identifier().getNamespace().equals("deepprimal"),
                "Owned biome selected in " + profile);
      }
      var encoded = RegionBiomeSource.CODEC.codec().encodeStart(paletteOps, source).getOrThrow();
      var reopened = RegionBiomeSource.CODEC.codec().parse(paletteOps, encoded).getOrThrow();
      require(reopened.possibleBiomes().size() == 40, "Saved palette round trip " + profile);
      sourceJson = sourceJson.deepCopy();
      var legacyPalette = new com.google.gson.JsonArray();
      for (var entry : entries) legacyPalette.add(entry.getAsJsonObject().get("legacy_id").getAsString());
      sourceJson.add("palette", legacyPalette);
      require(RegionBiomeSource.CODEC.codec().parse(paletteOps, sourceJson).getOrThrow()
              .possibleBiomes().size() == 40,
          "Legacy 40-slot saved palette readable " + profile);
      var legacyEight = new com.google.gson.JsonArray();
      for (int i = 0; i < 8; i++) legacyEight.add(entries.get(i).getAsJsonObject().get("legacy_id").getAsString());
      sourceJson.add("palette", legacyEight);
      require(RegionBiomeSource.CODEC.codec().parse(paletteOps, sourceJson).getOrThrow()
              .possibleBiomes().size() == 8,
          "Legacy 8-slot saved palette readable " + profile);
    }
    var broken = new JsonObject();
    var missing = new com.google.gson.JsonArray();
    for (int i = 0; i < 40; i++) missing.add("deepprimal:missing_biome");
    broken.add("palette", missing);
    boolean rejected;
    try {
      rejected = RegionBiomeSource.CODEC.codec().parse(paletteOps, broken).error().isPresent();
    } catch (IllegalArgumentException expected) {
      // The list codec can expose an empty partial result to the constructor on invalid IDs.
      rejected = "Engine biome palette".equals(expected.getMessage());
    }
    require(rejected, "Missing biome references rejected by native codec");
    System.out.println("OWNED_BIOME_NATIVE_PASS biomes=40 tags=14 profiles=4 checks=" + checks);
  }
}
