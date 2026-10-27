package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.WaterAccount.Parcel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
public final class ContactUnloadChecks {
  static final BlockPos A=new BlockPos(15,64,15),B=new BlockPos(16,64,15);
  static ServerLevel level(){
    var l=new ServerLevel();l.time=5;l.states.put(A.asLong(),new BlockState(16,false));l.states.put(B.asLong(),new BlockState(16,false));
    l.data.composition.put(A.asLong(),new Parcel(4096,512,0));l.data.composition.put(B.asLong(),new Parcel(4096,0,512));return l;
  }
  public static void main(String[] args){
    int cases=0;
    for(int at=0;at<3;at++){
      var l=level();var before=new java.util.HashMap<>(l.data.composition);
      if(at==0)l.onRead=()->l.unloaded.add(B.asLong());
      else l.onRead=()->l.onRead=()->l.unloaded.add(A.asLong());
      if(at<2)HydrologyRuntime.commitContact(l,A,B,l.states.get(A.asLong()),l.states.get(B.asLong()),before.get(A.asLong()),before.get(B.asLong()),new Parcel(4096,256,256),new Parcel(4096,256,256));
      else WaterContactRuntime.mix(l,A);
      if(l.data.dirty!=0||!before.equals(l.data.composition))throw new AssertionError("retired cell commit");cases++;
    }
    var l=level();l.onRead=()->l.unloaded.add(B.asLong());WaterContactRuntime.mix(l,A);
    if(l.data.dirty!=0)throw new AssertionError("adapter unloaded neighbor");cases++;
    System.out.println("CONTACT_UNLOAD_PASS cases="+cases+" noUnloadedRead=true noMetadataMutation=true");
  }
}
