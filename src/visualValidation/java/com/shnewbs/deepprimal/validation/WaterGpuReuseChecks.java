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
import org.lwjgl.system.MemoryUtil;

/** Isolated renderer, actual near-capacity GPU allocations, no authoritative world edits. */
final class WaterGpuReuseChecks {
  private static Field field(Class<?> c,String name)throws Exception {
    var f=c.getDeclaredField(name);f.setAccessible(true);return f;
  }
  private static Object renderer,page;
  private static Class<?> type,pageType;
  private static Constructor<?> ready;
  private static Method upload;
  private static long revision;
  private static WaterPatchMesher.Snapshot snapshot(int mode) {
    byte[] units=new byte[WaterPatchMesher.CELLS],solid=new byte[units.length];
    int[] lights=new int[units.length],colors=new int[256];Arrays.fill(colors,0xff3f76e4);
    for(int z=0;z<16;z++)for(int x=0;x<16;x++) {
      solid[WaterPatchMesher.index(x,-1,z)]=1;
      if(mode!=2 || x==8&&z==8)units[WaterPatchMesher.index(x,0,z)]=(byte)(mode==1?8:16);
      if(mode==3 && (x+z)%2==0)units[WaterPatchMesher.index(x,1,z)]=16;
    }
    return new WaterPatchMesher.Snapshot(units,solid,lights,colors);
  }
  private static void push(WaterPatchMesher.Snapshot next,WaterPatchMesher.Snapshot old,long time)throws Exception {
    field(type,"clock").setLong(renderer,time);field(pageType,"changed").setBoolean(page,false);
    upload.invoke(renderer,ready.newInstance(0L,++revision,page,next,WaterPatchMesher.build(next,old,true)));
  }
  private static WaterGpuMesh gpu()throws Exception{return (WaterGpuMesh)field(pageType,"gpu").get(page);}
  private static long bytes()throws Exception{return type.getField("residentBytes").getLong(renderer);}

  static void run(Minecraft mc,Path out)throws Exception {
    type=AnimatedWaterRenderer.class;
    for(var c:List.of(type,WaterGpuMesh.class))System.out.println("VALIDATION_CLASS_ORIGIN class="+c.getName()+" location="+c.getProtectionDomain().getCodeSource().getLocation());
    var ctor=type.getDeclaredConstructor();ctor.setAccessible(true);renderer=ctor.newInstance();
    pageType=Class.forName(type.getName()+"$Page");var pc=pageType.getDeclaredConstructor(long.class);pc.setAccessible(true);
    var rt=Class.forName(type.getName()+"$Ready");ready=rt.getDeclaredConstructor(long.class,long.class,pageType,WaterPatchMesher.Snapshot.class,WaterPatchMesher.Mesh.class);ready.setAccessible(true);
    upload=type.getDeclaredMethod("upload",rt);upload.setAccessible(true);
    @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)field(type,"pages").get(renderer);
    int cx=mc.player.chunkPosition().x(),cz=mc.player.chunkPosition().z(),cy=SectionPos.blockToSectionCoord(mc.player.getBlockY());
    long key=SectionPos.asLong(cx+96,32,cz),ballastKey=SectionPos.asLong(cx,cy,cz);
    page=pc.newInstance(key);pages.put(key,page);Object ballast=pc.newInstance(ballastKey);
    long targetBytes=0,ballastBytes=0;final int updates=16;
    try {
      var full=snapshot(0);var half=snapshot(1);var small=snapshot(2);var large=snapshot(3);
      push(full,full,0);var original=gpu();targetBytes=original.bytes;
      long cap=field(type,"MAX_BYTES").getLong(null);
      ballastBytes=(cap-targetBytes)/144*144;var data=MemoryUtil.memCalloc((int)ballastBytes);
      try {field(pageType,"gpu").set(ballast,new WaterGpuMesh(data,(int)ballastBytes/36));}finally {MemoryUtil.memFree(data);}
      pages.put(ballastKey,ballast);type.getField("residentBytes").setLong(renderer,targetBytes+ballastBytes);
      long before=type.getField("uploads").getLong(renderer),occupied=bytes();
      for(int i=0;i<updates;i++) {
        var next=(i%2==0?half:full);var old=(i%2==0?full:half);push(next,old,100L*(i+1));
        boolean accepted=field(pageType,"snapshot").get(page)==next;
        if(i==0)System.out.println("WATER_GPU_REUSE_OBSERVED targetBytes="+targetBytes+" ballastBytes="+ballastBytes+" resident="+bytes()+" accepted="+accepted);
        if(!accepted || gpu()!=original || bytes()!=occupied || !pages.containsKey(ballastKey))
          throw new AssertionError("Near-capacity same-size water update stalled or displaced closer coverage");
      }
      push(small,small,2000);
      if(gpu()!=original || bytes()!=occupied || field(WaterGpuMesh.class,"indices").getInt(original)!=WaterPatchMesher.build(small,small,true).count()/4*6)
        throw new AssertionError("Smaller update must reuse capacity and update draw count");
      push(full,full,2100);
      if(type.getField("uploads").getLong(renderer)!=before+updates+2 || gpu()!=original)throw new AssertionError("Retained capacity did not accept regrowth");
      if(WaterPatchMesher.build(large,large,true).count()*36L<=targetBytes)throw new AssertionError("Growth fixture must exceed capacity");
      push(large,large,2200);
      if(gpu()!=original || bytes()!=occupied || !field(pageType,"changed").getBoolean(page) || field(pageType,"snapshot").get(page)!=full)
        throw new AssertionError("Oversized update must preserve old coverage and request retry");
      var release=type.getDeclaredMethod("release",pageType);release.setAccessible(true);release.invoke(renderer,ballast);pages.remove(ballastKey);
      push(large,large,2300);
      if(gpu()==original || !((GpuBuffer)field(WaterGpuMesh.class,"vertices").get(original)).isClosed() || field(pageType,"snapshot").get(page)!=large || bytes()>cap)
        throw new AssertionError("Growth did not recover within budget after freeing actual ballast");
      System.out.println("WATER_GPU_REUSE_CAPACITY_PASS updates=16 smaller=true regrowth=true oversizedRetry=true closerCoveragePreserved=true");
    } finally {
      type.getMethod("reset").invoke(renderer);((ExecutorService)field(type,"workers").get(renderer)).shutdownNow();
    }
    if(bytes()!=0)throw new AssertionError("Isolated renderer retained GPU bytes");
    payloadCheck(out,targetBytes,ballastBytes);
  }

  private static void payloadCheck(Path out,long targetBytes,long ballastBytes)throws Exception {
    var device=RenderSystem.getDevice();var input=MemoryUtil.memAlloc(288);
    for(int i=0;i<288;i++)input.put((byte)(i*13+7));input.flip();
    var mesh=new WaterGpuMesh(input,8);var vf=field(WaterGpuMesh.class,"vertices");
    // A separate test-only source adds legal COPY_SRC for readback; production uses VERTEX|COPY_DST.
    ((GpuBuffer)vf.get(mesh)).close();
    var source=device.createBuffer(()->"Water reuse readback source",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,input);
    vf.set(mesh,source);var update=WaterGpuMesh.class.getMethod("update",ByteBuffer.class,int.class);
    input.clear().limit(144);for(int i=0;i<144;i++)input.put(i,(byte)(255-i));
    try {update.invoke(mesh,input,12);throw new AssertionError("Oversized vertex count accepted");}
    catch(InvocationTargetException e) {if(!(e.getCause() instanceof IllegalArgumentException))throw e;}
    update.invoke(mesh,input,4);
    if(field(WaterGpuMesh.class,"indices").getInt(mesh)!=6 || mesh.bytes!=288)throw new AssertionError("Draw count/capacity differs after shrink");
    MemoryUtil.memFree(input);
    var read=device.createBuffer(()->"Water reuse readback",GpuBuffer.USAGE_MAP_READ|GpuBuffer.USAGE_COPY_DST,288);
    device.createCommandEncoder().copyToBuffer(source.slice(),read.slice());
    RenderSystem.queueFencedTask(()->{
      try {
        try(var view=device.createCommandEncoder().mapBuffer(read,true,false)) {
          for(int i=0;i<288;i++)if(view.data().get(i)!=(byte)(i<144?255-i:i*13+7))throw new AssertionError("Water GPU update payload differs at "+i);
        }
        String record="WATER_GPU_REUSE_NATIVE_PASS targetBytes="+targetBytes+" ballastBytes="+ballastBytes+" updates=16 smallerAndRegrowth=true boundedGrowthRetry=true actualGpuPayloadBytes=288 isolatedGpuReleased=true scope=controlledGpuPagesNotLiveSceneFlickerAcceptance\n";
        Files.writeString(out.resolve("water-gpu-reuse.txt"),record);System.out.print(record);
      }catch(Exception e){throw new RuntimeException(e);}finally {read.close();mesh.close();}
    });
  }
  private WaterGpuReuseChecks() {}
}
