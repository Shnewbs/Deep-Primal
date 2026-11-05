package com.shnewbs.primalengine.core;

/** Original deterministic scalar field. No game noise router or platform state. */
public final class SeedField {
  private final long seed;

  public SeedField(long seed) {
    this.seed = seed;
  }

  public static long mix(long v) {
    v = (v ^ (v >>> 30)) * 0xbf58476d1ce4e5b9L;
    v = (v ^ (v >>> 27)) * 0x94d049bb133111ebL;
    return v ^ (v >>> 31);
  }

  public double at(int x, int z, long salt) {
    return (mix(seed ^ (x * 0x9e3779b97f4a7c15L) ^ (z * 0xd1b54a32d192ed03L) ^ salt) >>> 11)
        * 0x1.0p-53;
  }

  public double noise(double x, double z, double scale, long salt) {
    x /= scale;
    z /= scale;
    int ix = (int) Math.floor(x), iz = (int) Math.floor(z);
    double u = smooth(x - ix), v = smooth(z - iz);
    return lerp(
                lerp(at(ix, iz, salt), at(ix + 1, iz, salt), u),
                lerp(at(ix, iz + 1, salt), at(ix + 1, iz + 1, salt), u),
                v)
            * 2
        - 1;
  }

  public double detail(double x, double z, double scale, long salt) {
    return noise(x, z, scale, salt) * .64
        + noise(x, z, scale / 2, salt + 1) * .25
        + noise(x, z, scale / 4, salt + 2) * .11;
  }

  public static double lerp(double a, double b, double t) {
    return a + (b - a) * t;
  }

  public static double smooth(double t) {
    t = Math.clamp(t, 0, 1);
    return t * t * t * (t * (t * 6 - 15) + 10);
  }
}
