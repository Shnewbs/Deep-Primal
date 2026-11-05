package com.shnewbs.deepprimal.core.density;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

/** Exact product-graph parity for opt-in V8 chunk occupancy/heightmaps. */
public final class DensityChunkPlanChecks {
  private static long assertions;
  private static long voxels;
  private static long skipped;
  private static int maxQueries;

  private static void check(boolean condition, String message) {
    assertions++;
    if (!condition) throw new AssertionError(message);
  }

  private static void compare(DensityGraph graph, int x, int y, int z, int height, int budget) {
    var reference = DensityChunk.sample(graph, x, y, z, height);
    var planned = DensityChunk.samplePlanned(graph, x, y, z, height, budget);
    var candidate = planned.chunk();
    long cells = (long)16 * 16 * height;
    voxels += cells;
    skipped += planned.solidCertificates() + planned.airCertificates();
    maxQueries = Math.max(maxQueries, planned.classificationQueries());
    check(planned.totalCells() == cells, "per-chunk exact cell accounting");
    check(planned.exactSamples() + planned.solidCertificates() + planned.airCertificates() == cells,
          "no lost or fabricated occupancy");
    check(planned.classificationQueries() <= ((height + 63) / 64) * budget,
          "per-slab classification budget");
    check(candidate.solidCount() == reference.solidCount(), "solid count parity");
    check(candidate.minY() == reference.minY()
          && candidate.maxYExclusive() == reference.maxYExclusive(), "vertical bounds parity");
    check(candidate.payloadBytes() == reference.payloadBytes(), "bitset payload parity");
    for (int cz = 0; cz < 16; cz++) for (int cx = 0; cx < 16; cx++) {
      check(candidate.surfaceExclusive(cx, cz) == reference.surfaceExclusive(cx, cz),
            "exact surface/roof parity at " + (x + cx) + "," + (z + cz));
      for (int dy = 0; dy < height; dy++) {
        int yy = y + dy;
        check(candidate.solid(cx, yy, cz) == reference.solid(cx, yy, cz),
              "exact voxel parity at " + (x + cx) + "," + yy + "," + (z + cz));
      }
    }
    var actual = new ArrayList<Long>();
    var expected = new ArrayList<Long>();
    candidate.forEachSolid((xx, yy, zz) -> actual.add(((long)zz * 16 + xx) * height + yy - y));
    reference.forEachSolid((xx, yy, zz) -> expected.add(((long)zz * 16 + xx) * height + yy - y));
    check(actual.equals(expected), "deterministic solid iterator order");
  }

  private static void reject(Runnable action) {
    try { action.run(); throw new AssertionError("invalid planned sample accepted"); }
    catch (IllegalArgumentException expected) { assertions++; }
  }

  public static void main(String[] args) {
    var plane = new Definition(VERSION, "density", List.of(
        Node.constant("height", 15), Node.operation("y", Op.Y),
        Node.constant("minus_one", -1), Node.operation("negative_y", Op.MULTIPLY, "y", "minus_one"),
        Node.operation("density", Op.ADD, "height", "negative_y"))).compile(0);
    for (int budget : new int[]{0, 1, 8, 64, 256})
      compare(plane, -16, -65, 32, 150, budget); // Includes -1, 0, 64, 128 slab crossings.
    var planeResult = DensityChunk.samplePlanned(plane, -16, 0, 32, 128, 256);
    check(planeResult.airCertificates() > 0 && planeResult.solidCertificates() > 0,
          "partition actually uses both certificate kinds");
    check(planeResult.exactSamples() < 16L * 16 * 128,
          "exact sampler is avoided only for certified cells");
    var zero = DensityChunk.samplePlanned(plane, -16, 0, 32, 128, 0);
    check(zero.exactSamples() == 16L * 16 * 128
        && zero.classificationQueries() == 0 && zero.solidCertificates() == 0,
        "zero-query path must preserve exact sampling");

    var hollow = new Definition(VERSION, "density", List.of(
        Node.operation("y", Op.Y), Node.constant("center", -20),
        Node.operation("offset", Op.ADD, "y", "center"),
        Node.operation("distance", Op.ABS, "offset"), Node.constant("negative", -1),
        Node.operation("minus_distance", Op.MULTIPLY, "distance", "negative"),
        Node.constant("thickness", 5),
        Node.operation("density", Op.ADD, "thickness", "minus_distance"))).compile(7);
    for (int budget : new int[]{0, 4, 64})
      compare(hollow, 0, -32, 0, 120, budget);

    var soilNoise = new Definition(VERSION, "density", List.of(
        Node.operation("y", Op.Y), Node.constant("negative_one", -1),
        Node.operation("negative_y", Op.MULTIPLY, "y", "negative_one"),
        Node.constant("height", 30), Node.operation("base", Op.ADD, "height", "negative_y"),
        Node.noise("jitter", new NoiseSpec(.027, .031, .035, 3, 2, .5, 52), false),
        Node.operation("density", Op.ADD, "base", "jitter"))).compile(20261020);
    for (int budget : new int[]{0, 8, 48, 256})
      compare(soilNoise, -32, 12, -16, 64, budget);

    var xWarp = new Definition(VERSION, "density", List.of(
        Node.operation("x", Op.X), Node.operation("z", Op.Z),
        Node.bounded("shift", Op.CLAMP, "z", -.5, .5),
        Node.constant("zero", 0),
        new Node("density", Op.WARP, List.of("x", "shift", "zero", "zero"),
                 List.of(32.), null))).compile(42);
    for (int budget : new int[]{0, 8, 256})
      compare(xWarp, -16, -16, 0, 70, budget);

    var noiseOnly = new Definition(VERSION, "density", List.of(
        Node.noise("density", new NoiseSpec(.027, .031, .035, 3, 2, .5, 13), false)))
        .compile(20261020);
    for (int budget : new int[]{0, 16, 256})
      compare(noiseOnly, 16, -8, -32, 32, budget);

    // The actual unregistered V8 authored density graph, including terrain
    // overhangs and optional floating-island semantics, must preserve occupancy
    // and top-exclusive heightmaps with bounded certificates and exact fallback.
    for (boolean floating : new boolean[]{false, true}) {
      for (long seed : new long[]{73L, 20261020L}) {
        var authored = TerrainShapeV8.definition(floating).compile(seed);
        compare(authored, -16, 80, 16, 32, 16);
        compare(authored, 32, 356, -16, 32, 48);
      }
    }

    var random = new Random(0x514c00fL);
    for (int k = 0; k < 10; k++) {
      int x = 16 * (random.nextInt(24) - 12);
      int z = 16 * (random.nextInt(24) - 12);
      int minY = random.nextInt(180) - 90;
      compare(soilNoise, x, minY, z, 1 + random.nextInt(96), 1 + random.nextInt(40));
    }

    var empty = new Definition(VERSION, "density", List.of(Node.constant("density", 0)))
        .compile(0);
    compare(empty, 0, -1024, 0, DensityChunk.MAX_HEIGHT, 64);
    var allAir = DensityChunk.samplePlanned(empty, 0, -1024, 0, DensityChunk.MAX_HEIGHT, 64);
    check(allAir.airCertificates() == 16L * 16 * DensityChunk.MAX_HEIGHT
          && allAir.exactSamples() == 0 && allAir.chunk().solidCount() == 0,
          "all-air fast path skips exact samples without fabricating solids");
    var allSolid = new Definition(VERSION, "density", List.of(Node.constant("density", 1)))
        .compile(0);
    var solid = DensityChunk.samplePlanned(allSolid, 0, -1024, 0, 128, 64);
    check(solid.solidCertificates() == 16L * 16 * 128 && solid.exactSamples() == 0,
          "all-solid certificate covers every cell");
    compare(allSolid, 0, -1024, 0, 128, 64);

    compare(plane, -29_999_984, 30_000_000 - 15, 29_999_984, 16, 32);
    reject(() -> DensityChunk.samplePlanned(plane, 0, 0, 0, 0, 1));
    reject(() -> DensityChunk.samplePlanned(plane, 0, 0, 0, 2049, 1));
    reject(() -> DensityChunk.samplePlanned(plane, 0, 0, 0, 16, -1));
    reject(() -> DensityChunk.samplePlanned(plane, 0, 0, 0, 16, 257));
    reject(() -> DensityChunk.samplePlanned(plane, 1, 0, 0, 16, 10));
    reject(() -> DensityChunk.samplePlanned(plane, 0, 29_999_999, 0, 16, 10));
    reject(() -> DensityChunk.samplePlanned(plane, 30_000_000, 0, 0, 16, 10));
    System.out.println("DENSITY_CHUNK_PLAN_PASS assertions=" + assertions
        + " voxels=" + voxels + " certifiedSkipped=" + skipped
        + " maxClassificationQueries=" + maxQueries + " exactProductGraph=true");
  }
  private DensityChunkPlanChecks() {}
}
