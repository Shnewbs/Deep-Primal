package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationStamp;
public final class V8ChunkGenerationNbtAdapter {
  private V8ChunkGenerationNbtAdapter(){}
  public static final String KEY="deepprimal_v8_generation";
  public static void requireFreshCandidate(net.minecraft.nbt.CompoundTag nbt) {
    if(nbt.get(KEY)!=null)throw new IllegalStateException("existing V8 generation marker");
  }
  public record Generated(V8ChunkGenerationStamp.Stamp stamp,long epoch){}
}
