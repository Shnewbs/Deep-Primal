package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.Aquifer;
import com.shnewbs.deepprimal.core.TerrainEngine;
import com.shnewbs.deepprimal.core.WaterAccount;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import com.shnewbs.deepprimal.engine.EngineFluid;
import com.shnewbs.deepprimal.engine.EngineRegistry;
import com.shnewbs.deepprimal.engine.HydrologyData;
import com.shnewbs.deepprimal.engine.HydrologyRuntime;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/** Dedicated-server check of metered natural stream output through the runtime path. */
final class StreamSourceNativeChecks {
  private static Field field(Class<?> type, String name) throws ReflectiveOperationException {
    var field = type.getDeclaredField(name);
    field.setAccessible(true);
    return field;
  }

  private static Object instance(String nested) throws ReflectiveOperationException {
    var type = Class.forName(HydrologyRuntime.class.getName() + "$" + nested);
    Constructor<?> constructor = type.getDeclaredConstructor();
    constructor.setAccessible(true);
    return constructor.newInstance();
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  static void run(ServerLevel level) throws ReflectiveOperationException {
    if (!(level.getChunkSource().getGenerator() instanceof EngineChunkGenerator generator))
      throw new AssertionError("Stream source fixture requires the engine generator");

    int x = 2048, z = 2048, y = Math.min(288, level.getMaxY() - 8);
    var first = new BlockPos(x, y, z);
    var second = first.east();
    var untouched = first.east(2);
    level.getChunkAt(first);
    require(HydrologyRuntime.write(level, first, Blocks.AIR.defaultBlockState()),
        "Could not clear first stream test outlet");
    require(HydrologyRuntime.write(level, second, Blocks.AIR.defaultBlockState()),
        "Could not clear second stream test outlet");
    var water = EngineRegistry.WATER_BLOCK.get().defaultBlockState();
    require(HydrologyRuntime.write(level, first.below(), Blocks.STONE.defaultBlockState())
        && HydrologyRuntime.write(level, second.below(), Blocks.STONE.defaultBlockState()),
        "Could not create stream test bed");
    var deficit = first.east(3);
    require(HydrologyRuntime.write(level, deficit.below(2), Blocks.STONE.defaultBlockState())
        && HydrologyRuntime.write(level, deficit.below(), water.setValue(EngineFluid.UNITS, 8))
        && HydrologyRuntime.write(level, deficit, Blocks.AIR.defaultBlockState()),
        "Could not create drained natural pool");
    require(HydrologyRuntime.write(level, first, water.setValue(EngineFluid.UNITS, 6)),
        "Could not create fractional stream test cell");
    require(HydrologyRuntime.write(level, untouched, water.setValue(EngineFluid.UNITS, 8)),
        "Could not create untouched water control");

    var data = HydrologyData.get(level);
    var aquifer = data.aquifer(x, z, 1, level.getGameTime());
    require(aquifer.canSupply(true), "Fixture aquifer cannot supply a natural waterway");
    long initialGround = aquifer.ground.water();
    TerrainEngine terrain = generator.geography();

    Object work = instance("Work");
    Object cursor = instance("Cursor");
    boolean[] candidates = (boolean[]) field(cursor.getClass(), "streamCandidate").get(cursor);
    int[] outletY = (int[]) field(cursor.getClass(), "streamOutletY").get(cursor);
    double[] surfaces = (double[]) field(cursor.getClass(), "streamSurface").get(cursor);
    long[] replenished = (long[]) field(cursor.getClass(), "replenished").get(cursor);
    candidates[0] = true;
    candidates[1] = true;
    outletY[0] = y;
    outletY[1] = y;
    surfaces[0] = y + 0.5;
    surfaces[1] = y + 0.5;

    Method region = HydrologyRuntime.class.getDeclaredMethod(
        "region", work.getClass(), TerrainEngine.class, int.class, int.class);
    region.setAccessible(true);
    Object regional = region.invoke(null, work, terrain, x, z);
    field(regional.getClass(), "natural").setInt(regional, 0);
    Method replenish = HydrologyRuntime.class.getDeclaredMethod(
        "replenishStream", ServerLevel.class, work.getClass(), TerrainEngine.class,
        cursor.getClass(), int.class, int.class, int.class);
    replenish.setAccessible(true);

    int visits = 0;
    while (HydrologyRuntime.contents(level, first, level.getBlockState(first)).water()
        < 8L * WaterAccount.SCALE && visits++ < 16) {
      java.util.Arrays.fill(replenished, Long.MIN_VALUE); replenish.invoke(null, level, work, terrain, cursor, 0, x, z);
    }
    int firstUnits = level.getBlockState(first).getValue(EngineFluid.UNITS);
    require(firstUnits == 8, "Fractional stream cell exceeded or missed its planned 8-unit surface");

    int secondVisits = 0;
    while (HydrologyRuntime.contents(level, second, level.getBlockState(second)).water()
        < 8L * WaterAccount.SCALE && secondVisits++ < 16) {
      java.util.Arrays.fill(replenished, Long.MIN_VALUE); replenish.invoke(null, level, work, terrain, cursor, 1, x + 1, z);
    }
    int secondUnits = level.getBlockState(second).getValue(EngineFluid.UNITS);
    require(secondUnits == 8, "Dry stream outlet did not fill to its planned 8-unit surface");

    long deliveredGround = aquifer.ground.water();
    for (int i = 0; i < 3; i++) {
      java.util.Arrays.fill(replenished, Long.MIN_VALUE); replenish.invoke(null, level, work, terrain, cursor, 0, x, z);
      java.util.Arrays.fill(replenished, Long.MIN_VALUE); replenish.invoke(null, level, work, terrain, cursor, 1, x + 1, z);
    }
    require(aquifer.ground.water() == deliveredGround,
        "A full natural stream channel debited additional groundwater");
    require(level.getBlockState(first).getValue(EngineFluid.UNITS) == 8
        && level.getBlockState(second).getValue(EngineFluid.UNITS) == 8,
        "A full natural stream channel exceeded its planned surface");
    require(level.getBlockState(untouched).getValue(EngineFluid.UNITS) == 8,
        "Stream replenishment modified unrelated stored water");
    long expectedDebit = 10L * WaterAccount.SCALE;
    require(initialGround - deliveredGround == expectedDebit,
        "Aquifer debit did not equal the 10 accepted stream units");

    candidates[2] = true;
    outletY[2] = y;
    surfaces[2] = y + 0.5;
    field(regional.getClass(), "natural").setInt(regional, 0);
    long beforeDeficit = aquifer.ground.water();
    java.util.Arrays.fill(replenished, Long.MIN_VALUE); replenish.invoke(null, level, work, terrain, cursor, 2, x + 3, z);
    int repaid = level.getBlockState(deficit.below()).getValue(EngineFluid.UNITS);
    require(level.getBlockState(deficit).isAir() && repaid > 8 && repaid <= 16
        && beforeDeficit - aquifer.ground.water() == (repaid - 8L) * WaterAccount.SCALE,
        "Drained natural pool was not repaid, exactly, in its deficit cell");
    long paidGround = aquifer.ground.water();
    field(regional.getClass(), "natural").setInt(regional, 0);

    require(HydrologyRuntime.write(level, first, Blocks.AIR.defaultBlockState()), "Clear metered outlet");
    field(regional.getClass(), "natural").setInt(regional, Aquifer.baseflowUnits(com.shnewbs.deepprimal.engine.HydrologyConfig.RAIN_QUANTA.get()));
    java.util.Arrays.fill(replenished, Long.MIN_VALUE); replenish.invoke(null, level, work, terrain, cursor, 0, x, z);
    require(level.getBlockState(first).isAir() && aquifer.ground.water() == paidGround,
        "Natural local head must not bypass the regional discharge quota");
    field(regional.getClass(), "natural").setInt(regional, 0);
    require(HydrologyRuntime.write(level, first, Blocks.GOLD_BLOCK.defaultBlockState()), "Build source dam");
    java.util.Arrays.fill(replenished, Long.MIN_VALUE); replenish.invoke(null, level, work, terrain, cursor, 0, x, z);
    require(level.getBlockState(first).is(Blocks.GOLD_BLOCK) && aquifer.ground.water() == paidGround,
        "Blocked natural outlet preserves edits and groundwater");
    require(HydrologyRuntime.write(level, first, Blocks.AIR.defaultBlockState()), "Reopen source dam");
    aquifer.ground.take(aquifer.capacity());
    aquifer.step(aquifer.lastTick() + 20);
    java.util.Arrays.fill(replenished, Long.MIN_VALUE); replenish.invoke(null, level, work, terrain, cursor, 0, x, z);
    require(!aquifer.canSupply(true) && level.getBlockState(first).isAir(),
        "Exhausted natural source remains dry");
    aquifer.ground.add(new WaterAccount.Parcel(aquifer.start(), 0, 0));
    for (int i = 0; i < Aquifer.WET_STEPS - 1; i++) {
      aquifer.step(aquifer.lastTick() + 20);
      java.util.Arrays.fill(replenished, Long.MIN_VALUE); replenish.invoke(null, level, work, terrain, cursor, 0, x, z);
      require(level.getBlockState(first).isAir(), "Natural source cannot skip sustained recharge");
    }
    aquifer.step(aquifer.lastTick() + 20);
    long recharge = aquifer.ground.water();
    java.util.Arrays.fill(replenished, Long.MIN_VALUE); replenish.invoke(null, level, work, terrain, cursor, 0, x, z);
    int restartedUnits = level.getBlockState(first).getValue(EngineFluid.UNITS);
    require(restartedUnits > 0 && restartedUnits <= 8
        && recharge - aquifer.ground.water() == restartedUnits * WaterAccount.SCALE,
        "Recharged high-altitude source resumes with exact paid delivery");
    long paid = aquifer.ground.water();
    replenish.invoke(null, level, work, terrain, cursor, 0, x, z);
    require(level.getBlockState(first).getValue(EngineFluid.UNITS) == restartedUnits && aquifer.ground.water() == paid,
        "Natural source accepted a second top-up inside one tick");

    Method allowance = HydrologyRuntime.class.getDeclaredMethod("streamAllowance", int.class, long.class, long.class);
    allowance.setAccessible(true);
    int quota = Aquifer.baseflowUnits(com.shnewbs.deepprimal.engine.HydrologyConfig.RAIN_QUANTA.get());
    require((int) allowance.invoke(null, quota, Long.MIN_VALUE, 5L) == quota
        && (int) allowance.invoke(null, quota, 100L, 99L) == quota
        && (int) allowance.invoke(null, quota, 100L, 119L) == quota
        && (int) allowance.invoke(null, quota, 100L, 400L) == quota, "Stream allowance must reach exactly one quota");
    for (long tick = 100, previous = 0; tick < 120; tick++) {
      int released = (int) allowance.invoke(null, quota, 100L, tick);
      require(released >= previous && released - previous <= (quota + 19) / 20 + 1 && released > 0,
          "Stream allowance is not a smooth monotonic ramp at phase " + (tick - 100));
      previous = released;
    }

    Object ordered = instance("Cursor");
    double[] orderedSurface = (double[]) field(ordered.getClass(), "streamSurface").get(ordered);
    Method candidate = ordered.getClass().getDeclaredMethod("candidate", int.class, boolean.class);
    candidate.setAccessible(true);
    double[] planned = {100.25, 104.5, 99.0, 104.5, 102.0};
    for (int i = 0; i < planned.length; i++) { orderedSurface[i] = planned[i]; candidate.invoke(ordered, i, true); }
    candidate.invoke(ordered, 4, false);
    int[] streams = (int[]) field(ordered.getClass(), "streams").get(ordered);
    int count = field(ordered.getClass(), "streamCount").getInt(ordered);
    require(count == 4 && orderedSurface[streams[0]] == 104.5 && orderedSurface[streams[1]] == 104.5
        && streams[2] == 0 && streams[3] == 2, "Planned waterway quota order is not headwater-first");

    for (long remaining : new long[] {0, 128, 256, 384, 512, 768}) {
      require(HydrologyRuntime.write(level, first, Blocks.AIR.defaultBlockState()),
          "Could not clear drought reserve outlet");
      var finite = new Aquifer(new WaterAccount.Parcel(Aquifer.STOP + remaining, 8191, 16381),
          WaterAccount.Parcel.EMPTY, Aquifer.WET_STEPS, true, level.getGameTime());
      var initial = finite.ground.snapshot();
      // A deeply drained outlet must not draw the protected reserve between environmental steps.
      for (int attempt = 0; attempt < 16; attempt++)
        HydrologyRuntime.emit(level, first, finite, Aquifer.naturalDatum(y + 2.5), 16);
      var placed = HydrologyRuntime.contents(level, first, level.getBlockState(first));
      require(placed.water() == remaining / WaterAccount.SCALE * WaterAccount.SCALE
          && finite.ground.water() >= Aquifer.STOP
          && finite.ground.water() + placed.water() == initial.water()
          && finite.ground.snapshot().salt() + placed.salt() == initial.salt()
          && finite.ground.snapshot().sediment() + placed.sediment() == initial.sediment(),
          "Native deficit-cell drought reserve or exact chemistry accounting failed: " + remaining);
      finite.step(finite.lastTick() + 20);
      require(!HydrologyRuntime.emit(level, first, finite, Aquifer.naturalDatum(y + 2.5), 16),
          "Drought outlet spent a sub-unit reserve remainder");
    }
    System.out.println("STREAM_DROUGHT_RESERVE_NATIVE_PASS cases=6 attemptsPerCase=16"
        + " deficitCell=true exactWaterSaltSediment=true noStepOverspend=true");

    System.out.println("STREAM_SOURCE_NATIVE_PASS firstOutletUnits=" + firstUnits
        + " secondOutletUnits=" + secondUnits + " noOverfill=true"
        + " noRepeatDebit=true unrelatedWaterPreserved=true aquiferDebit=" + expectedDebit
        + " firstVisits=" + visits + " secondVisits=" + secondVisits
        + " outletY=" + y + " regionalQuota=true damEdits=true finiteDrought=true sustainedRecharge=true intervalCadence=true smoothRelease=true headwaterFirst=true deficitCellUnits=" + repaid);
  }

  private StreamSourceNativeChecks() {}
}
