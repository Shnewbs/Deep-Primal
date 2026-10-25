package com.shnewbs.deepprimal.core;

/** USDA fine-earth texture classes; orders, organic cover and horizons are separate axes. */
public enum SoilTexture {
  SAND("sand", 0xd6c49a, .90),
  LOAMY_SAND("loamy_sand", 0xbda77e, .78),
  SANDY_LOAM("sandy_loam", 0xae926a, .66),
  LOAM("loam", 0x9c815f, .50),
  SILT_LOAM("silt_loam", 0xa39478, .40),
  SILT("silt", 0xb3a68b, .32),
  SANDY_CLAY_LOAM("sandy_clay_loam", 0xa58060, .30),
  CLAY_LOAM("clay_loam", 0x98745a, .23),
  SILTY_CLAY_LOAM("silty_clay_loam", 0x99836e, .18),
  SANDY_CLAY("sandy_clay", 0xa27756, .12),
  SILTY_CLAY("silty_clay", 0x91806f, .08),
  CLAY("clay", 0x947b68, .04);

  private final String id;
  private final int color;
  private final double permeability;

  SoilTexture(String id, int color, double permeability) {
    this.id = id;
    this.color = color;
    this.permeability = permeability;
  }

  public String id() { return id; }
  public int color() { return color; }

  /** Gameplay infiltration coefficient, not a measured hydraulic conductivity. */
  public double permeability() { return permeability; }

  /** Percent by mass within the fine-earth fraction; no silent normalization of invalid data. */
  public static SoilTexture classify(double sand, double silt, double clay) {
    if (!Double.isFinite(sand) || !Double.isFinite(silt) || !Double.isFinite(clay)
        || sand < 0 || silt < 0 || clay < 0 || sand > 100 || silt > 100 || clay > 100
        || Math.abs(sand + silt + clay - 100) > 0.000001)
      throw new IllegalArgumentException("Soil fractions must be finite nonnegative percentages summing to 100");
    if (silt + 1.5 * clay < 15) return SAND;
    if (silt + 2 * clay < 30) return LOAMY_SAND;
    if ((clay >= 7 && clay < 20 && sand > 52)
        || (clay < 7 && silt < 50)) return SANDY_LOAM;
    if (clay >= 7 && clay < 27 && silt >= 28 && silt < 50) return LOAM;
    if (silt >= 50 && clay < 27) return silt >= 80 && clay < 12 ? SILT : SILT_LOAM;
    if (clay >= 20 && clay < 35 && silt < 28 && sand > 45) return SANDY_CLAY_LOAM;
    if (clay >= 27 && clay < 40 && sand > 20 && sand <= 45) return CLAY_LOAM;
    if (clay >= 27 && clay < 40 && sand <= 20) return SILTY_CLAY_LOAM;
    if (clay >= 35 && sand > 45) return SANDY_CLAY;
    if (clay >= 40 && silt >= 40) return SILTY_CLAY;
    if (clay >= 40) return CLAY;
    throw new IllegalStateException("Unclassified valid soil fraction: " + sand + "/" + silt + "/" + clay);
  }

  /** Continuous parent-material/weathering field, sampled in world coordinates, never chunk-local. */
  public static SoilTexture at(TerrainNoise noise, int x, int z, TerrainEngine.Sample sample,
      Geology.Column geology) {
    double sand = Math.exp(noise.field(x, z, 640, 34051) * 5
        + (geology.rock((int) Math.floor(sample.ground())) == Geology.Rock.SANDSTONE ? .9 : 0)
        - sample.sediment() * .6);
    double silt = Math.exp(noise.field(x, z, 760, 34063) * 5 + sample.sediment() * .8);
    double clay = Math.exp(noise.field(x, z, 880, 34057) * 5
        + sample.rain() * .5 - geology.permeability() * .75);
    double total = sand + silt + clay;
    return classify(sand * 100 / total, silt * 100 / total, clay * 100 / total);
  }
}
