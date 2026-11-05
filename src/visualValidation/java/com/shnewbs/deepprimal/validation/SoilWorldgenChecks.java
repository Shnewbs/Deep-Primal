package com.shnewbs.deepprimal.validation;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import com.shnewbs.deepprimal.engine.GroundMaterials;
import java.nio.file.Files;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;

final class SoilWorldgenChecks {
  static void run(ServerLevel level, boolean reopen) throws java.io.IOException {
    String expectedWorldRevision = System.getenv("DEEPPRIMAL_SOIL_WORLD_REVISION");
    int actualWorldRevision = ((EngineChunkGenerator) level.getChunkSource().getGenerator()).materialRevision();
    if (expectedWorldRevision != null)
      require(actualWorldRevision == Integer.parseInt(expectedWorldRevision),
          "Actual saved-world generator material revision");
    var ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    JsonObject template;
    try (var in = SoilWorldgenChecks.class.getResourceAsStream(
        "/worldgen/engine_standard/data/minecraft/worldgen/world_preset/normal.json")) {
      if (in == null) throw new AssertionError("Missing new-world material preset");
      template = com.google.gson.JsonParser.parseString(new String(in.readAllBytes(),
          java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("dimensions")
          .getAsJsonObject("minecraft:overworld").getAsJsonObject("generator");
    }
    require(template.get("material_revision").getAsInt() == 6,
        "New worlds select the current V7 soil and shoreline material revision");
    require(template.get("channel_revision").getAsInt() == 8,
        "New worlds select integrated river features and deep water-body basins");
    var saved = level.getServer().getWorldPath(LevelResource.ROOT).resolve("soil-material-validation.json");
    if (reopen) template = com.google.gson.JsonParser.parseString(Files.readString(saved)).getAsJsonObject();
    var current = (EngineChunkGenerator) ChunkGenerator.CODEC.parse(ops, template).getOrThrow();
    require(current.materialRevision() == 6, "Saved material revision");
    require(current.channelRevision() == 8, "Saved channel revision");
    var encoded = ChunkGenerator.CODEC.encodeStart(ops, current).getOrThrow().getAsJsonObject();
    var roundtrip = (EngineChunkGenerator) ChunkGenerator.CODEC.parse(ops, encoded).getOrThrow();
    require(roundtrip.materialRevision() == 6 && roundtrip.channelRevision() == 8,
        "Codec material and channel revision roundtrip");
    if (!reopen) Files.writeString(saved, encoded.toString());
    var legacyJson = encoded.deepCopy();
    legacyJson.remove("material_revision");
    var legacy = (EngineChunkGenerator) ChunkGenerator.CODEC.parse(ops, legacyJson).getOrThrow();
    require(legacy.materialRevision() == 0, "Old saves with missing field retain original materials");
    var explicitLegacy = legacyJson.deepCopy();
    explicitLegacy.addProperty("material_revision", 0);
    var control = (EngineChunkGenerator) ChunkGenerator.CODEC.parse(ops, explicitLegacy).getOrThrow();
    for (int invalid : new int[] {-1, 7}) {
      var bad = encoded.deepCopy();
      bad.addProperty("material_revision", invalid);
      require(ChunkGenerator.CODEC.parse(ops, bad).result().isEmpty(), "Invalid material revision rejected");
    }
    for (int invalid : new int[] {-1, 9}) {
      var bad = encoded.deepCopy();
      bad.addProperty("channel_revision", invalid);
      require(ChunkGenerator.CODEC.parse(ops, bad).result().isEmpty(), "Invalid channel revision rejected");
    }
    long blocks = 0, changed = 0, decorations = 0, outcrops = 0;
    var cursor = new BlockPos.MutableBlockPos();
    for (long seed : new long[] {20261020, Long.MIN_VALUE}) {
      for (var generator : List.of(current, legacy, control))
        generator.createState(level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET),
            level.getChunkSource().randomState(), seed);
      var sites = List.of(new int[] {-1, -1}, new int[] {0, -1}, new int[] {0, 0}, new int[] {4, 8});
      var fingerprints = new java.util.ArrayList<String>();
      for (var site : sites) {
        var candidate = DepositNativeChecks.chunk(level, current, site);
        var old = DepositNativeChecks.chunk(level, legacy, site);
        var original = DepositNativeChecks.chunk(level, control, site);
        for (int z = 0; z < 16; z++)
          for (int x = 0; x < 16; x++) {
            int wx = candidate.getPos().getMinBlockX() + x, wz = candidate.getPos().getMinBlockZ() + z;
            var column = current.getBaseColumn(wx, wz, level, level.getChunkSource().randomState());
            boolean decorationShift = false;
            for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
              cursor.set(wx, y, wz);
              var next = candidate.getBlockState(cursor);
              var previous = old.getBlockState(cursor);
              require(previous.equals(original.getBlockState(cursor)), "Missing-field legacy compatibility");
              boolean decoration = !next.equals(column.getBlock(y));
              if (decoration) {
                require((next.equals(previous) || current.materialRevision() >= 2) && !(next.getBlock() instanceof GroundMaterials.SoilBlock),
                    "Only unchanged decorations may differ from base columns at " + cursor);
                decorations++;
                if (!next.equals(previous)) decorationShift = true;
              }
              require(!next.is(Blocks.DIRT) && !next.is(Blocks.GRASS_BLOCK) && !next.is(Blocks.PODZOL)
                  && !next.is(Blocks.MUD) && !next.is(Blocks.CLAY) && !next.is(Blocks.SAND)
                  && !next.is(Blocks.RED_SAND), "No vanilla soil output in new material revision");
              if (!next.equals(previous) && !decoration) {
                if (!(next.getBlock() instanceof GroundMaterials.SoilBlock)) {
                  require(current.materialRevision() >= 2 && previous.isSolidRender()
                      && (LEGACY_HORIZONS.contains(previous.getBlock()) || LEGACY_HORIZONS.contains(next.getBlock()))
                      && next.equals(column.getBlock(y)),
                      "Only material blocks or revision-2 horizon/outcrop shifts may change at " + cursor
                          + " " + previous + " -> " + next);
                  outcrops++;
                }
                require(previous.isSolidRender() && next.isSolidRender(), "Material replacement preserves occupancy");
                changed++;
              }
              blocks++;
            }
            for (var heightmap : List.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG))
              require(decorationShift || candidate.getHeight(heightmap, x, z) == old.getHeight(heightmap, x, z),
                  "Material revision preserves heightmaps");
          }
        fingerprints.add(digest(candidate));
      }
      for (int i = sites.size() - 1; i >= 0; i--)
        require(digest(DepositNativeChecks.chunk(level, current, sites.get(i))).equals(fingerprints.get(i)),
            "Soil placement independent of chunk order");
    }
    require(changed > 0, "Actual soil placement witness");
    System.out.println("SOIL_WORLDGEN_PASS blocks=" + blocks + " changed=" + changed
        + " unchangedDecorations=" + decorations
        + " seeds=2 sites=4 legacyDefaults=true occupancyPreserved=true materialBaseColumns=true heightmaps=true"
        + " reverseOrder=true outcrops=" + outcrops + " savedMaterialRevision=2 actualWorldMaterialRevision=" + actualWorldRevision
        + " reopen=" + reopen);
  }

  private static final java.util.Set<net.minecraft.world.level.block.Block> LEGACY_HORIZONS = java.util.Set.of(
      Blocks.GRAVEL, Blocks.SANDSTONE, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.CLAY, Blocks.MUD,
      Blocks.SAND, Blocks.RED_SAND, Blocks.SNOW_BLOCK, Blocks.TERRACOTTA, Blocks.ORANGE_TERRACOTTA,
      Blocks.YELLOW_TERRACOTTA);

  private static String digest(net.minecraft.world.level.chunk.ProtoChunk chunk) {
    try {
      return DepositNativeChecks.digest(chunk);
    } catch (Exception failure) {
      throw new IllegalStateException("Soil fingerprint failed", failure);
    }
  }

  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  private SoilWorldgenChecks() {}
}
