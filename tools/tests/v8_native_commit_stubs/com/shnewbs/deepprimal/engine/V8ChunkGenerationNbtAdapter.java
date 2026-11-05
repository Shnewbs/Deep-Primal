package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationStamp;
import net.minecraft.nbt.CompoundTag;
public final class V8ChunkGenerationNbtAdapter {
  public record Generated(V8ChunkGenerationStamp.Stamp stamp,long epoch){}
  public static void writeNewGenerated(CompoundTag nbt, Generated g){
    if(nbt.has("marker"))throw new IllegalStateException("already marked");
    nbt.putString("marker",g.stamp().toString()+"@"+g.epoch());
  }
}
