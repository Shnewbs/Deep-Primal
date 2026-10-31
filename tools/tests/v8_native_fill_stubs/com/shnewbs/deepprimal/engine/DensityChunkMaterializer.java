package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.density.DensityChunk;
import net.minecraft.world.level.chunk.ChunkAccess;
public final class DensityChunkMaterializer {
  private DensityChunkMaterializer(){}
  public static int calls;
  public static boolean fail,failWithError;
  public static DensityChunk.Planned fillSavedSnapshot(V8GeneratorSnapshot.Prepared p,long fingerprint,ChunkAccess c,int q){
    calls++;if(!V8ChunkGenerationNativeCommit.activeTicket)throw new IllegalStateException("write before ticket");
    if(failWithError)throw new AssertionError("native callback error");
    if(fail)throw new IllegalStateException("native setter failed");
    return new DensityChunk.Planned(q);
  }
  public record V8VegetatedReceiptResult(int realized){}
  public static V8VegetatedReceiptResult fillSavedEcotoneVegetatedWithReceipt(
      V8GeneratorSnapshot.Prepared p,long fingerprint,ChunkAccess c,int q,
      V8EcotoneColumnPlan.ClimateSource climate,V8WaterDomainAdmission.Authority authority){
    calls++;if(!V8ChunkGenerationNativeCommit.activeTicket)throw new IllegalStateException("write before ticket");
    if(failWithError)throw new AssertionError("native callback error");
    if(fail)throw new IllegalStateException("native plant setter failed");
    if(!authority.accept(c.getPos().x,c.getPos().z))throw new IllegalStateException("water rejected");
    return new V8VegetatedReceiptResult((int)climate.sample(c.getPos().x,c.getPos().z));
  }
}
