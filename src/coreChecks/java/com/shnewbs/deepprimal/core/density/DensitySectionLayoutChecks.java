package com.shnewbs.deepprimal.core.density;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

/** Exact V8 product-graph-to-native-section projection, without Minecraft doubles. */
public final class DensitySectionLayoutChecks {
  private static long checks, voxels, sections, emptySections, certified;

  private static void check(boolean value, String why) {
    checks++;
    if (!value) throw new AssertionError(why);
  }

  private static void reject(Runnable action) {
    try { action.run(); }
    catch (IllegalArgumentException | NullPointerException expected) { checks++; return; }
    throw new AssertionError("invalid section operation accepted");
  }

  private static int packedIndex(int x, int localY, int z) {
    return (localY * 256) + (z * 16) + x;
  }

  private static void compare(DensityGraph graph, int ox, int minY, int oz,
                              int height, int budget) {
    DensityChunk baseline = DensityChunk.sample(graph, ox, minY, oz, height);
    var planned = DensityChunk.samplePlanned(graph, ox, minY, oz, height, budget);
    var exactSections = DensitySectionLayout.project(baseline);
    var fastSections = DensitySectionLayout.project(planned.chunk());
    int first = Math.floorDiv(minY, 16), last = Math.floorDiv(minY + height - 1, 16);
    check(exactSections.size() == last - first + 1 && exactSections.size() == fastSections.size(),
          "one section per intersecting vertical section");
    check(planned.totalCells() == (long)256 * height, "certification accounting");
    certified += planned.solidCertificates() + planned.airCertificates();
    long allSolid = 0;
    for (int i = 0; i < exactSections.size(); i++) {
      var section = exactSections.get(i);
      var fast = fastSections.get(i);
      int base = (first + i) * 16;
      check(section.sectionY() == first + i && section.baseY() == base,
            "signed section index and base");
      check(section.minCoveredY() == Math.max(base, minY), "partial section low bound");
      check(section.maxCoveredYExclusive() == Math.min(base + 16, minY + height),
            "partial section high bound");
      check(section.sectionY() == fast.sectionY() && section.solidCount() == fast.solidCount(),
            "exact/planned section metadata");
      check(Arrays.equals(section.packedWords(), fast.packedWords()), "exact/planned native words");
      long[] words = section.packedWords();
      long count = 0;
      for (long value : words) count += Long.bitCount(value);
      check(count == section.solidCount(), "words and count parity");
      check(section.empty() == (count == 0), "empty section accounting");
      if (section.empty()) emptySections++;
      for (int localY = 0; localY < 16; localY++) {
        int worldY = base + localY;
        for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
          boolean expected = worldY >= minY && worldY < minY + height
              && baseline.solid(x, worldY, z);
          int index = packedIndex(x, localY, z);
          boolean word = (words[index >>> 6] & (1L << (index & 63))) != 0;
          check(word == expected && section.solidLocal(x, localY, z) == expected,
                "native section index mismatch at " + x + "," + worldY + "," + z);
          voxels++;
        }
      }
      // The public bitmap accessor cannot permit a caller to change the cached result.
      words[0] = ~words[0];
      check(section.packedWords()[0] != words[0], "native packing must be defensive");

      var enumerated = new ArrayList<Integer>();
      section.forEachSolid((x, y, z) -> {
        int localY = y - base;
        check(localY >= 0 && localY < 16, "enumerated world Y must match section");
        enumerated.add(packedIndex(x, localY, z));
      });
      check(enumerated.size() == count, "solid traversal count");
      for (int k = 1; k < enumerated.size(); k++)
        check(enumerated.get(k - 1) < enumerated.get(k), "native Y/Z/X order");
      allSolid += count;
      sections++;
    }
    check(allSolid == baseline.solidCount() && allSolid == planned.chunk().solidCount(),
          "source and projected solid volumes identical");
    // Heightmap must be independently recoverable from the native palette layout;
    // roofs/floating islands are not assumed to have a solid vertical column.
    for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
      int recovered = minY;
      for (int i = exactSections.size() - 1; i >= 0 && recovered == minY; i--) {
        var section = exactSections.get(i);
        for (int yy = 15; yy >= 0; yy--) {
          int worldY = section.baseY() + yy;
          if (worldY >= minY && worldY < minY + height && section.solidLocal(x, yy, z)) {
            recovered = worldY + 1;
            break;
          }
        }
      }
      check(recovered == baseline.surfaceExclusive(x,z)
          && recovered == planned.chunk().surfaceExclusive(x,z),
          "top-exclusive native heightmap parity: " + x + "," + z);
    }
    try { exactSections.clear(); throw new AssertionError("mutable section list"); }
    catch (UnsupportedOperationException expected) { checks++; }
  }

  public static void main(String[] args) {
    var vertical = new Definition(VERSION, "density", List.of(
        Node.operation("y", Op.Y), Node.constant("minus_one", -1),
        Node.operation("neg_y", Op.MULTIPLY, "y", "minus_one"),
        Node.constant("roof", 17.5),
        Node.operation("density", Op.ADD, "roof", "neg_y"))).compile(7);
    // Nonaligned starting Y, negative-zero crossing, incomplete upper section.
    for (int start : new int[]{-33,-17,-16,-15,-1,0,1,15,16,17})
      for (int height : new int[]{1,15,16,17,33,65})
        compare(vertical, -16, start, 16, height, 32);

    var cave = new Definition(VERSION, "density", List.of(
        Node.operation("y", Op.Y), Node.constant("center", -5),
        Node.operation("centered", Op.ADD, "y", "center"),
        Node.operation("absolute", Op.ABS, "centered"),
        Node.constant("negative", -1),
        Node.operation("inverted", Op.MULTIPLY, "absolute", "negative"),
        Node.constant("roof", 6),
        Node.operation("density", Op.ADD, "inverted", "roof"))).compile(17);
    compare(cave, 0, -32, 0, 79, 256);

    var noise = new Definition(VERSION, "density", List.of(
        Node.noise("density", new NoiseSpec(.025,.034,.021,3,2,.5,71), false)))
        .compile(20261020L);
    compare(noise, -32, -31, 16, 52, 0);
    compare(noise, -32, -31, 16, 52, 256);
    for (boolean floating : new boolean[]{false, true}) {
      var graph = TerrainShapeV8.definition(floating).compile(20261020L);
      compare(graph, 16, 347, -16, 65, 32);
      compare(graph, -48, 48, 32, 33, 256);
    }
    var empty = new Definition(VERSION, "density", List.of(Node.constant("density", 0)))
        .compile(0);
    compare(empty, 0, -1024, 0, DensityChunk.MAX_HEIGHT, 16);
    var full = new Definition(VERSION, "density", List.of(Node.constant("density", 1)))
        .compile(0);
    compare(full, 0, -1023, 0, DensityChunk.MAX_HEIGHT, 8);

    var rng = new Random(0x6bcab117L);
    for (int i = 0; i < 18; i++) {
      int x = 16 * (rng.nextInt(17) - 8), z = 16 * (rng.nextInt(17) - 8);
      compare(noise, x, rng.nextInt(130)-65, z, 1+rng.nextInt(96),
              i % 2 == 0 ? 0 : 1 + rng.nextInt(256));
    }
    compare(vertical, -29_999_984, 30_000_000 - 15, 29_999_984, 16, 256);
    compare(vertical, -29_999_984, -30_000_000, 29_999_984, 16, 256);

    var one = DensitySectionLayout.project(DensityChunk.sample(vertical, 0, -17, 0, 18));
    check(one.getFirst().sectionY() == -2 && one.getLast().sectionY() == 0,
          "floor-division sections across negative origin");
    reject(() -> one.getFirst().solidLocal(-1,0,0));
    reject(() -> one.getFirst().solidLocal(16,0,0));
    reject(() -> one.getFirst().solidLocal(0,-1,0));
    reject(() -> one.getFirst().solidLocal(0,16,0));
    reject(() -> one.getFirst().solidLocal(0,0,16));
    reject(() -> one.getFirst().forEachSolid(null));
    reject(() -> DensitySectionLayout.project(null));
    System.out.println("DENSITY_SECTION_LAYOUT_PASS assertions="+checks
        + " nativePositions="+voxels+" sections="+sections
        + " emptySections="+emptySections+" certifiedCells="+certified
        + " productGraph=true");
  }
  private DensitySectionLayoutChecks() {}
}
