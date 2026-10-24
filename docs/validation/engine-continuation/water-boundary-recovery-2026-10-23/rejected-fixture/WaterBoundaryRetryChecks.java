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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.lwjgl.system.MemoryUtil;

/** Private readable GPU pages. One production retry call per distinct rendered frame. */
final class WaterBoundaryRetryChecks {
  private static Job active;
  private static long frame;
  private record Read(int id,String phase,byte[] cpu,GpuBuffer gpu) {}
  private static Object invoke(Method m,Object target,Object[] args)throws Throwable {
    try{return m.invoke(target,args);}catch(InvocationTargetException e){throw e.getCause();}
  }
  static void start(Path output,boolean required)throws Exception {
    if(active!=null)throw new IllegalStateException("Boundary retry fixture already active");
    active=new Job(output,required);
  }
  @SubscribeEvent public void before(RenderFrameEvent.Pre event) {
    frame++;Job job=active;if(job==null||job.finishing)return;
    try{job.turn();}catch(Exception|Error failure){active=null;job.close();throw new RuntimeException(failure);}
  }
  private static final class Job implements AutoCloseable {
    final Path output;final boolean required;final List<Map<String,Object>> rows=new ArrayList<>();
    final List<Read> reads=new ArrayList<>();Case current;int next;boolean finishing;
    Job(Path output,boolean required){this.output=output;this.required=required;}
    void turn()throws Exception {
      if(current==null){if(next==40){finish();return;}current=new Case(this,next++);return;}
      if(current.turn()){rows.add(current.row());current.close();current=null;}
    }
    void finish() {
      finishing=true;RenderSystem.queueFencedTask(()->{
        try {
          var raw=new java.io.ByteArrayOutputStream();var meta=new ArrayList<Map<String,Object>>();
          for(var r:reads)try(var view=RenderSystem.getDevice().createCommandEncoder().mapBuffer(r.gpu,true,false)){
            byte[] gpu=new byte[r.cpu.length];view.data().get(0,gpu);if(!Arrays.equals(gpu,r.cpu))throw new AssertionError("Retry CPU/GPU bytes differ");
            int at=raw.size();raw.writeBytes(r.cpu);raw.writeBytes(gpu);meta.add(Map.of("id",r.id,"phase",r.phase,"offset",at,"bytes",gpu.length,"sha256",WaterPublicationFailureChecks.sha(gpu)));
          }
          var result=new LinkedHashMap<String,Object>();result.put("pass",true);result.put("required",required);result.put("cases",rows);result.put("readbacks",meta);
          try(var in=WaterBoundaryRetryChecks.class.getResourceAsStream("WaterBoundaryRetryChecks.class")){result.put("fixtureClassSha256",WaterPublicationFailureChecks.sha(in.readAllBytes()));}
          Files.write(output.resolve("water-boundary-retry.bin"),raw.toByteArray());Files.writeString(output.resolve("water-boundary-retry.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
          System.out.println("WATER_BOUNDARY_RETRY_PASS cases=40 readbacks=120 required="+required+" distinctFrames=true");
        }catch(Exception e){throw new RuntimeException(e);}finally{active=null;close();}
      });
    }
    public void close(){if(current!=null)current.close();for(var r:reads)r.gpu.close();reads.clear();}
  }
  private static final class Case implements AutoCloseable {
    final Job owner;final int id,axis,scenario,count=2048,group,bytes,quads,origin;final long initialClock;
    final Class<?> type=AnimatedWaterRenderer.class,pt;final Object renderer,neighbor,admitted,device=RenderSystem.getDevice(),original,proxy;
    final Field backend;final Method sync,pump,defer,cancel;final Map<Long,Object> pages;
    final List<Map<String,Object>> writes=new ArrayList<>(),turns=new ArrayList<>();
    final GpuBuffer target;final int[] entries;final byte[] initial;
    int phase,attempts,turn;boolean recovering,initialThrew,terminal,firstRead,removed,closed;long storageBoundBytes;int storageBoundRanges;
    @SuppressWarnings("unchecked") Case(Job owner,int id)throws Exception {
      this.owner=owner;this.id=id;axis=id%8;scenario=id/8;group=scenario==1||scenario==2?8:2048;origin=(axis&1)==0?0:29999968;initialClock=origin==0?125:65545;
      pt=Class.forName(type.getName()+"$Page");var rc=type.getDeclaredConstructor();rc.setAccessible(true);renderer=rc.newInstance();var pc=pt.getDeclaredConstructor(long.class);pc.setAccessible(true);
      Method method;try{method=type.getDeclaredMethod("synchronizeCornerNeighbors",pt,boolean.class);recovering=true;}catch(NoSuchMethodException old){method=type.getDeclaredMethod("synchronizeCornerNeighbors",pt);}method.setAccessible(true);sync=method;
      Method retry=null,def=null,can=null;if(recovering){retry=type.getDeclaredMethod("retryCornerPublications");retry.setAccessible(true);def=type.getDeclaredMethod("deferCornerRange",pt,int.class,ByteBuffer.class,long.class);def.setAccessible(true);can=type.getDeclaredMethod("cancelCornerRetries",pt,boolean.class);can.setAccessible(true);}pump=retry;defer=def;cancel=can;
      int[][] deltas={{1,0},{0,1},{-1,0},{0,-1},{1,1},{-1,1},{1,-1},{-1,-1}};int dx=deltas[axis][0],dz=deltas[axis][1];
      int sx=origin>>4,sz=-origin>>4;admitted=pc.newInstance(SectionPos.asLong(sx,0,sz));neighbor=pc.newInstance(SectionPos.asLong(sx-dx,0,sz-dz));
      pages=(Map<Long,Object>)WaterPublicationFailureChecks.field(type,"pages").get(renderer);pages.put(key(neighbor),neighbor);pages.put(key(admitted),admitted);
      quads=count+(group==2048?0:(count-1)/8);bytes=quads*144;initial=new byte[bytes];var packet=ByteBuffer.wrap(initial).order(ByteOrder.nativeOrder());
      for(int q=0;q<quads;q++)for(int v=0;v<4;v++){
        int at=q*144+v*36;packet.putFloat(at,q+(v&1)*.5f);packet.putFloat(at+4,q%17+(v>>1)*.25f);packet.putFloat(at+8,-q);packet.putInt(at+12,0x1a234567);packet.putShort(at+24,(short)1234);packet.putShort(at+28,(short)176);packet.putShort(at+30,(short)240);packet.put(at+32,(byte)127);packet.put(at+34,(byte)(q%16));packet.put(at+35,(byte)91);WaterCornerPacket.write(packet,at,72L*(1+289+83521+24137569L),0,0,0,0,true);
      }
      entries=new int[count];for(int n=0;n<count;n++){
        int q=n+(group==2048?0:n/8),y=(n%256)/16,lane=n%16,cx=dx==0?lane:dx>0?15:0,cz=dz==0?lane:dz>0?15:0;
        if(group==8&&(n/8&1)!=0){if(dx!=0)cx=15-cx;else cz=15-cz;}entries[n]=(q<<12)|(y<<8)|(cz<<4)|cx;
      }
      var directions=(Long2ObjectLinkedOpenHashMap<WaterFlowTransition>)WaterPublicationFailureChecks.field(type,"cornerDirections").get(renderer);int ax=WaterPublicationFailureChecks.field(pt,"x").getInt(admitted),az=WaterPublicationFailureChecks.field(pt,"z").getInt(admitted);long base=initialClock-25;
      for(int y=0;y<16;y++)for(int x=ax;x<=ax+16;x++)for(int z=az;z<=az+16;z++){if(x!=ax&&x!=ax+16&&z!=az&&z!=az+16)continue;var state=new WaterFlowTransition(4,0);state.target(8,base,1);state.target(12,initialClock,2);directions.put(BlockPos.asLong(x,y,z),state);}
      var data=MemoryUtil.memAlloc(bytes).order(ByteOrder.nativeOrder());WaterGpuMesh mesh;Field vf=WaterPublicationFailureChecks.field(WaterGpuMesh.class,"vertices");
      try{data.put(initial).flip();mesh=new WaterGpuMesh(data,quads*4);((GpuBuffer)vf.get(mesh)).close();vf.set(mesh,RenderSystem.getDevice().createBuffer(()->"Boundary retry readable vertices",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,data));}finally{MemoryUtil.memFree(data);}target=(GpuBuffer)vf.get(mesh);
      WaterPublicationFailureChecks.field(pt,"gpu").set(neighbor,mesh);WaterPublicationFailureChecks.field(pt,"presentation").set(neighbor,initial.clone());WaterPublicationFailureChecks.field(pt,"boundaryQuads").set(neighbor,WaterBoundaryIndex.of(entries));WaterPublicationFailureChecks.field(type,"residentBytes").setLong(renderer,bytes);clock(initialClock);
      backend=Arrays.stream(device.getClass().getDeclaredFields()).filter(f->f.getType().isInterface()&&f.getType().getSimpleName().equals("GpuDeviceBackend")).findFirst().orElseThrow();backend.setAccessible(true);original=backend.get(device);
      proxy=Proxy.newProxyInstance(backend.getType().getClassLoader(),new Class<?>[]{backend.getType()},(o,m,a)->{
        Object value=invoke(m,original,a);if(!m.getName().equals("createCommandEncoder"))return value;
        return Proxy.newProxyInstance(m.getReturnType().getClassLoader(),new Class<?>[]{m.getReturnType()},(enc,op,args)->{
          if(op.getName().equals("writeToBuffer")&&args[0]instanceof GpuBufferSlice slice&&slice.buffer()==target){
            boolean reject=(phase==0&&(scenario==2||++attempts==1))||(phase==1&&scenario==3);int length=((ByteBuffer)args[1]).remaining();
            writes.add(Map.of("phase",phase,"frame",frame,"offset",slice.offset(),"bytes",length,"rejected",reject));
            if(reject){if(scenario==1)((ByteBuffer)args[1]).position(((ByteBuffer)args[1]).limit());throw new IllegalStateException("INJECTED_BOUNDARY_RETRY_REJECTION");}
          }
          return invoke(op,value,args);
        });
      });
      backend.set(device,proxy);try{publish();}catch(IllegalStateException failure){if(!failure.getMessage().equals("INJECTED_BOUNDARY_RETRY_REJECTION"))throw failure;initialThrew=true;}finally{backend.set(device,original);}
      read("after-call");
      if(owner.required&&(!recovering||initialThrew))throw new AssertionError("Production boundary rejection was not deferred");
      if(scenario==2){if(owner.required&&(pending()!=16||pendingBytes()>1048576||!urgent()))throw new AssertionError("Range cap/backpressure missing");phase=2;directions.replaceAll((key,state)->new WaterFlowTransition(16,initialClock));backend.set(device,proxy);try{publish();}finally{backend.set(device,original);}}
      if(scenario==4){pages.remove(key(neighbor));removed=true;}
      if(scenario==1){clock(initialClock+200);phase=3;mutateCurrent();}
    }
    void publish()throws Exception {if(recovering)WaterPublicationFailureChecks.call(sync,renderer,admitted,true);else WaterPublicationFailureChecks.call(sync,renderer,admitted);}
    long key(Object page)throws Exception{return WaterPublicationFailureChecks.field(pt,"key").getLong(page);}
    void clock(long value)throws Exception{WaterPublicationFailureChecks.field(type,"clock").setLong(renderer,value);}
    int pending()throws Exception{return recovering?((Collection<?>)WaterPublicationFailureChecks.field(type,"cornerRetries").get(renderer)).size():0;}
    int pendingBytes()throws Exception{return recovering?WaterPublicationFailureChecks.field(type,"cornerRetryBytes").getInt(renderer):0;}
    boolean urgent()throws Exception{return ((Set<?>)WaterPublicationFailureChecks.field(type,"urgent").get(renderer)).contains(key(neighbor));}
    void mutateCurrent()throws Exception{
      byte[] live=(byte[])WaterPublicationFailureChecks.field(pt,"presentation").get(neighbor);var b=ByteBuffer.wrap(live).order(ByteOrder.nativeOrder());
      for(int q=0;q<quads;q++)for(int v=0;v<4;v++){int at=q*144+v*36;b.putShort(at+24,(short)0);b.put(at+32,(byte)63);b.put(at+12,(byte)19);}
      var data=MemoryUtil.memAlloc(bytes).order(ByteOrder.nativeOrder());try{data.put(live).flip();((WaterGpuMesh)WaterPublicationFailureChecks.field(pt,"gpu").get(neighbor)).updateRange(0,data);}finally{MemoryUtil.memFree(data);}
    }
    boolean turn()throws Exception{
      if(turn>=64)throw new AssertionError("Recovery exceeded fixture bound");int before=writes.size();long start=System.nanoTime();phase=1;
      if(pump!=null){backend.set(device,proxy);try{WaterPublicationFailureChecks.call(pump,renderer);}catch(IllegalStateException failure){if(!failure.getMessage().equals("Water corner publication failed after three recovery attempts"))throw failure;terminal=true;}finally{backend.set(device,original);}}
      long nanos=System.nanoTime()-start;int count=writes.size()-before,total=0;for(int n=before;n<writes.size();n++)total+=((Number)writes.get(n).get("bytes")).intValue();
      if(count>1||total>65520||pending()>16||pendingBytes()>1048576)throw new AssertionError("Recovery work/memory bound exceeded");
      turns.add(Map.of("frame",frame,"writes",count,"bytes",total,"nanos",nanos,"pending",pending(),"pendingBytes",pendingBytes()));turn++;
      if(!firstRead){read("after-first-turn");firstRead=true;}
      if(pending()!=0&&!terminal)return false;
      if(owner.required&&scenario==3&&!terminal)throw new AssertionError("Permanent rejection was silently discarded");
      read("after-recovery");if(removed)pages.put(key(neighbor),neighbor);lifecycle();return true;
    }
    void lifecycle()throws Exception{
      if(!recovering)return;WaterPublicationFailureChecks.call(cancel,renderer,neighbor,false);
      var live=ByteBuffer.wrap((byte[])WaterPublicationFailureChecks.field(pt,"presentation").get(neighbor)).order(ByteOrder.nativeOrder());
      if(id==0){for(int n=0;n<5;n++)WaterPublicationFailureChecks.call(defer,renderer,neighbor,0,live.duplicate().limit(262080),201L);storageBoundBytes=pendingBytes();storageBoundRanges=pending();if(storageBoundBytes!=1048320||storageBoundRanges!=4||!urgent())throw new AssertionError("Byte cap/backpressure missing");WaterPublicationFailureChecks.call(cancel,renderer,neighbor,false);}
      WaterPublicationFailureChecks.call(defer,renderer,neighbor,0,live.duplicate().limit(144),201L);
      if((axis&1)==0){var release=type.getDeclaredMethod("release",pt);release.setAccessible(true);WaterPublicationFailureChecks.call(release,renderer,neighbor);}else type.getMethod("reset").invoke(renderer);
      if(pending()!=0||pendingBytes()!=0)throw new AssertionError("Release/reset retained recovery data");
    }
    void read(String phase)throws Exception {byte[] cpu=((byte[])WaterPublicationFailureChecks.field(pt,"presentation").get(neighbor)).clone();var gpu=RenderSystem.getDevice().createBuffer(()->"Boundary retry readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,cpu.length);RenderSystem.getDevice().createCommandEncoder().copyToBuffer(target.slice(0,cpu.length),gpu.slice());owner.reads.add(new Read(id,phase,cpu,gpu));}
    Map<String,Object> row() {
      var r=new LinkedHashMap<String,Object>();r.put("id",id);r.put("axis",axis);r.put("scenario",scenario);r.put("count",count);r.put("group",group);r.put("origin",origin);r.put("initialClock",initialClock);r.put("bytes",bytes);r.put("quads",quads);r.put("boundaryEntries",entries);r.put("initialSha256",WaterPublicationFailureChecks.sha(initial));r.put("recovering",recovering);r.put("initialThrew",initialThrew);r.put("terminal",terminal);r.put("writes",writes);r.put("turns",turns);r.put("storageBoundBytes",storageBoundBytes);r.put("storageBoundRanges",storageBoundRanges);return r;
    }
    public void close(){if(closed)return;closed=true;try{backend.set(device,original);type.getMethod("reset").invoke(renderer);((ExecutorService)WaterPublicationFailureChecks.field(type,"workers").get(renderer)).shutdownNow();}catch(Exception e){throw new RuntimeException(e);}}
  }
}
