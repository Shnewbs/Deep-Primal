package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.density.DensityChunk;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.TerrainShapeV8;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

/** Opt-in height-channel coastal invariance; NOT native ocean-mask validation. */
public final class V8ReliefV4CoastalChecks {
  private static long assertions;
  private static long visited, protectedColumns, shoulderColumns, inlandColumns;
  private static long v3Flips, v4Flips, changedInland;

  private static void require(boolean success, String description) {
    assertions++;
    if (!success) throw new AssertionError(description);
  }

  private static long bits(double value) { return Double.doubleToRawLongBits(value); }

  private static String digest(DensityGraph.Definition d) {
    try {
      byte[] sha=MessageDigest.getInstance("SHA-256")
          .digest(d.toString().getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(sha);
    } catch (NoSuchAlgorithmException impossible) {
      throw new IllegalStateException(impossible);
    }
  }

  private static void frozenGraphs() {
    var expected = new String[][] {
      {"a98116149da0df3df44a8d48611b440605891c03c1ad7105488bf6a6ef7e0238",
       "b00e9e1d39c62a73217c0909472c7c37f6a0dd566c1ae57f61662c5527fa28c5",
       "ad622e30a9e53c9447ae94d7c01912e9bc1d1b9bd4b019f685d27410f3a519ae"},
      {"f0a3ca189a8fc5a19e43647dab6fa0b4164ef443818f7229d6c02615c3d8b74e",
       "31afc7047135e772bdbec2c09d0e457073dfb22ebf26237011ee8f0bf6a2d7f0",
       "b2428d5eb7062b8e58595a7cf5cd4867b6438d9dfaeb3f4296b195b5202b58ee"}
    };
    for (int fi=0;fi<2;fi++) {
      boolean floating=fi==1;
      DensityGraph.Definition[] versions={TerrainShapeV8.definition(floating),
          TerrainShapeV8.definitionReliefV2(floating),TerrainShapeV8.definitionReliefV3(floating)};
      for (int r=0;r<versions.length;r++)
        require(expected[fi][r].equals(digest(versions[r])),
            "frozen V1/V2/V3 density graph drift; floating="+floating+" rev="+(r+1));
      var v4=TerrainShapeV8.definitionReliefV4(floating);
      require(!digest(v4).equals(expected[fi][2]),"V4 must be a distinct saved graph");
      require(v4.nodes().stream().anyMatch(n->n.id().equals("relief_recipe_revision")
          && n.parameters().getFirst()==4),"persisted graph revision marker");
      require(v4.compile(0).depth()<=DensityGraph.MAX_DEPTH,"bounded graph depth");
      require(v4.compile(0).maximumEvaluationCost()<=DensityGraph.MAX_COST,
          "bounded graph work cost");
    }
  }

  private static void point(DensityGraph v1,DensityGraph v2,DensityGraph v3,DensityGraph v4,
      long seed,int x,int z) {
    double c=v1.channel("continentalness",x,96,z);
    double historical=v1.channel("height",x,96,z);
    double uplift=v2.channel("height",x,96,z);
    double oldShoulder=v3.channel("height",x,96,z);
    double guarded=v4.channel("height",x,96,z);
    visited++;
    require(Double.isFinite(c)&&Double.isFinite(historical)&&Double.isFinite(uplift)
        &&Double.isFinite(guarded),"finite height field");
    require((historical>96)==(guarded>96),
        "V4 sea-plane classification changed: seed="+seed+" x="+x+" z="+z+
        " c="+c+" v1="+historical+" v2="+uplift+" v4="+guarded);
    if ((historical>96)!=(oldShoulder>96)) v3Flips++;
    if ((historical>96)!=(guarded>96)) v4Flips++;
    require(bits(v1.channel("canyon_cut",x,96,z))==
        bits(v4.channel("canyon_cut",x,96,z)),"canyon incision invariant");
    if (c<=.08) {
      protectedColumns++;
      require(bits(historical)==bits(guarded),
          "V4 must preserve historical coastal density height through V3 zero-release band");
    } else {
      if(c>=.16) inlandColumns++; else shoulderColumns++;
      if(bits(historical)!=bits(guarded)) changedInland++;
      double radius=Math.max(Math.abs(historical-96),Math.abs(uplift-96));
      require(Math.abs(guarded-96)<=radius+1e-8,
          "sea-side transform must remain within bounded clearance envelope");
    }
    // The absolute-position neighbor sample must never jump impulsively.
    double next=v4.channel("height",x+1,96,z);
    require(Double.isFinite(next)&&Math.abs(next-guarded)<24,
        "V4 must remain continuous across adjacent world columns");
  }

  private static void sample(long seed) {
    var v1=TerrainShapeV8.definition(false).compile(seed);
    var v2=TerrainShapeV8.definitionReliefV2(false).compile(seed);
    var v3=TerrainShapeV8.definitionReliefV3(false).compile(seed);
    var v4=TerrainShapeV8.definitionReliefV4(false).compile(seed);
    for(int z=-6144;z<=6144;z+=64)
      for(int x=-6144;x<=6144;x+=64)
        point(v1,v2,v3,v4,seed,x,z);
    // This real, audited V3 shoulder witness was not in the coarse grid.
    if (seed==0) point(v1,v2,v3,v4,seed,-2006,-2282);
    if (seed==922337203685L) point(v1,v2,v3,v4,seed,9459,9273);
  }

  @SuppressWarnings("unchecked")
  private static void reopen(long seed,boolean floating) {
    var saved=V8GeneratorSnapshot.newReliefV4(seed,-64,192,
        new GroundSurfaceProfile("height",5),
        new V8BlendPolicy(1,EcotoneBlend.Widths.PREVIEW),
        V8SurfacePolicy.FROZEN_R1,floating);
    var old=V8GeneratorSnapshot.newReliefV3(seed,-64,192,
        new GroundSurfaceProfile("height",5),
        new V8BlendPolicy(1,EcotoneBlend.Widths.PREVIEW),
        V8SurfacePolicy.FROZEN_R1,floating);
    require(saved.version()==V8GeneratorSnapshot.SURFACE_VERSION,"saved revision-3 envelope");
    require(!saved.density().equals(old.density()),"V3 saved density never rewritten");
    Map<String,Object> wire=(Map<String,Object>)V8GeneratorSnapshot.CODEC.encode(saved);
    require(wire.keySet().equals(Set.of("version","seed","min_y","height",
        "density","ground","blend","surface")),"saved versioned policy fields");
    var parsed=V8GeneratorSnapshot.CODEC.decode(wire);
    require(parsed.equals(saved),"source modeled V8 profile reload exact");
    var a=saved.prepare().surface().graph();
    var b=parsed.prepare().surface().graph();
    for(int z=-32;z<=16;z+=16)for(int x=-32;x<=16;x+=16) {
      var ca=DensityChunk.sample(a,x,-64,z,192);
      var cb=DensityChunk.sample(b,x,-64,z,192);
      for(int dz=0;dz<16;dz++)for(int dx=0;dx<16;dx++) {
        require(ca.surfaceExclusive(dx,dz)==cb.surfaceExclusive(dx,dz),
            "prewrite/saved-profile height parity for signed chunk origins");
        for(int y=-64;y<128;y++)
          require(ca.solid(dx,y,dz)==cb.solid(dx,y,dz),
              "prewrite/saved-profile voxel parity");
      }
    }
  }

  public static void main(String[] args) {
    frozenGraphs();
    for(long seed:new long[]{0,73,-20261028,922337203685L})sample(seed);
    System.out.println("SURVEY_PRECHECK visited="+visited+" protected="+protectedColumns+" shoulder="+shoulderColumns+" inland="+inlandColumns+" v3Flips="+v3Flips+" inlandChanged="+changedInland);
    require(protectedColumns>5000 && shoulderColumns>50 && inlandColumns>5,
        "must exercise protected coast, interior release and V2 inland");
    require(v3Flips>=2,"must reproduce both audited Dev61 transition witnesses");
    require(v4Flips==0,"no V4 coastal-height sea-plane changes");
    require(changedInland>0,"V4 must actually retain newer inland landforms");
    for(long seed:new long[]{0,73})for(boolean floating:new boolean[]{false,true})
      reopen(seed,floating);
    System.out.println("V8_RELIEF_V4_COAST_PASS assertions="+assertions+
        " visited="+visited+" protected="+protectedColumns+" shoulder="+shoulderColumns+
        " inland="+inlandColumns+" v3Flips="+v3Flips+" v4Flips="+v4Flips+
        " inlandChanged="+changedInland+" exactOldGraphDigests=true"+
        " nativeOceanMask=false realMinecraft=false");
  }
}
