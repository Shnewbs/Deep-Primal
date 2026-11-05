package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.WaterAccount;
import com.shnewbs.deepprimal.core.WaterAccount.Parcel;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class ContactCommitChecks {
  static int cases;
  static final BlockPos A=new BlockPos(15,64,15), B=new BlockPos(16,64,15);
  static void require(boolean yes,String message){if(!yes)throw new AssertionError(message);}
  static void put(ServerLevel l,BlockPos p,Parcel v,boolean ocean){
    l.states.put(p.asLong(),new BlockState((int)(v.water()/256),ocean));
    l.data.composition.put(p.asLong(),v);
  }
  static Parcel get(ServerLevel l,BlockPos p){return HydrologyRuntime.contents(l,p,l.getBlockState(p));}
  static boolean commit(ServerLevel l,BlockPos a,BlockPos b,Parcel pa,Parcel pb,Parcel qa,Parcel qb){
    return HydrologyRuntime.commitContact(l,a,b,l.states.get(a.asLong()),l.states.get(b.asLong()),pa,pb,qa,qb);
  }
  static void rejected(Parcel qa,Parcel qb,String label){
    var l=new ServerLevel();var pa=new Parcel(4096,512,0);var pb=new Parcel(4096,0,512);
    put(l,A,pa,false);put(l,B,pb,false);
    var metadata=new HashMap<>(l.data.composition);var states=new HashMap<>(l.states);
    require(!commit(l,A,B,pa,pb,qa,qb),label);
    require(metadata.equals(l.data.composition)&&states.equals(l.states)&&l.data.dirty==0,label+" side effect");cases++;
  }
  public static void main(String[] args){
    Random random=new Random(2900);
    for(int i=0;i<50000;i++){
      var l=new ServerLevel();var dir=Direction.values()[i%6];var b=A.relative(dir);
      int ua=1+random.nextInt(16),ub=1+random.nextInt(16);
      if(dir.y>0)ua=16;if(dir.y<0)ub=16;
      long saltA=random.nextInt(1000001),saltB=random.nextInt(1000001),mudA=random.nextInt(1000001),mudB=random.nextInt(1000001);
      var pa=new Parcel(ua*256L,saltA,mudA);var pb=new Parcel(ub*256L,saltB,mudB);
      long outSalt=random.nextLong(saltA+saltB+1),outMud=random.nextLong(mudA+mudB+1);
      var qa=new Parcel(pa.water(),outSalt,outMud);var qb=new Parcel(pb.water(),saltA+saltB-outSalt,mudA+mudB-outMud);
      put(l,A,pa,i%5==0);put(l,b,pb,i%7==0);var blocks=new HashMap<>(l.states);
      require(commit(l,A,b,pa,pb,qa,qb),"valid conservative contact");
      require(get(l,A).equals(qa)&&get(l,b).equals(qb),"exact committed parcels");
      require(l.states.equals(blocks)&&l.data.dirty==1,"metadata-only single dirty mark");
      require(!commit(l,A,b,pa,pb,qa,qb)&&l.data.dirty==1,"stale prepared replay rejected");cases++;
    }
    rejected(new Parcel(4097,256,256),new Parcel(4095,256,256),"global-volume-preserving cell shift rejected");
    rejected(new Parcel(4097,256,256),new Parcel(4096,256,256),"water creation");
    rejected(new Parcel(4095,256,256),new Parcel(4096,256,256),"water destruction");
    rejected(new Parcel(4096,257,256),new Parcel(4096,256,256),"salt creation");
    rejected(new Parcel(4096,255,256),new Parcel(4096,256,256),"salt destruction");
    rejected(new Parcel(4096,256,257),new Parcel(4096,256,256),"sediment creation");
    rejected(new Parcel(4096,256,255),new Parcel(4096,256,256),"sediment destruction");
    rejected(new Parcel(4096,512,0),new Parcel(4096,0,512),"no-op");
    var pa=new Parcel(4096,512,0);var pb=new Parcel(4096,0,512);var half=new Parcel(4096,256,256);
    for(var b:new BlockPos[]{A,new BlockPos(17,64,15),new BlockPos(16,65,15),new BlockPos(Integer.MIN_VALUE,64,15)}){
      var l=new ServerLevel();put(l,A,pa,false);put(l,b,pb,false);
      var before=new HashMap<>(l.data.composition);
      require(!commit(l,A,b,pa,pb,half,half)&&before.equals(l.data.composition)&&l.data.dirty==0,"non-contact / alias");cases++;
    }
    for(int i=0;i<2;i++){
      var l=new ServerLevel();put(l,A,pa,false);put(l,B,pb,false);l.unloaded.add((i==0?A:B).asLong());
      require(!commit(l,A,B,pa,pb,half,half)&&l.reads==0&&l.data.dirty==0,"unloaded without native read");cases++;
    }
    for(int lower:new int[]{1,8,15}){
      var l=new ServerLevel();var up=A.relative(Direction.UP);var partial=new Parcel(lower*256L,512,0);
      put(l,A,partial,false);put(l,up,pb,false);
      require(!commit(l,A,up,partial,pb,new Parcel(partial.water(),256,256),half)&&l.data.dirty==0,"vertical air gap");cases++;
    }
    var l=new ServerLevel();put(l,A,pa,false);put(l,B,pb,false);var sa=l.states.get(A.asLong());var sb=l.states.get(B.asLong());
    l.states.put(A.asLong(),new BlockState(0,false));
    require(!HydrologyRuntime.commitContact(l,A,B,sa,sb,pa,pb,half,half)&&l.data.dirty==0,"stale block state");cases++;
    put(l,A,new Parcel(4096,511,0),false);
    require(!HydrologyRuntime.commitContact(l,A,B,sa,sb,pa,pb,half,half)&&l.data.dirty==0,"stale composition");cases++;
    l.thread=false;
    try{HydrologyRuntime.commitContact(l,A,B,sa,sb,pa,pb,half,half);throw new AssertionError("wrong thread");}
    catch(IllegalStateException expected){cases++;}
    // Both upper constituent bounds remain exact, with no long addition overflow.
    l=new ServerLevel();long max=WaterAccount.MAX*1024;pa=new Parcel(4096,max,max);pb=new Parcel(4096,0,0);
    put(l,A,pa,false);put(l,B,pb,true);
    var qa=new Parcel(4096,0,0);var qb=new Parcel(4096,max,max);
    require(commit(l,A,B,pa,pb,qa,qb)&&get(l,A).equals(qa)&&get(l,B).equals(qb),"maximum constituents");
    require(!l.data.composition.containsKey(A.asLong()),"fresh empty composition compacted");
    require(commit(l,A,B,qa,qb,pa,pb)&&l.data.composition.containsKey(B.asLong())&&get(l,B).equals(pb),"zero boundary override retained");cases++;
    System.out.println("CONTACT_COMMIT_PASS cases="+cases+" exactMethodExtraction=true nativeDoubles=true");
  }
}
