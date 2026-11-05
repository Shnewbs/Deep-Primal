package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.*;
import com.shnewbs.deepprimal.graphics.WaterGpuMesh;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;

/** Actual water meshes/GPU ownership, controlled presentation clock, no authoritative world edits. */
final class WaterFadeContinuityChecks {
  private record Readback(GpuBuffer buffer,byte[] expected,float height,short displacement,float x,float z,
      boolean displayed,long elapsed) {}
  private static Field field(Class<?> type,String name)throws ReflectiveOperationException {
    var f=type.getDeclaredField(name);f.setAccessible(true);return f;
  }
  private static Object page,renderer;
  private static Class<?> type,pageType,readyType;
  private static Constructor<?> readyConstructor;
  private static Method upload;
  private static long revision;

  private static WaterPatchMesher.Snapshot snapshot(boolean wet) {
    return snapshot(wet ? 16 : 0);
  }
  private static WaterPatchMesher.Snapshot snapshot(int amount) {
    byte[] units=new byte[WaterPatchMesher.CELLS],solid=new byte[units.length];
    int[] light=new int[units.length],colors=new int[256];Arrays.fill(colors,0xff3f76e4);
    for(int z=0;z<16;z++)for(int x=0;x<16;x++) {
      solid[WaterPatchMesher.index(x,-1,z)]=1;
      units[WaterPatchMesher.index(x,0,z)]=(byte)amount;
    }
    return new WaterPatchMesher.Snapshot(units,solid,light,colors);
  }
  private static void push(WaterPatchMesher.Snapshot next,WaterPatchMesher.Snapshot previous,long time)throws ReflectiveOperationException {
    field(type,"clock").setLong(renderer,time);field(pageType,"changed").setBoolean(page,false);
    upload.invoke(renderer,readyConstructor.newInstance(0L,++revision,page,next,WaterPatchMesher.build(next,previous,true)));
  }
  private static Object gpu()throws ReflectiveOperationException{return field(pageType,"gpu").get(page);}
  private static long bytes()throws ReflectiveOperationException{return type.getField("residentBytes").getLong(renderer);}

  private static void read(List<Readback> reads,float height,short displacement)throws ReflectiveOperationException {
    read(reads,height,displacement,8,8,false);
  }

  private static void read(List<Readback> reads,float height,short displacement,float x,float z)throws ReflectiveOperationException {
    read(reads,height,displacement,x,z,true);
  }

  private static void read(List<Readback> reads,float height,short displacement,float x,float z,
      boolean displayed)throws ReflectiveOperationException {
    var expected=((byte[])field(pageType,"presentation").get(page)).clone();
    var source=(GpuBuffer)field(WaterGpuMesh.class,"vertices").get(gpu());
    var buffer=RenderSystem.getDevice().createBuffer(()->"Stopped water readback",
        GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_MAP_READ,expected.length);
    reads.add(new Readback(buffer,expected,height,displacement,x,z,displayed,
        field(type,"clock").getLong(renderer)-field(pageType,"uploaded").getLong(page)));
    RenderSystem.getDevice().createCommandEncoder().copyToBuffer(source.slice(0,expected.length),buffer.slice());
  }

  private static void readable()throws ReflectiveOperationException {
    var vertices=field(WaterGpuMesh.class,"vertices");
    var source=(GpuBuffer)vertices.get(gpu());
    byte[] accepted=(byte[])field(pageType,"presentation").get(page);
    var data=org.lwjgl.system.MemoryUtil.memAlloc(accepted.length);
    GpuBuffer replacement;
    try {
      data.put(accepted).flip();
      replacement=RenderSystem.getDevice().createBuffer(()->"Readable stopped water vertices",
          GpuBuffer.USAGE_VERTEX|GpuBuffer.USAGE_COPY_DST|GpuBuffer.USAGE_COPY_SRC,data);
    } finally {
      org.lwjgl.system.MemoryUtil.memFree(data);
    }
    source.close();vertices.set(gpu(),replacement);
  }

  static CompletableFuture<Void> run(Minecraft mc,Path output)throws ReflectiveOperationException,java.io.IOException {
    Files.createDirectories(output);
    var result=new CompletableFuture<Void>();
    var reads=new ArrayList<Readback>();
    boolean queued=false;
    type=AnimatedWaterRenderer.class;
    System.out.println("VALIDATION_CLASS_ORIGIN class="+type.getName()+" location="+type.getProtectionDomain().getCodeSource().getLocation());
    var ctor=type.getDeclaredConstructor();ctor.setAccessible(true);renderer=ctor.newInstance();
    pageType=Class.forName(type.getName()+"$Page");var pc=pageType.getDeclaredConstructor(long.class);pc.setAccessible(true);
    long key=SectionPos.asLong(mc.player==null?0:mc.player.chunkPosition().x(),32,
        mc.player==null?0:mc.player.chunkPosition().z());page=pc.newInstance(key);
    @SuppressWarnings("unchecked") var pages=(Map<Long,Object>)field(type,"pages").get(renderer);pages.put(key,page);
    readyType=Class.forName(type.getName()+"$Ready");readyConstructor=readyType.getDeclaredConstructor(long.class,long.class,pageType,WaterPatchMesher.Snapshot.class,WaterPatchMesher.Mesh.class);readyConstructor.setAccessible(true);
    upload=type.getDeclaredMethod("upload",readyType);upload.setAccessible(true);
    var wet=snapshot(true);var dry=snapshot(false);long retained=0;
    try {
      if(WaterPatchMesher.build(dry,dry,true).count()!=0)throw new AssertionError("Repeated dry snapshot must be empty");
      push(wet,wet,0);if(gpu()==null||bytes()<=0)throw new AssertionError("Actual wet GPU upload required");
      push(dry,wet,100);Object fading=gpu();retained=bytes();long deadline=field(pageType,"cleanup").getLong(page);
      if(fading==null||retained<=0||deadline!=201)throw new AssertionError("Drying page must own its 100 ms fade");
      push(dry,dry,125);
      System.out.println("WATER_DRY_FADE_OBSERVED elapsed=25 remaining=76 retained="+(gpu()==fading)+" bytes="+bytes());
      if(gpu()!=fading||bytes()!=retained||!pages.containsKey(key))
        throw new AssertionError("Empty recapture removed water before its fade completed");
      for(long time:new long[]{150,200}) {
        push(dry,dry,time);
        if(gpu()!=fading||bytes()!=retained||field(pageType,"cleanup").getLong(page)!=deadline)
          throw new AssertionError("Empty recapture must retain GPU ownership without extending fade");
      }
      push(dry,dry,deadline);
      if(gpu()!=null||bytes()!=0||pages.containsKey(key))throw new AssertionError("Completed dry fade must release GPU/page");
      System.out.println("WATER_DRY_FADE_LIFETIME_PASS repeatedEmptyUpdates=3 deadline=201 noExtension=true released=true");
      // A newly wet update interrupts a drying fade from the currently displayed opacity.
      pages.put(key,page);push(wet,wet,300);push(dry,wet,400);push(wet,dry,450);
      @SuppressWarnings("unchecked") var alpha=(List<Map<Long,WaterSurfaceTransition>>)field(type,"alpha").get(renderer);
      int x=field(pageType,"x").getInt(page),y=field(pageType,"y").getInt(page),z=field(pageType,"z").getInt(page);
      var opacity=alpha.get(0).get(BlockPos.asLong(x+8,y,z+8));
      if(opacity==null||Math.abs(opacity.at(450)-.5f)>.00001||opacity.target()!=1||gpu()==null)
        throw new AssertionError("Rewet must retarget the real page from half opacity");
      if(Math.abs(opacity.at(550)-1)>.00001)throw new AssertionError("Rewet opacity did not finish");
      // Preserve the existing newer-edit guard even beyond an old cleanup deadline.
      field(pageType,"changed").setBoolean(page,true);field(type,"clock").setLong(renderer,600);
      Object current=gpu();upload.invoke(renderer,readyConstructor.newInstance(0L,++revision,page,dry,WaterPatchMesher.build(dry,dry,true)));
      if(gpu()!=current)throw new AssertionError("Pending edit guard was lost");
      System.out.println("WATER_REWET_CONTINUITY_PASS displayedAtRetarget=0.5 target=1 newerEditGuard=true");
      type.getMethod("reset").invoke(renderer);
      pages.put(key,page);
      var eight=snapshot(8);var nine=snapshot(9);
      push(eight,eight,1000);
      readable();
      push(eight,eight,1000);push(nine,eight,1050);push(eight,nine,1100);
      @SuppressWarnings("unchecked") var heights=(List<Map<Long,WaterSurfaceTransition>>)field(type,"heights").get(renderer);
      var height=heights.get(0).get(BlockPos.asLong(x+8,y,z+8));
      long holdDeadline=field(pageType,"cleanup").getLong(page);
      if(height==null||height.target()!=.53125f||holdDeadline!=1501||gpu()==null)
        throw new AssertionError("Midpoint must retain a cleanup after its visible interpolation");
      push(eight,eight,1350);
      if(height.target()!=.53125f||field(pageType,"cleanup").getLong(page)!=holdDeadline)
        throw new AssertionError("Unchanged GPU recapture extended the midpoint hold");
      read(reads,.53125f,(short)0);
      push(eight,eight,holdDeadline);
      if(height.target()!=.5f||height.at(holdDeadline)!=.53125f
          ||field(pageType,"cleanup").getLong(page)!=1602)
        throw new AssertionError("Expired GPU midpoint must return continuously to the real height");
      read(reads,.5f,(short)512);
      push(eight,eight,1602);
      if(height.at(1602)!=.5f||height.target()!=.5f||field(pageType,"cleanup").getLong(page)!=0||gpu()==null)
        throw new AssertionError("Static water must finish exact and stop scheduling cleanup");
      var accepted=java.nio.ByteBuffer.wrap((byte[])field(pageType,"presentation").get(page))
          .order(java.nio.ByteOrder.nativeOrder());
      int matchingVertices=0;
      for(int offset=0;offset<accepted.limit();offset+=36)
        if(accepted.getFloat(offset)==8&&accepted.getFloat(offset+8)==8) {
          if(accepted.getFloat(offset+4)!=.5f||accepted.getShort(offset+24)!=0)
            throw new AssertionError("Uploaded static corner retained a midpoint/displacement");
          matchingVertices++;
        }
      if(matchingVertices<4)throw new AssertionError("Missing uploaded interior corner witnesses");
      read(reads,.5f,(short)0);
      System.out.println("WATER_MIDPOINT_RELEASE_NATIVE_PASS holdDeadline=1501"
          +" finalCleanup=0 exactUploadHeight=0.5 continuous=true noExtension=true vertices="+matchingVertices);
      type.getMethod("reset").invoke(renderer);
      page=pc.newInstance(key);pages.put(key,page);push(eight,eight,2000);readable();Object left=page;
      long neighborKey=SectionPos.asLong(SectionPos.x(key)+1,SectionPos.y(key),SectionPos.z(key));
      page=pc.newInstance(neighborKey);pages.put(neighborKey,page);push(eight,eight,2000);readable();Object right=page;
      var sync=type.getDeclaredMethod("synchronizeVisibleHeights",List.class);sync.setAccessible(true);
      page=left;push(nine,eight,2050);
      byte[] beforeShared=((byte[])field(pageType,"presentation").get(right)).clone();
      var device=RenderSystem.getDevice();
      var backend=Arrays.stream(device.getClass().getDeclaredFields())
          .filter(f->f.getType().isInterface()&&f.getType().getSimpleName().equals("GpuDeviceBackend"))
          .findFirst().orElseThrow();backend.setAccessible(true);
      Object original=backend.get(device);
      Object rejecting=Proxy.newProxyInstance(backend.getType().getClassLoader(),
          new Class<?>[]{backend.getType()},(proxy,method,args)->{
            Object encoder;
            try {encoder=method.invoke(original,args);}
            catch(InvocationTargetException failure){throw failure.getCause();}
            if(!method.getName().equals("createCommandEncoder"))return encoder;
            return Proxy.newProxyInstance(method.getReturnType().getClassLoader(),
                new Class<?>[]{method.getReturnType()},(p,operation,arguments)->{
                  if(operation.getName().equals("writeToBuffer"))
                    throw new IllegalStateException("INJECTED_SHARED_HEIGHT_REJECTION");
                  try {return operation.invoke(encoder,arguments);}
                  catch(InvocationTargetException failure){throw failure.getCause();}
                });
          });
      field(type,"clock").setLong(renderer,2075);
      backend.set(device,rejecting);
      try {
        sync.invoke(renderer,List.of(right));
        if(!Arrays.equals(beforeShared,(byte[])field(pageType,"presentation").get(right))
            ||field(pageType,"heightFailures").getInt(right)!=1)
          throw new AssertionError("Rejected height upload committed CPU bytes or lost its retry");
      } finally {
        backend.set(device,original);
      }
      field(type,"clock").setLong(renderer,2075);sync.invoke(renderer,List.of(left,right));
      if(field(pageType,"heightFailures").getInt(right)!=0)
        throw new AssertionError("Successful next-frame height retry did not reset failures");
      byte[] afterShared=(byte[])field(pageType,"presentation").get(right);
      for(int i=0;i<beforeShared.length;i++) {
        int component=i%36;
        if((component<4||component>=8)&&(component<24||component>=26)
            &&beforeShared[i]!=afterShared[i])
          throw new AssertionError("Shared height publication changed material/light/opacity/clock bytes");
      }
      read(reads,.515625f,(short)0,16,8);page=right;read(reads,.515625f,(short)0,0,8);
      page=left;push(eight,nine,2100);
      field(type,"clock").setLong(renderer,2175);sync.invoke(renderer,List.of(left,right));
      read(reads,.53125f,(short)0,16,8);page=right;read(reads,.53125f,(short)0,0,8);
      // The last-changing page need not upload again or remain visible for expiry.
      field(type,"clock").setLong(renderer,2501);sync.invoke(renderer,List.of(right));
      field(type,"clock").setLong(renderer,2551);sync.invoke(renderer,List.of(left,right));
      page=left;read(reads,.515625f,(short)0,16,8);page=right;read(reads,.515625f,(short)0,0,8);
      field(type,"clock").setLong(renderer,2601);sync.invoke(renderer,List.of(left,right));
      read(reads,.5f,(short)0,0,8);page=left;read(reads,.5f,(short)0,16,8);
      long writes=type.getField("heightBoundaryWrites").getLong(renderer);
      sync.invoke(renderer,List.of(left,right));
      if(type.getField("heightBoundaryWrites").getLong(renderer)!=writes)
        throw new AssertionError("Steady shared boundaries resubmitted unchanged GPU bytes");
      page=left;push(nine,eight,2601);field(type,"clock").setLong(renderer,2626);
      backend.set(device,rejecting);
      boolean fatal=false;
      try {
        sync.invoke(renderer,List.of(right));
        sync.invoke(renderer,List.of(right));
        try {sync.invoke(renderer,List.of(right));}
        catch(InvocationTargetException failure) {
          if(!(failure.getCause() instanceof IllegalStateException)
              ||!failure.getCause().getMessage().equals("Repeated shared water height upload failure"))
            throw failure;
          fatal=true;
        }
      } finally {
        backend.set(device,original);
      }
      if(!fatal)throw new AssertionError("Repeated height upload failure was silently ignored");
      long retainedBytes=retained;
      RenderSystem.queueFencedTask(()->{
        try {
          int witnesses=0;
          for(var read:reads)
            try(var mapped=RenderSystem.getDevice().createCommandEncoder().mapBuffer(read.buffer,true,false)) {
              var data=mapped.data().order(java.nio.ByteOrder.nativeOrder());
              byte[] actual=new byte[read.expected.length];data.get(0,actual);
              if(!Arrays.equals(actual,read.expected))
                throw new AssertionError("Stopped water GPU buffer differs from accepted upload");
              int corners=0;
              for(int offset=0;offset<actual.length;offset+=36)
                if(data.getFloat(offset)==read.x&&data.getFloat(offset+8)==read.z
                    &&WaterCornerPacket.packed(data,offset)) {
                  float actualHeight=data.getFloat(offset+4);
                  if(read.displayed) {
                    int age=(byte)data.getShort(offset+26);
                    actualHeight+=data.getShort(offset+24)/16384f
                        *(1-Math.clamp((read.elapsed-age)/100f,0,1));
                  }
                  if(Math.abs(actualHeight-read.height)>.00001f
                      ||(!read.displayed&&data.getShort(offset+24)!=read.displacement))
                    throw new AssertionError("Read-back water differs: expected="+read.height+" actual="+actualHeight
                        +" x="+read.x+" z="+read.z+" displayed="+read.displayed+" elapsed="+read.elapsed
                        +" target="+data.getFloat(offset+4)+" displacement="+data.getShort(offset+24));
                  corners++;
                }
              if(corners<2)throw new AssertionError("Missing read-back corner witnesses");
              witnesses+=corners;
            }
          String record="WATER_FADE_CONTINUITY_NATIVE_PASS retainedBytes="+retainedBytes
              +" repeatedEmptyUpdates=3 fadeMillis=100 noExtension=true rewetContinuous=true newerEditGuard=true"
              +" midpointRelease=true isolatedGpuReleased=true gpuReadbacks="+reads.size()+" differentBytes=0"
              +" cornerWitnesses="+witnesses
              +" sharedHeightFrames=4 sharedHeightContinuous=true unchangedBoundaryWrites=0"
              +" rejectedHeightWritePreserved=true nextFrameRecovery=true nonHeightBytesPreserved=true"
              +" thirdConsecutiveFailureFatal=true"
              +" hiddenSourceExpiry=true"
              +" scope=actualWaterGpuReadbackControlledClockNotCompleteSceneFlickerAcceptance\n";
          Files.writeString(output.resolve("water-fade-continuity.txt"),record);System.out.print(record);
          result.complete(null);
        } catch(java.io.IOException|RuntimeException|AssertionError failure) {
          result.completeExceptionally(failure);
          throw new RuntimeException("Stopped water GPU readback failed",failure);
        } finally {
          for(var read:reads)read.buffer.close();
        }
      });
      queued=true;
    } finally {
      type.getMethod("reset").invoke(renderer);((ExecutorService)field(type,"workers").get(renderer)).shutdownNow();
      if(!queued)for(var read:reads)read.buffer.close();
    }
    if(bytes()!=0)throw new AssertionError("Isolated water GPU payload retained");
    return result;
  }
  private WaterFadeContinuityChecks() {}
}
