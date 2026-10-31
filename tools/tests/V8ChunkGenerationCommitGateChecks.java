package com.shnewbs.deepprimal.core.density;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;
import static com.shnewbs.deepprimal.core.density.V8GenerationBorderAdmission.Direction.*;
import static com.shnewbs.deepprimal.core.density.V8GenerationBorderAdmission.State.*;
public final class V8ChunkGenerationCommitGateChecks {
  private static long assertions;
  private static void check(boolean b){assertions++;if(!b)throw new AssertionError("assertion "+assertions);}
  private static void reject(Runnable r){assertions++;try{r.run();throw new AssertionError("accepted invalid input "+assertions);}catch(IllegalArgumentException|IllegalStateException expected){}}
  private static List<V8ChunkGenerationCommitGate.VersionedNeighbor> neighbors(long epoch){
    return List.of(new V8ChunkGenerationCommitGate.VersionedNeighbor(NORTH,UNGENERATED,null,epoch),
      new V8ChunkGenerationCommitGate.VersionedNeighbor(EAST,UNGENERATED,null,epoch),
      new V8ChunkGenerationCommitGate.VersionedNeighbor(SOUTH,UNGENERATED,null,epoch),
      new V8ChunkGenerationCommitGate.VersionedNeighbor(WEST,UNGENERATED,null,epoch));
  }
  private static V8ChunkGenerationCommitGate.Snapshot snap(long epoch,List<V8ChunkGenerationCommitGate.VersionedNeighbor> n){
    return new V8ChunkGenerationCommitGate.Snapshot(UNGENERATED,null,epoch,n);
  }
  public static void main(String[] args){
    var random=new SplittableRandom(0xD3118L);
    for(int i=0;i<5000;i++){
      int x=random.nextInt(-1000000,1000000),z=random.nextInt(-1000000,1000000);
      int revision=random.nextInt(2,7);long fingerprint=random.nextLong()|1L;
      var intended=new V8ChunkGenerationStamp.Stamp(revision,x,z,fingerprint);
      long epoch=random.nextLong(0,Long.MAX_VALUE-10);
      var before=snap(epoch,neighbors(epoch));
      var admission=V8ChunkGenerationCommitGate.prepare(intended,before);
      check(admission.intended().equals(intended));
      check(admission.snapshot().equals(before));
      check(admission.encodedStamp().equals(intended.encode()));
      var reordered=new ArrayList<>(before.neighbors());Collections.reverse(reordered);
      V8ChunkGenerationCommitGate.requireUnchanged(admission,snap(epoch,reordered));check(true);
      reject(()->V8ChunkGenerationCommitGate.requireUnchanged(admission,snap(epoch+1,neighbors(epoch))));
      for(int j=0;j<4;j++){
        var changed=new ArrayList<>(before.neighbors());
        var old=changed.get(j);
        changed.set(j,new V8ChunkGenerationCommitGate.VersionedNeighbor(old.direction(),old.state(),old.persistedStamp(),epoch+1));
        reject(()->V8ChunkGenerationCommitGate.requireUnchanged(admission,snap(epoch,changed)));
      }
      reject(()->V8ChunkGenerationCommitGate.requirePublished(admission,a->false));
      V8ChunkGenerationCommitGate.requirePublished(admission,a->{check(a==admission);return true;});
      check(true);
    }
    var intended=new V8ChunkGenerationStamp.Stamp(6,0,0,1);
    var valid=V8ChunkGenerationCommitGate.prepare(intended,snap(2,neighbors(3)));
    reject(()->V8ChunkGenerationCommitGate.prepare(intended,new V8ChunkGenerationCommitGate.Snapshot(UNKNOWN,null,2,neighbors(3))));
    reject(()->V8ChunkGenerationCommitGate.prepare(intended,new V8ChunkGenerationCommitGate.Snapshot(GENERATED,intended.encode(),2,neighbors(3))));
    reject(()->V8ChunkGenerationCommitGate.prepare(intended,snap(2,List.of())));
    reject(()->V8ChunkGenerationCommitGate.prepare(intended,snap(2,List.of(neighbors(3).get(0),neighbors(3).get(0),neighbors(3).get(2),neighbors(3).get(3)))));
    reject(()->V8ChunkGenerationCommitGate.prepare(intended,snap(2,List.of(new V8ChunkGenerationCommitGate.VersionedNeighbor(NORTH,UNKNOWN,null,3),neighbors(3).get(1),neighbors(3).get(2),neighbors(3).get(3)))));
    reject(()->new V8ChunkGenerationCommitGate.VersionedNeighbor(NORTH,UNGENERATED,null,-1));
    reject(()->new V8ChunkGenerationCommitGate.Snapshot(UNGENERATED,null,-1,neighbors(3)));
    var changed=new ArrayList<>(neighbors(3));changed.set(0,new V8ChunkGenerationCommitGate.VersionedNeighbor(NORTH,GENERATED,new V8ChunkGenerationStamp.Stamp(6,0,-1,1).encode(),3));
    reject(()->V8ChunkGenerationCommitGate.requireUnchanged(valid,snap(2,changed)));
    check(assertions>50000);
    System.out.println("V8_COMMIT_GATE_PASS assertions="+assertions);
  }
}
