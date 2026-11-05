package com.shnewbs.deepprimal.core;

final class DepositFieldChecks {
  private static Geology.Ore allowed(int type,Geology.Rock rock){
    return switch(type){
      case 0 -> rock==Geology.Rock.SANDSTONE||rock==Geology.Rock.TUFF?Geology.Ore.COAL:Geology.Ore.NONE;
      case 1 -> rock==Geology.Rock.ANDESITE||rock==Geology.Rock.CALCITE?Geology.Ore.COPPER:Geology.Ore.NONE;
      case 2 -> Geology.Ore.IRON;
      case 3 -> rock==Geology.Rock.GRANITE||rock==Geology.Rock.DIORITE?Geology.Ore.GOLD:Geology.Ore.NONE;
      case 4 -> rock==Geology.Rock.BASALT||rock==Geology.Rock.GRANITE?Geology.Ore.REDSTONE:Geology.Ore.NONE;
      case 5 -> rock==Geology.Rock.CALCITE?Geology.Ore.LAPIS:Geology.Ore.NONE;
      case 6 -> rock==Geology.Rock.BASALT||rock==Geology.Rock.DEEPSLATE?Geology.Ore.DIAMOND:Geology.Ore.NONE;
      default -> rock==Geology.Rock.CALCITE||rock==Geology.Rock.GRANITE?Geology.Ore.EMERALD:Geology.Ore.NONE;
    };
  }
  // Wider49-source oracle: direct absolute centers and independent host eligibility.
  private static Geology.Ore oracle(TerrainNoise n,int x,int z,int y,int bed,Geology.Column host){
    if(y>=bed-5)return Geology.Ore.NONE;
    for(int gx=Math.floorDiv(x,32)-3;gx<=Math.floorDiv(x,32)+3;gx++)
      for(int gz=Math.floorDiv(z,32)-3;gz<=Math.floorDiv(z,32)+3;gz++){
        double radius=3+n.hash(gx,gz,1814)*5;
        double cx=(long)gx*32+8+n.hash(gx,gz,1810)*16,cz=(long)gz*32+8+n.hash(gx,gz,1811)*16;
        double cy=-96+n.hash(gx,gz,1812)*352;
        double ax=(x-cx)/(radius*1.7),az=(z-cz)/radius,ay=(y-cy)/(radius*.45);
        if(ax*ax+ay*ay+az*az<=1){var ore=allowed((int)(n.hash(gx,gz,1813)*8),host.rock(y));if(ore!=Geology.Ore.NONE)return ore;}
      }
    return Geology.Ore.NONE;
  }
  static void run(){
    var random=new java.util.Random(202610240532L);int cases=0,clipped=0,seams=0;String first="";
    var sample=new TerrainEngine.Sample(400,Double.NaN,.5,.5,.1,.1,0,0,0,0);
    for(long seed:new long[]{0,42,-914,Long.MIN_VALUE,Long.MAX_VALUE}){
      var noise=new TerrainNoise(seed);var geology=new Geology(noise);var field=new DepositField(noise);
      for(int i=0;i<10000;i++){
        int x=random.nextInt(60000001)-30000000,z=random.nextInt(60000001)-30000000;
        if(i%2==0){x=(random.nextInt(10001)-5000)*32+(i%3-1);seams++;}
        int y=random.nextInt(641)-128,bed=random.nextInt(400)+100;var host=geology.column(x,z,sample);
        var column=field.column(x,z,host);var expected=oracle(noise,x,z,y,bed,host);
        if(column.candidateCount()!=9||column.activeCandidateCount()<0||column.activeCandidateCount()>9||column.ore(y,bed)!=expected||field.column(x,z,host).ore(y,bed)!=expected)
          throw new AssertionError("bounded deposit oracle/replay seed="+seed+" x="+x+" z="+z+" y="+y);
        if(column.ore(bed-5,bed)!=Geology.Ore.NONE)throw new AssertionError("near-surface exclusion");cases++;
      }
      // Target ellipsoids spilling over either X cell edge, including negative coordinates.
      for(int gx=-128;gx<128;gx++)for(int gz=-8;gz<8;gz++){
        int z=(int)Math.round((long)gz*32+8+noise.hash(gx,gz,1811)*16);
        int y=(int)Math.round(-96+noise.hash(gx,gz,1812)*352);
        for(int edge:new int[]{gx*32-1,(gx+1)*32}){
          var host=geology.column(edge,z,sample);var expected=oracle(noise,edge,z,y,512,host);
          var actual=field.column(edge,z,host).ore(y,512);if(actual!=expected)throw new AssertionError("cross-cell field");
          if(host.ore(y,512)==Geology.Ore.NONE&&expected!=Geology.Ore.NONE){clipped++;if(first.isEmpty())first="seed="+seed+" x="+edge+" z="+z+" y="+y+" ore="+expected;}
          cases++;
        }
      }
    }
    if(clipped<20)throw new AssertionError("missing clipped-deposit witnesses "+clipped);
    System.out.println("DEPOSIT_FIELD_CORE_PASS cases="+cases+" clippedV4="+clipped+" signedSeams="+seams+" candidates=9 oracleCandidates=49 replay=true first={"+first+"}");
  }
  public static void main(String[] args){run();}
}
