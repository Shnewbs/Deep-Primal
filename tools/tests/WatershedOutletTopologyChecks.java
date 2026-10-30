package com.shnewbs.deepprimal.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/** Port-level exact-rationality regression; deliberately no API doubles. */
public final class WatershedOutletTopologyChecks {
  private static long checks;
  private WatershedOutletTopologyChecks() {}
  private static void check(boolean value, String reason) {
    checks++;
    if (!value) throw new AssertionError(reason + " at " + checks);
  }
  private static void reject(Runnable run, String reason) {
    try {run.run();} catch (IllegalArgumentException | ArithmeticException expected) {
      checks++; return;
    }
    throw new AssertionError("failed to reject " + reason);
  }
  private static WatershedOutletTopology.Port port(int x,int z,double floor,long runoff,
      boolean ocean,boolean unknown) {
    return new WatershedOutletTopology.Port(x,z,floor,runoff,ocean,unknown);
  }
  private static WatershedOutletTopology.Link link(int x1,int z1,int x2,int z2,double sill) {
    return new WatershedOutletTopology.Link(x1,z1,x2,z2,sill);
  }
  private static void oceanAndSpill() {
    var ports = List.of(port(8192,0,83,1,true,false),port(8160,0,87,7,false,false),
        port(8128,0,76,13,false,false),port(8096,0,82,19,false,false));
    var links = List.of(link(8192,0,8160,0,92),link(8160,0,8128,0,93),
        link(8128,0,8096,0,118));
    var sol = WatershedOutletTopology.solve(ports,links,96);
    check(sol.totalRunoffQuanta()==40 && sol.terminalRunoffQuanta()==40,"exact source conservation");
    check(sol.portCount()==4 && sol.linkCount()==3,"topology size");
    check(sol.at(8128,0).status()==WatershedOutletTopology.Status.OCEAN,"low pass marine");
    check(sol.at(8128,0).minimumSpillLevel()==96,"sea-height not cumulative surface");
    check(sol.at(8096,0).status()==WatershedOutletTopology.Status.INLAND_SPILL,
        "upland lake must overtop 118 sill before ocean");
    check(sol.at(8096,0).minimumSpillLevel()==118,"exact lake spill level");
    check(sol.at(8192,0).accumulatedRunoffQuanta()==40,"quanta all reach sole ocean outlet");
    check(sol.at(8128,0).accumulatedRunoffQuanta()==32,"no duplicate branch supply");
    check(sol.at(8096,0).receiverKey()!=null,"lake has explicit receiver");
    check(sol.at(8192,0).receiverKey()==null,"marine terminal");
    for (int i=0;i<300;i++) {
      var shuffledPorts = new ArrayList<>(ports);
      var shuffledLinks = new ArrayList<>(links);
      Collections.shuffle(shuffledPorts,new Random(i*19L));
      Collections.shuffle(shuffledLinks,new Random(~i*31L));
      if ((i & 1)==0) {
        for(int j=0;j<shuffledLinks.size();j++) {
          var e=shuffledLinks.get(j);
          shuffledLinks.set(j,link(e.bx(),e.bz(),e.ax(),e.az(),e.sill()));
        }
      }
      check(sol.equals(WatershedOutletTopology.solve(shuffledPorts,shuffledLinks,96)),
          "reversed ports, edges and execution must be bit exact");
    }
  }
  private static void inlandAndUncertain() {
    var b1=port(-8192,-8192,-900,8,false,false);
    var b2=port(-8160,-8192,-800,5,false,false);
    var edge=link(-8192,-8192,-8160,-8192,-700);
    var closed=WatershedOutletTopology.solve(List.of(b1,b2),List.of(edge),96);
    check(closed.at(-8192,-8192).status()==WatershedOutletTopology.Status.CLOSED,
        "extremely deep sealed inland lake must not become ocean");
    check(Double.isNaN(closed.at(-8160,-8192).minimumSpillLevel()),"no fabricated spill");
    check(closed.terminalRunoffQuanta()==13,"retained source quanta at closed domain");
    var uncertain=WatershedOutletTopology.solve(List.of(b1,
        port(-8160,-8192,-800,5,false,true)),List.of(edge),96);
    check(uncertain.at(-8192,-8192).status()==WatershedOutletTopology.Status.UNRESOLVED,
        "unknown cross-region boundary taints entire water component");
    var withOcean=WatershedOutletTopology.solve(List.of(b1,
        port(-8160,-8192,-800,5,true,true)),List.of(edge),96);
    check(withOcean.at(-8192,-8192).status()==WatershedOutletTopology.Status.OCEAN,
        "explicit valid marine corridor can prove connectivity despite another unknown exit");
    check(withOcean.at(-8160,-8192).accumulatedRunoffQuanta()==13,"negative seam accounted");
  }
  private static void portGraphRunoff() {
    // Join and split geometry collapses into one deterministic receiver per
    // port. Multiple basins, roots and crossings cannot clone any quanta.
    Random random=new Random(0x7501E7L);
    for(int run=0;run<150;run++) {
      int rows=7,cols=8;
      var ports=new ArrayList<WatershedOutletTopology.Port>();
      var links=new ArrayList<WatershedOutletTopology.Link>();
      long input=0;
      int baseX=run%2==0?8192:-8192;
      for(int z=0;z<rows;z++) for(int x=0;x<cols;x++) {
        long supply=random.nextInt(100000);
        input+=supply;
        ports.add(port(baseX+x*32,z*32,60+random.nextInt(65),supply,
            x==0 && z==0,false));
        if(x>0)links.add(link(baseX+x*32,z*32,baseX+(x-1)*32,z*32,random.nextInt(125)));
        if(z>0)links.add(link(baseX+x*32,z*32,baseX+x*32,(z-1)*32,random.nextInt(125)));
      }
      // Ensure anchor's floor is always beneath the sea stage.
      ports.set(0,port(baseX,0,40,ports.get(0).runoffQuanta(),true,false));
      var sol=WatershedOutletTopology.solve(ports,links,96);
      check(sol.totalRunoffQuanta()==input,"input sum");
      check(sol.terminalRunoffQuanta()==input,"terminal sum");
      for(var node:ports) {
        var observation=sol.at(node.x(),node.z());
        check(observation.accumulatedRunoffQuanta()>=node.runoffQuanta(),
            "no constituent-free runoff loss");
        check(observation.status()!=WatershedOutletTopology.Status.CLOSED,
            "connected component sees explicit marine anchor");
      }
      var rports=new ArrayList<>(ports);
      var rlinks=new ArrayList<>(links);
      Collections.reverse(rports); Collections.reverse(rlinks);
      check(sol.equals(WatershedOutletTopology.solve(rports,rlinks,96)),"reverse-order parity");
    }
  }
  private static void deterministicTies() {
    var ports=List.of(port(0,0,70,9,false,false),port(-32,0,70,1,true,false),
        port(32,0,70,2,true,false),port(0,32,70,3,false,false));
    var links=List.of(link(0,0,-32,0,70),link(0,0,32,0,70),link(0,0,0,32,70));
    var result=WatershedOutletTopology.solve(ports,links,96);
    check(result.at(0,0).receiverKey().equals(WatershedOutletTopology.key(-32,0)),
        "equal spill/hop picks stable signed anchor");
    check(result.terminalRunoffQuanta()==15,"multi-root runoff exactly conserved");
    for(int trial=0;trial<512;trial++) {
      var reordered=new ArrayList<>(links);
      Collections.shuffle(reordered,new Random(trial));
      check(result.equals(WatershedOutletTopology.solve(ports,reordered,96)),
          "tie choice independent of HashMap traversal");
    }
    var futures = new ArrayList<CompletableFuture<WatershedOutletTopology.Solution>>();
    for(int i=0;i<16;i++) futures.add(CompletableFuture.supplyAsync(
        () -> WatershedOutletTopology.solve(ports,links,96)));
    for(var future:futures) check(result.equals(future.join()),"parallel strict parity");
    var canonical = new ArrayList<>(result.outlets().keySet());
    var sorted = new ArrayList<>(canonical); Collections.sort(sorted);
    check(canonical.equals(sorted),"canonical output iteration order");
  }
  private static void invalidInputs() {
    var inland=port(0,0,88,1,false,false);
    reject(()->WatershedOutletTopology.solve(List.of(inland,inland),List.of(),96),"duplicate port");
    reject(()->WatershedOutletTopology.solve(List.of(port(1,0,80,0,false,false)),List.of(),96),
        "unaligned coordinate");
    reject(()->WatershedOutletTopology.solve(List.of(port(0,0,Double.NaN,0,false,false)),
        List.of(),96),"nonfinite floor");
    reject(()->WatershedOutletTopology.solve(List.of(port(0,0,80,-1,false,false)),
        List.of(),96),"negative runoff");
    reject(()->WatershedOutletTopology.solve(List.of(port(0,0,100,0,true,false)),
        List.of(),96),"ocean anchor above sea");
    reject(()->WatershedOutletTopology.solve(List.of(inland),List.of(link(0,0,32,0,75)),96),
        "missing neighbor cannot be assumed traversable");
    reject(()->WatershedOutletTopology.solve(List.of(inland),List.of(link(0,0,0,0,75)),96),
        "self edge");
    reject(()->WatershedOutletTopology.solve(List.of(inland,port(32,0,70,0,false,false)),
        List.of(link(0,0,32,0,70),link(32,0,0,0,70)),96),"bidirectional duplicate");
    reject(()->WatershedOutletTopology.solve(List.of(inland,port(32,0,70,0,false,false)),
        List.of(link(0,0,32,0,Double.POSITIVE_INFINITY)),96),"invalid sill");
    reject(()->WatershedOutletTopology.solve(List.of(inland),List.of(),Double.NaN),
        "nonfinite sea stage");
    reject(()->WatershedOutletTopology.solve(List.of(port(0,0,90,Long.MAX_VALUE,false,false),
        port(32,0,90,1,false,false)),List.of(),96),"input overflow");
    reject(()->WatershedOutletTopology.solve(List.of(port(0,0,90,Long.MAX_VALUE,true,false),
        port(32,0,90,1,false,false)),List.of(link(32,0,0,0,90)),96),"routed overflow");
    check(WatershedOutletTopology.key(Integer.MIN_VALUE,0)!=
        WatershedOutletTopology.key(Integer.MAX_VALUE-31,0),"signed coordinate packing");
    check(WatershedOutletTopology.solve(List.of(),List.of(),96).totalRunoffQuanta()==0,
        "empty admitted domain neutral");
  }
  public static void main(String[] args) {
    try {
      runChecks();
    } catch (RuntimeException failure) {
      // Valid fixture inputs must execute successfully. A production invariant
      // rejecting a mutated implementation is a test failure, not a JVM failure.
      throw new AssertionError("outlet fixture rejected valid input", failure);
    }
  }
  private static void runChecks() {
    oceanAndSpill();
    inlandAndUncertain();
    portGraphRunoff();
    deterministicTies();
    invalidInputs();
    System.out.println("WATERSHED_OUTLET_TOPOLOGY_PASS checks="+checks+
        " modes=marine,spill,closed,unresolved,runoff,seams,ordering,guards");
  }
}

