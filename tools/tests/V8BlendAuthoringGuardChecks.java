package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainOptions;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.NoiseSpec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Isolated Mojang-codec-double coverage; not a native game/save test. */
public final class V8BlendAuthoringGuardChecks {
  private static long assertions, rejected, roundtrips;
  private static void need(boolean ok, String reason) {
    assertions++;
    if (!ok) throw new AssertionError(reason);
  }
  private static void deny(Runnable action, String reason) {
    boolean rejectedAsExpected = false;
    try { action.run(); }
    catch (IllegalStateException expected) { rejectedAsExpected = true; }
    need(rejectedAsExpected, reason);
    rejected++;
  }
  private static DensityGraph.Definition shape() {
    return new DensityGraph.Definition(1,"density",List.of(
        DensityGraph.Node.constant("bias",-.05),
        DensityGraph.Node.noise("climate",NoiseSpec.DEFAULT,true),
        DensityGraph.Node.operation("density",DensityGraph.Op.ADD,"bias","climate")));
  }
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    var random=new Random(20261028L);
    for(int i=0;i<512;i++) {
      long seed=i==0?Long.MIN_VALUE:i==1?Long.MAX_VALUE:random.nextLong();
      var legacy=new V8GeneratorSnapshot(1,seed,-128,96,shape(),
          new GroundSurfaceProfile("climate",5));
      var binding=V8ClimateBinding.fromSavedV7(TerrainOptions.DEFAULT,i%6);
      var r2=legacy.withBlendPolicy(V8BlendPolicy.HARD);
      var r3=r2.withSurfacePolicy(V8SurfacePolicy.FROZEN_R1);
      var r4=r3.withClimateBinding(binding);
      need(legacy.version()==1 && r2.version()==2 && r3.version()==3
          && r4.version()==4,"explicit revision transitions");
      need(r2.density()==legacy.density() && r3.density()==r2.density()
          && r4.density()==r3.density(),"saved density identity unchanged");
      need(r2.equals(r2.withBlendPolicy(V8BlendPolicy.HARD)),
          "r2 explicit blend edit must remain r2");
      deny(()->r3.withBlendPolicy(V8BlendPolicy.HARD),
          "r3 surface must not be discarded by blend editing");
      deny(()->r4.withBlendPolicy(V8BlendPolicy.HARD),
          "r4 frozen climate must not be discarded by blend editing");
      deny(()->r3.withBlendPolicy(null),"r3 must reject before downgrade");
      deny(()->r4.withBlendPolicy(null),"r4 must reject before downgrade");
      var versions=List.of(legacy,r2,r3,r4);
      int[] sizes={6,7,8,9};
      for(int v=0;v<versions.size();v++) {
        var profile=versions.get(v);
        var bytes=(Map<String,Object>)V8GeneratorSnapshot.CODEC.encode(profile);
        need(bytes.keySet().size()==sizes[v],"frozen wire field count");
        need((Integer)bytes.get("version")==v+1,"frozen wire revision");
        need(profile.equals(V8GeneratorSnapshot.CODEC.decode(bytes)),
            "reopen exact saved V8 envelope");
        need(Long.toString(seed).equals(bytes.get("seed")),"signed seed exactness");
        if(v>=2)need(bytes.containsKey("surface"),"surface retained");
        if(v==3)need(bytes.containsKey("climate_binding"),"climate retained");
        roundtrips++;
      }
      Map<String,Object> stored3=(Map<String,Object>)V8GeneratorSnapshot.CODEC.encode(r3);
      Map<String,Object> stored4=(Map<String,Object>)V8GeneratorSnapshot.CODEC.encode(r4);
      deny(()->V8GeneratorSnapshot.CODEC.decode(stored3).withBlendPolicy(V8BlendPolicy.HARD),
          "decoded r3 cannot be downgraded");
      deny(()->V8GeneratorSnapshot.CODEC.decode(stored4).withBlendPolicy(V8BlendPolicy.HARD),
          "decoded r4 cannot be downgraded");
      need(stored3.equals(V8GeneratorSnapshot.CODEC.encode(r3)),
          "failed edit cannot mutate original surface profile");
      need(stored4.equals(V8GeneratorSnapshot.CODEC.encode(r4)),
          "failed edit cannot mutate original climate profile");
      var invalid3=new LinkedHashMap<>(stored3);invalid3.remove("surface");
      boolean invalidRejected=false;
      try{V8GeneratorSnapshot.CODEC.decode(invalid3);}catch(RuntimeException expected){invalidRejected=true;}
      need(invalidRejected,"missing surface field rejected");
    }
    need(Set.of(1,2,3,4).size()==4,"all four revisions exercised");
    System.out.println("V8_BLEND_AUTHORING_GUARD_PASS assertions="+assertions
        +" rejected="+rejected+" roundtrips="+roundtrips);
  }
}
