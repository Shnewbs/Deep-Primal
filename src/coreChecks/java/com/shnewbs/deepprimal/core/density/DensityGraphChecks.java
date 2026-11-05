package com.shnewbs.deepprimal.core.density;

import java.util.*;
import java.util.concurrent.*;
import java.security.*;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

public final class DensityGraphChecks {
  private static int checks;
  private static void check(boolean condition,String message) {checks++;if(!condition)throw new AssertionError(message);}
  private static void reject(Runnable r) {try{r.run();throw new AssertionError("expected invalid graph rejection");}catch(IllegalArgumentException expected){checks++;}}
  private static void same(double a,double b,String message) {check(Double.doubleToLongBits(a)==Double.doubleToLongBits(b),message+": "+a+" != "+b);}
  private static Definition d(String root,Node... n) {return new Definition(1,root,List.of(n));}
  public static void main(String[] args) throws Exception {
    var affine=d("out",Node.operation("x",Op.X),Node.operation("y",Op.Y),Node.operation("z",Op.Z),
        Node.constant("three",3),Node.operation("scaled",Op.MULTIPLY,"x","three"),
        Node.operation("xy",Op.ADD,"scaled","y"),Node.operation("out",Op.ADD,"xy","z")).compile(1);
    var random=new Random(0x8831);
    for(int i=0;i<10_000;i++) {
      double x=random.nextInt(60_000_001)-30_000_000,y=random.nextInt(2048)-512,z=random.nextInt(60_000_001)-30_000_000;
      same(affine.sample(x,y,z),3*x+y+z,"affine independent formula");
    }
    var warped=d("out",Node.operation("x",Op.X),Node.operation("y",Op.Y),Node.operation("z",Op.Z),
        Node.operation("sum",Op.ADD,"x","y"),Node.operation("source",Op.ADD,"sum","z"),
        Node.constant("wx",.25),Node.constant("wy",-.5),Node.constant("wz",1),
        new Node("out",Op.WARP,List.of("source","wx","wy","wz"),List.of(64.),null)).compile(4);
    for(int i=0;i<500;i++) {int x=i-250,y=i*2,z=-i;same(warped.sample(x,y,z),(x+16.)+(y-32.)+(z+64.),"warp world coordinates");}
    for(var op:List.of(Op.MIN,Op.MAX,Op.MULTIPLY,Op.ADD)) {
      var g=d("out",Node.operation("x",Op.X),Node.operation("y",Op.Y),Node.operation("out",op,"x","y"));
      if(op==Op.MULTIPLY) {reject(()->g.compile(0));continue;} // Conservative full-world range exceeds budget.
      var p=g.compile(0);for(int i=-20;i<20;i++) {double expected=switch(op){case MIN->Math.min(i,-i);case MAX->Math.max(i,-i);default->0;};same(p.sample(i,-i,0),expected,"binary "+op);}
    }
    var lerp=d("out",Node.constant("a",-5),Node.constant("b",9),Node.operation("y",Op.Y),Node.operation("out",Op.LERP,"a","b","y")).compile(0);
    same(lerp.sample(0,-10,0),-5,"lerp lower endpoint");same(lerp.sample(0,10,0),9,"lerp upper endpoint");same(lerp.sample(0,.5,0),2,"lerp midpoint");
    var smooth=d("out",Node.operation("y",Op.Y),Node.bounded("out",Op.SMOOTHSTEP,"y",-2,2)).compile(0);
    same(smooth.sample(0,0,0),.5,"smooth midpoint");same(smooth.sample(0,-3,0),0,"smooth low");same(smooth.sample(0,3,0),1,"smooth high");
    var clamp=d("out",Node.operation("y",Op.Y),Node.bounded("out",Op.CLAMP,"y",-2,2)).compile(0);
    same(clamp.sample(0,-4,0),-2,"clamp low");same(clamp.sample(0,1,0),1,"clamp interior");same(clamp.sample(0,4,0),2,"clamp high");
    var abs=d("out",Node.operation("y",Op.Y),Node.operation("out",Op.ABS,"y")).compile(0);same(abs.sample(0,-3,0),3,"abs");
    invalids(affine);
    var digest=MessageDigest.getInstance("SHA-256");var fingerprints=new HashSet<String>();long seamSamples=0;
    int overhangColumns=0;double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
    for(long seed:new long[]{0,1,73,20261020}) {
      var shape=TerrainShapeV8.definition(false).compile(seed);var floating=TerrainShapeV8.definition(true).compile(seed);
      check(shape.maximumEvaluationCost()<=MAX_COST&&shape.depth()<=MAX_DEPTH,"bounded built-in recipe");
      var seedHash=MessageDigest.getInstance("SHA-256");
      for(int i=0;i<128;i++) {
        int x=(i*137)%8192-4096,z=(i*367)%8192-4096;
        double h=shape.channel("height",x,0,z);min=Math.min(min,h);max=Math.max(max,h);
        byte[] bits=java.nio.ByteBuffer.allocate(8).putLong(Double.doubleToLongBits(h)).array();seedHash.update(bits);digest.update(bits);
        int transitions=0;boolean last=shape.sample(x,-128,z)>0;
        for(int y=-124;y<=640;y+=4){boolean solid=shape.sample(x,y,z)>0;if(solid!=last)transitions++;last=solid;}
        if(transitions>1)overhangColumns++;
        double a=shape.sample(x,370,z),b=floating.sample(x,370,z);check(b>=a,"floating density union");
      }
      fingerprints.add(HexFormat.of().formatHex(seedHash.digest()));
      for(int[] origin:new int[][]{{-16,-128,-16},{-30_000_000,-128,29_999_968},{29_999_968,-128,-30_000_000}}) {
        int x=origin[0],y=origin[1],z=origin[2];var a=DensityGrid.sample(shape,x,y,z,4,8,4,5,17,5);
        var right=DensityGrid.sample(shape,x+16,y,z,4,8,4,5,17,5);var above=DensityGrid.sample(shape,x,y+128,z,4,8,4,5,17,5);
        for(int iy=0;iy<=128;iy++)for(int iz=0;iz<=16;iz++){same(a.sample(x+16,y+iy,z+iz),right.sample(x+16,y+iy,z+iz),"shape shared X face");seamSamples++;}
        for(int ix=0;ix<=16;ix++)for(int iz=0;iz<=16;iz++){same(a.sample(x+ix,y+128,z+iz),above.sample(x+ix,y+128,z+iz),"shape shared Y face");seamSamples++;}
      }
    }
    check(fingerprints.size()==4,"distinct seed recipe heights");check(max-min>100,"height variety corpus");check(overhangColumns>0,"genuine non-heightfield columns");
    var shared=TerrainShapeV8.definition(false).compile(73);var jobs=Executors.newFixedThreadPool(4);
    try {var tasks=new ArrayList<Callable<Boolean>>();for(int j=0;j<32;j++)tasks.add(()->{for(int i=0;i<128;i++)if(Double.doubleToLongBits(shared.sample(i,-17,i*3))!=Double.doubleToLongBits(TerrainShapeV8.definition(false).compile(73).sample(i,-17,i*3)))return false;return true;});
      for(var f:jobs.invokeAll(tasks))check(f.get(),"concurrent seed-isolated replay");
    } finally {jobs.shutdownNow();}
    String fingerprint=HexFormat.of().formatHex(digest.digest());
    check(fingerprint.equals("c997cbd6ea9c6149ebc0942d841e4632e919c184a9b1bf809274dd36a2614f26"),"version-one shape fingerprint");
    System.out.println("DENSITY_GRAPH_PASS checks="+checks+" sharedFaceSamples="+seamSamples+" overhangColumns="+overhangColumns+" minHeight="+min+" maxHeight="+max+" fingerprint="+fingerprint);
  }
  private static void invalids(DensityGraph valid) {
    reject(()->new Definition(2,"a",List.of(Node.constant("a",0))));reject(()->d("missing",Node.constant("a",0)).compile(0));
    reject(()->d("a",Node.constant("a",0),Node.constant("a",1)).compile(0));reject(()->d("a",Node.constant("a",0),Node.constant("unused",1)).compile(0));
    reject(()->d("a",Node.operation("a",Op.ABS,"b")).compile(0));reject(()->d("a",Node.operation("a",Op.ABS,"b"),Node.operation("b",Op.ABS,"a")).compile(0));
    reject(()->Node.constant("a",Double.NaN));reject(()->Node.constant("a",Double.POSITIVE_INFINITY));reject(()->Node.constant("bad-id",0));
    reject(()->Node.operation("a",Op.ADD,"b"));reject(()->Node.bounded("a",Op.CLAMP,"b",1,1));
    reject(()->new Node("a",Op.NOISE_3D,List.of(),List.of(),null));
    reject(()->new Node("a",Op.X,List.of(),List.of(),NoiseSpec.DEFAULT));
    var deep=new ArrayList<Node>();deep.add(Node.constant("a0",0));for(int i=1;i<=24;i++)deep.add(Node.operation("a"+i,Op.ABS,"a"+(i-1)));
    reject(()->new Definition(1,"a24",deep).compile(0));
    var costly=new ArrayList<Node>();costly.add(Node.constant("a0",0));for(int i=1;i<=12;i++)costly.add(Node.operation("a"+i,Op.ADD,"a"+(i-1),"a"+(i-1)));
    reject(()->new Definition(1,"a12",costly).compile(0));
    var huge=new ArrayList<Node>();for(int i=0;i<=128;i++)huge.add(Node.constant("a"+i,0));reject(()->new Definition(1,"a0",huge));
    reject(()->d("out",Node.operation("x",Op.X),new Node("out",Op.WARP,List.of("x","x","x","x"),List.of(2.),null)).compile(0));
    reject(()->valid.sample(Double.NaN,0,0));reject(()->valid.sample(30_000_001,0,0));reject(()->valid.channel("missing",0,0,0));
  }
}
