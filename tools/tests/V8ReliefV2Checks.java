package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.density.DensityChunk;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.TerrainShapeV8;
import java.util.Map;
import java.util.Set;

/** Opt-in V8 relief-recipe regression against production source, API doubles only. */
public final class V8ReliefV2Checks {
  private static long assertions;
  private static void check(boolean ok, String reason) {
    assertions++;
    if (!ok) throw new AssertionError(reason);
  }
  private static void reject(Runnable run, String reason) {
    boolean bad=false;
    try { run.run(); } catch (IllegalArgumentException | IllegalStateException expected) { bad=true; }
    check(bad,reason);
  }
  private static void compareDefinition(long seed, boolean floating) {
    var old = TerrainShapeV8.definition(floating);
    var next = TerrainShapeV8.definitionReliefV2(floating);
    check(old.nodes().stream().noneMatch(n->n.id().equals("relief_recipe_revision")),
        "old graph must never acquire relief revision nodes");
    check(next.nodes().stream().anyMatch(n->n.id().equals("relief_recipe_revision")),
        "new graph must persist its revision marker");
    Set<String> preserved = Set.of("canyon_cut","canyon_inland","erosion","continentalness",
        "overhang","island_density");
    for (var node : old.nodes()) if (preserved.contains(node.id()) &&
        (!node.id().equals("island_density") || floating)) {
      check(next.nodes().contains(node), "geology/canyon/island source node must be intact: "+node.id());
    }
    var legacy = old.compile(seed);
    var fresh = next.compile(seed);
    long reliefChanges = 0;
    for (int z=-2304;z<=2304;z+=64) for (int x=-2304;x<=2304;x+=64) {
      double a = legacy.channel("height",x,96,z), b = fresh.channel("height",x,96,z);
      check(Double.isFinite(a) && Double.isFinite(b), "relief must remain finite");
      check(Math.abs(a-b)<260, "bounded new relief departure; no impulse cliffs");
      if (Double.doubleToRawLongBits(a)!=Double.doubleToRawLongBits(b)) reliefChanges++;
      double canyonA=legacy.channel("canyon_cut",x,96,z);
      double canyonB=fresh.channel("canyon_cut",x,96,z);
      check(Double.doubleToRawLongBits(canyonA)==Double.doubleToRawLongBits(canyonB),
          "canyon incision remains identical input and last relief cut");
      double seamA=fresh.channel("height",x+1,96,z);
      check(Double.isFinite(seamA) && Math.abs(seamA-b)<24,
          "adjacent absolute signed columns must not have a discontinuous jump");
    }
    check(reliefChanges>200,"new recipe must actually alter landforms");
    double plateauWitness=0;
    for(int z=-4096;z<=4096;z+=256)for(int x=-4096;x<=4096;x+=256)
      plateauWitness=Math.max(plateauWitness,fresh.channel("plateau_shoulder",x,96,z));
    check(plateauWitness>.02,"saved relief recipe needs genuine plateau shoulders");
    for (int x : new int[]{-512,0,512}) for (int z:new int[]{-512,0,512}) {
      var first=next.compile(seed); var second=next.compile(seed);
      for (int y=-64;y<512;y+=24) check(
          Double.doubleToRawLongBits(first.sample(x,y,z))==Double.doubleToRawLongBits(second.sample(x,y,z)),
          "deterministic density replay for signed coordinates");
    }
  }
  @SuppressWarnings("unchecked") private static void persistenceAndParity() {
    for (long seed: new long[]{-73, 0, 20261028, Long.MIN_VALUE+31}) {
      var frozen = V8GeneratorSnapshot.newReliefV2(seed, 0, 128,
          new GroundSurfaceProfile("height",5),
          new V8BlendPolicy(1,EcotoneBlend.Widths.PREVIEW),
          V8SurfacePolicy.FROZEN_R1,false);
      check(frozen.version()==V8GeneratorSnapshot.SURFACE_VERSION,
          "explicit saved surface profile version");
      Object wire=V8GeneratorSnapshot.CODEC.encode(frozen);
      Map<String,Object> root=(Map<String,Object>)wire;
      check(root.keySet().equals(Set.of("version","seed","min_y","height","density","ground","blend","surface")),
          "exact serialized V8 rev3 envelope");
      var reopen=V8GeneratorSnapshot.CODEC.decode(wire);
      check(frozen.equals(reopen),"relief recipe profile must reopen exactly");
      var fresh=frozen.prepare().surface().graph();
      var resumed=reopen.prepare().surface().graph();
      for (int z=-64;z<=64;z+=32) for (int x=-64;x<=64;x+=32)
        for (int y=0;y<=128;y+=16) {
          check(Double.doubleToRawLongBits(fresh.sample(x,y,z))==
              Double.doubleToRawLongBits(resumed.sample(x,y,z)),
              "full-seed graph replay across V8 save/reopen");
        }
      var chunk=DensityChunk.sample(fresh,-32,0,-16,128);
      var restored=DensityChunk.sample(resumed,-32,0,-16,128);
      for (int z=0;z<16;z++) for (int x=0;x<16;x++) {
        check(chunk.surfaceExclusive(x,z)==restored.surfaceExclusive(x,z),
            "signed chunk height parity after saved reopen");
        for (int y=0;y<128;y++) check(chunk.solid(x,y,z)==restored.solid(x,y,z),
            "signed chunk voxel parity after saved reopen");
      }
      var old = new V8GeneratorSnapshot(1,seed,0,128,TerrainShapeV8.definition(false),
          new GroundSurfaceProfile("height",5));
      var olderWire=V8GeneratorSnapshot.CODEC.encode(old);
      check(old.equals(V8GeneratorSnapshot.CODEC.decode(olderWire)),
          "revision1 snapshot must not change or auto-migrate");
      reject(()->V8GeneratorSnapshot.newReliefV2(seed,0,0,
          new GroundSurfaceProfile("height",5),new V8BlendPolicy(1,EcotoneBlend.Widths.PREVIEW),
          V8SurfacePolicy.FROZEN_R1,false),"invalid new save profile height rejected");
    }
  }
  public static void main(String[] args) {
    for (long seed:new long[]{0L,73L,20261028L}) for(boolean floating:new boolean[]{false,true})
      compareDefinition(seed,floating);
    persistenceAndParity();
    System.out.println("V8_RELIEF_V2_PASS assertions="+assertions+
        " optInOnly=true legacyGraphFrozen=true realMinecraft=false");
  }
}
