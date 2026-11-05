package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Private synchronous server fixture: physical ticket release/reload, no synthetic events. */
@Mod(value="deepprimal",dist=Dist.DEDICATED_SERVER)
public final class PhysicalRegionalChecks {
  private static final int[] SITES=new int[2];
  private final Set<Long> targets=new HashSet<>();
  private int unloadEvents;
  public PhysicalRegionalChecks(){NeoForge.EVENT_BUS.register(this);}
  private static void check(boolean x,String m){if(!x)throw new AssertionError(m);}
  private static Object field(Object o,String n)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
  @SubscribeEvent public void unload(ChunkEvent.Unload e){if(targets.contains(e.getChunk().getPos().pack()))unloadEvents++;}
  private static int units(ServerLevel l,int x,int index,int y){return WaterRuntime.units(l,new BlockPos(x+8+(index&3),y+1,8+(index>>2)));}
  private static long[] total(ServerLevel l,HydrologyData d,Aquifer[] a,int y){
    long[] out=new long[3];var parcels=new ArrayList<WaterAccount.Parcel>();
    parcels.add(d.ocean.snapshot());parcels.add(d.atmosphere.snapshot());for(var q:a){parcels.add(q.ground.snapshot());parcels.add(q.snow.snapshot());}
    for(int x:SITES)for(int i=0;i<16;i++){var pos=new BlockPos(x+8+(i&3),y+1,8+(i>>2));parcels.add(HydrologyRuntime.contents(l,pos,l.getBlockState(pos)));}
    for(var q:parcels){out[0]+=q.water();out[1]+=q.salt();out[2]+=q.sediment();}return out;
  }
  @SubscribeEvent public void start(net.neoforged.neoforge.event.server.ServerStartedEvent event)throws Exception{
    String dest=System.getenv("DEEPPRIMAL_PHYSICAL_RECORD");if(dest==null)return;
    var l=event.getServer().overworld();var src=l.getChunkSource();int y=l.getMinY()+8;
    var terrain=((EngineChunkGenerator)src.getGenerator()).geography();int found=0;
    for(int x=4096;x<4096+256*64&&found<2;x+=64){
      boolean land=true;for(int i=0;i<16;i++){int px=x+8+(i&3),pz=8+(i>>2);var sample=terrain.column(px,pz);if(sample.water()==TerrainEngine.SEA&&terrain.ocean(px,pz)){land=false;break;}}
      if(land)SITES[found++]=x;
    }
    check(found==2,"two bounded non-ocean regional fixtures");
    for(int x:SITES){
      l.setChunkForced(x>>4,0,true);l.getChunk(x>>4,0);
      for(int i=0;i<16;i++){
        int px=x+8+(i&3),pz=8+(i>>2);
        for(int h=y+1;h<l.getMaxY();h++)HydrologyRuntime.write(l,new BlockPos(px,h,pz),Blocks.AIR.defaultBlockState());
        HydrologyRuntime.write(l,new BlockPos(px,y,pz),Blocks.GRASS_BLOCK.defaultBlockState());
      }
    }
    var wf=HydrologyRuntime.class.getDeclaredField("WORK");wf.setAccessible(true);var work=((Map<?,?>)wf.get(null)).get(l);
    var chunks=(Map<?,?>)field(work,"chunks");var regions=(Map<?,?>)field(work,"regions");
    var visit=HydrologyRuntime.class.getDeclaredMethod("visit",ServerLevel.class,work.getClass(),int.class,int.class);visit.setAccessible(true);
    var region=HydrologyRuntime.class.getDeclaredMethod("region",work.getClass(),TerrainEngine.class,int.class,int.class);region.setAccessible(true);
    var d=HydrologyData.get(l);l.setRainLevel(0);long now=l.getGameTime();var aquifers=new Aquifer[2];
    for(int n=0;n<2;n++){aquifers[n]=new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY/2,400000,12000),WaterAccount.Parcel.EMPTY,30,true,now-20,30);d.aquifers.put(HydrologyData.key(SITES[n]+8,8),aquifers[n]);}
    long[] initial=total(l,d,aquifers,y);
    for(int n=0;n<2;n++)for(int i=0;i<8;i++)visit.invoke(null,l,work,SITES[n]+8+(i&3),8+(i>>2));
    for(int x:SITES){int count=0;for(int i=0;i<16;i++)count+=units(l,x,i,y);check(count==32,"ordinary regional32 delivery "+count);}
    int auxiliary=Math.max(32,chunks.size()+6);check(auxiliary<=128,"bounded auxiliary chunk fixture");
    int reloaded=0,evicted=0;var oldChunks=new HashMap<Integer,LevelChunk>();
    for(int cycle=0;cycle<2;cycle++){
      for(int i=0;i<auxiliary;i++){
        int x=8192+i*64; l.setChunkForced(x>>4,0,true);var chunk=l.getChunk(x>>4,0);
        if(oldChunks.containsKey(i)){check(oldChunks.get(i)!=chunk,"reload must produce another LevelChunk");reloaded++;}
        oldChunks.put(i,chunk);targets.add(chunk.getPos().pack());region.invoke(null,work,terrain,x+8,8);
      }
      // Forced tickets can load neighboring chunks too; test observed cache clearing, not a guessed count.
      for(int i=0;i<auxiliary;i++)l.setChunkForced((8192+i*64)>>4,0,false);
      long deadline=System.nanoTime()+8_000_000_000L;boolean gone=false;
      for(int turn=0;turn<1000&&System.nanoTime()<deadline;turn++){
        src.tick(()->true,false);for(int n=0;n<128&&src.pollTask();n++){}
        gone=true;for(int i=0;i<auxiliary;i++)if(src.getChunk((8192+i*64)>>4,0,ChunkStatus.FULL,false)!=null){gone=false;break;}
        gone &= !regions.containsKey(HydrologyData.key(SITES[0]+8,8));
        if(gone)break;Thread.sleep(1);
      }
      check(gone,"server must physically evict auxiliary FULL chunks");evicted+=auxiliary;
      check(!regions.containsKey(HydrologyData.key(SITES[0]+8,8)),"unrelated eviction clears active region cache");
      check(l.getGameTime()==now,"eviction stays in same aquifer interval");
      for(int n=0;n<2;n++)for(int i=8;i<16;i++)visit.invoke(null,l,work,SITES[n]+8+(i&3),8+(i>>2));
      for(int x:SITES){int count=0;for(int i=0;i<16;i++)count+=units(l,x,i,y);check(count==32,"eviction granted another regional allowance "+count);}
      check(Arrays.equals(initial,total(l,d,aquifers,y)),"water/salt/sediment conservation after physical eviction");
    }
    check(unloadEvents>=evicted,"real chunk unload events observed");
    ((ServerLevelData)l.getLevelData()).setGameTime(now+20);
    for(int n=0;n<2;n++)for(int i=0;i<8;i++)visit.invoke(null,l,work,SITES[n]+8+(i&3),8+(i>>2));
    for(int x:SITES){int count=0;for(int i=0;i<16;i++)count+=units(l,x,i,y);check(count==64,"next interval resumes normal32 delivery "+count);}
    check(Arrays.equals(initial,total(l,d,aquifers,y)),"next-interval constituent conservation");
    d.setDirty();event.getServer().saveAllChunks(false,true,true);
    Files.writeString(Path.of(dest),"PHYSICAL_REGIONAL_PASS regions=2 cycles=2 evicted="+evicted+" reloaded="+reloaded+" events="+unloadEvents+" sameIntervalUnitsEach=32 nextIntervalTotalEach=64 waterSaltSediment=true syntheticEvents=false physicalFULL=true controlledClockAdvance=true\n");
  }
}
