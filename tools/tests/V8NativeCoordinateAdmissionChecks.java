package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationStamp;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.ChunkAccess;
public final class V8NativeCoordinateAdmissionChecks {
  private static int checks;
  private static final class Owner implements V8ChunkGenerationNativeCommit.Owner {
    public boolean allowBegin(){return true;}
    public boolean allowCommit(){return true;}
  }
  private static void check(int x, int z) {
    boolean valid = ((long)x*16 >= Integer.MIN_VALUE && (long)x*16+15 <= Integer.MAX_VALUE
        && (long)z*16 >= Integer.MIN_VALUE && (long)z*16+15 <= Integer.MAX_VALUE);
    var owner = new Owner();
    var chunk = new ChunkAccess(x,z,-64,384);
    var stamp = new V8ChunkGenerationStamp.Stamp(6,x,z,1234);
    var profile = new V8GeneratorSnapshot.Prepared(6,1234);
    int begins = V8ChunkGenerationNativeCommit.begins;
    int writes = DensityChunkMaterializer.calls;
    int[] discards = {0};
    boolean terrainAccepted = false;
    try {
      var published = V8ChunkGenerationNativeFill.fillSavedSnapshotAndCommit(
          profile,1234,owner,stamp,chunk,5,new CompoundTag(),(c,n)->discards[0]++);
      terrainAccepted = true;
      if(published.result().id()!=5 || published.identity().epoch()!=42)
        throw new AssertionError("terrain publication");
    } catch (IllegalArgumentException expected) { }
    if(terrainAccepted!=valid)throw new AssertionError("terrain range x="+x+" z="+z);
    checks++;
    boolean vegetationAccepted = false;
    try {
      var published = V8ChunkGenerationNativeFill.fillSavedVegetationAndCommit(
          profile,1234,owner,stamp,chunk,4,new CompoundTag(),(c,n)->discards[0]++,
          (a,b)->7,(a,b)->true);
      vegetationAccepted = true;
      if(published.result().realized()!=7)throw new AssertionError("vegetation publication");
    } catch (IllegalArgumentException expected) { }
    if(vegetationAccepted!=valid)throw new AssertionError("vegetation range x="+x+" z="+z);
    checks++;
    if(V8ChunkGenerationNativeCommit.begins-begins != (valid?2:0))
      throw new AssertionError("owner entered on invalid");
    if(DensityChunkMaterializer.calls-writes != (valid?2:0))
      throw new AssertionError("native setter entered on invalid");
    if(discards[0]!=0)throw new AssertionError("unexpected discard");
    checks+=3;
  }
  public static void main(String[] args) {
    int[] edges={Integer.MIN_VALUE,-134217729,-134217728,-134217727,-1,0,1,
        134217726,134217727,134217728,Integer.MAX_VALUE};
    for(int x:edges)for(int z:edges)check(x,z);
    java.util.Random rng=new java.util.Random(20261031L);
    for(int i=0;i<20000;i++)check(rng.nextInt(),rng.nextInt());
    System.out.println("V8_NATIVE_COORDINATE_PASS assertions="+checks);
  }
}
