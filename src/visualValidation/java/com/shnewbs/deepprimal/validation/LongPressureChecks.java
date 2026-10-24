package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.EngineRegistry;
import com.shnewbs.deepprimal.engine.WaterRuntime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Opt-in native diagnosis, excluded from release jars. Reproduction is not solver acceptance. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class LongPressureChecks {
  private final LinkedHashSet<ChunkPos> forced = new LinkedHashSet<>();
  private List<BlockPos> tube;
  private List<BlockPos> secondTube;
  private int ticks, quantity, firstSearchVisits, secondQuantity;
  private int peakJobs, peakNodes, peakVisits, idleSamples, baseY = 500;
  private boolean headFrontierRetained;
  private ServerLevel testLevel;
  private final String mode = System.getenv().getOrDefault("DEEPPRIMAL_LONG_PRESSURE_MODE", "reproduce");
  private boolean done, gapUnloaded;
  private final int offset = mode.equals("unload") ? 4096 : 0;

  public LongPressureChecks() { NeoForge.EVENT_BUS.register(this); }

  private static void check(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  private List<BlockPos> cells(int offset, int width) {
    var cells = new ArrayList<BlockPos>();
    for (int x = 0; x <= width; x++) cells.add(new BlockPos(offset + x, baseY, 8));
    for (int y = baseY + 1; y <= baseY + 5; y++) {
      cells.add(new BlockPos(offset, y, 8));
      cells.add(new BlockPos(offset + width, y, 8));
    }
    return cells;
  }

  private void load(ServerLevel level, List<BlockPos> cells) {
    for (BlockPos p : cells) {
      var chunk = new ChunkPos(p.getX() >> 4, p.getZ() >> 4);
      if (forced.add(chunk)) level.setChunkForced(chunk.x(), chunk.z(), true);
      level.getChunk(p);
    }
  }

  private List<BlockPos> create(ServerLevel level, int offset, int width, int rightTop) {
    var cells = cells(offset, width);
    load(level, cells);
    for (BlockPos p : cells) {
      for (Direction direction : Direction.values())
        level.setBlock(p.relative(direction), Blocks.STONE.defaultBlockState(), 2 | 16);
    }
    for (BlockPos p : cells) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
    for (BlockPos p : cells)
      if (p.getY() == baseY || (p.getX() == offset && p.getY() <= baseY + 3)
          || (p.getX() == offset + width && p.getY() <= rightTop))
        level.setBlock(p, EngineRegistry.WATER_BLOCK.get().defaultBlockState(), 2 | 16);
    return cells;
  }

  @SubscribeEvent
  public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    if (!Boolean.parseBoolean(System.getenv("DEEPPRIMAL_LONG_PRESSURE_CHECKS"))) return;
    var level = (mode.equals("idle") || mode.equals("reopen"))
        ? event.getServer().getLevel(net.minecraft.world.level.Level.END) : event.getServer().overworld();
    check(level != null, "Fixture dimension exists");
    testLevel = level;
    if (mode.equals("idle") || mode.equals("reopen")) baseY = 200;
    check(level.getMaxY() > baseY + 6, "Fixture fits its dimension");
    if (mode.equals("reopen")) {
      // Read saved blocks without reconstructing the fixtures or applying new water.
      tube = cells(0, 256); secondTube = cells(2048, 256);
      load(level, tube); load(level, secondTube);
      quantity = tube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum();
      secondQuantity = secondTube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum();
      check(quantity == 4192 && secondQuantity == 4192, "Saved quantities survive a new process");
      check(WaterRuntime.units(level, new BlockPos(0, baseY + 3, 8)) == 8
          && WaterRuntime.units(level, new BlockPos(256, baseY + 3, 8)) == 8
          && WaterRuntime.units(level, new BlockPos(2048, baseY + 3, 8)) == 8
          && WaterRuntime.units(level, new BlockPos(2304, baseY + 3, 8)) == 8,
          "Saved communicating surfaces survive reopen");
      return;
    }
    var shortTube = create(level, 512, 4, baseY + 1);
    int before = shortTube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum();
    check(before == 144, "Short control initial volume");
    for (int n = 0; n < 4; n++) WaterRuntime.tick(level, new BlockPos(512, baseY + 3, 8));
    check(WaterRuntime.units(level, new BlockPos(516, baseY + 2, 8)) == 16,
        "Short communicating-vessel control must rise");
    check(shortTube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum() == before,
        "Short control conservation");
    System.out.println("SHORT_PRESSURE_CONTROL_NATIVE_PASS units=144 raisedSurface=true conserved=true");
    tube = create(level, offset, 256, baseY + 2);
    if (mode.equals("heads")) {
      level.setBlock(new BlockPos(offset, baseY + 3, 8), EngineRegistry.WATER_BLOCK.get()
          .defaultBlockState().setValue(com.shnewbs.deepprimal.engine.EngineFluid.UNITS, 15), 2 | 16);
      level.setBlock(new BlockPos(offset + 256, baseY + 2, 8), EngineRegistry.WATER_BLOCK.get()
          .defaultBlockState().setValue(com.shnewbs.deepprimal.engine.EngineFluid.UNITS, 15), 2 | 16);
    }
    quantity = tube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum();
    check(quantity == 262 * 16 - (mode.equals("heads") ? 2 : 0), "Known long-bend volume");
    int beforeSearch = WaterRuntime.diagnostics(level).pressureVisits();
    WaterRuntime.tick(level, new BlockPos(offset, baseY + 3, 8));
    firstSearchVisits = WaterRuntime.diagnostics(level).pressureVisits() - beforeSearch;
    check(firstSearchVisits == 128, "Expected one bounded 128-node native turn");
    if (!mode.equals("reproduce")) {
      check(WaterRuntime.pressureDiagnostics(level).jobs() > 0,
          "First slice must retain its frontier");
      if (mode.equals("heads")) {
        var beforeHeadChange = WaterRuntime.pressureDiagnostics(level);
        // Both cells stay wet. Shift one unit without adding/removing a graph node.
        level.setBlock(new BlockPos(offset, baseY + 3, 8), EngineRegistry.WATER_BLOCK.get()
            .defaultBlockState().setValue(com.shnewbs.deepprimal.engine.EngineFluid.UNITS, 14), 2 | 16);
        level.setBlock(new BlockPos(offset + 256, baseY + 2, 8), EngineRegistry.WATER_BLOCK.get()
            .defaultBlockState().setValue(com.shnewbs.deepprimal.engine.EngineFluid.UNITS, 16), 2 | 16);
        WaterRuntime.tick(level, new BlockPos(offset, baseY + 3, 8));
        var resumed = WaterRuntime.pressureDiagnostics(level);
        check(resumed.restarts() == beforeHeadChange.restarts()
            && resumed.retainedNodes() > beforeHeadChange.retainedNodes(),
            "Positive-volume changes must preserve and extend the native frontier");
        check(tube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum() == quantity,
            "Head-change control conserves volume");
        headFrontierRetained = true;
      } else if (mode.equals("edit")) {
        // Already visited prefix: continuing from the old frontier would bypass this wall.
        level.setBlock(new BlockPos(8, baseY, 8), Blocks.STONE.defaultBlockState(), 2 | 16);
        quantity -= 16;
      } else if (mode.equals("unload")) {
        // Release a band behind the frontier, far from the spawn region and forced endpoints.
        for (int x = (offset >> 4) + 3; x <= (offset >> 4) + 13; x++)
          level.setChunkForced(x, 0, false);
        var source = level.getChunkSource();
        long deadline = System.nanoTime() + 5_000_000_000L;
        for (int turn = 0; turn < 500 && System.nanoTime() < deadline; turn++) {
          source.tick(() -> true, false);
          for (int work = 0; work < 64 && source.pollTask(); work++) {}
          if (source.getChunk((offset >> 4) + 6, 0,
              net.minecraft.world.level.chunk.status.ChunkStatus.FULL, false) == null) {
            gapUnloaded = true;
            break;
          }
          Thread.sleep(1);
        }
        check(gapUnloaded, "The fixture must actually unload an already visited chunk");
        System.out.println("PRESSURE_NATIVE_GAP_UNLOADED behindFrontier=true missingChunk="
            + ((offset >> 4) + 6) + ",0");
      } else if ((mode.equals("fixed") || mode.equals("idle") || mode.equals("reopen"))) {
        secondTube = create(level, 2048, 256, baseY + 2);
        secondQuantity = secondTube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum();
        WaterRuntime.tick(level, new BlockPos(2048, baseY + 3, 8));
      } else throw new AssertionError("Unknown native pressure mode: " + mode);
    }
  }

  @SubscribeEvent
  public void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) throws Exception {
    if (tube == null || done) return;
    var level = testLevel;
    var pressure = WaterRuntime.pressureDiagnostics(level);
    peakJobs = Math.max(peakJobs, pressure.jobs());
    peakNodes = Math.max(peakNodes, pressure.retainedNodes());
    peakVisits = Math.max(peakVisits, WaterRuntime.diagnostics(level).pressureVisits());
    check(pressure.jobs() <= 16 && pressure.retainedNodes() <= 16 * 8192,
        "Pressure residency bounds");
    check(pressure.maxSliceVisits() <= 128 && peakVisits <= 12288,
        "Per-turn and per-host-tick pressure visit bounds");
    ticks++;
    if ((mode.equals("idle") || mode.equals("reopen")) && ticks >= 100) {
      check(WaterRuntime.queued(level) == 0 && pressure.jobs() == 0
          && pressure.retainedNodes() == 0 && pressure.watchedChunks() == 0,
          "Settled fixtures release work, frontiers and chunk watches");
      check(WaterRuntime.diagnostics(level).processed() == 0
          && WaterRuntime.diagnostics(level).pressureVisits() == 0,
          "Settled fixture dimension must do no transport/search work");
      idleSamples++;
    }
    if (ticks < 120) return;
    done = true;
    if (mode.equals("unload")) {
      check(gapUnloaded && level.getChunkSource().getChunk((offset >> 4) + 6, 0,
          net.minecraft.world.level.chunk.status.ChunkStatus.FULL, false) == null,
          "Continuation must never force-load its missing witness chunk");
      check(WaterRuntime.units(level, new BlockPos(offset + 256, baseY + 3, 8)) == 0,
          "An unloaded connecting passage cannot transmit pressure");
      // Only the test harness reloads after the no-force-load assertion, to audit saved quantity.
      for (BlockPos p : tube) level.getChunk(p);
    }
    int after = tube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum();
    int outlet = WaterRuntime.units(level, new BlockPos(offset + 256, baseY + 3, 8));
    check(after == quantity, "Long sealed bend conservation");
    String label;
    if ((mode.equals("fixed") || mode.equals("idle") || mode.equals("reopen") || mode.equals("heads"))) {
      int expectedSurface = mode.equals("heads") ? 7 : 8;
      check(outlet == expectedSurface && WaterRuntime.units(level, new BlockPos(offset, baseY + 3, 8)) == expectedSurface,
          "Long communicating surfaces must equalize, not restart the old prefix");
      if (secondTube != null) {
      check(secondTube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum() == secondQuantity,
          "Second independent bend conservation");
      check(WaterRuntime.units(level, new BlockPos(2304, baseY + 3, 8)) == 8
          && WaterRuntime.units(level, new BlockPos(2048, baseY + 3, 8)) == 8,
          "Both independent components must progress fairly");
      }
      if (mode.equals("heads")) check(headFrontierRetained, "Head-change frontier check executed");
      label = mode.equals("heads") ? "LONG_PRESSURE_HEAD_CHANGE_NATIVE_PASS" : mode.equals("idle") ? "LONG_PRESSURE_IDLE_NATIVE_PASS"
          : mode.equals("reopen") ? "LONG_PRESSURE_REOPEN_NATIVE_PASS"
          : "LONG_PRESSURE_CONTINUATION_NATIVE_PASS";
      if (mode.equals("idle")) check(pressure.coalesced() > 0,
          "Completed communicating components must coalesce duplicate native requests");
    } else {
      check(outlet == 0 && WaterRuntime.units(level, new BlockPos(offset, baseY + 3, 8)) == 16,
          "Disconnected/reproduction case must not raise the remote outlet");
      if (mode.equals("edit")) {
        check(level.getBlockState(new BlockPos(8, baseY, 8)).is(Blocks.STONE), "Player wall preserved");
        check(pressure.restarts() > 0, "Silent chunk mutation must invalidate retained search");
        label = "LONG_PRESSURE_EDIT_NATIVE_PASS";
      } else if (mode.equals("unload")) {
        check(pressure.restarts() > 0, "Actual chunk unload must invalidate retained search");
        label = "LONG_PRESSURE_UNLOAD_NATIVE_PASS";
      } else label = "LONG_PRESSURE_CUTOFF_NATIVE_REPRODUCED";
    }
    String result = label + " ticks=" + ticks + " initialWaterCells="
        + (mode.equals("reopen") ? 263 : 262) + " initialUnits=" + quantity
        + " finalUnits=" + after + " outletUnits=" + outlet + " forcedChunks=" + forced.size()
        + " firstSearchVisits=" + firstSearchVisits + " maxSliceVisits=" + pressure.maxSliceVisits()
        + " peakJobs=" + peakJobs + " peakRetainedNodes=" + peakNodes
        + " peakTickPressureVisits=" + peakVisits + " restarts=" + pressure.restarts()
        + " coalesced=" + pressure.coalesced() + " completed=" + pressure.completed() + " saturated=" + pressure.saturated()
        + " headFrontierRetained=" + headFrontierRetained
        + " headRefreshes=" + pressure.headRefreshes() + " headDeferrals=" + pressure.headDeferrals()
        + " idleSamples=" + idleSamples + " finalWatchedChunks=" + pressure.watchedChunks()
        + " actualGapUnloaded=" + gapUnloaded + " independentComponents=" + (secondTube == null ? 1 : 2)
        + " conserved=true solverAcceptance=" + !mode.equals("reproduce") + "\n";
    var output = Path.of(System.getenv("DEEPPRIMAL_LONG_PRESSURE_RECORD"));
    Files.createDirectories(output.toAbsolutePath().getParent());
    Files.writeString(output, result);
    System.out.print(result);
    for (ChunkPos chunk : forced) level.setChunkForced(chunk.x(), chunk.z(), false);
    event.getServer().halt(false);
  }

  @SubscribeEvent
  public void stopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
    if (System.getenv("DEEPPRIMAL_LAKE_RECORD") != null)
      System.out.println("LAKE_PRESSURE_SEARCH_SUMMARY "
          + WaterRuntime.pressureDiagnostics(event.getServer().overworld()));
  }
}
