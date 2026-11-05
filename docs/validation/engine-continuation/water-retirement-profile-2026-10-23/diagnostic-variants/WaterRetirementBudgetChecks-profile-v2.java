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
  static void run(Minecraft mc,Path out)throws Exception { run(mc,out,8,false); }
  static void run(Minecraft mc,Path out,int pageCount)throws Exception { run(mc,out,pageCount,false); }
  static void run(Minecraft mc,Path out,int pageCount,boolean duplicateSetup)throws Exception {
    if(pageCount!=8&&pageCount!=2048)throw new IllegalArgumentException();
    Class<?> type=AnimatedWaterRenderer.class,pt=Class.forName(type.getName()+"$Page");var ctor=type.getDeclaredConstructor();ctor.setAccessible(true);Object renderer=ctor.newInstance();var pc=pt.getDeclaredConstructor(long.class);pc.setAccessible(true);
    var retire=type.getDeclaredMethod("retirePresentation");retire.setAccessible(true);var vf=WaterPublicationFailureChecks.field(WaterGpuMesh.class,"vertices");
    @SuppressWarnings("unchecked")var pages=(Map<Long,Object>)WaterPublicationFailureChecks.field(type,"pages").get(renderer);
    @SuppressWarnings("unchecked")var queue=(Set<Long>)WaterPublicationFailureChecks.field(type,"retireQueue").get(renderer);
    var reads=new ArrayList<Read>();int total=(32*1024*1024/144)*144,quads=total/144,baseQuads=quads/pageCount,extra=quads%pageCount,maxPageBytes=(baseQuads+(extra>0?1:0))*144,turns=(total+WaterCornerPacket.SLICE_BYTES-1)/WaterCornerPacket.SLICE_BYTES;long[] nanos=new long[turns],writes={0,0,0,0,0};var samples=new ArrayList<Map<String,Long>>();var stagingEvents=new ArrayList<Map<String,Object>>();int[] activeTurn={-1};long[] stagingWraps={0};Class<?> vk=Class.forName("net.vulkanmod.vulkan.Vulkan");var getStaging=vk.getMethod("getStagingBuffer");Object initialStaging=getStaging.invoke(null);var usedBytes=initialStaging.getClass().getMethod("getUsedBytes");var capacity=initialStaging.getClass().getMethod("getBufferSize");var collectors=java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();boolean queued=false;
    Object device=RenderSystem.getDevice(),original=null;Field backend=null;
    try {
      var data=MemoryUtil.memCalloc(maxPageBytes).order(ByteOrder.nativeOrder());
      try {
        long pairs=104L*(1+289+83521+24137569);
        for(int p=0;p<maxPageBytes;p+=36)WaterCornerPacket.write(data,p,pairs,0,0,0,0,true);
        for(int n=0;n<pageCount;n++) {
          int pageBytes=(baseQuads+(n<extra?1:0))*144;data.limit(pageBytes);
          long key=SectionPos.asLong(n,0,0);Object page=pc.newInstance(key);
          // Private fixture buffers need COPY_SRC. Use one seed quad rather than uploading all
          // resident bytes twice merely to replace the constructor's non-readable allocation.
          if(!duplicateSetup)data.limit(144);
          var mesh=new WaterGpuMesh(data,data.remaining()/36);((GpuBuffer)vf.get(mesh)).close();data.limit(pageBytes);
          vf.set(mesh,RenderSystem.getDevice().createBuffer(()->"Retirement budget readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,data));
          WaterPublicationFailureChecks.field(WaterGpuMesh.class,"bytes").setLong(mesh,pageBytes);WaterPublicationFailureChecks.field(WaterGpuMesh.class,"indices").setInt(mesh,pageBytes/144*6);
          if(mesh.bytes!=pageBytes)throw new AssertionError("Fixture allocation metadata differs");
          byte[] cpu=new byte[pageBytes];data.get(0,cpu);WaterPublicationFailureChecks.field(pt,"gpu").set(page,mesh);WaterPublicationFailureChecks.field(pt,"presentation").set(page,cpu);pages.put(key,page);queue.add(key);
        }
      }finally{MemoryUtil.memFree(data);}
      WaterPublicationFailureChecks.field(type,"residentBytes").setLong(renderer,total);WaterPublicationFailureChecks.field(type,"clock").setLong(renderer,201);
      backend=Arrays.stream(device.getClass().getDeclaredFields()).filter(f->f.getType().isInterface()&&f.getType().getSimpleName().equals("GpuDeviceBackend")).findFirst().orElseThrow();backend.setAccessible(true);original=backend.get(device);final Object delegate=original;
      backend.set(device,Proxy.newProxyInstance(backend.getType().getClassLoader(),new Class<?>[]{backend.getType()},(o,m,args)->{
        long started=System.nanoTime();Object encoder=invoke(m,delegate,args);if(!m.getName().equals("createCommandEncoder"))return encoder;writes[4]+=System.nanoTime()-started;
        return Proxy.newProxyInstance(m.getReturnType().getClassLoader(),new Class<?>[]{m.getReturnType()},(e,op,a)->{
          if(op.getName().equals("writeToBuffer")&&a[0]instanceof GpuBufferSlice){int bytes=((ByteBuffer)a[1]).remaining();writes[0]++;writes[1]+=bytes;writes[2]=Math.max(writes[2],bytes);Object staging=getStaging.invoke(null);long before=(long)usedBytes.invoke(staging),cap=(long)capacity.invoke(staging);boolean wrap=bytes>cap-((before+3)&~3L);if(wrap)stagingWraps[0]++;
            long begin=System.nanoTime();try{return invoke(op,encoder,a);}finally{long elapsed=System.nanoTime()-begin;writes[3]+=elapsed;if(wrap||writes[0]<=8){var event=new LinkedHashMap<String,Object>();event.put("turn",activeTurn[0]);event.put("write",writes[0]);event.put("bytes",bytes);event.put("stagingUsedBefore",before);event.put("stagingCapacity",cap);event.put("predictedWrap",wrap);event.put("stagingUsedAfter",(long)usedBytes.invoke(staging));event.put("backendNanos",elapsed);stagingEvents.add(event);}}}
          return invoke(op,encoder,a);
        });
      }));
      try {for(int n=0;n<turns;n++){
        long beforeWrites=writes[0],beforeBytes=writes[1],beforeBackend=writes[3],beforeEncoder=writes[4],gcCount=collectors.stream().mapToLong(x->Math.max(0,x.getCollectionCount())).sum(),gcTime=collectors.stream().mapToLong(x->Math.max(0,x.getCollectionTime())).sum();
        activeTurn[0]=n;long started=System.nanoTime();WaterPublicationFailureChecks.call(retire,renderer);nanos[n]=System.nanoTime()-started;
        if(writes[1]-beforeBytes>WaterCornerPacket.SLICE_BYTES||writes[0]-beforeWrites>32)throw new AssertionError("Per-turn GPU work bound differs");
        var sample=new LinkedHashMap<String,Long>();sample.put("turn",(long)n);sample.put("elapsedNanos",nanos[n]);sample.put("rangeWrites",writes[0]-beforeWrites);sample.put("rangeBytes",writes[1]-beforeBytes);sample.put("backendWriteNanos",writes[3]-beforeBackend);sample.put("backendEncoderNanos",writes[4]-beforeEncoder);sample.put("otherCpuNanos",nanos[n]-(writes[3]-beforeBackend)-(writes[4]-beforeEncoder));sample.put("observedGcCountDelta",collectors.stream().mapToLong(x->Math.max(0,x.getCollectionCount())).sum()-gcCount);sample.put("observedGcMillisDelta",collectors.stream().mapToLong(x->Math.max(0,x.getCollectionTime())).sum()-gcTime);samples.add(sample);
      }}
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
          var result=new LinkedHashMap<String,Object>();result.put("pass",bad==0);result.put("pages",pageCount);result.put("residentBytes",total);result.put("turns",turns);result.put("rangeWrites",writes[0]);result.put("rangeBytes",writes[1]);result.put("maxRangeBytes",writes[2]);result.put("differentBytes",bad);result.put("enqueueCpuMedianNanos",nanos[nanos.length/2]);result.put("enqueueCpuP95Nanos",nanos[(int)Math.ceil(nanos.length*.95)-1]);result.put("enqueueCpuMaxNanos",nanos[nanos.length-1]);result.put("readbacks",digests);result.put("turnSamples",samples);result.put("backendWriteTotalNanos",writes[3]);result.put("backendEncoderTotalNanos",writes[4]);result.put("duplicateFullSetupUploads",duplicateSetup);result.put("stagingWrapPredictions",stagingWraps[0]);result.put("stagingWriteEvents",stagingEvents);result.put("timingInstrumentation","Validation proxy times backend write and encoder creation; other CPU includes scan/copy/allocation/proxy/reflection and occupancy probes. Staging wrap observations predict the backend overflow branch; exclusive fence timing is not instrumented. GC counters are concurrent observations, not attribution. Instrumented burst turns are not normal-frame or comparative acceptance.");result.put("scope","synthetic32MiB actual GPU residency with selected8/2048pages; instrumented diagnostic burst retirement CPU enqueue excludes GPU completion/frame time; no churn/normal-frame or ownerGPU/FPS acceptance");Files.writeString(out.resolve("water-retirement-budget.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");if(bad!=0)throw new AssertionError("GPU retirement differs "+bad);System.out.println("WATER_RETIREMENT_BUDGET_NATIVE_PASS pages="+pageCount+" residentBytes="+total+" turns="+turns+" rangeWrites="+writes[0]+" maxRangeBytes="+writes[2]+" differentBytes=0 cpuP95Nanos="+result.get("enqueueCpuP95Nanos"));
        }catch(Exception e){throw new RuntimeException(e);}finally{for(var r:reads)r.gpu.close();}
      });queued=true;
    }finally {
      if(backend!=null)backend.set(device,original);type.getMethod("reset").invoke(renderer);((ExecutorService)WaterPublicationFailureChecks.field(type,"workers").get(renderer)).shutdownNow();if(!queued)for(var r:reads)r.gpu.close();
    }
  }
  static Object invoke(Method m,Object target,Object[]args)throws Throwable{try{return m.invoke(target,args);}catch(InvocationTargetException e){throw e.getCause();}}
}
