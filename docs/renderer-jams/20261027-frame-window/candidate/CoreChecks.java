package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Dependency-free differential checks for hot-path math and bounded memory behavior. */
public final class CoreChecks {
  public static void main(String[] args) throws Exception {
    FrameIntervalChecks.main(args);
    AquiferTickChecks.main(args);
    EngineIntegrationTest.main(new String[0]);
    CacheLifecycleChecks.main(new String[0]);
    CoreCloseLifecycleChecks.main(new String[0]);
    PipelineInlineCloseChecks.main(new String[0]);
    ByteLruOverflowChecks.main(new String[0]);
    DensityGridFiniteChecks.main(new String[0]);

    com.shnewbs.deepprimal.core.lod.LodStoreBudgetChecks.main(new String[0]);
    com.shnewbs.deepprimal.core.lod.LodByteAccountingChecks.main(new String[0]);
    TerrainV6Checks.run();
    TerrainV7Checks.run();
    GradientPaletteChecks.run();
    SoilTextureChecks.run();
    com.shnewbs.deepprimal.core.density.NoiseCoreChecks.main(new String[0]);
    com.shnewbs.deepprimal.core.density.DensityGraphChecks.main(new String[0]);
    com.shnewbs.deepprimal.core.density.ReuseChecks.main(new String[0]);
    com.shnewbs.deepprimal.core.density.AdversarialChecks.main(new String[0]);
    com.shnewbs.deepprimal.core.density.DensityChunkChecks.run();
    WaterBedChecks.run();
    DepositPlanChecks.run();
    WaterBudgetChecks.run();
    SplitAdmissionChecks.main(args);
    StreamSourceCapacityChecks.run();
    BoatBuoyancyChecks.run();
    TerrainV4Checks.run();
    DepositFieldChecks.run();
    DepositAllocationChecks.run();
    WaterAnimationChecks.run();
    WaterFlowTextureChecks.run();
    WaterFlowBlendChecks.run();
    WaterFlowFieldChecks.run();
    WaterCornerPacketChecks.run();
    WaterCornerBulkChecks.run();
    WaterBoundaryIndexChecks.run();
    WaterCornerRetryChecks.run();
    WaterRetirementStateChecks.run();
    WaterSurfaceShapeChecks.run();
    WaterfallSideChecks.run();
    CaveColumnChecks.run();
    IntegratedEngineChecks.run();
    EngineAuditChecks.run();
    TerrainRevisionChecks.run();
    WatershedChecks.run();
    WaterwayChecks.run();
    DrainageChecks.run();
    RuntimeChecks.run();
    HorizonChecks.run();
    WaterDynamicsChecks.run();
    HydrologyChecks.run();
    check(
        RendererPolicy.ownsRenderer(java.util.Set.of("deepprimal", "lithium", "ferritecore")),
        "unrelated optimizers retain renderer");
    for (String id : RendererPolicy.REPLACEMENTS)
      check(
          !RendererPolicy.ownsRenderer(java.util.Set.of(id)), "replacement owns rendering: " + id);
    for (int x : new int[] {-58000, -1, 0, 1, 58000})
      for (int z : new int[] {-58000, -1, 0, 1, 58000}) {
        long key = FarTerrain.key(x, z);
        check(FarTerrain.x(key) == x && FarTerrain.z(key) == z, "signed tile key");
      }
    check(FarTerrain.inRange(-1, -1, 0, 0, 1), "negative tile touching view");
    check(!FarTerrain.inRange(9, 0, 0, 0, 256), "outside horizon");
    check(FarTerrain.inRange(7, 0, 0, 0, 256), "inside horizon");
    for (int capacity = 2; capacity <= DistancePolicy.MAX_RENDER; capacity++) {
      int buckets = DistancePolicy.queuePriorityCount(46, capacity);
      check(capacity + 2 < buckets, "tracker removal priority fits queue");
      check(buckets >= 46, "native generation queue bounds retained");
      if (capacity <= 43) check(buckets == 46, "ordinary views retain native allocation");
    }
    check(DistancePolicy.parse("0", 0, 96).orElseThrow() == 0, "full-height input");
    check(DistancePolicy.parse("1", 0, 96).orElseThrow() == 1, "minimum vertical radius");
    check(
        VerticalRange.fogScale(2, 64, 8) == 0.25f, "vertical fog extends beyond horizontal range");
    check(VerticalRange.fogScale(16, 64, 2) == 8.0f, "vertical fog closes within smaller radius");
    check(VerticalRange.fogScale(2, 64, 0) == 0.03125f, "full-height fog radius");
    for (String invalid : new String[] {"", "-1", "97", "1.0", "no", "1000"})
      check(DistancePolicy.parse(invalid, 0, 96).isEmpty(), "invalid vertical input");
    for (int worldMin : new int[] {-24, -8, 0})
      for (int worldSize : new int[] {24, 48, 64}) {
        for (int radius : new int[] {0, 1, 2, 8, 32, 96})
          for (int camera = worldMin - 4; camera <= worldMin + worldSize + 4; camera++) {
            int count = VerticalRange.count(worldSize, radius);
            int low = VerticalRange.low(camera, worldMin, worldSize, radius);
            boolean[] found = new boolean[count];
            for (int slot = 0; slot < count; slot++) {
              int y = VerticalRange.coordinate(low, count, slot);
              check(
                  y >= low && y < low + count && !found[y - low], "unique vertical ring coverage");
              check(Math.floorMod(y, count) == slot, "negative vertical lookup");
              found[y - low] = true;
            }
            check(
                low >= worldMin && low + count <= worldMin + worldSize,
                "window inside saved height");
          }
      }
    WorkBudget budget = new WorkBudget();
    for (int i = 0; i < 100; i++) budget.observe(50, 16.67, 0.5);
    check(budget.fraction() == 0.125, "work backoff floor");
    for (int i = 0; i < 59; i++) budget.observe(16, 16.67, 0.5);
    check(budget.fraction() == 0.125, "recovery hysteresis");
    budget.observe(16, 16.67, 0.5);
    check(budget.fraction() > 0.125, "gradual recovery");
    for (int i = 0; i < 10000; i++) budget.observe(16, 16.67, 0.5);
    check(budget.fraction() == 1, "recovery ceiling");
    budget.observe(1, 16.67, 0.9);
    check(budget.fraction() < 1, "heap pressure backoff");
    Random random = new Random(20261018L);
    int checked = 0;
    for (int lower : new int[] {-384, -128}) {
      for (int i = 0; i < 100_000; i++) {
        // Quarter-block heights exercise negative coordinates and exact cell boundaries.
        double h = (random.nextInt(6000) - 2000) / 4.0;
        int expected = lower;
        for (int y = 632; y >= lower; y -= 8)
          if (h > y) {
            expected = y;
            break;
          }
        int actual = ColumnHeight.surfaceY(h, lower, 632, 8);
        if (actual != expected) throw new AssertionError(h + ": " + actual + " != " + expected);
        checked++;
      }
    }
    BoundedLru<Integer, Integer> cache = new BoundedLru<>(3);
    cache.put(1, 1);
    cache.put(2, 2);
    cache.put(3, 3);
    check(cache.get(1) == 1, "cache hit");
    cache.put(4, 4);
    check(cache.get(2) == null && cache.size() == 3, "LRU eviction");
    for (int i = 0; i < 100_000; i++) cache.put(i, i);
    check(cache.size() == 3, "cache capacity under stress");
    cache.clear();
    check(cache.size() == 0 && cache.hits() == 0, "reload reset");
    BoundedLru<Integer, Integer> disabled = new BoundedLru<>(0);
    disabled.put(1, 1);
    check(disabled.size() == 0, "disabled cache");
    TickWindow ticks = new TickWindow(3);
    ticks.record(1_000_000);
    ticks.record(2_000_000);
    ticks.record(3_000_000);
    ticks.record(4_000_000);
    check(ticks.count() == 3 && ticks.snapshot().medianMs() == 3, "tick ring rollover");
    ticks.clear();
    check(ticks.snapshot().count() == 0, "tick reset");
    System.out.println(
        "PASS: "
            + checked
            + " differential surface cases, bounded-cache stress, tick-ring rollover.");
  }

  private static void check(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }
}

