package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.WaterPatchMesher;
import com.shnewbs.deepprimal.engine.EngineFluid;
import com.shnewbs.deepprimal.engine.EngineRegistry;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;

/** Disposable-client capture changes are restored before any assertion; no server/world edits. */
final class WaterFlowTextureChecks {
  private static Field field(Class<?> c,String n)throws Exception {var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  private static Method method(Class<?> c,String n,Class<?>... args)throws Exception {var m=c.getDeclaredMethod(n,args);m.setAccessible(true);return m;}
  private record Sample(int direction,int count,float[] cpu,WaterPatchMesher.Snapshot snapshot,GpuBuffer read) {}
  private static List<Float> topUv(WaterPatchMesher.Mesh mesh,int cell) {
    var result=new ArrayList<Float>();var v=mesh.vertices();
    for(int i=0;i<v.length;i+=WaterPatchMesher.STRIDE)if((int)v[i+9]==cell && ((int)v[i+10]&7)==0){result.add(v[i+5]);result.add(v[i+6]);}
    return result;
  }
  static void run(Minecraft mc,Path out)throws Exception {
    var type=AnimatedWaterRenderer.class;
    System.out.println("VALIDATION_CLASS_ORIGIN class="+type.getName()+" location="+type.getProtectionDomain().getCodeSource().getLocation());
    BlockPos found=null;var origin=mc.player.blockPosition();
    search:for(int y=origin.getY();y>=origin.getY()-96;y--)for(int z=-8;z<=8;z++)for(int x=-8;x<=8;x++){
      var p=origin.offset(x,y-origin.getY(),z);
      if(mc.level.hasChunkAt(p) && mc.level.getBlockState(p).is(EngineRegistry.WATER_BLOCK.get()) && !mc.level.getBlockState(p.above()).is(EngineRegistry.WATER_BLOCK.get())){found=p;break search;}
    }
    if(found==null)throw new AssertionError("No loaded water surface in bounded capture search");
    var position=found;var original=mc.level.getBlockState(position);var ctor=type.getDeclaredConstructor();ctor.setAccessible(true);var renderer=ctor.newInstance();
    field(type,"level").set(renderer,mc.level);
    var pt=Class.forName(type.getName()+"$Page");var pc=pt.getDeclaredConstructor(long.class);pc.setAccessible(true);long key=SectionPos.asLong(position);var page=pc.newInstance(key);
    var capture=method(type,"capture",pt);var snapshots=new ArrayList<WaterPatchMesher.Snapshot>();var meshes=new ArrayList<WaterPatchMesher.Mesh>();var samples=new ArrayList<Sample>();boolean queued=false;
    int cx=position.getX()&15,cy=position.getY()&15,cz=position.getZ()&15,cell=cx|(cz<<4)|(cy<<8);
    try {
      try {
        for(int direction:new int[]{0,4,16}) {
          if(!mc.level.setBlock(position,original.setValue(EngineFluid.FLOW,direction),2)) {
            if(mc.level.getBlockState(position).getValue(EngineFluid.FLOW)!=direction)throw new AssertionError("Client fixture direction change rejected");
          }
          var s=(WaterPatchMesher.Snapshot)capture.invoke(renderer,page);snapshots.add(s);meshes.add(WaterPatchMesher.build(s,s,true));
          if(s.water(cx,cy,cz)!=original.getValue(EngineFluid.UNITS))throw new AssertionError("Capture changed water quantity");
        }
      } finally {mc.level.setBlock(position,original,2);}
      if(!mc.level.getBlockState(position).equals(original))throw new AssertionError("Client fixture state was not restored");
      var east=topUv(meshes.get(0),cell);var south=topUv(meshes.get(1),cell);
      System.out.println("WATER_FLOW_CAPTURE_OBSERVED topUvEqual="+east.equals(south)+" sourceDirections=0,4,16 actualCapture=true clientStateRestored=true unitsUnchanged=true position="+position);
      if(east.isEmpty() || east.equals(south))throw new AssertionError("Animated capture/meshing ignored a real water cell FLOW direction change");
      var a=meshes.get(0).vertices();
      for(int n=1;n<3;n++) {
        var b=meshes.get(n).vertices();if(a.length!=b.length)throw new AssertionError("Flow changed face coverage");
        for(int i=0;i<a.length;i+=WaterPatchMesher.STRIDE) {
          if(((int)a[i+10]&7)!=((int)b[i+10]&7))throw new AssertionError("Direction changed face identity");
          for(int j=0;j<14;j++)if(j!=5&&j!=6&&j!=10&&Float.floatToRawIntBits(a[i+j])!=Float.floatToRawIntBits(b[i+j]))throw new AssertionError("Direction changed geometry, light, color or transitions");
        }
        if(!Arrays.equals(snapshots.get(0).units(),snapshots.get(n).units()))throw new AssertionError("Direction changed quantity snapshot");
      }
      @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)field(type,"pages").get(renderer);pages.put(key,page);
      var rt=Class.forName(type.getName()+"$Ready");var ready=rt.getDeclaredConstructor(long.class,long.class,pt,WaterPatchMesher.Snapshot.class,WaterPatchMesher.Mesh.class);ready.setAccessible(true);var upload=method(type,"upload",rt);long revision=0;
      for(int n=0;n<3;n++) {
        var mesh=meshes.get(n);var s=snapshots.get(n);field(type,"clock").setLong(renderer,1000+n*100L);field(pt,"changed").setBoolean(page,false);
        upload.invoke(renderer,ready.newInstance(0L,++revision,page,s,mesh));var gpu=(WaterGpuMesh)field(pt,"gpu").get(page);var vf=field(WaterGpuMesh.class,"vertices");
        if(n==0) {
          ((GpuBuffer)vf.get(gpu)).close();vf.set(gpu,RenderSystem.getDevice().createBuffer(()->"Flow readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,gpu.bytes));
          upload.invoke(renderer,ready.newInstance(0L,++revision,page,s,mesh));
        }
        int count=field(WaterGpuMesh.class,"indices").getInt(gpu)/6*4;
        if(count!=mesh.count() || count*36L>33554432)throw new AssertionError("Flow GPU coverage/budget changed");
        var source=(GpuBuffer)vf.get(gpu);var read=RenderSystem.getDevice().createBuffer(()->"Flow UV readback",GpuBuffer.USAGE_MAP_READ|GpuBuffer.USAGE_COPY_DST,count*36L);
        RenderSystem.getDevice().createCommandEncoder().copyToBuffer(source.slice(0,count*36L),read.slice());samples.add(new Sample(new int[]{0,4,16}[n],count,mesh.vertices(),s,read));
      }
      var atlas=(TextureAtlas)mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
      RenderSystem.queueFencedTask(()->{
        int vertices=0;long bytes=0;var selected=new java.io.ByteArrayOutputStream();var rows=new ArrayList<Map<String,Object>>();
        try {
          for(var s:samples) {
            var sprite=atlas.getSprite(Identifier.withDefaultNamespace(s.direction==16?"block/water_still":"block/water_flow"));int matched=0;
            try(var mapped=RenderSystem.getDevice().createCommandEncoder().mapBuffer(s.read,true,false)) {
              var data=mapped.data().order(ByteOrder.nativeOrder());bytes+=s.count*36L;
              for(int v=0;v<s.count;v++) {
                int i=v*WaterPatchMesher.STRIDE;if((int)s.cpu[i+9]!=cell || ((int)s.cpu[i+10]&7)!=0)continue;
                if(!WaterCornerPacking.spatial(data,v*36))throw new AssertionError("Actual FLOW top missing corner payload");
                int[] targets=WaterCornerPacking.ids(data,v*36,true);
                for(int n=0;n<4;n++) {
                  // Independent canonical mean of the four captured wet cells.
                  double vx=0,vz=0;int x=cx+(n&1),z=cz+(n>>1);
                  for(int dz=-1;dz<=0;dz++)for(int dx=-1;dx<=0;dx++)if(s.snapshot.water(x+dx,cy,z+dz)>0){int d=s.snapshot.direction(x+dx,cy,z+dz);if(d<16){vx+=(float)Math.cos(d*Math.PI/8);vz+=(float)Math.sin(d*Math.PI/8);}}
                  int expected=vx*vx+vz*vz<1e-12?16:((int)Math.round(Math.atan2(vz,vx)*8/Math.PI)&15);
                  if(targets[n]!=expected)throw new AssertionError("Actual captured FLOW corner direction differs");
                }
                byte[] rawVertex=new byte[36];data.duplicate().position(v*36).get(rawVertex);selected.writeBytes(rawVertex);
                rows.add(Map.of("direction",s.direction,"cornerTargets",targets,"packedU",data.getFloat(v*36+16),"packedV",data.getFloat(v*36+20),"x",data.getFloat(v*36),"y",data.getFloat(v*36+4),"z",data.getFloat(v*36+8)));
                matched++;
              }
            }
            if(matched<4)throw new AssertionError("No actual flow GPU top coverage");vertices+=matched;
            System.out.println("WATER_FLOW_GPU_UV_PASS direction="+s.direction+" topVertices="+matched+" sprite="+(s.direction==16?"water_still":"water_flow"));
          }
          String record="WATER_FLOW_TEXTURE_NATIVE_PASS directions=0,4,16 samples=3 topVertices="+vertices+" readbackBytes="+bytes+" actualCapture=true geometryAndQuantityUnchanged=true clientStateRestored=true isolatedGpuReleased=true scope=actualCapturedCellAndGpuUvsNotLiveFlowFlickerAcceptance\n";
          Files.write(out.resolve("selected-flow-gpu-vertices.bin"),selected.toByteArray());
          Files.writeString(out.resolve("selected-flow-gpu-uv.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("sourcePosition",position.toString(),"sourceRestored",original.toString(),"vertexStrideBytes",36,"byteOrder",ByteOrder.nativeOrder().toString(),"vertices",rows))+"\n");
          Files.writeString(out.resolve("water-flow-texture.txt"),record);System.out.print(record);
        }catch(Exception e){throw new RuntimeException(e);}finally {for(var s:samples)s.read.close();}
      });queued=true;
    }finally {
      mc.level.setBlock(position,original,2);type.getMethod("reset").invoke(renderer);((ExecutorService)field(type,"workers").get(renderer)).shutdownNow();if(!queued)for(var s:samples)s.read.close();
    }
  }
  private WaterFlowTextureChecks() {}
}
