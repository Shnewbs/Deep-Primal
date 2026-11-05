package com.shnewbs.deepprimal.core;

final class DepositAllocationChecks {
  static void run() {
    var random=new java.util.Random(202610240702L);
    var sample=new TerrainEngine.Sample(400,Double.NaN,.5,.5,.1,.1,0,0,0,0);
    long cases=0,hits=0;int empty=0,active=0;
    for(int seedIndex=0;seedIndex<32;seedIndex++) {
      long seed=seedIndex==0?Long.MIN_VALUE:seedIndex==1?Long.MAX_VALUE:random.nextLong();
      var noise=new TerrainNoise(seed);var geo=new Geology(noise);
      var control=new R50DepositField(noise);var candidate=new DepositField(noise);
      var held=new DepositField.Column[2048];var prior=new R50DepositField.Column[2048];
      for(int i=0;i<2048;i++) {
        int x=i<2?i==0?Integer.MIN_VALUE:Integer.MAX_VALUE:random.nextInt(60000001)-30000000;
        int z=i<2?i==0?Integer.MAX_VALUE:Integer.MIN_VALUE:random.nextInt(60000001)-30000000;
        var host=geo.column(x,z,sample);held[i]=candidate.column(x,z,host);prior[i]=control.column(x,z,host);
        if(held[i].activeCandidateCount()!=prior[i].activeCandidateCount())throw new AssertionError("footprint changed");
        if(held[i].activeCandidateCount()==0)empty++;else active++;
      }
      // Query after all later columns have been constructed, testing retained/empty isolation too.
      for(int i=0;i<held.length;i++)for(int j=0;j<64;j++) {
        int y=-128+j*7,bed=100+(i%400);var expected=prior[i].ore(y,bed);
        if(held[i].ore(y,bed)!=expected)throw new AssertionError("R50 output changed seed="+seed+" column="+i+" y="+y);
        if(expected!=Geology.Ore.NONE)hits++;cases++;
      }
      try {candidate.column(0,0,null);throw new AssertionError("null host accepted");}
      catch(NullPointerException expected) {}
    }
    if(hits<100||empty==0||active==0)throw new AssertionError("insufficient footprint coverage");
    System.out.println("DEPOSIT_ALLOCATION_CORE_PASS cases="+cases+" oreHits="+hits+" emptyColumns="+empty+" activeColumns="+active+" integerExtremes=true retainedColumns=true nullHostRejected=true");
  }
  public static void main(String[] args){run();}
}
