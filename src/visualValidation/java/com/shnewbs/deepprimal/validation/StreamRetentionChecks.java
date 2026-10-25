package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.TerrainEngine;
import com.shnewbs.deepprimal.core.Aquifer;
import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import com.shnewbs.deepprimal.engine.HydrologyData;
import com.shnewbs.deepprimal.engine.WaterRuntime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Live generated source reach; no client edits, synthetic water or unlimited refill. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class StreamRetentionChecks {
  private record Column(int x, int z, TerrainEngine.Sample planned, double initial) {}
  private final List<ChunkPos> forced = new ArrayList<>();
  private final List<Column> columns = new ArrayList<>();
  private int ticks;
  private final int target = Integer.parseInt(
      System.getenv().getOrDefault("DEEPPRIMAL_STREAM_RETENTION_TICKS", "400"));
  private int lastRetained, lastInitiallyWet, lastOverBank;
  private int minimumWet = Integer.MAX_VALUE;
  private int quotaTicks, saturatedTicks, scannedChunks, lastDischarged, intervals, unsuppliedTicks;
  private long deliveredUnits;
  private double minFill = Double.POSITIVE_INFINITY, lastFill = Double.NaN;
  private long siteRegion;

  /** Diagnostic only: reads the runtime's private regional allowance for the observed source region. */
  private void quotaSample(ServerLevel level) {
    try {
      var workField = com.shnewbs.deepprimal.engine.HydrologyRuntime.class.getDeclaredField("WORK");
      workField.setAccessible(true);
      var work = ((java.util.Map<?, ?>) workField.get(null)).get(level);
      if (work == null) return;
      var chunksField = work.getClass().getDeclaredField("chunks");
      chunksField.setAccessible(true);
      scannedChunks = Math.max(scannedChunks, ((java.util.Map<?, ?>) chunksField.get(work)).size());
      var regionsField = work.getClass().getDeclaredField("regions");
      regionsField.setAccessible(true);
      var region = ((java.util.Map<?, ?>) regionsField.get(work)).get(siteRegion);
      if (region == null) return;
      var discharged = region.getClass().getDeclaredField("natural");
      discharged.setAccessible(true);
      quotaTicks++;
      int now = discharged.getInt(region);
      if (now >= com.shnewbs.deepprimal.core.Aquifer.baseflowUnits(com.shnewbs.deepprimal.engine.HydrologyConfig.RAIN_QUANTA.get())) saturatedTicks++;
      if (now < lastDischarged && quotaTicks > 1 && lastDischarged < 1 << 16) { deliveredUnits += lastDischarged; intervals++; }
      lastDischarged = now;
      var aquifer = com.shnewbs.deepprimal.engine.HydrologyData.get(level).aquifers.get(siteRegion);
      if (aquifer != null) {
        double fill = aquifer.ground.water() / (double) com.shnewbs.deepprimal.core.Aquifer.CAPACITY;
        minFill = Math.min(minFill, fill); lastFill = fill;
        if (!aquifer.canSupply(true)) unsuppliedTicks++;
      }
    } catch (ReflectiveOperationException failure) {
      throw new AssertionError("Quota diagnostic reflection", failure);
    }
  }
  private long dryColumnTicks;

  public StreamRetentionChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  private static double surface(ServerLevel level, int x, int z, TerrainEngine.Sample planned) {
    int start = (int) Math.ceil(planned.wet() ? planned.water() : planned.ground()) + 4;
    for (int y = start; y >= Math.floor(planned.ground()) - 4; y--) {
      int units = WaterRuntime.units(level, new BlockPos(x, y, z));
      if (units > 0) return y + units / 16.;
    }
    return Double.NaN;
  }

  @SubscribeEvent
  public void start(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
    if (System.getenv("DEEPPRIMAL_STREAM_RETENTION_RECORD") == null) return;
    var level = event.getServer().overworld();
    var terrain = ((EngineChunkGenerator) level.getChunkSource().getGenerator()).geography();
    var probes = terrain.waterways(0, 0).probes();
    var candidates = probes.stream()
        .filter(p -> p.source() && p.stage() > 110 && terrain.sample(p.x(), p.z()).speed() > 0)
        .toList();
    String selected = System.getenv("DEEPPRIMAL_STREAM_RETENTION_SITE");
    var site = "widest".equals(selected)
        ? probes.stream().filter(p -> p.stage() > 110 && terrain.sample(p.x(), p.z()).speed() > 0)
            .max(java.util.Comparator.comparingDouble(p -> p.radius())).orElseThrow()
        : "highest".equals(selected)
        ? candidates.stream().max(java.util.Comparator.comparingDouble(p -> p.stage())).orElseThrow()
        : candidates.stream().findFirst().orElseThrow(() -> new AssertionError("No generated source reach"));
    if (target < 400 || target > 5000) throw new AssertionError("Stream observation tick bounds");
    siteRegion = HydrologyData.key((int) Math.floor(site.x()), (int) Math.floor(site.z()));
    int cx = (int) Math.floor(site.x()) >> 4, cz = (int) Math.floor(site.z()) >> 4;
    for (int z = cz - 1; z <= cz + 1; z++)
      for (int x = cx - 1; x <= cx + 1; x++) {
        level.getChunk(x, z);
        level.setChunkForced(x, z, true);
        forced.add(new ChunkPos(x, z));
      }
    for (int z = cz * 16 - 12; z < cz * 16 + 28; z++)
      for (int x = cx * 16 - 12; x < cx * 16 + 28; x++) {
        var planned = terrain.column(x, z);
        double initial = surface(level, x, z, planned);
        columns.add(new Column(x, z, planned, initial));
        if (Double.isFinite(initial))
          WaterRuntime.activate(level, new BlockPos(x, (int) Math.ceil(initial) - 1, z));
      }
    System.out.println("STREAM_RETENTION_START source=" + site + " chunks=" + forced.size());
    System.out.println("STREAM_RETENTION_RUNTIME core=" + Aquifer.class.getProtectionDomain().getCodeSource().getLocation()
        + " runtime=" + com.shnewbs.deepprimal.engine.HydrologyRuntime.class.getProtectionDomain().getCodeSource().getLocation());
    report(level);
  }

  private String report(ServerLevel level) {
    int initiallyWet = 0, retained = 0, newlyWet = 0, overBank = 0, naturalHeadBlocked = 0;
    var terrain = ((EngineChunkGenerator) level.getChunkSource().getGenerator()).geography();
    var datums = new java.util.HashMap<Long, Double>();
    double maxRise = 0, maxFall = 0, maxAbovePlanned = Double.NEGATIVE_INFINITY;
    for (var column : columns) {
      double current = surface(level, column.x, column.z, column.planned);
      if (Double.isFinite(column.initial)) {
        initiallyWet++;
        long key = HydrologyData.key(column.x, column.z);
        var aquifer = HydrologyData.get(level).aquifers.get(key);
        if (aquifer != null && aquifer.canSupply(true) && column.planned.speed() > 0) {
          double datum = datums.computeIfAbsent(key, unused -> {
            int ox = Math.floorDiv(column.x, 64) * 64, oz = Math.floorDiv(column.z, 64) * 64;
            double low = Double.POSITIVE_INFINITY;
            for (int dz = 8; dz < 64; dz += 16)
              for (int dx = 8; dx < 64; dx += 16)
                low = Math.min(low, terrain.column(ox + dx, oz + dz).ground());
            return low + 1;
          });
          if (aquifer.head(datum) <= Math.ceil(column.planned.water()) - 1) naturalHeadBlocked++;
        }
        if (Double.isFinite(current)) {
          retained++;
          maxRise = Math.max(maxRise, current - column.initial);
          maxAbovePlanned = Math.max(maxAbovePlanned, current - column.planned.water());
          maxFall = Math.max(maxFall, column.initial - current);
        }
      } else if (Double.isFinite(current)) {
        newlyWet++;
        if (current > Math.floor(column.planned.ground()) + 1.0625) overBank++;
      }
    }
    String record = "STREAM_RETENTION_SAMPLE tick=" + ticks + " initiallyWet=" + initiallyWet
        + " retained=" + retained + " newlyWet=" + newlyWet + " overBank=" + overBank
        + " maxRise=" + maxRise + " maxFall=" + maxFall + " maxAbovePlanned=" + maxAbovePlanned
        + " regionalMinimumHeadBlocked=" + naturalHeadBlocked
        + " diagnostics=" + WaterRuntime.diagnostics(level);
    System.out.println(record);
    if (ticks >= target) {
      columns.stream().filter(c -> Double.isFinite(c.initial))
          .map(c -> new Object[] {c, c.initial - surface(level, c.x, c.z, c.planned)})
          .filter(e -> (double) e[1] > .25)
          .sorted((a, b) -> Double.compare((double) b[1], (double) a[1])).limit(12)
          .forEach(e -> {
            var c = (Column) e[0];
            int below = 0;
            for (int y = (int) Math.floor(c.initial) - 1; y >= (int) Math.floor(c.planned.ground()) - 3; y--)
              if (WaterRuntime.units(level, new BlockPos(c.x, y, c.z)) > 0) below++;
            System.out.println("STREAM_RETENTION_FALL x=" + c.x + " z=" + c.z + " fall=" + e[1]
                + " initial=" + c.initial + " planned=" + c.planned.water()
                + " ground=" + c.planned.ground() + " speed=" + c.planned.speed()
                + " wetBlocksBelow=" + below);
          });
    }
    lastRetained = retained;
    lastInitiallyWet = initiallyWet;
    lastOverBank = overBank;
    return record;
  }

  @SubscribeEvent
  public void post(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) throws java.io.IOException {
    if (columns.isEmpty()) return;
    var level = event.getServer().overworld();
    int wet = 0, expectedWet = 0;
    for (var column : columns)
      if (Double.isFinite(column.initial)) {
        expectedWet++;
        if (Double.isFinite(surface(level, column.x, column.z, column.planned))) wet++;
      }
    minimumWet = Math.min(minimumWet, wet);
    dryColumnTicks += expectedWet - wet;
    quotaSample(level);
    if (++ticks % 100 != 0) return;
    String result = report(level);
    Files.writeString(Path.of(System.getenv("DEEPPRIMAL_STREAM_RETENTION_RECORD")),
        result + "\n", java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
    if (ticks < target) return;
    for (var chunk : forced) level.setChunkForced(chunk.x(), chunk.z(), false);
    if (Boolean.parseBoolean(System.getenv("DEEPPRIMAL_STREAM_RETENTION_REQUIRE")))
      if (lastInitiallyWet == 0 || dryColumnTicks != 0 || lastOverBank > 0)
        throw new AssertionError("Generated source reach drained or overtopped: " + result);
    System.out.println("STREAM_RETENTION_CONTINUITY ticks=" + ticks
        + " minimumWet=" + minimumWet + " dryColumnTicks=" + dryColumnTicks
        + " retained=" + lastRetained + " sampledBankOvertop=" + lastOverBank
        + " sourceRegionQuotaTicks=" + quotaTicks + " saturatedQuotaTicks=" + saturatedTicks
        + " maxScannedChunks=" + scannedChunks + " regionDeliveredUnits=" + deliveredUnits
        + " quotaIntervals=" + intervals + " aquiferMinFill=" + minFill + " aquiferFinalFill=" + lastFill
        + " unsuppliedTicks=" + unsuppliedTicks);
    columns.clear();
    System.out.println("STREAM_RETENTION_OBSERVATION_COMPLETE ticks=" + target
        + " scope=loadedGeneratedReach notFullConservationAudit=true");
    event.getServer().halt(false);
  }
}
