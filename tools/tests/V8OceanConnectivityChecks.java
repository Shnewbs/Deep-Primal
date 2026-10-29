package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainOptions;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import java.util.List;

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
    check(legacy.version() == 5 && stitched.version() == 5, "explicit saved hydrology envelopes");
    check(!legacy.hydrologyBinding().equals(stitched.hydrologyBinding()), "new policy has distinct identity");
    check(legacy.hydrologyBinding().sourceFingerprint().equals(
        V8RoutedWaterSource.fingerprint(r4)), "v1 frozen fingerprint");
    check(stitched.hydrologyBinding().sourceFingerprint().equals(
        V8RoutedWaterSource.stitchedFingerprint(r4)), "v2 fingerprint");
    var r5a = V8GeneratorSnapshot.CODEC.decode(V8GeneratorSnapshot.CODEC.encode(legacy));
    var r5b = V8GeneratorSnapshot.CODEC.decode(V8GeneratorSnapshot.CODEC.encode(stitched));
    check(r5a.equals(legacy) && r5b.equals(stitched), "frozen and new saved profile roundtrip");
    var older = V8RoutedWaterSource.open(r5a, seed);
    var safer = V8RoutedWaterSource.open(r5b, seed);
    check(!older.stitchedPolicy() && safer.stitchedPolicy(), "explicit policy selection survives reopen");
    for (int x : new int[] {-8194, -8192, -8190, 8190, 8192, 8194}) {
      var oldKind = older.sample(x, 2048).kind();
      var newKind = safer.sample(x, 2048).kind();
      check(oldKind == V8WaterDomainAdmission.WaterKind.OCEAN,
          "legacy policy output unchanged at regional sea edge");
      check(newKind != V8WaterDomainAdmission.WaterKind.OCEAN,
          "no marine core, no new ocean designation");
      check(safer.cachedMarineRegionCount() <= 4, "bounded production marine cache");
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
    check(older.sourceFingerprint().equals(legacy.hydrologyBinding().sourceFingerprint()),
        "legacy fingerprint not migrated");
  }

  public static void main(String[] args) {
    closedBasinAtTwoSignedSeams();
    connectedAndBlockedMarineAtSeam();
    pinnedPolicyAndLegacyReplay();
    System.out.println("V8_STITCHED_MARINE_PASS checks=" + checks
        + " routeVersion=2 oceanAtRegionBoundary=falseByDefault"
        + " savedLegacyUnchanged=true actualWatershed=API_DOUBLE");
  }
}
