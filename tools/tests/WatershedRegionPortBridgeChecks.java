import com.shnewbs.deepprimal.core.WatershedOutletTopology;
import com.shnewbs.deepprimal.core.WatershedRegion;
import com.shnewbs.deepprimal.core.WatershedRegionPortBridge;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.UnaryOperator;

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
  private static WatershedRegion.BoundarySnapshot change(
      WatershedRegion.BoundarySnapshot snapshot, int x, int z,
      UnaryOperator<WatershedRegion.BoundaryNode> modifier) {
    List<WatershedRegion.BoundaryNode> next = new ArrayList<>(snapshot.nodes());
    for (int i = 0; i < next.size(); i++) {
      var n = next.get(i);
      if (n.x() == x && n.z() == z) {
        next.set(i, modifier.apply(n));
        return new WatershedRegion.BoundarySnapshot(
            snapshot.originX(), snapshot.originZ(), List.copyOf(next));
      }
    }
    throw new AssertionError("missing fixture node");
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
    // Dev78: actual adjacent downstream perimeter parents, including diagonals,
    // now contribute physical spill links. A source with an interior receiver
    // must NOT be promoted to a fabricated boundary outlet.
    var alone=WatershedRegionPortBridge.assemble(List.of(a),Set.of());
    var diagonal=change(a,0,32,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),n.lake(),WatershedOutletTopology.key(32,0)));
    var diagonalDraft=WatershedRegionPortBridge.assemble(List.of(diagonal),Set.of());
    check(diagonalDraft.links().size()==alone.links().size()+1,
        "real diagonal downstream perimeter link must be exported");
    check(signature(diagonalDraft).equals(signature(
        WatershedRegionPortBridge.assemble(List.of(diagonal),Set.of()))),
        "receiver-link canonical replay");
    var interior=change(a,0,32,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),n.lake(),WatershedOutletTopology.key(32,32)));
    check(WatershedRegionPortBridge.assemble(List.of(interior),Set.of()).links().size()
        ==alone.links().size(),"interior receiver cannot invent boundary shortcut");
    // Dev80: a production-exported interior receiver path may connect two
    // perimeter ports. Ordinary assemble() stays perimeter-only and unchanged.
    var route=new com.shnewbs.deepprimal.core.WatershedInteriorPortRoutes.Route(
        0,32,8192,32,WatershedOutletTopology.key(32,32),90,256);
    var routed=new WatershedRegion.RoutingSnapshot(interior,List.of(route));
    var empty=new WatershedRegion.RoutingSnapshot(b,List.of());
    var planned=WatershedRegionPortBridge.assembleRouted(List.of(routed,empty),Set.of());
    check(planned.links().size()==WatershedRegionPortBridge.assemble(
        List.of(interior,b),Set.of()).links().size()+1,
        "one observed interior pass not guessed by original perimeter bridge");
    check(signature(planned).equals(signature(WatershedRegionPortBridge.assembleRouted(
        List.of(empty,routed),Set.of()))),"routed region reversal parity");
    check(planned.solve(96).totalRunoffQuanta()==0,"interior route adds no physical volume");
    // Dev82: the standalone region receiver DAG protects its interior, while
    // the bridge must ALSO protect the combined port receivers. Perimeter-only
    // acyclicity cannot see A -> interior -> B -> interior -> A. An undirected
    // spillway edge would otherwise conceal the physical direction cycle.
    var twoWay=change(interior,8192,32,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),n.lake(),WatershedOutletTopology.key(8160,32)));
    var backward=new com.shnewbs.deepprimal.core.WatershedInteriorPortRoutes.Route(
        8192,32,0,32,WatershedOutletTopology.key(8160,32),90,256);
    reject(()->WatershedRegionPortBridge.assembleRouted(List.of(
        new WatershedRegion.RoutingSnapshot(twoWay,List.of(route,backward))),Set.of()),
        "reciprocal collapsed interior receivers");
    var forward=new com.shnewbs.deepprimal.core.WatershedInteriorPortRoutes.Route(
        8192,32,8192,64,WatershedOutletTopology.key(8160,32),90,4);
    var routedChain=new WatershedRegion.RoutingSnapshot(twoWay,List.of(route,forward));
    var chain=WatershedRegionPortBridge.assembleRouted(List.of(routedChain),Set.of());
    check(chain.links().size()>WatershedRegionPortBridge.assemble(
        List.of(twoWay),Set.of()).links().size(),
        "acyclic observed interior receiver chain accepted");
    check(signature(chain).equals(signature(WatershedRegionPortBridge.assembleRouted(
        List.of(new WatershedRegion.RoutingSnapshot(twoWay,List.of(forward,route))),Set.of()))),
        "reverse interior route order keeps exact output");
    // Reversing route discovery must never evade admission.
    reject(()->WatershedRegionPortBridge.assembleRouted(List.of(
        new WatershedRegion.RoutingSnapshot(twoWay,List.of(backward,route))),Set.of()),
        "alternate interior order cannot evade cycle admission");
    reject(()->WatershedRegionPortBridge.assembleRouted(List.of(
        new WatershedRegion.RoutingSnapshot(interior,List.of(new
            com.shnewbs.deepprimal.core.WatershedInteriorPortRoutes.Route(
                0,32,8192,32,WatershedOutletTopology.key(64,32),90,256))),empty),Set.of()),
        "first receiver must match actual boundary producer");
    reject(()->WatershedRegionPortBridge.assembleRouted(List.of(
        new WatershedRegion.RoutingSnapshot(interior,List.of(new
            com.shnewbs.deepprimal.core.WatershedInteriorPortRoutes.Route(
                0,32,8192,32,WatershedOutletTopology.key(32,32),70,256))),empty),Set.of()),
        "interior pass crest below both admitted endpoints");
    reject(()->WatershedRegionPortBridge.assembleRouted(List.of(
        new WatershedRegion.RoutingSnapshot(interior,List.of(route,route)),empty),Set.of()),
        "one source cannot export two interior routes");
    reject(()->WatershedRegionPortBridge.assembleRouted(List.of(
        new WatershedRegion.RoutingSnapshot(interior,List.of(new
            com.shnewbs.deepprimal.core.WatershedInteriorPortRoutes.Route(
                0,32,4096,4096,WatershedOutletTopology.key(32,32),90,256))),empty),Set.of()),
        "interior destination is not an observed perimeter port");
    var badStage=change(interior,8192,32,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),100,
        n.catchmentArea(),n.ocean(),n.lake(),n.downstreamKey()));
    reject(()->WatershedRegionPortBridge.assembleRouted(List.of(
        new WatershedRegion.RoutingSnapshot(badStage,List.of(route)),empty),Set.of()),
        "interior receiver cannot raise downstream water stage");
    // Independent Dev78 Java25 audit: equal-stage reciprocal parents were
    // accepted and collapsed to one undirected edge. Direction is authoritative:
    // reject both a two-node cycle and a longer perimeter cycle.
    var reciprocal=change(diagonal,32,0,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),n.lake(),WatershedOutletTopology.key(0,32)));
    reject(()->WatershedRegionPortBridge.assemble(List.of(reciprocal),Set.of()),
        "equal-stage reciprocal perimeter parents");
    var triangle=change(diagonal,32,0,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),n.lake(),WatershedOutletTopology.key(0,0)));
    triangle=change(triangle,0,0,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),n.lake(),WatershedOutletTopology.key(0,32)));
    final var cycle3=triangle;
    reject(()->WatershedRegionPortBridge.assemble(List.of(cycle3),Set.of()),
        "three-node equal-stage perimeter receiver cycle");
    var negativeBase=region(-8192,-8192,Integer.MIN_VALUE,Integer.MIN_VALUE,false);
    var negativeForward=change(negativeBase,-8192,-8160,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),n.lake(),WatershedOutletTopology.key(-8160,-8192)));
    var negativeReciprocal=change(negativeForward,-8160,-8192,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),n.lake(),WatershedOutletTopology.key(-8192,-8160)));
    reject(()->WatershedRegionPortBridge.assemble(List.of(negativeReciprocal),Set.of()),
        "signed negative two-node receiver cycle");
    check(signature(diagonalDraft).equals(signature(
        WatershedRegionPortBridge.assemble(List.of(diagonal),Set.of()))),
        "one-way equal-stage receiver remains admitted");
    var inverted=change(diagonal,0,32,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),90,
        n.catchmentArea(),n.ocean(),n.lake(),n.downstreamKey()));
    reject(()->WatershedRegionPortBridge.assemble(List.of(inverted),Set.of()),
        "water stage increases along observed downstream parent");
    for (long invalid : new long[]{WatershedOutletTopology.key(96,0),
        WatershedOutletTopology.key(-32,32),WatershedOutletTopology.key(0,32),
        ((long)33<<32)|32L}) {
      var badReceiver=change(a,0,32,n->new WatershedRegion.BoundaryNode(
          n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
          n.catchmentArea(),n.ocean(),n.lake(),invalid));
      reject(()->WatershedRegionPortBridge.assemble(List.of(badReceiver),Set.of()),
          "invalid distant, external, self or non-lattice downstream parent");
    }
    var oceanParent=change(neg1,-8192,-8192,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),n.lake(),WatershedOutletTopology.key(-8160,-8192)));
    reject(()->WatershedRegionPortBridge.assemble(List.of(oceanParent),Set.of()),
        "ocean source must be a downstream root");
    var oceanLake=change(neg1,-8192,-8192,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),true,n.downstreamKey()));
    reject(()->WatershedRegionPortBridge.assemble(List.of(oceanLake),Set.of()),
        "ocean and lake classes mutually exclusive");
    var skew=change(b,8192,32,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground()+1,n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),n.lake(),n.downstreamKey()));
    reject(()->WatershedRegionPortBridge.assemble(List.of(a,skew),Set.of()),
        "changed height on shared exact lattice");
    // Dev76 independent audit: matching ground with conflicting stages (90/900)
    // silently produced identical ports and links. All water observations and
    // parent receivers at the same absolute node must be reconciled or reject.
    int sharedX=8192,sharedZ=32;
    var stage90=change(a,sharedX,sharedZ,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),90,90,n.catchmentArea(),n.ocean(),n.lake(),n.downstreamKey()));
    var stage900=change(b,sharedX,sharedZ,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),900,900,n.catchmentArea(),n.ocean(),n.lake(),n.downstreamKey()));
    reject(()->WatershedRegionPortBridge.assemble(List.of(stage90,stage900),Set.of()),
        "independent audit 90/900 shared flood/stage mismatch");
    reject(()->WatershedRegionPortBridge.assemble(List.of(stage900,stage90),Set.of()),
        "shared flood/stage mismatch reversed order");
    var floodChange=change(b,sharedX,sharedZ,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation()+0.5,n.plannedStage(),
        n.catchmentArea(),n.ocean(),n.lake(),n.downstreamKey()));
    reject(()->WatershedRegionPortBridge.assemble(List.of(a,floodChange),Set.of()),
        "flood-alone mismatch");
    var stageChange=change(b,sharedX,sharedZ,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage()+.5,
        n.catchmentArea(),n.ocean(),n.lake(),n.downstreamKey()));
    reject(()->WatershedRegionPortBridge.assemble(List.of(a,stageChange),Set.of()),
        "stage-alone mismatch");
    var catchmentChange=change(b,sharedX,sharedZ,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea()+32,n.ocean(),n.lake(),n.downstreamKey()));
    reject(()->WatershedRegionPortBridge.assemble(List.of(a,catchmentChange),Set.of()),
        "cumulative catchment mismatch");
    var oceanChange=change(b,sharedX,sharedZ,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),!n.ocean(),n.lake(),n.downstreamKey()));
    reject(()->WatershedRegionPortBridge.assemble(List.of(a,oceanChange),Set.of()),
        "ocean label mismatch without certified key");
    var lakeChange=change(b,sharedX,sharedZ,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),!n.lake(),n.downstreamKey()));
    reject(()->WatershedRegionPortBridge.assemble(List.of(a,lakeChange),Set.of()),
        "lake label mismatch without certified key");
    var receiverChange=change(b,sharedX,sharedZ,n->new WatershedRegion.BoundaryNode(
        n.x(),n.z(),n.ground(),n.floodedElevation(),n.plannedStage(),
        n.catchmentArea(),n.ocean(),n.lake(),
        WatershedOutletTopology.key(sharedX+32,sharedZ)));
    reject(()->WatershedRegionPortBridge.assemble(List.of(a,receiverChange),Set.of()),
        "divergent downstream parent at same lattice node");
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
