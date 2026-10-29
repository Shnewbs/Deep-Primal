package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainOptions;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.NoiseSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Exact route-adapter source checks; watershed behavior is an isolated API double. */
public final class V8RoutedWaterSourceChecks {
  private static long checks, rejected, sampled;
  private static void check(boolean ok, String what) {
    checks++;
    if(!ok) throw new AssertionError(what+" #"+checks);
  }
  private static void reject(Runnable r,String what) {
    try {r.run();}catch(IllegalArgumentException | IllegalStateException expected){checks++;rejected++;return;}
    throw new AssertionError("failed to reject "+what);
  }
  private static DensityGraph.Definition graph(long salt) {
    List<DensityGraph.Node> nodes=new ArrayList<>();
    nodes.add(DensityGraph.Node.constant("sea",96));
    nodes.add(DensityGraph.Node.noise("coastal",new NoiseSpec(1./380,1./380,1./380,3,2,.5,salt),true));
    nodes.add(DensityGraph.Node.constant("scale",30));
    nodes.add(DensityGraph.Node.operation("variation",DensityGraph.Op.MULTIPLY,"coastal","scale"));
    nodes.add(DensityGraph.Node.operation("height",DensityGraph.Op.ADD,"sea","variation"));
    nodes.add(DensityGraph.Node.operation("y",DensityGraph.Op.Y));
    nodes.add(DensityGraph.Node.constant("negative",-1));
    nodes.add(DensityGraph.Node.operation("negative_y",DensityGraph.Op.MULTIPLY,"y","negative"));
    nodes.add(DensityGraph.Node.operation("density",DensityGraph.Op.ADD,"height","negative_y"));
    return new DensityGraph.Definition(1,"density",nodes);
  }
  private static V8GeneratorSnapshot v4(long seed,int channel,long salt) {
    var v3=new V8GeneratorSnapshot(3,seed,-64,320,graph(salt),
        new GroundSurfaceProfile("height",4),V8BlendPolicy.HARD,V8SurfacePolicy.FROZEN_R1);
    return v3.withClimateBinding(V8ClimateBinding.fromSavedV7(TerrainOptions.DEFAULT,channel));
  }
  private static void authorAndReopen() {
    var random=new Random(0xCAFE2868L);
    for(int i=0;i<32;i++) {
      long seed=i==0?Long.MIN_VALUE:i==1?Long.MAX_VALUE:random.nextLong();
      int channel=i%6;
      var r4=v4(seed,channel,8100+i);
      String expected=V8RoutedWaterSource.fingerprint(r4);
      check(expected.matches("[0-9a-f]{64}"),"canonical digest");
      check(expected.equals(V8RoutedWaterSource.fingerprint(r4)),"repeat fingerprint");
      var r5=V8RoutedWaterSource.author(r4);
      check(r5.version()==5,"revision five opt in");
      check(r5.density().equals(r4.density()),"density identity");
      check(r5.climateBinding().equals(r4.climateBinding()),"climate identity");
      check(r5.hydrologyBinding().sourceFingerprint().equals(expected),"pin actual source");
      var decoded=V8GeneratorSnapshot.CODEC.decode(V8GeneratorSnapshot.CODEC.encode(r5));
      check(decoded.equals(r5),"modeled saved roundtrip");
      var route=V8RoutedWaterSource.open(decoded,seed);
      check(route.sourceFingerprint().equals(expected),"reopen same source");
      check(route.authority().sourceFingerprint().equals(expected),"authority pinned");
      var reference=V8SavedClimateSource.fromSavedV8(decoded,seed);
      var admitted=V8WaterDomainAdmission.bind(decoded.prepare(),reference,route.authority());
      for(int x=-31;x<32;x+=7)for(int z=-19;z<20;z+=9) {
        var a=route.sample(x,z);
        var b=route.plan(x,z).datum();
        check(a.equals(b),"planned water datum parity");
        check(a.kind()!=null,"typed routed water");
        var climate=admitted.sample(x,128,z);
        check(climate.domain()!=null,"admitted climate domain");
        sampled++;
      }
      reject(()->V8RoutedWaterSource.open(r5,seed^1L),"wrong saved seed");
      reject(()->V8RoutedWaterSource.author(r5),"silently rewrite saved rev5");
      var fake=r4.withHydrologyBinding(V8HydrologyBinding.pinned("a".repeat(64)));
      reject(()->V8RoutedWaterSource.open(fake,seed),"arbitrary claimed source digest");
      var wrongGraph=v4(seed,channel,9100+i);
      check(!expected.equals(V8RoutedWaterSource.fingerprint(wrongGraph)),"changed graph digest");
      var changedClimate=v4(seed,(channel+1)%6,8100+i);
      check(!expected.equals(V8RoutedWaterSource.fingerprint(changedClimate)),"changed climate routing digest");
    }
  }
  private static void coordinatesAndLeases() {
    var r=V8RoutedWaterSource.open(V8RoutedWaterSource.author(v4(-999,5,31)),-999);
    int[][] locations={{-1,-1},{0,0},{8191,0},{8192,0},{-8192,-8192},{-8193,8192}};
    for(var p:locations){
      var first=r.plan(p[0],p[1]);
      check(first.equals(r.plan(p[0],p[1])),"repeat same result");
      check(r.cachedRegionCount()<=2,"cache cap");
    }
    for(var p:locations) {
      var a=r.plan(p[0],p[1]);
      r.plan(2*8192,2*8192);
      r.plan(-2*8192,-2*8192);
      var b=r.plan(p[0],p[1]);
      check(a.equals(b),"eviction/rebuild repeat at signed coordinate");
      check(r.cachedRegionCount()<=2,"eviction bounded");
    }
    reject(()->r.sample(29_999_999,0),"positive graph footprint overflow");
    reject(()->r.sample(-30_000_000,0),"negative graph footprint overflow");
    check(r.cachedRegionCount()<=2,"resident bound preserved on rejection");
  }
  private static void invalidHeightGraphs() {
    var base=v4(42,0,8);
    var nodes=new ArrayList<>(base.density().nodes());
    int index=-1;for(int i=0;i<nodes.size();i++)if(nodes.get(i).id().equals("height"))index=i;
    var yNodes=new ArrayList<>(nodes);
    yNodes.set(index,DensityGraph.Node.operation("height",DensityGraph.Op.ADD,"sea","height_y"));
    yNodes.add(DensityGraph.Node.operation("height_y",DensityGraph.Op.ADD,"variation","y"));
    var yDef=new DensityGraph.Definition(1,"density",yNodes);
    var yProfile=new V8GeneratorSnapshot(4,42,-64,320,yDef,base.ground(),base.blendPolicy(),
        base.surfacePolicy(),base.climateBinding());
    reject(()->V8RoutedWaterSource.author(yProfile),"Y-dependent watershed height");
    var threeNodes=new ArrayList<>(nodes);
    threeNodes.set(index,DensityGraph.Node.operation("height",DensityGraph.Op.ADD,"sea","height_3d"));
    threeNodes.add(DensityGraph.Node.operation("height_3d",DensityGraph.Op.ADD,"variation","noisy3d"));
    threeNodes.add(DensityGraph.Node.noise("noisy3d",NoiseSpec.DEFAULT,false));
    var threeDef=new DensityGraph.Definition(1,"density",threeNodes);
    var threeProfile=new V8GeneratorSnapshot(4,42,-64,320,threeDef,base.ground(),base.blendPolicy(),
        base.surfacePolicy(),base.climateBinding());
    reject(()->V8RoutedWaterSource.author(threeProfile),"3D-noise watershed height");
    reject(()->V8RoutedWaterSource.author(base.withHydrologyBinding(
        V8HydrologyBinding.pinned("0".repeat(63)+"1"))),
        "revision5 cannot reauthor silently");
  }
  private static void optInOutletSeededPriorityFlood() {
    var base=v4(7331,2,23);
    // This test graph is uniformly dry-land continentalness and low elevation.
    // The deliberately isolated WatershedRegion double models a low unconnected
    // lake with the new seed-gated constructor. Native priority flood not tested.
    var nodes=new ArrayList<DensityGraph.Node>();
    nodes.add(DensityGraph.Node.constant("base_height",80));
    nodes.add(DensityGraph.Node.constant("continentalness",0.35));
    nodes.add(DensityGraph.Node.constant("zero",0));
    nodes.add(DensityGraph.Node.operation("inert_continental",DensityGraph.Op.MULTIPLY,
        "continentalness","zero"));
    nodes.add(DensityGraph.Node.operation("height",DensityGraph.Op.ADD,
        "base_height","inert_continental"));
    nodes.add(DensityGraph.Node.operation("y",DensityGraph.Op.Y));
    nodes.add(DensityGraph.Node.constant("negative",-1));
    nodes.add(DensityGraph.Node.operation("neg_y",DensityGraph.Op.MULTIPLY,"y","negative"));
    nodes.add(DensityGraph.Node.operation("density",DensityGraph.Op.ADD,"height","neg_y"));
    var definition=new DensityGraph.Definition(1,"density",nodes);
    var r4=new V8GeneratorSnapshot(4,base.seed(),base.minY(),base.height(),definition,
        base.ground(),base.blendPolicy(),base.surfacePolicy(),base.climateBinding());
    var old=V8RoutedWaterSource.open(V8RoutedWaterSource.authorBasinGuarded(r4),r4.seed());
    var snap=V8RoutedWaterSource.authorOutletSeeded(r4);
    check(snap.version()==5,"outlet seed has explicit saved r5 envelope");
    check(!snap.hydrologyBinding().equals(V8RoutedWaterSource.authorBasinGuarded(r4).hydrologyBinding()),
        "new provenance distinct from v3");
    var reopened=V8GeneratorSnapshot.CODEC.decode(V8GeneratorSnapshot.CODEC.encode(snap));
    check(reopened.equals(snap),"outlet seed saved roundtrip");
    var guarded=V8RoutedWaterSource.open(reopened,r4.seed());
    check(guarded.outletSeededPolicy() && !old.outletSeededPolicy(),
        "new source mode retained and old version untouched");
    check(guarded.sourceFingerprint().equals(V8RoutedWaterSource.outletSeedFingerprint(r4)),
        "version-4 saved source contract");
    var previously=old.plan(8192,2048);
    var now=guarded.plan(8192,2048);
    check(previously.kind()==V8WaterDomainAdmission.WaterKind.LAKE
        && now.kind()==V8WaterDomainAdmission.WaterKind.LAKE,
        "enclosed sea-like depression remains lake in both versions");
    check(previously.plannedStage()==96 && now.plannedStage()==82,
        "new ocean boundary gate affects priority-flood source, not just water label");
    check(!now.equals(previously),"routing stage changed via new source constructor");
    for(int x: new int[]{-8192,-8191,-1,0,8191,8192,8193}) {
      check(guarded.plan(x,2048).equals(guarded.plan(x,2048)),
          "seed-gated absolute-coordinate repeat across signed boundaries");
      check(guarded.cachedMarineRegionCount()<=4,"marine cache cap");
    }
    reject(()->V8RoutedWaterSource.open(reopened,r4.seed()+1),"new v4 wrong world seed");
    reject(()->V8RoutedWaterSource.authorOutletSeeded(snap),"no in-place r5 rewrite");
    check(old.sourceFingerprint().equals(V8RoutedWaterSource.basinGuardFingerprint(r4)),
        "frozen v3 fingerprint remains identical");
  }
  private static void optInSavedOceanDomain() {
    var original = v4(-742, 2, 980);
    var missing = V8RoutedWaterSource.author(original);
    reject(() -> V8RoutedWaterSource.authorOceanDomain(original),
        "saved ocean policy requires real graph ocean-domain source");
    var nodes = new ArrayList<>(original.density().nodes());
    // DensityGraph intentionally rejects unreachable entries. Link both ocean
    // authority and climate source into a zero-weight dependency of the root.
    for (int i = 0; i < nodes.size(); i++)
      if (nodes.get(i).id().equals("height")) {
        nodes.set(i, DensityGraph.Node.operation("height_raw", DensityGraph.Op.ADD,
            "sea", "variation"));
        break;
      }
    nodes.add(DensityGraph.Node.constant("continentalness", -0.4));
    nodes.add(DensityGraph.Node.constant("ocean_domain", +1));
    nodes.add(DensityGraph.Node.operation("mask_sources", DensityGraph.Op.ADD,
        "continentalness", "ocean_domain"));
    nodes.add(DensityGraph.Node.constant("inert_zero", 0));
    nodes.add(DensityGraph.Node.operation("inert_masks", DensityGraph.Op.MULTIPLY,
        "mask_sources", "inert_zero"));
    nodes.add(DensityGraph.Node.operation("height", DensityGraph.Op.ADD,
        "height_raw", "inert_masks"));
    var def = new DensityGraph.Definition(1, original.density().root(), nodes);
    var r4 = new V8GeneratorSnapshot(4, original.seed(), original.minY(), original.height(), def,
        original.ground(), original.blendPolicy(), original.surfacePolicy(),
        original.climateBinding());
    var saved = V8RoutedWaterSource.authorOceanDomain(r4);
    check(saved.version() == 5, "new route v5 uses existing explicit saved profile revision");
    check(!saved.hydrologyBinding().equals(missing.hydrologyBinding()), "no legacy migration");
    check(saved.hydrologyBinding().sourceFingerprint().equals(
        V8RoutedWaterSource.oceanDomainFingerprint(r4)), "domain-separated routing v5 key");
    var reopened = V8GeneratorSnapshot.CODEC.decode(V8GeneratorSnapshot.CODEC.encode(saved));
    check(reopened.equals(saved), "source mask survives modeled codec reopening");
    var route = V8RoutedWaterSource.open(reopened, original.seed());
    check(route.oceanDomainPolicy() && route.outletSeededPolicy(),
        "saved mask drives priority-flood outlet admission");
    for (int x : new int[] {-8192, -1, 0, 8191, 8192}) {
      check(route.plan(x, 2048).kind() != V8WaterDomainAdmission.WaterKind.OCEAN,
          "inland ocean-like climate denied by explicit positive mask");
      check(route.plan(x, 2048).equals(route.plan(x, 2048)), "repeated query parity");
    }
    reject(() -> V8RoutedWaterSource.open(saved, original.seed() + 1), "wrong world seed");
    reject(() -> V8RoutedWaterSource.authorOceanDomain(saved), "no saved r5 mutation");
    var alteredNodes = new ArrayList<>(nodes);
    for (int i = 0; i < alteredNodes.size(); i++)
      if (alteredNodes.get(i).id().equals("ocean_domain"))
        alteredNodes.set(i, DensityGraph.Node.constant("ocean_domain", -1));
    var alteredDef = new DensityGraph.Definition(1, def.root(), alteredNodes);
    var changed = new V8GeneratorSnapshot(4, r4.seed(), r4.minY(), r4.height(), alteredDef,
        r4.ground(), r4.blendPolicy(), r4.surfacePolicy(), r4.climateBinding());
    check(!V8RoutedWaterSource.oceanDomainFingerprint(r4).equals(
        V8RoutedWaterSource.oceanDomainFingerprint(changed)), "mask edits change saved identity");
    var yNodes = new ArrayList<>(nodes);
    for (int i = 0; i < yNodes.size(); i++)
      if (yNodes.get(i).id().equals("ocean_domain"))
        yNodes.set(i, DensityGraph.Node.operation("ocean_domain",
            DensityGraph.Op.ADD, "continentalness", "y"));
    var yDef = new DensityGraph.Definition(1, def.root(), yNodes);
    var yProfile = new V8GeneratorSnapshot(4, r4.seed(), r4.minY(), r4.height(), yDef,
        r4.ground(), r4.blendPolicy(), r4.surfacePolicy(), r4.climateBinding());
    reject(() -> V8RoutedWaterSource.authorOceanDomain(yProfile), "Y-dependent ocean mask");
  }

  public static void main(String[] args) {
    authorAndReopen();coordinatesAndLeases();invalidHeightGraphs();
    optInOutletSeededPriorityFlood();
    optInSavedOceanDomain();
    System.out.println("V8_ROUTED_SOURCE_PASS checks="+checks+" samples="+sampled+
        " rejected="+rejected+" actualWatershed=API_DOUBLE");
  }
}
