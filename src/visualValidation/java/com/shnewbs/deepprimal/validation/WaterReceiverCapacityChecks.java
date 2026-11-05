package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.engine.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Private native fault boundary: valid accounts may have no constituent room left. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class WaterReceiverCapacityChecks {
  public WaterReceiverCapacityChecks() { NeoForge.EVENT_BUS.register(this); }
  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
  private static final WaterAccount.Parcel SOURCE = new WaterAccount.Parcel(4096, 4096, 4096);
  private static final long MAX_MASS = WaterAccount.MAX * 1024;
  private interface Attempt { boolean run() throws Exception; }
  private static final ArrayList<String> observations = new ArrayList<>();
  private static int failures;
  private static void reject(String name, Attempt attempt, java.util.function.Supplier<String> snapshot) {
    String before = snapshot.get();
    boolean accepted = false;
    Throwable failure = null;
    try { accepted = attempt.run(); }
    catch (Exception problem) { failure = problem; }
    String after = snapshot.get();
    boolean correct = failure == null && !accepted && before.equals(after);
    if (!correct) failures++;
    observations.add(name + " correct=" + correct + " accepted=" + accepted
        + " failure=" + (failure == null ? "none" : failure.toString())
        + " before=" + before + " after=" + after);
  }
  private static void source(ServerLevel level, BlockPos cell) {
    level.getChunkAt(cell);
    level.setBlock(cell, Blocks.AIR.defaultBlockState(), 2);
    require(HydrologyRuntime.place(level, cell, SOURCE), "Fixture placement");
  }
  private static WaterAccount.Parcel contents(ServerLevel level, BlockPos cell) {
    return HydrologyRuntime.contents(level, cell, level.getBlockState(cell));
  }
  @SubscribeEvent
  public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    String output = System.getenv("DEEPPRIMAL_RECEIVER_CAPACITY_RECORD");
    if (output == null) return;
    var level = event.getServer().overworld();
    observations.clear(); failures = 0;
    boolean baseline = Boolean.parseBoolean(System.getenv("DEEPPRIMAL_RECEIVER_EXPECT_FAILURE"));
    int exactCapacityControls = 0;
    for (boolean salt : new boolean[] {true, false}) {
      var cell = new BlockPos(8, 500, 8);
      source(level, cell);
      var aquifer = new Aquifer(new WaterAccount.Parcel(0, salt ? MAX_MASS : 0, salt ? 0 : MAX_MASS),
          WaterAccount.Parcel.EMPTY, 0, false, level.getGameTime());
      reject("infiltration-" + (salt ? "salt" : "sediment"),
          () -> HydrologyRuntime.infiltrate(level, cell, aquifer, 4),
          () -> contents(level, cell) + ":" + aquifer.ground.snapshot());
      if (!baseline) {
        var room = new Aquifer(new WaterAccount.Parcel(0, salt ? MAX_MASS - 1024 : 0,
            salt ? 0 : MAX_MASS - 1024), WaterAccount.Parcel.EMPTY, 0, false, level.getGameTime());
        require(HydrologyRuntime.infiltrate(level, cell, room, 4), "Exact constituent room accepts infiltration");
        require(contents(level, cell).equals(new WaterAccount.Parcel(3072, 3072, 3072))
            && room.ground.snapshot().equals(new WaterAccount.Parcel(1024, salt ? MAX_MASS : 1024,
                salt ? 1024 : MAX_MASS)), "Exact infiltration ledger");
        reject("infiltration-after-full-" + salt, () -> HydrologyRuntime.infiltrate(level, cell, room, 4),
            () -> contents(level, cell) + ":" + room.ground.snapshot());
        exactCapacityControls++;
      }
    }
    var data = HydrologyData.get(level);
    data.ocean.take(WaterAccount.MAX);
    require(data.ocean.add(new WaterAccount.Parcel(0, MAX_MASS, MAX_MASS)), "Ocean fixture");
    var river = new BlockPos(24, 96, 8);
    source(level, river);
    var sea = river.below();
    level.setBlock(sea, EngineRegistry.WATER_BLOCK.get().defaultBlockState().setValue(EngineWaterBlock.OCEAN, true), 2);
    reject("ocean", () -> HydrologyRuntime.oceanSink(level, river, sea),
        () -> contents(level, river) + ":" + data.ocean.snapshot());
    if (!baseline) {
      require(data.ocean.add(new WaterAccount.Parcel(1, 0, 0)), "Clear disposable fixture account");
      data.ocean.take(WaterAccount.MAX);
      require(data.ocean.add(new WaterAccount.Parcel(0, MAX_MASS - 1024, MAX_MASS - 1024)), "Exact ocean room");
      require(HydrologyRuntime.oceanSink(level, river, sea), "Exact constituent room accepts ocean intake");
      require(contents(level, river).equals(new WaterAccount.Parcel(3072, 3072, 3072))
          && data.ocean.snapshot().equals(new WaterAccount.Parcel(1024, MAX_MASS, MAX_MASS)), "Exact ocean ledger");
      reject("ocean-after-full", () -> HydrologyRuntime.oceanSink(level, river, sea),
          () -> contents(level, river) + ":" + data.ocean.snapshot());
      exactCapacityControls++;
    }

    var a = new BlockPos(40, 500, 8);
    var b = a.below();
    source(level, a);
    level.setBlock(b, EngineRegistry.WATER_BLOCK.get().defaultBlockState().setValue(EngineFluid.UNITS, 8), 2);
    var store = HydrologyRuntime.class.getDeclaredMethod("store", ServerLevel.class, BlockPos.class, WaterAccount.Parcel.class);
    store.setAccessible(true);
    store.invoke(null, level, b, new WaterAccount.Parcel(2048, MAX_MASS, MAX_MASS));
    var transfer = WaterRuntime.class.getDeclaredMethod("transfer", ServerLevel.class, BlockPos.class,
        BlockPos.class, WaterTransport.Exchange.class, int.class);
    transfer.setAccessible(true);
    reject("cell-transfer", () -> (boolean) transfer.invoke(null, level, a, b, WaterTransport.exchange(16, 8, true), 16),
        () -> contents(level, a) + ":" + contents(level, b));
    if (!baseline) {
      store.invoke(null, level, b, new WaterAccount.Parcel(2048, MAX_MASS - 2048, MAX_MASS - 2048));
      require((boolean) transfer.invoke(null, level, a, b, WaterTransport.exchange(16, 8, true), 16),
          "Exact constituent room accepts cell transfer");
      require(contents(level, a).equals(new WaterAccount.Parcel(2048, 2048, 2048))
          && contents(level, b).equals(new WaterAccount.Parcel(4096, MAX_MASS, MAX_MASS)), "Exact cell transfer ledger");
      exactCapacityControls++;
    }
    String record = String.join("\n", observations) + "\nfailures=" + failures + "\n";
    Files.writeString(Path.of(output), record);
    System.out.print(record);
    require(failures == (baseline ? 4 : 0), "Expected " + (baseline ? "four reproduced failures" : "atomic rejections"));
    System.out.println(baseline ? "WATER_RECEIVER_EXPECTED_REPRO failures=4" : "WATER_RECEIVER_CAPACITY_PASS rejections="
        + observations.size() + " exactCapacityControls=" + exactCapacityControls + " exactWaterSaltSediment=true");
  }
}
