import com.shnewbs.deepprimal.core.WatershedOutletTopology;
import com.shnewbs.deepprimal.core.WatershedRegion;
import com.shnewbs.deepprimal.core.WatershedRegionPortBridge;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class WatershedRegionPortBridgeChecks {
  private static long checks;
  private static void check(boolean expected, String message) {
    checks++;
    if (!expected) throw new AssertionError(message);
  }
  private static void reject(Runnable work, String message) {
    try { work.run(); }
    catch (IllegalArgumentException expected) { checks++; return; }
    throw new AssertionError("accepted invalid " + message);
  }
  private static WatershedRegion.BoundarySnapshot region(int ox,int oz,
      int biasX, int biasZ, boolean ocean) {
    int side=WatershedRegion.SIDE, step=WatershedRegion.STEP;
    List<WatershedRegion.BoundaryNode> nodes=new ArrayList<>();
    for(int z=0;z<side;z++)for(int x=0;x<side;x++) {
      if(x!=0&&z!=0&&x!=side-1&&z!=side-1)continue;
      int wx=ox+x*step,wz=oz+z*step;
      double floor= 82 + (Math.floorMod(wx, 512)/128)*.25
          + (Math.floorMod(wz,256)/64)*.125 + (wx==biasX&&wz==biasZ?1:0);
      nodes.add(new WatershedRegion.BoundaryNode(wx,wz,floor,floor,
          96,1024,ocean,false,null));
    }
    return new WatershedRegion.BoundarySnapshot(ox,oz,List.copyOf(nodes));
  }
  private static String signature(WatershedRegionPortBridge.Draft d) {
    return d.ports().toString()+d.links().toString()+d.sharedCoordinates()+d.unresolvedFrontiers();
  }
  public static void main(String[] args) throws Exception {
    var a=region(0,0,Integer.MIN_VALUE,Integer.MIN_VALUE,false);
    var b=region(8192,0,Integer.MIN_VALUE,Integer.MIN_VALUE,false);
    var d=WatershedRegionPortBridge.assemble(List.of(a,b),Set.of());
    check(d.sharedCoordinates()==WatershedRegion.SIDE,"shared seam exact");
    check(d.ports().size()==2*4*(WatershedRegion.SIDE-1)-WatershedRegion.SIDE,"dedup");
    check(d.unresolvedFrontiers()>0,"external open frontiers");
    check(d.links().size()>1000,"real lattice perimeter links");
    var solution=d.solve(96);
    check(solution.totalRunoffQuanta()==0&&solution.terminalRunoffQuanta()==0,
        "cumulative area never fabricated into physical runoff");
    for(var entry:solution.outlets().entrySet()) {
      check(entry.getValue().status()==WatershedOutletTopology.Status.UNRESOLVED,
          "without anchors no closed/known marine claim");
      check(entry.getValue().accumulatedRunoffQuanta()==0,"no invented volume");
    }
    check(signature(d).equals(signature(WatershedRegionPortBridge.assemble(List.of(b,a),Set.of()))),
        "reversed region order parity");
    var neg1=region(-8192,-8192,Integer.MIN_VALUE,Integer.MIN_VALUE,true);
    var neg2=region(0,-8192,Integer.MIN_VALUE,Integer.MIN_VALUE,true);
    var neg=WatershedRegionPortBridge.assemble(List.of(neg2,neg1),Set.of());
    check(neg.sharedCoordinates()==257,"signed negative seam");
    long seamKey=WatershedOutletTopology.key(0,-8192+32);
    var certified=WatershedRegionPortBridge.assemble(List.of(neg1,neg2),Set.of(seamKey));
    check(certified.solve(96).at(0,-8160).status()==WatershedOutletTopology.Status.OCEAN,
        "explicit verified sea root reaches perimeter");
    reject(()->WatershedRegionPortBridge.assemble(List.of(neg1),Set.of(seamKey)),
        "unwitnessed marine anchor");
    reject(()->WatershedRegionPortBridge.assemble(List.of(a,b),Set.of(seamKey)),
        "unobserved marine key");
    reject(()->WatershedRegionPortBridge.assemble(List.of(a,b),Set.of(
        WatershedOutletTopology.key(8192,32))),"nonmarine label");
    reject(()->WatershedRegionPortBridge.assemble(List.of(a,b,b),Set.of()),
        "duplicate region");
    var skew=region(8192,0,8192,32,false);
    reject(()->WatershedRegionPortBridge.assemble(List.of(a,skew),Set.of()),
        "changed height on shared exact lattice");
    var bad=new WatershedRegion.BoundarySnapshot(0,0,a.nodes().subList(0,800));
    reject(()->WatershedRegionPortBridge.assemble(List.of(bad),Set.of()),
        "truncated perimeter");
    reject(()->WatershedRegionPortBridge.assemble(List.of(),Set.of()),
        "empty topology");
    for(int k=0;k<96;k++) {
      int x=(k%12-6)*8192,z=(k/12-4)*8192;
      var left=region(x,z,Integer.MIN_VALUE,Integer.MIN_VALUE,false);
      var right=region(x+8192,z,Integer.MIN_VALUE,Integer.MIN_VALUE,false);
      var sample=WatershedRegionPortBridge.assemble(List.of(left,right),Set.of());
      check(sample.sharedCoordinates()==257,"randomized seam copies");
      check(sample.unresolvedFrontiers()>0,"finite graph not globally closed");
      check(sample.solve(96).totalRunoffQuanta()==0,"runoff budget");
    }
    try(var pool=Executors.newFixedThreadPool(4)) {
      List<Future<String>> jobs=new ArrayList<>();
      for(int i=0;i<16;i++)jobs.add(pool.submit(()->signature(
          WatershedRegionPortBridge.assemble(List.of(a,b),Set.of()))));
      for(Future<String> job:jobs)check(job.get().equals(signature(d)),"parallel deterministic");
    }
    System.out.println("V8_REGION_PORT_BRIDGE_PASS assertions="+checks+
        " seamPorts="+d.ports().size()+" seamLinks="+d.links().size()+
        " shared="+d.sharedCoordinates()+" unresolved="+d.unresolvedFrontiers());
  }
}
