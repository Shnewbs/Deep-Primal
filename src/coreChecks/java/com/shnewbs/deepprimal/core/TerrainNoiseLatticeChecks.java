package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Regression for 32-bit hash-lattice wrapping and high-altitude interpolation. */
public final class TerrainNoiseLatticeChecks {
  private static long checks;

  private static void check(boolean valid, String message) {
    checks++;
    if (!valid) throw new AssertionError(message);
  }

  public static void main(String[] args) {
    Random random = new Random(0x21720261102L);
    for (boolean shifted : new boolean[] {false, true}) {
      for (long seed : new long[] {0, 1, -1, 73, Long.MIN_VALUE, Long.MAX_VALUE}) {
        TerrainNoise noise = new TerrainNoise(seed, shifted);
        // Normal world-generation coordinates/scales must remain finite.
        for (int i = 0; i < 30_000; i++) {
          double x = (random.nextDouble() - .5) * 60_000_000;
          double z = (random.nextDouble() - .5) * 60_000_000;
          double scale = 4 + random.nextDouble() * 4092;
          long salt = random.nextLong();
          check(Double.isFinite(noise.noise(x, z, scale, salt)), "ordinary noise");
          check(Double.isFinite(noise.field(x, z, scale, salt)), "ordinary field");
          check(Double.isFinite(noise.volume(x, (random.nextDouble() - .5) * 8192, z, scale, salt)), "ordinary volume");
        }
        // A 0.01-block lattice near the playable world edge exceeds signed int.
        for (int i = 0; i < 5_000; i++) {
          double x = (i % 2 == 0 ? 1 : -1) * (29_500_000 + random.nextDouble() * 500_000);
          double z = (i % 3 == 0 ? 1 : -1) * (29_500_000 + random.nextDouble() * 500_000);
          double value = noise.noise(x, z, .01, random.nextLong());
          check(Double.isFinite(value) && Math.abs(value) <= 3, "saturated horizontal lattice: " + value);
        }
      }
    }
    TerrainNoise unshifted = new TerrainNoise(73);
    for (int i = 0; i < 50_000; i++) {
      double x = random.nextInt(100_000) + .25;
      double z = random.nextInt(100_000) + .75;
      long salt = random.nextLong();
      double original = unshifted.noise(x, z, 1, salt);
      double wrapped = unshifted.noise(x + 0x1.0p32, z - 0x1.0p32, 1, salt);
      check(Double.doubleToLongBits(original) == Double.doubleToLongBits(wrapped), "horizontal hash lattice must wrap exactly");
    }
    for (int i = 0; i < 50_000; i++) {
      double y = (i % 2 == 0 ? 1 : -1) * (0x1.0p31 + random.nextInt(10_000) + .25);
      double q = y;
      long iy = (long)Math.floor(q);
      long salt = random.nextLong();
      double expected = TerrainNoise.lerp(
          unshifted.noise(31.25, -17.75, 1, salt + iy * 7919L),
          unshifted.noise(31.25, -17.75, 1, salt + (iy + 1) * 7919L),
          TerrainNoise.smooth(q - Math.floor(q)));
      double actual = unshifted.volume(31.25, y, -17.75, 1, salt);
      check(Double.doubleToLongBits(actual) == Double.doubleToLongBits(expected), "vertical long lattice interpolation");
    }
    System.out.println("TERRAIN_NOISE_LATTICE_PASS checks=" + checks);
  }
}
