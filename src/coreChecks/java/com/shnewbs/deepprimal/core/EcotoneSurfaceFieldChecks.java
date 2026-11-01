package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.biome.EcotoneSurfaceField;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend.Climate;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend.Domain;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend.Widths;
import com.shnewbs.deepprimal.engine.*;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.NoiseSpec;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;

/** Java21 API-double integration checks. Not native NeoForge or GPU acceptance. */
public final class EcotoneSurfaceFieldChecks {
  private static long assertions;
  private static void check(boolean b,String s) {assertions++; if(!b) throw new AssertionError(s);}
  private static void near(double a,double b,double tol,String s){check(Double.isFinite(a)&&Math.abs(a-b)<=tol,s+" "+a+" != "+b);}
  private static void reject(Runnable task) {boolean rejected=false;try{task.run();}catch(IllegalArgumentException|NullPointerException|IllegalStateException expected){rejected=true;}check(rejected,"bad request accepted");}
  private static DensityGraph.Definition graph() {
    return new DensityGraph.Definition(1,"density",List.of(
        DensityGraph.Node.constant("bias",-.05),
        DensityGraph.Node.noise("climate",NoiseSpec.DEFAULT,true),
        DensityGraph.Node.operation("density",DensityGraph.Op.ADD,"bias","climate")));
  }
  public static void main(String[] args) throws Exception {
    var b = new EcotoneSurfaceField(123456789L);
    var weights = EcotoneBlend.sample(new Climate(100,.5,.5,.5,0),Domain.LAND,Widths.PREVIEW);
    double expectedGrass=0,expectedPodzol=0,expectedVegetation=0,expectedSoil=0;
    for(int id=0;id<EcotoneBlend.PALETTE_SIZE;id++) {
      double w=weights.weight(id);var props=EcotoneSurfaceField.PREVIEW_R1.get(id);
      expectedGrass+=w*props.grass(); expectedPodzol+=w*props.podzol();
      expectedVegetation+=w*props.vegetation(); expectedSoil+=w*props.soilFactor();
    }
    var sample = b.sample(-17, 31,Domain.LAND,weights,EcotoneSurfaceField.PREVIEW_R1);
    near(sample.grass(), expectedGrass,1e-15,"actual continuous weighted grass");
    near(sample.podzol(),expectedPodzol,1e-15,"actual continuous weighted podzol");
    near(sample.vegetation(),expectedVegetation,1e-15,"actual continuous vegetation density");
    near(sample.soilFactor(),expectedSoil,1e-15,"actual continuous soil factor");
    near(sample.grass()+sample.podzol()+sample.bare(),1,1e-15,"complete cover partition");
    reject(()->b.sample(1,1,Domain.RIVER,weights,EcotoneSurfaceField.PREVIEW_R1));
    reject(()->b.sample(1,1,Domain.OCEAN,weights,EcotoneSurfaceField.PREVIEW_R1));
    reject(()->b.sample(1,1,Domain.LAND,weights,null));
    reject(()->b.sample(1,1,Domain.LAND,null,EcotoneSurfaceField.PREVIEW_R1));
    reject(()->new EcotoneSurfaceField.BiomeCover(-.1,0,0,0,0));
    reject(()->new EcotoneSurfaceField.BiomeCover(.8,.3,0,0,0));
    reject(()->new EcotoneSurfaceField.Palette(new EcotoneSurfaceField.BiomeCover[39]));
    var copied = EcotoneSurfaceField.PREVIEW_R1.toArray(); copied[0]=new EcotoneSurfaceField.BiomeCover(0,0,0,0,0);
    near(EcotoneSurfaceField.PREVIEW_R1.get(0).grass(),.72,0,"palette immutable");
    for(int z=-96; z<=96;z++)for(int x=-96; x<=96;x++){
      var v=b.sample(x,z,Domain.LAND,weights,EcotoneSurfaceField.PREVIEW_R1);
      check(v.grass()>=0&&v.podzol()>=0&&v.bare()>=0,"nonnegative weighted cover");
      check(v.snow()>=0&&v.snow()<=1,"snow fraction bounded");
      check(v.vegetation()>=0&&v.vegetation()<=1,"vegetation fraction bounded");
      double draw=b.coverField(x,z);
      check(draw>=0&&draw<1,"stochastic draw unit interval");
      check(v.cover() == (draw<v.grass()?EcotoneSurfaceField.Cover.GRASS:
          draw<v.grass()+v.podzol()?EcotoneSurfaceField.Cover.PODZOL:EcotoneSurfaceField.Cover.BARE),"cover choice weighted");
      check(b.sample(x,z,Domain.LAND,weights,EcotoneSurfaceField.PREVIEW_R1).equals(v),"repeat bit exact");
      if(x>-96)check(Math.abs(b.coverField(x,z)-b.coverField(x-1,z))<.04,"not speckled adjacent X");
      if(z>-96)check(Math.abs(b.coverField(x,z)-b.coverField(x,z-1))<.04,"not speckled adjacent Z");
    }
    for(int x:new int[]{Integer.MIN_VALUE,-65537,-17,-16,-1,0,1,15,16,17,65535,Integer.MAX_VALUE})
      for(int z:new int[]{Integer.MIN_VALUE,-17,-16,-1,0,1,15,16,17,Integer.MAX_VALUE}){
        var sampleA=b.sample(x,z,Domain.LAND,weights,EcotoneSurfaceField.PREVIEW_R1);
        var sampleB=new EcotoneSurfaceField(123456789L).sample(x,z,Domain.LAND,weights,EcotoneSurfaceField.PREVIEW_R1);
        check(sampleA.equals(sampleB),"signed absolute-coord replay");
      }
    check(Double.doubleToLongBits(b.coverField(0,0))!=Double.doubleToLongBits(new EcotoneSurfaceField(99).coverField(0,0)),"seed separation");
    var ground = new GroundSurfaceProfile(1, List.of(GroundSurfaceProfile.Material.DIRT, GroundSurfaceProfile.Material.GRASS, GroundSurfaceProfile.Material.PODZOL), List.of(0.33, 0.66, 1.0), 0.1, 1.0, 0L, 4, "climate", -1.0, 1.0);
    var hard=new V8GeneratorSnapshot(1,91L,-64,256,graph(),ground);
    reject(()->hard.prepare().previewEcotoneSurface(0,0,new Climate(100,.5,.5,.5,0),Domain.LAND));
    var soft=hard.withBlendPolicy(new V8BlendPolicy(1,Widths.PREVIEW));
    var prepared=soft.prepare();
    var c=new Climate(100,.5,.5,.5,0);
    var actual=prepared.previewEcotoneSurface(-16,16,c,Domain.LAND);
    var expected=new EcotoneSurfaceField(91L).sample(-16,16,Domain.LAND,
        EcotoneBlend.sample(c,Domain.LAND,Widths.PREVIEW),EcotoneSurfaceField.PREVIEW_R1);
    check(actual.equals(expected),"saved policy width and seed correctly wired");
    reject(()->prepared.previewEcotoneSurface(0,0,c,Domain.RIVER));
    var encoded=V8GeneratorSnapshot.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, soft).getOrThrow();
    var reloaded=V8GeneratorSnapshot.CODEC.decode(com.mojang.serialization.JsonOps.INSTANCE, encoded).getOrThrow().getFirst().prepare();
    check(reloaded.previewEcotoneSurface(-16,16,c,Domain.LAND).equals(actual),"saved policy object-codec roundtrip parity");
    try (var exec=Executors.newFixedThreadPool(4)) {
      var jobs=new java.util.ArrayList<Callable<Boolean>>();
      for(int job=0;job<8;job++){
        final boolean reverse=job%2!=0;
        jobs.add(()->{for(int i=0;i<2048;i++){
          int k=reverse?2047-i:i;int x=-40000+37*k,z=55000-19*k;
          if(!prepared.previewEcotoneSurface(x,z,c,Domain.LAND).equals(
                reloaded.previewEcotoneSurface(x,z,c,Domain.LAND)))return false;
        }return true;});
      }
      for(var future:exec.invokeAll(jobs))check(future.get(),"parallel/reversed replay");
    }
    System.out.println("ECOTONE_SURFACE_CHECKS_PASS assertions="+assertions+" java="+System.getProperty("java.version")+" backend=API_DOUBLES");
  }
}
