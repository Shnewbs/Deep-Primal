package com.shnewbs.deepprimal.core;

public final class HorizonChecks {
  static void run() {
    int comparisons = 0;
    if (MemoryDistancePolicy.renderLimit(8L * 1024 * 1024 * 1024, 48) != 15
        || MemoryDistancePolicy.renderLimit(8L * 1024 * 1024 * 1024, 64) != 13)
      throw new AssertionError("8 GiB crash regression: full-height admission");
    for (int gib = 1; gib <= 64; gib++) {
      int previous = 96;
      for (int sections : new int[] {24, 48, 64, 128}) {
        int limit = MemoryDistancePolicy.renderLimit(gib * 1024L * 1024 * 1024, sections);
        if (limit > previous || limit < 2 || limit > 96)
          throw new AssertionError("Memory guard height monotonicity");
        previous = limit;
      }
    }
    var sync = new SyncRebuildBudget();
    sync.reset(2, 1500000);
    if (!sync.allows(false)) throw new AssertionError("First native build deferred");
    sync.completed(500000);
    sync.completed(500000);
    if (sync.allows(false) || !sync.allows(true))
      throw new AssertionError("Count budget/player edit exemption");
    sync.reset(4, 1500000);
    sync.completed(1600000);
    if (sync.allows(false) || !sync.allows(true))
      throw new AssertionError("Elapsed native build budget");
    sync.reset(2, 1500000);
    if (!sync.allows(false)) throw new AssertionError("Frame budget did not reset");
    var deps = new TransitionDependencies(4);
    Object sourceA = new Object(), sourceB = new Object();
    deps.begin(-512, 0, 48);
    deps.include(sourceA);
    deps.include(null);
    if (!deps.end()) throw new AssertionError("Initial transition missing");
    deps.begin(-512, 0, 48);
    deps.include(sourceA);
    deps.include(null);
    if (deps.end()) throw new AssertionError("Unchanged transition rebuilt");
    deps.begin(-512, 0, 48);
    deps.include(sourceA);
    deps.include(sourceB);
    if (!deps.end()) throw new AssertionError("New local mesh ignored");
    deps.begin(-512, 0, 48);
    deps.include(sourceA);
    if (!deps.end()) throw new AssertionError("Removed dependency ignored");
    deps.begin(-496, 0, 48);
    deps.include(sourceA);
    if (!deps.end()) throw new AssertionError("Moved closure ignored");
    deps.clear();
    deps.begin(-496, 0, 48);
    deps.include(sourceA);
    if (!deps.end()) throw new AssertionError("World change retained old closure");
    if (DistancePolicy.parse("4000", 100, 4000).orElseThrow() != 4000
        || DistancePolicy.parse("2147483648", 0, Integer.MAX_VALUE).isPresent())
      throw new AssertionError("Budget input range/overflow");
    if (HorizonGeometry.sample(new short[] {0, 0, 0, 10}, 1, 256, 256) != 0)
      throw new AssertionError("Transition must match triangle plane, not bilinear saddle");
    for (int detail = 0; detail < 3; detail++) {
      int n = HorizonGeometry.cells(detail);
      short[] heights = new short[(n + 1) * (n + 1)];
      int[] colors = new int[heights.length];
      for (int z = 0; z <= n; z++)
        for (int x = 0; x <= n; x++) {
          heights[z * (n + 1) + x] = (short) (100 + x * 512 / n / 4 + z * 512 / n / 8);
          colors[z * (n + 1) + x] =
              0xff000000 | ((x * 7) & 255) << 16 | ((z * 13) & 255) << 8 | ((x + z * 3) & 255);
        }
      for (int z = 0; z <= n; z++)
        for (int x = 0; x <= n; x++) {
          double actual = HorizonGeometry.sample(heights, n, x * 512.0 / n, z * 512.0 / n);
          if (actual != heights[z * (n + 1) + x])
            throw new AssertionError("Interpolation at sample");
          comparisons++;
        }
      var samples = new HorizonSamples(detail, heights, colors);
      for (int target = detail; target <= 2; target++) {
        var reduced = samples.forDetail(target);
        int cells = HorizonGeometry.cells(target), ratio = n / cells;
        for (int z = 0; z <= cells; z++)
          for (int x = 0; x <= cells; x++) {
            int source = z * ratio * (n + 1) + x * ratio, dest = z * (cells + 1) + x;
            if (reduced.heights()[dest] != heights[source]
                || reduced.colors()[dest] != colors[source])
              throw new AssertionError("Sample reuse changed data");
          }
      }
      var mesh = HorizonMesh.build(heights, colors, detail, -384);
      if (mesh.indices().length != n * n * 6 + n * 4 * 12 + 6)
        throw new AssertionError("Mesh count");
      var reference = LegacyHorizonMesh.build(heights, colors, detail, -384);
      for (int i = 0; i < mesh.indices().length; i++) {
        int index = mesh.indices()[i];
        if (index < 0 || index >= mesh.vertices() || index > 65535)
          throw new AssertionError("Index bounds");
        for (int axis = 0; axis < 3; axis++)
          if (mesh.positions()[index * 3 + axis] != reference.positions()[i * 3 + axis])
            throw new AssertionError("Indexed geometry changed");
        if (mesh.colors()[index] != reference.colors()[i])
          throw new AssertionError("Indexed color changed");
      }
      if (mesh.gpuBytes() >= reference.vertices() * 16L)
        throw new AssertionError("Mesh memory regression");
      if (mesh.gpuBytes() != HorizonMesh.gpuBytes(detail))
        throw new AssertionError("Memory estimate differs from payload");
      if (mesh.minimum() != -384 || mesh.maximum() != 292) throw new AssertionError("Bounds");
      for (int i = 0; i < mesh.vertices(); i++) {
        float x = mesh.positions()[i * 3],
            y = mesh.positions()[i * 3 + 1],
            z = mesh.positions()[i * 3 + 2];
        if (!Float.isFinite(x + y + z)
            || x < 0
            || x > 512
            || z < 0
            || z > 512
            || y < mesh.minimum()
            || y > mesh.maximum()) throw new AssertionError("Invalid geometry");
      }
    }
    // Nonlinear height field: all resolutions share coarse boundary anchors.
    for (int fine = 0; fine < 3; fine++)
      for (int coarse = 0; coarse < 3; coarse++) {
        int a = HorizonGeometry.cells(fine), b = HorizonGeometry.cells(coarse);
        short[] left = new short[(a + 1) * (a + 1)], right = new short[(b + 1) * (b + 1)];
        for (int z = 0; z <= a; z++)
          left[z * (a + 1) + a] = (short) (100 + Math.sin(z * 512.0 / a / 100) * 90);
        for (int z = 0; z <= b; z++)
          right[z * (b + 1)] = (short) (100 + Math.sin(z * 512.0 / b / 100) * 90);
        for (int z = 0; z <= 8; z++) {
          double l = HorizonGeometry.stitched(left, a, a, z * a / 8),
              r = HorizonGeometry.stitched(right, b, 0, z * b / 8);
          if (l != r) throw new AssertionError("Shared boundary anchors");
          comparisons++;
        }
      }
    java.util.Random random = new java.util.Random(8181);
    for (int i = 0; i < 10000; i++) {
      double x = random.nextDouble() * 20000 - 10000,
          z = random.nextDouble() * 20000 - 10000,
          fraction = .125 + random.nextDouble() * .875;
      int high = 480 + random.nextInt(1600),
          fov = 30 + random.nextInt(81),
          tx = random.nextInt(40) - 20,
          tz = random.nextInt(40) - 20;
      var view = HorizonView.create(x, z, high, fov, fraction);
      double dx = Math.max(0, Math.abs((tx + .5) * 512 - x) - 256),
          dz = Math.max(0, Math.abs((tz + .5) * 512 - z) - 256);
      double projection =
          Math.clamp(
              high / 720.0 * Math.tan(Math.toRadians(35)) / Math.tan(Math.toRadians(fov / 2.0)),
              .5,
              3);
      int expected =
          HorizonGeometry.detail(
              Math.sqrt(dx * dx + dz * dz) / Math.max(.5, projection * Math.sqrt(fraction)));
      if (view.detail(tx, tz) != expected)
        throw new AssertionError("Cached view math changed detail");
    }
    if (VisualBudget.frameLimit(10, false, false, 20, 5) != 10
        || VisualBudget.frameLimit(144, true, false, 20, 5) != 144
        || VisualBudget.frameLimit(144, false, true, 20, 5) != 5)
      throw new AssertionError("FPS policy");
    if (VisualBudget.outside(-10, -10, -10, 10, 10, 10, 0, 0, 0, 1)
        || VisualBudget.outside(0, 0, 0, 1, 1, 1, 11, 0, 0, 10)
        || !VisualBudget.outside(0, 0, 0, 1, 1, 1, 12, 0, 0, 10))
      throw new AssertionError("Conservative visual bounds");
    ByteLru<Integer, byte[]> cache = new ByteLru<>(100, 10, v -> v.length);
    cache.put(1, new byte[60]);
    cache.put(2, new byte[60]);
    if (cache.get(1) != null || cache.bytes() != 60) throw new AssertionError("Byte eviction");
    cache.put(2, new byte[20]);
    if (cache.bytes() != 20) throw new AssertionError("Replacement accounting");
    cache.put(3, new byte[101]);
    if (cache.size() != 1) throw new AssertionError("Oversized value retained");
    for (int i = 0; i < 10000; i++) {
      cache.put(i, new byte[11]);
      if (cache.bytes() > 100 || cache.size() > 10) throw new AssertionError("Byte budget");
    }
    cache.clear();
    if (cache.bytes() != 0 || cache.hits() != 0) throw new AssertionError("Clear");
    System.out.println(
        "PASS: horizon snapshots, closed mesh bounds, "
            + comparisons
            + " interpolation/seam cases, byte-budget stress.");
  }

  private HorizonChecks() {}
}
