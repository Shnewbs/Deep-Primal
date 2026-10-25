package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Sampling-only timings in a disposable loaded fixture, excluding generation and setup. */
final class StormSamplingChecks {
  static void run(ServerLevel level, Path record) throws Exception {
    var terrain = ((EngineChunkGenerator) level.getChunkSource().getGenerator()).geography();
    var precipitation = HydrologyRuntime.class.getDeclaredMethod("precipitation",
        ServerLevel.class, TerrainEngine.class, int.class, int.class, HydrologyData.class, Aquifer.class);
    precipitation.setAccessible(true);
    var origins = List.of(new BlockPos(-64, 499, -64), new BlockPos(0, 499, -64),
        new BlockPos(-64, 499, 0), new BlockPos(0, 499, 0));
    var samples = new ArrayList<BlockPos>();
    for (var origin : origins) {
      for (int dz = 8; dz < 64; dz += 16)
        for (int dx = 8; dx < 64; dx += 16) {
          var p = origin.offset(dx, 0, dz);
          level.getChunkAt(p);
          for (int y = 500; y < level.getMaxY(); y++)
            level.setBlock(p.atY(y), Blocks.AIR.defaultBlockState(), 2 | 16);
          samples.add(p);
        }
      level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(),
          "fillbiome " + origin.getX() + " 496 " + origin.getZ() + " "
              + (origin.getX() + 63) + " 503 " + (origin.getZ() + 63) + " minecraft:jungle");
    }
    var rows = new ArrayList<Map<String, Object>>();
    var materials = new ArrayList<BlockState>();
    for (var soil : GroundMaterials.soilsList()) materials.add(soil.defaultBlockState());
    materials.addAll(List.of(Blocks.SAND.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(),
        Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.DIRT.defaultBlockState()));
    var rockIce = List.of(Blocks.STONE, Blocks.GRANITE, Blocks.ANDESITE, Blocks.DIORITE,
        Blocks.BASALT, Blocks.TUFF, Blocks.SANDSTONE, Blocks.CALCITE, Blocks.DEEPSLATE,
        Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.SNOW_BLOCK);
    for (String scenario : List.of("soil", "rock-ice", "sealed")) {
      long[] expected = new long[origins.size()];
      for (int i = 0; i < samples.size(); i++) {
        var p = samples.get(i);
        if (level.getChunkSource().getChunkNow(p.getX() >> 4, p.getZ() >> 4) == null)
          throw new AssertionError("Storm sample chunk is not resident " + p);
        var state = scenario.equals("soil") ? materials.get(i % materials.size())
            : scenario.equals("rock-ice") ? rockIce.get(i % rockIce.size()).defaultBlockState()
            : Blocks.GLASS.defaultBlockState();
        level.setBlock(p, state, 2 | 16);
        var light = level.getChunkSource().getLightEngine();
        var chunkPos = new net.minecraft.world.level.ChunkPos(p.getX() >> 4, p.getZ() >> 4);
        light.setLightEnabled(chunkPos, true);
        light.propagateLightSources(chunkPos);
        var pending = light.waitForPendingTasks(p.getX() >> 4, p.getZ() >> 4);
        light.tryScheduleUpdate();
        long lightDeadline = System.nanoTime() + 30_000_000_000L;
        level.getServer().managedBlock(() -> pending.isDone() || System.nanoTime() >= lightDeadline);
        pending.get(0, java.util.concurrent.TimeUnit.SECONDS);
        var actualTop = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, p);
        if (!actualTop.equals(p.above()) || !level.canSeeSky(actualTop))
          throw new AssertionError("Storm top fixture p=" + p + " top=" + actualTop);
        if (level.getBiome(p.above()).value().getPrecipitationAt(p.above(), level.getSeaLevel())
            != net.minecraft.world.level.biome.Biome.Precipitation.RAIN)
          throw new AssertionError("Storm warm biome fixture");
        double permeability = scenario.equals("sealed") ? 0
            : scenario.equals("rock-ice") ? new Geology(terrain.noise)
                .column(p.getX(), p.getZ(), terrain.column(p.getX(), p.getZ())).permeability()
            : state.getBlock() instanceof GroundMaterials.SoilBlock soil
                ? soil.texture().permeability()
                : state.is(Blocks.SAND) || state.is(Blocks.GRAVEL) ? .9 : .5;
        expected[i / 16] += (long) (HydrologyConfig.RAIN_QUANTA.get() * permeability * 256);
      }
      long[] times = new long[200];
      for (int iteration = -100; iteration < times.length; iteration++) {
        var data = new HydrologyData();
        var stores = new Aquifer[origins.size()];
        long before = data.atmosphere.water();
        for (int i = 0; i < stores.length; i++)
          stores[i] = new Aquifer(new WaterAccount.Parcel(1000, 17, 31),
              WaterAccount.Parcel.EMPTY, 0, false, 0);
        long start = System.nanoTime();
        for (int i = 0; i < origins.size(); i++) {
          var origin = origins.get(i);
          precipitation.invoke(null, level, terrain, origin.getX(), origin.getZ(), data, stores[i]);
        }
        long elapsed = System.nanoTime() - start;
        if (iteration >= 0) times[iteration] = elapsed;
        long total = 0;
        for (int i = 0; i < stores.length; i++) {
          var store = stores[i];
          if (store.ground.water() != 1000 + expected[i] || store.snow.water() != 0
              || store.ground.snapshot().salt() != 17 || store.ground.snapshot().sediment() != 31)
            throw new AssertionError("Storm recharge oracle scenario=" + scenario + " region=" + i
                + " expected=" + expected[i] + " actual=" + (store.ground.water() - 1000)
                + " snow=" + store.snow.water());
          total += expected[i];
        }
        if (before - data.atmosphere.water() != total) throw new AssertionError("Storm atmospheric debit");
      }
      Arrays.sort(times);
      var row = new LinkedHashMap<String, Object>();
      row.put("scenario", scenario);
      row.put("expected_quanta_per_batch", Arrays.stream(expected).sum());
      row.put("p50_ns", times[99]);
      row.put("p95_ns", times[189]);
      row.put("p99_ns", times[197]);
      row.put("max_ns", times[199]);
      rows.add(row);
      System.out.println("STORM_SAMPLING_ROW " + row);
    }
    var result = new LinkedHashMap<String, Object>();
    result.put("scope", "Four-region precipitation component; not full tick or end-to-end storm latency");
    result.put("regions", origins.size());
    result.put("samples_per_batch", samples.size());
    result.put("warmup_batches", 100);
    result.put("measured_batches", 200);
    result.put("java", System.getProperty("java.runtime.version"));
    result.put("cpu", System.getenv("PROCESSOR_IDENTIFIER"));
    result.put("rows", rows);
    result.put("pass", true);
    Files.writeString(record, new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n");
    System.out.println("STORM_SAMPLING_PASS regions=4 samples=64 exactRecharge=true exactConstituents=true");
  }

  private StormSamplingChecks() {}
}
