package com.shnewbs.deepprimal.core;

import java.util.Arrays;
import java.util.Objects;

/** Deterministic ordered material gradient with optional coherent spatial boundary breakup. */
public final class GradientPalette {
  private static final int MAX_PALETTE_SIZE = 64;
  private static final double MAX_VARIATION = .25;
  private static final double MIN_SCALE = 8;
  private static final double MAX_SCALE = 8192;
  private static final double WORLD_LIMIT = 30_000_000;

  private final int[] materials;
  private final double[] boundaries;
  private final TerrainNoise noise;
  private final double variation;
  private final double scale;
  private final long salt;

  /**
   * @param materials ordered material IDs from the low to high end of the gradient
   * @param boundaries strictly increasing normalized transition points, one fewer than materials
   * @param variation maximum normalized boundary displacement; zero produces a clean linear ramp
   * @param scale spatial scale of coherent breakup, in blocks
   */
  public GradientPalette(
      long seed,
      int[] materials,
      double[] boundaries,
      double variation,
      double scale,
      long salt) {
    Objects.requireNonNull(materials, "materials");
    Objects.requireNonNull(boundaries, "boundaries");
    if (materials.length < 2 || materials.length > MAX_PALETTE_SIZE)
      throw new IllegalArgumentException("gradient palette must contain 2..64 materials");
    if (boundaries.length != materials.length - 1)
      throw new IllegalArgumentException("gradient needs one boundary between each material");
    if (!Double.isFinite(variation) || variation < 0 || variation > MAX_VARIATION)
      throw new IllegalArgumentException("gradient variation must be within 0..0.25");
    if (!Double.isFinite(scale) || scale < MIN_SCALE || scale > MAX_SCALE)
      throw new IllegalArgumentException("gradient scale must be within 8..8192 blocks");

    this.materials = materials.clone();
    this.boundaries = boundaries.clone();
    double previous = 0;
    for (double boundary : this.boundaries) {
      if (!Double.isFinite(boundary) || boundary <= previous || boundary >= 1)
        throw new IllegalArgumentException("gradient boundaries must increase strictly within (0,1)");
      previous = boundary;
    }
    this.noise = new TerrainNoise(seed);
    this.variation = variation;
    this.scale = scale;
    this.salt = salt;
  }

  public int size() {
    return materials.length;
  }

  public int material(double progression, double x, double z) {
    return materials[index(progression, x, z)];
  }

  /** Returns the ordered palette index selected at this world-space point. */
  public int index(double progression, double x, double z) {
    if (!Double.isFinite(progression) || progression < 0 || progression > 1)
      throw new IllegalArgumentException("gradient progression must be finite within 0..1");
    coordinate(x);
    coordinate(z);
    double value = progression;
    if (variation != 0)
      value = Math.clamp(value + Math.clamp(noise.field(x, z, scale, salt), -1, 1) * variation, 0, 1);
    int index = Arrays.binarySearch(boundaries, value);
    return index >= 0 ? index + 1 : -index - 1;
  }

  private static void coordinate(double coordinate) {
    if (!Double.isFinite(coordinate) || Math.abs(coordinate) > WORLD_LIMIT)
      throw new IllegalArgumentException("gradient coordinate must be finite within world bounds");
  }
}
