package com.shnewbs.deepprimal.core;

/** Explicit organic/topsoil/subsoil/weathered horizons above the same parent rock exposed in cuts. */
public record SoilProfile(int topsoil,int subsoil,int weathered,Kind kind) {
  public enum Kind { LOAM, SAND, CLAY, PODZOL, GRAVEL, FROZEN }
  public int depth() { return topsoil+subsoil+weathered; }
  /** Material revisions 0 and 1: exact legacy horizons. */
  public static SoilProfile at(TerrainEngine.Sample s,Geology.Column g) { return at(s,g,s.slope(),false,true); }

  /**
   * Material revision 2. The world-space slope jitter breaks iso-slope contours, and a
   * profile with no topsoil or subsoil exposes parent rock instead of a weathered ribbon.
   */
  public static SoilProfile at(TerrainNoise noise,int x,int z,TerrainEngine.Sample s,Geology.Column g) {
    return at(s,g,s.slope()+slopeJitter(noise,x,z),true,false);
  }

  public static double slopeJitter(TerrainNoise noise,int x,int z) {
    return noise.field(x,z,48,34101)*.32+noise.field(x,z,13,34102)*.14;
  }

  private static SoilProfile at(TerrainEngine.Sample s,Geology.Column g,double slope,
      boolean outcrop, boolean legacyMaterials) {
    // Frozen material revisions 0/1 must replay the original dry-soil threshold.
    // Revision 2 retains its existing dither byte-for-byte until a new saved
    // material revision can explicitly opt into a corrected two-sided blend.
    if (legacyMaterials) {
      Kind kind=s.temperature()<.23?Kind.FROZEN:s.wet()?(s.speed()>.3?Kind.GRAVEL:s.rain()>.72?Kind.CLAY:Kind.LOAM)
          :s.ground()<101||s.rain()<.35?Kind.SAND:s.temperature()<.42?Kind.PODZOL:s.rain()>.8?Kind.CLAY:Kind.LOAM;
      return profile(s,g,slope,outcrop,kind);
    }
    Kind kind;
    if (s.temperature() < .23) {
      kind = Kind.FROZEN;
    } else if (s.wet()) {
      kind = s.speed() > .3 ? Kind.GRAVEL : s.rain() > .72 ? Kind.CLAY : Kind.LOAM;
    } else {
      double rain = s.rain();
      double ground = s.ground();
      boolean sandByGround = ground < 101;
      boolean sandByRain = rain < .35;
      if (sandByGround || sandByRain) {
        // Soft transition zone around rain = 0.35 to avoid hard sand/dirt line
        if (!sandByGround && rain > .33 && rain < .37) {
          long h = Double.doubleToLongBits(ground) ^ Double.doubleToLongBits(rain) ^ Double.doubleToLongBits(s.temperature());
          double r = ((h & 0xffffffffL) / (double)0xffffffffL);
          double t = (0.35 - rain) / 0.04; // -1 to 1
          if (r < 0.5 + t * 0.5) {
            kind = Kind.SAND;
          } else {
            kind = Kind.LOAM;
          }
        } else {
          kind = Kind.SAND;
        }
      } else {
        kind = s.temperature() < .42 ? Kind.PODZOL : s.rain() > .8 ? Kind.CLAY : Kind.LOAM;
      }
    }
    return profile(s,g,slope,outcrop,kind);
  }

  private static SoilProfile profile(TerrainEngine.Sample s,Geology.Column g,
      double slope,boolean outcrop,Kind kind) {
    int top=slope>1.6?0:kind==Kind.GRAVEL?1:s.rain()>.7?2:1;
    int sub=(int)Math.clamp(1+s.rain()*3+s.sediment()*2-slope*2,0,6);
    int weathered;
    if (slope > 2.2) {
      weathered = 0;
    } else if (outcrop && top + sub == 0 && !s.wet()) {
      weathered = 0;
    } else {
      weathered = g.permeability() > .4 ? 2 : 1;
    }
    return new SoilProfile(top,sub,weathered,kind);
  }
}
