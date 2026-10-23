package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import net.minecraft.core.*;
import org.lwjgl.system.MemoryUtil;

/** Real neighbor publication, contiguous/scattered quad layout and synchronous range rejection.
 * Private readable GPU pages; no full-scene admission/eviction or FPS acceptance. */
final class WaterBoundaryBatchChecks {
  private record Read(int caseId,String phase,byte[] cpu,GpuBuffer gpu) {}
  private static Object invoke(Method m,Object target,Object[] args)throws Throwable {
    try{return m.invoke(target,args);}catch(InvocationTargetException e){throw e.getCause();}
  }
  static void run(Path output,boolean requireBatches)throws Exception {
    var rows=new ArrayList<Map<String,Object>>();var reads=new ArrayList<Read>();
    var type=AnimatedWaterRenderer.class;var pt=Class.forName(type.getName()+"$Page");
    var rc=type.getDeclaredConstructor();rc.setAccessible(true);var pc=pt.getDeclaredConstructor(long.class);pc.setAccessible(true);
    var sync=type.getDeclaredMethod("synchronizeCornerNeighbors",pt);sync.setAccessible(true);
    Field vf=WaterPublicationFailureChecks.field(WaterGpuMesh.class,"vertices");
    int id=0;
    try {
      for(int origin:new int[]{0,29999968})for(int axis=0;axis<2;axis++)for(int group:new int[]{256,8})for(int fail:new int[]{0,1,2}) {
        if(group==256&&fail==2)continue;
        int caseId=id++;Object renderer=rc.newInstance();Object device=RenderSystem.getDevice();
        Field backend=Arrays.stream(device.getClass().getDeclaredFields()).filter(f->f.getType().isInterface()&&f.getType().getSimpleName().equals("GpuDeviceBackend")).findFirst().orElseThrow();backend.setAccessible(true);Object original=backend.get(device);
        try {
          int sx=origin>>4,sz=-origin>>4;Object admitted=pc.newInstance(SectionPos.asLong(sx,0,sz));
          Object neighbor=pc.newInstance(SectionPos.asLong(sx-(axis==0?1:0),0,sz-(axis==1?1:0)));
          int px=WaterPublicationFailureChecks.field(pt,"x").getInt(neighbor),pz=WaterPublicationFailureChecks.field(pt,"z").getInt(neighbor);
          int[] entries=new int[256];int quads=256+(group==256?0:31),bytes=quads*144;
          byte[] initial=new byte[bytes];ByteBuffer packet=ByteBuffer.wrap(initial).order(ByteOrder.nativeOrder());
          long pair=72L*(1+289+83521+24137569L);
          for(int q=0;q<quads;q++)for(int v=0;v<4;v++) {
            int at=q*144+v*36;packet.putFloat(at,(float)(q+(v&1)*.5));packet.putFloat(at+4,(float)(q%17+(v>>1)*.25));packet.putFloat(at+8,(float)-q);packet.putInt(at+12,0x1a234567);packet.putShort(at+24,(short)1234);packet.putShort(at+28,(short)176);packet.putShort(at+30,(short)240);packet.put(at+32,(byte)127);packet.put(at+34,(byte)(q%16));packet.put(at+35,(byte)91);WaterCornerPacket.write(packet,at,pair,0,0,0,0,true);
          }
          @SuppressWarnings("unchecked") var directions=(Long2ObjectLinkedOpenHashMap<WaterFlowTransition>)WaterPublicationFailureChecks.field(type,"cornerDirections").get(renderer);
          int ax=WaterPublicationFailureChecks.field(pt,"x").getInt(admitted),az=WaterPublicationFailureChecks.field(pt,"z").getInt(admitted);
          for(int y=0;y<16;y++)for(int lane=0;lane<=16;lane++) {
            int x=axis==0?ax:ax+lane,z=axis==0?az+lane:az;var state=new WaterFlowTransition(4,0);state.target(8,100,1);directions.put(BlockPos.asLong(x,y,z),state);
          }
          for(int n=0;n<256;n++){int q=n+(group==256?0:n/group);int y=n/16,lane=n%16,cx=axis==0?15:lane,cz=axis==0?lane:15;entries[n]=(q<<12)|(y<<8)|(cz<<4)|cx;}
          var data=MemoryUtil.memAlloc(bytes).order(ByteOrder.nativeOrder());WaterGpuMesh mesh;
          try {data.put(initial).flip();mesh=new WaterGpuMesh(data,quads*4);((GpuBuffer)vf.get(mesh)).close();vf.set(mesh,RenderSystem.getDevice().createBuffer(()->"Boundary batch readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,data));}
          finally {MemoryUtil.memFree(data);}
          WaterPublicationFailureChecks.field(pt,"gpu").set(neighbor,mesh);WaterPublicationFailureChecks.field(pt,"presentation").set(neighbor,initial.clone());WaterPublicationFailureChecks.field(pt,"boundaryQuads").set(neighbor,entries);WaterPublicationFailureChecks.field(type,"residentBytes").setLong(renderer,bytes);WaterPublicationFailureChecks.field(type,"clock").setLong(renderer,100);
          @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)WaterPublicationFailureChecks.field(type,"pages").get(renderer);pages.put(WaterPublicationFailureChecks.field(pt,"key").getLong(neighbor),neighbor);pages.put(WaterPublicationFailureChecks.field(pt,"key").getLong(admitted),admitted);
          var ranges=new ArrayList<Map<String,Object>>();int[] attempts={0};boolean[] rejected={false};GpuBuffer target=(GpuBuffer)vf.get(mesh);
          Object proxy=Proxy.newProxyInstance(backend.getType().getClassLoader(),new Class<?>[]{backend.getType()},(o,m,a)->{
            Object value=invoke(m,original,a);if(!m.getName().equals("createCommandEncoder"))return value;
            return Proxy.newProxyInstance(m.getReturnType().getClassLoader(),new Class<?>[]{m.getReturnType()},(enc,op,args)->{
              if(op.getName().equals("writeToBuffer")&&args[0]instanceof GpuBufferSlice slice&&slice.buffer()==target) {
                int number=++attempts[0];boolean reject=!rejected[0]&&fail>0&&number==fail;
                ranges.add(Map.of("offset",slice.offset(),"bytes",((ByteBuffer)args[1]).remaining(),"rejected",reject));
                if(reject){rejected[0]=true;throw new IllegalStateException("INJECTED_BOUNDARY_BATCH_REJECTION");}
              }
              return invoke(op,value,args);
            });
          });
          long begin=System.nanoTime();backend.set(device,proxy);
          try {try {WaterPublicationFailureChecks.call(sync,renderer,admitted);}catch(IllegalStateException e){if(!e.getMessage().equals("INJECTED_BOUNDARY_BATCH_REJECTION"))throw e;}}
          finally {backend.set(device,original);}
          long elapsed=System.nanoTime()-begin;
          if(rejected[0]!=(fail>0))throw new AssertionError("Batch rejection route missed");
          int firstAttempts=attempts[0];read(reads,caseId,"after-call",renderer,neighbor,pt,target);
          backend.set(device,proxy);try {WaterPublicationFailureChecks.call(sync,renderer,admitted);}finally {backend.set(device,original);}
          int retryAttempts=attempts[0]-firstAttempts;read(reads,caseId,"after-retry",renderer,neighbor,pt,target);
          int beforeStable=attempts[0];backend.set(device,proxy);try {WaterPublicationFailureChecks.call(sync,renderer,admitted);}finally {backend.set(device,original);}
          if(attempts[0]!=beforeStable)throw new AssertionError("Idempotent boundary publication wrote GPU bytes");
          int expected=group==256?1:32;
          if(requireBatches&&fail==0&&firstAttempts!=expected)throw new AssertionError("Contiguous boundary batches were not combined");
          var row=new LinkedHashMap<String,Object>();row.put("id",caseId);row.put("origin",origin);row.put("axis",axis);row.put("group",group);row.put("rejectWrite",fail);row.put("bytes",bytes);row.put("quads",quads);row.put("boundaryEntries",entries);row.put("initialSha256",WaterPublicationFailureChecks.sha(initial));row.put("firstWriteAttempts",firstAttempts);row.put("retryWriteAttempts",retryAttempts);row.put("idempotentWrites",attempts[0]-beforeStable);row.put("syncCpuNanos",elapsed);row.put("ranges",ranges);rows.add(row);
        } finally {backend.set(device,original);type.getMethod("reset").invoke(renderer);((ExecutorService)WaterPublicationFailureChecks.field(type,"workers").get(renderer)).shutdownNow();}
      }
    }catch(Throwable failure){for(var r:reads)r.gpu.close();throw failure;}
    RenderSystem.queueFencedTask(()->{
      try {
        var raw=new java.io.ByteArrayOutputStream();var metadata=new ArrayList<Map<String,Object>>();long bad=0;
        for(var r:reads)try(var view=RenderSystem.getDevice().createCommandEncoder().mapBuffer(r.gpu,true,false)) {
          byte[] gpu=new byte[r.cpu.length];view.data().get(0,gpu);int diff=0;for(int n=0;n<gpu.length;n++)if(gpu[n]!=r.cpu[n])diff++;
          int at=raw.size();raw.writeBytes(r.cpu);raw.writeBytes(gpu);metadata.add(Map.of("id",r.caseId,"phase",r.phase,"offset",at,"bytes",gpu.length,"cpuSha256",WaterPublicationFailureChecks.sha(r.cpu),"gpuSha256",WaterPublicationFailureChecks.sha(gpu),"differentBytes",diff));bad+=diff;
        }
        if(bad!=0)throw new AssertionError("Boundary batch accepted mirror differs from GPU");
        var result=new LinkedHashMap<String,Object>();result.put("pass",true);result.put("requireBatches",requireBatches);result.put("cases",rows);result.put("readbacks",metadata);result.put("differentBytes",bad);
        try(var input=WaterBoundaryBatchChecks.class.getResourceAsStream("WaterBoundaryBatchChecks.class")){result.put("fixtureClassSha256",WaterPublicationFailureChecks.sha(input.readAllBytes()));}
        result.put("scope","Private256boundaryquad realGPU fixture; contiguous/scattered range submissions and synchronous rejection/manualretry/idempotence. No wholecohort/asyncfailure/fullscene/capchurn/FPS acceptance.");
        Files.write(output.resolve("water-boundary-batch-readbacks.bin"),raw.toByteArray());Files.writeString(output.resolve("water-boundary-batch.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
        System.out.println("WATER_BOUNDARY_BATCH_PASS cases=20 readbacks=40 differentBytes=0 batched="+requireBatches);
      }catch(Exception e){throw new RuntimeException(e);}finally {for(var r:reads)r.gpu.close();}
    });
  }
  private static void read(List<Read> reads,int id,String phase,Object renderer,Object page,Class<?> pt,GpuBuffer target)throws Exception {
    byte[] cpu=((byte[])WaterPublicationFailureChecks.field(pt,"presentation").get(page)).clone();var gpu=RenderSystem.getDevice().createBuffer(()->"Boundary batch readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,cpu.length);RenderSystem.getDevice().createCommandEncoder().copyToBuffer(target.slice(0,cpu.length),gpu.slice());reads.add(new Read(id,phase,cpu,gpu));
  }
}
