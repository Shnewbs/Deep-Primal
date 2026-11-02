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
  public CompletableFuture<MaterializedChunk> generateChunk(ChunkKey key){
    final CompletableFuture<MaterializedChunk> result;
    synchronized(this){
    if(stopped)return CompletableFuture.failedFuture(new IllegalStateException("pipeline stopped"));
    MaterializedChunk ready=cache.get(key);if(ready!=null)return CompletableFuture.completedFuture(ready);
    var existing=pending.get(key);if(existing!=null)return existing.copy();
    if(pending.size()>=64)return CompletableFuture.failedFuture(new RejectedExecutionException("64 pending chunk queries"));
    result=new CompletableFuture<MaterializedChunk>();pending.put(key,result);
    }
    // An Executor may run inline or block on admission: dispatch outside the monitor.
    try{materializer.materializeAsync(key,()->stopped).whenComplete((chunk,error)->{
      Throwable failure=error;
      synchronized(this){
        // Keep this query discoverable by shutdown until its shared future is settled.
        // Otherwise shutdown can miss a completion between cache admission and complete().
        if(stopped)failure=new CancellationException("pipeline stopped");
        else if(failure==null){
          // An invalid successful result or a rejected cache admission must not
          // strand the shared query future (whenComplete's return value is ignored).
          if(chunk==null)failure=new IllegalStateException("materializer returned null chunk");
          else if(!key.equals(chunk.key()))
            failure=new IllegalStateException("materializer returned a chunk for a different key");
          else try{cache.put(key,chunk,chunk.payloadBytes());}
          catch(Throwable cacheFailure){failure=cacheFailure;}
        }
      }
      // User continuations may call back into EngineCore: never run them under this lock.
      try {
        if(failure!=null)result.completeExceptionally(failure);else result.complete(chunk);
      } finally {
        synchronized(this){pending.remove(key,result);}
      }
    });}catch(Throwable error){
      try {result.completeExceptionally(error);}
      finally {synchronized(this){pending.remove(key,result);}}
    }
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

