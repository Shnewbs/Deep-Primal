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
  private int ticks, quantity, firstSearchVisits;
  private boolean done;

  public LongPressureChecks() { NeoForge.EVENT_BUS.register(this); }

  private static void check(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  private List<BlockPos> create(ServerLevel level, int offset, int width, int rightTop) {
    var cells = new ArrayList<BlockPos>();
    for (int x = 0; x <= width; x++) cells.add(new BlockPos(offset + x, 500, 8));
    for (int y = 501; y <= 505; y++) {
      cells.add(new BlockPos(offset, y, 8));
      cells.add(new BlockPos(offset + width, y, 8));
    }
    for (BlockPos p : cells) {
      var chunk = new ChunkPos(p.getX() >> 4, p.getZ() >> 4);
      if (forced.add(chunk)) level.setChunkForced(chunk.x(), chunk.z(), true);
      level.getChunk(p);
      for (Direction direction : Direction.values())
        level.setBlock(p.relative(direction), Blocks.STONE.defaultBlockState(), 2 | 16);
    }
    for (BlockPos p : cells) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
    for (BlockPos p : cells)
      if (p.getY() == 500 || (p.getX() == offset && p.getY() <= 503)
          || (p.getX() == offset + width && p.getY() <= rightTop))
        level.setBlock(p, EngineRegistry.WATER_BLOCK.get().defaultBlockState(), 2 | 16);
    return cells;
  }

  @SubscribeEvent
  public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
    if (!Boolean.parseBoolean(System.getenv("DEEPPRIMAL_LONG_PRESSURE_CHECKS"))) return;
    var level = event.getServer().overworld();
    check(level.getMaxY() > 506, "Native pressure fixture needs the independent tall dimension");
    var shortTube = create(level, 512, 4, 501);
    int before = shortTube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum();
    check(before == 144, "Short control initial volume");
    for (int n = 0; n < 4; n++) WaterRuntime.tick(level, new BlockPos(512, 503, 8));
    check(WaterRuntime.units(level, new BlockPos(516, 502, 8)) == 16,
        "Short communicating-vessel control must rise");
    check(shortTube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum() == before,
        "Short control conservation");
    System.out.println("SHORT_PRESSURE_CONTROL_NATIVE_PASS units=144 raisedSurface=true conserved=true");
    tube = create(level, 0, 256, 502);
    quantity = tube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum();
    check(quantity == 262 * 16, "Long bend starts with 262 full water cells");
    int beforeSearch = WaterRuntime.diagnostics(level).pressureVisits();
    WaterRuntime.tick(level, new BlockPos(0, 503, 8));
    firstSearchVisits = WaterRuntime.diagnostics(level).pressureVisits() - beforeSearch;
    check(firstSearchVisits == 128, "Expected first native search to reach the 128-cell cutoff");
  }

  @SubscribeEvent
  public void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) throws Exception {
    if (tube == null || done || ++ticks < 120) return;
    done = true;
    var level = event.getServer().overworld();
    int after = tube.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum();
    int outlet = WaterRuntime.units(level, new BlockPos(256, 503, 8));
    check(after == quantity, "Long sealed bend conservation");
    check(outlet == 0 && WaterRuntime.units(level, new BlockPos(0, 503, 8)) == 16,
        "Expected long-route cutoff changed; inspect instead of declaring this reproduction valid");
    String result = "LONG_PRESSURE_CUTOFF_NATIVE_REPRODUCED ticks=" + ticks
        + " initialWaterCells=262 initialUnits=" + quantity + " finalUnits=" + after
        + " outletUnits=" + outlet + " forcedChunks=" + forced.size()
        + " firstSearchVisits=" + firstSearchVisits
        + " conserved=true solverAcceptance=false\n";
    var output = Path.of(System.getenv("DEEPPRIMAL_LONG_PRESSURE_RECORD"));
    Files.createDirectories(output.toAbsolutePath().getParent());
    Files.writeString(output, result);
    System.out.print(result);
    for (ChunkPos chunk : forced) level.setChunkForced(chunk.x(), chunk.z(), false);
    event.getServer().halt(false);
  }
}
