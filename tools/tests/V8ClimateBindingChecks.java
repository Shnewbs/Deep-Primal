package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainOptions;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.NoiseSpec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Focused API-double contract checks, not a native Mojang codec/save test. */
public final class V8ClimateBindingChecks {
  private static long checks, roundtrips, prewriteComparisons, rejected;
  private static void need(boolean result, String reason) {
    checks++;
    if (!result) throw new AssertionError(reason);
  }
  private static void reject(Runnable action, String name) {
    boolean denied=false;
    try {action.run();} catch (IllegalArgumentException | IllegalStateException | NullPointerException expected) {denied=true;}
    need(denied,"accepted invalid " + name);
    rejected++;
  }
  private static DensityGraph.Definition graph() {
    return new DensityGraph.Definition(1, "density", List.of(
        DensityGraph.Node.constant("bias", -0.05),
        DensityGraph.Node.noise("climate", NoiseSpec.DEFAULT, true),
        DensityGraph.Node.operation("density", DensityGraph.Op.ADD, "bias", "climate")));
  }
  private static V8GeneratorSnapshot frozen(long seed) {
    return new V8GeneratorSnapshot(3,seed,-128,96,graph(),
        new GroundSurfaceProfile("climate",5),V8BlendPolicy.HARD,V8SurfacePolicy.FROZEN_R1);
  }
  @SuppressWarnings("unchecked")
  private static void strictCodec() {
    Random random = new Random(64);
    var opts=TerrainOptions.DEFAULT;
    var binding=V8ClimateBinding.fromSavedV7(opts,5);
    need(V8ClimateBinding.CODEC.decode(binding.key()).equals(binding),"binding exact source roundtrip");
    var old=frozen(-4);
    var oldEncoded=(Map<String,Object>)V8GeneratorSnapshot.CODEC.encode(old);
    need(oldEncoded.keySet().equals(Set.of("version","seed","min_y","height","density","ground","blend","surface")),"unchanged r3 envelope");
    need(old.equals(V8GeneratorSnapshot.CODEC.decode(oldEncoded)),"r3 saved replay");
    reject(()->old.withClimateBinding(null),"missing binding");
    reject(()->old.withBlendPolicy(V8BlendPolicy.HARD).withClimateBinding(binding),"r2 binding");
    for(int i=0;i<1536;i++) {
      long seed=i==0?Long.MIN_VALUE:i==1?Long.MAX_VALUE:random.nextLong();
      TerrainOptions option=new TerrainOptions(random.nextBoolean(),random.nextBoolean(),
          random.nextBoolean(),random.nextBoolean(),random.nextBoolean(),
          .5+random.nextDouble()*1.5,4000+random.nextDouble()*996000);
      V8ClimateBinding policy=V8ClimateBinding.fromSavedV7(option,i%6);
      var v4=frozen(seed).withClimateBinding(policy);
      var stored=(Map<String,Object>)V8GeneratorSnapshot.CODEC.encode(v4);
      need(stored.size()==9&&stored.containsKey("climate_binding"),"r4 exact 9-field envelope");
      need(v4.equals(V8GeneratorSnapshot.CODEC.decode(stored)),"r4 object+policy roundtrip");
      need(policy.equals(V8ClimateBinding.CODEC.decode(policy.key())),"canonical options bits roundtrip");
      need(policy.key().equals(stored.get("climate_binding")),"persist complete frozen options key");
      need(v4.prepare().snapshot().climateBinding().equals(policy),"compiled snapshot retains binding");
      roundtrips++;
      if(i<80) {
        V8SavedClimateSource source=V8SavedClimateSource.fromSavedV8(v4,seed);
        need(source.savedOptions().equals(option),"reopen exact boolean and double bits");
        need(source.savedChannelRevision()==i%6,"reopen channel revision");
        reject(()->V8SavedClimateSource.fromSavedV8(v4,seed^1),"wrong seed admitted");
      }
    }
    var r4=old.withClimateBinding(binding);
    var encoded=(Map<String,Object>)V8GeneratorSnapshot.CODEC.encode(r4);
    for(String key:encoded.keySet()) {
      var altered=new LinkedHashMap<>(encoded);altered.remove(key);
      reject(()->V8GeneratorSnapshot.CODEC.decode(altered),"removed root field:"+key);
    }
    for(String invalid:List.of("","1:7:6:11111:3ff4cccccccccccd:40cf400000000000",
        "1:7:5:21111:3ff4cccccccccccd:40cf400000000000",
        "1:7:5:11111:7ff8000000000000:40cf400000000000",
        "1:7:5:11111:3ff4CCCCCCCCCCCD:40cf400000000000",
        "1:7:5:11111:3ff4cccccccccccd:0000000000000000",
        "1:7:5:11111:3ff4cccccccccccd:40cf400000000000:extra")) {
      var altered=new LinkedHashMap<>(encoded);altered.put("climate_binding",invalid);
      reject(()->V8GeneratorSnapshot.CODEC.decode(altered),"invalid frozen binding:"+invalid);
    }
    var surplus=new LinkedHashMap<>(encoded);surplus.put("climateBinding",binding.key());
    reject(()->V8GeneratorSnapshot.CODEC.decode(surplus),"unknown policy sibling");
    var downgrade=new LinkedHashMap<>(encoded);downgrade.put("version",3);
    reject(()->V8GeneratorSnapshot.CODEC.decode(downgrade),"silently reinterpret r4 as r3");
    var upgrade=new LinkedHashMap<>(oldEncoded);upgrade.put("version",4);
    reject(()->V8GeneratorSnapshot.CODEC.decode(upgrade),"silently reinterpret r3 as r4");
    reject(()->V8SavedClimateSource.fromSavedV8(old,old.seed()),"r3 via r4 climate path");
    reject(()->V8SavedClimateSource.fromSavedV7(r4,
        new TerrainProfile(opts),r4.seed(),7,5),"r4 via r3 climate path");
  }
  private static void prewrite() {
    var r3=frozen(20261028).prepare();
    var r4=frozen(20261028).withClimateBinding(
        V8ClimateBinding.fromSavedV7(TerrainOptions.DEFAULT,3)).prepare();
    var source3=V8SavedClimateSource.fromSavedV7(r3.snapshot(),
        new TerrainProfile(TerrainOptions.DEFAULT),r3.snapshot().seed(),7,3);
    var source4=V8SavedClimateSource.fromSavedV8(r4.snapshot(),r4.snapshot().seed());
    for(int cz=-2;cz<=1;cz++)for(int cx=-2;cx<=1;cx++) {
      var a=DensityChunkMaterializer.planSavedEcotone(r3,cx*16,cz*16,-128,96,32,source3);
      var b=DensityChunkMaterializer.planSavedEcotone(r4,cx*16,cz*16,-128,96,32,source4);
      for(int x=0;x<16;x++)for(int z=0;z<16;z++) {
        need(a.terrain().chunk().surfaceExclusive(x,z)==b.terrain().chunk().surfaceExclusive(x,z),"r4 unchanged height");
        var oldCol=a.columns().column(x,z);
        var newCol=b.columns().column(x,z);
        need((oldCol==null)==(newCol==null),"same reference water exclusions");
        if(oldCol!=null){
          need(oldCol.topY()==newCol.topY() && oldCol.soilDepth()==newCol.soilDepth(),"same climate/soil plan");
          need(oldCol.cover().equals(newCol.cover()),"same cover sample");
        }
        prewriteComparisons++;
      }
    }
  }
  public static void main(String[] args) {
    strictCodec();prewrite();
    System.out.println("V8_CLIMATE_BINDING_PASS checks="+checks+" persistedRoundtrips="+roundtrips+
        " prewriteColumns="+prewriteComparisons+" rejected="+rejected+" native=false");
  }
}
