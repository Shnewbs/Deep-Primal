package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.lod.client.VoxelLodRenderer;
import com.shnewbs.deepprimal.settings.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.Minecraft;

/** Native production tick with controlled scan completion; no synthetic terrain or GPU payload. */
final class LodIndexMergeChecks {
  private static Field field(Class<?> c,String n) throws Exception {
    var f=c.getDeclaredField(n);f.setAccessible(true);return f;
  }
  static void run(Minecraft mc,Path output) throws Exception {
    var type=VoxelLodRenderer.class;
    System.out.println("VALIDATION_CLASS_ORIGIN class="+type.getName()+" location="
        +type.getProtectionDomain().getCodeSource().getLocation());
    var ctor=type.getDeclaredConstructor();ctor.setAccessible(true);
    var tick=type.getMethod("tick",net.neoforged.neoforge.client.event.ClientTickEvent.Post.class);
    boolean enabled=LodConfig.ENABLED.get();LodConfig.ENABLED.set(true);
    int cases=0;
    try {
      for(int seeded:new int[]{1,8192,0,4096}) {
        Object renderer=ctor.newInstance();
        var worker=new ThreadPoolExecutor(1,1,20,TimeUnit.SECONDS,new ArrayBlockingQueue<>(2));
        try {
          field(type,"level").set(renderer,mc.level);field(type,"worker").set(renderer,worker);
          // An in-flight placeholder stops unrelated capture scheduling after the actual scan merge.
          field(type,"future").set(renderer,new CompletableFuture<>());
          @SuppressWarnings("unchecked") var disk=(LinkedHashSet<Long>)field(type,"disk").get(renderer);
          @SuppressWarnings("unchecked") var latest=(Map<Long,Long>)field(type,"latest").get(renderer);
          @SuppressWarnings("unchecked") var deferred=(Map<Long,Object>)field(type,"deferredDisk").get(renderer);
          int cx=mc.player.chunkPosition().x(),cz=mc.player.chunkPosition().z();
          long guarded=VoxelLodRenderer.key(cx-1,cz);
          for(int i=0;i<seeded;i++)disk.add(VoxelLodRenderer.key(cx-1-i,cz));
          Object guard=new Object();
          if(seeded>0){latest.put(guarded,43L);deferred.put(guarded,guard);}
          var keys=new ArrayList<Long>();
          // Reverse ordering verifies production nearest-first sorting remains effective.
          for(int i=8192;i>0;i--)keys.add(VoxelLodRenderer.key(cx+i,cz));
          if(seeded==4096){keys.clear();for(long key:disk)keys.add(key);for(int i=4096;i>0;i--)keys.add(VoxelLodRenderer.key(cx+i,cz));}
          field(type,"scan").set(renderer,CompletableFuture.completedFuture(keys));
          tick.invoke(renderer,(Object)null);
          System.out.println("LOD_INDEX_MERGE_OBSERVED seeded="+seeded+" incoming="+keys.size()+" merged="+disk.size());
          if(disk.size()!=8192)throw new AssertionError("Completed LOD scan exceeded bounded recovery index: "+disk.size());
          if(field(type,"scan").get(renderer)!=null)throw new AssertionError("Completed scan future retained");
          if(seeded>0 && (disk.getFirst()!=guarded || latest.get(guarded)!=43L || deferred.get(guarded)!=guard))
            throw new AssertionError("Existing recovery order/edit guard was overwritten");
          if(seeded==1 && (!disk.contains(VoxelLodRenderer.key(cx+1,cz)) || disk.contains(VoxelLodRenderer.key(cx+8192,cz))))
            throw new AssertionError("New scan admissions must keep nearest-first remaining slots");
          cases++;
        } finally {type.getMethod("reset").invoke(renderer);worker.shutdownNow();}
      }
    } finally {LodConfig.ENABLED.set(enabled);}
    String record="LOD_INDEX_MERGE_NATIVE_PASS cases="+cases
        +" indexBound=8192 existingOrderAndEditGuards=true nearestRemainingSlots=true completedScanReleased=true scope=controlledScanFutureActualNativeTick\n";
    Files.writeString(output.resolve("lod-index-merge.txt"),record);System.out.print(record);
  }
  private LodIndexMergeChecks() {}
}
