package com.shnewbs.deepprimal.validation;

import com.mojang.brigadier.Command;
import com.shnewbs.deepprimal.DeepPrimal;
import com.shnewbs.deepprimal.core.MountainDrainage;
import com.shnewbs.deepprimal.worldgen.SurfaceWaterAquifer;
import com.shnewbs.deepprimal.worldgen.WaterwayHeight;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Dedicated-server checks; excluded from the distributed mod. */
@Mod(value = DeepPrimal.ID, dist = Dist.DEDICATED_SERVER)
public final class DrainageServerChecks {
  private volatile boolean started;
  private static final StringBuilder JOURNAL = new StringBuilder();

  public DrainageServerChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
    started = true;
    record("DRAINAGE_SERVER_STARTED");
  }

  static synchronized void record(String message) {
    System.out.println(message);
    try {
      String destination = System.getenv("DEEPPRIMAL_VALIDATION_RECORD");
      Path file = destination == null ? Path.of("build/drainage-events.txt") : Path.of(destination);
      Files.createDirectories(file.getParent());
      JOURNAL.append(message).append('\n');
      // Each snapshot is complete, so external log collectors cannot lose an
      // earlier append while observing a live generation process.
      Path pending = file.resolveSibling(file.getFileName() + ".pending");
      Files.writeString(pending, JOURNAL);
      Files.move(
          pending,
          file,
          java.nio.file.StandardCopyOption.ATOMIC_MOVE,
          java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    } catch (java.io.IOException e) {
      throw new java.io.UncheckedIOException(e);
    }
  }

  private static final java.util.concurrent.atomic.AtomicBoolean AUTOMATIC =
      new java.util.concurrent.atomic.AtomicBoolean();

  @SubscribeEvent
  public void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
    String selected = System.getenv("DEEPPRIMAL_VALIDATION_CASE");
    if (!started || selected == null || !AUTOMATIC.compareAndSet(false, true)) return;
    try {
      var level = event.getServer().overworld();
      profile(level);
      if (selected.equals("standard") || selected.equals("deep")) {
        check(level);
        OceanServerChecks.run(level);
        RiverCurrentChecks.run(level);
        WaterEnvironmentChecks.run(level);
      }
      if (selected.equals("weather") || selected.equals("weather-ocean"))
        WaterEnvironmentChecks.run(level);
      if (selected.equals("ocean") || selected.equals("weather-ocean"))
        OceanServerChecks.run(level);
      if (selected.equals("standard-reopen") || selected.equals("deep-reopen")) {
        RiverCurrentChecks.reopen(level);
        HydrologyServerChecks.reopen(level);
      }
      record("DRAINAGE_AUTO_PASS case=" + selected);
    } catch (java.io.IOException | RuntimeException | Error e) {
      record("DRAINAGE_FAILURE " + e);
    } finally {
      event.getServer().halt(false);
    }
  }

  private static void profile(ServerLevel level) {
    var generator =
        (net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator)
            level.getChunkSource().getGenerator();
    var state = level.getChunkSource().randomState();
    var values = new StringBuilder();
    for (int x : new int[] {-4096, -513, -1, 0, 1024, 8192}) {
      values
          .append(
              Double.toHexString(
                  state
                      .router()
                      .preliminarySurfaceLevel()
                      .compute(
                          new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(
                              x, 0, -x))))
          .append(',');
    }
    record(
        "DRAINAGE_PROFILE settings="
            + generator.generatorSettings().unwrapKey().orElseThrow().identifier()
            + " minY="
            + level.getMinY()
            + " values="
            + values);
  }

  @SubscribeEvent
  public void commands(RegisterCommandsEvent event) {
    event
        .getDispatcher()
        .register(
            Commands.literal("drainage")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(
                    Commands.literal("profile")
                        .executes(
                            c -> {
                              profile(c.getSource().getLevel());
                              return Command.SINGLE_SUCCESS;
                            }))
                .then(
                    Commands.literal("check")
                        .executes(
                            c -> {
                              try {
                                check(c.getSource().getLevel());
                              } catch (java.io.IOException e) {
                                record("DRAINAGE_FAILURE " + e);
                                throw new java.io.UncheckedIOException(e);
                              } catch (RuntimeException | Error e) {
                                record("DRAINAGE_FAILURE " + e);
                                throw e;
                              }
                              return Command.SINGLE_SUCCESS;
                            })));
  }

  private static WaterwayHeight field(ServerLevel level) {
    WaterwayHeight[] found = {null};
    level
        .getChunkSource()
        .randomState()
        .router()
        .preliminarySurfaceLevel()
        .mapAll(
            f -> {
              if (f instanceof WaterwayHeight h && h.routed()) found[0] = h;
              return f;
            });
    if (found[0] == null || found[0].drainage() == null)
      throw new AssertionError("Routed profile is not seeded");
    return found[0];
  }

  private static void check(ServerLevel level) throws java.io.IOException {
    var h = field(level);
    var network = h.drainage();
    level
        .getChunkSource()
        .randomState()
        .router()
        .depth()
        .mapAll(
            f -> {
              if (f instanceof WaterwayHeight w && w.routed() && w.drainage() != network)
                throw new AssertionError("Terrain and water created different catchment caches");
              return f;
            });
    MountainDrainage.Region mountain = null, gorge = null;
    int mountainNode = -1, gorgeNode = -1, channels = 0, elevated = 0, cuts = 0;
    long start = System.nanoTime();
    Path dir =
        Path.of(
            "build/drainage-validation",
            Long.toString(level.getSeed()),
            Integer.toString(level.getMinY()));
    Files.createDirectories(dir);
    try (var csv = Files.newBufferedWriter(dir.resolve("channels.csv"))) {
      csv.write("x,z,parent_x,parent_z,terrain,water,parent_water,width,area\n");
      for (int rz = -1; rz <= 1; rz++)
        for (int rx = -1; rx <= 1; rx++) {
          var region = network.region(rx, rz);
          for (int k = 0; k < region.nodeCount(); k++) {
            int n = region.node(k), p = region.parent(n);
            if (region.width(n) <= 0) continue;
            channels++;
            if (p < 0 || region.water(p) > region.water(n))
              throw new AssertionError("Uphill river profile");
            int end = n, steps = 0;
            while (region.parent(end) >= 0) {
              end = region.parent(end);
              if (++steps > 5000) throw new AssertionError("Drainage cycle");
            }
            if (region.terrain(end) >= 95) throw new AssertionError("Dry outlet");
            var sample = h.sample(region.x(n), region.z(n));
            var point =
                new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(
                    region.x(n), 0, region.z(n));
            if (Math.abs(
                    level.getChunkSource().randomState().router().depth().compute(point) * 80
                        - sample.height())
                > 1e-7) throw new AssertionError("Native terrain and horizon diverged");
            if (region.water(n) > 160) {
              elevated++;
              if (region.area(n) < 200
                  && (mountain == null || region.water(n) > mountain.water(mountainNode))) {
                mountain = region;
                mountainNode = n;
              }
            }
            if (region.terrain(n) - region.water(n) > 16) {
              cuts++;
              if (region.water(n) > 112
                  && (gorge == null
                      || region.terrain(n) - region.water(n)
                          > gorge.terrain(gorgeNode) - gorge.water(gorgeNode))) {
                gorge = region;
                gorgeNode = n;
              }
            }
            csv.write(
                region.x(n)
                    + ","
                    + region.z(n)
                    + ","
                    + region.x(p)
                    + ","
                    + region.z(p)
                    + ","
                    + region.terrain(n)
                    + ","
                    + region.water(n)
                    + ","
                    + region.water(p)
                    + ","
                    + region.width(n)
                    + ","
                    + region.area(n)
                    + "\n");
          }
        }
    }
    if (mountain == null || gorge == null)
      throw new AssertionError("Missing high stream/gorge: " + elevated + "/" + cuts);
    int headwaters = 0, lakes = 0;
    for (int rz = -1; rz <= 1; rz++)
      for (int rx = -1; rx <= 1; rx++) {
        var region = network.region(rx, rz);
        for (int k = 0; k < region.nodeCount(); k++) {
          int n = region.node(k);
          if (region.headwater(n)) headwaters++;
          if (region.lakeRadius(n) > 0) {
            lakes++;
            if (lakes == 1) {
              record(
                  "HEADWATER_LAKE_POINT x="
                      + region.x(n)
                      + " z="
                      + region.z(n)
                      + " water="
                      + region.water(n)
                      + " radius="
                      + region.lakeRadius(n));
              full(level, h, region, n, "headwater-lake");
            }
          }
        }
      }
    record(
        "HEADWATER_MAP_PASS naturalSteps="
            + h.naturalSteps()
            + " sources="
            + headwaters
            + " lakes="
            + lakes);
    record(
        "DRAINAGE_MAP_PASS seed="
            + level.getSeed()
            + " minY="
            + level.getMinY()
            + " channels="
            + channels
            + " elevated="
            + elevated
            + " gorges="
            + cuts
            + " millis="
            + (System.nanoTime() - start) / 1000000);
    blocks(level, h, mountain, mountainNode, "mountain");
    blocks(level, h, gorge, gorgeNode, "gorge");
    int current = mountainNode, checked = 0;
    double last = Double.POSITIVE_INFINITY;
    while (current >= 0 && checked < 12) {
      int parent = mountain.parent(current);
      int subdivisions = parent >= 0 ? 4 : 1;
      for (int part = 0; part < subdivisions; part++) {
        double t = part / 4.0;
        int x =
            parent < 0
                ? mountain.x(current)
                : (int)
                    Math.round(
                        mountain.x(current) + t * (mountain.x(parent) - mountain.x(current)));
        int z =
            parent < 0
                ? mountain.z(current)
                : (int)
                    Math.round(
                        mountain.z(current) + t * (mountain.z(parent) - mountain.z(current)));
        double water = h.sample(x, z).waterLevel();
        if (water > last + 1.0)
          throw new AssertionError(
              "Actual channel surface rises at " + x + "," + z + ": " + last + " -> " + water);
        last = water;
        if (part % 2 == 0)
          blocksAt(level, h, x, z, (int) Math.floor(water), "course-" + checked + "-" + part);
      }
      current = parent;
      checked++;
    }
    if (checked < 3) throw new AssertionError("No descending course");
    full(level, h, mountain, mountainNode, "mountain");
    full(level, h, gorge, gorgeNode, "gorge");
    coast(level, h);
    record(
        "DRAINAGE_COURSE_PASS nodes="
            + checked
            + " sharedPlanner=true quarterSamples=true fullChunks=true");
  }

  private static void coast(ServerLevel level, WaterwayHeight h) throws java.io.IOException {
    var generator = level.getChunkSource().getGenerator();
    var state = level.getChunkSource().randomState();
    for (int z = -4096; z <= 4096; z += 32)
      for (int x = -4096; x <= 4096; x += 32) {
        var p = new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(x, 0, z);
        double c = h.continents().compute(p), e = h.erosion().compute(p), r = h.ridges().compute(p);
        if (c <= -.20 || c >= -.08 || e <= .25 || Math.abs(r) <= .3) continue;
        double height = h.compute(p);
        if (height < 97 || height > 100) continue;
        var biome =
            generator
                .getBiomeSource()
                .getNoiseBiome(
                    net.minecraft.core.QuartPos.fromBlock(x),
                    net.minecraft.core.QuartPos.fromBlock(99),
                    net.minecraft.core.QuartPos.fromBlock(z),
                    state.sampler());
        if (biome.is(net.minecraft.world.level.biome.Biomes.BEACH)
            || biome.is(net.minecraft.world.level.biome.Biomes.SNOWY_BEACH)) {
          WaterwayServerChecks.verify(level, "beach", x, z);
          record("DRAINAGE_COAST_PASS x=" + x + " z=" + z);
          return;
        }
      }
    throw new AssertionError("No dry coastal beach in survey");
  }

  private static void full(
      ServerLevel level, WaterwayHeight h, MountainDrainage.Region region, int n, String kind) {
    int x = region.x(n), z = region.z(n), water = (int) Math.floor(h.sample(x, z).waterLevel());
    int wet = 0;
    var pos = new BlockPos.MutableBlockPos();
    for (int dz = -3; dz <= 3; dz++)
      for (int dx = -3; dx <= 3; dx++) {
        var chunk =
            level.getChunkSource().getChunk((x + dx) >> 4, (z + dz) >> 4, ChunkStatus.FULL, true);
        for (int y = water - 12; y < water; y++)
          if (chunk
              .getBlockState(pos.set(x + dx, y, z + dz))
              .getFluidState()
              .is(net.minecraft.tags.FluidTags.WATER)) wet++;
      }
    if (wet < 10) throw new AssertionError("Channel lost water after carvers/features: " + kind);
    support(
        level.getChunkSource().getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true),
        level,
        kind + "-full");
    record("DRAINAGE_FULL_PASS kind=" + kind + " waterLevel=" + water + " water=" + wet);
  }

  private static void blocks(
      ServerLevel level, WaterwayHeight h, MountainDrainage.Region region, int n, String kind) {
    blocksAt(
        level,
        h,
        region.x(n),
        region.z(n),
        (int) Math.floor(h.sample(region.x(n), region.z(n)).waterLevel()),
        kind);
  }

  private static void blocksAt(
      ServerLevel level, WaterwayHeight h, int x, int z, int water, String kind) {
    if (h.naturalSteps() && h.sample(x, z).strength() > 0)
      water = com.shnewbs.deepprimal.core.WaterDynamics.surfaceTopY(h.sample(x, z).waterLevel());
    var chunk = level.getChunkSource().getChunk(x >> 4, z >> 4, ChunkStatus.SURFACE, true);
    support(chunk, level, kind);
    var noise =
        chunk.getOrCreateNoiseChunk(
            c -> {
              throw new AssertionError("No fresh native noise chunk");
            });
    if (!(noise.aquifer() instanceof SurfaceWaterAquifer))
      throw new AssertionError("No routed aquifer");
    WaterwayHeight[] mapped = {null};
    // mapAll models the native wrapping lifecycle without generating a second catchment cache.
    h.mapAll(
        f -> {
          if (f instanceof WaterwayHeight w) mapped[0] = w;
          return f;
        });
    if (mapped[0].drainage() != h.drainage())
      throw new AssertionError("Planner duplicated while wrapping");
    var center = new BlockPos(x, water - 1, z);
    if (!chunk.getBlockState(center).getFluidState().is(net.minecraft.tags.FluidTags.WATER))
      throw new AssertionError(
          "Dry channel center: "
              + kind
              + " at "
              + x
              + ","
              + z
              + " sample="
              + h.sample(x, z)
              + " center="
              + chunk.getBlockState(center)
              + " lower="
              + chunk.getBlockState(center.below())
              + " upper="
              + chunk.getBlockState(center.above()));
    int bottom = water - 1;
    while (bottom > level.getMinY()
        && chunk
            .getBlockState(new BlockPos(x, bottom, z))
            .getFluidState()
            .is(net.minecraft.tags.FluidTags.WATER)) bottom--;
    if (chunk.getBlockState(new BlockPos(x, bottom, z)).isAir())
      throw new AssertionError("Unsupported floating surface water: " + kind);
    int wet = 0, aboveSea = 0, gravel = 0, rock = 0;
    var pos = new BlockPos.MutableBlockPos();
    for (int dz = -3; dz <= 3; dz++)
      for (int dx = -3; dx <= 3; dx++) {
        var c =
            level
                .getChunkSource()
                .getChunk((x + dx) >> 4, (z + dz) >> 4, ChunkStatus.SURFACE, true);
        for (int y = water - 12; y <= water + 1; y++) {
          var b = c.getBlockState(pos.set(x + dx, y, z + dz));
          if (b.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) {
            wet++;
            if (y >= 96) aboveSea++;
          }
          if (b.is(Blocks.GRAVEL)) gravel++;
          if (b.is(Blocks.ANDESITE) || b.is(Blocks.STONE) || b.is(Blocks.GRANITE)) rock++;
        }
      }
    if (wet < 10 || (water > 112 && aboveSea < 10))
      throw new AssertionError(
          "Dry elevated channel "
              + kind
              + " at "
              + x
              + ","
              + z
              + " water="
              + water
              + " blocks="
              + wet);
    record(
        "DRAINAGE_BLOCKS_PASS kind="
            + kind
            + " x="
            + x
            + " z="
            + z
            + " waterLevel="
            + water
            + " water="
            + wet
            + " elevatedWater="
            + aboveSea
            + " gravel="
            + gravel
            + " rock="
            + rock
            + " centerWet=true supported=true");
  }

  private static void support(
      net.minecraft.world.level.chunk.ChunkAccess chunk, ServerLevel level, String kind) {
    int currents = 0, partial = 0;
    var pos = new BlockPos.MutableBlockPos();
    for (int z = chunk.getPos().getMinBlockZ(); z < chunk.getPos().getMinBlockZ() + 16; z++)
      for (int x = chunk.getPos().getMinBlockX(); x < chunk.getPos().getMinBlockX() + 16; x++)
        for (int y = level.getMinY(); y < level.getMaxY(); y++) {
          var state = chunk.getBlockState(pos.set(x, y, z));
          if (!(state.getBlock() instanceof com.shnewbs.deepprimal.water.RiverWaterBlock)) continue;
          currents++;
          if (!state.getFluidState().isSource()) partial++;
          var below = chunk.getBlockState(pos.set(x, y - 1, z));
          if (!below.getFluidState().is(net.minecraft.tags.FluidTags.WATER)
              && !below.blocksMotion())
            throw new AssertionError("Water curtain at " + x + "," + y + "," + z + " kind=" + kind);
          if (state.getFluidState().getFlow(chunk, pos.set(x, y, z)).lengthSqr() < .1)
            throw new AssertionError("Generated current has no native flow");
        }
    record(
        "RIVER_SUPPORT_PASS kind="
            + kind
            + " currentBlocks="
            + currents
            + " partialSurfaces="
            + partial
            + " chunk="
            + chunk.getPos());
  }
}
