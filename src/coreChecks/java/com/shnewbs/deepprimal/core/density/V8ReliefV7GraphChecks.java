package com.shnewbs.deepprimal.core.density;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Mandatory real DensityGraph VM regression for explicitly authored relief V7.
 * Runs in CoreChecks; it is NOT a Minecraft native, GPU or save/reopen test.
 */
public final class V8ReliefV7GraphChecks {
  private static long checks, coast, transition, highland, changed;
  private static void require(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }
  private static long bits(double value) { return Double.doubleToRawLongBits(value); }

  private static void survey(long seed, boolean floating) {
    var originalDefinition = TerrainShapeV8.definition(floating);
    var frozenV6 = TerrainShapeV8.definitionReliefV6(floating);
    var v7Definition = TerrainShapeV8.definitionReliefV7(floating);
    require(!frozenV6.equals(v7Definition), "V7 must be explicit and distinct");
    require(v7Definition.nodes().stream().anyMatch(n -> n.id().equals("relief_recipe_revision")
        && n.parameters().getFirst() == 7.0), "saved graph pins V7");
    require(frozenV6.nodes().stream().anyMatch(n -> n.id().equals("relief_recipe_revision")
        && n.parameters().getFirst() == 6.0), "frozen V6 graph pins V6");
    require(v7Definition.nodes().stream().anyMatch(n -> n.id().equals("relief7_before_canyon")),
        "new join is consumed by saved graph");
    var original = originalDefinition.compile(seed);
    var v6 = frozenV6.compile(seed);
    var v7 = v7Definition.compile(seed);
    require(v7Definition.nodes().size() <= DensityGraph.MAX_NODES, "node budget");
    require(v7.depth() <= DensityGraph.MAX_DEPTH, "depth budget");
    require(v7.maximumEvaluationCost() <= DensityGraph.MAX_COST, "VM work budget");

    // Exact signed world columns: the sea-side and canyon masks are frozen,
    // while V7's active join is measured in the sampled continentalness range.
    for (int z = -4096; z <= 4096; z += 128)
      for (int x = -4096; x <= 4096; x += 128) {
        double c = original.channel("continentalness", x, 96, z);
        double h0 = original.channel("height", x, 96, z);
        double h6 = v6.channel("height", x, 96, z);
        double h7 = v7.channel("height", x, 96, z);
        require(Double.isFinite(c) && Double.isFinite(h0) && Double.isFinite(h6)
            && Double.isFinite(h7), "nonfinite signed relief column");
        require((h0 > 96) == (h7 > 96), "historical sea-plane side changed");
        require(bits(original.channel("canyon_cut", x, 96, z)) ==
            bits(v7.channel("canyon_cut", x, 96, z)), "canyon incision changed");
        double window = v7.channel("relief7_join_window", x, 96, z);
        double delta = v7.channel("relief7_banded_joins", x, 96, z);
        require(Double.isFinite(window) && window >= 0 && window <= 1,
            "join window outside unit interval");
        require(Double.isFinite(delta) && Math.abs(delta) <= 7.000001,
            "unbounded relief displacement");
        if (c <= .08) {
          coast++;
          require(bits(h0) == bits(h7), "historical protected coast changed");
          require(bits(h6) == bits(h7), "V6 protected coast changed");
          require(delta == 0.0, "coastal join not zero");
        } else if (c > .10 && c < .34) {
          transition++;
          require(window > 0, "missing broad transition window");
          if (bits(h6) != bits(h7)) changed++;
        } else if (c >= .80) {
          highland++;
          require(bits(h6) == bits(h7), "protected highland changed");
          require(delta == 0.0, "highland join not zero");
        }
        double adjacent = v7.channel("height", x + 1, 96, z);
        require(Double.isFinite(adjacent) && Math.abs(adjacent - h7) < 24,
            "world-column continuity impulse");
      }
  }

  private static String signature(DensityChunk chunk) {
    long hash = 0x9e3779b97f4a7c15L;
    for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
      hash = Long.rotateLeft(hash ^ chunk.surfaceExclusive(x, z), 7);
      for (int y = -64; y < 96; y++)
        hash = Long.rotateLeft(hash ^ (chunk.solid(x, y, z) ? 1 : 0), 1);
    }
    return Long.toUnsignedString(hash, 16);
  }

  private static void chunkOrder(long seed) {
    var graph = TerrainShapeV8.definitionReliefV7(false).compile(seed);
    var positions = List.of(new int[]{-32, -16}, new int[]{-16, -16},
        new int[]{0, -16}, new int[]{-32, 0}, new int[]{-16, 0}, new int[]{0, 0});
    var expected = new ArrayList<String>();
    for (var p : positions)
      expected.add(signature(DensityChunk.sample(graph, p[0], -64, p[1], 160)));
    var reversed = new ArrayList<>(positions);
    Collections.reverse(reversed);
    var reversedHashes = new ArrayList<String>();
    for (var p : reversed)
      reversedHashes.add(signature(DensityChunk.sample(graph, p[0], -64, p[1], 160)));
    Collections.reverse(reversedHashes);
    require(expected.equals(reversedHashes), "reversed signed chunk parity");
    var futures = positions.stream().map(p -> CompletableFuture.supplyAsync(
        () -> signature(DensityChunk.sample(graph, p[0], -64, p[1], 160)))).toList();
    for (int i = 0; i < positions.size(); i++)
      require(expected.get(i).equals(futures.get(i).join()), "parallel chunk parity");
  }

  public static void main(String[] args) {
    for (long seed : new long[]{0L, 73L, -20261030L, Long.MIN_VALUE + 31})
      survey(seed, false);
    survey(73L, true);
    chunkOrder(73L);
    require(coast > 100 && transition > 20,
        "incomplete multi-seed/climate V7 survey: coast="+coast+
            " transition="+transition+" highland="+highland);
    require(changed > 20, "V7 did not correct the narrow V6 join: changed="+changed);
    System.out.println("V8_RELIEF_V7_GRAPH_PASS assertions=" + checks +
        " changed=" + changed + " coast=" + coast +
        " transition=" + transition + " highland=" + highland +
        " signedChunkParity=true native=false realMinecraft=false");
  }
}
