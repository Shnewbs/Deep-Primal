package com.shnewbs.deepprimal.engine;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.state.BlockState;

/** Source-only exact production admission with clearly labeled Minecraft API doubles. */
public final class V8VegetationNativeAdmissionChecks {
  private static final BlockState AIR=new BlockState("air",true,false,true);
  private static final BlockState GRASS=new BlockState("grass",false,true,true);
  private static final BlockState GRASS_EQUAL=new BlockState("grass",false,true,true);
  private static final BlockState ROCK=new BlockState("rock",false,true,true);
  private static final BlockState SOFT=new BlockState("grass",false,false,true);
  private static final BlockState SATURATED=new BlockState("grass",false,true,false);
  private static final BlockState WATER=new BlockState("water",false,false,false);
  private static final BlockState ODD_AIR=new BlockState("air",true,false,false);
  private static long assertions;
  private static void check(boolean result,String message){assertions++;if(!result)throw new AssertionError(message);}
  private static void one(int x,int y,int z,BlockState support,BlockState destination,
      BlockState expected,boolean allow){
    ChunkAccess chunk=new ChunkAccess(AIR);
    chunk.set(x,y-1,z,support);
    chunk.set(x,y,z,destination);
    var pos=new BlockPos.MutableBlockPos();
    long before=chunk.reads();
    boolean accepted=V8VegetationNativeAdmission.permits(chunk,pos,x,y,z,expected);
    check(accepted==allow,"wrong plant admission at "+x+","+y+","+z);
    check(chunk.reads()>before || expected.isAir(),"must inspect live native state unless absent support");
    check(chunk.getBlockState(new BlockPos.MutableBlockPos().set(x,y,z)).equals(destination),
        "admission must not modify native destination");
    check(chunk.getBlockState(new BlockPos.MutableBlockPos().set(x,y-1,z)).equals(support),
        "admission must not modify native support");
  }
  public static void main(String[] args){
    int[] coords={-30_000_000,-1025,-257,-33,-17,-1,0,1,15,16,31,512,30_000_000};
    for(int x:coords)for(int z:coords){
      one(x,96,z,GRASS,AIR,GRASS_EQUAL,true);
      one(x,-62,z,GRASS,ROCK,GRASS,false);
      one(x,17,z,ROCK,AIR,GRASS,false);
      one(x,106,z,SOFT,AIR,SOFT,false);
      one(x,108,z,SATURATED,AIR,SATURATED,false);
      one(x,112,z,GRASS,WATER,GRASS,false);
      one(x,115,z,GRASS,ODD_AIR,GRASS,false);
      one(x,121,z,AIR,AIR,AIR,false);
    }
    Random random=new Random(8720261029L);
    for(int n=0;n<5000;n++){
      int x=random.nextInt(100000)-50000,z=random.nextInt(100000)-50000;
      boolean same=random.nextBoolean(), sturdy=random.nextBoolean(),dry=random.nextBoolean();
      boolean free=random.nextBoolean();
      var actual=new BlockState(same?"grass":"rock",false,sturdy,dry);
      var expected=new BlockState("grass",false,sturdy,dry);
      one(x,64,z,actual,free?AIR:WATER,expected,same&&sturdy&&dry&&free);
    }
    try { V8VegetationNativeAdmission.permits(new ChunkAccess(AIR),
        new BlockPos.MutableBlockPos(),0,1,0,null);
      throw new AssertionError("null planned support accepted");
    } catch (NullPointerException expected) { assertions++; }
    System.out.println("V8_NATIVE_PLANT_SUPPORT_PASS assertions="+assertions+
        " signed=true drySupport=true unobstructed=true immutable=true native=false");
  }
}
