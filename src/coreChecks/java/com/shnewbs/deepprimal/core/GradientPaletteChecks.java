package com.shnewbs.deepprimal.core;

/** Determinism, ordering, validation and spatial-coherence checks for material gradients. */
public final class GradientPaletteChecks {
  private static int checks;

  public static void run() {
    int[] materials = {11, 12, 13, 14};
    double[] boundaries = {.25, .5, .75};
    var linear = new GradientPalette(73, materials, boundaries, 0, 64, 981);
    materials[0] = -1;
    boundaries[0] = .9;
    check(linear.material(0, 0, 0) == 11, "palette input arrays copied");
    check(linear.material(1, 0, 0) == 14, "upper endpoint");
    check(linear.index(.25, 0, 0) == 1, "transition selects next material");
    check(linear.index(.5, 0, 0) == 2, "middle transition");
    check(linear.index(.75, 0, 0) == 3, "upper transition");
    check(linear.size() == 4, "palette size");

    int previous = -1;
    for (int i = 0; i <= 10_000; i++) {
      int index = linear.index(i / 10_000.0, -16_000 + i * .25, 8_000);
      check(index >= previous, "linear gradient remains ordered");
      check(index <= previous + 1 || previous < 0, "small progression step cannot skip a band");
      previous = index;
    }

    int[] clusteredMaterials = {0, 1, 2, 3, 4};
    double[] clusteredBoundaries = {.2, .4, .6, .8};
    var clustered = new GradientPalette(20261024, clusteredMaterials, clusteredBoundaries, .2, 32, 4501);
    var replay = new GradientPalette(20261024, clusteredMaterials, clusteredBoundaries, .2, 32, 4501);
    boolean[] seen = new boolean[clusteredMaterials.length];
    int[][] field = new int[96][96];
    for (int z = 0; z < field.length; z++) {
      for (int x = 0; x < field[z].length; x++) {
        int wx = x - 48, wz = z - 48;
        int index = clustered.index(.5, wx, wz);
        field[z][x] = index;
        seen[index] = true;
        check(index == replay.index(.5, wx, wz), "same-seed replay");
        check(clustered.material(.5, wx, wz) == clusteredMaterials[index], "material index mapping");
      }
    }
    int representedBands = 0;
    for (boolean found : seen) if (found) representedBands++;
    check(representedBands >= 2, "clustered field varies across ordered bands");
    int isolated = 0, transitions = 0;
    for (int z = 1; z < field.length - 1; z++)
      for (int x = 1; x < field[z].length - 1; x++) {
        int value = field[z][x];
        boolean sameNeighbor = field[z - 1][x] == value || field[z + 1][x] == value
            || field[z][x - 1] == value || field[z][x + 1] == value;
        if (!sameNeighbor) isolated++;
        if (field[z][x + 1] != value) transitions++;
        check(Math.abs(field[z][x + 1] - value) <= 1,
            "coherent neighboring transition skipped palette stages");
      }
    check(isolated < 96, "coherent clusters avoid isolated speckles: " + isolated);
    check(transitions > 0 && transitions < field.length * field.length / 2,
        "clusters vary without checkerboard splatter");

    reject(() -> new GradientPalette(0, new int[] {1}, new double[0], 0, 16, 0));
    reject(() -> new GradientPalette(0, new int[] {1, 2, 3}, new double[0], 0, 16, 0));
    reject(() -> new GradientPalette(0, new int[] {1, 2, 3}, new double[] {.5, .5}, 0, 16, 0));
    reject(() -> new GradientPalette(0, new int[] {1, 2}, new double[] {Double.NaN}, 0, 16, 0));
    reject(() -> new GradientPalette(0, new int[] {1, 2}, new double[] {.5}, .26, 16, 0));
    reject(() -> new GradientPalette(0, new int[] {1, 2}, new double[] {.5}, 0, 7, 0));
    reject(() -> linear.index(Double.NaN, 0, 0));
    reject(() -> linear.index(-.01, 0, 0));
    reject(() -> linear.index(1.01, 0, 0));
    reject(() -> linear.index(.5, Double.POSITIVE_INFINITY, 0));
    reject(() -> linear.index(.5, 0, 30_000_001));

    System.out.println(
        "TERRAIN_GRADIENT_PASS checks=" + checks + " linearBands=4 clusteredBands=5 isolated=" + isolated
            + " transitions=" + transitions + " deterministic=true");
  }

  private static void reject(Runnable action) {
    try {
      action.run();
      throw new AssertionError("expected invalid gradient rejection");
    } catch (IllegalArgumentException expected) {
      checks++;
    }
  }

  private static void check(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }
}
