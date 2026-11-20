package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.Aquifer;
import com.shnewbs.deepprimal.core.TerrainEngine;
import com.shnewbs.deepprimal.core.WaterAccount;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import com.shnewbs.deepprimal.engine.HydrologyConfig;
import com.shnewbs.deepprimal.engine.HydrologyData;
import com.shnewbs.deepprimal.engine.HydrologyRuntime;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Runs on the server thread of a disposable seed-20261020 standard engine world. */
final class SeasonalWaterChecks {
  private static final long YEAR = 96L * 24_000;
  private static final BlockPos COLD = new BlockPos(-376, 0, 280);
  private static final BlockPos WARM = new BlockPos(-328, 0, 264);
  private static final long REGION = HydrologyData.key(COLD.getX(), COLD.getZ());

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  private static Object runtimeState(String name) throws ReflectiveOperationException {
    var type = Class.forName(HydrologyRuntime.class.getName() + "$" + name);
    var constructor = type.getDeclaredConstructor();
    constructor.setAccessible(true);
    return constructor.newInstance();
  }

  private static Field field(Class<?> type, String name) throws ReflectiveOperationException {
    var field = type.getDeclaredField(name);
    field.setAccessible(true);
    return field;
  }

  private static final class RegionVisit {
    final Object work = runtimeState("Work");
    final Object cursor = runtimeState("Cursor");
    final Method visit = HydrologyRuntime.class.getDeclaredMethod("visit", ServerLevel.class,
        work.getClass(), cursor.getClass(), int.class, TerrainEngine.class, int.class, int.class);
    final ServerLevel level;
    final TerrainEngine terrain;

    RegionVisit(ServerLevel level) throws ReflectiveOperationException {
      this.level = level;
      this.terrain = ((EngineChunkGenerator) level.getChunkSource().getGenerator()).geography();
      visit.setAccessible(true);
    }

    void at(BlockPos column) throws ReflectiveOperationException {
      int x = column.getX(), z = column.getZ();
      int columnIndex = (249 * ((x & 15) + (z & 15) * 16)) & 255;
      visit.invoke(null, level, work, cursor, columnIndex, terrain, x, z);
    }
  }

  private static BlockPos surface(ServerLevel level, BlockPos column) {
    return new BlockPos(column.getX(), level.getMaxY() - 8, column.getZ());
  }

  private static void seal(ServerLevel level, BlockPos column) {
    var pos = surface(level, column);
    level.getChunkAt(pos);
    level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 2);
    require(level.getBlockState(pos).is(Blocks.BEDROCK), "Sealed sample column");
  }

  private static void clock(ServerLevel level, long tick) {
    ((ServerLevelData) level.getLevelData()).setGameTime(tick);
  }

  @SuppressWarnings("unchecked")
  static void pendingChunk(ServerLevel level) throws ReflectiveOperationException {
    var ready = level.getChunkAt(COLD).getPos();
    var pending = new ChunkPos(ready.x() + 23, ready.z() + 17);
    require(level.getChunkSource().getChunkNow(pending.x(), pending.z()) == null,
        "Pending test chunk must start outside the loaded area");
    var work = runtimeState("Work");
    var queued = (Map<Long, Object>) field(work.getClass(), "chunks").get(work);
    var all = (Map<ServerLevel, Object>) field(HydrologyRuntime.class, "WORK").get(null);
    Object readyCursor = runtimeState("Cursor"), pendingCursor = runtimeState("Cursor");
    var column = field(readyCursor.getClass(), "column");
    queued.put(pending.pack(), pendingCursor);
    queued.put(ready.pack(), readyCursor);
    var previous = all.put(level, work);
    try {
      var runtime = new HydrologyRuntime();
      var tick = new LevelTickEvent.Post(() -> true, level);
      for (int i = 0; i < 64 && column.getInt(readyCursor) == 0; i++) runtime.tick(tick);
      require(column.getInt(readyCursor) > 0, "Ready chunk starved behind an unavailable chunk");
      require(queued.get(pending.pack()) == pendingCursor, "Regional pass discarded the pending chunk");
      require(level.getChunkSource().getChunkNow(pending.x(), pending.z()) == null,
          "Regional pass loaded the missing chunk");
      level.getChunk(pending.x(), pending.z());
      for (int i = 0; i < 64 && column.getInt(pendingCursor) == 0; i++) runtime.tick(tick);
      require(queued.get(pending.pack()) == pendingCursor && column.getInt(pendingCursor) > 0,
          "Pending cursor failed to resume after delivery");
    } finally {
      if (previous == null) all.remove(level);
      else all.put(level, previous);
    }
  }

  static void visitOrder(ServerLevel level) throws ReflectiveOperationException {
    var data = HydrologyData.get(level);
    var original = data.aquifers.get(REGION);
    long oldTime = level.getGameTime();
    float oldRain = level.getRainLevel(1);
    try {
      seal(level, COLD);
      seal(level, WARM);
      level.setRainLevel(0);
      for (long tick : new long[] {YEAR, YEAR + 3 * YEAR / 4}) {
        long[] remaining = new long[2];
        for (int order = 0; order < 2; order++) {
          var region = new RegionVisit(level);
          var aquifer = new Aquifer(WaterAccount.Parcel.EMPTY, new WaterAccount.Parcel(8192, 61, 89),
              0, false, tick - 20);
          data.aquifers.put(REGION, aquifer);
          clock(level, tick);
          region.at(order == 0 ? COLD : WARM);
          region.at(order == 0 ? WARM : COLD);
          remaining[order] = aquifer.snow.water();
        }
        long expected = tick == YEAR ? 4096 : 8192;
        require(remaining[0] == expected && remaining[1] == expected,
            "Regional snowpack at tick " + tick + " expected " + expected + " in both orders, got "
                + remaining[0] + " and " + remaining[1]);
      }
    } finally {
      clock(level, oldTime);
      level.setRainLevel(oldRain);
      if (original == null) data.aquifers.remove(REGION);
      else data.aquifers.put(REGION, original);
    }
  }

  static void exposedIce(ServerLevel level) throws ReflectiveOperationException {
    var data = HydrologyData.get(level);
    var original = data.aquifers.get(REGION);
    long time = level.getGameTime();
    var cold = surface(level, COLD);
    var first = surface(level, WARM);
    var second = first.east();
    var observer = surface(level, OBSERVER);
    for (var pos : new BlockPos[] {cold, first, second, observer}) level.getChunkAt(pos);
    var coldState = level.getBlockState(cold);
    var firstState = level.getBlockState(first);
    var secondState = level.getBlockState(second);
    var observerState = level.getBlockState(observer);
    try {
      var region = new RegionVisit(level);
      var aquifer = new Aquifer(new WaterAccount.Parcel(100_000, 19, 31), WaterAccount.Parcel.EMPTY,
          0, false, YEAR - 20);
      data.aquifers.put(REGION, aquifer);
      level.setBlock(observer, Blocks.BEDROCK.defaultBlockState(), 2);
      level.setBlock(cold, Blocks.ICE.defaultBlockState(), 2);
      level.setBlock(first, Blocks.BLUE_ICE.defaultBlockState(), 2);
      level.setBlock(second, Blocks.PACKED_ICE.defaultBlockState(), 2);
      clock(level, YEAR);
      region.at(observer);
      region.at(cold);
      region.at(first);
      region.at(second);
      require(HydrologyRuntime.contents(level, first, level.getBlockState(first)).equals(
          new WaterAccount.Parcel(4096, 0, 0)), "Warm glacial ice missed its melt after a dry column");
      require(level.getBlockState(cold).is(Blocks.ICE) && level.getBlockState(second).is(Blocks.PACKED_ICE),
          "One exposed warm ice block may melt per aquifer interval");
      // Transient region state can be rebuilt in the same interval after a chunk reload.
      var rebuilt = new RegionVisit(level);
      rebuilt.at(second);
      require(level.getBlockState(second).is(Blocks.PACKED_ICE), "Reload reopened the melt allowance");
      clock(level, YEAR + 20);
      rebuilt.at(cold);
      rebuilt.at(second);
      require(HydrologyRuntime.contents(level, second, level.getBlockState(second)).equals(
          new WaterAccount.Parcel(4096, 0, 0)), "The next interval must release a second fresh parcel");
    } finally {
      clock(level, time);
      level.setBlock(cold, coldState, 2);
      level.setBlock(first, firstState, 2);
      level.setBlock(second, secondState, 2);
      level.setBlock(observer, observerState, 2);
      if (original == null) data.aquifers.remove(REGION);
      else data.aquifers.put(REGION, original);
    }
  }

  private static final BlockPos OBSERVER = new BlockPos(-360, 0, 280);
  private static final BlockPos SEA = new BlockPos(2232, TerrainEngine.SEA - 1, 8);

  private static void weatherColumn(ServerLevel level, BlockPos column, boolean snow) {
    var bed = surface(level, column);
    level.setBlock(bed, (snow ? Blocks.SAND : Blocks.DIRT).defaultBlockState(), 2);
    var sky = bed.above();
    EnvironmentalWaterChecks.sampleBiome(level, sky, snow ? "minecraft:snowy_plains" : "minecraft:jungle");
    var kind = level.getBiome(sky).value().getPrecipitationAt(sky, level.getSeaLevel());
    require(kind == (snow ? net.minecraft.world.level.biome.Biome.Precipitation.SNOW
        : net.minecraft.world.level.biome.Biome.Precipitation.RAIN), "Native weather at " + sky);
  }

  private record Accounts(WaterAccount.Parcel ground, WaterAccount.Parcel snow,
      WaterAccount.Parcel air, WaterAccount.Parcel ocean, WaterAccount.Parcel seaCell,
      WaterAccount.Parcel inlet, long tick, int wetSteps, int dynamicWetSteps, int depth, boolean active) {
    static Accounts read(ServerLevel level, Aquifer aquifer) {
      var data = HydrologyData.get(level);
      return new Accounts(aquifer.ground.snapshot(), aquifer.snow.snapshot(), data.atmosphere.snapshot(),
          data.ocean.snapshot(), HydrologyRuntime.contents(level, SEA, level.getBlockState(SEA)),
          HydrologyRuntime.contents(level, SEA.above(), level.getBlockState(SEA.above())),
          aquifer.lastTick(), aquifer.wetSteps(), aquifer.dynamicWetSteps(), aquifer.depth(), aquifer.active());
    }

    WaterAccount.Parcel total() {
      long water = 0, salt = 0, sediment = 0;
      for (var account : new WaterAccount.Parcel[] {ground, snow, air, ocean, seaCell, inlet}) {
        water += account.water();
        salt += account.salt();
        sediment += account.sediment();
      }
      return new WaterAccount.Parcel(water, salt, sediment);
    }
  }

  private static void exchange(ServerLevel level, Aquifer aquifer) {
    var inlet = SEA.above();
    var data = HydrologyData.get(level);
    long oceanBefore = data.ocean.water();
    long groundBefore = aquifer.ground.water();
    require(HydrologyRuntime.emit(level, inlet, aquifer, inlet.getY() + 8, 4), "Paid surface discharge");
    require(HydrologyRuntime.oceanSink(level, inlet, SEA), "Freshwater reached the ocean");
    require(oceanBefore + 4L * WaterAccount.SCALE == data.ocean.water()
        && groundBefore - 4L * WaterAccount.SCALE == aquifer.ground.water(), "Ocean and aquifer transfer");
    var bucket = HydrologyRuntime.pickup(level, SEA);
    require(bucket != null && HydrologyRuntime.place(level, inlet, bucket), "Return measured sea water");
    for (int i = 0; i < 4; i++)
      require(HydrologyRuntime.oceanSink(level, inlet, SEA), "Return measured parcel to the ocean");
    require(level.getBlockState(inlet).isAir() && HydrologyRuntime.boundary(level.getBlockState(SEA))
        && HydrologyRuntime.contents(level, SEA, level.getBlockState(SEA)).water() == 4096,
        "Ocean exchange changed sea stage");
  }

  static void run(ServerLevel level, Path receipt, boolean reopen) throws Exception {
    require(level.getSeed() == 20261020L, "Use disposable standard engine world with seed 20261020");
    boolean enabled = HydrologyConfig.ENABLED.get();
    long oldTime = level.getGameTime();
    float oldRain = level.getRainLevel(1);
    try {
      HydrologyConfig.ENABLED.set(true);
      if (!reopen) {
        visitOrder(level);
        pendingChunk(level);
        System.out.println("SEASONAL_REGRESSION_PASS oppositeVisitOrders=2 climatePhases=2 pendingChunkResumed=true");
      }
      var region = new RegionVisit(level);
      var data = HydrologyData.get(level);
      long start = 3 * YEAR / 4 + (reopen ? YEAR : 0);
      for (int z = 264; z < 320; z += 16) for (int x = -376; x < -320; x += 16) {
        var column = new BlockPos(x, 0, z);
        level.getChunkAt(column);
        if (!reopen) seal(level, column);
      }
      level.getChunkAt(SEA);
      require(region.terrain.ocean(SEA.getX(), SEA.getZ()) && HydrologyRuntime.boundary(level.getBlockState(SEA))
          && level.getBlockState(SEA.above()).isAir(), "Expected an open generated ocean surface at " + SEA);
      if (!reopen) {
        weatherColumn(level, COLD, true);
        weatherColumn(level, WARM, false);
        data.aquifers.put(REGION, new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY * 32, 700_000, 400_000),
            new WaterAccount.Parcel(128_000, 91, 127), Aquifer.WET_STEPS, true, start - 20, 0, Aquifer.DEEP));
        data.setDirty();
      }
      var aquifer = data.aquifers.get(REGION);
      require(aquifer != null, "Expected saved regional aquifer");
      var before = Accounts.read(level, aquifer);
      if (reopen) {
        require(Files.readString(receipt).equals(before.toString()), "All saved water accounts and aquifer clock must match");
        System.out.println("SEASONAL_REOPEN_PASS snow=" + before.snow() + " ocean=" + before.ocean());
      }
      var total = before.total();
      int storms = 0, accumulation = 0, thaw = 0, exchanges = 0;
      for (int i = 0; i < YEAR / 20; i++) {
        long now = start + 20L * i;
        clock(level, now);
        boolean storm = i % 900 < 12;
        level.setRainLevel(storm ? 1 : 0);
        long snowBefore = aquifer.snow.water();
        long groundBefore = aquifer.ground.water();
        region.at(OBSERVER);
        require(aquifer.lastTick() == now, "Missed regional interval " + i);
        if (storm && aquifer.ground.water() > groundBefore && aquifer.snow.water() > snowBefore) storms++;
        if (aquifer.snow.water() > snowBefore) accumulation++;
        if (aquifer.snow.water() < snowBefore) thaw++;
        if (i % 600 == 0) {
          exchange(level, aquifer);
          exchanges++;
          require(Accounts.read(level, aquifer).total().equals(total), "Budget changed at interval " + i);
        }
      }
      var after = Accounts.read(level, aquifer);
      require(after.total().equals(total) && after.snow().water() > 0, "Year-end conservation and winter snowpack");
      require(storms > 0 && accumulation > 0 && thaw > 0 && after.ocean().water() < before.ocean().water(),
          "Seasonal rain, snow, thaw and net ocean evaporation must all occur");
      require(after.ground().water() > before.ground().water(), "Wet-year groundwater recharge");
      if (!reopen) Files.writeString(receipt, after.toString());
      System.out.println("SEASONAL_YEAR_PASS year=" + (reopen ? 2 : 1) + " intervals=" + YEAR / 20
          + " stormRecharge=" + storms + " snowGain=" + accumulation + " snowLoss=" + thaw
          + " oceanExchanges=" + exchanges + " endSnow=" + after.snow().water() + " total=" + total);
    } finally {
      clock(level, oldTime);
      level.setRainLevel(oldRain);
      HydrologyConfig.ENABLED.set(enabled);
    }
  }

  private SeasonalWaterChecks() {}
}
