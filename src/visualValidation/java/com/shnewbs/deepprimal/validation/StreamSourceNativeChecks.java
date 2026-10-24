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

    int x = 2048, z = 2048, y = level.getMinY();
    var first = new BlockPos(x, y, z);
    var second = first.east();
    var untouched = first.east(2);
    level.getChunkAt(first);
    require(HydrologyRuntime.write(level, first, Blocks.AIR.defaultBlockState()),
        "Could not clear first stream test outlet");
    require(HydrologyRuntime.write(level, second, Blocks.AIR.defaultBlockState()),
        "Could not clear second stream test outlet");
    var water = EngineRegistry.WATER_BLOCK.get().defaultBlockState();
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
    field(regional.getClass(), "discharged").setInt(regional, 0);
    Method replenish = HydrologyRuntime.class.getDeclaredMethod(
        "replenishStream", ServerLevel.class, work.getClass(), TerrainEngine.class,
        cursor.getClass(), int.class, int.class, int.class);
    replenish.setAccessible(true);

    int visits = 0;
    while (HydrologyRuntime.contents(level, first, level.getBlockState(first)).water()
        < 8L * WaterAccount.SCALE && visits++ < 16) {
      replenish.invoke(null, level, work, terrain, cursor, 0, x, z);
    }
    int firstUnits = level.getBlockState(first).getValue(EngineFluid.UNITS);
    require(firstUnits == 8, "Fractional stream cell exceeded or missed its planned 8-unit surface");

    int secondVisits = 0;
    while (HydrologyRuntime.contents(level, second, level.getBlockState(second)).water()
        < 8L * WaterAccount.SCALE && secondVisits++ < 16) {
      replenish.invoke(null, level, work, terrain, cursor, 1, x + 1, z);
    }
    int secondUnits = level.getBlockState(second).getValue(EngineFluid.UNITS);
    require(secondUnits == 8, "Dry stream outlet did not fill to its planned 8-unit surface");

    long deliveredGround = aquifer.ground.water();
    for (int i = 0; i < 3; i++) {
      replenish.invoke(null, level, work, terrain, cursor, 0, x, z);
      replenish.invoke(null, level, work, terrain, cursor, 1, x + 1, z);
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

    System.out.println("STREAM_SOURCE_NATIVE_PASS firstOutletUnits=" + firstUnits
        + " secondOutletUnits=" + secondUnits + " noOverfill=true"
        + " noRepeatDebit=true unrelatedWaterPreserved=true aquiferDebit=" + expectedDebit
        + " firstVisits=" + visits + " secondVisits=" + secondVisits);
  }

  private StreamSourceNativeChecks() {}
}
