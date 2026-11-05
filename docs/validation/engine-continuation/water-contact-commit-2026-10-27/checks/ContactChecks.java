package com.shnewbs.deepprimal.engine;
import com.shnewbs.deepprimal.core.WaterAccount;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

public class ContactChecks {
  static int cases;
  static final BlockPos A=new BlockPos(15,64,15);
  static void check(boolean yes,String label){if(!yes)throw new AssertionError(label);}
  static void put(ServerLevel l,BlockPos p,int units,long salt,long sediment,boolean ocean){
    l.states.put(p.asLong(),new BlockState(units,ocean));
    l.data.composition.put(p.asLong(),new WaterAccount.Parcel(units*256L,salt,sediment));
  }
  static WaterAccount.Parcel get(ServerLevel l,BlockPos p){return HydrologyRuntime.contents(l,p,l.getBlockState(p));}
  static void pair(Direction d,int ua,int ub,long salt,long mud) {
    var l=new ServerLevel(); var b=A.relative(d);
    put(l,A,ua,salt,0,false);put(l,b,ub,0,mud,false);
    var beforeA=get(l,A);var beforeB=get(l,b);var blocks=new HashMap<>(l.states);
    WaterContactRuntime.mix(l,A);
    var aa=get(l,A);var bb=get(l,b);
    check(aa.water()==beforeA.water() && bb.water()==beforeB.water(),"individual volume");
    check(aa.salt()+bb.salt()==salt && aa.sediment()+bb.sediment()==mud,"mass");
    check(l.states.equals(blocks),"no block writes");
    boolean contact=d.y==0 || (d.y>0?ua:ub)==16;
    var ca=new WaterAccount(4096,beforeA);var cb=new WaterAccount(4096,beforeB);
    if(contact)ca.diffuseWith(cb,256,256);
    check(aa.equals(ca.snapshot()) && bb.equals(cb.snapshot()),"contact oracle");
    WaterContactRuntime.mix(l,b);WaterContactRuntime.mix(l,A);
    check(get(l,A).equals(aa)&&get(l,b).equals(bb),"pair once per tick");
    WaterContactRuntime.forget(l);cases++;
  }
  public static void main(String[] args){
    Random r=new Random(2600);
    for(Direction d:Direction.values())for(int a=1;a<=16;a++)for(int b=1;b<=16;b++)
      for(int n=0;n<8;n++)pair(d,a,b,r.nextInt(100000),r.nextInt(100000));
    var l=new ServerLevel();var b=A.relative(Direction.EAST);
    put(l,A,16,0,0,false);put(l,b,16,10000,10000,false);
    l.unloaded.add(b.asLong());WaterContactRuntime.mix(l,A);
    check(l.data.dirty==0 && l.data.composition.get(b.asLong()).salt()==10000,"sealed border");cases++;
    l.unloaded.clear();l.time++;WaterContactRuntime.mix(l,A);check(get(l,A).salt()==256,"loaded next tick");cases++;
    l.time++;WaterContactRuntime.mix(l,b);check(get(l,A).salt()==512,"new tick permits next bounded step");cases++;
    var before=new HashMap<>(l.data.composition);l.thread=false;
    try{WaterContactRuntime.mix(l,A);throw new AssertionError("wrong thread accepted");}catch(IllegalStateException expected){}
    check(l.data.composition.equals(before),"wrong thread unchanged");l.thread=true;cases++;
    var copy=new ServerLevel();copy.states.putAll(l.states);copy.data.composition.putAll(l.data.composition);
    WaterContactRuntime.forget(l);l.time=copy.time=100;
    WaterContactRuntime.mix(l,A);WaterContactRuntime.mix(copy,A);
    check(l.data.composition.equals(copy.data.composition),"snapshot reconstruction");cases++;
    // Explicit fresh boundary composition must not disappear and recreate implicit salt.
    var ocean=new ServerLevel();put(ocean,A,16,0,0,true);put(ocean,b,16,2,0,false);
    WaterContactRuntime.mix(ocean,A);check(get(ocean,A).salt()==1 && get(ocean,b).salt()==1,"boundary override");cases++;
    var freshOcean=new ServerLevel();put(freshOcean,A,16,0,0,true);put(freshOcean,b,16,0,1000,false);
    WaterContactRuntime.mix(freshOcean,A);
    check(freshOcean.data.composition.containsKey(A.asLong()) && get(freshOcean,A).salt()==0,"zero-salt boundary retained");cases++;
    pair(Direction.EAST,1,16,WaterAccount.MAX*1024,WaterAccount.MAX*1024);
    var clock=new ServerLevel();put(clock,A,16,10000,0,false);put(clock,b,16,0,0,false);
    clock.time=Long.MAX_VALUE;WaterContactRuntime.mix(clock,A);
    clock.time=Long.MIN_VALUE;WaterContactRuntime.mix(clock,b);
    check(get(clock,b).salt()==512,"clock wrap one interval no catch-up");cases++;
    // Complete dry-neighbor scans remain bounded; retries use only the existing owner queue.
    var busy=new ServerLevel();put(busy,A,16,0,0,false);
    for(int n=0;n<100000;n++)WaterContactRuntime.mix(busy,A);
    check(WaterContactRuntime.visits(busy)==2048,"visit cap");
    check(WaterContactRuntime.retainedEdges(busy)<=2048 && busy.active.size()==1,"residency/retry cap");
    check(busy.reads<=4096,"world reads bounded");cases++;
    // A player edit during the read phase invalidates the pair before metadata is written.
    var edited=new ServerLevel();put(edited,A,16,1000,0,false);put(edited,b,16,0,0,false);
    HydrologyRuntime.onContents=()->edited.states.put(A.asLong(),new BlockState(0,false));
    WaterContactRuntime.mix(edited,A);check(edited.data.dirty==0,"stale volume rejected");cases++;
    var metadata=new ServerLevel();put(metadata,A,16,1000,0,false);put(metadata,b,16,0,0,false);
    // Schedule edit on the first revalidation block read, after both parcels were sampled.
    HydrologyRuntime.onContents=()->metadata.onRead=()->metadata.data.composition.put(A.asLong(),new WaterAccount.Parcel(4096,999,0));
    WaterContactRuntime.mix(metadata,A);
    check(metadata.data.dirty==0 && get(metadata,A).salt()==999 && get(metadata,b).salt()==0,"stale metadata rejected");cases++;
    var wet=new ServerLevel();
    for(int n=0;n<1000;n++){
      var p=new BlockPos(n*4,64,0);put(wet,p,16,10000,0,false);
      for(Direction d:Direction.values())put(wet,p.relative(d),16,0,0,false);
      WaterContactRuntime.mix(wet,p);
    }
    check(WaterContactRuntime.visits(wet)==2048 && WaterContactRuntime.retainedEdges(wet)==2048,"wet edge cap");cases++;
    WaterContactRuntime.clear();check(WaterContactRuntime.visits(busy)==0,"shutdown cleanup");cases++;
    System.out.println("CONTACT_CHECKS_PASS cases="+cases+" random_pair_cases=12288 dry_reentries=100000 actual_native=false");
  }
}
