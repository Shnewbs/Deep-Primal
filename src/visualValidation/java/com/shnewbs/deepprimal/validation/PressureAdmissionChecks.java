package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.*;
import java.lang.reflect.*;
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

/** Exact native ownership probe; opt-in reflection arranges an already-queued resident origin. */
@Mod(value="deepprimal",dist=Dist.DEDICATED_SERVER)
public final class PressureAdmissionChecks {
  public PressureAdmissionChecks(){NeoForge.EVENT_BUS.register(this);}
  private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
  private static Object call(String name,Class<?>[] types,Object... args)throws Exception{
    Method method=WaterRuntime.class.getDeclaredMethod(name,types);method.setAccessible(true);
    return method.invoke(null,args);
  }
  private static Object field(Object owner,String name)throws Exception{
    Field field=owner.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(owner);
  }
  private static int integer(Object owner,String name)throws Exception{return (Integer)field(owner,name);}
  private static Set<Object> watched(Object budget)throws Exception{
    Set<Object> jobs=Collections.newSetFromMap(new IdentityHashMap<>());
    for(Object bucket:((Map<?,?>)field(budget,"watchedChunks")).values())jobs.addAll((Collection<?>)bucket);
    return jobs;
  }
  @SubscribeEvent
  public void start(net.neoforged.neoforge.event.server.ServerStartedEvent event)throws Exception{
    String path=System.getenv("DEEPPRIMAL_PRESSURE_ADMISSION_RECORD");if(path==null)return;
    ServerLevel level=event.getServer().getLevel(Level.END);
    int offset=16384,length=256;
    for(int cx=(offset-1)>>4;cx<=(offset+length+1)>>4;cx++){
      level.getChunk(cx,0);level.setChunkForced(cx,0,true);
    }
    for(int x=-1;x<=length+1;x++)for(int y=199;y<=201;y++)for(int z=7;z<=9;z++)
      level.setBlock(new BlockPos(offset+x,y,z),Blocks.STONE.defaultBlockState(),2|16);
    List<BlockPos> cells=new ArrayList<>();
    for(int x=0;x<=length;x++){
      BlockPos p=new BlockPos(offset+x,200,8);cells.add(p);
      level.setBlock(p,EngineRegistry.WATER_BLOCK.get().defaultBlockState()
          .setValue(EngineFluid.UNITS,x==0?8:16),2|16);
    }
    int initial=cells.stream().mapToInt(p->WaterRuntime.units(level,p)).sum();
    Object budget=call("budget",new Class<?>[]{ServerLevel.class},level);Class<?> type=budget.getClass();
    BlockPos origin=cells.getFirst();
    call("equalize",new Class<?>[]{ServerLevel.class,BlockPos.class,type},level,origin,budget);
    Map<?,?> searches=(Map<?,?>)field(budget,"searches");Object retained=searches.get(origin);
    check(retained!=null,"A real bounded pressure search remains resident");
    int beforeHead=integer(retained,"head"),beforeTail=integer(retained,"tail");
    check(beforeHead==128 && beforeTail>128,"A native frontier has already passed its first bounded turn");
    // A deferred request and an immediate admission can refer to the same origin.
    call("deferPressure",new Class<?>[]{ServerLevel.class,BlockPos.class,type},level,origin,budget);
    Field limit=type.getDeclaredField("pressureLimit");limit.setAccessible(true);
    limit.setInt(budget,integer(budget,"pressureVisits")); // Isolate admission after this phase's visit quota.
    call("runPressure",new Class<?>[]{ServerLevel.class,type},level,budget);
    Object current=searches.get(origin);boolean same=current==retained;
    int afterHead=integer(current,"head"),afterTail=integer(current,"tail");
    Set<Object> watched=watched(budget);Set<Object> owners=Collections.newSetFromMap(new IdentityHashMap<>());
    owners.addAll(searches.values());int orphans=0;for(Object job:watched)if(!owners.contains(job))orphans++;
    call("finish",new Class<?>[]{type,current.getClass()},budget,current);
    int remaining=watched(budget).size();
    int total=cells.stream().mapToInt(p->WaterRuntime.units(level,p)).sum();
    String observed="PRESSURE_ADMISSION_OBSERVED beforeHead="+beforeHead+" beforeTail="+beforeTail
        +" afterHead="+afterHead+" afterTail="+afterTail+" sameJob="+same+" orphanJobs="+orphans
        +" watchedJobsAfterFinish="+remaining+" total="+total+" conserved="+(total==initial);
    System.out.println(observed);
    check(total==initial && initial==4104,"Admission cannot change water quantity");
    check(same && beforeHead==afterHead && beforeTail==afterTail && orphans==0 && remaining==0,
        "Duplicate admission retains its frontier and releases every watcher: "+observed);
    String record="PRESSURE_ADMISSION_NATIVE_PASS "+observed+" noInterveningLevelStep=true\n";
    Files.writeString(Path.of(path),record);System.out.print(record);event.getServer().halt(false);
  }
}
