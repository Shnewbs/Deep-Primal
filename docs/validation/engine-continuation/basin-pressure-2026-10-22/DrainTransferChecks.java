package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** No intervening level steps: measure one retained-search commit through a wet witness. */
@Mod(value="deepprimal", dist=Dist.DEDICATED_SERVER)
public final class DrainTransferChecks {
  public DrainTransferChecks() { NeoForge.EVENT_BUS.register(this); }
  private static void check(boolean condition,String message) {
    if(!condition)throw new AssertionError(message);
  }
  @SubscribeEvent
  public void start(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    String record=System.getenv("DEEPPRIMAL_DRAIN_TRANSFER_RECORD");
    if(record==null)return;
    ServerLevel level=event.getServer().getLevel(Level.END);
    int length=257,offset=16384;
    for(int cx=(offset-1)>>4;cx<=(offset+length+1)>>4;cx++) {
      level.getChunk(cx,0);level.setChunkForced(cx,0,true);
    }
    for(int x=-1;x<=length+1;x++) for(int y=198;y<=201;y++) for(int z=7;z<=9;z++)
      level.setBlock(new BlockPos(offset+x,y,z),Blocks.STONE.defaultBlockState(),2|16);
    var cells=new ArrayList<BlockPos>();
    for(int x=0;x<=length;x++) {
      var p=new BlockPos(offset+x,200,8);cells.add(p);
      level.setBlock(p,EngineRegistry.WATER_BLOCK.get().defaultBlockState()
          .setValue(EngineFluid.UNITS,x==0?15:16),2|16);
    }
    var outlet=new BlockPos(offset+length,199,8);cells.add(outlet);
    level.setBlock(outlet,Blocks.AIR.defaultBlockState(),2|16);
    int before=cells.stream().mapToInt(p->WaterRuntime.units(level,p)).sum();
    var source=cells.getFirst();int turns=0;
    while(WaterRuntime.units(level,outlet)==0 && turns<4) {
      int visits=WaterRuntime.diagnostics(level).drainVisits();
      WaterRuntime.tick(level,source);turns++;
      check(WaterRuntime.diagnostics(level).drainVisits()-visits<=128,"A retained drain turn stays bounded");
      check(cells.stream().mapToInt(p->WaterRuntime.units(level,p)).sum()==before,"Single commit conserves every unit");
    }
    int moved=WaterRuntime.units(level,outlet);
    String observation="DRAIN_TRANSFER_OBSERVED turns="+turns+" from=15 to=0 outlet="+moved
        +" source="+WaterRuntime.units(level,source)+" total="+before+" visits="
        +WaterRuntime.diagnostics(level).drainVisits()+" maxSlice="+WaterRuntime.drainDiagnostics(level).maxSliceVisits();
    System.out.println(observation);
    int expected=Integer.parseInt(System.getenv().getOrDefault("DEEPPRIMAL_DRAIN_TRANSFER_EXPECT","4"));
    check(moved==expected,"Head-limited distant wet-path transfer: expected="+expected+" actual="+moved);
    String result="DRAIN_TRANSFER_NATIVE_PASS "+observation+" conserved=true noInterveningLevelStep=true\n";
    Files.writeString(Path.of(record),result);System.out.print(result);event.getServer().halt(false);
  }
}
