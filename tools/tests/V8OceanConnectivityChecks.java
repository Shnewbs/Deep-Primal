package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainOptions;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/** Actual stitched component/seam code; WatershedRegion and Minecraft remain API doubles. */
public final class V8OceanConnectivityChecks {
  private static long checks;
  private static void check(boolean ok, String label) {
    checks++;
    if (!ok) throw new AssertionError(label + " #" + checks);
  }
  private static void reject(Runnable op, String label) {
    try { op.run(); }
    catch (IllegalArgumentException expected) { checks++; return; }
    throw new AssertionError("failed to reject " + label);
  }

  private static V8OceanConnectivity basin(int left, int right,
      int lowerZ, int upperZ, int ridgeX, boolean marine) {
    return new V8OceanConnectivity((x, z) -> {
      boolean low = x >= left && x <= right && z >= lowerZ && z <= upperZ
          && (ridgeX == Integer.MIN_VALUE || x < ridgeX || x >= ridgeX + 96);
      return new V8OceanConnectivity.Point(low ? 60 : 120,
          marine && x >= left + 450 ? -0.4 : 0.4);
    });
  }

  private static void closedBasinAtTwoSignedSeams() {
    var positive = basin(7000, 9200, 1700, 2700, Integer.MIN_VALUE, false);
    for (int x : new int[] {8190, 8192, 8194}) {
      check(!positive.isMarineAt(x, 2048), "closed positive-boundary basin not ocean");
      check(!positive.isMarineAt(x, 2048), "same cache gives same negative");
      check(positive.cachedRegionCount() <= 4, "positive region cache bounded");
    }
    var negative = basin(-9300, -7000, 1700, 2700, Integer.MIN_VALUE, false);
    for (int x : new int[] {-8194, -8192, -8190}) {
      check(!negative.isMarineAt(x, 2048), "closed negative-boundary basin not ocean");
      check(negative.cachedRegionCount() <= 4, "negative region cache bounded");
    }
  }

  private static void connectedAndBlockedMarineAtSeam() {
    var open = basin(7900, 9500, 1700, 2700, Integer.MIN_VALUE, true);
    for (int x : new int[] {8190, 8192, 8194})
      check(open.isMarineAt(x, 2048), "connected deep marine core crosses positive seam");
    check(open.cachedRegionCount() <= 4, "marine cache bound");
    // A full ridge across the low corridor prevents a marine claim on its west side.
    var ridge = basin(7900, 9500, 1700, 2700, 8320, true);
    // Ridge must separate from all marine anchors: left bank continentalness remains land.
    check(!ridge.isMarineAt(8190, 2048), "ridge blocks false western ocean");
    check(!ridge.isMarineAt(8192, 2048), "ridge blocks seam ocean");
    check(!ridge.isMarineAt(8194, 2048), "ridge blocks eastern-edge ocean");
    var signed = new V8OceanConnectivity((x, z) -> {
      boolean low = x >= -8500 && x <= -7700 && z >= 1700 && z <= 2700;
      return new V8OceanConnectivity.Point(low ? 60 : 120, x <= -8300 ? -0.4 : 0.4);
    });
    for (int x : new int[] {-8194, -8192, -8190})
      check(signed.isMarineAt(x, 2048), "negative seam marine component connected");
    check(!open.isMarineAt(29_999_999, 0), "outside full V8 lattice fails closed");
    // A marine cell beside a dry corner is not enough to certify a whole
    // sampled column; this protects sub-cell beaches and narrow coast joins.
    var shore = new V8OceanConnectivity((x, z) -> {
      boolean low = x >= 7900 && x <= 9500 && z >= 2048 && z <= 2700;
      return new V8OceanConnectivity.Point(low ? 60 : 120, x >= 8350 ? -0.4 : 0.4);
    });
    check(!shore.isMarineAt(8190, 2047), "dry corner rejects marine designation");
    check(shore.isMarineAt(8190, 2048), "adjacent wet corner quartet can connect");
    for (int offset = -5; offset <= 5; offset++) {
      check(open.isMarineAt(8192 + offset, 2048), "signed seam query order stable");
      check(!ridge.isMarineAt(8192 + offset, 2048), "sealed ridge query order stable");
    }
  }

  /** Independent auditor's dev69 counterexample, with actual deep-core samples. */
  private static void deepInlandEnclosureCannotBecomeMarineV3() {
    for (int sign : new int[] {-1, 1}) {
      int left = sign > 0 ? 7900 : -9500;
      int right = sign > 0 ? 9500 : -7900;
      var lake = new V8OceanConnectivity((x, z) -> {
        boolean depression = x >= left && x <= right && z >= 1700 && z <= 2700;
        return new V8OceanConnectivity.Point(depression ? 20 : 120,
            depression ? -0.4 : 0.4);
      }, V8OceanConnectivity.BASIN_GUARD_POLICY_REVISION);
      var frozenV2 = new V8OceanConnectivity((x, z) -> {
        boolean depression = x >= left && x <= right && z >= 1700 && z <= 2700;
        return new V8OceanConnectivity.Point(depression ? 20 : 120,
            depression ? -0.4 : 0.4);
      });
      for (int dx : new int[] {-2, 0, 2}) {
        int x = sign * 8192 + dx;
        check(!lake.isMarineAt(x, 2048),
            "v3 deep inland dry-ring basin has no outlet at signed seam");
        check(frozenV2.isMarineAt(x, 2048),
            "frozen v2 source classification is not silently rewritten");
      }
      check(lake.cachedRegionCount() <= 4, "v3 closed lake keeps bounded cache");
    }
    // Independently reject an ordinary closed inland basin without deep-core cells.
    var shallow = new V8OceanConnectivity((x, z) -> new V8OceanConnectivity.Point(
        x >= 7900 && x <= 9500 && z >= 1700 && z <= 2700 ? 72 : 120, 0.35),
        V8OceanConnectivity.BASIN_GUARD_POLICY_REVISION);
    check(!shallow.isMarineAt(8192, 2048), "v3 closed shallow basin remains inland");
  }

  /** A genuinely outward-continuing *sampled* marine corridor can still be admitted. */
  private static void boundedOpenPathAndDryRidgesV3() {
    var wide = new V8OceanConnectivity((x, z) -> {
      boolean corridor = x >= 7900 && x <= 9500 && z >= 1700;
      return new V8OceanConnectivity.Point(corridor ? 20 : 120,
          corridor ? -0.4 : 0.4);
    }, V8OceanConnectivity.BASIN_GUARD_POLICY_REVISION);
    check(wide.isMarineAt(8192, 2048), "v3 open marine corridor extends beyond bounded sample graph");
    check(wide.isMarineAt(8190, 2048), "v3 open sea crosses 8192 seam without change");
    check(wide.cachedRegionCount() <= 4, "v3 open path keeps resident cache bounded");
    var ridge = new V8OceanConnectivity((x, z) -> {
      boolean corridor = x >= 7900 && x <= 9500 && z >= 1700 && z <= 2700;
      boolean wall = x >= 8320 && x < 8416;
      return new V8OceanConnectivity.Point(corridor && !wall ? 20 : 120,
          corridor && !wall ? -0.4 : 0.4);
    }, V8OceanConnectivity.BASIN_GUARD_POLICY_REVISION);
    check(!ridge.isMarineAt(8192, 2048), "bounded ridge protects inland seam");
    reject(() -> new V8OceanConnectivity((x, z) ->
        new V8OceanConnectivity.Point(70, -0.6), 99), "unknown routing policy");
  }

  private static void exactBoundarySeedCache() {
    var noMarine = new V8OceanConnectivity((x, z) -> {
      boolean wet = x >= 7900 && x <= 9500 && z >= 1700 && z <= 2700;
      return new V8OceanConnectivity.Point(wet ? 20 : 120, wet ? -0.4 : 0.4);
    }, V8OceanConnectivity.BASIN_GUARD_POLICY_REVISION);
    for (int x = 8128; x <= 8256; x += 32) {
      check(!noMarine.isMarineSeedAt(x, 2048), "closed deep basin not priority-flood sea seed");
      check(!noMarine.isMarineSeedAt(x, 2048), "seed cache preserves denial");
    }
    check(noMarine.cachedSeedResultCount() <= 256 && noMarine.cachedRegionCount() <= 4,
        "new seed and marine caches bounded");
    var connected = new V8OceanConnectivity((x, z) -> {
      boolean wet = x >= 7900 && x <= 9500 && z >= 1700;
      return new V8OceanConnectivity.Point(wet ? 20 : 120, wet ? -0.4 : 0.4);
    }, V8OceanConnectivity.BASIN_GUARD_POLICY_REVISION);
    for (int x = 8128; x <= 8256; x += 32) {
      check(connected.isMarineSeedAt(x, 2048), "open marine corridor can seed boundary");
      check(connected.isMarineSeedAt(x, 2048), "positive seed cache replays");
    }
    check(connected.cachedSeedResultCount() <= 256, "positive cache bounded");
    reject(() -> connected.isMarineSeedAt(8193, 2048), "unaligned boundary seed");
    var old = new V8OceanConnectivity((x, z) ->
        new V8OceanConnectivity.Point(20, -0.4));
    try { old.isMarineSeedAt(8192, 2048); throw new AssertionError("old policy seeded"); }
    catch (IllegalStateException expected) { check(true, "v2 cannot use new boundary seeding"); }
  }

  private static void reversedAndParallelOrders() throws Exception {
    var open = basin(7900, 9500, 1700, 2700, Integer.MIN_VALUE, true);
    var closed = basin(7900, 9500, 1700, 2700, 8320, true);
    for (int i = 11; i >= -11; i--) {
      check(open.isMarineAt(8192 + i, 2048), "reverse order marine seam parity");
      check(!closed.isMarineAt(8192 + i, 2048), "reverse order dry seam parity");
    }
    var pool = Executors.newFixedThreadPool(4);
    try {
      List<Callable<Boolean>> tasks = new ArrayList<>();
      for (int i = 0; i < 32; i++) {
        final int coordinate = 8192 + ((i * 13) % 31) - 15;
        tasks.add(() -> open.isMarineAt(coordinate, 2048)
            && !closed.isMarineAt(coordinate, 2048));
      }
      for (var result : pool.invokeAll(tasks))
        check(result.get(), "parallel ordering independent of query/cache history");
    } finally {
      pool.shutdownNow();
    }
    check(open.cachedRegionCount() <= 4 && closed.cachedRegionCount() <= 4,
        "parallel resident caches still bounded");
  }

  private static DensityGraph.Definition constantGraph(double continental) {
    return new DensityGraph.Definition(1, "density", List.of(
        DensityGraph.Node.constant("base_height", 80),
        DensityGraph.Node.constant("continentalness", continental),
        DensityGraph.Node.constant("zero", 0),
        DensityGraph.Node.operation("inert_continentalness", DensityGraph.Op.MULTIPLY,
            "continentalness", "zero"),
        DensityGraph.Node.operation("height", DensityGraph.Op.ADD,
            "base_height", "inert_continentalness"),
        DensityGraph.Node.operation("y", DensityGraph.Op.Y),
        DensityGraph.Node.constant("negative", -1),
        DensityGraph.Node.operation("negative_y", DensityGraph.Op.MULTIPLY, "y", "negative"),
        DensityGraph.Node.operation("density", DensityGraph.Op.ADD, "height", "negative_y")
    ));
  }
  private static V8GeneratorSnapshot v4(long seed, DensityGraph.Definition graph) {
    var r3 = new V8GeneratorSnapshot(3, seed, -64, 320, graph,
        new GroundSurfaceProfile("height", 4), V8BlendPolicy.HARD, V8SurfacePolicy.FROZEN_R1);
    return r3.withClimateBinding(V8ClimateBinding.fromSavedV7(TerrainOptions.DEFAULT, 2));
  }

  private static void pinnedPolicyAndLegacyReplay() {
    long seed = -100987654321L;
    var r4 = v4(seed, constantGraph(0.35));
    var legacy = V8RoutedWaterSource.author(r4);
    var stitched = V8RoutedWaterSource.authorStitched(r4);
    var guarded = V8RoutedWaterSource.authorBasinGuarded(r4);
    check(legacy.version() == 5 && stitched.version() == 5 && guarded.version() == 5,
        "explicit saved hydrology envelopes");
    check(!legacy.hydrologyBinding().equals(stitched.hydrologyBinding()), "new policy has distinct identity");
    check(legacy.hydrologyBinding().sourceFingerprint().equals(
        V8RoutedWaterSource.fingerprint(r4)), "v1 frozen fingerprint");
    check(stitched.hydrologyBinding().sourceFingerprint().equals(
        V8RoutedWaterSource.stitchedFingerprint(r4)), "v2 fingerprint");
    var r5a = V8GeneratorSnapshot.CODEC.decode(V8GeneratorSnapshot.CODEC.encode(legacy));
    var r5b = V8GeneratorSnapshot.CODEC.decode(V8GeneratorSnapshot.CODEC.encode(stitched));
    var r5c = V8GeneratorSnapshot.CODEC.decode(V8GeneratorSnapshot.CODEC.encode(guarded));
    check(r5c.equals(guarded), "v3 opt-in saved profile roundtrip");
    check(r5a.equals(legacy) && r5b.equals(stitched), "frozen and new saved profile roundtrip");
    var older = V8RoutedWaterSource.open(r5a, seed);
    var safer = V8RoutedWaterSource.open(r5b, seed);
    var basinSafe = V8RoutedWaterSource.open(r5c, seed);
    check(basinSafe.basinGuardedPolicy(), "v3 saved policy recovered on reopen");
    check(!safer.basinGuardedPolicy() && !older.basinGuardedPolicy(),
        "prior v1/v2 routing policy remains frozen");
    check(basinSafe.sourceFingerprint().equals(V8RoutedWaterSource.basinGuardFingerprint(r4)),
        "v3 saved profile pins distinct domain-separated source fingerprint");
    check(!older.stitchedPolicy() && safer.stitchedPolicy(), "explicit policy selection survives reopen");
    for (int x : new int[] {-8194, -8192, -8190, 8190, 8192, 8194}) {
      var oldKind = older.sample(x, 2048).kind();
      var newKind = safer.sample(x, 2048).kind();
      check(oldKind == V8WaterDomainAdmission.WaterKind.OCEAN,
          "legacy policy output unchanged at regional sea edge");
      check(newKind != V8WaterDomainAdmission.WaterKind.OCEAN,
          "no marine core, no new ocean designation");
      check(safer.cachedMarineRegionCount() <= 4, "bounded production marine cache");
      check(basinSafe.sample(x, 2048).kind() != V8WaterDomainAdmission.WaterKind.OCEAN,
          "bounded guard does not change hard-land profile into ocean");
    }
    var reference = V8SavedClimateSource.fromSavedV8(r5b, seed);
    var admitted = V8WaterDomainAdmission.bind(r5b.prepare(), reference, safer.authority());
    check(admitted.sample(8190, 80, 2048).domain() != null, "new fingerprint admitted to ecotone");
    reject(() -> V8RoutedWaterSource.authorStitched(r4.withHydrologyBinding(
        V8HydrologyBinding.pinned("0".repeat(64)))), "cannot silently reauthor r5");
    var missingChannel = new DensityGraph.Definition(1, "density", List.of(
        DensityGraph.Node.constant("height", 80),
        DensityGraph.Node.operation("y", DensityGraph.Op.Y),
        DensityGraph.Node.constant("negative", -1),
        DensityGraph.Node.operation("negative_y", DensityGraph.Op.MULTIPLY, "y", "negative"),
        DensityGraph.Node.operation("density", DensityGraph.Op.ADD, "height", "negative_y")));
    var validUnbound = v4(seed, missingChannel);
    reject(() -> V8RoutedWaterSource.authorStitched(validUnbound),
        "new policy requires continental channel, not a malformed graph");
    reject(() -> V8RoutedWaterSource.open(stitched, seed + 1), "new saved seed guard");
    reject(() -> V8RoutedWaterSource.open(guarded, seed + 1), "v3 saved seed guard");
    reject(() -> V8RoutedWaterSource.authorBasinGuarded(guarded),
        "v3 cannot silently reauthor an existing saved r5");
    check(older.sourceFingerprint().equals(legacy.hydrologyBinding().sourceFingerprint()),
        "legacy fingerprint not migrated");
  }

  public static void main(String[] args) throws Exception {
    closedBasinAtTwoSignedSeams();
    connectedAndBlockedMarineAtSeam();
    deepInlandEnclosureCannotBecomeMarineV3();
    boundedOpenPathAndDryRidgesV3();
    exactBoundarySeedCache();
    reversedAndParallelOrders();
    pinnedPolicyAndLegacyReplay();
    System.out.println("V8_STITCHED_MARINE_PASS checks=" + checks
        + " routeVersions=2,3 v3ClosedDeepBasinsRejected=true"
        + " savedLegacyUnchanged=true actualWatershed=API_DOUBLE");
  }
}
