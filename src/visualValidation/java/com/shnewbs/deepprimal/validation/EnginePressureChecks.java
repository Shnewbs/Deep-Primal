package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Real native communicating vessels, sealed separation and unloaded-boundary checks. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class EnginePressureChecks {
  private boolean done;

  public EnginePressureChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  @SubscribeEvent
  public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event)
      throws Exception {
    if (!Boolean.parseBoolean(System.getenv("DEEPPRIMAL_PRESSURE_CHECKS")) || done) return;
    done = true;
    var server = event.getServer();
    var l = server.overworld();
    List<BlockPos> tube = new ArrayList<>();
    for (int x = 0; x <= 4; x++) tube.add(new BlockPos(x, 500, 8));
    for (int y = 501; y <= 504; y++) {
      tube.add(new BlockPos(0, y, 8));
      tube.add(new BlockPos(4, y, 8));
    }
    for (BlockPos p : tube)
      for (Direction d : Direction.values())
        l.setBlock(p.relative(d), Blocks.STONE.defaultBlockState(), 2 | 16);
    for (BlockPos p : tube) l.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
    for (BlockPos p : tube)
      if (p.getY() == 500
          || (p.getX() == 0 && p.getY() <= 503)
          || (p.getX() == 4 && p.getY() <= 501))
        l.setBlock(p, EngineRegistry.WATER_BLOCK.get().defaultBlockState(), 2 | 16);
    int before = tube.stream().mapToInt(p -> WaterRuntime.units(l, p)).sum();
    require(before == 144, "Known U-bend volume");
    for (int n = 0; n < 4; n++) WaterRuntime.tick(l, new BlockPos(0, 503, 8));
    require(
        WaterRuntime.units(l, new BlockPos(4, 502, 8)) == 16
            && WaterRuntime.units(l, new BlockPos(0, 503, 8)) == 0,
        "Submerged passage raises the lower surface");
    for (int n = 0; n < 10; n++) for (BlockPos p : tube) WaterRuntime.tick(l, p);
    require(
        tube.stream().mapToInt(p -> WaterRuntime.units(l, p)).sum() == before,
        "Pressure conserves total volume");
    // Rebuild unequal chambers with their connecting passage blocked.
    l.setBlock(new BlockPos(2, 500, 8), Blocks.STONE.defaultBlockState(), 2 | 16);
    l.setBlock(
        new BlockPos(0, 503, 8), EngineRegistry.WATER_BLOCK.get().defaultBlockState(), 2 | 16);
    l.setBlock(new BlockPos(4, 502, 8), Blocks.AIR.defaultBlockState(), 2 | 16);
    for (int n = 0; n < 10; n++) WaterRuntime.tick(l, new BlockPos(0, 501, 8));
    require(
        WaterRuntime.units(l, new BlockPos(4, 502, 8)) == 0,
        "Disconnected chambers cannot exchange volume");
    var missing = new BlockPos(3_200_000, 500, 3_200_000);
    WaterRuntime.tick(l, missing);
    require(
        l.getChunkSource()
                .getChunk(
                    200000, 200000, net.minecraft.world.level.chunk.status.ChunkStatus.FULL, false)
            == null,
        "Tick never requests missing chunk");
    Path out = Path.of(System.getenv("DEEPPRIMAL_PRESSURE_RECORD"));
    Files.createDirectories(out.getParent());
    String result =
        "ENGINE_PRESSURE_NATIVE_PASS units=144 raisedSurface=true conserved=true disconnected=true"
            + " noForceLoad=true\n";
    Files.writeString(out, result);
    System.out.print(result);
    server.halt(false);
  }
}
