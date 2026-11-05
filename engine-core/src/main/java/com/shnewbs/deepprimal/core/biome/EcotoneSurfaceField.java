package com.shnewbs.deepprimal.core.biome;

import java.util.Arrays;
import java.util.Objects;

/**
 * Deterministic, spatially coherent V8 land-cover preview derived from *all*
 * ecotone weights. Does not place blocks, modify relief, or touch finite water.
 * Callers must opt into the saved V8 blend policy; old generators never use it.
 *
 * <p>The palette and spatial-noise algorithm are versioned independently so a
 * later native integration cannot silently reinterpret existing V8 previews.
 * Absolute signed world coordinates (not chunk coordinates) drive every draw.
 */
public final class EcotoneSurfaceField {
  public static final int PALETTE_REVISION = 1;
  public static final int FIELD_REVISION = 1;
  public static final int BIOMES = EcotoneBlend.PALETTE_SIZE;
  private static final int SMALL_CELLS = 48;
  private static final int LARGE_CELLS = 160;
  private static final long COVER_SALT = 0x4c91e776a30d26b5L;
  private static final long SNOW_SALT = 0x01ca85a7b229e7d1L;
  private static final long VEGETATION_SALT = 0x52f6441a873ad925L;

  public enum Cover { BARE, GRASS, PODZOL }

  /** Values are nonphysical coverage/density fractions in [0,1]. */
  public record BiomeCover(double grass, double podzol, double snow,
                           double vegetation, double soilFactor) {
    public BiomeCover {
      for (double value : new double[]{grass, podzol, snow, vegetation, soilFactor})
        if (!Double.isFinite(value) || value < 0 || value > 1)
          throw new IllegalArgumentException("biome cover components must be finite [0,1]");
      if (grass + podzol > 1 + 1e-14)
        throw new IllegalArgumentException("grass + podzol must not exceed 1");
    }
  }

  /** A defensive, stable mapping from the frozen 40-biome palette to land-cover fractions. */
  public static final class Palette {
    private final BiomeCover[] covers;

    public Palette(BiomeCover[] covers) {
      Objects.requireNonNull(covers, "covers");
      if (covers.length != BIOMES)
        throw new IllegalArgumentException("exactly " + BIOMES + " biome entries required");
      this.covers = covers.clone();
      for (int id = 0; id < BIOMES; id++)
        Objects.requireNonNull(this.covers[id], "biome " + id);
    }

    public BiomeCover get(int biomeId) { return covers[biomeId]; }
    public BiomeCover[] toArray() { return covers.clone(); }
  }

  /**
   * Explicit experimental preview for the original 40 biome IDs. This is not
   * an asset/art approval or a native material registry mapping. If adopted by
   * a native generator these values must become a persisted/frozen policy.
   */
  public static final Palette PREVIEW_R1 = new Palette(previewPalette());

  public record Sample(double grass, double podzol, double bare,
                       double snow, double vegetation, double soilFactor,
                       Cover cover, boolean snowPatch, boolean vegetationPatch) {
    public Sample {
      Objects.requireNonNull(cover, "cover");
    }
  }

  private final long seed;
  public EcotoneSurfaceField(long worldSeed) { seed = worldSeed; }

  /**
   * Sample already computed, normalized continuous weights at world (x,z).
   * Hydrological domains are deliberately forbidden: river/ocean ownership,
   * physical water level, and constituent accounting are outside this field.
   */
  public Sample sample(int x, int z, EcotoneBlend.Domain domain,
                       EcotoneBlend.Weights weights, Palette palette) {
    if (domain != EcotoneBlend.Domain.LAND)
      throw new IllegalArgumentException("only dry land-cover classification is supported");
    Objects.requireNonNull(weights, "weights");
    Objects.requireNonNull(palette, "palette");
    double grass = 0, podzol = 0, snow = 0, vegetation = 0, soil = 0;
    double sum = 0;
    for (int id = 0; id < BIOMES; id++) {
      double w = weights.weight(id);
      if (!Double.isFinite(w) || w < 0 || w > 1)
        throw new IllegalArgumentException("non-normalized ecotone distribution");
      if (w == 0) continue;
      var b = palette.get(id);
      grass += w * b.grass;
      podzol += w * b.podzol;
      snow += w * b.snow;
      vegetation += w * b.vegetation;
      soil += w * b.soilFactor;
      sum += w;
    }
    if (Math.abs(sum - 1) > 1e-12)
      throw new IllegalArgumentException("ecotone weights do not sum to one");
    double bare = Math.max(0, 1 - grass - podzol);
    // Value-noise distributions are spatially correlated. They cannot produce
    // independent block-level random checkerboards at biome transition lines.
    double coverDraw = correlated(seed ^ COVER_SALT, x, z);
    Cover selected = coverDraw < grass ? Cover.GRASS
        : coverDraw < grass + podzol ? Cover.PODZOL : Cover.BARE;
    boolean snowy = correlated(seed ^ SNOW_SALT, x, z) < snow;
    boolean plants = correlated(seed ^ VEGETATION_SALT, x, z) < vegetation;
    return new Sample(grass, podzol, bare, snow, vegetation, soil, selected, snowy, plants);
  }

  /** Exposes deterministic cover-field value for seam and continuity tests only. */
  public double coverField(int x, int z) { return correlated(seed ^ COVER_SALT, x, z); }

  private static double correlated(long saltedSeed, int x, int z) {
    return .7 * lattice(saltedSeed, x, z, SMALL_CELLS)
        + .3 * lattice(saltedSeed ^ 0x7a96f34b1ce0d525L, x, z, LARGE_CELLS);
  }

  private static double lattice(long seed, int x, int z, int cellSize) {
    long gx = Math.floorDiv(x, cellSize);
    long gz = Math.floorDiv(z, cellSize);
    double dx = Math.floorMod(x, cellSize) / (double) cellSize;
    double dz = Math.floorMod(z, cellSize) / (double) cellSize;
    double u = fade(dx), v = fade(dz);
    double lo = lerp(hash01(seed, gx, gz), hash01(seed, gx + 1, gz), u);
    double hi = lerp(hash01(seed, gx, gz + 1), hash01(seed, gx + 1, gz + 1), u);
    return lerp(lo, hi, v);
  }

  private static double hash01(long seed, long x, long z) {
    long h = mix(seed ^ x * 0x9e3779b97f4a7c15L ^ z * 0xd1b54a32d192ed03L);
    return (h >>> 11) * 0x1.0p-53;
  }

  private static long mix(long n) {
    n = (n ^ (n >>> 30)) * 0xbf58476d1ce4e5b9L;
    n = (n ^ (n >>> 27)) * 0x94d049bb133111ebL;
    return n ^ (n >>> 31);
  }
  private static double fade(double f) { return f*f*f*(f*(f*6-15)+10); }
  private static double lerp(double a, double b, double t) { return a + (b-a)*t; }

  private static BiomeCover[] previewPalette() {
    BiomeCover[] result = new BiomeCover[BIOMES];
    Arrays.fill(result, new BiomeCover(.72, .03, .02, .55, .72));
    for (int id : new int[]{2, 8, 9, 20, 23, 25, 27})
      result[id] = new BiomeCover(.17, .01, 0, .11, .32);
    for (int id : new int[]{5, 16, 17, 19, 26})
      result[id] = new BiomeCover(.22, .09, .83, .15, .44);
    for (int id : new int[]{1, 4, 13, 14, 15})
      result[id] = new BiomeCover(.47, .39, .10, .84, .83);
    for (int id : new int[]{6, 10, 11, 12, 28})
      result[id] = new BiomeCover(.79, .14, .01, .95, .92);
    for (int id : new int[]{7, 18, 21, 22, 24})
      result[id] = new BiomeCover(.26, .03, .24, .21, .28);
    for (int id : new int[]{3, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39})
      result[id] = new BiomeCover(0, 0, 0, 0, 0);
    return result;
  }
}
