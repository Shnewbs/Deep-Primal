package com.shnewbs.deepprimal.core.density;

import java.util.List;
import java.util.Random;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

/** V8 occupancy-preflight gates; no active world generator invokes this staged component. */
public final class DensityVolumeBoundsChecks {
  private static long assertions;
  private static void check(boolean okay, String message) {
    assertions++;
    if (!okay) throw new AssertionError(message);
  }
  private static void reject(Runnable action) {
    try {action.run(); throw new AssertionError("invalid volume accepted");}
    catch (IllegalArgumentException accepted) {assertions++;}
  }

  private static void oracle(DensityGraph graph, DensityVolumeBounds bounds,
                             int x, int y, int z, int dx, int dy, int dz) {
    var claimed=bounds.classifyCells(x,y,z,dx,dy,dz);
    for(int oz=0;oz<dz;oz++)for(int oy=0;oy<dy;oy++)for(int ox=0;ox<dx;ox++) {
      double sample=graph.sample(x+ox,y+oy,z+oz);
      check(Double.isFinite(sample),"finite exact sample");
      if(claimed==DensityVolumeBounds.Occupancy.SOLID)
        check(sample>0,"false solid certificate at "+x+","+y+","+z);
      else if(claimed==DensityVolumeBounds.Occupancy.EMPTY)
        check(sample<=0,"false empty certificate at "+x+","+y+","+z);
    }
  }

  public static void main(String[] args) {
    var linear=new Definition(1,"density",List.of(
        Node.constant("hundred",100),Node.operation("y",Op.Y),Node.constant("minus_one",-1),
        Node.operation("negative_y",Op.MULTIPLY,"y","minus_one"),
        Node.operation("density",Op.ADD,"hundred","negative_y"))).compile(0);
    var bounds=new DensityVolumeBounds(linear);
    check(bounds.classifyCells(-4,0,-4,8,8,8)==DensityVolumeBounds.Occupancy.SOLID,"deep solid");
    check(bounds.classifyCells(-4,300,-4,8,8,8)==DensityVolumeBounds.Occupancy.EMPTY,"high air");
    check(bounds.classifyCells(-4,97,-4,8,8,8)==DensityVolumeBounds.Occupancy.UNCERTAIN,"crossing");
    for(int y=-200;y<400;y++)oracle(linear,bounds,-1,y,-1,2,2,2);
    reject(()->bounds.classify(1,0,0,0,0,0));
    reject(()->bounds.classify(Double.NaN,0,0,1,1,1));
    reject(()->bounds.classifyCells(DensityGraph.WORLD_LIMIT,0,0,64,1,1));
    reject(()->bounds.classifyCells(0,0,0,0,1,1));

    var noise=new Definition(1,"density",List.of(
        Node.operation("y",Op.Y),Node.constant("neg",-1),
        Node.operation("negative_y",Op.MULTIPLY,"y","neg"),
        Node.constant("offset",40),Node.operation("height",Op.ADD,"offset","negative_y"),
        Node.noise("noise",new NoiseSpec(.027,.027,.027,3,2,.5,17),false),
        Node.operation("density",Op.ADD,"height","noise"))).compile(123);
    var noiseBounds=new DensityVolumeBounds(noise);
    check(noiseBounds.classifyCells(-10,-200,-10,8,8,8)==DensityVolumeBounds.Occupancy.SOLID,"noise-backed deep");
    check(noiseBounds.classifyCells(-10,200,-10,8,8,8)==DensityVolumeBounds.Occupancy.EMPTY,"noise-backed air");
    var random=new Random(0x43a35b7a);
    for(int i=0;i<2500;i++) {
      int x=random.nextInt(4096)-2048,y=random.nextInt(180)-60,z=random.nextInt(4096)-2048;
      oracle(noise,noiseBounds,x,y,z,2,2,2);
    }

    // A corner estimate is not an occupancy certificate for unsampled noise.
    // A pure noise graph must remain uncertain even at a single cell. A falsely
    // assumed zero noise value would certify EMPTY despite positive interiors.
    var pureNoise=new Definition(1,"density",List.of(
        Node.noise("density",new NoiseSpec(.027,.027,.027,3,2,.5,17),false))).compile(123);
    var pureBounds=new DensityVolumeBounds(pureNoise);
    int positiveWitnesses=0;
    for(int k=0;k<200;k++) {
      int x=k*13-1000,y=k%23-11,z=k*37-3000;
      double value=pureNoise.sample(x,y,z);
      var claim=pureBounds.classifyCells(x,y,z,1,1,1);
      if(value>0) {positiveWitnesses++;check(claim!=DensityVolumeBounds.Occupancy.EMPTY,"noise positive under false empty certificate");}
      if(value<0) check(claim!=DensityVolumeBounds.Occupancy.SOLID,"noise negative under false solid certificate");
    }
    check(positiveWitnesses>0,"positive noise witness exists");

    // Warp must enclose all possible displaced input coordinates, not just
    // sample the corners of the unwarped voxel. This case has a fixed +16 X shift.
    var warped=new Definition(1,"density",List.of(
        Node.operation("x",Op.X),Node.constant("half",.5),Node.constant("zero",0),
        new Node("density",Op.WARP,List.of("x","half","zero","zero"),List.of(32.),null)
    )).compile(1);
    var warpedBounds=new DensityVolumeBounds(warped);
    check(warpedBounds.classifyCells(0,0,0,1,1,1)==DensityVolumeBounds.Occupancy.SOLID,"widened positive X");
    check(warpedBounds.classifyCells(-100,0,0,1,1,1)==DensityVolumeBounds.Occupancy.EMPTY,"widened negative X");
    for(int x=-64;x<=64;x++)oracle(warped,warpedBounds,x,4,-1,2,2,2);

    // Cover non-monotone, clamped and convex operators with integer-box oracles.
    var absolute=new Definition(1,"density",List.of(
        Node.operation("x",Op.X),Node.operation("density",Op.ABS,"x"))).compile(0);
    var absoluteBounds=new DensityVolumeBounds(absolute);
    check(absoluteBounds.classify(-2,0,0,2,0,0)==DensityVolumeBounds.Occupancy.UNCERTAIN,
        "absolute value crosses zero inside box");
    var clamp=new Definition(1,"density",List.of(
        Node.operation("y",Op.Y),
        new Node("density",Op.CLAMP,List.of("y"),List.of(-2.,2.),null))).compile(0);
    var clampBounds=new DensityVolumeBounds(clamp);
    var step=new Definition(1,"density",List.of(
        Node.operation("y",Op.Y),
        new Node("density",Op.SMOOTHSTEP,List.of("y"),List.of(-2.,2.),null))).compile(0);
    var stepBounds=new DensityVolumeBounds(step);
    var lerp=new Definition(1,"density",List.of(
        Node.constant("left",-3),Node.constant("right",7),Node.operation("x",Op.X),
        Node.operation("density",Op.LERP,"left","right","x"))).compile(0);
    var lerpBounds=new DensityVolumeBounds(lerp);
    var maximum=new Definition(1,"density",List.of(
        Node.operation("x",Op.X),Node.operation("y",Op.Y),
        Node.operation("density",Op.MAX,"x","y"))).compile(0);
    var maximumBounds=new DensityVolumeBounds(maximum);
    for(int x=-10;x<10;x++)for(int y=-10;y<10;y++) {
      oracle(absolute,absoluteBounds,x,y,0,2,2,1);
      oracle(clamp,clampBounds,x,y,0,2,2,1);
      oracle(step,stepBounds,x,y,0,2,2,1);
      oracle(lerp,lerpBounds,x,y,0,2,2,1);
      oracle(maximum,maximumBounds,x,y,0,2,2,1);
    }

    // The real V8 recipe must never use an interpolated lattice sign as an
    // authoritative occupancy certificate. Sample several spatial/altitude bands.
    DensityGraph recipe=TerrainShapeV8.definition(false).compile(73);
    var recipeBounds=new DensityVolumeBounds(recipe);
    for(int i=0;i<40;i++) {
      int x=(i*317)%8000-4000,z=(i*787)%8000-4000;
      int y=switch(i%5) {case 0->-2048;case 1->-60;case 2->96;case 3->384;default->2048;};
      oracle(recipe,recipeBounds,x,y,z,3,3,3);
    }
    System.out.println("DENSITY_VOLUME_BOUNDS_PASS assertions="+assertions+" conservativeV8=true");
  }
  private DensityVolumeBoundsChecks() {}
}
