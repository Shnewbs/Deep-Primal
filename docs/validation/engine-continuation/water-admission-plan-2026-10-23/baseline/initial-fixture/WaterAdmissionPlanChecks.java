package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.*;
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
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.AABB;
import org.lwjgl.system.MemoryUtil;

/** Actual production mesh upload/eviction at the real resident GPU cap; private non-drawn pages. */
final class WaterAdmissionPlanChecks {
  private static final long CAP=32L*1024*1024;
  private record Read(int id,String page,String phase,long offset,byte[] expected,GpuBuffer gpu) {}
  private static Object invoke(Method m,Object target,Object[] args)throws Throwable {
    try{return m.invoke(target,args);}catch(InvocationTargetException e){throw e.getCause();}
  }
  private static WaterPatchMesher.Snapshot snapshot(boolean large) {
    byte[] u=new byte[WaterPatchMesher.CELLS],s=new byte[u.length];int[] light=new int[u.length],color=new int[256];Arrays.fill(color,0xff3f76e4);
    for(int z=0;z<16;z++)for(int x=0;x<16;x++){s[WaterPatchMesher.index(x,-1,z)]=1;u[WaterPatchMesher.index(x,0,z)]=16;light[WaterPatchMesher.index(x,0,z)]=0x00f000b0;if(large&&(x+z)%2==0)u[WaterPatchMesher.index(x,1,z)]=16;}
    return new WaterPatchMesher.Snapshot(u,s,light,color);
  }
  static void run(Minecraft mc,Path out,boolean requirePlan)throws Exception {
    var type=AnimatedWaterRenderer.class;var pt=Class.forName(type.getName()+"$Page");var rt=Class.forName(type.getName()+"$Ready");
    var rc=type.getDeclaredConstructor();rc.setAccessible(true);var pc=pt.getDeclaredConstructor(long.class);pc.setAccessible(true);
    var ready=rt.getDeclaredConstructor(long.class,long.class,pt,WaterPatchMesher.Snapshot.class,WaterPatchMesher.Mesh.class);ready.setAccessible(true);var upload=type.getDeclaredMethod("upload",rt);upload.setAccessible(true);
    var vf=WaterPublicationFailureChecks.field(WaterGpuMesh.class,"vertices");var rows=new ArrayList<Map<String,Object>>();var reads=new ArrayList<Read>();
    boolean allDeniedPreserved=true;int caseId=0;
    try {
      for(int origin:new int[]{0,29999968})for(int axis=0;axis<2;axis++)for(String mode:List.of("insufficient","busy","single","multiple")) {
        int id=caseId++;Object renderer=rc.newInstance();Object device=RenderSystem.getDevice();
        Field backend=Arrays.stream(device.getClass().getDeclaredFields()).filter(f->f.getType().isInterface()&&f.getType().getSimpleName().equals("GpuDeviceBackend")).findFirst().orElseThrow();backend.setAccessible(true);Object original=backend.get(device);
        // Test-only COPY_SRC makes the actual production upload readable; no shipping flags change.
        Object proxy=Proxy.newProxyInstance(backend.getType().getClassLoader(),new Class<?>[]{backend.getType()},(o,m,args)->{
          Object[] a=args;
          if(m.getName().equals("createBuffer")&&args!=null){a=args.clone();for(int n=0;n<a.length;n++)if(a[n]instanceof Integer usage&&(usage&GpuBuffer.USAGE_VERTEX)!=0&&(usage&GpuBuffer.USAGE_COPY_DST)!=0){a[n]=usage|GpuBuffer.USAGE_COPY_SRC;break;}}
          return invoke(m,original,a);
        });
        try {
          int anchor=origin==0?(axis==0?mc.player.chunkPosition().x():mc.player.chunkPosition().z()):(origin>>4)-256;
          int fixed=axis==0?mc.player.chunkPosition().z():mc.player.chunkPosition().x(),cy=SectionPos.blockToSectionCoord(mc.player.getBlockY());
          @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)WaterPublicationFailureChecks.field(type,"pages").get(renderer);
          var labels=new LinkedHashMap<String,Object>();Object target=page(pc,pages,labels,"target",anchor+96,fixed,cy,axis);
          var full=snapshot(false);var large=snapshot(true);var initialMesh=WaterPatchMesher.build(full,full,true);var grownMesh=WaterPatchMesher.build(large,full,true);long incoming=grownMesh.count()*36L;
          backend.set(device,proxy);try {WaterPublicationFailureChecks.call(upload,renderer,ready.newInstance(0L,1L,target,full,initialMesh));}finally {backend.set(device,original);}
          var oldGpu=(WaterGpuMesh)WaterPublicationFailureChecks.field(pt,"gpu").get(target);long oldBytes=oldGpu.bytes;
          if(incoming<=oldBytes+288||incoming%144!=0)throw new AssertionError("Admission fixture needs a real larger mesh");
          long[] farSizes=switch(mode){case"single"->new long[]{incoming,144};case"multiple"->new long[]{(incoming/288)*144,incoming-(incoming/288)*144,144};default->new long[]{144,144};};
          long total=oldBytes;
          for(int n=0;n<farSizes.length;n++) {Object p=page(pc,pages,labels,"far"+n,anchor+180-n*16,fixed,cy,axis);ballast(p,pt,vf,farSizes[n],40+n);total+=farSizes[n];}
          Object busy=page(pc,pages,labels,"busy",anchor+216,fixed,cy,axis);long busyBytes=mode.equals("busy")?incoming:144;ballast(busy,pt,vf,busyBytes,70);total+=busyBytes;WaterPublicationFailureChecks.field(pt,"busy").setBoolean(busy,true);
          Object placeholder=page(pc,pages,labels,"placeholder",anchor+240,fixed,cy,axis);
          Object near=page(pc,pages,labels,"near",anchor,fixed,cy,axis);long nearBytes=(CAP/144)*144-total;ballast(near,pt,vf,nearBytes,90);total+=nearBytes;
          WaterPublicationFailureChecks.field(type,"residentBytes").setLong(renderer,total);
          if(total>CAP||CAP-total>=144||sum(pages,pt)!=total)throw new AssertionError("Actual resident allocation accounting differs");
          var before=state(labels,pages,pt,vf);long beforeUploads=type.getField("uploads").getLong(renderer);var oldBuffer=(GpuBuffer)vf.get(oldGpu);
          readTarget(reads,id,"before",target,pt,vf);
          WaterPublicationFailureChecks.field(type,"clock").setLong(renderer,200);WaterPublicationFailureChecks.field(pt,"changed").setBoolean(target,false);
          long started=System.nanoTime();backend.set(device,proxy);try {WaterPublicationFailureChecks.call(upload,renderer,ready.newInstance(0L,2L,target,large,grownMesh));}finally {backend.set(device,original);}
          long elapsed=System.nanoTime()-started;boolean accepted=WaterPublicationFailureChecks.field(pt,"snapshot").get(target)==large;
          var after=state(labels,pages,pt,vf);long resident=type.getField("residentBytes").getLong(renderer);
          if(resident>CAP||sum(pages,pt)!=resident)throw new AssertionError("Resident cap/accounting failed after production admission");
          boolean denied=mode.equals("insufficient")||mode.equals("busy");
          if(accepted==denied)throw new AssertionError("Fixture expected a different nominal budget decision");
          boolean preserved=before.stream().allMatch(b->after.stream().anyMatch(a->a.get("label").equals(b.get("label"))&&(Boolean)a.get("present")));
          if(denied&&!preserved)allDeniedPreserved=false;
          if(requirePlan&&denied&&!preserved)throw new AssertionError("Budget-denied growth displaced covering pages");
          if(!accepted&&((WaterGpuMesh)WaterPublicationFailureChecks.field(pt,"gpu").get(target)!=oldGpu||oldBuffer.isClosed()||!WaterPublicationFailureChecks.field(pt,"changed").getBoolean(target)))throw new AssertionError("Denied growth lost old target/retry marker");
          if(accepted&&!oldBuffer.isClosed())throw new AssertionError("Successful growth retained old allocation");
          readTarget(reads,id,"after",target,pt,vf);
          for(var entry:labels.entrySet())if(!entry.getKey().equals("target")&&!entry.getKey().equals("placeholder")&&pages.containsKey(WaterPublicationFailureChecks.field(pt,"key").getLong(entry.getValue()))) {
            int tag=entry.getKey().equals("near")?90:entry.getKey().equals("busy")?70:40+Integer.parseInt(entry.getKey().substring(3));readSample(reads,id,entry.getKey(),entry.getValue(),pt,vf,tag);
          }
          var camera=mc.gameRenderer.getMainCamera().position();var row=new LinkedHashMap<String,Object>();row.put("id",id);row.put("origin",origin);row.put("axis",axis);row.put("mode",mode);row.put("camera",new double[]{camera.x,camera.y,camera.z});row.put("before",before);row.put("after",after);row.put("cap",CAP);row.put("incomingBytes",incoming);row.put("beforeResidentBytes",total);row.put("afterResidentBytes",resident);row.put("accepted",accepted);row.put("deniedPreserved",preserved);row.put("targetOldBytes",oldBytes);row.put("oldTargetClosed",oldBuffer.isClosed());row.put("retryRequested",WaterPublicationFailureChecks.field(pt,"changed").getBoolean(target));row.put("uploadsDelta",type.getField("uploads").getLong(renderer)-beforeUploads);row.put("uploadCpuNanos",elapsed);rows.add(row);
        }finally {backend.set(device,original);type.getMethod("reset").invoke(renderer);((ExecutorService)WaterPublicationFailureChecks.field(type,"workers").get(renderer)).shutdownNow();if(type.getField("residentBytes").getLong(renderer)!=0)throw new AssertionError("Private renderer leaked resident bytes");}
      }
    }catch(Throwable failure){for(var r:reads)r.gpu.close();throw failure;}
    boolean preserved=allDeniedPreserved;
    RenderSystem.queueFencedTask(()->{
      try {
        var raw=new java.io.ByteArrayOutputStream();var metadata=new ArrayList<Map<String,Object>>();long bad=0;
        for(var r:reads)try(var mapped=RenderSystem.getDevice().createCommandEncoder().mapBuffer(r.gpu,true,false)) {
          byte[] actual=new byte[r.expected.length];mapped.data().get(0,actual);int diff=0;for(int n=0;n<actual.length;n++)if(actual[n]!=r.expected[n])diff++;
          int at=raw.size();raw.writeBytes(r.expected);raw.writeBytes(actual);var m=new LinkedHashMap<String,Object>();m.put("id",r.id);m.put("page",r.page);m.put("phase",r.phase);m.put("bufferOffset",r.offset);m.put("offset",at);m.put("bytes",actual.length);m.put("cpuSha256",WaterPublicationFailureChecks.sha(r.expected));m.put("gpuSha256",WaterPublicationFailureChecks.sha(actual));m.put("differentBytes",diff);metadata.add(m);bad+=diff;
        }
        if(bad!=0)throw new AssertionError("Admission GPU payload/survivor sample differs");
        var result=new LinkedHashMap<String,Object>();result.put("fixtureExecutionPass",true);result.put("requirePlan",requirePlan);result.put("budgetDeniedCoveragePreserved",preserved);result.put("cases",rows);result.put("readbacks",metadata);result.put("differentBytes",bad);
        try(var input=WaterAdmissionPlanChecks.class.getResourceAsStream("WaterAdmissionPlanChecks.class")){result.put("fixtureClassSha256",WaterPublicationFailureChecks.sha(input.readAllBytes()));}
        result.put("scope","Actual production upload/eviction with real32MiB allocations, real mesher growth, protectednear/busy/placeholder pages and bothaxes/origins. Full target readbacks and prefix/suffix survivor samples. Private non-drawn pages bypass discovery/visible-scene churn; synchronous nominal-budget planning is not allocation/device-failure atomicity or FPS acceptance.");
        Files.write(out.resolve("water-admission-readbacks.bin"),raw.toByteArray());Files.writeString(out.resolve("water-admission.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");System.out.println("WATER_ADMISSION_RESULT cases=16 budgetDeniedCoveragePreserved="+preserved+" requirePlan="+requirePlan+" differentBytes=0");
        System.out.println("WATER_ADMISSION_NATIVE_PASS cases=16 nominalBudgetDecisions=true payloads=true expectedPlan="+requirePlan);
      }catch(Exception e){throw new RuntimeException(e);}finally {for(var r:reads)r.gpu.close();}
    });
  }
  private static Object page(Constructor<?> pc,Map<Long,Object> pages,Map<String,Object> labels,String label,int along,int fixed,int y,int axis)throws Exception {
    long key=SectionPos.asLong(axis==0?along:fixed,y,axis==0?fixed:along);Object p=pc.newInstance(key);pages.put(key,p);labels.put(label,p);return p;
  }
  private static void ballast(Object page,Class<?> pt,Field vf,long bytes,int tag)throws Exception {
    var data=MemoryUtil.memAlloc((int)bytes);try {MemoryUtil.memSet(data,tag);data.limit(144);var mesh=new WaterGpuMesh(data,4);((GpuBuffer)vf.get(mesh)).close();data.clear();var buffer=RenderSystem.getDevice().createBuffer(()->"Admission actual readable ballast",GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,data);vf.set(mesh,buffer);WaterPublicationFailureChecks.field(WaterGpuMesh.class,"bytes").setLong(mesh,bytes);WaterPublicationFailureChecks.field(WaterGpuMesh.class,"indices").setInt(mesh,(int)(bytes/144)*6);if(mesh.bytes!=buffer.size())throw new AssertionError("Actual buffer size differs");WaterPublicationFailureChecks.field(pt,"gpu").set(page,mesh);}finally {MemoryUtil.memFree(data);}
  }
  private static long sum(Map<Long,Object> pages,Class<?> pt)throws Exception {long total=0;for(Object p:pages.values()){var mesh=(WaterGpuMesh)WaterPublicationFailureChecks.field(pt,"gpu").get(p);if(mesh!=null)total+=mesh.bytes;}return total;}
  private static List<Map<String,Object>> state(Map<String,Object> labels,Map<Long,Object> pages,Class<?> pt,Field vf)throws Exception {
    var out=new ArrayList<Map<String,Object>>();
    for(var e:labels.entrySet()){Object p=e.getValue();var gpu=(WaterGpuMesh)WaterPublicationFailureChecks.field(pt,"gpu").get(p);var b=(AABB)WaterPublicationFailureChecks.field(pt,"bounds").get(p);var row=new LinkedHashMap<String,Object>();row.put("label",e.getKey());row.put("key",WaterPublicationFailureChecks.field(pt,"key").getLong(p));row.put("present",pages.containsKey(row.get("key")));row.put("bytes",gpu==null?0:gpu.bytes);row.put("actualBufferBytes",gpu==null?0:((GpuBuffer)vf.get(gpu)).size());row.put("busy",WaterPublicationFailureChecks.field(pt,"busy").getBoolean(p));row.put("bounds",new double[]{b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ});out.add(row);}return out;
  }
  private static void readTarget(List<Read> reads,int id,String phase,Object p,Class<?> pt,Field vf)throws Exception {
    byte[] cpu=((byte[])WaterPublicationFailureChecks.field(pt,"presentation").get(p)).clone();GpuBuffer source=(GpuBuffer)vf.get(WaterPublicationFailureChecks.field(pt,"gpu").get(p));read(reads,id,"target",phase,0,cpu,source);
  }
  private static void readSample(List<Read> reads,int id,String label,Object p,Class<?> pt,Field vf,int tag)throws Exception {
    GpuBuffer source=(GpuBuffer)vf.get(WaterPublicationFailureChecks.field(pt,"gpu").get(p));int n=(int)Math.min(144,source.size());byte[] expected=new byte[n];Arrays.fill(expected,(byte)tag);read(reads,id,label,"prefix",0,expected,source);if(source.size()>n)read(reads,id,label,"suffix",source.size()-n,expected,source);
  }
  private static void read(List<Read> reads,int id,String label,String phase,long offset,byte[] cpu,GpuBuffer source) {
    GpuBuffer copy=RenderSystem.getDevice().createBuffer(()->"Admission data readback",GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,cpu.length);RenderSystem.getDevice().createCommandEncoder().copyToBuffer(source.slice(offset,cpu.length),copy.slice());reads.add(new Read(id,label,phase,offset,cpu,copy));
  }
}
