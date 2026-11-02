package com.shnewbs.deepprimal.core;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicInteger;
import com.shnewbs.deepprimal.core.ChunkMaterializer.*;

/** Coalesced, bounded CPU queries. Worlds and dimensions are part of cache identity. */
public final class WorldGenPipeline {
  private final ChunkMaterializer materializer;private final ChunkCache cache;
  private static final int OPEN=0, SETTLING=1, CANCELLED=2;
  private static final class Query {
    final CompletableFuture<MaterializedChunk> future=new CompletableFuture<>();
    final AtomicInteger decision=new AtomicInteger(OPEN);
  }
  private final Map<ChunkKey,Query> pending=new HashMap<>();
  private volatile boolean stopped;
  public WorldGenPipeline(ChunkMaterializer materializer,ChunkCache cache){this.materializer=materializer;this.cache=cache;}
  public CompletableFuture<MaterializedChunk> generateChunk(ChunkKey key){
    final Query request;
    synchronized(this){
    if(stopped)return CompletableFuture.failedFuture(new IllegalStateException("pipeline stopped"));
    MaterializedChunk ready=cache.get(key);if(ready!=null)return CompletableFuture.completedFuture(ready);
    var existing=pending.get(key);if(existing!=null)return existing.future.copy();
    if(pending.size()>=64)return CompletableFuture.failedFuture(new RejectedExecutionException("64 pending chunk queries"));
    request=new Query();pending.put(key,request);
    }
    // An Executor may run inline or block on admission: dispatch outside the monitor.
    try{materializer.materializeAsync(key,()->stopped).whenComplete((chunk,error)->{
      Throwable failure=error;
      synchronized(this){
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
      settle(key,request,chunk,failure);
    });}catch(Throwable error){settle(key,request,null,error);}
    return request.future.copy();
  }
  public CompletableFuture<Void> generateChunks(Iterable<ChunkKey> keys,Consumer<MaterializedChunk> consumer){
    Objects.requireNonNull(consumer);
    var batch=new ArrayList<ChunkKey>();for(var key:keys){if(batch.size()>=4096)return CompletableFuture.failedFuture(new IllegalArgumentException("batch exceeds 4096"));batch.add(Objects.requireNonNull(key));}
    Iterator<ChunkKey> iterator=batch.iterator();
    CompletableFuture<Void> chain=CompletableFuture.completedFuture(null);
    // At most 4096 items per explicit batch; sequential admission avoids queue floods.
    int count=0;while(iterator.hasNext()){if(++count>4096)return CompletableFuture.failedFuture(new IllegalArgumentException("batch exceeds 4096"));ChunkKey key=iterator.next();chain=chain.thenCompose(v->generateChunk(key).thenAccept(consumer));}return chain;
  }
  private void settle(ChunkKey key,Query request,MaterializedChunk chunk,Throwable failure){
    // A shutdown decision made after cache admission but before this CAS wins.
    // Future completion stays outside the pipeline monitor (user callbacks can reenter).
    if(request.decision.compareAndSet(OPEN,SETTLING)){
      try {
        if(failure!=null)request.future.completeExceptionally(failure);
        else request.future.complete(chunk);
      } finally {synchronized(this){pending.remove(key,request);}}
    } else synchronized(this){pending.remove(key,request);}
  }
  public void shutdown(){
    List<Query> cancelled=new ArrayList<>();
    synchronized(this){
      stopped=true;
      for(Query request:pending.values())
        if(request.decision.compareAndSet(OPEN,CANCELLED))cancelled.add(request);
      pending.clear();cache.clear();
    }
    for(Query request:cancelled)
      request.future.completeExceptionally(new CancellationException("pipeline stopped"));
  }
  public synchronized int pendingCount(){return pending.size();}
}

