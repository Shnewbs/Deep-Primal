package com.shnewbs.deepprimal.core.density;
import java.util.List;
import static com.shnewbs.deepprimal.core.density.V8GenerationBorderAdmission.Direction.*;
import static com.shnewbs.deepprimal.core.density.V8GenerationBorderAdmission.State.*;
public final class GeneratedEpochChecks {
  private static int assertions;
  private static void ok(boolean value) {assertions++;if(!value)throw new AssertionError("failed "+assertions);}
  private static void reject(Runnable action) {assertions++;try{action.run();throw new AssertionError("accepted invalid input "+assertions);}catch(IllegalArgumentException expected){}}
  public static void main(String[] args) {
    for(int i=0;i<20000;i++) {
      long epoch=i;
      reject(()->new V8ChunkGenerationCommitGate.VersionedNeighbor(NORTH,GENERATED,"valid",0));
      reject(()->new V8ChunkGenerationCommitGate.Snapshot(GENERATED,"valid",0,List.of()));
      var ungenerated=new V8ChunkGenerationCommitGate.VersionedNeighbor(NORTH,UNGENERATED,null,epoch);
      ok(ungenerated.epoch()==epoch);
      var generated=new V8ChunkGenerationCommitGate.VersionedNeighbor(EAST,GENERATED,"valid",epoch+1);
      ok(generated.epoch()==epoch+1);
      var target=new V8ChunkGenerationCommitGate.Snapshot(UNGENERATED,null,epoch,List.of());
      ok(target.targetEpoch()==epoch);
      var committed=new V8ChunkGenerationCommitGate.Snapshot(GENERATED,"valid",epoch+1,List.of());
      ok(committed.targetEpoch()==epoch+1);
    }
    System.out.println("V8_GENERATED_EPOCH_PASS assertions="+assertions);
  }
}
