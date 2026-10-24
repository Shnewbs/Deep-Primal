package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.core.lod.*;
import com.shnewbs.deepprimal.graphics.TerrainGpuMesh;
import com.shnewbs.deepprimal.settings.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.Minecraft;

/** Real worker/store/GPU upload cancellation in an isolated renderer. */
final class LodUploadRetryChecks {
  private static Field field(Class<?> c, String n) throws Exception {
    var f = c.getDeclaredField(n); f.setAccessible(true); return f;
  }
  static void run(Minecraft mc, Path output) throws Exception {
    var type = Class.forName("com.shnewbs.deepprimal.lod.client.VoxelLodRenderer");
    System.out.println("VALIDATION_CLASS_ORIGIN class=" + type.getName()
        + " location=" + type.getProtectionDomain().getCodeSource().getLocation());
    var ctor = type.getDeclaredConstructor(); ctor.setAccessible(true); Object renderer = ctor.newInstance();
    var start = type.getDeclaredMethod("startDiskJob"); start.setAccessible(true);
    var upload = type.getDeclaredMethod("processUploads"); upload.setAccessible(true);
    var evict = type.getDeclaredMethod("evictToFit", long.class, int.class, Long.class,
        net.minecraft.world.phys.Vec3.class); evict.setAccessible(true);
    var worker = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2));
    int memory = LodConfig.MEMORY.get(), uploads = LodConfig.UPLOADS.get();
    boolean persist = LodConfig.PERSIST.get(), pause = ClientPerformanceConfig.PAUSE_BACKGROUND.get();
    long partialBytes = 0;
    try {
      int x = mc.player.chunkPosition().x(), z = mc.player.chunkPosition().z();
      long key = ((long) x << 32) ^ (z & 0xffffffffL), ballastKey = key + (1L << 32);
      int[] offsets = new int[257], runs = new int[256 * 3];
      for (int i = 0; i < 256; i++) { offsets[i] = i * 3; runs[i*3] = 192; runs[i*3+1] = 193; }
      offsets[256] = runs.length;
      var store = new LodStore(output.resolve("upload-retry-store"));
      store.write(new ColumnarChunk(x,z,192,1,42,List.of("ff777777|minecraft:stone"),offsets,runs));
      field(type,"store").set(renderer,store); field(type,"worker").set(renderer,worker);
      @SuppressWarnings("unchecked") var disk = (Collection<Long>) field(type,"disk").get(renderer);
      @SuppressWarnings("unchecked") var cache = (Map<Long,Object>) field(type,"cache").get(renderer);
      @SuppressWarnings("unchecked") var latest = (Map<Long,Long>) field(type,"latest").get(renderer);
      @SuppressWarnings("unchecked") var dirty = (Map<Long,Long>) field(type,"dirty").get(renderer);
      LodConfig.PERSIST.set(true); LodConfig.MEMORY.set(8); LodConfig.UPLOADS.set(1);
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(false); disk.add(key);
      if (!(Boolean)start.invoke(renderer)) throw new AssertionError("Initial real disk read missing");
      Object ready = ((Future<?>)field(type,"future").get(renderer)).get(5,TimeUnit.SECONDS);
      field(type,"future").set(renderer,null); field(type,"workingKey").set(renderer,null);
      var pendingType = Class.forName(type.getName()+"$PendingGpu");
      var pc = pendingType.getDeclaredConstructor(ready.getClass()); pc.setAccessible(true);
      var entryType = Class.forName(type.getName()+"$Entry");
      var ec = entryType.getDeclaredConstructor(long.class,List.class,long.class,int[].class,int.class,boolean.class);
      ec.setAccessible(true);
      var buffer = RenderSystem.getDevice().createBuffer(() -> "LOD cancelled upload ballast",
          com.mojang.blaze3d.buffers.GpuBuffer.USAGE_VERTEX,4L*1048576);
      boolean owned = false;
      try {
        var ballast = new TerrainGpuMesh(x+1,z,192,193,buffer,null,3,0,0,new short[0]);
        cache.put(ballastKey,ec.newInstance(0L,List.of(List.of(ballast),List.of(),List.of()),
            ballast.byteSize,new int[]{0},193,false)); owned = true;
        type.getField("residentBytes").setLong(renderer,ballast.byteSize);
        field(type,"pendingGpu").set(renderer,pc.newInstance(ready));
        for(int i=0;i<16 && type.getField("residentBytes").getLong(renderer)==ballast.byteSize;i++) upload.invoke(renderer);
        partialBytes = type.getField("residentBytes").getLong(renderer)-ballast.byteSize;
        if(partialBytes<=0 || field(type,"pendingGpu").get(renderer)==null)
          throw new AssertionError("Actual partial GPU upload required");
        LodConfig.MEMORY.set(4); upload.invoke(renderer);
        boolean indexed = disk.contains(key);
        System.out.println("LOD_UPLOAD_CANCEL_OBSERVED partialBytes="+partialBytes+" indexed="+indexed
            +" resident="+type.getField("residentBytes").getLong(renderer));
        if(field(type,"pendingGpu").get(renderer)!=null || type.getField("residentBytes").getLong(renderer)!=ballast.byteSize)
          throw new AssertionError("Cancelled partial upload must release GPU payload");
        if(!indexed) throw new AssertionError("Cancelled persisted upload lost its recovery key");
        for(int i=0;i<16;i++) if((Boolean)start.invoke(renderer)) throw new AssertionError("Cancelled page retried without free bytes");
        evict.invoke(renderer,0L,0,null,mc.player.position());
        if(!(Boolean)start.invoke(renderer)) throw new AssertionError("Cancelled page did not wake after bytes freed");
        ready=((Future<?>)field(type,"future").get(renderer)).get(5,TimeUnit.SECONDS);
        field(type,"future").set(renderer,null);field(type,"workingKey").set(renderer,null);
        field(type,"pendingGpu").set(renderer,pc.newInstance(ready));
        LodConfig.UPLOADS.set(4);
        for(int i=0;i<32 && field(type,"pendingGpu").get(renderer)!=null;i++) upload.invoke(renderer);
        if(!cache.containsKey(key)) throw new AssertionError("Cancelled page did not regain GPU residency");
      } finally { if(!owned)buffer.close(); }
      // Admission guards for the cancellation path, independently of eviction guards.
      for(String control:List.of("dirty","newer-revision","not-persisted","persistence-disabled","obsolete-session")) {
        LodConfig.PERSIST.set(false); evict.invoke(renderer,0L,0,null,mc.player.position());
        LodConfig.PERSIST.set(true); disk.clear(); latest.clear(); dirty.clear();
        Object candidate = ready;
        if(control.equals("not-persisted") || control.equals("obsolete-session")) {
          var cs=ready.getClass().getRecordComponents();var values=new Object[cs.length];var types=new Class<?>[cs.length];
          for(int i=0;i<cs.length;i++){types[i]=cs[i].getType();var a=cs[i].getAccessor();a.setAccessible(true);values[i]=a.invoke(ready);}
          if(control.equals("not-persisted"))values[cs.length-1]=false;else values[0]=99L;
          var rc=ready.getClass().getDeclaredConstructor(types);rc.setAccessible(true);candidate=rc.newInstance(values);
        }
        if(control.equals("dirty"))dirty.put(key,43L);
        if(control.equals("newer-revision"))latest.put(key,43L);
        if(control.equals("persistence-disabled"))LodConfig.PERSIST.set(false);
        // Occupancy ledger forces pre-upload cancellation without allocating another GPU page.
        // The primary reproduction above uses an actual allocation and a partial upload.
        type.getField("residentBytes").setLong(renderer,4L*1048576);
        field(type,"pendingGpu").set(renderer,pc.newInstance(candidate)); upload.invoke(renderer);
        type.getField("residentBytes").setLong(renderer,0);
        if(disk.contains(key))throw new AssertionError("Invalid cancellation retry: "+control);
        System.out.println("LOD_UPLOAD_RETRY_GUARD_PASS control="+control);
      }
    } finally {
      type.getMethod("reset").invoke(renderer);worker.shutdownNow();
      LodConfig.MEMORY.set(memory); LodConfig.UPLOADS.set(uploads); LodConfig.PERSIST.set(persist);
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(pause);
    }
    if(type.getField("residentBytes").getLong(renderer)!=0)throw new AssertionError("Isolated resources retained");
    String record="LOD_UPLOAD_RETRY_NATIVE_PASS partialBytes="+partialBytes
        +" budgetReducedMiB=8-to-4 actualBallastBytes=4194304 blockedCalls=16 restored=true guards=5 isolatedGpuReleased=true\n";
    Files.writeString(output.resolve("lod-upload-retry.txt"),record);System.out.print(record);
  }
  private LodUploadRetryChecks() {}
}
