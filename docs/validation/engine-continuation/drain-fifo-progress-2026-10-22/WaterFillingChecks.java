package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Real per-tick settling: thin films, wash-through plants, dormant interiors and queue fairness.
 */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class WaterFillingChecks {
  private boolean started, done;
  private int baseY, ticks, previousSink, previousBasin, lastProgress, maxGap, progressTicks;
  private int firstFilledTick;
  private final List<BlockPos> rapid = new ArrayList<>();
  private final List<BlockPos> film = new ArrayList<>(),
      plant = new ArrayList<>(),
      basin = new ArrayList<>();
  private final WaterInteriorWake wake = new WaterInteriorWake();

  public WaterFillingChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  private static void walls(ServerLevel l, List<BlockPos> cells) {
    for (BlockPos p : cells)
      for (Direction d : Direction.values())
        l.setBlock(p.relative(d), Blocks.STONE.defaultBlockState(), 2 | 16);
    for (BlockPos p : cells) l.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
  }

  private static void water(ServerLevel l, BlockPos p, int units) {
    l.setBlock(
        p,
        EngineRegistry.WATER_BLOCK.get().defaultBlockState().setValue(EngineFluid.UNITS, units),
        2 | 16);
  }

  @SubscribeEvent
  public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
    if (!Boolean.parseBoolean(System.getenv("DEEPPRIMAL_FILL_CHECKS"))) return;
    ServerLevel l = event.getServer().overworld();
    baseY = Math.min(500, l.getMaxY() - 128);
    // A new falling cell must advance through three substeps in the first host update.
    for (int y = baseY + 30; y <= baseY + 33; y++) rapid.add(new BlockPos(64, y, 8));
    walls(l, rapid);
    water(l, rapid.getLast(), 16);
    // Budget saturation precedes all live cells; FIFO work must eventually reach them.
    for (int x = 0; x < 48; x++)
      for (int z = 0; z < 48; z++) {
        l.getChunk(x >> 4, z >> 4);
        WaterRuntime.activate(l, new BlockPos(x, baseY + 100, z));
      }
    // Competing settled films must not delay a moving basin's last unit behind pressure scans.
    var stable = new ArrayList<BlockPos>();
    for (int x = 96; x < 128; x++)
      for (int z = 0; z < 32; z++) stable.add(new BlockPos(x, baseY, z));
    walls(l, stable);
    for (BlockPos p : stable) water(l, p, 1);
    for (int x = 0; x <= 8; x++) film.add(new BlockPos(x, baseY, 8));
    film.add(new BlockPos(8, baseY - 1, 8));
    walls(l, film);
    for (int x = 0; x < 8; x++) water(l, new BlockPos(x, baseY, 8), 1);
    // Tall grass is replaceable space, not a watertight wall.
    for (int y = baseY; y <= baseY + 2; y++) plant.add(new BlockPos(24, y, 8));
    walls(l, plant);
    l.setBlock(new BlockPos(24, baseY, 8), Blocks.SHORT_GRASS.defaultBlockState(), 2 | 16);
    water(l, new BlockPos(24, baseY + 2, 8), 16);
    // Two-cell basin and 32-unit reservoir. No external source is injected during filling.
    basin.add(new BlockPos(40, baseY, 8));
    basin.add(new BlockPos(41, baseY, 8));
    basin.add(new BlockPos(40, baseY + 1, 8));
    basin.add(new BlockPos(40, baseY + 2, 8));
    walls(l, basin);
    water(l, new BlockPos(40, baseY + 1, 8), 16);
    water(l, new BlockPos(40, baseY + 2, 8), 16);
    // Set section data directly to reproduce generated interior water with no scheduled tick.
    var dormant = new BlockPos(56, baseY + 20, 8);
    List<BlockPos> shaft = List.of(dormant, dormant.below(), dormant.below(2));
    walls(l, shaft);
    var chunk = l.getChunk(3, 0);
    chunk
        .getSection(chunk.getSectionIndex(baseY + 20))
        .setBlockState(
            8, (baseY + 20) & 15, 8, EngineRegistry.WATER_BLOCK.get().defaultBlockState());
    wake.enqueue(l, 3, 0);
    started = true;
  }

  @SubscribeEvent
  public void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) throws Exception {
    if (!started || done) return;
    ServerLevel l = event.getServer().overworld();
    ticks++;
    var bounds = WaterRuntime.drainDiagnostics(l);
    require(bounds.jobs() <= 16 && bounds.retainedNodes() <= 16 * 8192
        && bounds.waitingJobs() <= 128 && bounds.waitingLinks() <= 1024
        && bounds.readyProbes() <= 16 && bounds.readyPending() <= 1024
        && bounds.pending() <= 65536 && bounds.admissionVisits() <= 3072
        && bounds.maxSliceVisits() <= 128 && WaterRuntime.diagnostics(l).drainVisits() <= 6144,
        "Retained, admission, missing-chunk and per-host drain bounds");
    var field = WaterRuntime.class.getDeclaredField("BUDGETS"); field.setAccessible(true);
    var budget = ((Map<?, ?>) field.get(null)).get(l);
    var jobsField = budget.getClass().getDeclaredField("drains"); jobsField.setAccessible(true);
    var chunksField = budget.getClass().getDeclaredField("drainChunks"); chunksField.setAccessible(true);
    var resident = new HashSet<>(((Map<?, ?>) jobsField.get(budget)).values());
    for (Object watchers : ((Map<?, ?>) chunksField.get(budget)).values())
      for (Object watched : (Collection<?>) watchers)
        require(resident.contains(watched), "A temporary drain witness must release every chunk watch");

    wake.step(l);
    require(
        WaterRuntime.units(l, new BlockPos(56, baseY + 18, 8))
                + WaterRuntime.units(l, new BlockPos(56, baseY + 19, 8))
                + WaterRuntime.units(l, new BlockPos(56, baseY + 20, 8))
            == 16,
        "Dormant shaft conserves water");
    if (ticks == 1)
      require(
          WaterRuntime.units(l, rapid.getFirst()) == 16,
          "Three transport substeps continue the falling front within one host tick");
    require(
        rapid.stream().mapToInt(p -> WaterRuntime.units(l, p)).sum() == 16,
        "Substeps conserve the falling column");
    int sink = WaterRuntime.units(l, new BlockPos(8, baseY - 1, 8));
    int filled =
        WaterRuntime.units(l, new BlockPos(40, baseY, 8))
            + WaterRuntime.units(l, new BlockPos(41, baseY, 8));
    require(
        film.stream().mapToInt(p -> WaterRuntime.units(l, p)).sum() == 8,
        "Thin film conserves eight units");
    require(
        plant.stream().mapToInt(p -> WaterRuntime.units(l, p)).sum() == 16,
        "Plant wash-through conserves sixteen units");
    require(
        basin.stream().mapToInt(p -> WaterRuntime.units(l, p)).sum() == 32,
        "Filling reservoir conserves thirty-two units");
    require(sink >= previousSink && filled >= previousBasin, "Lower basins fill monotonically");
    if (filled > previousBasin) {
      if (lastProgress > 0) maxGap = Math.max(maxGap, ticks - lastProgress);
      lastProgress = ticks;
      progressTicks++;
    }
    if (filled == 32 && firstFilledTick == 0) firstFilledTick = ticks;
    if (sink > previousSink) System.out.println("FILL_FLOW_PROGRESS tick=" + ticks + " sink=" + sink);
    previousSink = sink;
    previousBasin = filled;
    if (ticks >= 120) {
      if (Boolean.parseBoolean(System.getenv("DEEPPRIMAL_FILL_TRACE"))) {
        Object drains;
        try { drains = WaterRuntime.class.getMethod("drainDiagnostics", ServerLevel.class).invoke(null, l); }
        catch (NoSuchMethodException absentOnAcceptedBase) { drains = "unavailable-on-base"; }
        System.out.println("FILL_FAILURE_STATE sink=" + sink + " pending=" + WaterRuntime.queued(l)
            + " diagnostics=" + WaterRuntime.diagnostics(l) + " drains=" + drains);
      }
      require(sink == 8, "Thin film reaches the lower outlet without stopping");
      require(
          WaterRuntime.units(l, new BlockPos(24, baseY, 8)) == 16, "Grass cannot suspend water");
      require(
          filled == 32 && progressTicks >= 1 && maxGap <= 2,
          "Basin fills across consecutive updates, with no long work-budget pauses: filled="
              + filled
              + " progressTicks="
              + progressTicks
              + " maxGap="
              + maxGap
              + " cells="
              + basin.stream().map(p -> p + "=" + WaterRuntime.units(l, p)).toList());
      require(
          WaterRuntime.units(l, new BlockPos(56, baseY + 18, 8)) == 16,
          "Dormant interior water falls after loaded-only activation: bottom="
              + WaterRuntime.units(l, new BlockPos(56, baseY + 18, 8))
              + " top="
              + WaterRuntime.units(l, new BlockPos(56, baseY + 20, 8)));
      BlockPos missing = new BlockPos(3_200_000, baseY, 3_200_000);
      WaterRuntime.activate(l, missing);
      wake.enqueue(l, 200000, 200000);
      wake.step(l);
      require(
          l.getChunkSource()
                  .getChunk(
                      200000,
                      200000,
                      net.minecraft.world.level.chunk.status.ChunkStatus.FULL,
                      false)
              == null,
          "No missing chunk is requested");
      Path out = Path.of(System.getenv("DEEPPRIMAL_FILL_RECORD"));
      Files.createDirectories(out.getParent());
      System.out.println("FILL_FINAL_BOUNDS " + WaterRuntime.drainDiagnostics(l));
      String result =
          "WATER_FILLING_NATIVE_PASS ticks="
              + ticks
              + " nominalSolverHz="
              + WaterRuntime.SUBSTEPS * 20
              + " firstTickFall=3 basinFilledTick="
              + firstFilledTick
              + " thinFilm=8 plant=16 basin=32 interiorFall=true progressTicks="
              + progressTicks
              + " maxProgressGap="
              + maxGap
              + " competingStableCells=1024 conserved=true noForceLoad=true pending="
              + WaterRuntime.queued(l)
              + "\n";
      Files.writeString(out, result);
      System.out.print(result);
      done = true;
      event.getServer().halt(false);
    }
  }
}
