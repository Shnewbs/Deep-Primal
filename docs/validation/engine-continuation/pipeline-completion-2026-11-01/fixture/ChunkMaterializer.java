package com.shnewbs.deepprimal.core;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/** Isolated test double; never compile alongside production ChunkMaterializer. */
public class ChunkMaterializer {
  public record ChunkKey(int chunkX, int chunkZ, String dimension) {}
  public record MaterializedChunk(ChunkKey key, long bytes) {
    public long payloadBytes() { return bytes; }
  }
  private final Function<ChunkKey, CompletableFuture<MaterializedChunk>> handler;
  public ChunkMaterializer(Function<ChunkKey, CompletableFuture<MaterializedChunk>> handler) {
    this.handler = handler;
  }
  CompletableFuture<MaterializedChunk> materializeAsync(ChunkKey key, BooleanSupplier cancelled) {
    return handler.apply(key);
  }
}
