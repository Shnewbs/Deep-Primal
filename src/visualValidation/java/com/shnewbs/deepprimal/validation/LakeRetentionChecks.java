package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.WatershedRegion;
import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Run a complete generated basin with the real solver, not a visual-only pool. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class LakeRetentionChecks {
  private WatershedRegion.LakeProbe lake;
  private int ticks, peakQueue;
  private long before;
  private final int target =
      Math.clamp(
          Integer.parseInt(System.getenv().getOrDefault("DEEPPRIMAL_LAKE_TICKS", "200")),
          200,
          5000);
  private final long[] durations = new long[target];
  private final List<net.minecraft.world.level.ChunkPos> forced = new ArrayList<>();

  public LakeRetentionChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void start(net.neoforged.neoforge.event.server.ServerStartedEvent e) {
    if (System.getenv("DEEPPRIMAL_LAKE_RECORD") == null) return;
    var level = e.getServer().overworld();
    var g = (EngineChunkGenerator) level.getChunkSource().getGenerator();
    lake =
        g.geography().waterways(0, 0).lakeProbes().stream()
            .filter(
                p ->
                    p.area() >= 3072
                        && p.area() < 16000
                        && p.level() > 110
                        && g.geography().sample(p.x(), p.z()).ground() < p.level() - 3)
            .min(Comparator.comparingDouble(WatershedRegion.LakeProbe::area))
            .orElseThrow();
    for (int z = (int) Math.floor(lake.minZ()) >> 4; z <= (int) Math.ceil(lake.maxZ()) >> 4; z++)
      for (int x = (int) Math.floor(lake.minX()) >> 4;
          x <= (int) Math.ceil(lake.maxX()) >> 4;
          x++) {
        level.getChunk(x, z);
        level.setChunkForced(x, z, true);
        forced.add(new net.minecraft.world.level.ChunkPos(x, z));
      }
    for (int z = (int) Math.floor(lake.minZ()); z <= lake.maxZ(); z++)
      for (int x = (int) Math.floor(lake.minX()); x <= lake.maxX(); x++) {
        var s = g.geography().sample(x, z);
        if (s.wet())
          WaterRuntime.activate(level, new BlockPos(x, (int) Math.ceil(s.water()) - 1, z));
      }
    System.out.println("LAKE_RETENTION_START " + lake + " forcedChunks=" + forced.size());
  }

  @SubscribeEvent
  public void pre(net.neoforged.neoforge.event.tick.ServerTickEvent.Pre e) {
    if (lake != null) before = System.nanoTime();
  }

  @SubscribeEvent
  public void post(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e) throws Exception {
    if (lake == null) return;
    var level = e.getServer().overworld();
    durations[ticks++] = System.nanoTime() - before;
    peakQueue = Math.max(peakQueue, WaterRuntime.queued(level));
    if (ticks % 50 == 0)
      System.out.println(
          "LAKE_RETENTION_PROGRESS tick=" + ticks + " " + WaterRuntime.diagnostics(level));
    if (ticks < target) return;
    double low = Double.POSITIVE_INFINITY, high = Double.NEGATIVE_INFINITY;
    for (int dz = -2; dz <= 2; dz++)
      for (int dx = -2; dx <= 2; dx++) {
        int x = (int) Math.rint(lake.x()) + dx, z = (int) Math.rint(lake.z()) + dz;
        double surface = Double.NaN;
        for (int y = (int) Math.ceil(lake.level()) + 2; y > lake.level() - 12; y--) {
          int units = WaterRuntime.units(level, new BlockPos(x, y, z));
          if (units > 0) {
            surface = y + units / 16.;
            break;
          }
        }
        if (!Double.isFinite(surface) || surface < lake.level() - 1.0625)
          throw new AssertionError(
              "Lake drained below outlet sill: initial="
                  + lake.level()
                  + " actual="
                  + surface
                  + " at "
                  + x
                  + ","
                  + z);
        low = Math.min(low, surface);
        high = Math.max(high, surface);
      }
    Arrays.sort(durations);
    String report =
        "LAKE_RETENTION_NATIVE_PASS ticks="
            + target
            + " initialLevel="
            + lake.level()
            + " retainedLevel="
            + low
            + ".."
            + high
            + " forcedChunks="
            + forced.size()
            + " peakPending="
            + peakQueue
            + " pending="
            + WaterRuntime.queued(level)
            + " diagnostics="
            + WaterRuntime.diagnostics(level)
            + " medianTickMs="
            + durations[target / 2] / 1e6
            + " p95TickMs="
            + durations[(int) (target * .95)] / 1e6
            + "\n";
    Files.writeString(Path.of(System.getenv("DEEPPRIMAL_LAKE_RECORD")), report);
    System.out.print(report);
    for (var p : forced) level.setChunkForced(p.x(), p.z(), false);
    lake = null;
    e.getServer().halt(false);
  }
}
