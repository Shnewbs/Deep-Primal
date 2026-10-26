package com.shnewbs.deepprimal.core;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/** World-scoped, synchronized weighted LRU; byte reservation cannot overflow. */
public final class ChunkCache {
  private record Entry(Object value,long bytes) {}
  private final LinkedHashMap<Object,Entry> entries = new LinkedHashMap<>(16,.75f,true);
  private final long capacity;
  private final int limit;
  private long bytes;
  public final AtomicLong hits=new AtomicLong(), misses=new AtomicLong(), evictions=new AtomicLong(), memoryPressureEvents=new AtomicLong();
  public ChunkCache(long megabytes,int limit) {
    if(megabytes<0 || megabytes>Long.MAX_VALUE/(1024*1024) || limit<1) throw new IllegalArgumentException("cache limits");
    capacity=megabytes*1024*1024; this.limit=limit;
  }
  @SuppressWarnings("unchecked") public synchronized <T> T get(Object key) {
    Entry e=entries.get(Objects.requireNonNull(key));
    if(e==null){misses.incrementAndGet();EngineMetrics.CACHE_MISSES.increment();return null;}
    hits.incrementAndGet();EngineMetrics.CACHE_HITS.increment();return (T)e.value;
  }
  public synchronized <T> T getOrCompute(Object key,Function<Object,T> computer,long weight) {
    T v=get(key); if(v!=null)return v;
    v=Objects.requireNonNull(computer.apply(key));put(key,v,weight);return v;
  }
  public synchronized void put(Object key,Object value,long weight) {
    Objects.requireNonNull(key);Objects.requireNonNull(value);
    if(weight<0)throw new IllegalArgumentException("negative cache weight");
    remove(key);
    if(weight>capacity || capacity==0)return;
    while(!entries.isEmpty() && (bytes>capacity-weight || entries.size()>=limit))evict();
    entries.put(key,new Entry(value,weight));bytes+=weight;
  }
  private void evict(){var it=entries.entrySet().iterator();bytes-=it.next().getValue().bytes;it.remove();evictions.incrementAndGet();EngineMetrics.CACHE_EVICTIONS.increment();}
  public synchronized boolean remove(Object key){var e=entries.remove(key);if(e==null)return false;bytes-=e.bytes;return true;}
  public synchronized void clear(){entries.clear();bytes=0;}
  public synchronized long getMemoryUsage(){return bytes;}
  public synchronized int size(){return entries.size();}
  public double getHitRate(){double h=hits.get(),m=misses.get();return h+m==0?1:h/(h+m);}
  public synchronized void trimTo(long target){if(target<0)throw new IllegalArgumentException("negative target");while(bytes>target && !entries.isEmpty())evict();}
  public synchronized void handleMemoryPressure(){memoryPressureEvents.incrementAndGet();trimTo(bytes/2);}
  public record Snapshot(int entries,long bytes,double hitRate,long evictions) {
    public static Snapshot from(ChunkCache c){synchronized(c){return new Snapshot(c.size(),c.bytes,c.getHitRate(),c.evictions.get());}}
  }
}
