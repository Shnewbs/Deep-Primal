package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.TerrainEngine;
import com.shnewbs.deepprimal.core.TerrainOptions;
import com.shnewbs.deepprimal.core.biome.EcotoneBlend;
import com.shnewbs.deepprimal.core.density.DensityChunk;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Isolated Java21 API-contract test; the real TerrainEngine is NOT linked here. */
public final class V8SavedClimateSourceChecks {
  private static long checks;
  private static void check(boolean yes, String problem) {
    checks++;
    if (!yes) throw new AssertionError(problem);
  }
  private static void rejects(Runnable bad, String label) {
    boolean rejected = false;
    try { bad.run(); }
    catch (IllegalArgumentException | NullPointerException | ArithmeticException expected) { rejected = true; }
    check(rejected, label);
  }
  private static boolean bits(double a, double b) {
    return Double.doubleToLongBits(a) == Double.doubleToLongBits(b);
  }
  private static V8SavedClimateSource newSource(long seed, TerrainOptions options, int channel) {
    return V8SavedClimateSource.fromSavedV7(
        new V8GeneratorSnapshot(3, seed, V8SurfacePolicy.FROZEN_R1),
        new TerrainProfile(options), seed, 7, channel);
  }
  private static void checkSample(V8SavedClimateSource adapter, long seed,
      TerrainOptions opts, int channel, int x, int y, int z) {
    var got = adapter.sample(x, y, z);
    // Separate immutable test oracle, not the adapter's internal TerrainEngine.
    var ref = new TerrainEngine(seed, 7, opts, channel);
    var s = ref.sample(x, z);
    var domain = ref.ocean(x, z) ? EcotoneBlend.Domain.OCEAN
        : s.wet() ? EcotoneBlend.Domain.RIVER : EcotoneBlend.Domain.LAND;
    check(got.domain() == domain, "hydrology domain mismatch at " + x + "," + z);
    check(bits(got.climate().height(), y), "density surface altitude mismatch");
    check(bits(got.climate().temperature(), ref.temperature(x, z, y)), "temperature mismatch");
    check(bits(got.climate().rainfall(), s.rain()), "rainfall mismatch");
    check(bits(got.climate().slope(), s.slope()), "slope mismatch");
    check(bits(got.climate().patch(), ref.noise.field(x, z, 1100., 3130L)), "patch mismatch");
  }

  public static void main(String[] args) throws Exception {
    var opts = new TerrainOptions(7);
    var profile = new TerrainProfile(opts);
    var seed = 0x5b5bc0ffeeL;
    var snap = new V8GeneratorSnapshot(3, seed, V8SurfacePolicy.FROZEN_R1);
    var source = V8SavedClimateSource.fromSavedV7(snap, profile, seed, 7, 6);
    check(source.savedOptions().equals(opts), "saved options lost");
    check(source.savedChannelRevision() == 6, "saved channels lost");
    rejects(() -> V8SavedClimateSource.fromSavedV7(null, profile, seed, 7, 0), "null V8 accepted");
    rejects(() -> V8SavedClimateSource.fromSavedV7(snap, null, seed, 7, 0), "null profile accepted");
    rejects(() -> V8SavedClimateSource.fromSavedV7(new V8GeneratorSnapshot(1, seed,
        V8SurfacePolicy.FROZEN_R1), profile, seed, 7, 0), "legacy revision accepted");
    rejects(() -> V8SavedClimateSource.fromSavedV7(new V8GeneratorSnapshot(2, seed,
        V8SurfacePolicy.FROZEN_R1), profile, seed, 7, 0), "rev2 accepted");
    rejects(() -> V8SavedClimateSource.fromSavedV7(new V8GeneratorSnapshot(3, seed,
        new V8SurfacePolicy("unknown")), profile, seed, 7, 0), "unknown surface policy accepted");
    rejects(() -> V8SavedClimateSource.fromSavedV7(new V8GeneratorSnapshot(3, seed,
        null), profile, seed, 7, 0), "missing surface policy accepted");
    rejects(() -> V8SavedClimateSource.fromSavedV7(snap, new TerrainProfile(null), seed, 7, 0),
        "missing options accepted");
    rejects(() -> V8SavedClimateSource.fromSavedV7(snap, profile, seed ^ 1L, 7, 0),
        "mismatched saved world seed accepted");
    for (int rev : new int[]{0, 1, 6, 8, Integer.MAX_VALUE})
      rejects(() -> V8SavedClimateSource.fromSavedV7(snap, profile, seed, rev, 0),
          "wrong V7 version accepted " + rev);
    for (int channel : new int[]{-1, 7, Integer.MIN_VALUE, Integer.MAX_VALUE})
      rejects(() -> V8SavedClimateSource.fromSavedV7(snap, profile, seed, 7, channel),
          "wrong V7 channel revision accepted " + channel);

    int land = 0, river = 0, ocean = 0, stillLake = 0;
    for (int channel = 0; channel <= 6; channel++) {
      var adapter = newSource(seed, opts, channel);
      for (int i = 0; i < 3072; i++) {
        int x = (i % 2 == 0 ? 1 : -1) * (i * 37 + 11);
        int z = (i % 3 == 0 ? 1 : -1) * (i * 31 + 7);
        int y = 48 + i % 258;
        checkSample(adapter, seed, opts, channel, x, y, z);
        var a = adapter.sample(x, y, z);
        var b = adapter.sample(x, y, z);
        check(a.equals(b), "repeat changed climate");
        switch (a.domain()) {
          case LAND -> land++;
          case RIVER -> river++;
          case OCEAN -> ocean++;
        }
        // Stagnant freshwater must remain protected, not be painted as LAND.
        if (x % 29 == 0 && x % 17 != 0 && z % 19 != 0) {
          stillLake++;
          check(a.domain() == EcotoneBlend.Domain.RIVER, "still lake not excluded");
        }
      }
    }
    check(land > 0 && river > 0 && ocean > 0 && stillLake > 0,
        "not all water/land cases were actually exercised");
    for (long otherSeed : new long[]{Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE}) {
      var a = newSource(otherSeed, new TerrainOptions(3), 2);
      checkSample(a, otherSeed, new TerrainOptions(3), 2, -100_123, 1024, 100_005);
      check(!a.sample(13, 200, 19).equals(source.sample(13, 200, 19)),
          "saved identity unexpectedly equal");
    }

    // Compile the real V8EcotoneColumnPlan source, not an API-double planner.
    // This checks that the bridge's V7 ocean, flowing-river and still-lake
    // hydrology domains survive into the actual prewrite dry-land mask.
    var planned = new V8GeneratorSnapshot.Prepared(snap, -128, 768,
        new GroundSurfaceProfile(8));
    var blockDensity = new DensityChunk(-128, 768, 96);
    var maskSource = newSource(seed, opts, 5);
    var plan = V8EcotoneColumnPlan.build(planned, blockDensity, -64, -80, maskSource);
    var repeated = V8EcotoneColumnPlan.build(planned, blockDensity, -64, -80, maskSource);
    int protectedWater = 0, dryMaterial = 0;
    for (int localZ = 0; localZ < 16; localZ++) for (int localX = 0; localX < 16; localX++) {
      var domain = maskSource.sample(-64 + localX, 96, -80 + localZ).domain();
      var column = plan.column(localX, localZ);
      var replay = repeated.column(localX, localZ);
      check(java.util.Objects.equals(column, replay), "plan changes between replays");
      if (domain == EcotoneBlend.Domain.LAND) {
        dryMaterial++;
        check(column != null, "dry column missing surface plan");
        check(column.topY() == 96 && column.soilDepth() >= 1
            && column.soilDepth() <= 8, "invalid dry soil plan");
      } else {
        protectedWater++;
        check(column == null, "water column was accidentally given soil/snow");
      }
    }
    check(plan.eligibleColumns() == dryMaterial && protectedWater > 0 && dryMaterial > 0,
        "prewrite eligibility total incorrect");
    rejects(() -> V8EcotoneColumnPlan.build(new V8GeneratorSnapshot.Prepared(
        new V8GeneratorSnapshot(1, seed, V8SurfacePolicy.FROZEN_R1),
        -128, 768, new GroundSurfaceProfile(8)), blockDensity, -64, -80, maskSource),
        "V8 revision-1 prewrite accepted");
    rejects(() -> V8EcotoneColumnPlan.build(new V8GeneratorSnapshot.Prepared(
        snap, -128, 767, new GroundSurfaceProfile(8)),
        blockDensity, -64, -80, maskSource), "different vertical bounds accepted");
    rejects(() -> V8EcotoneColumnPlan.build(planned, blockDensity,
        Integer.MAX_VALUE, -80, maskSource), "signed chunk-origin overflow accepted");
    rejects(() -> V8EcotoneColumnPlan.build(planned, blockDensity,
        -64, -80, null), "null source accepted");

    var shared = newSource(seed, opts, 3);
    var cases = new ArrayList<int[]>();
    for (int i = 0; i < 2048; i++)
      cases.add(new int[]{(i * 1039) % 130000 - 65000,
          (i * 11) % 500 - 128, (i * 79) % 120000 - 60000});
    List<V8EcotoneColumnPlan.ClimateDatum> expected = new ArrayList<>();
    for (var xyz : cases) expected.add(shared.sample(xyz[0], xyz[1], xyz[2]));
    try (var pool = Executors.newFixedThreadPool(6)) {
      List<Callable<Long>> jobs = new ArrayList<>();
      for (int order = 0; order < 6; order++) {
        final int permutation = order;
        jobs.add(() -> {
          long count = 0;
          for (int n = 0; n < cases.size(); n++) {
            int i = permutation % 2 == 0 ? n : cases.size() - 1 - n;
            int[] xyz = cases.get(i);
            var got = shared.sample(xyz[0], xyz[1], xyz[2]);
            if (!got.equals(expected.get(i)))
              throw new AssertionError("parallel/reversed saved-source divergence");
            count++;
          }
          return count;
        });
      }
      for (Future<Long> completed : pool.invokeAll(jobs))
        check(completed.get() == cases.size(), "parallel case count mismatch");
    }
    check(shared.sample(-65536, 90, 65536).equals(shared.sample(-65536, 90, 65536)),
        "signed-coordinate repeat differs");
    System.out.println("V8_SAVED_CLIMATE_BRIDGE_PASS checks=" + checks
        + " land=" + land + " river=" + river + " ocean=" + ocean
        + " stillLakes=" + stillLake + " planWater=" + protectedWater
        + " planLand=" + dryMaterial + " parallelQueries=" + (cases.size() * 6)
        + " reference=modeled-v7-API-double");
  }
}
