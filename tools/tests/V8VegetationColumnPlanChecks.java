package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.biome.EcotoneSurfaceField;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Source-only checks; test-only V8 column and cover API doubles, not Minecraft. */
public final class V8VegetationColumnPlanChecks {
  private static long checks;
  private static final EcotoneSurfaceField.Sample GRASS = new EcotoneSurfaceField.Sample(
      .8, .2, 0, 0, .9, .8, EcotoneSurfaceField.Cover.GRASS, false, true);
  private static final EcotoneSurfaceField.Sample FERN = new EcotoneSurfaceField.Sample(
      0, 1, 0, 0, .95, .8, EcotoneSurfaceField.Cover.PODZOL, false, true);
  private static final EcotoneSurfaceField.Sample SNOW = new EcotoneSurfaceField.Sample(
      .8, .2, 0, .9, .9, .8, EcotoneSurfaceField.Cover.GRASS, true, true);
  private static final EcotoneSurfaceField.Sample BARE = new EcotoneSurfaceField.Sample(
      .1, 0, .9, 0, .5, .8, EcotoneSurfaceField.Cover.BARE, false, true);
  private static final EcotoneSurfaceField.Sample NO_PLANTS = new EcotoneSurfaceField.Sample(
      .8, .2, 0, 0, .01, .8, EcotoneSurfaceField.Cover.GRASS, false, false);
  private static void require(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message + " at " + checks);
  }
  private static V8EcotoneColumnPlan all(EcotoneSurfaceField.Sample cover) {
    var cols = new V8EcotoneColumnPlan.Column[256];
    Arrays.fill(cols, new V8EcotoneColumnPlan.Column(cover, 4, 98));
    return new V8EcotoneColumnPlan(cols);
  }
  private static String signature(V8VegetationColumnPlan p) {
    StringBuilder b=new StringBuilder();
    for (int z=0;z<16;z++) for (int x=0;x<16;x++) {
      var site=p.placement(x,z);
      b.append(site==null?'.':site.plant()==V8VegetationColumnPlan.Plant.FERN?'f':'g');
    }
    return b.toString();
  }
  private static V8VegetationColumnPlan sample(V8EcotoneColumnPlan c,
      int x,int z,long seed,int spacing,double density) {
    return V8VegetationColumnPlan.build(c,x,z,-64,384,seed,
        new V8VegetationPolicy(1,spacing,density));
  }
  private static void rejects(Runnable call) {
    checks++;
    try { call.run(); } catch (IllegalArgumentException | ArithmeticException | NullPointerException good) { return; }
    throw new AssertionError("expected invalid V8 argument");
  }
  public static void main(String[] args) throws Exception {
    var grass=all(GRASS);
    for (int spacing: new int[]{4,8,16}) {
      int expected=256/(spacing*spacing);
      for (int seed=0;seed<700;seed++) {
        long worldSeed=0x7391abcde0f415L+seed*37L;
        for (int origin: new int[]{-64,-32,-16,0,16,64,1024,-1024}) {
          var p=sample(grass,origin,-origin,worldSeed,spacing,1);
          require(p.count()==expected,"one canonical anchor per lattice cell");
          require(signature(p).equals(signature(sample(grass,origin,-origin,worldSeed,spacing,1))),"replay");
          for (int z=0;z<16;z++) for (int x=0;x<16;x++) {
            var site=p.placement(x,z);
            if(site!=null) require(site.y()==99 && site.plant()!=null,"height/plant");
          }
        }
      }
    }
    int halfDensityTotal=0;
    for (int seed=0;seed<512;seed++) {
      long s=0xabcde0001L+seed;
      var p=sample(all(FERN),-32,0,s,4,1);
      require(p.count()==16,"ferns occupancy");
      for(int z=0;z<16;z++) for(int x=0;x<16;x++) {
        var site=p.placement(x,z);
        if(site!=null) require(site.plant()==V8VegetationColumnPlan.Plant.FERN,"ferns species");
      }
      require(sample(all(SNOW),-32,0,s,4,1).count()==0,"snow protected");
      require(sample(all(BARE),-32,0,s,4,1).count()==0,"bare protected");
      require(sample(all(NO_PLANTS),-32,0,s,4,1).count()==0,"coherent patch protected");
      require(sample(grass,-32,0,s,4,0).count()==0,"saved zero density");
      int thinned=sample(grass,-32,0,s,4,.5).count();
      require(thinned<=16,"bounded thinning");
      halfDensityTotal+=thinned;
    }
    require(halfDensityTotal>0 && halfDensityTotal<512*16,"effective saved density scaling");
    var cols = new V8EcotoneColumnPlan.Column[256];
    Arrays.fill(cols,new V8EcotoneColumnPlan.Column(GRASS,4,98));
    cols[0]=null; cols[1]=new V8EcotoneColumnPlan.Column(SNOW,4,98);
    cols[2]=new V8EcotoneColumnPlan.Column(GRASS,4,319);
    var masked=new V8EcotoneColumnPlan(cols);
    for (int s=0;s<64;s++) {
      var plan=sample(masked,0,-16,s,4,1);
      require(plan.placement(0,0)==null,"water excluded");
      require(plan.placement(1,0)==null,"snow excluded");
      require(plan.placement(2,0)==null,"high ceiling excluded");
    }
    rejects(()->sample(grass,-15,0,1,4,1));
    rejects(()->sample(grass,0,-1,1,4,1));
    rejects(()->sample(grass,Integer.MAX_VALUE-14,0,1,4,1));
    rejects(()->sample(grass,0,Integer.MAX_VALUE-14,1,4,1));
    rejects(()->V8VegetationColumnPlan.build(grass,0,0,Integer.MAX_VALUE,10,1,
        new V8VegetationPolicy(1,4,1)));
    rejects(()->new V8VegetationPolicy(1,3,1));
    rejects(()->new V8VegetationPolicy(1,4,Double.NaN));
    rejects(()->new V8VegetationPolicy(1,4,1.1));
    rejects(()->V8VegetationColumnPlan.build(null,0,0,0,100,1,
        new V8VegetationPolicy(1,4,1)));
    rejects(()->V8VegetationColumnPlan.build(grass,0,0,0,100,1,null));
    var p=sample(grass,-16,0,17,4,1);
    try {p.placement(16,0);throw new AssertionError("local out-of-range accepted");}
    catch(IndexOutOfBoundsException good){checks++;}
    // Reversed and concurrent independent calls yield the same immutable plans.
    String expected=signature(sample(grass,-16,-16,73,4,1));
    ExecutorService pool=Executors.newFixedThreadPool(4);
    try {
      var tasks=new ArrayList<Future<String>>();
      for(int i=0;i<64;i++) tasks.add(pool.submit(()->signature(sample(grass,-16,-16,73,4,1))));
      for(Future<String> task:tasks)require(expected.equals(task.get()),"parallel exact replay");
    } finally {pool.shutdownNow();}
    System.out.println("V8_VEGETATION_PLAN_PASS assertions="+checks+" replay=true signed=true parallel=true");
  }
}
