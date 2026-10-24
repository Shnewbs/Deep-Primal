package com.shnewbs.deepprimal.core.density;
import com.shnewbs.deepprimal.core.density.*;
import java.util.*;
import java.util.concurrent.*;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;
public class ReuseChecks {
 static long checks;static void ok(boolean v,String why){checks++;if(!v)throw new AssertionError(why);}
 static void same(double a,double b){ok(Double.doubleToRawLongBits(a)==Double.doubleToRawLongBits(b),"bit mismatch "+a+" != "+b);}
 public static void main(String[] args)throws Exception {
  for(long seed:new long[]{0,1,-41,Long.MIN_VALUE})for(boolean floating:new boolean[]{false,true}){
   var g=TerrainShapeV8.definition(floating).compile(seed);var memo=g.newSampler();
   int x=(int)(seed^0x5c713);int z=(int)(seed^0x7219);x=x%29000000;z=z%29000000;
   for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++)for(int y=-128;y<640;y++)same(g.sample(x+dx,y,z+dz),memo.sample(x+dx,y,z+dz));
   for(var n:g.definition().nodes())for(int y=-8;y<8;y++)same(g.channel(n.id(),-.0,y,17.125),memo.channel(n.id(),-.0,y,17.125));
  }
  var def=new Definition(1,"root",List.of(Node.operation("x",Op.X),Node.operation("y",Op.Y),Node.operation("z",Op.Z),Node.constant("zero",0),Node.constant("one",1),new Node("shift",Op.WARP,List.of("x","one","zero","zero"),List.of(5.),null),Node.operation("sum",Op.ADD,"shift","x"),Node.bounded("t",Op.SMOOTHSTEP,"y",-3,7),Node.operation("root",Op.LERP,"sum","z","t")));
  var g=def.compile(71);var memo=g.newSampler();
  for(double x:new double[]{-.0,.0,-29999999.5,29999999.5,.125})for(int y=-10;y<10;y++)same(g.sample(x,y,3),memo.sample(x,y,3));
  double before=memo.sample(1,2,3);try{memo.sample(Double.NaN,2,3);throw new AssertionError("accepted nan");}catch(IllegalArgumentException expected){checks++;}same(before,memo.sample(1,2,3));
  try{memo.channel("missing",1,2,3);throw new AssertionError("accepted missing");}catch(IllegalArgumentException expected){checks++;}
  var identity=new Definition(1,"x",List.of(Node.operation("x",Op.X))).compile(0);var signed=identity.newSampler();
  same(-0.0,signed.sample(-0.0,0,0));same(0.0,signed.sample(0.0,0,0));same(-0.0,signed.sample(-0.0,0,0));
  for(double addend:new double[]{-0.0,0.0,1.0,-1.0,Double.MIN_VALUE,-Double.MIN_VALUE})for(double zero:new double[]{-0.0,0.0}){
   var n=List.of(Node.constant("a",addend),Node.constant("zero",zero),Node.noise("noise",NoiseSpec.DEFAULT,false),Node.operation("product",Op.MULTIPLY,"noise","zero"),Node.operation("root",Op.ADD,"a","product"));
   var masked=new Definition(1,"root",n).compile(17);var reuse=masked.newSampler();
   for(int k=-20;k<20;k++)same(masked.sample(k,k,k),reuse.sample(k,k,k));
  }
  var pool=Executors.newFixedThreadPool(4);try{
   ok(pool.submit(()->{try{memo.sample(1,2,3);return false;}catch(IllegalStateException expected){return true;}}).get(),"cross-thread sampler accepted");
   var tasks=new ArrayList<Callable<Boolean>>();for(int i=0;i<16;i++){final int j=i;tasks.add(()->{var local=g.newSampler();for(int y=-20;y<20;y++)if(Double.doubleToRawLongBits(local.sample(j,y,j))!=Double.doubleToRawLongBits(g.sample(j,y,j)))return false;return true;});}
   for(var f:pool.invokeAll(tasks))ok(f.get(),"independent worker mismatch");
  }finally{pool.shutdown();ok(pool.awaitTermination(5,TimeUnit.SECONDS),"pool termination");}
  System.out.println("DENSITY_REUSE_PASS checks="+checks+" recipeVolumes=8 exactSamples=1572864 sharedWarpCoordinates=true signedZero=true independentJobs=16");
 }
}
