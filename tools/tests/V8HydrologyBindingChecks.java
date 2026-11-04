package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainOptions;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.NoiseSpec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Isolated DFU API-double contract, not native Mojang / Minecraft execution. */
public final class V8HydrologyBindingChecks {
  private static long assertions, roundtrips, rejected, admissions, comparisons;
  private static void need(boolean valid, String why) {
    assertions++;
    if (!valid) throw new AssertionError(why + " #" + assertions);
  }
  private static void reject(Runnable operation, String why) {
    boolean failed = false;
    try {operation.run();}
    catch (IllegalArgumentException | IllegalStateException | NullPointerException expected) {
      failed = true;
    }
    need(failed, "did not reject " + why);
    rejected++;
  }
  private static DensityGraph.Definition graph() {
    return new DensityGraph.Definition(1, "density", List.of(
        DensityGraph.Node.constant("bias", -.05),
        DensityGraph.Node.noise("climate", NoiseSpec.DEFAULT, true),
        DensityGraph.Node.operation("density", DensityGraph.Op.ADD, "bias", "climate")));
  }
  private static V8GeneratorSnapshot v3(long seed) {
    return new V8GeneratorSnapshot(3, seed, -128, 96, graph(),
        new GroundSurfaceProfile("climate", 5), V8BlendPolicy.HARD,
        V8SurfacePolicy.FROZEN_R1);
  }
  private static String fingerprint(Random random) {
    StringBuilder b = new StringBuilder(64);
    for (int j=0;j<8;j++) b.append(String.format(java.util.Locale.ROOT,
        "%08x", random.nextInt()));
    return b.toString();
  }
  @SuppressWarnings("unchecked")
  private static void policies() {
    var climate = V8ClimateBinding.fromSavedV7(TerrainOptions.DEFAULT, 5);
    var r3=v3(-15); var r4=r3.withClimateBinding(climate);
    var old3=(Map<String,Object>)V8GeneratorSnapshot.CODEC.encode(r3);
    var old4=(Map<String,Object>)V8GeneratorSnapshot.CODEC.encode(r4);
    need(old3.keySet().equals(Set.of("version","seed","min_y","height","density",
        "ground","blend","surface")), "legacy r3 exact field set");
    need(old4.keySet().equals(Set.of("version","seed","min_y","height","density",
        "ground","blend","surface","climate_binding")), "legacy r4 exact field set");
    need(V8GeneratorSnapshot.CODEC.decode(old3).equals(r3), "old r3 exact decoding");
    need(V8GeneratorSnapshot.CODEC.decode(old4).equals(r4), "old r4 exact decoding");
    reject(()->r3.withHydrologyBinding(V8HydrologyBinding.pinned("a".repeat(64))),"r3 upgrade");
    reject(()->r4.withHydrologyBinding(null),"null provenance");
    reject(()->r4.withBlendPolicy(V8BlendPolicy.HARD),"r4 downgrade");
    var rand=new Random(0x8060cafeL);
    for(int i=0;i<800;i++) {
      long seed=i==0?Long.MIN_VALUE:i==1?Long.MAX_VALUE:rand.nextLong();
      var options=new TerrainOptions(rand.nextBoolean(),rand.nextBoolean(),rand.nextBoolean(),
          rand.nextBoolean(),rand.nextBoolean(),.5+rand.nextDouble()*1.5,
          4000+rand.nextDouble()*996000);
      var climateBinding=V8ClimateBinding.fromSavedV7(options,i%7);
      String source=fingerprint(rand);
      var hydro=V8HydrologyBinding.pinned(source);
      var r5=v3(seed).withClimateBinding(climateBinding).withHydrologyBinding(hydro);
      var saved=(Map<String,Object>)V8GeneratorSnapshot.CODEC.encode(r5);
      need(saved.size()==10,"r5 ten exact fields");
      need(saved.get("hydrology_binding").equals(hydro.key()),"routed source provenance stored");
      need(r5.equals(V8GeneratorSnapshot.CODEC.decode(saved)),"r5 full codec replay");
      need(hydro.equals(V8HydrologyBinding.CODEC.decode(hydro.key())),"hydrology key replay");
      need(r5.prepare().snapshot().hydrologyBinding().equals(hydro),"prepared saved policy");
      need(V8SavedClimateSource.fromSavedV8(r5,seed).savedOptions().equals(options),
          "r5 saved climate exact bits");
      reject(()->V8SavedClimateSource.fromSavedV8(r5,seed^1L),"r5 wrong world seed");
      reject(()->r5.withHydrologyBinding(hydro),"r5 cannot rebind source");
      reject(()->r5.withClimateBinding(climateBinding),"r5 cannot discard routing identity");
      if (i < 40) {
        for(String field:saved.keySet()) {
          var changed=new LinkedHashMap<>(saved);changed.remove(field);
          reject(()->V8GeneratorSnapshot.CODEC.decode(changed),"missing r5 field "+field);
        }
        var changed=new LinkedHashMap<>(saved);changed.put("version",4);
        reject(()->V8GeneratorSnapshot.CODEC.decode(changed),"silent r5 downgrade");
        var changed4=new LinkedHashMap<>(old4);changed4.put("version",5);
        reject(()->V8GeneratorSnapshot.CODEC.decode(changed4),"silent r4 upgrade");
        var altered=new LinkedHashMap<>(saved);altered.put("hydrology_binding", "1:1:"+"0".repeat(64));
        reject(()->V8GeneratorSnapshot.CODEC.decode(altered),"placeholder zero digest");
        for(String bad:List.of("", "1:2:"+source,"2:1:"+source,"1:1:"+source.toUpperCase(java.util.Locale.ROOT),
            "1:1:"+source+"00", "1:1:xyz", "1:1:"+"0".repeat(64)))
          reject(()->V8HydrologyBinding.CODEC.decode(bad),"noncanonical source fingerprint");
      }
      roundtrips++;
    }
  }
  private static void admission() {
    var climate=V8ClimateBinding.fromSavedV7(TerrainOptions.DEFAULT,5);
    var rand=new Random(0x29b0aL);
    for(int i=0;i<256;i++) {
      long seed=i==0?Long.MIN_VALUE:rand.nextLong();
      var saved=v3(seed).withClimateBinding(climate);
      String source=fingerprint(rand);
      var r5=saved.withHydrologyBinding(V8HydrologyBinding.pinned(source));
      V8EcotoneColumnPlan.ClimateSource reference=(x,y,z)->
          new V8EcotoneColumnPlan.ClimateDatum(new EcotoneBlend.Climate(
              y,.5,.6,.01,.4), EcotoneBlend.Domain.LAND);
      V8WaterDomainAdmission.WaterSource mask=(x,z)->
          Math.floorMod(x,29)==0?V8WaterDomainAdmission.WaterDatum.ocean():
          Math.floorMod(z,13)==0?new V8WaterDomainAdmission.WaterDatum(
              V8WaterDomainAdmission.WaterKind.RIVER,99):
          V8WaterDomainAdmission.WaterDatum.dry();
      var authority=new V8WaterDomainAdmission.Authority(seed,r5.density(),climate,1,source,mask);
      var admitted=V8WaterDomainAdmission.bind(r5.prepare(),reference,authority);
      var old=V8WaterDomainAdmission.bind(saved.prepare(),reference,
          new V8WaterDomainAdmission.Authority(seed,saved.density(),climate,1,mask));
      reject(()->V8WaterDomainAdmission.bind(r5.prepare(),reference,
          new V8WaterDomainAdmission.Authority(seed,r5.density(),climate,1,mask)),
          "missing r5 source fingerprint");
      reject(()->V8WaterDomainAdmission.bind(r5.prepare(),reference,
          new V8WaterDomainAdmission.Authority(seed,r5.density(),climate,1,"f".repeat(64),mask)),
          "incorrect saved routing digest");
      reject(()->V8WaterDomainAdmission.bind(saved.prepare(),reference,authority),
          "r4 cannot claim provenance");
      reject(()->V8WaterDomainAdmission.bind(r5.prepare(),reference,
          new V8WaterDomainAdmission.Authority(seed^1L,r5.density(),climate,1,source,mask)),
          "incorrect saved seed");
      for(int z=-17;z<17;z+=3)for(int x=-17;x<17;x+=3) {
        var a=admitted.sample(x,101,z);var b=old.sample(x,101,z);
        need(a.equals(b),"saved identity preserves old sampled mask");
        comparisons++;
      }
      admissions++;
    }
  }
  public static void main(String[] args) {
    policies();admission();
    System.out.println("V8_HYDROLOGY_BINDING_PASS assertions="+assertions+
        " roundtrips="+roundtrips+" admissions="+admissions+
        " maskComparisons="+comparisons+" rejected="+rejected+
        " apiDoubles=true java="+System.getProperty("java.version"));
  }
}
