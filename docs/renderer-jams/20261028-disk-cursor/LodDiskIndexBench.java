package com.shnewbs.deepprimal.lod.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;

/** Diagnostic client-callback timing, not a Minecraft frame/GPU benchmark. */
public final class LodDiskIndexBench {
  private LodDiskIndexBench() {}
  private static long key(int x, int z) { return ((long)x << 32) ^ (z & 0xffffffffL); }
  private static long percentile(long[] samples, int p) {
    Arrays.sort(samples);
    return samples[Math.min(samples.length - 1,(int)Math.ceil(samples.length * p / 100.0) - 1)];
  }
  public static void main(String[] args) throws Exception {
    Path root=Files.createTempDirectory("lod-index-bench-");
    try {
      for(int i=0;i<8205;i++) Files.createFile(root.resolve((10000+i)+"_"+(12000+i%247)+".plod"));
      ArrayList<Long> callbacks=new ArrayList<>(), completes=new ArrayList<>();
      for(int run=0;run<18;run++) {
        LinkedHashSet<Long> disk=new LinkedHashSet<>();
        for(int i=0;i<8192;i++) disk.add(key(-200000-i,300000+i));
        LodDiskIndexService service=new LodDiskIndexService();
        long start=System.nanoTime();
        long max=0; int maxTick=-1;
        for(int tick=0;tick<2000;tick++) {
          long before=System.nanoTime();
          if(service.tick(root,10000,12000,disk,true,k->{}))throw new AssertionError("scan failed");
          long took=System.nanoTime()-before;
          if(run>=3) callbacks.add(took);
          if(took>max){max=took;maxTick=tick;}
          if(service.started()&&!service.scanning())break;
          Thread.sleep(1);
        }
        if(disk.size()!=8192 || !disk.contains(key(10000,12000)))
          throw new AssertionError("index did not admit nearest page");
        if(run>=3)completes.add(System.nanoTime()-start);
        service.reset();
        if(run>=3)System.out.println("RUN="+run+" completionMs="+((System.nanoTime()-start)/1e6)+" callbackMaxUs="+(max/1e3)+" maxTick="+maxTick);
      }
      long[] each=callbacks.stream().mapToLong(Long::longValue).toArray();
      long[] all=completes.stream().mapToLong(Long::longValue).toArray();
      System.out.println("LOD_DISK_CALLBACK_CPU_ONLY measuredCallbacks="+each.length+
        " p50ns="+percentile(each,50)+" p95ns="+percentile(each,95)+" p99ns="+percentile(each,99)+
        " maxns="+each[each.length-1]+" totalP50ms="+(percentile(all,50)/1e6));
    } finally {
      try (var stream = Files.walk(root)) {
        for (var p : stream.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(p);
      }
    }
  }
}
