package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.WaterCornerPacket;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import org.lwjgl.system.MemoryUtil;

/** Synthetic real GPU residency and retirement turns; never draws this geometry or measures normal frames. */
final class WaterRetirementBudgetChecks {
  private record Read(byte[] cpu,GpuBuffer gpu){}
  static void run(Minecraft mc,Path out)throws Exception {
    Class<?> type=AnimatedWaterRenderer.class,pt=Class.forName(type.getName()+"$Page");var ctor=type.getDeclaredConstructor();ctor.setAccessible(true);Object renderer=ctor.newInstance();var pc=pt.getDeclaredConstructor(long.class);pc.setAccessible(true);
    var retire=type.getDeclaredMethod("retirePresentation");retire.setAccessible(true);var vf=WaterPublicationFailureChecks.field(WaterGpuMesh.class,"vertices");
    @SuppressWarnings("unchecked")var pages=(Map<Long,Object>)WaterPublicationFailureChecks.field(type,"pages").get(renderer);
    @SuppressWarnings("unchecked")var queue=(Set<Long>)WaterPublicationFailureChecks.field(type,"retireQueue").get(renderer);
    var reads=new ArrayList<Read>();int pageBytes=(4*1024*1024/144)*144,total=pageBytes*8,turns=(total+WaterCornerPacket.SLICE_BYTES-1)/WaterCornerPacket.SLICE_BYTES;long[] nanos=new long[turns],writes={0,0,0};boolean queued=false;
    Object device=RenderSystem.getDevice(),original=null;Field backend=null;
    try {
      var data=MemoryUtil.memCalloc(pageBytes).order(ByteOrder.nativeOrder());
      try {
        long pairs=104L*(1+289+83521+24137569);
        for(int p=0;p<pageBytes;p+=36)WaterCornerPacket.write(data,p,pairs,0,0,0,0,true);
        for(int n=0;n<8;n++) {
          long key=SectionPos.asLong(n,0,0);Object page=pc.newInstance(key);var mesh=new WaterGpuMesh(data,data.remaining()/36);((GpuBuffer)vf.get(mesh)).close();vf.set(mesh,RenderSystem.getDevice().createBuffer(()->"Retirement budget readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,data));
          byte[] cpu=new byte[pageBytes];data.get(0,cpu);WaterPublicationFailureChecks.field(pt,"gpu").set(page,mesh);WaterPublicationFailureChecks.field(pt,"presentation").set(page,cpu);pages.put(key,page);queue.add(key);
        }
      }finally{MemoryUtil.memFree(data);}
      WaterPublicationFailureChecks.field(type,"residentBytes").setLong(renderer,total);WaterPublicationFailureChecks.field(type,"clock").setLong(renderer,201);
      backend=Arrays.stream(device.getClass().getDeclaredFields()).filter(f->f.getType().isInterface()&&f.getType().getSimpleName().equals("GpuDeviceBackend")).findFirst().orElseThrow();backend.setAccessible(true);original=backend.get(device);final Object delegate=original;
      backend.set(device,Proxy.newProxyInstance(backend.getType().getClassLoader(),new Class<?>[]{backend.getType()},(o,m,args)->{
        Object encoder=invoke(m,delegate,args);if(!m.getName().equals("createCommandEncoder"))return encoder;
        return Proxy.newProxyInstance(m.getReturnType().getClassLoader(),new Class<?>[]{m.getReturnType()},(e,op,a)->{
          if(op.getName().equals("writeToBuffer")&&a[0]instanceof GpuBufferSlice){int bytes=((ByteBuffer)a[1]).remaining();writes[0]++;writes[1]+=bytes;writes[2]=Math.max(writes[2],bytes);}
          return invoke(op,encoder,a);
        });
      }));
      try {for(int n=0;n<turns;n++){long started=System.nanoTime();WaterPublicationFailureChecks.call(retire,renderer);nanos[n]=System.nanoTime()-started;}}
      finally {backend.set(device,original);backend=null;}
      if(writes[1]!=total||writes[2]>WaterCornerPacket.SLICE_BYTES)throw new AssertionError("Retirement GPU byte bound differs");
      for(Object page:pages.values()) {
        byte[] cpu=(byte[])WaterPublicationFailureChecks.field(pt,"presentation").get(page);var b=ByteBuffer.wrap(cpu).order(ByteOrder.nativeOrder());
        for(int q=0;q<cpu.length;q+=144)for(int n=0;n<4;n++)if(WaterCornerPacket.source(b,q,n)!=6||WaterCornerPacket.target(b,q,n)!=6)throw new AssertionError("Full residency retirement incomplete");
        var mesh=(WaterGpuMesh)WaterPublicationFailureChecks.field(pt,"gpu").get(page);var read=RenderSystem.getDevice().createBuffer(()->"Full retirement readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,cpu.length);RenderSystem.getDevice().createCommandEncoder().copyToBuffer(((GpuBuffer)vf.get(mesh)).slice(0,cpu.length),read.slice());reads.add(new Read(cpu,read));
      }
      Arrays.sort(nanos);
      RenderSystem.queueFencedTask(()->{
        try {
          int bad=0;var digests=new ArrayList<Map<String,Object>>();for(var r:reads)try(var mapped=RenderSystem.getDevice().createCommandEncoder().mapBuffer(r.gpu,true,false)){byte[] actual=new byte[r.cpu.length];mapped.data().get(0,actual);for(int n=0;n<actual.length;n++)if(actual[n]!=r.cpu[n])bad++;digests.add(Map.of("bytes",actual.length,"gpuSha256",WaterPublicationFailureChecks.sha(actual),"cpuSha256",WaterPublicationFailureChecks.sha(r.cpu)));}
          var result=new LinkedHashMap<String,Object>();result.put("pass",bad==0);result.put("pages",8);result.put("residentBytes",total);result.put("turns",turns);result.put("rangeWrites",writes[0]);result.put("rangeBytes",writes[1]);result.put("maxRangeBytes",writes[2]);result.put("differentBytes",bad);result.put("enqueueCpuMedianNanos",nanos[nanos.length/2]);result.put("enqueueCpuP95Nanos",nanos[(int)Math.ceil(nanos.length*.95)-1]);result.put("enqueueCpuMaxNanos",nanos[nanos.length-1]);result.put("readbacks",digests);result.put("scope","synthetic8page32MiB real GPU buffers; turn CPU enqueue includes scan/copy/command submission, excludes GPU completion/frame time; no2048page/churn/normal-frame or ownerGPU/FPS acceptance");Files.writeString(out.resolve("water-retirement-budget.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");if(bad!=0)throw new AssertionError("GPU retirement differs "+bad);System.out.println("WATER_RETIREMENT_BUDGET_NATIVE_PASS pages=8 residentBytes="+total+" turns="+turns+" rangeWrites="+writes[0]+" maxRangeBytes="+writes[2]+" differentBytes=0 cpuP95Nanos="+result.get("enqueueCpuP95Nanos"));
        }catch(Exception e){throw new RuntimeException(e);}finally{for(var r:reads)r.gpu.close();}
      });queued=true;
    }finally {
      if(backend!=null)backend.set(device,original);type.getMethod("reset").invoke(renderer);((ExecutorService)WaterPublicationFailureChecks.field(type,"workers").get(renderer)).shutdownNow();if(!queued)for(var r:reads)r.gpu.close();
    }
  }
  static Object invoke(Method m,Object target,Object[]args)throws Throwable{try{return m.invoke(target,args);}catch(InvocationTargetException e){throw e.getCause();}}
}
