package com.shnewbs.deepprimal.core;

/** Saved channel revision 9: coherent lake shelves, bed facies and dry-bank vegetation. */
public final class NaturalShore {
  public enum Bed { SAND, SILT, CLAY, GRAVEL }

  /** Vary lake shelves and pools while keeping the admitted footprint and wet-depth floor. */
  public static double lakeBed(TerrainNoise noise, double x, double z,
      double stage, double coarse, double legacyBed) {
    double depth = Math.max(0, stage - coarse);
    double extra = Math.max(0, coarse - legacyBed);
    double broad = noise.field(x, z, 73, 94501);
    double local = noise.field(x, z, 23, 94502);
    double shelf = 1.5 + 3 * (.5 + .5 * broad);
    double admission = smooth(depth / shelf);
    double relief = Math.clamp(.82 + broad * .46 + local * .25, .32, 1.35);
    double shaped = (depth + extra * admission) * (1 + (relief - 1) * admission);
    return stage - Math.max(Math.min(depth, 1.001), shaped);
  }

  /** A bounded dry-bank adjustment which cannot lower the containment floor. */
  public static double bankGround(TerrainNoise noise, double x, double z,
      double stage, double coarse, double ground) {
    double above = coarse - stage;
    if (above < 0 || above >= 5) return ground;
    double envelope = smooth(above / .7) * (1 - smooth((above - 1) / 4));
    return Math.max(stage + .35, ground + noise.field(x, z, 29, 94503) * .7 * envelope);
  }

  /** Only the original shallow wet band can become an exposed margin; no new wet area is added. */
  public static double marginRise(TerrainNoise noise, double x, double z, double coarseDepth) {
    double field = noise.field(x,z,81,94508) * .65 + noise.field(x,z,27,94509) * .35;
    return smooth((field + .4) / .7) * 3.6 - coarseDepth;
  }

  public static boolean exposedBank(TerrainNoise noise, int x, int z, TerrainEngine.Sample sample) {
    return !sample.wet() && sample.sediment() > .98 && sample.slope() < 1.2
        && noise.field(x,z,33,94510) > .25;
  }

  /** Coherent material patches follow transport energy and depositional depth. */
  public static Bed bed(TerrainNoise noise, int x, int z, TerrainEngine.Sample sample) {
    double patch = noise.field(x, z, 47, 94504) * .7 + noise.field(x, z, 17, 94505) * .3;
    if (sample.slope() > .85 || sample.speed() > .5) return Bed.GRAVEL;
    if (sample.speed() > .18) return patch > -.2 ? Bed.GRAVEL : Bed.SAND;
    double depth = sample.water() - sample.ground();
    if (depth > 5 && sample.rain() > .7) return patch > -.1 ? Bed.CLAY : Bed.SILT;
    return patch < -.26 ? Bed.SAND : patch > .42 ? Bed.GRAVEL : Bed.SILT;
  }

  public static double treeChance(int biome, double density, double patch) {
    double original = Vegetation.treeChance(biome, density);
    if (original == 0) return 0;
    double chance = (original + .12) * (.35 + 1.15 * smooth(patch));
    return Math.clamp(chance, 0, biome == 0 ? .20 : 1);
  }

  public static double coverChance(double patch) {
    return .30 + .42 * smooth(patch);
  }

  private static double smooth(double value) {
    double t = Math.clamp(value, 0, 1);
    return t * t * (3 - 2 * t);
  }

  private NaturalShore() {}
}
