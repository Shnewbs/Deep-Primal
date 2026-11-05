package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.EngineFluid;
import com.shnewbs.deepprimal.engine.EngineRegistry;
import com.shnewbs.deepprimal.engine.WaterRuntime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Same native probe on old/new production source; excludes newer diagnostics APIs. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class HeadFrontierProbe {
  public HeadFrontierProbe() { NeoForge.EVENT_BUS.register(this); }

  @SubscribeEvent
  public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    String mode = System.getenv("DEEPPRIMAL_HEAD_FRONTIER_CHECK");
    if (mode == null) return;
    var level = event.getServer().overworld();
    var cells = new ArrayList<BlockPos>();
    for (int x = 0; x <= 256; x++) cells.add(new BlockPos(x, 500, 8));
    for (int y = 501; y <= 505; y++) {
      cells.add(new BlockPos(0, y, 8)); cells.add(new BlockPos(256, y, 8));
    }
    for (BlockPos p : cells) {
      level.getChunk(p);
      for (Direction direction : Direction.values())
        level.setBlock(p.relative(direction), Blocks.STONE.defaultBlockState(), 2 | 16);
    }
    for (BlockPos p : cells) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
    for (BlockPos p : cells)
      if (p.getY() == 500 || (p.getX() == 0 && p.getY() <= 503)
          || (p.getX() == 256 && p.getY() <= 502))
        level.setBlock(p, EngineRegistry.WATER_BLOCK.get().defaultBlockState(), 2 | 16);
    var high = new BlockPos(0, 503, 8); var low = new BlockPos(256, 502, 8);
    var water = EngineRegistry.WATER_BLOCK.get().defaultBlockState();
    level.setBlock(high, water.setValue(EngineFluid.UNITS, 15), 2 | 16);
    level.setBlock(low, water.setValue(EngineFluid.UNITS, 15), 2 | 16);
    WaterRuntime.tick(level, high);
    var before = WaterRuntime.pressureDiagnostics(level);
    level.setBlock(high, water.setValue(EngineFluid.UNITS, 14), 2 | 16);
    level.setBlock(low, water.setValue(EngineFluid.UNITS, 16), 2 | 16);
    WaterRuntime.tick(level, high);
    var after = WaterRuntime.pressureDiagnostics(level);
    boolean reset = after.restarts() > before.restarts()
        && after.retainedNodes() <= before.retainedNodes();
    boolean retained = after.restarts() == before.restarts()
        && after.retainedNodes() > before.retainedNodes();
    if ((!mode.equals("before") && !mode.equals("after"))
        || (mode.equals("before") ? !reset : !retained))
      throw new AssertionError("Unexpected frontier result before=" + before + " after=" + after);
    int volume = cells.stream().mapToInt(p -> WaterRuntime.units(level, p)).sum();
    if (volume != 4190) throw new AssertionError("Head-only edits lost volume: " + volume);
    String result = (mode.equals("before") ? "HEAD_FRONTIER_RESET_BASELINE_REPRODUCED"
        : "HEAD_FRONTIER_RETAINED_NATIVE_PASS") + " firstNodes=" + before.retainedNodes()
        + " nextNodes=" + after.retainedNodes() + " restartDelta="
        + (after.restarts() - before.restarts()) + " units=" + volume + " conserved=true\n";
    Files.writeString(Path.of(System.getenv("DEEPPRIMAL_HEAD_PROBE_RECORD")), result);
    System.out.print(result);
    event.getServer().halt(false);
  }
}
