package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;

/** Actual page uploads/readback with controlled snapshots/clock; not a live-flow pixel test. */
final class WaterPartialFadeChecks {
  private static Field field(Class<?> c,String n)throws Exception {var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  private record Sample(long time,long uploaded,int count,float expected,GpuBuffer read) {}
  private static Object renderer,page;
  private static Class<?> type,pageType;
  private static Constructor<?> ready;
  private static Method upload;
  private static long revision;
  private static WaterPatchMesher.Snapshot snapshot(boolean both) {
    byte[] units=new byte[WaterPatchMesher.CELLS],solid=new byte[units.length];
    int[] lights=new int[units.length],colors=new int[256];Arrays.fill(colors,0xff3f76e4);
    for(int z=0;z<16;z++)for(int x=0;x<16;x++)solid[WaterPatchMesher.index(x,-1,z)]=1;
    if(both)units[WaterPatchMesher.index(3,0,3)]=16;units[WaterPatchMesher.index(12,0,12)]=16;
    return new WaterPatchMesher.Snapshot(units,solid,lights,colors);
  }
  private static void push(WaterPatchMesher.Snapshot next,WaterPatchMesher.Snapshot old,long time)throws Exception {
    field(type,"clock").setLong(renderer,time);field(pageType,"changed").setBoolean(page,false);
    upload.invoke(renderer,ready.newInstance(0L,++revision,page,next,WaterPatchMesher.build(next,old,true)));
  }
  private static WaterGpuMesh gpu()throws Exception{return (WaterGpuMesh)field(pageType,"gpu").get(page);}
  private static void sample(List<Sample> samples,long time,float expected)throws Exception {
    var mesh=gpu();int count=field(WaterGpuMesh.class,"indices").getInt(mesh)/6*4;
    if(count!=40)throw new AssertionError("Mixed wet/dry page discarded fading faces before their deadline: vertices="+count+" at="+time);
    var source=(GpuBuffer)field(WaterGpuMesh.class,"vertices").get(mesh);
    var read=RenderSystem.getDevice().createBuffer(()->"Partial fade vertex readback",GpuBuffer.USAGE_MAP_READ|GpuBuffer.USAGE_COPY_DST,count*36L);
    try {RenderSystem.getDevice().createCommandEncoder().copyToBuffer(source.slice(0,count*36L),read.slice());}
    catch(Throwable failure){read.close();throw failure;}
    samples.add(new Sample(time,field(pageType,"uploaded").getLong(page),count,expected,read));
  }
  static void run(Minecraft mc,Path out)throws Exception {
    type=AnimatedWaterRenderer.class;
    System.out.println("VALIDATION_CLASS_ORIGIN class="+type.getName()+" location="+type.getProtectionDomain().getCodeSource().getLocation());
    var ctor=type.getDeclaredConstructor();ctor.setAccessible(true);renderer=ctor.newInstance();
    pageType=Class.forName(type.getName()+"$Page");var pc=pageType.getDeclaredConstructor(long.class);pc.setAccessible(true);
    long key=SectionPos.asLong(mc.player.chunkPosition().x(),32,mc.player.chunkPosition().z());page=pc.newInstance(key);
    @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)field(type,"pages").get(renderer);pages.put(key,page);
    var rt=Class.forName(type.getName()+"$Ready");ready=rt.getDeclaredConstructor(long.class,long.class,pageType,WaterPatchMesher.Snapshot.class,WaterPatchMesher.Mesh.class);ready.setAccessible(true);
    upload=type.getDeclaredMethod("upload",rt);upload.setAccessible(true);
    var both=snapshot(true);var one=snapshot(false);var samples=new ArrayList<Sample>();boolean queued=false;
    try {
      push(both,both,0);var original=gpu();long capacity=original.bytes;
      // A test-only buffer adds legal COPY_SRC for actual readback. Next upload initializes all bytes.
      var vf=field(WaterGpuMesh.class,"vertices");((GpuBuffer)vf.get(original)).close();
      vf.set(original,RenderSystem.getDevice().createBuffer(()->"Partial fade readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,capacity));
      push(one,both,100);long deadline=field(pageType,"cleanup").getLong(page);
      if(deadline!=201)throw new AssertionError("Expected original fade deadline");
      push(one,one,125);
      System.out.println("WATER_PARTIAL_FADE_OBSERVED vertices="+(field(WaterGpuMesh.class,"indices").getInt(gpu())/6*4)+" elapsed=25 originalVertices=40 originalDeadline=201");
      sample(samples,125,.75f);if(field(pageType,"cleanup").getLong(page)!=deadline)throw new AssertionError("Recapture extended fade deadline");
      @SuppressWarnings("unchecked") var retained=(Map<Integer,Object>)field(pageType,"fading").get(page);
      long retainedBytes=0;
      for(var face:retained.values()) {
        retainedBytes+=((float[])field(face.getClass(),"vertices").get(face)).length*4L;
        if(field(face.getClass(),"revision").getLong(face)!=2)throw new AssertionError("Retained geometry lost original capture revision");
      }
      if(retained.size()!=5 || retainedBytes!=1120 || retainedBytes>original.bytes*14/9)throw new AssertionError("Retained face CPU payload exceeds admitted geometry");
      push(one,one,150);sample(samples,150,.5f);
      var retainedAgain=(Map<?,?>)field(pageType,"fading").get(page);
      for(var entry:retained.entrySet())if(retainedAgain.get(entry.getKey())!=entry.getValue())throw new AssertionError("Unchanged retained CPU arrays were copied");
      // Interrupt drying at half opacity, then recapture twice during rewetting.
      push(both,one,150);sample(samples,150,.5f);
      if(!((Map<?,?>)field(pageType,"fading").get(page)).isEmpty())throw new AssertionError("Rewet retained obsolete dying faces");
      for(long time:new long[]{175,225,250}) {
        push(both,both,time);sample(samples,time,time==175?.625f:time==225?.875f:1f);
        if(time<250 && field(pageType,"cleanup").getLong(page)!=251)throw new AssertionError("Rewet recapture extended transition deadline");
      }
      if(gpu()!=original || type.getField("residentBytes").getLong(renderer)!=capacity)throw new AssertionError("Partial retention changed allocation ownership");
      // Completed partial fades release CPU history/draw faces while the other patch stays wet.
      push(one,both,300);push(one,one,325);push(one,one,401);
      if(field(WaterGpuMesh.class,"indices").getInt(gpu())!=30 || !((Map<?,?>)field(pageType,"fading").get(page)).isEmpty() || gpu()!=original)
        throw new AssertionError("Completed partial fade retained dead faces/history or lost steady water");
      System.out.println("WATER_PARTIAL_CLEANUP_PASS steadyVertices=20 retainedCpuBytes=0 arraysReused=true originalRetainedBytes=1120");
      System.out.println("WATER_PARTIAL_FADE_LIFETIME_PASS retainedVertices=40 originalDeadline=201 rewetDeadline=251 samples=6 sameAllocation=true");
      RenderSystem.queueFencedTask(()->{
        try {
          for(var s:samples) {
            int fading=0,steady=0;
            try(var mapped=RenderSystem.getDevice().createCommandEncoder().mapBuffer(s.read,true,false)) {
              var data=mapped.data().order(ByteOrder.nativeOrder());
              for(int v=0;v<s.count;v++) {
                int base=v*36;float x=data.getFloat(base),z=data.getFloat(base+8);
                float from=data.get(base+32)/127f,target=WaterCornerPacking.opacityTarget(data,base)/127f;
                float t=Math.clamp((s.time-s.uploaded-data.get(base+34))/100f,0,1),opacity=from+(target-from)*t;
                if(x>=3&&x<=4&&z>=3&&z<=4){fading++;if(Math.abs(opacity-s.expected)>.005f)throw new AssertionError("Retained/rewet GPU opacity differs at "+s.time+": "+opacity+" expected "+s.expected);}
                else if(x>=12&&x<=13&&z>=12&&z<=13){steady++;if(Math.abs(opacity-1)>.00001f)throw new AssertionError("Steady water changed opacity");}
                else throw new AssertionError("Unexpected page vertex");
              }
            }
            if(fading!=20||steady!=20)throw new AssertionError("Partial face coverage differs");
            System.out.println("WATER_PARTIAL_GPU_SAMPLE_PASS time="+s.time+" fadingVertices="+fading+" steadyVertices="+steady+" opacity="+s.expected);
          }
        }finally {for(var s:samples)s.read.close();}
        try {
          String record="WATER_PARTIAL_FADE_NATIVE_PASS samples=6 retainedVertices=40 readbackBytes=8640 originalDeadline=201 rewetDeadline=251 sameAllocation=true completedPartialReleased=true retainedArraysReused=true cpuBytes=1120 isolatedGpuReleased=true scope=controlledPageGpuGeometryOpacityNotLiveScenePixels\n";
          Files.writeString(out.resolve("water-partial-fade.txt"),record);System.out.print(record);
        }catch(Exception e){throw new RuntimeException(e);}
      });queued=true;
    }finally {
      type.getMethod("reset").invoke(renderer);((ExecutorService)field(type,"workers").get(renderer)).shutdownNow();
      if(!queued)for(var s:samples)s.read.close();
    }
  }
  private WaterPartialFadeChecks() {}
}
