package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationCommitGate;
import com.shnewbs.deepprimal.core.density.V8ChunkGenerationStamp;
import net.minecraft.nbt.CompoundTag;
import java.util.Arrays;

public final class V8ChunkGenerationNativeCommitChecks {
  private static int checks;
  private static void ok(boolean condition) {checks++;if(!condition)throw new AssertionError("check "+checks);}
  private static void reject(Runnable run) {checks++;try{run.run();throw new AssertionError("accepted invalid input "+checks);}catch(IllegalStateException|IllegalArgumentException expected){}}
  private static final class Owner implements V8ChunkGenerationNativeCommit.Owner {
    long epoch; long[] neighbors={1,2,3,4}; boolean publish=true;int publishes;
    public V8ChunkGenerationCommitGate.Snapshot snapshot(){return new V8ChunkGenerationCommitGate.Snapshot(epoch,neighbors.clone());}
    public boolean compareAndPublish(V8ChunkGenerationCommitGate.Admission a,CompoundTag tag,long next){
      publishes++;
      if(!publish||epoch!=a.snapshot().targetEpoch()||!Arrays.equals(neighbors,a.snapshot().neighborEpochs())||
         !tag.has("marker")||next!=epoch+1)return false;
      epoch=next;return true;
    }
  }
  public static void main(String[] args){
    var stamp=new V8ChunkGenerationStamp.Stamp(6,-5,7,123);
    for(int i=0;i<2000;i++){
      Owner owner=new Owner();owner.epoch=i;
      var ticket=V8ChunkGenerationNativeCommit.begin(owner,stamp);
      CompoundTag tag=new CompoundTag();
      var result=V8ChunkGenerationNativeCommit.commit(ticket,owner,tag);
      ok(result.epoch()==i+1);ok(owner.epoch==i+1);ok(owner.publishes==1);ok(tag.has("marker"));
      reject(()->V8ChunkGenerationNativeCommit.commit(ticket,owner,new CompoundTag()));
      Owner other=new Owner();var another=V8ChunkGenerationNativeCommit.begin(owner,stamp);
      reject(()->V8ChunkGenerationNativeCommit.commit(another,other,new CompoundTag()));
      ok(V8ChunkGenerationNativeCommit.commit(another,owner,new CompoundTag()).epoch()==i+2);
      var changed=V8ChunkGenerationNativeCommit.begin(owner,stamp);owner.neighbors[0]++;
      CompoundTag unchanged=new CompoundTag();int count=owner.publishes;
      reject(()->V8ChunkGenerationNativeCommit.commit(changed,owner,unchanged));
      ok(!unchanged.has("marker"));ok(owner.publishes==count);
      var denied=V8ChunkGenerationNativeCommit.begin(owner,stamp);owner.publish=false;
      reject(()->V8ChunkGenerationNativeCommit.commit(denied,owner,new CompoundTag()));
      ok(owner.publishes==count+1);
      owner.publish=true;
      var preMarked=V8ChunkGenerationNativeCommit.begin(owner,stamp);
      CompoundTag pre=new CompoundTag();pre.putString("marker","existing");
      reject(()->V8ChunkGenerationNativeCommit.commit(preMarked,owner,pre));
      reject(()->V8ChunkGenerationNativeCommit.commit(preMarked,owner,new CompoundTag()));
      ok(owner.publishes==count+1);
    }
    Owner overflow=new Owner();overflow.epoch=Long.MAX_VALUE;
    var ticket=V8ChunkGenerationNativeCommit.begin(overflow,stamp);
    CompoundTag tag=new CompoundTag();
    reject(()->V8ChunkGenerationNativeCommit.commit(ticket,overflow,tag));
    ok(!tag.has("marker"));ok(overflow.publishes==0);
    ok(checks>=26000);
    System.out.println("V8_NATIVE_COMMIT_PASS assertions="+checks);
  }
}
