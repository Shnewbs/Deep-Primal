package com.shnewbs.deepprimal.validation;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.shnewbs.deepprimal.core.WaterwayShape;
import com.shnewbs.deepprimal.worldgen.ColumnSurface;
import com.shnewbs.deepprimal.worldgen.WaterwayHeight;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Bounded native worldgen fixtures. Development source set; never shipped. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class WaterwayServerChecks {
  private record Point(int x, int z, double height) {}

  public WaterwayServerChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void commands(RegisterCommandsEvent event) {
    event
        .getDispatcher()
        .register(
            Commands.literal("waterways")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(
                    Commands.literal("map")
                        .executes(
                            c -> {
                              try {
                                map(c.getSource().getLevel());
                              } catch (java.io.IOException e) {
                                throw new java.io.UncheckedIOException(e);
                              }
                              return 1;
                            }))
                .then(
                    Commands.literal("legacy")
                        .executes(
                            c -> {
                              var level = c.getSource().getLevel();
                              boolean[] changed = {false};
                              find(level.getChunkSource().randomState())
                                  .mapAll(
                                      f -> {
                                        if (f instanceof WaterwayHeight) changed[0] = true;
                                        return f;
                                      });
                              if (changed[0])
                                throw new AssertionError("Saved legacy world changed generator");
                              var chunk =
                                  level
                                      .getChunkSource()
                                      .getChunk(
                                          com.shnewbs.deepprimal.WorldgenConfig.RIVERS_AND_COASTS
                                                  .get()
                                              ? 12001
                                              : 12000,
                                          12000,
                                          ChunkStatus.SURFACE,
                                          true);
                              var nativeNoise =
                                  chunk.getOrCreateNoiseChunk(
                                      cn -> {
                                        throw new AssertionError("Missing legacy noise chunk");
                                      });
                              if (nativeNoise.aquifer()
                                  instanceof com.shnewbs.deepprimal.worldgen.SurfaceWaterAquifer)
                                throw new AssertionError("Legacy aquifer was replaced");
                              System.out.println(
                                  "WATERWAY_LEGACY_PASS settings="
                                      + ((NoiseBasedChunkGenerator)
                                              level.getChunkSource().getGenerator())
                                          .generatorSettings()
                                          .unwrapKey()
                                          .orElseThrow()
                                          .identifier());
                              return 1;
                            }))
                .then(
                    Commands.literal("verify")
                        .then(
                            Commands.argument("kind", StringArgumentType.word())
                                .then(
                                    Commands.argument("x", IntegerArgumentType.integer())
                                        .then(
                                            Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(
                                                    c -> {
                                                      try {
                                                        verify(
                                                            c.getSource().getLevel(),
                                                            StringArgumentType.getString(c, "kind"),
                                                            IntegerArgumentType.getInteger(c, "x"),
                                                            IntegerArgumentType.getInteger(c, "z"));
                                                      } catch (java.io.IOException e) {
                                                        throw new java.io.UncheckedIOException(e);
                                                      }
                                                      return 1;
                                                    }))))));
  }

  private static DensityFunction find(RandomState state) {
    ColumnSurface[] found = new ColumnSurface[1];
    state
        .router()
        .preliminarySurfaceLevel()
        .mapAll(
            f -> {
              if (f instanceof ColumnSurface column) found[0] = column;
              return f;
            });
    if (found[0] == null) throw new AssertionError("No horizon/surface height source");
    return found[0].height();
  }

  private static void map(ServerLevel level) throws java.io.IOException {
    var state = level.getChunkSource().randomState();
    var source = find(state);
    WaterwayHeight[] adapter = new WaterwayHeight[1];
    source.mapAll(
        f -> {
          if (f instanceof WaterwayHeight h) adapter[0] = h;
          return f;
        });
    if (adapter[0] == null) throw new AssertionError("New-world waterways profile not selected");
    var shaped = adapter[0];
    var generator = level.getChunkSource().getGenerator();
    var legacyKey =
        ResourceKey.create(
            Registries.NOISE_SETTINGS,
            Identifier.fromNamespaceAndPath(
                "deepprimal", level.getMinY() == -384 ? "prototype_deep" : "prototype"));
    var legacy = find(RandomState.create(level.registryAccess(), legacyKey, level.getSeed()));
    var targets = new LinkedHashMap<String, Point>();
    Path dir =
        Path.of(
            "build/waterways-validation",
            Long.toString(level.getSeed()),
            Integer.toString(level.getMinY()));
    Files.createDirectories(dir);
    int wet = 0, riverPoints = 0, streamPoints = 0, beaches = 0;
    long start = System.nanoTime();
    try (var csv = Files.newBufferedWriter(dir.resolve("height-map.csv"))) {
      csv.write(
          "x,z,height,legacy,continents,ridges,erosion,branch,river_height,river_weight,stream_weight\n");
      for (int z = -4096; z <= 4096; z += 32)
        for (int x = -4096; x <= 4096; x += 32) {
          var p = new DensityFunction.SinglePointContext(x, 0, z);
          double h = source.compute(p), old = legacy.compute(p);
          double c = state.router().continents().compute(p), r = state.router().ridges().compute(p);
          double e = state.router().erosion().compute(p), branch = shaped.tributaries().compute(p);
          double bed = 89 + 1.5 * Math.clamp(shaped.detail().compute(p), -1.0, 1.0);
          double coast = WaterwayShape.coastHeight(old, c, e, 96);
          double main = WaterwayShape.riverHeight(coast, c, r, bed);
          double rw = WaterwayShape.riverWeight(c, r),
              sw = WaterwayShape.streamWeight(c, r, branch);
          if (Math.abs(h - state.router().depth().compute(p) * 80.0) > 1e-7)
            throw new AssertionError("Biome depth and horizon use different heights");
          if (h < 96) wet++;
          if (c > -0.05 && c < 0.18 && old > 115 && Math.abs(r) < 0.025 && h < 92) {
            riverPoints++;
            targets.putIfAbsent("river", new Point(x, z, h));
          }
          if (c > -0.05 && c < 0.18 && main > 99 && h < 92 && sw > 0.9) {
            streamPoints++;
            if (!targets.containsKey("stream")) {
              double bank =
                  Math.max(
                      Math.max(
                          source.compute(new DensityFunction.SinglePointContext(x - 12, 0, z)),
                          source.compute(new DensityFunction.SinglePointContext(x + 12, 0, z))),
                      Math.max(
                          source.compute(new DensityFunction.SinglePointContext(x, 0, z - 12)),
                          source.compute(new DensityFunction.SinglePointContext(x, 0, z + 12))));
              if (bank > 99) targets.put("stream", new Point(x, z, h));
            }
          }
          if (c > -0.20 && c < -0.08 && h >= 97 && h <= 100 && e > 0.25 && Math.abs(r) > 0.3) {
            var biome =
                generator
                    .getBiomeSource()
                    .getNoiseBiome(
                        QuartPos.fromBlock(x),
                        QuartPos.fromBlock(99),
                        QuartPos.fromBlock(z),
                        state.sampler());
            if (biome.is(Biomes.BEACH) || biome.is(Biomes.SNOWY_BEACH)) {
              beaches++;
              targets.putIfAbsent("beach", new Point(x, z, h));
            }
          }
          if (c > -0.2 && c < -0.05 && e < -0.5 && Math.abs(r) > 0.4) {
            if (Math.abs(h - old) > 1e-8) throw new AssertionError("Rocky coast was flattened");
            targets.putIfAbsent("rough-coast", new Point(x, z, h));
          }
          if (c > -0.19 && c < -0.13 && Math.abs(r) < 0.025 && h < 92)
            targets.putIfAbsent("mouth", new Point(x, z, h));
          csv.write(
              String.format(
                  Locale.ROOT,
                  "%d,%d,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f%n",
                  x,
                  z,
                  h,
                  old,
                  c,
                  r,
                  e,
                  branch,
                  main,
                  rw,
                  sw));
        }
    }
    for (String needed : new String[] {"river", "stream", "beach", "mouth"})
      if (!targets.containsKey(needed)) throw new AssertionError("No target found: " + needed);
    try (var csv = Files.newBufferedWriter(dir.resolve("targets.csv"))) {
      csv.write("kind,x,z,height\n");
      for (var entry : targets.entrySet()) {
        var p = entry.getValue();
        csv.write(entry.getKey() + "," + p.x + "," + p.z + "," + p.height + "\n");
      }
    }
    System.out.println(
        "WATERWAY_MAP_PASS seed="
            + level.getSeed()
            + " minY="
            + level.getMinY()
            + " points=66049 wet="
            + wet
            + " river="
            + riverPoints
            + " stream="
            + streamPoints
            + " beach="
            + beaches
            + " milliseconds="
            + (System.nanoTime() - start) / 1_000_000
            + " output="
            + dir.toAbsolutePath());
  }

  private static void verify(ServerLevel level, String kind, int centerX, int centerZ)
      throws java.io.IOException {
    var centerChunk =
        level.getChunkSource().getChunk(centerX >> 4, centerZ >> 4, ChunkStatus.SURFACE, true);
    var noiseChunk =
        centerChunk.getOrCreateNoiseChunk(
            c -> {
              throw new AssertionError("Missing native noise chunk");
            });
    if (!(noiseChunk.aquifer() instanceof com.shnewbs.deepprimal.worldgen.SurfaceWaterAquifer))
      throw new AssertionError("Versioned surface-water adapter is not active");
    int[] calls = {0};
    var delegate =
        new net.minecraft.world.level.levelgen.Aquifer() {
          public net.minecraft.world.level.block.state.BlockState computeSubstance(
              DensityFunction.FunctionContext c, double density) {
            calls[0]++;
            return Blocks.AIR.defaultBlockState();
          }

          public boolean shouldScheduleFluidUpdate() {
            return true;
          }
        };
    var scoped = new com.shnewbs.deepprimal.worldgen.SurfaceWaterAquifer(delegate, noiseChunk, 96);
    scoped.computeSubstance(new DensityFunction.SinglePointContext(centerX, 79, centerZ), -1);
    scoped.computeSubstance(new DensityFunction.SinglePointContext(centerX, 95, centerZ), 1);
    scoped.computeSubstance(new DensityFunction.SinglePointContext(centerX, 96, centerZ), -1);
    if (calls[0] != 3 || !scoped.shouldScheduleFluidUpdate())
      throw new AssertionError("Deep/solid/above-sea aquifer was overridden");
    if (noiseChunk.preliminarySurfaceLevel(centerX, centerZ) <= 95) {
      if (!scoped
              .computeSubstance(new DensityFunction.SinglePointContext(centerX, 95, centerZ), -1)
              .is(Blocks.WATER)
          || calls[0] != 3
          || scoped.shouldScheduleFluidUpdate())
        throw new AssertionError("Surface fill did not remain scoped");
    }
    int water = 0, gravel = 0, sand = 0, sandstone = 0, drySand = 0, submerged = 0;
    var pos = new BlockPos.MutableBlockPos();
    Path dir =
        Path.of(
            "build/waterways-validation",
            Long.toString(level.getSeed()),
            Integer.toString(level.getMinY()));
    try (var csv = Files.newBufferedWriter(dir.resolve(kind + "-blocks.csv"))) {
      csv.write("x,z,floor,material,water\n");
      for (int z = centerZ - 12; z <= centerZ + 12; z++)
        for (int x = centerX - 12; x <= centerX + 12; x++) {
          var chunk = level.getChunkSource().getChunk(x >> 4, z >> 4, ChunkStatus.SURFACE, true);
          if (chunk == null) throw new AssertionError("No native chunk");
          int floor = -1, columnWater = 0;
          String material = "none";
          for (int y = 108; y >= 72; y--) {
            var block = chunk.getBlockState(pos.set(x, y, z));
            if (block.is(Blocks.WATER)) {
              water++;
              columnWater++;
            }
            if (block.is(Blocks.GRAVEL)) gravel++;
            if (block.is(Blocks.SAND)) {
              sand++;
              if (y >= 96) drySand++;
            }
            if (block.is(Blocks.SANDSTONE)) sandstone++;
            if (floor < 0 && !block.isAir() && block.getFluidState().isEmpty()) {
              floor = y;
              material =
                  net.minecraft.core.registries.BuiltInRegistries.BLOCK
                      .getKey(block.getBlock())
                      .toString();
            }
          }
          if (columnWater > 0) submerged++;
          csv.write(x + "," + z + "," + floor + "," + material + "," + columnWater + "\n");
        }
    }
    if (kind.equals("beach")) {
      if (drySand < 10 || sandstone < 10)
        throw new AssertionError(
            "No layered dry coastal beach: sand=" + drySand + " sandstone=" + sandstone);
    } else if (kind.equals("river") || kind.equals("stream") || kind.equals("mouth")) {
      if (water < 20 || submerged < 5) throw new AssertionError("Channel has no actual water");
      if (!kind.equals("mouth") && gravel < 10) throw new AssertionError("No gravel channel bed");
    }
    System.out.println(
        "WATERWAY_BLOCKS_PASS kind="
            + kind
            + " x="
            + centerX
            + " z="
            + centerZ
            + " water="
            + water
            + " wetColumns="
            + submerged
            + " gravel="
            + gravel
            + " sand="
            + sand
            + " sandstone="
            + sandstone
            + " drySand="
            + drySand);
  }
}
