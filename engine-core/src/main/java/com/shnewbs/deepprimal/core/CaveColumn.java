package com.shnewbs.deepprimal.core;

/** Exact vertical reuse of the cave field for one block column. No shared mutable cache. */
public final class CaveColumn {
  private final TerrainNoise noise;
  private final int x, z, low, high, first54, first62;
  private final double[] values;
  private final int offset62;

  public CaveColumn(TerrainNoise noise, int x, int z, double surface, int minY, int maxY) {
    this.noise = noise;
    this.x = x;
    this.z = z;
    low = minY + 12;
    high = Math.min(maxY, (int) Math.floor(Math.min(180, surface - 10)));
    first54 = (int) Math.floor(low / 54.0);
    first62 = (int) Math.floor(low / 62.0);
    // Normal engine columns are at most 2,048 blocks. Keep unusual direct callers bounded.
    if (high < low || (long) high - low > 4096) {
      values = null;
      offset62 = 0;
    } else {
      offset62 = (int) Math.floor(high / 54.0) - first54 + 2;
      int count62 = (int) Math.floor(high / 62.0) - first62 + 2;
      values = new double[offset62 + count62];
      for (int i = 0; i < offset62; i++)
        values[i] = noise.noise(x, z, 54, 140 + (first54 + i) * 7919L);
      for (int i = 0; i < count62; i++)
        values[offset62 + i] = noise.noise(x, z, 62, 740 + (first62 + i) * 7919L);
    }
  }

  private double field(int y, int scale, int first, int offset, long salt) {
    if (values == null) return noise.volume(x, y, z, scale, salt);
    double q = y / (double) scale;
    int iy = (int) Math.floor(q), i = offset + iy - first;
    return TerrainNoise.lerp(values[i], values[i + 1], TerrainNoise.smooth(q - iy));
  }

  public boolean contains(int y) {
    if (y < low || y > high) return false;
    double a = field(y, 54, first54, 0, 140), b = field(y, 62, first62, offset62, 740);
    return a * a + b * b < .012;
  }
}
