package com.shnewbs.deepprimal.core;

/** Seeded gradient fields owned by Deep Primal; no platform noise router. */
public final class TerrainNoise {
  private final long seed;
  private final double phaseX, phaseZ;

  public TerrainNoise(long seed) {
    this(seed, false);
  }

  /** V7 shifts the sampling lattice; V1–V6 retain their exact original fields. */
  public TerrainNoise(long seed, boolean shifted) {
    this.seed = seed;
    phaseX = shifted ? .125 + hash(0, 0, 0x7139L) * 1024 : 0;
    phaseZ = shifted ? .125 + hash(0, 0, 0x7193L) * 1024 : 0;
  }

  public static long mix(long v) {
    v = (v ^ v >>> 30) * 0xbf58476d1ce4e5b9L;
    v = (v ^ v >>> 27) * 0x94d049bb133111ebL;
    return v ^ v >>> 31;
  }

  public double hash(int x, int z, long salt) {
    return (mix(seed ^ x * 0x9e3779b97f4a7c15L ^ z * 0xd1b54a32d192ed03L ^ salt) >>> 11)
        * 0x1.0p-53;
  }

  public static double smooth(double t) {
    t = Math.clamp(t, 0, 1);
    return t * t * t * (t * (t * 6 - 15) + 10);
  }

  public static double lerp(double a, double b, double t) {
    return a + (b - a) * t;
  }

  private double gradient(int x, int z, double dx, double dz, long salt) {
    int g = (int) (hash(x, z, salt) * 8);
    return switch (g) {
      case 0 -> dx;
      case 1 -> -dx;
      case 2 -> dz;
      case 3 -> -dz;
      case 4 -> (dx + dz) * .70710678;
      case 5 -> (dx - dz) * .70710678;
      case 6 -> (-dx + dz) * .70710678;
      default -> (-dx - dz) * .70710678;
    };
  }

  public double noise(double x, double z, double scale, long salt) {
    x /= scale;
    z /= scale;
    x += phaseX;
    z += phaseZ;
    // Cast through long to preserve the 32-bit hash lattice's wraparound instead
    // of saturating at Integer.MIN/MAX_VALUE. Keep fractional offsets separate:
    // subtracting the saturated int produced enormous gradients near the border
    // for fine scales, while normal in-range samples remain bit-identical.
    double floorX = Math.floor(x), floorZ = Math.floor(z);
    int a = (int) (long) floorX, b = (int) (long) floorZ;
    double dx = x - floorX, dz = z - floorZ;
    return lerp(
            lerp(gradient(a, b, dx, dz, salt), gradient(a + 1, b, dx - 1, dz, salt), smooth(dx)),
            lerp(
                gradient(a, b + 1, dx, dz - 1, salt),
                gradient(a + 1, b + 1, dx - 1, dz - 1, salt),
                smooth(dx)),
            smooth(dz))
        * 1.7;
  }

  public double field(double x, double z, double scale, long salt) {
    return noise(x, z, scale, salt) * .65
        + noise(x, z, scale / 2, salt + 1) * .25
        + noise(x, z, scale / 4, salt + 2) * .10;
  }

  public double volume(double x, double y, double z, double scale, long salt) {
    double q = y / scale;
    // A tall/small-scale query must not saturate the vertical lattice at 2^31.
    // The salt arithmetic already uses long, so preserve its full integer range.
    double floorY = Math.floor(q);
    long iy = (long) floorY;
    return lerp(
        noise(x, z, scale, salt + iy * 7919L),
        noise(x, z, scale, salt + (iy + 1) * 7919L),
        smooth(q - floorY));
  }
}
