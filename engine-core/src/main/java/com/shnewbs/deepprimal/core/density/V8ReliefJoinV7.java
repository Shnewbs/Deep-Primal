package com.shnewbs.deepprimal.core.density;

import java.util.List;
import static com.shnewbs.deepprimal.core.density.DensityGraph.*;

/**
 * Explicit V8 relief recipe 7: repairs V6's vanishingly narrow effective
 * plateau/coast overlap without changing any V6 saved density graph. The
 * bounded, continuous join is a PRE-hydrology proposal; canyon incision and
 * the existing sea-side protection remain authoritative in TerrainShapeV8.
 * This code never touches liquid quantities, saved chunks, or native registration.
 */
public final class V8ReliefJoinV7 {
  public static final int RECIPE_REVISION = 7;
  public static final String OUTPUT = "relief7_before_canyon";

  private V8ReliefJoinV7() {}

  public static List<Node> nodes() {
    return List.of(
        Node.bounded("relief7_foothill_entry", Op.SMOOTHSTEP,
            "continentalness", .10, .34),
        Node.bounded("relief7_highland_entry", Op.SMOOTHSTEP,
            "continentalness", .56, .80),
        Node.operation("relief7_negative_highland", Op.MULTIPLY,
            "relief7_highland_entry", "negative_one"),
        Node.operation("relief7_low_highland", Op.ADD,
            "one", "relief7_negative_highland"),
        Node.operation("relief7_join_window", Op.MULTIPLY,
            "relief7_foothill_entry", "relief7_low_highland"),
        Node.noise("relief7_join_noise", noise(820, 22071), true),
        Node.constant("relief7_join_scale", 7),
        Node.operation("relief7_join_delta", Op.MULTIPLY,
            "relief7_join_noise", "relief7_join_scale"),
        Node.operation("relief7_banded_joins", Op.MULTIPLY,
            "relief7_join_delta", "relief7_join_window"),
        Node.operation(OUTPUT, Op.ADD,
            "relief_before_canyon", "relief7_banded_joins")
    );
  }

  private static NoiseSpec noise(double blocks, long salt) {
    return new NoiseSpec(1 / blocks, 1 / blocks, 1 / blocks, 3, 2, .5, salt);
  }
}
