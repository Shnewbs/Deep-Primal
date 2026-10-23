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
final class WaterSideUvUploadChecks {
  private static Field field(Class<?> c,String n)throws Exception {var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  private record Sample(int direction,long time,long uploaded,boolean stale,int count,Matrix4f texture,GpuBuffer read) {}
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
      for(int direction:new int[]{16,0}) {
        type.getMethod("reset").invoke(renderer);long session=field(type,"session").getLong(renderer);var page=pc.newInstance(0L);
        @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)field(type,"pages").get(renderer);pages.put(0L,page);
        var half=snapshot(8,direction);var full=snapshot(16,direction);long revision=0;
        field(type,"clock").setLong(renderer,0);field(pt,"changed").setBoolean(page,false);upload.invoke(renderer,ready.newInstance(session,++revision,page,half,WaterPatchMesher.build(half,half,true)));
        var gpu=(WaterGpuMesh)field(pt,"gpu").get(page);long capacity=gpu.bytes;var vf=field(WaterGpuMesh.class,"vertices");((GpuBuffer)vf.get(gpu)).close();vf.set(gpu,RenderSystem.getDevice().createBuffer(()->"Side UV readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,capacity));
        for(long time:new long[]{0,100,125,150,200,225}) {
          boolean stale=time==225;var next=time==0||stale?half:full;var old=time==100?half:next;long rev=stale?0:++revision;
          field(type,"clock").setLong(renderer,time);field(pt,"changed").setBoolean(page,false);upload.invoke(renderer,ready.newInstance(session,rev,page,next,WaterPatchMesher.build(next,old,true)));
          gpu=(WaterGpuMesh)field(pt,"gpu").get(page);if(gpu.bytes!=capacity)throw new AssertionError("UV fix changed allocation");int count=field(WaterGpuMesh.class,"indices").getInt(gpu)/6*4;if(count!=20)throw new AssertionError("UV fix changed face coverage");
          var read=RenderSystem.getDevice().createBuffer(()->"Side UV vertex readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,count*36L);var source=(GpuBuffer)vf.get(gpu);RenderSystem.getDevice().createCommandEncoder().copyToBuffer(source.slice(0,count*36L),read.slice());
          samples.add(new Sample(direction,time,field(pt,"uploaded").getLong(page),stale,count,new Matrix4f((Matrix4f)field(pt,"texture").get(page)),read));
        }
      }
      RenderSystem.queueFencedTask(()->{
        var rows=new ArrayList<Map<String,Object>>();var raw=new java.io.ByteArrayOutputStream();int side=0,top=0;
        try {
          for(var sample:samples) {
            int sideCount=0,topCount=0;float span=sample.direction==16?sample.texture.m10():sample.texture.m11(),v0=sample.direction==16?sample.texture.m20():sample.texture.m21();if(!(span>0&&span<1))throw new AssertionError("Missing actual atlas V span");
            try(var mapped=RenderSystem.getDevice().createCommandEncoder().mapBuffer(sample.read,true,false)) {
              var b=mapped.data().order(ByteOrder.nativeOrder());
              for(int v=0;v<sample.count;v++) {
                int i=v*36;int light=b.getShort(i+28)&65535,sky=b.getShort(i+30)&65535;boolean sideFlag=!WaterCornerPacking.spatial(b,i)&&(light&256)!=0,flowFlag=(light&512)!=0;
                if((light&255)!=176 || (sky&255)!=240 || (v>=4&&flowFlag!=(sideFlag&&sample.direction!=16)))throw new AssertionError("Water metadata corrupts light or material");
                float y=b.getFloat(i+4),delta=b.getShort(i+24)/16384f;int offset=WaterCornerPacking.heightOffset(b,i);float t=Math.clamp((sample.time-sample.uploaded-offset)/100f,0,1);float shown=y+delta*(1-t);float uv=b.getFloat(i+20);
                // Mesher emits top quad first, followed by the four vertical sides.
                boolean expectedSide=v>=4;if(sideFlag!=expectedSide)throw new AssertionError("Surface/side tag differs");
                if(sideFlag) {
                  sideCount++;float displayed=Math.clamp(uv-delta*(1-t)*span,v0,v0+span);float expected=v0+Math.clamp(1-shown,0,1)*span;
                  if(Math.abs(displayed-expected)>1e-6)throw new AssertionError("Side UV does not follow displayed height");
                  if(sample.stale&&y>0&&Math.abs(y-1)>1e-6)throw new AssertionError("Stale revision reversed height target");
                }else {if(!WaterCornerPacking.spatial(b,i))throw new AssertionError("Missing corner field");topCount++;}
                byte[] bytes=new byte[36];b.duplicate().position(i).get(bytes);raw.writeBytes(bytes);
              }
            }
            if(sideCount!=16||topCount!=4)throw new AssertionError("Incomplete side/top coverage");side+=sideCount;top+=topCount;
            rows.add(Map.of("direction",sample.direction,"time",sample.time,"uploaded",sample.uploaded,"stale",sample.stale,"vSpan",span,"v0",v0,"sideVertices",sideCount,"surfaceVertices",topCount));
          }
          Files.write(out.resolve("water-side-uv-upload.bin"),raw.toByteArray());Files.writeString(out.resolve("water-side-uv-upload-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\n");
          String record="WATER_SIDE_UV_UPLOAD_NATIVE_PASS samples=12 readbackBytes=8640 sideVertices="+side+" surfaceVertices="+top+" stillAndFlowing=true repeatedUpload=true staleRevision=true lightPreserved=true vertexStride=36 isolatedGpuReleased=true scope=controlledActualProductionUploads\n";Files.writeString(out.resolve("water-side-uv-upload-pass.txt"),record);System.out.print(record);
        }catch(Exception e){throw new RuntimeException(e);}finally {for(var sample:samples)sample.read.close();}
      });queued=true;
    }finally {type.getMethod("reset").invoke(renderer);((ExecutorService)field(type,"workers").get(renderer)).shutdownNow();if(!queued)for(var sample:samples)sample.read.close();}
  }
  private WaterSideUvUploadChecks() {}
}
