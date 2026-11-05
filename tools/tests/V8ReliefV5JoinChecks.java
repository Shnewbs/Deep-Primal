package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.density.DensityChunk;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.TerrainShapeV8;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Run against the REAL engine-core graph in a full Java25/Gradle checkout. */
public final class V8ReliefV5JoinChecks {
  private static long checks, changed, shore, joins, inland;
  private static void require(boolean ok,String message) {
    checks++;
    if (!ok) throw new AssertionError(message);
  }
  private static long bits(double v) { return Double.doubleToRawLongBits(v); }

  private static void survey(long seed, boolean floating) {
    DensityGraph.Definition original=TerrainShapeV8.definition(floating);
    DensityGraph.Definition v4=TerrainShapeV8.definitionReliefV4(floating);
    DensityGraph.Definition v5=TerrainShapeV8.definitionReliefV5(floating);
    require(!v5.equals(v4),"new recipe must be explicit and distinct");
    require(v5.nodes().stream().anyMatch(n->n.id().equals("relief_recipe_revision")
        && n.parameters().getFirst()==5.0),"saved graph must pin recipe version");
    require(v4.nodes().stream().anyMatch(n->n.id().equals("relief_recipe_revision")
        && n.parameters().getFirst()==4.0),"frozen V4 recipe marker");
    require(v5.nodes().stream().anyMatch(n->n.id().equals("relief5_before_canyon")),
        "new join must be consumed inside density graph");
    var old=original.compile(seed);
    var old4=v4.compile(seed);
    var now=v5.compile(seed);
    require(v5.nodes().size()<=DensityGraph.MAX_NODES,"graph node budget");
    require(now.depth()<=DensityGraph.MAX_DEPTH,"graph depth budget");
    require(now.maximumEvaluationCost()<=DensityGraph.MAX_COST,"graph evaluation budget");
    for (int z=-4096;z<=4096;z+=80) for(int x=-4096;x<=4096;x+=80) {
      double c=old.channel("continentalness",x,96,z);
      double previous=old4.channel("height",x,96,z);
      double current=now.channel("height",x,96,z);
      double historical=old.channel("height",x,96,z);
      require(Double.isFinite(c)&&Double.isFinite(previous)&&Double.isFinite(current),
          "nonfinite relief height at signed world coordinate");
      require((historical>96)==(current>96),"changed historical sea-plane side");
      require(bits(old.channel("canyon_cut",x,96,z))==
          bits(now.channel("canyon_cut",x,96,z)),"changed canyon incision");
      if(c<=.08) {
        shore++;
        require(bits(historical)==bits(current),"historical coastal V1 bits changed");
        require(bits(previous)==bits(current),"previous V4 protected coast drift");
      } else if(c<.68) {
        joins++;
        if(bits(previous)!=bits(current)) changed++;
      } else inland++;
      double side=now.channel("height",x+1,96,z);
      require(Double.isFinite(side)&&Math.abs(side-current)<24,
          "absolute-world neighbor impulse");
      double weight=now.channel("relief5_join_window",x,96,z);
      require(weight>=0 && weight<=1,"unbounded foothill/plateau overlap");
      double delta=now.channel("relief5_banded_joins",x,96,z);
      require(Math.abs(delta)<=11.000001,"unbounded new relief proposal");
    }
  }

  private static String signature(DensityChunk c) {
    long hash=0x9e3779b97f4a7c15L;
    for(int z=0;z<16;z++)for(int x=0;x<16;x++) {
      hash=Long.rotateLeft(hash^c.surfaceExclusive(x,z),7);
      for(int y=-64;y<96;y++)hash=Long.rotateLeft(hash^(c.solid(x,y,z)?1:0),1);
    }
    return Long.toUnsignedString(hash,16);
  }

  private static void chunkOrder(long seed) {
    var graph=TerrainShapeV8.definitionReliefV5(false).compile(seed);
    var coords=List.of(new int[]{-32,-16},new int[]{-16,-16},new int[]{0,-16},
        new int[]{-32,0},new int[]{-16,0},new int[]{0,0});
    List<String> ordered=new ArrayList<>();
    for(int[] p:coords)ordered.add(signature(DensityChunk.sample(graph,p[0],-64,p[1],160)));
    List<int[]> reversed=new ArrayList<>(coords);
    Collections.reverse(reversed);
    List<String> reversedHashes=new ArrayList<>();
    for(int[] p:reversed)reversedHashes.add(signature(DensityChunk.sample(graph,p[0],-64,p[1],160)));
    Collections.reverse(reversedHashes);
    require(ordered.equals(reversedHashes),"signed chunk-order parity");
    var futures=coords.stream().map(p->CompletableFuture.supplyAsync(
        ()->signature(DensityChunk.sample(graph,p[0],-64,p[1],160)))).toList();
    for(int i=0;i<coords.size();i++)
      require(ordered.get(i).equals(futures.get(i).join()),"parallel chunk occupancy parity");
  }

  public static void main(String[] args) {
    for (long seed:new long[]{0L,73L,-20261029L,Long.MIN_VALUE+31}) {
      survey(seed,false);
      chunkOrder(seed);
    }
    survey(73,true);
    require(shore>1000&&joins>100&&inland>10,"incomplete multi-climate relief survey");
    require(changed>100,"new relief recipe has no actual join geometry");
    System.out.println("V8_RELIEF_V5_GRAPH_PASS assertions="+checks+
        " changed="+changed+" coast="+shore+" joins="+joins+" inland="+inland+
        " signedChunkParity=true native=false realMinecraft=false");
  }
}
