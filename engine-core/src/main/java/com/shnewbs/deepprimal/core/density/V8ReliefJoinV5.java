package com.shnewbs.deepprimal.core.density;

import java.util.List;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

/**
 * Frozen, opt-in V8 relief recipe 5: broad, warped foothill/mountain/plateau
 * join band. The result is a PRE-hydrology height proposal, not a chunk edit.
 * The caller must pass it through the historical coast-preserving sea-side
 * blend and then apply the unchanged canyon cut. No water/biome quantities
 * are read or produced here. The complete nodes are saved with the V8 graph.
 */
public final class V8ReliefJoinV5 {
  public static final int RECIPE_REVISION = 5;
  public static final String OUTPUT = "relief5_before_canyon";

  private V8ReliefJoinV5() {}

  /**
   * Append AFTER relief_before_canyon, BEFORE coastal guarding and canyon cut.
   * Absolute-coordinate seeded noise makes the signed-world join continuous
   * regardless of chunk ordering; all widths and salts are frozen for replay.
   * The bounded join cannot change geological strata or routed fluid height.
   */
  public static List<Node> nodes() {
    return List.of(
        Node.noise("relief5_join_warp", noise(1200, 21011), true),
        Node.constant("relief5_warp_size", .06),
        Node.operation("relief5_warp_delta", Op.MULTIPLY,
            "relief5_join_warp", "relief5_warp_size"),
        Node.operation("relief5_shifted_continent", Op.ADD,
            "continentalness", "relief5_warp_delta"),
        Node.bounded("relief5_foothill_entry", Op.SMOOTHSTEP,
            "relief5_shifted_continent", .06, .40),
        Node.bounded("relief5_mountain_entry", Op.SMOOTHSTEP,
            "relief5_shifted_continent", .28, .68),
        Node.operation("relief5_mountain_negative", Op.MULTIPLY,
            "relief5_mountain_entry", "negative_one"),
        Node.operation("relief5_low_mountain", Op.ADD,
            "one", "relief5_mountain_negative"),
        Node.operation("relief5_join_window", Op.MULTIPLY,
            "relief5_foothill_entry", "relief5_low_mountain"),
        Node.noise("relief5_join_noise", noise(640, 21012), true),
        Node.constant("relief5_join_scale", 11),
        Node.operation("relief5_join_height", Op.MULTIPLY,
            "relief5_join_noise", "relief5_join_scale"),
        Node.operation("relief5_banded_joins", Op.MULTIPLY,
            "relief5_join_height", "relief5_join_window"),
        Node.operation(OUTPUT, Op.ADD,
            "relief_before_canyon", "relief5_banded_joins")
    );
  }

  private static NoiseSpec noise(double blocks, long salt) {
    return new NoiseSpec(1 / blocks, 1 / blocks, 1 / blocks, 3, 2, .5, salt);
  }
}
