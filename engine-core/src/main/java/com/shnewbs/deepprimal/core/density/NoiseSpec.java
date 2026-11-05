package com.shnewbs.deepprimal.core.density;

/** Immutable stage-one profile. Frequencies are cycles per block; no live global settings. */
public record NoiseSpec(double frequencyX, double frequencyY, double frequencyZ,
                        int octaves, double lacunarity, double gain, long salt) {
  public static final NoiseSpec DEFAULT = new NoiseSpec(1.0 / 192, 1.0 / 96, 1.0 / 192, 4, 2, .5, 0);

  public NoiseSpec {
    if (octaves < 1 || octaves > 8) throw new IllegalArgumentException("octaves must be 1..8");
    finiteRange("lacunarity", lacunarity, 1, 4);
    finiteRange("gain", gain, 0, 1);
    for (double f : new double[] {frequencyX, frequencyY, frequencyZ}) {
      finiteRange("frequency", f, 1.0 / 1_048_576, 16);
      for (int i = 1; i < octaves; i++) f *= lacunarity;
      if (f > 16) throw new IllegalArgumentException("highest octave frequency must be <=16");
    }
  }

  private static void finiteRange(String name, double value, double min, double max) {
    if (!Double.isFinite(value) || value < min || value > max)
      throw new IllegalArgumentException(name + " must be finite and within " + min + ".." + max);
  }
}
