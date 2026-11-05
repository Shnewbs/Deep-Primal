package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Closed native fixtures: no emitters or open mass boundaries; opt-in and never packaged. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class ClosedWaterChecks {
  private final String mode = System.getenv("DEEPPRIMAL_CLOSED_WATER_MODE");
  private final List<List<BlockPos>> scenes = new ArrayList<>();
  private final LinkedHashSet<ChunkPos> forced = new LinkedHashSet<>();
  private final List<Long> durations = new ArrayList<>();
  private ServerLevel level;
  private int ticks, target, idleSamples, peakPending, firstVisits;
  private int peakDrainJobs, peakDrainNodes, peakDrainVisits, maxDrainSlice;
  private long before, totalDrainVisits, totalPressureVisits, totalTransfers;
  private int firstSettledTick, basinIdleSamples;
  private boolean gapUnloaded, gapReloaded;
  private final java.util.HashSet<Integer> physicallyUnloaded = new java.util.HashSet<>();

  public ClosedWaterChecks() { NeoForge.EVENT_BUS.register(this); }
  private static void check(boolean b, String text) { if (!b) throw new AssertionError(text); }
  private void load(BlockPos p) {
    var chunk = new ChunkPos(p.getX() >> 4, p.getZ() >> 4);
    if (forced.add(chunk)) { level.getChunk(p); level.setChunkForced(chunk.x(), chunk.z(), true); }
  }
  private void put(int x, int y, int z, boolean air) {
    level.setBlock(new BlockPos(x, y, z), (air ? Blocks.AIR : Blocks.STONE).defaultBlockState(), 2 | 16);
  }
  private List<BlockPos> tube(int offset, int length, boolean restore) {
    var cells = new ArrayList<BlockPos>();
    for (int x = -1; x <= length + 1; x++) load(new BlockPos(offset + x, 200, 8));
    for (int x = 0; x <= length; x++) cells.add(new BlockPos(offset + x, 200, 8));
    cells.add(new BlockPos(offset + length, 199, 8));
    cells.add(new BlockPos(offset + length, 198, 8));
    if (restore) return cells;
    for (int x = -1; x <= length + 1; x++)
      for (int y = 197; y <= 201; y++)
        for (int z = 7; z <= 9; z++) put(offset + x, y, z, false);
    for (BlockPos p : cells) put(p.getX(), p.getY(), p.getZ(), true);
    level.setBlock(cells.getFirst(), EngineRegistry.WATER_BLOCK.get().defaultBlockState()
        .setValue(EngineFluid.UNITS, 1), 2 | 16);
    return cells;
  }
  private List<BlockPos> basin() {
    var cells = new ArrayList<BlockPos>();
    if (mode.equals("basin-inspect")) {
      for (int x = -1; x <= 64; x++)
        for (int z = 63; z <= 80; z++) load(new BlockPos(x, 200, z));
      for (int x = 0; x < 64; x++)
        for (int z = 64; z < 80; z++)
          for (int y = 199; y < 204; y++) cells.add(new BlockPos(x, y, z));
      return cells;
    }
    for (int x = -1; x <= 64; x++)
      for (int z = 63; z <= 80; z++) load(new BlockPos(x, 200, z));
    for (int x = -1; x <= 64; x++)
      for (int z = 63; z <= 80; z++)
        for (int y = 198; y <= 204; y++) {
          boolean interior = x >= 0 && x < 64 && z >= 64 && z < 80;
          boolean air = interior && y > (x < 32 ? 200 : 198) && y < 204;
          put(x, y, z, air);
          if (interior && y > 198 && y < 204) cells.add(new BlockPos(x, y, z));
        }
    for (int x = 0; x < 32; x++)
      for (int z = 64; z < 80; z++)
        level.setBlock(new BlockPos(x, 201, z), EngineRegistry.WATER_BLOCK.get().defaultBlockState(), 2 | 16);
    return cells;
  }
  private int amount(BlockPos p) {
    if (level.getChunkSource().getChunk(p.getX() >> 4, p.getZ() >> 4, ChunkStatus.FULL, false) == null) return 0;
    return WaterRuntime.units(level, p);
  }
  private int quantity(List<BlockPos> cells) { return cells.stream().mapToInt(this::amount).sum(); }
  private int diagnostic(String field) throws Exception {
    try {
      var d = WaterRuntime.class.getMethod("drainDiagnostics", ServerLevel.class).invoke(null, level);
      return (Integer) d.getClass().getMethod(field).invoke(d);
    } catch (NoSuchMethodException missingOnBaseline) { return 0; }
  }
  private String digest(boolean flow) throws Exception {
    var hash = MessageDigest.getInstance("SHA-256"); var bytes = ByteBuffer.allocate(16);
    for (var scene : scenes)
      for (var p : scene) {
        int units = amount(p);
        bytes.clear(); bytes.putLong(p.asLong()).putInt(units);
        bytes.putInt(flow && units > 0 ? level.getBlockState(p).getValue(EngineFluid.FLOW) : 0);
        hash.update(bytes.array());
      }
    return HexFormat.of().formatHex(hash.digest());
  }
  @SubscribeEvent
  public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    if (mode == null) return;
    level = event.getServer().getLevel(Level.END);
    check(level != null, "End fixture dimension");
    target = mode.equals("physical-reload") ? 3200 : mode.equals("basin-inspect") ? 200 : mode.startsWith("basin") ? Math.clamp(Integer.parseInt(System.getenv().getOrDefault("DEEPPRIMAL_CLOSED_WATER_TICKS", "3200")), 200, 5000) : (mode.equals("reload") || mode.equals("physical-reload") || mode.equals("reload-edit")) ? 480 : 240;
    if (mode.startsWith("basin")) scenes.add(basin());
    else {
      scenes.add(tube(4096, mode.equals("physical-reload") ? 1025 : 257, mode.equals("reopen")));
      scenes.add(tube(8192, 16, mode.equals("reopen")));
      if (!mode.equals("edit") && !mode.equals("unload") && !(mode.equals("reload") || mode.equals("physical-reload") || mode.equals("reload-edit")))
        scenes.add(tube(12288, 257, mode.equals("reopen")));
      if (!mode.equals("reopen")) {
        int initial = WaterRuntime.diagnostics(level).drainVisits();
        WaterRuntime.tick(level, scenes.getFirst().getFirst());
        firstVisits = WaterRuntime.diagnostics(level).drainVisits() - initial;
        check(firstVisits > 0 && firstVisits <= 128, "First drain turn remains bounded");
        if (!mode.equals("reproduce")) check(diagnostic("jobs") > 0 && diagnostic("retainedNodes") > 128,
            "The first bounded turn must retain a growing native frontier");
        if (mode.equals("physical-reload")) {
          for (int turn = 0; turn < 5; turn++) WaterRuntime.tick(level, scenes.getFirst().getFirst());
          check(diagnostic("retainedNodes") > 512, "Retain a witness through the future physical gap");
        }
        if (mode.equals("edit")) put(4104, 200, 8, false);
        if (mode.equals("unload") || (mode.equals("reload") || mode.equals("physical-reload") || mode.equals("reload-edit"))) {
          for (int x = 259; x <= (mode.equals("physical-reload") ? 317 : 269); x++) level.setChunkForced(x, 0, false);
          var source = level.getChunkSource();
          long deadline = System.nanoTime() + 5_000_000_000L;
          for (int turn = 0; turn < 500 && System.nanoTime() < deadline; turn++) {
            source.tick(() -> true, false);
            for (int work = 0; work < 64 && source.pollTask(); work++) {}
            if (source.getChunk(mode.equals("physical-reload") ? 290 : 262, 0, ChunkStatus.FULL, false) == null && (!mode.equals("physical-reload") || physicallyUnloaded.contains(290))) { gapUnloaded = true; break; }
            Thread.sleep(1);
          }
          check(gapUnloaded, "Gap must actually unload behind the drain frontier");
        }
      }
    }
    check(quantity(scenes.getFirst()) == (mode.startsWith("basin") ? 8192 : 1), "Known initial quantity");
    System.out.println("CLOSED_WATER_START mode=" + mode + " firstVisits=" + firstVisits + " initialUnitsSha=" + digest(false));
  }
  @SubscribeEvent
  public void unloaded(net.neoforged.neoforge.event.level.ChunkEvent.Unload event) {
    if (event.getLevel() == level && event.getChunk().getPos().z() == 0)
      physicallyUnloaded.add(event.getChunk().getPos().x());
  }
  @SubscribeEvent
  public void pre(net.neoforged.neoforge.event.tick.LevelTickEvent.Pre event) {
    if (level != null && event.getLevel() == level) before = System.nanoTime();
  }
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void post(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) throws Exception {
    if (level == null || event.getLevel() != level) return;
    durations.add(System.nanoTime() - before); ticks++;
    if (mode.equals("reload-edit") && ticks == 60) put(4104, 200, 8, false);
    if ((mode.equals("unload") || ((mode.equals("reload") || mode.equals("physical-reload") || mode.equals("reload-edit")) && ticks <= 120))) check(level.getChunkSource().getChunk(mode.equals("physical-reload") ? 290 : 262, 0, ChunkStatus.FULL, false) == null,
        "Production drain continuation must not force-load its missing witness");
    // Missing unloaded cells contain no fixture water: the one finite unit stays at its source.
    for (var scene : scenes) check(quantity(scene) == (mode.startsWith("basin") ? 8192 : 1), "Closed water conserved at tick " + ticks);
    var physics = WaterRuntime.diagnostics(level);
    totalDrainVisits += physics.drainVisits(); totalPressureVisits += physics.pressureVisits();
    totalTransfers += physics.transfers();
    if (mode.equals("basin-fixed") && WaterRuntime.queued(level) == 0) {
      int basinBottom = scenes.getFirst().stream().filter(p -> p.getX() >= 32 && p.getY() == 199).mapToInt(this::amount).sum();
      if (basinBottom == 8192) {
        if (firstSettledTick == 0) firstSettledTick = ticks;
        check(diagnostic("watchedChunks") == 0 && diagnostic("waitingJobs") == 0, "Settled basin releases retained watches");
        basinIdleSamples++;
      }
    }
    if (firstSettledTick > 0) check(WaterRuntime.queued(level) == 0
        && diagnostic("watchedChunks") == 0 && diagnostic("waitingJobs") == 0,
        "A settled closed basin stays idle on every following host tick");
    if (firstSettledTick > 0 && ticks > firstSettledTick)
      check(physics.processed() == 0 && physics.pressureVisits() == 0
              && physics.drainVisits() == 0 && physics.transfers() == 0,
          "The settled basin performs zero transport work on subsequent host ticks");
    peakPending = Math.max(peakPending, WaterRuntime.queued(level));
    peakDrainJobs = Math.max(peakDrainJobs, diagnostic("jobs"));
    peakDrainNodes = Math.max(peakDrainNodes, diagnostic("retainedNodes"));
    peakDrainVisits = Math.max(peakDrainVisits, WaterRuntime.diagnostics(level).drainVisits());
    maxDrainSlice = Math.max(maxDrainSlice, diagnostic("maxSliceVisits"));
    check(diagnostic("pending") <= 65536 && diagnostic("admissionVisits") <= 3072, "Bounded FIFO admission and visits");
    check(peakDrainVisits <= 6144, "The ordinary three-substep host-tick drain budget remains bounded");
    check(diagnostic("readyProbes") <= 16 && diagnostic("readyPending") <= 1024, "Bounded readiness probes and keys");
    check(diagnostic("waitingJobs") <= 128 && diagnostic("waitingLinks") <= 1024
        && diagnostic("jobs") <= 16 && diagnostic("retainedNodes") <= 16 * 8192
        && diagnostic("maxSliceVisits") <= 128, "Drain residency and slice bounds");
    if ((mode.equals("reload") || mode.equals("physical-reload") || mode.equals("reload-edit")) && ticks == 120) {
      for (var p : scenes.getFirst()) {
        level.getChunk(p);
        level.setChunkForced(p.getX() >> 4, p.getZ() >> 4, true);
      }
      gapReloaded = true;
      System.out.println("CLOSED_DRAIN_GAP_RELOADED noOriginInjection=true physicalUnload=" + physicallyUnloaded.contains(290));
    }
    if (ticks % 20 == 0) System.out.println("DRAIN_NATIVE_STATE tick=" + ticks + " jobs=" + diagnostic("jobs") + " waiting=" + diagnostic("waitingJobs") + " links=" + diagnostic("waitingLinks") + " source=" + amount(scenes.getFirst().getFirst()));
    if (ticks % 100 == 0) System.out.println("CLOSED_WATER_PROGRESS tick=" + ticks + " pending=" + WaterRuntime.queued(level) + " physics=" + WaterRuntime.diagnostics(level) + " upper="
        + (mode.startsWith("basin") ? scenes.getFirst().stream().filter(p -> p.getX() < 32).mapToInt(this::amount).sum() : -1));
    if (ticks >= target - 20 && !mode.equals("reproduce") && !mode.startsWith("basin")
        && !mode.equals("edit") && !mode.equals("unload")) {
      check(WaterRuntime.queued(level) == 0 && diagnostic("watchedChunks") == 0, "Settled routes release all work/watches");
      idleSamples++;
    }
    if (ticks < target) return;
    if (mode.equals("unload")) for (var p : scenes.getFirst()) level.getChunk(p);
    int upper = 0, bottom = 0;
    if (mode.startsWith("basin")) {
      upper = scenes.getFirst().stream().filter(p -> p.getX() < 32).mapToInt(this::amount).sum();
      bottom = scenes.getFirst().stream().filter(p -> p.getX() >= 32 && p.getY() == 199).mapToInt(this::amount).sum();
      if (mode.equals("basin-fixed")) {
        var sorted = new ArrayList<Long>(durations); Collections.sort(sorted);
        System.out.println("BASIN_AUDIT_FINAL ticks=" + ticks + " upper=" + upper + " bottom=" + bottom
            + " total=8192 pending=" + WaterRuntime.queued(level) + " firstSettledTick=" + firstSettledTick
            + " idleSamples=" + basinIdleSamples + " totalDrainVisits=" + totalDrainVisits
            + " totalPressureVisits=" + totalPressureVisits + " totalTransfers=" + totalTransfers
            + " drain=" + WaterRuntime.drainDiagnostics(level)
            + " medianLevelTickMs=" + sorted.get(target/2)/1e6 + " p95LevelTickMs=" + sorted.get((int)(target*.95))/1e6
            + " unitsSha=" + digest(false) + " flowSha=" + digest(true));
      }
      if (mode.equals("basin-fixed")) check(upper == 0 && bottom == 8192 && WaterRuntime.queued(level) == 0,
          "The closed stepped basin must drain fully: upper=" + upper + " bottom=" + bottom + " pending=" + WaterRuntime.queued(level));
    }
    if (!mode.startsWith("basin")) {
      var longCells = scenes.getFirst(); var shortCells = scenes.get(1);
      check(WaterRuntime.units(level, shortCells.getLast()) == 1, "Short control must drain");
      boolean expectedStuck = mode.equals("reproduce") || mode.equals("edit") || mode.equals("unload") || mode.equals("reload-edit");
      check(WaterRuntime.units(level, expectedStuck ? longCells.getFirst() : longCells.getLast()) == 1,
          "Long route must " + (expectedStuck ? "remain safely blocked" : "reach its distant drop"));
      if (scenes.size() == 3) check(WaterRuntime.units(level, expectedStuck ? scenes.get(2).getFirst() : scenes.get(2).getLast()) == 1,
          "The second independent long drain must progress fairly");
      if (mode.equals("edit") || mode.equals("reload-edit")) check(level.getBlockState(new BlockPos(4104, 200, 8)).is(Blocks.STONE), "Preserve the silent wall edit");
    }
    Collections.sort(durations);
    String record = "CLOSED_WATER_NATIVE_PASS mode=" + mode + " ticks=" + ticks + " conservedEveryTick=true initialUnits="
        + (mode.startsWith("basin") ? 8192 : scenes.size()) + " firstVisits=" + firstVisits + " pending=" + WaterRuntime.queued(level)
        + " firstSettledTick=" + firstSettledTick + " basinIdleSamples=" + basinIdleSamples
        + " remainingUpperUnits=" + upper + " bottomUnits=" + bottom
        + " peakDrainJobs=" + peakDrainJobs + " peakDrainNodes=" + peakDrainNodes + " maxDrainSlice=" + maxDrainSlice
        + " peakDrainVisits=" + peakDrainVisits + " drainJobs=" + diagnostic("jobs") + " saturated=" + diagnostic("saturated") + " restarts=" + diagnostic("restarts")
        + " waitingJobs=" + diagnostic("waitingJobs") + " waitingLinks=" + diagnostic("waitingLinks")
        + " idleSamples=" + idleSamples + " gapUnloaded=" + gapUnloaded + " gapReloaded=" + gapReloaded + " unitsSha=" + digest(false) + " flowSha=" + digest(true)
        + " medianLevelTickMs=" + durations.get(target / 2) / 1e6 + " p95LevelTickMs=" + durations.get((int) (target * .95)) / 1e6 + "\n";
    Files.writeString(Path.of(System.getenv("DEEPPRIMAL_CLOSED_WATER_RECORD")), record); System.out.print(record);
    for (var c : forced) level.setChunkForced(c.x(), c.z(), false);
    level = null; event.getLevel().getServer().halt(false);
  }
}
