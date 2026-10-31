package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationStamp;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.ChunkAccess;
public final class V8ChunkGenerationNativeFillChecks {
  private static int checks;
  private static void ok(boolean b){checks++;if(!b)throw new AssertionError("check "+checks);}
  private static void reject(Runnable r){checks++;try{r.run();throw new AssertionError("accepted invalid input "+checks);}catch(IllegalArgumentException|IllegalStateException expected){}}
  private static final class Owner implements V8ChunkGenerationNativeCommit.Owner {
    boolean begin=true,commit=true;
    public boolean allowBegin(){return begin;}
    public boolean allowCommit(){return commit;}
  }
  public static void main(String[] args){
    for(int i=0;i<1000;i++){
      int x=(i%2==0?-1:1)*(i*31+3),z=(i%3==0?-1:1)*(i*17+9);
      var owner=new Owner();var chunk=new ChunkAccess(x,z,-64,384);
      var stamp=new V8ChunkGenerationStamp.Stamp(6,x,z,1234);
      var profile=new V8GeneratorSnapshot.Prepared(6,1234);
      var nbt=new CompoundTag();int[] discards={0};
      var result=V8ChunkGenerationNativeFill.fillSavedSnapshotAndCommit(profile,1234,owner,stamp,chunk,5,nbt,(c,n)->discards[0]++);
      ok(result.result().id()==5);ok(result.identity().epoch()==42);ok(discards[0]==0);
      ok(profile.identityChecks==1&&profile.heightChecks==1);ok(nbt.marker);
      var plants=V8ChunkGenerationNativeFill.fillSavedVegetationAndCommit(profile,1234,owner,stamp,chunk,4,new CompoundTag(),(c,n)->discards[0]++, (a,b)->7,(a,b)->true);
      ok(plants.result().realized()==7);ok(discards[0]==0);
      int before=DensityChunkMaterializer.calls;
      reject(()->V8ChunkGenerationNativeFill.fillSavedSnapshotAndCommit(profile,4321,owner,stamp,chunk,5,new CompoundTag(),(c,n)->discards[0]++));
      reject(()->V8ChunkGenerationNativeFill.fillSavedSnapshotAndCommit(profile,1234,owner,new V8ChunkGenerationStamp.Stamp(6,x+1,z,1234),chunk,5,new CompoundTag(),(c,n)->discards[0]++));
      reject(()->V8ChunkGenerationNativeFill.fillSavedSnapshotAndCommit(new V8GeneratorSnapshot.Prepared(1,1234),1234,owner,new V8ChunkGenerationStamp.Stamp(1,x,z,1234),chunk,5,new CompoundTag(),(c,n)->discards[0]++));
      reject(()->V8ChunkGenerationNativeFill.fillSavedVegetationAndCommit(new V8GeneratorSnapshot.Prepared(5,1234),1234,owner,new V8ChunkGenerationStamp.Stamp(5,x,z,1234),chunk,5,new CompoundTag(),(c,n)->discards[0]++, (a,b)->1,(a,b)->true));
      ok(DensityChunkMaterializer.calls==before);ok(discards[0]==0);
      owner.begin=false;
      reject(()->V8ChunkGenerationNativeFill.fillSavedSnapshotAndCommit(profile,1234,owner,stamp,chunk,5,new CompoundTag(),(c,n)->discards[0]++));
      ok(discards[0]==0);owner.begin=true;
      owner.commit=false;var failedNbt=new CompoundTag();
      reject(()->V8ChunkGenerationNativeFill.fillSavedSnapshotAndCommit(profile,1234,owner,stamp,chunk,5,failedNbt,(c,n)->discards[0]++));
      ok(failedNbt.marker);ok(discards[0]==1);owner.commit=true;
      DensityChunkMaterializer.fail=true;
      reject(()->V8ChunkGenerationNativeFill.fillSavedSnapshotAndCommit(profile,1234,owner,stamp,chunk,5,new CompoundTag(),(c,n)->discards[0]++));
      ok(discards[0]==2);DensityChunkMaterializer.fail=false;
      DensityChunkMaterializer.failWithError=true;
      try {
        V8ChunkGenerationNativeFill.fillSavedSnapshotAndCommit(profile,1234,owner,stamp,chunk,5,new CompoundTag(),(c,n)->discards[0]++);
        throw new AssertionError("accepted native error");
      } catch (AssertionError expected) {ok("native callback error".equals(expected.getMessage()));}
      ok(discards[0]==3);DensityChunkMaterializer.failWithError=false;
      owner.commit=false;
      try {
        V8ChunkGenerationNativeFill.fillSavedSnapshotAndCommit(profile,1234,owner,stamp,chunk,5,new CompoundTag(),(c,n)->{discards[0]++;throw new IllegalStateException("cleanup");});
        throw new AssertionError("accepted invalid input");
      } catch (IllegalStateException expected) {
        ok(expected.getSuppressed().length==1);
        ok("cleanup".equals(expected.getSuppressed()[0].getMessage()));
      }
      ok(discards[0]==4);owner.commit=true;
    }
    System.out.println("V8_NATIVE_FILL_PASS assertions="+checks);
  }
}
