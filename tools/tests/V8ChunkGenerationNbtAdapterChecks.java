package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationStamp;
import com.shnewbs.deepprimal.core.density.V8GenerationBorderAdmission;
import java.util.List;
import java.util.SplittableRandom;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class V8ChunkGenerationNbtAdapterChecks {
  private static long checks;
  private static void check(boolean condition) {
    checks++;
    if(!condition)throw new AssertionError("check "+checks);
  }
  private static void reject(Runnable action) {
    checks++;
    try {action.run();throw new AssertionError("accepted invalid input "+checks);}
    catch(IllegalArgumentException|IllegalStateException expected) {}
  }
  public static void main(String[] args) {
    SplittableRandom rng=new SplittableRandom(0xD119L);
    for(int i=0;i<5000;i++) {
      int x=rng.nextInt(),z=rng.nextInt(),revision=rng.nextInt(2,7);
      long fingerprint=rng.nextLong()|1L,epoch=Math.max(1L,rng.nextLong(0,Long.MAX_VALUE));
      var stamp=new V8ChunkGenerationStamp.Stamp(revision,x,z,fingerprint);
      var identity=new V8ChunkGenerationNbtAdapter.Generated(stamp,epoch);
      CompoundTag tag=new CompoundTag();
      V8ChunkGenerationNbtAdapter.requireFreshCandidate(tag);
      check(tag.get(V8ChunkGenerationNbtAdapter.KEY)==null);
      reject(()->V8ChunkGenerationNbtAdapter.readGenerated(tag));
      V8ChunkGenerationNbtAdapter.writeNewGenerated(tag,identity);
      reject(()->V8ChunkGenerationNbtAdapter.requireFreshCandidate(tag));
      check(V8ChunkGenerationNbtAdapter.readGenerated(tag).equals(identity));
      check(V8ChunkGenerationNbtAdapter.requireGenerated(tag,x,z,revision,fingerprint,epoch).equals(identity));
      reject(()->V8ChunkGenerationNbtAdapter.writeNewGenerated(tag,identity));
      reject(()->V8ChunkGenerationNbtAdapter.requireGenerated(tag,x+1,z,revision,fingerprint,epoch));
      reject(()->V8ChunkGenerationNbtAdapter.requireGenerated(tag,x,z+1,revision,fingerprint,epoch));
      reject(()->V8ChunkGenerationNbtAdapter.requireGenerated(tag,x,z,revision,fingerprint^2L,epoch));
      reject(()->V8ChunkGenerationNbtAdapter.requireGenerated(tag,x,z,revision,fingerprint,epoch+1));
      var direction=V8GenerationBorderAdmission.Direction.NORTH;
      var neighbor=V8ChunkGenerationNbtAdapter.neighbor(direction,V8GenerationBorderAdmission.State.GENERATED,
          tag,epoch,x,z,revision,fingerprint);
      check(neighbor.persistedStamp().equals(stamp.encode()));
      check(neighbor.epoch()==epoch);
      reject(()->V8ChunkGenerationNbtAdapter.neighbor(direction,V8GenerationBorderAdmission.State.UNGENERATED,
          tag,epoch,x,z,revision,fingerprint));
      reject(()->V8ChunkGenerationNbtAdapter.neighbor(direction,V8GenerationBorderAdmission.State.UNKNOWN,
          tag,epoch,x,z,revision,fingerprint));
      reject(()->V8ChunkGenerationNbtAdapter.ungeneratedTarget(
          V8GenerationBorderAdmission.State.UNGENERATED,tag,epoch,List.of(neighbor)));
      String valid=((StringTag)tag.get(V8ChunkGenerationNbtAdapter.KEY)).getAsString();
      CompoundTag corrupt=new CompoundTag();
      corrupt.putString(V8ChunkGenerationNbtAdapter.KEY,valid.substring(0,valid.length()-1)+"f");
      if(!valid.endsWith("f"))reject(()->V8ChunkGenerationNbtAdapter.readGenerated(corrupt));
      CompoundTag missing=new CompoundTag();
      var empty=V8ChunkGenerationNbtAdapter.neighbor(direction,V8GenerationBorderAdmission.State.UNGENERATED,
          missing,epoch,x,z,revision,fingerprint);
      check(empty.persistedStamp()==null);
      check(V8ChunkGenerationNbtAdapter.ungeneratedTarget(
          V8GenerationBorderAdmission.State.UNGENERATED,missing,epoch,List.of(empty)).targetEpoch()==epoch);
      reject(()->V8ChunkGenerationNbtAdapter.neighbor(direction,V8GenerationBorderAdmission.State.GENERATED,
          missing,epoch,x,z,revision,fingerprint));
    }
    CompoundTag wrongType=new CompoundTag();
    wrongType.put(V8ChunkGenerationNbtAdapter.KEY,new Tag(){});
    reject(()->V8ChunkGenerationNbtAdapter.readGenerated(wrongType));
    CompoundTag malformed=new CompoundTag();
    for(String text:List.of("", "dp-v8-nbt/2|0|junk", "dp-v8-nbt/1|01|junk", "x".repeat(161),
        "dp-v8-nbt/1|-1|junk", "dp-v8-nbt/1|9223372036854775808|junk")) {
      malformed.putString(V8ChunkGenerationNbtAdapter.KEY,text);
      reject(()->V8ChunkGenerationNbtAdapter.readGenerated(malformed));
    }
    var zeroStamp = new V8ChunkGenerationStamp.Stamp(6, -123, 456, 1234L);
    reject(()->new V8ChunkGenerationNbtAdapter.Generated(zeroStamp, -1));
    // The native coordinator publishes nextEpoch=targetEpoch+1, so a
    // GENERATED marker at epoch zero was never a committed chunk.
    reject(()->new V8ChunkGenerationNbtAdapter.Generated(zeroStamp, 0));
    String zeroBody = "dp-v8-nbt/1|0|" + zeroStamp.encode();
    var crc = new java.util.zip.CRC32();
    crc.update(zeroBody.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    CompoundTag zeroNbt = new CompoundTag();
    zeroNbt.putString(V8ChunkGenerationNbtAdapter.KEY, zeroBody + "|"
        + String.format(java.util.Locale.ROOT, "%08x", crc.getValue()));
    reject(()->V8ChunkGenerationNbtAdapter.readGenerated(zeroNbt));
    reject(()->V8ChunkGenerationNbtAdapter.neighbor(V8GenerationBorderAdmission.Direction.NORTH,
        V8GenerationBorderAdmission.State.GENERATED, zeroNbt, 0,
        zeroStamp.chunkX(), zeroStamp.chunkZ(), zeroStamp.revision(), zeroStamp.profileFingerprint()));
    check(checks>70000);
    System.out.println("V8_NBT_ADAPTER_PASS assertions="+checks);
  }
}
