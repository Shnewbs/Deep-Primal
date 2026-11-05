package com.shnewbs.deepprimal.core.density;

import java.util.*;
import java.util.concurrent.*;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

/** Bit-exact scalar/sampler comparisons, signed-zero cases and per-thread ownership. */
public final class ReuseChecks {
  private static long checks;

  private static void same(double a,double b) {
    checks++;
    if(Double.doubleToRawLongBits(a)!=Double.doubleToRawLongBits(b))
      throw new AssertionError("bit mismatch "+a+" != "+b);
  }

  public static void main(String[] args)throws Exception {
    for(long seed:new long[]{0,1,-41,Long.MIN_VALUE})for(boolean floating:new boolean[]{false,true}) {
      var graph=TerrainShapeV8.definition(floating).compile(seed);
      var sampler=graph.newSampler();
      int x=(int)(seed^0x5c713),z=(int)(seed^0x7219);
      x%=29_000_000;z%=29_000_000;
      for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++)for(int y=-128;y<640;y++)
        same(graph.sample(x+dx,y,z+dz),sampler.sample(x+dx,y,z+dz));
      for(var node:graph.definition().nodes())for(int y=-8;y<8;y++)
        same(graph.channel(node.id(),-.0,y,17.125),sampler.channel(node.id(),-.0,y,17.125));
    }
    var definition=new Definition(1,"root",List.of(
        Node.operation("x",Op.X),Node.operation("y",Op.Y),Node.operation("z",Op.Z),
        Node.constant("one",1),Node.constant("zero",0),
        new Node("shift",Op.WARP,List.of("x","one","zero","zero"),List.of(5.),null),
        Node.operation("sum",Op.ADD,"shift","x"),
        Node.bounded("t",Op.SMOOTHSTEP,"y",-3,7),
        Node.operation("root",Op.LERP,"sum","z","t")));
    var graph=definition.compile(71);
    var sampler=graph.newSampler();
    for(double x:new double[]{-.0,.0,-29_999_999.5,29_999_999.5,.125})
      for(int y=-10;y<10;y++)same(graph.sample(x,y,3),sampler.sample(x,y,3));
    double before=sampler.sample(1,2,3);
    try {
      sampler.sample(Double.NaN,2,3);
      throw new AssertionError("accepted NaN");
    } catch(IllegalArgumentException expected) {checks++;}
    same(before,sampler.sample(1,2,3));
    try {
      sampler.channel("missing",1,2,3);
      throw new AssertionError("accepted missing channel");
    } catch(IllegalArgumentException expected) {checks++;}

    var identity=new Definition(1,"x",List.of(Node.operation("x",Op.X))).compile(0);
    var signed=identity.newSampler();
    same(-0.0,signed.sample(-0.0,0,0));
    same(0.0,signed.sample(0.0,0,0));
    same(-0.0,signed.sample(-0.0,0,0));
    for(double addend:new double[]{-0.0,0.0,1.0,-1.0,Double.MIN_VALUE,-Double.MIN_VALUE})
      for(double zero:new double[]{-0.0,0.0}) {
        var nodes=List.of(Node.constant("a",addend),Node.constant("zero",zero),
            Node.noise("noise",NoiseSpec.DEFAULT,false),
            Node.operation("product",Op.MULTIPLY,"noise","zero"),
            Node.operation("root",Op.ADD,"a","product"));
        var masked=new Definition(1,"root",nodes).compile(17);
        var reused=masked.newSampler();
        for(int k=-20;k<20;k++)same(masked.sample(k,k,k),reused.sample(k,k,k));
      }

    var pool=Executors.newFixedThreadPool(4);
    try {
      if(!pool.submit(()->{
        try {sampler.sample(1,2,3);return false;}
        catch(IllegalStateException expected) {return true;}
      }).get())throw new AssertionError("cross-thread sampler accepted");
      checks++;
      var tasks=new ArrayList<Callable<Boolean>>();
      for(int i=0;i<16;i++) {
        final int job=i;
        tasks.add(()->{
          var local=graph.newSampler();
          for(int y=-20;y<20;y++)
            if(Double.doubleToRawLongBits(local.sample(job,y,job))
                !=Double.doubleToRawLongBits(graph.sample(job,y,job)))return false;
          return true;
        });
      }
      for(var future:pool.invokeAll(tasks)) {
        if(!future.get())throw new AssertionError("independent sampler mismatch");
        checks++;
      }
    } finally {
      pool.shutdown();
      if(!pool.awaitTermination(5,TimeUnit.SECONDS))throw new AssertionError("worker pool did not stop");
      checks++;
    }
    System.out.println("DENSITY_REUSE_PASS checks="+checks+" recipeVolumes=8 exactSamples=1572864 sharedWarpCoordinates=true signedZero=true independentJobs=16");
  }
}
