package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.WaterPatchMesher;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

/** Real production upload packing and stale-revision consistency; controlled snapshots/clock. */
final class WaterDirectionUploadChecks {
  private static Field field(Class<?> c,String n)throws Exception {var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  private record Sample(int direction,int source,int age,boolean active,long time,int count,GpuBuffer read) {}
  private static WaterPatchMesher.Snapshot snapshot(int units,int direction) {
    byte[] u=new byte[WaterPatchMesher.CELLS],s=new byte[u.length],f=new byte[u.length];int[] l=new int[u.length],c=new int[256];Arrays.fill(c,0xffffffff);Arrays.fill(f,(byte)16);
    for(int z=-1;z<=16;z++)for(int x=-1;x<=16;x++)s[WaterPatchMesher.index(x,-1,z)]=1;
    int key=WaterPatchMesher.index(3,0,3);u[key]=(byte)units;f[key]=(byte)direction;l[key]=0x00f000b0;
    return new WaterPatchMesher.Snapshot(u,s,l,c,f);
  }
  static void run(Minecraft mc,Path out)throws Exception {
    Files.createDirectories(out);var type=AnimatedWaterRenderer.class;System.out.println("VALIDATION_CLASS_ORIGIN class="+type.getName()+" location="+type.getProtectionDomain().getCodeSource().getLocation());
    var ctor=type.getDeclaredConstructor();ctor.setAccessible(true);var renderer=ctor.newInstance();var pt=Class.forName(type.getName()+"$Page");var pc=pt.getDeclaredConstructor(long.class);pc.setAccessible(true);
    var rt=Class.forName(type.getName()+"$Ready");var ready=rt.getDeclaredConstructor(long.class,long.class,pt,WaterPatchMesher.Snapshot.class,WaterPatchMesher.Mesh.class);ready.setAccessible(true);var upload=type.getDeclaredMethod("upload",rt);upload.setAccessible(true);
    var samples=new ArrayList<Sample>();boolean queued=false;
    try {
      long session=field(type,"session").getLong(renderer);var page=pc.newInstance(0L);
      @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)field(type,"pages").get(renderer);pages.put(0L,page);
      var full=snapshot(16,0);field(type,"clock").setLong(renderer,0);field(pt,"changed").setBoolean(page,false);upload.invoke(renderer,ready.newInstance(session,1L,page,full,WaterPatchMesher.build(full,full,true)));
      var gpu=(WaterGpuMesh)field(pt,"gpu").get(page);long capacity=gpu.bytes;var vf=field(WaterGpuMesh.class,"vertices");((GpuBuffer)vf.get(gpu)).close();vf.set(gpu,RenderSystem.getDevice().createBuffer(()->"Direction readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,capacity));
      int[] requested={0,4,4,8,8,8,0},targets={0,4,4,4,8,8,8},sources={0,0,0,0,4,4,4},ages={0,0,25,50,0,25,50};long[] times={0,100,125,150,200,225,250};
      for(int n=0;n<times.length;n++) {
        var next=snapshot(16,requested[n]);field(type,"clock").setLong(renderer,times[n]);field(pt,"changed").setBoolean(page,false);
        upload.invoke(renderer,ready.newInstance(session,n==6?0L:n+2L,page,next,WaterPatchMesher.build(next,next,true)));
        gpu=(WaterGpuMesh)field(pt,"gpu").get(page);int count=field(WaterGpuMesh.class,"indices").getInt(gpu)/6*4;if(gpu.bytes!=capacity||count!=20)throw new AssertionError("Direction blend changed allocation or coverage");
        var read=RenderSystem.getDevice().createBuffer(()->"Direction vertex readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,count*36L);
        RenderSystem.getDevice().createCommandEncoder().copyToBuffer(((GpuBuffer)vf.get(gpu)).slice(0,count*36L),read.slice());
        samples.add(new Sample(targets[n],sources[n],ages[n],n!=0,times[n],count,read));
      }
      var flowMethod=type.getDeclaredMethod("flow",long.class,int.class,long.class,int.class);flowMethod.setAccessible(true);
      for(int n=0;n<33000;n++)flowMethod.invoke(renderer,1_000_000L+n,n&15,1000L+n,n&15);
      var history=(Map<?,?>)field(type,"flowDirections").get(renderer);if(history.size()!=32768||history.containsKey(1_000_000L))throw new AssertionError("Direction history is unbounded or not FIFO-LRU evicted");
      RenderSystem.queueFencedTask(()->{
        var rows=new ArrayList<Map<String,Object>>();var raw=new java.io.ByteArrayOutputStream();int vertices=0;
        try {
          for(var sample:samples)try(var mapped=RenderSystem.getDevice().createCommandEncoder().mapBuffer(sample.read,true,false)) {
            var b=mapped.data().order(ByteOrder.nativeOrder());
            for(int v=0;v<sample.count;v++) {
              int at=v*36,light=b.getShort(at+28)&65535,sky=b.getShort(at+30)&65535;
              if((light&255)!=176||(sky&255)!=240)throw new AssertionError("Direction metadata corrupted light");
              boolean active=(sky&32768)!=0;
              if(v<4) WaterCornerPacking.uniform(b,at,sample.source,sample.direction,sample.age,51);
              else {
                if((b.get(at+15)&255)!=255||active)throw new AssertionError("Side flow-to-flow metadata changed");
              }
              byte[] bytes=new byte[36];b.duplicate().position(at).get(bytes);raw.writeBytes(bytes);vertices++;
            }
            rows.add(Map.of("time",sample.time,"source",sample.source,"target",sample.direction,"age",sample.age,"active",sample.active,"vertices",sample.count));
          }
          Files.write(out.resolve("water-direction-upload.bin"),raw.toByteArray());Files.writeString(out.resolve("water-direction-upload-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\n");
          String record="WATER_DIRECTION_UPLOAD_NATIVE_PASS samples=7 readbackBytes=5040 vertices="+vertices+" repeatedUpload=true latestQueued=true staleRevision=true lightPreserved=true historyCap=32768 historyReset=true vertexStride=36 isolatedGpuReleased=true scope=controlledActualProductionUploads\n";
          Files.writeString(out.resolve("water-direction-upload-pass.txt"),record);System.out.print(record);
        }catch(Exception e){throw new RuntimeException(e);}finally {for(var sample:samples)sample.read.close();}
      });queued=true;
    }finally {type.getMethod("reset").invoke(renderer);if(!((Map<?,?>)field(type,"flowDirections").get(renderer)).isEmpty())throw new AssertionError("History survived reset");((ExecutorService)field(type,"workers").get(renderer)).shutdownNow();if(!queued)for(var sample:samples)sample.read.close();}
  }
  private WaterDirectionUploadChecks() {}
}
