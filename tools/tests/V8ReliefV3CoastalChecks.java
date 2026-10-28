package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.density.DensityChunk;
import com.shnewbs.deepprimal.core.density.TerrainShapeV8;
import java.util.Map;
import java.util.Set;

/** Source-level regression for opt-in relief V3. Not a native sea-mask check. */
public final class V8ReliefV3CoastalChecks {
  private static long assertions;
  private static void check(boolean valid, String message) {
    assertions++;
    if (!valid) throw new AssertionError(message);
  }

  private static void survey(long seed) {
    var v1=TerrainShapeV8.definition(false).compile(seed);
    var v2=TerrainShapeV8.definitionReliefV2(false).compile(seed);
    var v3=TerrainShapeV8.definitionReliefV3(false).compile(seed);
    long coastCount=0, inlandCount=0, transitionCount=0, v2CoastMismatch=0;
    double largestAdjacent=0;
    for (int z=-6144;z<=6144;z+=64) for (int x=-6144;x<=6144;x+=64) {
      double c=v1.channel("continentalness",x,96,z);
      double before=v1.channel("height",x,96,z);
      double unguarded=v2.channel("height",x,96,z);
      double after=v3.channel("height",x,96,z);
      check(Double.isFinite(c)&&Double.isFinite(before)&&Double.isFinite(unguarded)
          &&Double.isFinite(after),"finite world-coordinate columns");
      check(Double.doubleToRawLongBits(v1.channel("canyon_cut",x,96,z))==
          Double.doubleToRawLongBits(v3.channel("canyon_cut",x,96,z)),
          "incision input must remain untouched");
      check(Math.abs(after-before)<260,"bounded signed relief");
      if (Math.abs(c)<.08) {
        coastCount++;
        check(Double.doubleToRawLongBits(before)==Double.doubleToRawLongBits(after),
            "coast V1 bit-level height must be preserved seed="+seed+" x="+x+" z="+z);
        check((before>96)==(after>96),"coast sea-plane side must remain unchanged");
        if ((before>96)!=(unguarded>96))v2CoastMismatch++;
      }
      if (c>=.16) {
        inlandCount++;
        check(Double.doubleToRawLongBits(unguarded)==Double.doubleToRawLongBits(after),
            "inland relief-2 values must be unchanged");
      }
      if (c>.08&&c<.16) {
        transitionCount++;
        check(after>=Math.min(before,unguarded)-1e-8 &&
            after<=Math.max(before,unguarded)+1e-8,
            "relief shoulder must remain between historical and V2 heights");
      }
      double adjacent=v3.channel("height",x+1,96,z);
      largestAdjacent=Math.max(largestAdjacent,Math.abs(after-adjacent));
      check(Double.isFinite(adjacent)&&Math.abs(after-adjacent)<24,
          "absolute-coordinate guard must have no impulse cliff");
    }
    check(coastCount>1000,"must observe substantial nearshore corpus");
    check(inlandCount>10,"must observe substantial inland V2 parity corpus");
    check(transitionCount>200,"must exercise the smooth transition band");
    check(v2CoastMismatch>0,"must reproduce the audited V2 sea-plane mismatch");
    System.out.println("COAST_SURVEY seed="+seed+" coast="+coastCount+
        " unguardedFlips="+v2CoastMismatch+" transition="+transitionCount+
        " inland="+inlandCount+" adjacentMax="+largestAdjacent);
  }

  @SuppressWarnings("unchecked")
  private static void saveReplay(long seed, boolean floating) {
    var v3=V8GeneratorSnapshot.newReliefV3(seed,-64,192,
        new GroundSurfaceProfile("height",5),
        new V8BlendPolicy(1,EcotoneBlend.Widths.PREVIEW),V8SurfacePolicy.FROZEN_R1,floating);
    var v2=V8GeneratorSnapshot.newReliefV2(seed,-64,192,
        new GroundSurfaceProfile("height",5),
        new V8BlendPolicy(1,EcotoneBlend.Widths.PREVIEW),V8SurfacePolicy.FROZEN_R1,floating);
    check(v3.version()==V8GeneratorSnapshot.SURFACE_VERSION,"V3 requires explicit revision-3 envelope");
    check(!v3.density().equals(v2.density()),"V2 saved definition must not change");
    check(v2.density().equals(TerrainShapeV8.definitionReliefV2(floating)),
        "historical V2 factory graph is frozen");
    Map<String,Object> wire=(Map<String,Object>)V8GeneratorSnapshot.CODEC.encode(v3);
    check(wire.keySet().equals(Set.of("version","seed","min_y","height","density","ground","blend","surface")),
        "no silent policy migration or additional wire fields");
    var resumed=V8GeneratorSnapshot.CODEC.decode(wire);
    check(v3.equals(resumed),"V3 saved graph must reopen without recipe substitution");
    var fresh=v3.prepare().surface().graph();
    var reopened=resumed.prepare().surface().graph();
    for(int z=-16;z<=16;z+=16)for(int x=-32;x<=32;x+=32){
      var a=DensityChunk.sample(fresh,x,-64,z,192);
      var b=DensityChunk.sample(reopened,x,-64,z,192);
      for (int zz=0;zz<16;zz++)for(int xx=0;xx<16;xx++) {
        check(a.surfaceExclusive(xx,zz)==b.surfaceExclusive(xx,zz),
            "signed chunk generation/reopen height parity");
        for(int y=-64;y<128;y++)check(a.solid(xx,y,zz)==b.solid(xx,y,zz),
            "signed chunk generation/reopen voxel parity");
      }
    }
  }

  public static void main(String[] args) {
    for (long seed:new long[]{0,73,-20261028,Long.MIN_VALUE+37})survey(seed);
    var old=TerrainShapeV8.definitionReliefV2(false);
    var after=TerrainShapeV8.definitionReliefV3(false);
    check(old.nodes().stream().anyMatch(n->n.id().equals("relief_recipe_revision")&&n.parameters().getFirst()==2),
        "V2 graph revision marker remains 2");
    check(after.nodes().stream().anyMatch(n->n.id().equals("relief_recipe_revision")&&n.parameters().getFirst()==3),
        "V3 graph persistently marks revision 3");
    var legacy=TerrainShapeV8.definition(false).compile(0);
    var fixed=TerrainShapeV8.definitionReliefV3(false).compile(0);
    check(legacy.channel("continentalness",4279,96,579)>.008 &&
        legacy.channel("continentalness",4279,96,579)<.009,"audit witness remains nearshore");
    check(Double.doubleToRawLongBits(legacy.channel("height",4279,96,579))==
        Double.doubleToRawLongBits(fixed.channel("height",4279,96,579)),
        "audit witness coastal classification restored bit-exact");
    for(long seed:new long[]{0,73})for(boolean floating:new boolean[]{false,true})saveReplay(seed,floating);
    System.out.println("V8_RELIEF_V3_COAST_PASS assertions="+assertions+
        " optInOnly=true oldGraphsFrozen=true realMinecraft=false");
  }
}
