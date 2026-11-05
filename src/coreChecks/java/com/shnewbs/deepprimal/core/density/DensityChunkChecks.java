package com.shnewbs.deepprimal.core.density;

import java.util.List;

public final class DensityChunkChecks {
  private DensityChunkChecks() {}

  public static void run() {
    var graph = TerrainShapeV8.definition(false).compile(42);
    var chunk = DensityChunk.sample(graph, -16, 64, 32, 64);
    int expectedSolids = 0;
    for (int z = 0; z < DensityChunk.WIDTH; z++) {
      for (int x = 0; x < DensityChunk.WIDTH; x++) {
        int top = 64;
        for (int y = 64; y < 128; y++) {
          boolean expected = graph.sample(-16 + x, y, 32 + z) > 0.0;
          need(chunk.solid(x, y, z) == expected, "chunk occupancy differs from exact graph");
          if (expected) {
            expectedSolids++;
            top = y + 1;
          }
        }
        need(chunk.surfaceExclusive(x, z) == top, "surface differs from exact occupancy");
      }
    }
    need(chunk.solidCount() == expectedSolids, "solid count mismatch");
    int[] visited = {0};
    chunk.forEachSolid((x, y, z) -> {
      need(chunk.solid(x, y, z), "solid iterator visited air");
      visited[0]++;
    });
    need(visited[0] == expectedSolids, "solid iterator count mismatch");
    need(chunk.payloadBytes() <= 16 * 16 * 64 / 8 + 16 * 16 * Integer.BYTES,
        "chunk payload exceeded occupancy bound");

    var threshold = new DensityGraph.Definition(
        DensityGraph.VERSION,
        "density",
        List.of(
            DensityGraph.Node.operation("y", DensityGraph.Op.Y),
            DensityGraph.Node.constant("negative_one", -1),
            DensityGraph.Node.operation("negative_y", DensityGraph.Op.MULTIPLY, "y", "negative_one"),
            DensityGraph.Node.constant("one", 1),
            DensityGraph.Node.operation("density", DensityGraph.Op.ADD, "negative_y", "one")))
        .compile(0);
    var thresholdChunk = DensityChunk.sample(threshold, -DensityGraph.WORLD_LIMIT, -2, 0, 5);
    for (int z = 0; z < 16; z++)
      for (int x = 0; x < 16; x++) {
        need(thresholdChunk.solid(x, -2, z), "negative Y occupancy");
        need(thresholdChunk.solid(x, -1, z), "negative Y occupancy");
        need(thresholdChunk.solid(x, 0, z), "zero Y occupancy");
        need(!thresholdChunk.solid(x, 1, z), "zero density must remain air");
        need(!thresholdChunk.solid(x, 2, z), "negative density must remain air");
        need(thresholdChunk.surfaceExclusive(x, z) == 1, "top-exclusive surface semantics");
      }
    int[] negativeYVisited = {0};
    thresholdChunk.forEachSolid((x, y, z) -> {
      need(y >= -2 && y <= 0, "solid iterator Y decode");
      negativeYVisited[0]++;
    });
    need(negativeYVisited[0] == 16 * 16 * 3, "negative-Y solid iterator count");

    var empty = new DensityGraph.Definition(
        DensityGraph.VERSION, "empty", List.of(DensityGraph.Node.constant("empty", 0)))
        .compile(0);
    var maximum = DensityChunk.sample(empty, 0, -1024, 0, DensityChunk.MAX_HEIGHT);
    need(maximum.solidCount() == 0, "zero density generated solid occupancy");
    need(maximum.surfaceExclusive(15, 15) == -1024, "empty column surface");
    need(maximum.payloadBytes() <= DensityChunk.MAX_HEIGHT * 16 * 16 / 8 + 16 * 16 * Integer.BYTES,
        "maximum chunk payload exceeded bound");

    rejects(() -> DensityChunk.sample(empty, 0, 0, 0, 0), "zero height");
    rejects(() -> DensityChunk.sample(empty, 0, 0, 0, DensityChunk.MAX_HEIGHT + 1), "oversized height");
    rejects(() -> DensityChunk.sample(empty, DensityGraph.WORLD_LIMIT, 0, 0, 16), "X world bound");
    rejects(() -> DensityChunk.sample(empty, 0, DensityGraph.WORLD_LIMIT, 0, 2), "Y world bound");
    rejects(() -> DensityChunk.sample(empty, 1, 0, 0, 16), "unaligned X origin");
    rejects(() -> DensityChunk.sample(empty, 0, 0, -1, 16), "unaligned Z origin");
    rejects(() -> thresholdChunk.solid(0, -3, 0), "voxel bounds");
    rejects(() -> thresholdChunk.surfaceExclusive(16, 0), "column bounds");

    System.out.println(
        "DENSITY_CHUNK_PASS exactV8Samples="
            + (16 * 16 * 64)
            + " maxHeight="
            + DensityChunk.MAX_HEIGHT
            + " boundedOccupancy=true topExclusive=true zeroIsAir=true");
  }

  private static void rejects(Runnable action, String label) {
    try {
      action.run();
      throw new AssertionError("accepted invalid " + label);
    } catch (IllegalArgumentException expected) {
      // Expected validation failure.
    }
  }

  private static void need(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
}
