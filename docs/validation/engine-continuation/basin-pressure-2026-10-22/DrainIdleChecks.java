package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Fully loaded competing pool: bounded drainage requests eventually idle and edits wake them. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class DrainIdleChecks {
  private final String record = System.getenv("DEEPPRIMAL_DRAIN_IDLE_RECORD");
  private final List<BlockPos> pool = new ArrayList<>(), film = new ArrayList<>();
  private final BlockPos pit = new BlockPos(31, 199, 80);
  private ServerLevel level;
  private int ticks, idleSamples;
  public DrainIdleChecks() { NeoForge.EVENT_BUS.register(this); }
  private static void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
  private void block(int x, int y, int z, boolean air) {
    level.setBlock(new BlockPos(x,y,z), (air ? Blocks.AIR : Blocks.STONE).defaultBlockState(), 2|16);
  }
  private int quantity(List<BlockPos> cells) {
    return cells.stream().mapToInt(p -> WaterRuntime.units(level,p)).sum();
  }
  @SubscribeEvent
  public void start(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
    if (record == null) return;
    level = event.getServer().getLevel(Level.END);
    for (int cx = -1; cx <= 2; cx++) for (int cz = 3; cz <= 6; cz++) {
      level.getChunk(cx,cz); level.setChunkForced(cx,cz,true);
    }
    for (int cx = 255; cx <= 257; cx++) {
      level.getChunk(cx,0); level.setChunkForced(cx,0,true);
    }
    for (int x = -1; x <= 32; x++) for (int z = 63; z <= 96; z++)
      for (int y = 198; y <= 201; y++) block(x,y,z,x >= 0 && x < 32 && z >= 64 && z < 96 && y == 200);
    for (int x=0;x<32;x++) for (int z=64;z<96;z++) {
      var p=new BlockPos(x,200,z);pool.add(p);
      level.setBlock(p,EngineRegistry.WATER_BLOCK.get().defaultBlockState().setValue(EngineFluid.UNITS,1),2|16);
    }
    // Add this live route after all 1024 competing requests.
    for(int x=4095;x<=4105;x++) for(int z=7;z<=9;z++) for(int y=197;y<=201;y++) block(x,y,z,false);
    for(int x=4096;x<=4104;x++) {var p=new BlockPos(x,200,8);film.add(p);block(x,200,8,true);}
    film.add(new BlockPos(4104,199,8));block(4104,199,8,true);
    for(int x=4096;x<4104;x++) level.setBlock(new BlockPos(x,200,8),
        EngineRegistry.WATER_BLOCK.get().defaultBlockState().setValue(EngineFluid.UNITS,1),2|16);
  }
  @SubscribeEvent(priority=EventPriority.LOWEST)
  public void post(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) throws Exception {
    if(level==null || event.getLevel()!=level)return;
    ticks++;
    var d=WaterRuntime.drainDiagnostics(level);
    check(quantity(pool)+WaterRuntime.units(level,pit)==1024,"Closed pool conserves quantity at "+ticks);
    check(quantity(film)==8,"Competing film conserves quantity at "+ticks);
    check(d.jobs()<=16 && d.retainedNodes()<=16*8192 && d.pending()<=65536
        && d.admissionVisits()<=3072 && d.maxSliceVisits()<=128
        && WaterRuntime.diagnostics(level).drainVisits()<=6144,"Per-host and resident bounds");
    if(ticks==120) check(WaterRuntime.units(level,film.getLast())==8,"Late unrelated route progresses under saturation");
    if(ticks>=380 && ticks<400) {
      check(WaterRuntime.queued(level)==0 && d.watchedChunks()==0 && d.waitingJobs()==0,
          "A fully loaded settled pool must release all work before edit: pending="+WaterRuntime.queued(level));
      var physics=WaterRuntime.diagnostics(level);
      check(physics.processed()==0 && physics.pressureVisits()==0
          && physics.drainVisits()==0 && physics.transfers()==0,
          "An idle loaded pool performs zero transport work");
      idleSamples++;
    }
    if(ticks==400) {
      check(idleSamples==20,"Twenty stable host ticks before edit");
      level.setBlock(pit,Blocks.AIR.defaultBlockState(),3);
      System.out.println("DRAIN_IDLE_EDIT ordinaryNeighborWake=true idleSamples="+idleSamples);
    }
    if(ticks<480)return;
    check(WaterRuntime.units(level,pit)==16,"Opening the floor wakes the previously settled pool");
    check(WaterRuntime.queued(level)==0 && d.watchedChunks()==0 && d.waitingJobs()==0,
        "The edited pool releases all work and watches again");
    String result="DRAIN_IDLE_NATIVE_PASS ticks=480 poolUnits=1024 filmUnits=8 lateFilmBy=120 idleSamples="
        +idleSamples+" editedSinkUnits=16 zeroIdleTickWork=true conservedEveryTick=true forcedChunks=19 pendingAfterEdit="+WaterRuntime.queued(level)+"\n";
    Files.writeString(Path.of(record),result);System.out.print(result);level.getServer().halt(false);
  }
}
