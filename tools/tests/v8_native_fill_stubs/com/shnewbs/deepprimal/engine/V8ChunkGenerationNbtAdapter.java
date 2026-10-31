package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationStamp;
public final class V8ChunkGenerationNbtAdapter {
  private V8ChunkGenerationNbtAdapter(){}
  public record Generated(V8ChunkGenerationStamp.Stamp stamp,long epoch){}
}
