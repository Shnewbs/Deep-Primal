package com.shnewbs.deepprimal.core;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import com.shnewbs.deepprimal.core.ChunkMaterializer.*;

/** Coalesced, bounded CPU queries. Worlds and dimensions are part of cache identity. */
public final class WorldGenPipeline {
  private final ChunkMaterializer materializer;private final ChunkCache cache;
  private final Map<ChunkKey,CompletableFuture<MaterializedChunk>> pending=new HashMap<>();
  private volatile boolean stopped;
  public WorldGenPipeline(ChunkMaterializer materializer,ChunkCache cache){this.materializer=materializer;this.cache=cache;}
  public synchronized CompletableFuture<MaterializedChunk> generateChunk(ChunkKey key){
    if(stopped)return CompletableFuture.failedFuture(new IllegalStateException("pipeline stopped"));
    MaterializedChunk ready=cache.get(key);if(ready!=null)return CompletableFuture.completedFuture(ready);
    var existing=pending.get(key);if(existing!=null)return existing.copy();
    if(pending.size()>=64)return CompletableFuture.failedFuture(new RejectedExecutionException("64 pending chunk queries"));
    var result=new CompletableFuture<MaterializedChunk>();pending.put(key,result);
    try{materializer.materializeAsync(key,()->stopped).whenComplete((chunk,error)->{
      Throwable failure=error;
      synchronized(this){
        pending.remove(key);
        if(stopped)failure=new CancellationException("pipeline stopped");
        else if(failure==null)cache.put(key,chunk,chunk.payloadBytes());
      }
      // User continuations may call back into EngineCore: never run them under this lock.
      if(failure!=null)result.completeExceptionally(failure);else result.complete(chunk);
    });}catch(RuntimeException error){pending.remove(key);result.completeExceptionally(error);}
    return result.copy();
  }
  public CompletableFuture<Void> generateChunks(Iterable<ChunkKey> keys,Consumer<MaterializedChunk> consumer){
    Objects.requireNonNull(consumer);
    var batch=new ArrayList<ChunkKey>();for(var key:keys){if(batch.size()>=4096)return CompletableFuture.failedFuture(new IllegalArgumentException("batch exceeds 4096"));batch.add(Objects.requireNonNull(key));}
    Iterator<ChunkKey> iterator=batch.iterator();
    CompletableFuture<Void> chain=CompletableFuture.completedFuture(null);
    // At most 4096 items per explicit batch; sequential admission avoids queue floods.
    int count=0;while(iterator.hasNext()){if(++count>4096)return CompletableFuture.failedFuture(new IllegalArgumentException("batch exceeds 4096"));ChunkKey key=iterator.next();chain=chain.thenCompose(v->generateChunk(key).thenAccept(consumer));}return chain;
  }
  public void shutdown(){
    List<CompletableFuture<MaterializedChunk>> cancelled;
    synchronized(this){stopped=true;cancelled=new ArrayList<>(pending.values());pending.clear();cache.clear();}
    for(var future:cancelled)future.completeExceptionally(new CancellationException("pipeline stopped"));
  }
  public synchronized int pendingCount(){return pending.size();}
}
