package com.shnewbs.deepprimal.core;

import java.util.*;

/** Independent absolute-coordinate corner intersection oracle, stable order and storage bounds. */
final class WaterBoundaryIndexChecks {
  private static void need(boolean pass,String why) { if(!pass)throw new AssertionError(why); }
  private static int oracle(int entry,int dx,int dz,int origin) {
    int mask=0,x=origin+(entry&15),z=-origin+((entry>>>4)&15);
    int lowX=origin+dx*16,lowZ=-origin+dz*16;
    for(int n=0;n<4;n++) {
      int px=x+(n&1),pz=z+(n>>1);
      if(px>=lowX&&px<=lowX+16&&pz>=lowZ&&pz<=lowZ+16)mask|=1<<n;
    }
    return mask;
  }
  static void run() {
    Random random=new Random(0x421023);long cases=0;int[] origins={-29999968,-16,0,16,29999968};
    for(int origin:origins)for(int cell=0;cell<4096;cell++)for(int dz=-1;dz<=1;dz++)for(int dx=-1;dx<=1;dx++) {
      if(dx==0&&dz==0)continue;
      need(WaterBoundaryIndex.mask((12345<<12)|cell,dx,dz)==oracle(cell,dx,dz,origin),"Shared-corner mask differs");cases++;
    }
    int[] full=new int[960];int count=0;
    for(int cell=0;cell<4096;cell++)if((cell&15)==0||(cell&15)==15||((cell>>>4)&15)==0||((cell>>>4)&15)==15)full[count]=(count++<<12)|cell;
    need(count==960,"Full boundary fixture differs");
    long oldVisits=0,newVisits=0;
    var fullIndex=WaterBoundaryIndex.of(full);
    for(int dz=-1;dz<=1;dz++)for(int dx=-1;dx<=1;dx++)if(dx!=0||dz!=0) {
      oldVisits+=full.length;newVisits+=fullIndex.end(dx,dz)-fullIndex.begin(dx,dz);
    }
    need(oldVisits==7680&&newVisits==1088,"Boundary scan reproduction differs");
    for(int trial=0;trial<1000;trial++) {
      int[] input=trial==0?new int[0]:trial==1?full:random.ints(random.nextInt(4097),0,4096).toArray();
      if(trial>1)for(int i=0;i<input.length;i++)input[i]|=i<<12;
      int[] before=input.clone();var index=WaterBoundaryIndex.of(input);
      need(index.retainedBytes()<=40+12*input.length,"Index storage exceeded three entries per input");
      for(int dz=-1;dz<=1;dz++)for(int dx=-1;dx<=1;dx++)if(dx!=0||dz!=0) {
        int position=index.begin(dx,dz),end=index.end(dx,dz);need(position>=0&&end>=position,"Invalid range");
        for(int entry:input)if(oracle(entry,dx,dz,origins[trial%origins.length])!=0) {
          need(position<end&&index.entry(position++)==entry,"Missing, reordered or extra quad");cases++;
        }
        need(position==end,"Unexpected indexed quad");
      }
      need(Arrays.equals(input,before),"Index construction mutated caller input");
      Arrays.fill(input,0);
      for(int dz=-1;dz<=1;dz++)for(int dx=-1;dx<=1;dx++)if(dx!=0||dz!=0) {
        int position=index.begin(dx,dz);
        for(int entry:before)if(oracle(entry,dx,dz,0)!=0)need(index.entry(position++)==entry,"Index aliases caller array");
      }
    }
    int rejected=0;for(int[] delta:new int[][]{{0,0},{-2,0},{0,2},{2,-2}}) {
      try {WaterBoundaryIndex.EMPTY.begin(delta[0],delta[1]);}catch(IllegalArgumentException expected){rejected++;}
    }
    need(rejected==4,"Invalid neighbor accepted");
    System.out.println("WATER_BOUNDARY_INDEX_CORE_PASS cases="+cases+" oldVisits="+oldVisits+" indexedVisits="+newVisits+" exactCoordinateOracle=true stableOrder=true immutable=true maxEntriesPerInput=3");
  }
  public static void main(String[] args){run();}
}
