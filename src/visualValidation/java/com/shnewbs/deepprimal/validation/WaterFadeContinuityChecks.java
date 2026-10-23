package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;

/** Actual water meshes/GPU ownership, controlled presentation clock, no authoritative world edits. */
final class WaterFadeContinuityChecks {
  private static Field field(Class<?> type,String name)throws Exception {
    var f=type.getDeclaredField(name);f.setAccessible(true);return f;
  }
  private static Object page,renderer;
  private static Class<?> type,pageType,readyType;
  private static Constructor<?> readyConstructor;
  private static Method upload;
  private static long revision;

  private static WaterPatchMesher.Snapshot snapshot(boolean wet) {
    byte[] units=new byte[WaterPatchMesher.CELLS],solid=new byte[units.length];
    int[] light=new int[units.length],colors=new int[256];Arrays.fill(colors,0xff3f76e4);
    for(int z=0;z<16;z++)for(int x=0;x<16;x++) {
      solid[WaterPatchMesher.index(x,-1,z)]=1;
      if(wet)units[WaterPatchMesher.index(x,0,z)]=16;
    }
    return new WaterPatchMesher.Snapshot(units,solid,light,colors);
  }
  private static void push(WaterPatchMesher.Snapshot next,WaterPatchMesher.Snapshot previous,long time)throws Exception {
    field(type,"clock").setLong(renderer,time);field(pageType,"changed").setBoolean(page,false);
    upload.invoke(renderer,readyConstructor.newInstance(0L,++revision,page,next,WaterPatchMesher.build(next,previous,true)));
  }
  private static Object gpu()throws Exception{return field(pageType,"gpu").get(page);}
  private static long bytes()throws Exception{return type.getField("residentBytes").getLong(renderer);}

  static void run(Minecraft mc,Path output)throws Exception {
    type=AnimatedWaterRenderer.class;
    System.out.println("VALIDATION_CLASS_ORIGIN class="+type.getName()+" location="+type.getProtectionDomain().getCodeSource().getLocation());
    var ctor=type.getDeclaredConstructor();ctor.setAccessible(true);renderer=ctor.newInstance();
    pageType=Class.forName(type.getName()+"$Page");var pc=pageType.getDeclaredConstructor(long.class);pc.setAccessible(true);
    long key=SectionPos.asLong(mc.player.chunkPosition().x(),32,mc.player.chunkPosition().z());page=pc.newInstance(key);
    @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)field(type,"pages").get(renderer);pages.put(key,page);
    readyType=Class.forName(type.getName()+"$Ready");readyConstructor=readyType.getDeclaredConstructor(long.class,long.class,pageType,WaterPatchMesher.Snapshot.class,WaterPatchMesher.Mesh.class);readyConstructor.setAccessible(true);
    upload=type.getDeclaredMethod("upload",readyType);upload.setAccessible(true);
    var wet=snapshot(true);var dry=snapshot(false);long retained=0;
    try {
      if(WaterPatchMesher.build(dry,dry,true).count()!=0)throw new AssertionError("Repeated dry snapshot must be empty");
      push(wet,wet,0);if(gpu()==null||bytes()<=0)throw new AssertionError("Actual wet GPU upload required");
      push(dry,wet,100);Object fading=gpu();retained=bytes();long deadline=field(pageType,"cleanup").getLong(page);
      if(fading==null||retained<=0||deadline!=201)throw new AssertionError("Drying page must own its 100 ms fade");
      push(dry,dry,125);
      System.out.println("WATER_DRY_FADE_OBSERVED elapsed=25 remaining=76 retained="+(gpu()==fading)+" bytes="+bytes());
      if(gpu()!=fading||bytes()!=retained||!pages.containsKey(key))
        throw new AssertionError("Empty recapture removed water before its fade completed");
      for(long time:new long[]{150,200}) {
        push(dry,dry,time);
        if(gpu()!=fading||bytes()!=retained||field(pageType,"cleanup").getLong(page)!=deadline)
          throw new AssertionError("Empty recapture must retain GPU ownership without extending fade");
      }
      push(dry,dry,deadline);
      if(gpu()!=null||bytes()!=0||pages.containsKey(key))throw new AssertionError("Completed dry fade must release GPU/page");
      System.out.println("WATER_DRY_FADE_LIFETIME_PASS repeatedEmptyUpdates=3 deadline=201 noExtension=true released=true");
      // A newly wet update interrupts a drying fade from the currently displayed opacity.
      pages.put(key,page);push(wet,wet,300);push(dry,wet,400);push(wet,dry,450);
      @SuppressWarnings("unchecked") var alpha=(List<Map<Long,WaterSurfaceTransition>>)field(type,"alpha").get(renderer);
      int x=field(pageType,"x").getInt(page),y=field(pageType,"y").getInt(page),z=field(pageType,"z").getInt(page);
      var opacity=alpha.get(0).get(BlockPos.asLong(x+8,y,z+8));
      if(opacity==null||Math.abs(opacity.at(450)-.5f)>.00001||opacity.target()!=1||gpu()==null)
        throw new AssertionError("Rewet must retarget the real page from half opacity");
      if(Math.abs(opacity.at(550)-1)>.00001)throw new AssertionError("Rewet opacity did not finish");
      // Preserve the existing newer-edit guard even beyond an old cleanup deadline.
      field(pageType,"changed").setBoolean(page,true);field(type,"clock").setLong(renderer,600);
      Object current=gpu();upload.invoke(renderer,readyConstructor.newInstance(0L,++revision,page,dry,WaterPatchMesher.build(dry,dry,true)));
      if(gpu()!=current)throw new AssertionError("Pending edit guard was lost");
      System.out.println("WATER_REWET_CONTINUITY_PASS displayedAtRetarget=0.5 target=1 newerEditGuard=true");
    } finally {
      type.getMethod("reset").invoke(renderer);((ExecutorService)field(type,"workers").get(renderer)).shutdownNow();
    }
    if(bytes()!=0)throw new AssertionError("Isolated water GPU payload retained");
    String record="WATER_FADE_CONTINUITY_NATIVE_PASS retainedBytes="+retained
        +" repeatedEmptyUpdates=3 fadeMillis=100 noExtension=true rewetContinuous=true newerEditGuard=true isolatedGpuReleased=true scope=actualWaterGpuMeshesControlledClockNotCompleteSceneFlickerAcceptance\n";
    Files.writeString(output.resolve("water-fade-continuity.txt"),record);System.out.print(record);
  }
  private WaterFadeContinuityChecks() {}
}
