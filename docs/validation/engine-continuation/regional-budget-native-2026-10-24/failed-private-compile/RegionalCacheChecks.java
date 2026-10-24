package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.engine.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Private cache-event injection and real loaded-column emission test; not physical chunk eviction. */
@Mod(value="deepprimal",dist=Dist.DEDICATED_SERVER)
public final class RegionalCacheChecks {
  public RegionalCacheChecks() { NeoForge.EVENT_BUS.register(this); }
  private static Object field(Object object,String name) throws Exception {
    var f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);
  }
  private static void check(boolean ok,String what) { if(!ok)throw new AssertionError(what); }
  @SubscribeEvent public void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) throws Exception {
    var destination=System.getenv("DEEPPRIMAL_REGIONAL_RECORD");if(destination==null)return;
    var level=event.getServer().overworld();level.getChunk(0,0);var other=level.getChunk(4,0);
    var wf=HydrologyRuntime.class.getDeclaredField("WORK");wf.setAccessible(true);
    var work=((Map<?,?>)wf.get(null)).get(level);check(work!=null,"loaded work registered");
    var visit=HydrologyRuntime.class.getDeclaredMethod("visit",net.minecraft.server.level.ServerLevel.class,work.getClass(),int.class,int.class);visit.setAccessible(true);
    var chunks=(Map<Long,Object>)field(work,"chunks");var regions=(Map<Long,Object>)field(work,"regions");
    var cursor=chunks.get(0L);check(cursor!=null,"native loaded cursor");
    var otherCursor=chunks.get(other.getPos().pack());check(otherCursor!=null,"second native cursor");
    chunks.clear();chunks.put(0L,cursor);chunks.put(other.getPos().pack(),otherCursor);regions.clear();
    int y=level.getMinY()+8;
    for(int i=0;i<16;i++) {
      int x=8+(i&3),z=8+(i>>2);
      for(int h=y+1;h<level.getMaxY();h++)HydrologyRuntime.write(level,new BlockPos(x,h,z),Blocks.AIR.defaultBlockState());
      HydrologyRuntime.write(level,new BlockPos(x,y,z),Blocks.GRASS_BLOCK.defaultBlockState());
    }
    var data=HydrologyData.get(level);level.setRainLevel(0);level.setGameTime(100);
    var aquifer=new Aquifer(new WaterAccount.Parcel(Aquifer.CAPACITY/2,400000,12000),WaterAccount.Parcel.EMPTY,30,true,80,30);
    data.aquifers.put(HydrologyData.key(8,8),aquifer);
    long initial=aquifer.ground.water()+data.ocean.water()+data.atmosphere.water();
    int total=0,first=0;
    for(int i=0;i<16;i++) {
      if(i==8) {
        first=total;check(first==32,"ordinary first allowance");
        Object regional=regions.values().iterator().next();
        var ctor=regional.getClass().getDeclaredConstructor(double.class);ctor.setAccessible(true);
        regions.put(HydrologyData.key(72,8),ctor.newInstance(0d));
        // Real NeoForge event dispatch, injected without physically evicting the chunk.
        NeoForge.EVENT_BUS.post(new ChunkEvent.Unload(other));
        check(regions.isEmpty(),"unrelated region unload cleared current cache");
      }
      visit.invoke(null,level,work,8+(i&3),8+(i>>2));
      total+=WaterRuntime.units(level,new BlockPos(8+(i&3),y+1,8+(i>>2)));
    }
    check(total==32,"cache reconstruction must not authorize second allowance: "+total);
    check(initial==aquifer.ground.water()+data.ocean.water()+data.atmosphere.water()+total*256L,"exact native volume conservation");
    check(aquifer.lastTick()==100,"cache event does not advance aquifer interval");
    data.setDirty();event.getServer().saveAllChunks(false,true,true);
    Files.writeString(Path.of(destination),"REGIONAL_CACHE_NATIVE_PASS ordinary=32 afterInjectedUnload=32 volume=true aquiferIntervalUnchanged=true physicalEviction=false\n");
  }
}
