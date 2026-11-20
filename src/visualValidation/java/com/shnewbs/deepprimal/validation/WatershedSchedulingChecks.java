package com.shnewbs.deepprimal.validation;

import static com.shnewbs.deepprimal.validation.StreamSourceNativeChecks.field;
import static com.shnewbs.deepprimal.validation.StreamSourceNativeChecks.instance;

import com.shnewbs.deepprimal.core.Aquifer;
import com.shnewbs.deepprimal.core.TerrainEngine;
import com.shnewbs.deepprimal.core.WaterAccount;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import com.shnewbs.deepprimal.engine.HydrologyConfig;
import com.shnewbs.deepprimal.engine.HydrologyData;
import com.shnewbs.deepprimal.engine.HydrologyRuntime;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Exercises public server ticks with controlled deficits in resident and unavailable chunks. */
final class WatershedSchedulingChecks {
  private static final int PRESSURE = 8192;
  private static final BlockPos HIGH = new BlockPos(-2040, 302, -1784);
  private static final BlockPos LOW = HIGH.east().below(32);
  private static final BlockPos MIDDLE = HIGH.east(16).below(12);
  private static final BlockPos REMOTE = new BlockPos(2120, 252, 2120);

  private static void require(boolean ok, String why) { if (!ok) throw new AssertionError(why); }

  private static Aquifer supply(long tick) {
    return new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY / 2, 77_111, 51_991),
        WaterAccount.Parcel.EMPTY, Aquifer.WET_STEPS, true, tick);
  }

  private static void bed(ServerLevel level, BlockPos p, boolean blocked) {
    level.getChunkAt(p);
    level.setBlock(p.below(), Blocks.BEDROCK.defaultBlockState(), 2);
    level.setBlock(p, (blocked ? Blocks.STONE : Blocks.AIR).defaultBlockState(), 2);
  }

  @SuppressWarnings("unchecked")
  private static final class Network implements AutoCloseable {
    final Object work = instance("Work");
    final Map<Long,Object> chunks = (Map<Long,Object>) field(work.getClass(), "chunks").get(work);
    final Map<ServerLevel,Object> all = (Map<ServerLevel,Object>) field(HydrologyRuntime.class, "WORK").get(null);
    final Object previous;
    final ServerLevel level;
    final TerrainEngine terrain;
    final Method plan;
    final Method register;
    final Method region;

    Network(ServerLevel level) throws ReflectiveOperationException {
      this.level = level;
      terrain = ((EngineChunkGenerator) level.getChunkSource().getGenerator()).geography();
      var cursor = instance("Cursor").getClass();
      plan = cursor.getDeclaredMethod("plan", int.class, double.class);
      register = work.getClass().getDeclaredMethod("indexStreams", long.class, cursor);
      region = HydrologyRuntime.class.getDeclaredMethod("region", work.getClass(), TerrainEngine.class, int.class, int.class);
      plan.setAccessible(true);
      register.setAccessible(true);
      region.setAccessible(true);
      previous = all.put(level, work);
    }

    void source(BlockPos p) throws ReflectiveOperationException {
      long key = ChunkPos.pack(p.getX() >> 4, p.getZ() >> 4);
      var cursor = chunks.get(key);
      if (cursor == null) {
        cursor = instance("Cursor");
        Arrays.fill((long[]) field(cursor.getClass(), "visited").get(cursor), Long.MAX_VALUE);
        chunks.put(key, cursor);
      }
      int col = 249 * ((p.getX() & 15) + ((p.getZ() & 15) << 4)) & 255;
      plan.invoke(cursor, col, p.getY() + 1.);
      register.invoke(work, key, cursor);
    }

    Aquifer prime(BlockPos p, long now) throws ReflectiveOperationException {
      var data = HydrologyData.get(level);
      var aquifer = supply(now);
      data.aquifers.put(HydrologyData.key(p.getX(),p.getZ()), aquifer);
      var budget = region.invoke(null, work, terrain, p.getX(), p.getZ());
      field(budget.getClass(), "tick").setLong(budget, now);
      field(budget.getClass(), "natural").setInt(budget, 0);
      return aquifer;
    }

    @Override public void close() {
      if (previous == null) all.remove(level);
      else all.put(level, previous);
    }
  }

  private static WaterAccount.Parcel cell(ServerLevel l, BlockPos p) {
    return HydrologyRuntime.contents(l, p, l.getBlockState(p));
  }

  static void priority(ServerLevel level) throws ReflectiveOperationException {
    bed(level, HIGH, true); bed(level, LOW, false); bed(level, MIDDLE, false);
    var d = HydrologyData.get(level);
    long key = HydrologyData.key(HIGH.getX(), HIGH.getZ());
    var original = d.aquifers.get(key);
    long now = level.getGameTime();
    try (var net = new Network(level)) {
      net.source(LOW); net.source(HIGH); net.source(MIDDLE);
      var aquifer = net.prime(HIGH, level.getGameTime());
      var before = aquifer.ground.snapshot();
      var runtime = new HydrologyRuntime();
      var tick = new LevelTickEvent.Post(() -> true, level);
      for (int i = 0; i < 64 && aquifer.ground.water() == before.water(); i++) runtime.tick(tick);
      var low = cell(level, LOW);
      var middle = cell(level, MIDDLE);
      require(low.water() == 0 && middle.water() == 4L * WaterAccount.SCALE,
          "Across the chunk edge the next highest open spring should get four units. low=" + low.water() + " middle=" + middle.water());
      var ground = aquifer.ground.snapshot();
      require(ground.water() + middle.water() == before.water() && ground.salt() + middle.salt() == before.salt()
          && ground.sediment() + middle.sediment() == before.sediment(), "Exact cross-chunk source debit");
      level.setBlock(HIGH, Blocks.AIR.defaultBlockState(), 2);
      ((ServerLevelData) level.getLevelData()).setGameTime(now + 20);
      for (int i = 0; i < 64 && cell(level, HIGH).water() == 0; i++) runtime.tick(tick);
      require(cell(level, HIGH).water() == 4L * WaterAccount.SCALE && cell(level, MIDDLE).equals(middle),
          "A reopened headwater must regain its share in the next interval");
    } finally {
      ((ServerLevelData) level.getLevelData()).setGameTime(now);
      if (original == null) d.aquifers.remove(key);
      else d.aquifers.put(key, original);
    }
  }

  static void pressure(ServerLevel level) throws ReflectiveOperationException {
    bed(level, REMOTE, false);
    var data = HydrologyData.get(level);
    long key = HydrologyData.key(REMOTE.getX(), REMOTE.getZ());
    var original = data.aquifers.get(key);
    long oldTime = level.getGameTime();
    try (var net = new Network(level)) {
      // High-elevation parcels awaiting chunk delivery precede the resident lowland.
      for (int i = 0; i < PRESSURE; i++)
        net.source(new BlockPos(-1_000_000 - i * 64, 360 + ((i * 73) & 127), 1_000_000));
      net.source(REMOTE);
      net.prime(REMOTE, oldTime);
      var cache = level.getChunkSource();
      int loaded = cache.getLoadedChunksCount();
      long initialized = data.initialized;
      var tick = new LevelTickEvent.Post(() -> true, level);
      var runtime = new HydrologyRuntime();
      var bean = ManagementFactory.getThreadMXBean();
      var meter = bean instanceof com.sun.management.ThreadMXBean m && m.isThreadAllocatedMemorySupported() ? m : null;
      if (meter != null) meter.setThreadAllocatedMemoryEnabled(true);
      long[] bytes = new long[96];
      int first = 0;
      for (int i = 0; i < bytes.length; i++) {
        ((ServerLevelData)level.getLevelData()).setGameTime(oldTime + i);
        long before = meter == null ? 0 : meter.getCurrentThreadAllocatedBytes();
        runtime.tick(tick);
        if (meter != null) bytes[i] = meter.getCurrentThreadAllocatedBytes() - before;
        if (first == 0 && cell(level, REMOTE).water() > 0) first = i + 1;
      }
      Arrays.sort(bytes);
      System.out.println("WATERSHED_STREAM_PRESSURE pending=" + PRESSURE + " ticks=" + bytes.length
          + " firstDelivery=" + first + " medianAllocatedBytes=" + (meter == null ? "unavailable" : bytes[bytes.length / 2]));
      require(cache.getLoadedChunksCount() == loaded && data.initialized == initialized,
          "Examining unavailable sources must neither load nor seed a chunk");
      require(first > 0, "A resident lowland received no water under stream pressure");
    } finally {
      ((ServerLevelData)level.getLevelData()).setGameTime(oldTime);
      if (original == null) data.aquifers.remove(key);
      else data.aquifers.put(key, original);
    }
  }

  static void run(ServerLevel level) throws ReflectiveOperationException {
    require(level.getSeed() == 20261020L, "Use the disposable seed-20261020 engine world");
    boolean enabled = HydrologyConfig.ENABLED.get();
    int rain = HydrologyConfig.RAIN_QUANTA.get();
    try {
      HydrologyConfig.ENABLED.set(true);
      HydrologyConfig.RAIN_QUANTA.set(64);
      priority(level);
      pressure(level);
      SeasonalWaterChecks.exposedIce(level);
      System.out.println("WATERSHED_SCHEDULING_PASS crossChunkHeadwater=true paidLowland=true glacierIntervals=2");
    } finally {
      HydrologyConfig.RAIN_QUANTA.set(rain);
      HydrologyConfig.ENABLED.set(enabled);
    }
  }

  private WatershedSchedulingChecks() {}
}
