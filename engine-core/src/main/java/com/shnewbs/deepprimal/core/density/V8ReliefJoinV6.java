package com.shnewbs.deepprimal.core.density;

import java.util.List;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

/**
 * Explicit V8 relief recipe 6: low-amplitude plateau/coast join before
 * canyon incision. The graph is absolute-coordinate deterministic and does
 * not read or create water quantities.
 */
public final class V8ReliefJoinV6 {
  public static final int RECIPE_REVISION = 6;
  public static final String OUTPUT = "relief6_before_canyon";

  private V8ReliefJoinV6() {}

  public static List<Node> nodes() {
    return List.of(
        Node.bounded("relief6_plateau_entry", Op.SMOOTHSTEP,
            "continentalness", .12, .48),
        Node.bounded("relief6_coast_release", Op.SMOOTHSTEP,
            "continentalness", -.02, .14),
        Node.operation("relief6_negative_coast", Op.MULTIPLY,
            "relief6_coast_release", "negative_one"),
        Node.operation("relief6_coast_window", Op.ADD,
            "one", "relief6_negative_coast"),
        Node.operation("relief6_join_window", Op.MULTIPLY,
            "relief6_plateau_entry", "relief6_coast_window"),
        Node.noise("relief6_join_noise", noise(820, 22061), true),
        Node.constant("relief6_join_scale", 7),
        Node.operation("relief6_join_delta", Op.MULTIPLY,
            "relief6_join_noise", "relief6_join_scale"),
        Node.operation("relief6_banded_joins", Op.MULTIPLY,
            "relief6_join_delta", "relief6_join_window"),
        Node.operation(OUTPUT, Op.ADD,
            "relief_before_canyon", "relief6_banded_joins")
    );
  }

  private static NoiseSpec noise(double blocks, long salt) {
    return new NoiseSpec(1 / blocks, 1 / blocks, 1 / blocks, 3, 2, .5, salt);
  }
}
