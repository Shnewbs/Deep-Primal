import com.shnewbs.deepprimal.core.WatershedInteriorPortRoutes;
import com.shnewbs.deepprimal.core.WatershedOutletTopology;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class WatershedInteriorPortRoutesChecks {
  private static long checks;
  private static void check(boolean yes, String what) {
    checks++;
    if (!yes) throw new AssertionError(what);
  }
  private static void reject(Runnable f, String what) {
    try { f.run(); }
    catch (IllegalArgumentException expected) { checks++; return; }
    throw new AssertionError("accepted malformed: " + what);
  }
  private static int at(int x, int z, int side) { return z * side + x; }
  private static Fixture fixture(int side) {
    int[] p = new int[side * side];
    double[] g = new double[p.length], s = new double[p.length];
    Arrays.fill(p, -1); Arrays.fill(g, 82); Arrays.fill(s, 96);
    return new Fixture(side,g,s,p);
  }
  private record Fixture(int side,double[] ground,double[] stage,int[] parent) {
    List<WatershedInteriorPortRoutes.Route> run(int x,int z) {
      return WatershedInteriorPortRoutes.trace(x,z,side,32,ground,stage,parent);
    }
  }
  public static void main(String[] args) throws Exception {
    var small = fixture(5);
    small.parent[at(0,1,5)] = at(1,1,5);
    small.parent[at(0,2,5)] = at(1,1,5);
    small.parent[at(1,1,5)] = at(2,1,5);
    small.parent[at(2,1,5)] = at(3,1,5);
    small.parent[at(3,1,5)] = at(4,1,5);
    small.ground[at(2,1,5)] = 90;
    var routes=small.run(0,0);
    check(routes.size()==2,"two shared interior receiver routes");
    var first=routes.get(0);
    check(first.fromX()==0&&first.fromZ()==32&&first.toX()==128&&first.toZ()==32,
        "first source reaches first downstream perimeter, not arbitrary ocean");
    check(first.firstReceiverKey()==WatershedOutletTopology.key(32,32),
        "first parent bound exactly to observed source");
    check(first.hops()==4&&first.crest()==90,"4 hops with maximum intervening bed crest");
    check(routes.get(1).hops()==4 && routes.get(1).toX()==128,
        "memoized tributary shares the same exit without duplicate source");
    check(routes.equals(small.run(0,0)),"bit-exact same-source replay");
    var signed=small.run(-128,-128);
    check(signed.get(0).fromX()==-128&&signed.get(0).toX()==0
        &&signed.get(0).fromZ()==-96,"negative world coordinates exact");
    check(routes.size()==small.run(0,0).size(),"signed translation does not change routing");
    var original=small.parent.clone();
    check(Arrays.equals(original,small.parent),"input receivers never mutated");
    var stop=fixture(5);
    stop.parent[at(0,1,5)]=at(1,1,5);
    check(stop.run(0,0).isEmpty(),"inland sink cannot invent a boundary outlet");
    var cyclic=fixture(5);
    cyclic.parent[at(0,1,5)]=at(1,1,5);
    cyclic.parent[at(1,1,5)]=at(2,1,5);
    cyclic.parent[at(2,1,5)]=at(1,1,5);
    reject(()->cyclic.run(0,0),"interior two-node receiver cycle");
    var isolated = fixture(5);
    isolated.parent[at(1,2,5)] = at(2,2,5);
    isolated.parent[at(2,2,5)] = at(1,2,5);
    reject(()->isolated.run(0,0),"disconnected two-node interior cycle");
    reject(()->isolated.run(-128,-128),"disconnected cycle across signed origin");
    var perimeterCycle = fixture(5);
    perimeterCycle.parent[at(0,2,5)] = at(1,2,5);
    perimeterCycle.parent[at(1,2,5)] = at(0,2,5);
    reject(()->perimeterCycle.run(0,0),"perimeter through interior back to itself");
    var boundaryCycle = fixture(5);
    boundaryCycle.parent[at(0,0,5)] = at(1,0,5);
    boundaryCycle.parent[at(1,0,5)] = at(0,0,5);
    reject(()->boundaryCycle.run(0,0),"perimeter-to-perimeter cycle");
    var safeUnused = fixture(5);
    safeUnused.parent[at(1,2,5)] = at(2,2,5);
    check(safeUnused.run(0,0).isEmpty(),
        "unconnected acyclic interior path remains a legitimate empty route");
    var illegal=fixture(5);
    illegal.parent[at(0,1,5)]=at(3,1,5);
    reject(()->illegal.run(0,0),"jump over interior cells");
    illegal.parent[at(0,1,5)]=-1;
    illegal.parent[at(1,1,5)]=at(1,1,5);
    reject(()->illegal.run(0,0),"unobserved self parent");
    illegal.parent[at(1,1,5)]=-1;
    illegal.parent[at(0,1,5)]=at(1,1,5);
    illegal.stage[at(1,1,5)]=96.5;
    reject(()->illegal.run(0,0),"downhill receiver stage required");
    illegal.stage[at(1,1,5)]=96;
    illegal.ground[at(1,1,5)]=Double.NaN;
    reject(()->illegal.run(0,0),"nonfinite interior floor");
    reject(()->small.run(Integer.MAX_VALUE-31,0),"world coordinate overflow");
    reject(()->WatershedInteriorPortRoutes.trace(0,0,5,16,small.ground,small.stage,small.parent),
        "unsupported lattice spacing");
    var wide=fixture(257);
    for(int x=0;x<256;x++) wide.parent[at(x,1,257)]=at(x+1,1,257);
    var path=wide.run(0,0);
    check(path.size()==1&&path.get(0).hops()==256&&path.get(0).toX()==8192,
        "real 8K-wide interior path and bounded memoized traversal");
    check(path.get(0).crest()==82,"no synthetic extra sill");
    try(var executor=Executors.newFixedThreadPool(4)) {
      java.util.ArrayList<Future<List<WatershedInteriorPortRoutes.Route>>> jobs=new java.util.ArrayList<>();
      for(int i=0;i<16;i++)jobs.add(executor.submit(()->wide.run(-8192,-8192)));
      var expected=wide.run(-8192,-8192);
      for(var task:jobs)check(expected.equals(task.get()),"parallel source replay");
    }
    for(int i=0;i<1000;i++)
      check(small.run(0,0).equals(routes),"stable repeated observations no state leakage");
    System.out.println("V8_INTERIOR_ROUTES_PASS assertions="+checks+
        " routed="+routes.size()+" wideHops="+path.get(0).hops());
  }
}
