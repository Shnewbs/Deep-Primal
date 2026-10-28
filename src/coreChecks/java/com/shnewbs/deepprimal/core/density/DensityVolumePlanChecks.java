package com.shnewbs.deepprimal.core.density;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

/** Source-only coverage, topology, budget and exact-sample gates for experimental V8. */
public final class DensityVolumePlanChecks {
  private static long assertions;
  private static void check(boolean condition, String message) {
    assertions++;
    if (!condition) throw new AssertionError(message);
  }
  private static void reject(Runnable runnable) {
    try { runnable.run(); throw new AssertionError("invalid plan accepted"); }
    catch (IllegalArgumentException expected) { assertions++; }
  }

  private static void oracle(DensityGraph graph, DensityVolumeBounds bound,
      int x, int y, int z, int sx, int sy, int sz, int budget) {
    var result = DensityVolumePlan.plan(bound, x,y,z,sx,sy,sz,budget);
    check(result.queries() <= budget, "query budget");
    check(result.totalCells() == (long)sx*sy*sz, "conservation of covered cells");
    check(result.tiles().size() <= budget + 32, "bounded tile count");
    check(result.equals(DensityVolumePlan.plan(bound,x,y,z,sx,sy,sz,budget)),"determinism");
    var visited = new HashSet<Long>();
    long s=0,e=0,u=0;
    for(var t : result.tiles()) {
      long volume = t.cells();
      if (t.occupancy() == DensityVolumeBounds.Occupancy.SOLID) s+=volume;
      else if (t.occupancy() == DensityVolumeBounds.Occupancy.EMPTY) e+=volume;
      else u+=volume;
      for (int iz=0; iz<t.sizeZ(); iz++) for (int iy=0; iy<t.sizeY(); iy++)
          for (int ix=0; ix<t.sizeX(); ix++) {
        int xx=t.x()+ix, yy=t.y()+iy, zz=t.z()+iz;
        check(xx>=x&&xx<x+sx&&yy>=y&&yy<y+sy&&zz>=z&&zz<z+sz,"within input");
        long key=(((long)(xx-x)*sy)+(yy-y))*sz+(zz-z);
        check(visited.add(key),"no double coverage");
        double exact=graph.sample(xx,yy,zz);
        if(t.occupancy()==DensityVolumeBounds.Occupancy.SOLID)
          check(exact>0,"false SOLID proof");
        if(t.occupancy()==DensityVolumeBounds.Occupancy.EMPTY)
          check(exact<=0,"false EMPTY proof");
      }
    }
    check(visited.size()==(long)sx*sy*sz,"no missing cells");
    check(result.solidCells()==s && result.emptyCells()==e && result.uncertainCells()==u,
          "component volume sums");
  }

  public static void main(String[] args) {
    var linear = new Definition(1,"density",List.of(
        Node.constant("h",15),Node.operation("y",Op.Y),Node.constant("minus",-1),
        Node.operation("ny",Op.MULTIPLY,"y","minus"),
        Node.operation("density",Op.ADD,"h","ny"))).compile(1);
    var bound=new DensityVolumeBounds(linear);
    var exact=DensityVolumePlan.plan(bound,-8,0,-8,16,32,16,256);
    check(exact.solidCells()==16L*15*16,"exact positive halfspace");
    check(exact.emptyCells()==16L*16*16,"certified negative halfspace");
    check(exact.uncertainCells()==16L*16,"zero-density layer requires exact fallback");
    check(exact.queries()<=64,"Y-only source avoids 256 redundant X/Z probes");
    check(bound.coordinateMask()==2,"Y-only dependency");
    oracle(linear,bound,-8,0,-8,16,32,16,256);
    var noQueries=DensityVolumePlan.plan(bound,0,0,0,64,64,64,0);
    check(noQueries.tiles().size()==1 && noQueries.uncertainCells()==64L*64*64,
          "zero-budget fails open with one tile");
    check(noQueries.queries()==0,"zero-budget no probes");
    oracle(linear,bound,0,0,0,16,16,16,1);
    oracle(linear,bound,0,0,0,16,16,16,3);
    oracle(linear,bound,0,0,0,16,16,16,18);
    oracle(linear,bound,0,0,0,16,16,16,256);
    var far=DensityVolumePlan.plan(bound, -2,-200,-2,16,16,16,8);
    check(far.tiles().size()==1 && far.solidCells()==4096,"single solid tile");
    var air=DensityVolumePlan.plan(bound, -2,200,-2,16,16,16,8);
    check(air.tiles().size()==1 && air.emptyCells()==4096,"single empty tile");
    var noisy=new Definition(1,"density",List.of(
        Node.operation("y",Op.Y),Node.constant("minus",-1),
        Node.operation("ny",Op.MULTIPLY,"y","minus"),
        Node.constant("h",20),Node.operation("base",Op.ADD,"h","ny"),
        Node.noise("noise",new NoiseSpec(.027,.027,.027,3,2,.5,17),false),
        Node.operation("density",Op.ADD,"base","noise"))).compile(17);
    var noiseBounds=new DensityVolumeBounds(noisy);
    check(noiseBounds.coordinateMask()==7,"3D noise needs all axes");
    Random random=new Random(0x5261be8L);
    for(int k=0;k<28;k++) {
      int x=random.nextInt(80)-40,z=random.nextInt(80)-40,y=random.nextInt(72)-35;
      oracle(noisy,noiseBounds,x,y,z,8,12,8,10+k%5);
    }
    // A planar X field must not spend its budget on irrelevant Y/Z subdivisions.
    var xField=new Definition(1,"density",List.of(Node.operation("density",Op.X))).compile(0);
    var xBounds=new DensityVolumeBounds(xField);
    check(xBounds.coordinateMask()==1,"X-only dependency");
    var xPlan=DensityVolumePlan.plan(xBounds,-8,-8,-8,16,16,16,256);
    check(xPlan.queries()<=32,"X-only axis selection is bounded");
    oracle(xField,xBounds,-8,-8,-8,16,16,16,256);

    var zField=new Definition(1,"density",List.of(Node.operation("density",Op.Z))).compile(0);
    var zBounds=new DensityVolumeBounds(zField);
    check(zBounds.coordinateMask()==4,"Z-only dependency");
    oracle(zField,zBounds,-8,-8,-8,16,16,16,256);

    var flatNoise=new Definition(1,"density",List.of(
        Node.noise("density",new NoiseSpec(.027,.027,.027,3,2,.5,4),true))).compile(0);
    var flatBounds=new DensityVolumeBounds(flatNoise);
    check(flatBounds.coordinateMask()==5,"horizontal noise ignores Y");
    oracle(flatNoise,flatBounds,-8,-8,-8,8,8,8,32);

    var warped=new Definition(1,"density",List.of(
        Node.operation("x",Op.X),Node.operation("z",Op.Z),
        new Node("bounded_z",Op.CLAMP,List.of("z"),List.of(-.5,.5),null),
        Node.constant("zero",0),
        new Node("density",Op.WARP,List.of("x","bounded_z","zero","zero"),List.of(1.),null)
    )).compile(0);
    var warpBounds=new DensityVolumeBounds(warped);
    check(warpBounds.coordinateMask()==5,"warp displacement dependencies retained");
    oracle(warped,warpBounds,-8,-8,-8,16,4,16,128);

    // Extreme signed coordinates do not overflow split origins or logical counts.
    oracle(linear,bound,-29_999_990,-8,29_999_990,8,8,8,16);
    reject(()->DensityVolumePlan.plan(bound,0,0,0,0,1,1,1));
    reject(()->DensityVolumePlan.plan(bound,0,0,0,65,1,1,1));
    reject(()->DensityVolumePlan.plan(bound,0,0,0,1,1,1,-1));
    reject(()->DensityVolumePlan.plan(bound,0,0,0,1,1,1,257));
    reject(()->DensityVolumePlan.plan(bound,30_000_000,0,0,64,1,1,0));
    reject(()->DensityVolumePlan.plan(bound,Integer.MIN_VALUE,0,0,1,1,1,0));
    System.out.println("DENSITY_VOLUME_PLAN_PASS assertions="+assertions
        +" certified="+ (exact.solidCells()+exact.emptyCells())
        +" maxProbes="+DensityVolumePlan.MAX_QUERIES);
  }
  private DensityVolumePlanChecks() {}
}
