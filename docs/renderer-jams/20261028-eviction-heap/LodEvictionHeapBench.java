package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Arrays;

/** Isolated CPU-only paired selector benchmark; not native frames or GPU timing. */
public final class LodEvictionHeapBench {
  private static volatile long sink;
  private LodEvictionHeapBench() {}
  private static long key(int x, int z) {return ((long)x<<32) ^ (z & 0xffffffffL);}
  private static long old(LinkedHashMap<Long,Integer> cache, Long keep,
      double cx, double cz, int evictions) {
    var victims = new ArrayList<>(cache.keySet());
    victims.sort(Comparator.<Long>comparingInt(k -> keep != null && k.longValue()==keep ? 1 : 0)
        .thenComparing(Comparator.comparingDouble((Long k) -> {
          double dx=(int)(k>>32)*16.+8-cx,dz=(int)(long)k*16.+8-cz;
          return dx*dx+dz*dz;
        }).reversed()));
    long checksum=0;
    for(int i=0;i<evictions;i++) checksum^=victims.get(i);
    return checksum;
  }
  private static long newer(LinkedHashMap<Long,Integer> cache, Long keep,
      double cx, double cz, int evictions) {
    var victims = new LodEvictionHeap(cache.keySet(), keep, cx, cz);
    long checksum=0;
    for(int i=0;i<evictions;i++) checksum^=victims.next();
    return checksum;
  }
  private static long pct(long[] values,int percentile){
    long[] sorted=values.clone();Arrays.sort(sorted);
    return sorted[Math.min(sorted.length-1,(int)Math.ceil(sorted.length*percentile/100.0)-1)];
  }
  public static void main(String[] args) {
    Random random = new Random(19792);
    for(int capacity:new int[]{256,4096}){
      var map=new LinkedHashMap<Long,Integer>(capacity,.75f,true);
      while(map.size()<capacity){map.put(key(random.nextInt(6000)-3000,random.nextInt(6000)-3000),map.size());}
      Long keep = map.keySet().iterator().next();
      for (int remove: new int[]{1,16,Math.min(256,capacity),capacity}) {
        int warm=1000, samples=360;
        for(int i=0;i<warm;i++){sink^=old(map,keep,128.0,-1024.0,remove);sink^=newer(map,keep,128.0,-1024.0,remove);}
        long[] oldTimes=new long[samples],newTimes=new long[samples];
        for(int i=0;i<samples;i++){
          long start=System.nanoTime();
          if((i&1)==0){sink^=old(map,keep,128.0,-1024.0,remove);oldTimes[i]=System.nanoTime()-start;
            start=System.nanoTime();sink^=newer(map,keep,128.0,-1024.0,remove);newTimes[i]=System.nanoTime()-start;
          }else{sink^=newer(map,keep,128.0,-1024.0,remove);newTimes[i]=System.nanoTime()-start;
            start=System.nanoTime();sink^=old(map,keep,128.0,-1024.0,remove);oldTimes[i]=System.nanoTime()-start;}
        }
        System.out.printf("LOD_EVICTION_BENCH pages=%d removes=%d oldNs_p50=%d oldNs_p95=%d heapNs_p50=%d heapNs_p95=%d%n",
            capacity,remove,pct(oldTimes,50),pct(oldTimes,95),pct(newTimes,50),pct(newTimes,95));
      }
    }
    System.out.println("BENCH_SINK="+sink);
  }
}
