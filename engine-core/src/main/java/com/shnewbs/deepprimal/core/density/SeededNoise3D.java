package com.shnewbs.deepprimal.core.density;

import java.util.Objects;

/**
 * Immutable genuine 3D gradient noise with fixed arithmetic order and no platform trigonometry.
 * Scalar output is within [-1,1]. Bulk output is bit-identical to scalar output in x-fast order.
 * This is a new field; legacy TerrainNoise and saved V1–V7 geography are unchanged.
 */
public final class SeededNoise3D implements DensityField {
  public static final int ALGORITHM_VERSION = 1;
  public static final int COORDINATE_LIMIT = 32_000_000;
  private static final int[] GX = {1,-1,1,-1,1,-1,1,-1,0,0,0,0};
  private static final int[] GY = {1,1,-1,-1,0,0,0,0,1,-1,1,-1};
  private static final int[] GZ = {0,0,0,0,1,1,-1,-1,1,1,-1,-1};
  private final long seed;
  private final NoiseSpec spec;
  private final double[] fx, fy, fz, weights, px, py, pz;
  private final long[] salts;
  private final double inverseWeight;

  public SeededNoise3D(long seed, NoiseSpec spec) {
    this.seed = seed;
    this.spec = Objects.requireNonNull(spec, "spec");
    // With zero persistence every octave after the first has exactly zero weight.
    // Do not allocate or evaluate those octaves; the saved spec remains unchanged.
    int count = spec.gain() == 0.0 ? 1 : spec.octaves();
    fx = new double[count]; fy = new double[count]; fz = new double[count];
    weights = new double[count]; px = new double[count]; py = new double[count]; pz = new double[count];
    salts = new long[count];
    double frequency = 1, weight = 1, sum = 0;
    for (int i = 0; i < count; i++) {
      fx[i] = spec.frequencyX() * frequency;
      fy[i] = spec.frequencyY() * frequency;
      fz[i] = spec.frequencyZ() * frequency;
      weights[i] = weight; sum += weight;
      salts[i] = mix(spec.salt() + i * 0x9e3779b97f4a7c15L);
      // A seed-specific noninteger origin avoids a shared zero at the lattice origin.
      px[i] = phase(seed ^ salts[i] ^ 0x13b586a51L);
      py[i] = phase(seed ^ salts[i] ^ 0x6a8ef452dL);
      pz[i] = phase(seed ^ salts[i] ^ 0x31658b9a3L);
      frequency *= spec.lacunarity(); weight *= spec.gain();
    }
    inverseWeight = 1 / sum;
  }

  public NoiseSpec spec() { return spec; }
  public long seed() { return seed; }

  @Override
  public double sample(double x, double y, double z) {
    coordinate(x); coordinate(y); coordinate(z);
    return unchecked(x, y, z);
  }

  private double unchecked(double x, double y, double z) {
    double sum = 0;
    for (int i = 0; i < fx.length; i++)
      sum += weights[i] * octave(x * fx[i] + px[i], y * fy[i] + py[i], z * fz[i] + pz[i], salts[i]);
    return sum * inverseWeight;
  }

  /** Validates the whole request before touching output. Coordinates use origin + index*step. */
  public void fill(double[] output, int offset, int sizeX, int sizeY, int sizeZ,
                   double originX, double originY, double originZ,
                   double stepX, double stepY, double stepZ) {
    Objects.requireNonNull(output, "output");
    long count = checkedCount(sizeX, sizeY, sizeZ);
    if (offset < 0 || count > output.length - (long)offset)
      throw new IllegalArgumentException("output range");
    axis(originX, stepX, sizeX); axis(originY, stepY, sizeY); axis(originZ, stepZ, sizeZ);
    int index = offset;
    for (int z = 0; z < sizeZ; z++) {
      double wz = originZ + z * stepZ;
      for (int y = 0; y < sizeY; y++) {
        double wy = originY + y * stepY;
        for (int x = 0; x < sizeX; x++) output[index++] = unchecked(originX + x * stepX, wy, wz);
      }
    }
  }

  static long checkedCount(int x, int y, int z) {
    if (x <= 0 || y <= 0 || z <= 0 || x > 1_048_576 || y > 1_048_576 || z > 1_048_576)
      throw new IllegalArgumentException("positive bounded dimensions required");
    long count = (long)x * y * z;
    if (count > 1_048_576) throw new IllegalArgumentException("sample budget exceeds 1048576");
    return count;
  }

  private static void axis(double origin, double step, int size) {
    coordinate(origin);
    if (!Double.isFinite(step) || step <= 0) throw new IllegalArgumentException("positive finite step required");
    coordinate(origin + (size - 1) * step);
  }

  static void coordinate(double value) {
    if (!Double.isFinite(value) || Math.abs(value) > COORDINATE_LIMIT)
      throw new IllegalArgumentException("coordinate must be finite and within +/-32000000");
  }

  private double octave(double x, double y, double z, long salt) {
    int ix = (int)Math.floor(x), iy = (int)Math.floor(y), iz = (int)Math.floor(z);
    double dx = x - ix, dy = y - iy, dz = z - iz;
    double u = fade(dx), v = fade(dy), w = fade(dz);
    double low = lerp(lerp(gradient(ix,iy,iz,dx,dy,dz,salt), gradient(ix+1,iy,iz,dx-1,dy,dz,salt),u),
        lerp(gradient(ix,iy+1,iz,dx,dy-1,dz,salt), gradient(ix+1,iy+1,iz,dx-1,dy-1,dz,salt),u),v);
    double high = lerp(lerp(gradient(ix,iy,iz+1,dx,dy,dz-1,salt), gradient(ix+1,iy,iz+1,dx-1,dy,dz-1,salt),u),
        lerp(gradient(ix,iy+1,iz+1,dx,dy-1,dz-1,salt), gradient(ix+1,iy+1,iz+1,dx-1,dy-1,dz-1,salt),u),v);
    return lerp(low, high, w) * .5;
  }

  private double gradient(int x, int y, int z, double dx, double dy, double dz, long salt) {
    long h = mix(seed ^ salt ^ x * 0x9e3779b97f4a7c15L ^ y * 0xd1b54a32d192ed03L ^ z * 0x94d049bb133111ebL);
    int g = (int)((h >>> 1) % 12);
    return GX[g] * dx + GY[g] * dy + GZ[g] * dz;
  }

  private static double phase(long value) { return .125 + (mix(value) >>> 11) * 0x1.0p-53 * 1024; }
  private static double fade(double t) { return t*t*t*(t*(t*6-15)+10); }
  private static double lerp(double a, double b, double t) { return a + (b-a)*t; }
  private static long mix(long value) {
    value = (value ^ value >>> 30) * 0xbf58476d1ce4e5b9L;
    value = (value ^ value >>> 27) * 0x94d049bb133111ebL;
    return value ^ value >>> 31;
  }
}
