package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.WaterPatchMesher;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;

/** Synchronous backend rejection before a real range write; retries are explicitly driven by this fixture. */
final class WaterPublicationFailureChecks {
  static Field field(Class<?> c,String name)throws Exception {var f=c.getDeclaredField(name);f.setAccessible(true);return f;}
  static Object call(Method method,Object target,Object... args)throws Exception {
    try{return method.invoke(target,args);}catch(InvocationTargetException e){if(e.getCause()instanceof Exception x)throw x;throw e;}
  }
  static String sha(byte[] bytes)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}
  private record Read(String path,int origin,int axis,int rejectedWrite,String phase,int side,byte[] cpu,GpuBuffer gpu){}
  private static final class Reject implements AutoCloseable {
    final Object device=RenderSystem.getDevice(),original;final Field backend;int writes;boolean rejected;
    Reject(GpuBuffer target,int at)throws Exception {
      backend=Arrays.stream(device.getClass().getDeclaredFields()).filter(f->f.getType().isInterface()&&f.getType().getSimpleName().equals("GpuDeviceBackend")).findFirst().orElseThrow();backend.setAccessible(true);original=backend.get(device);
      Object proxy=Proxy.newProxyInstance(backend.getType().getClassLoader(),new Class<?>[]{backend.getType()},(o,m,args)->{
        Object value=invoke(m,original,args);
        if(!m.getName().equals("createCommandEncoder"))return value;
        return Proxy.newProxyInstance(m.getReturnType().getClassLoader(),new Class<?>[]{m.getReturnType()},(encoder,op,a)->{
          if(op.getName().equals("writeToBuffer")&&a[0]instanceof GpuBufferSlice slice&&(target==null||slice.buffer()==target)&&++writes==at){rejected=true;throw new IllegalStateException("INJECTED_WATER_PUBLICATION_REJECTION");}
          return invoke(op,value,a);
        });
      });backend.set(device,proxy);
    }
    static Object invoke(Method m,Object target,Object[]args)throws Throwable{try{return m.invoke(target,args);}catch(InvocationTargetException e){throw e.getCause();}}
    public void close()throws Exception{backend.set(device,original);}
  }
  private static final class Session implements AutoCloseable {
    final Class<?> type=AnimatedWaterRenderer.class,pt=Class.forName(type.getName()+"$Page"),rt=Class.forName(type.getName()+"$Ready");
    final Object renderer;final Object[] page=new Object[2];final Method upload,sync,retire;final Constructor<?> ready;final int origin,axis,leftX,leftZ;long revision;
    Session(int origin,int axis)throws Exception {
      this.origin=origin;this.axis=axis;leftX=origin+(axis==0?15:3);leftZ=-origin+(axis==0?3:15);
      var c=type.getDeclaredConstructor();c.setAccessible(true);renderer=c.newInstance();var pc=pt.getDeclaredConstructor(long.class);pc.setAccessible(true);
      ready=rt.getDeclaredConstructor(long.class,long.class,pt,WaterPatchMesher.Snapshot.class,WaterPatchMesher.Mesh.class);ready.setAccessible(true);
      upload=type.getDeclaredMethod("upload",rt);upload.setAccessible(true);sync=type.getDeclaredMethod("synchronizeCornerNeighbors",pt);sync.setAccessible(true);retire=type.getDeclaredMethod("retirePresentation");retire.setAccessible(true);
      @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)field(type,"pages").get(renderer);
      for(int side=0;side<2;side++) {int x=leftX+(axis==0?side:0),z=leftZ+(axis==1?side:0);long key=SectionPos.asLong(Math.floorDiv(x,16),0,Math.floorDiv(z,16));page[side]=pc.newInstance(key);pages.put(key,page[side]);put(side,0,4,0);var mesh=gpu(side);var vf=field(WaterGpuMesh.class,"vertices");((GpuBuffer)vf.get(mesh)).close();vf.set(mesh,RenderSystem.getDevice().createBuffer(()->"Publication readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,mesh.bytes));put(side,0,4,0);}
    }
    WaterPatchMesher.Snapshot snapshot(int side,int a,int b)throws Exception {
      int px=field(pt,"x").getInt(page[side]),pz=field(pt,"z").getInt(page[side]);byte[] u=new byte[WaterPatchMesher.CELLS],flows=new byte[u.length];Arrays.fill(flows,(byte)16);int[] light=new int[u.length],colors=new int[256];Arrays.fill(colors,0xffffffff);
      for(int z=-1;z<=16;z++)for(int x=-1;x<=16;x++)for(int lane:new int[]{0,4})for(int s=0;s<2;s++) {
        int wx=leftX+(axis==0?s:lane),wz=leftZ+(axis==1?s:lane);if(px+x==wx&&pz+z==wz){int i=WaterPatchMesher.index(x,0,z);u[i]=16;flows[i]=(byte)(s==0?a:b);light[i]=0x00f000b0;}
      }
      return new WaterPatchMesher.Snapshot(u,new byte[u.length],light,colors,flows);
    }
    void clock(long time)throws Exception{field(type,"clock").setLong(renderer,time);}
    void put(int side,int a,int b,long time)throws Exception {clock(time);field(pt,"changed").setBoolean(page[side],false);var s=snapshot(side,a,b);call(upload,renderer,ready.newInstance(field(type,"session").getLong(renderer),++revision,page[side],s,WaterPatchMesher.build(s,s,true)));}
    WaterGpuMesh gpu(int side)throws Exception{return(WaterGpuMesh)field(pt,"gpu").get(page[side]);}
    GpuBuffer buffer(int side)throws Exception{return(GpuBuffer)field(WaterGpuMesh.class,"vertices").get(gpu(side));}
    void read(List<Read> reads,String path,int fail,String phase)throws Exception {
      for(int side=0;side<2;side++){byte[] cpu=((byte[])field(pt,"presentation").get(page[side])).clone();var b=RenderSystem.getDevice().createBuffer(()->"Publication failure readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,cpu.length);RenderSystem.getDevice().createCommandEncoder().copyToBuffer(buffer(side).slice(0,cpu.length),b.slice());reads.add(new Read(path,origin,axis,fail,phase,side,cpu,b));}
    }
    public void close()throws Exception{type.getMethod("reset").invoke(renderer);((ExecutorService)field(type,"workers").get(renderer)).shutdownNow();}
  }
  static void run(Minecraft mc,Path out)throws Exception {
    var reads=new ArrayList<Read>();int cases=0;
    try {
      for(String path:List.of("neighbor","retirement"))for(int origin:new int[]{0,29999968})for(int axis=0;axis<2;axis++)for(int fail:new int[]{1,2})try(var s=new Session(origin,axis)){
        if(path.equals("retirement")){s.put(0,4,8,100);s.clock(201);}
        boolean caught=false;
        try(var reject=new Reject(path.equals("neighbor")?s.buffer(1):null,fail)) {
          try{if(path.equals("neighbor"))s.put(0,4,8,100);else call(s.retire,s.renderer);}
          catch(IllegalStateException expected){if(!expected.getMessage().equals("INJECTED_WATER_PUBLICATION_REJECTION"))throw expected;caught=true;}
          if(!caught||!reject.rejected||reject.writes!=fail)throw new AssertionError("Injection missed production range path "+path);
        }
        s.read(reads,path,fail,"after-rejection");
        if(path.equals("neighbor"))call(s.sync,s.renderer,s.page[0]);else call(s.retire,s.renderer);
        s.read(reads,path,fail,"after-retry");cases++;
      }
    }catch(Throwable failure){for(var r:reads)r.gpu.close();throw failure;}
    final int count=cases;
    RenderSystem.queueFencedTask(()->{
      try {
        var rows=new ArrayList<Map<String,Object>>();var bytes=new java.io.ByteArrayOutputStream();int bad=0;
        for(var r:reads)try(var mapped=RenderSystem.getDevice().createCommandEncoder().mapBuffer(r.gpu,true,false)){
          byte[] gpu=new byte[r.cpu.length];mapped.data().get(0,gpu);int differences=0;for(int n=0;n<gpu.length;n++)if(gpu[n]!=r.cpu[n])differences++;
          int at=bytes.size();bytes.writeBytes(r.cpu);bytes.writeBytes(gpu);var row=new LinkedHashMap<String,Object>();row.put("path",r.path);row.put("origin",r.origin);row.put("axis",r.axis);row.put("rejectedWrite",r.rejectedWrite);row.put("phase",r.phase);row.put("side",r.side);row.put("bytes",gpu.length);row.put("offset",at);row.put("cpuSha256",sha(r.cpu));row.put("gpuSha256",sha(gpu));row.put("differentBytes",differences);rows.add(row);bad+=differences;
        }
        Files.write(out.resolve("water-publication-failure-readbacks.bin"),bytes.toByteArray());Files.writeString(out.resolve("water-publication-failure-details.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\n");
        boolean pass=bad==0;String result="WATER_PUBLICATION_FAILURE_RESULT pass="+pass+" cases="+count+" readbacks="+reads.size()+" differentBytes="+bad+" injectedBeforeBackendWrite=true manualRetry=true scope=controlledSynchronousRejectionNotAsyncDeviceLossOrWholeCohortAtomicity\n";Files.writeString(out.resolve("water-publication-failure-result.txt"),result);System.out.print(result);if(pass)System.out.println("WATER_PUBLICATION_FAILURE_NATIVE_PASS cases="+count+" acceptedCpuMatchesGpuAfterRejectionAndRetry=true");
      }catch(Exception e){throw new RuntimeException(e);}finally{for(var r:reads)r.gpu.close();}
    });
  }
}
