package com.shnewbs.deepprimal.core;

/** Explicit organic/topsoil/subsoil/weathered horizons above the same parent rock exposed in cuts. */
public record SoilProfile(int topsoil,int subsoil,int weathered,Kind kind) {
  public enum Kind { LOAM, SANDY_LOAM, LOAMY_SAND, SAND, CLAY, PODZOL, GRAVEL, FROZEN }
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

  /** Material revision 6 uses a coherent shoreline ecotone instead of per-column hash dithering. */
  public static SoilProfile atCoherentShore(TerrainNoise noise,int x,int z,
      TerrainEngine.Sample s,Geology.Column g) {
    Kind kind;
      double sandWeight = shoreSandWeight(s);
      if (s.temperature() < .23) kind = Kind.FROZEN;
      else if (s.wet()) kind = s.speed() > .3 ? Kind.GRAVEL : s.rain() > .72 ? Kind.CLAY : Kind.LOAM;
      // Coherent world-space variation keeps a mixed shore even at constant elevation/climate.
      else if (noise.field(x,z,40,34103)*.5+.5 < sandWeight) kind = Kind.SAND;
      else if (sandWeight > .65) kind = Kind.LOAMY_SAND;  // More sand-like transition
      else if (sandWeight > .35) kind = Kind.SANDY_LOAM;  // Balanced blend
      else if (sandWeight > .15) kind = Kind.LOAMY_SAND;  // More grass-like transition
      else kind = s.temperature() < .42 ? Kind.PODZOL : s.rain() > .8 ? Kind.CLAY : Kind.LOAM;
      return profile(s,g,s.slope()+slopeJitter(noise,x,z),true,kind);
    }

  /** Continuous sand propensity used by the saved shoreline material revision. */
  public static double shoreSandWeight(TerrainEngine.Sample s) {
      double groundWeight = 1 - smooth((s.ground() - 97) / 12);
      double rainWeight = 1 - smooth((s.rain() - .28) / .16);
      double tempWeight = 1 - smooth((s.temperature() - .42) / .2);
      // Combine multiple factors for smoother ecotone transition
      return Math.max(Math.max(groundWeight, rainWeight), tempWeight * 0.5);
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
        return legacyProfile(s,g,slope,outcrop,kind);
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
      // Enhanced dirt level/depth variation based on biome characteristics
      double rain = s.rain();
      double temp = s.temperature();
      double sediment = s.sediment();
    
      // Topsoil depth: more organic matter in wet, temperate areas
      int top;
      if (slope > 1.6) {
        top = 0;
      } else if (kind == Kind.GRAVEL) {
        top = 1;
      } else if (kind == Kind.CLAY) {
        top = rain > .8 ? 3 : 2;  // Thicker topsoil in very wet clay areas
      } else if (kind == Kind.SAND || kind == Kind.LOAMY_SAND) {
        top = 1;  // Thin topsoil in sandy areas
      } else if (kind == Kind.SANDY_LOAM) {
        top = rain > .6 ? 2 : 1;  // Moderate topsoil
      } else if (kind == Kind.PODZOL) {
        top = temp < .3 ? 2 : 1;  // Thicker in cold areas
      } else if (kind == Kind.FROZEN) {
        top = 1;
      } else {
        top = rain > .7 ? 3 : rain > .5 ? 2 : 1;  // LOAM: more topsoil in wet areas
      }
    
      // Subsoil depth: deeper in areas with more sediment and less slope
      int sub = (int) Math.clamp(1 + rain * 3 + sediment * 2 - slope * 2, 0, 8);
    
      // Weathered layer: thicker in permeable rock, thinner on steep slopes
      int weathered;
      if (slope > 2.2) {
        weathered = 0;
        } else {
          weathered = g.permeability() > .4 ? 2 : 1;
        }
    
        // Add variation for specific soil types
        if (kind == Kind.SAND || kind == Kind.LOAMY_SAND) {
          // Sandy soils have less weathered layer
          weathered = Math.max(0, weathered - 1);
        } else if (kind == Kind.CLAY) {
          // Clay soils can have more weathered material
          weathered += 1;
        }
    
        // Outcrop rule: no weathered ribbon when no topsoil/subsoil on dry land
        if (outcrop && top + sub == 0 && !s.wet()) {
          weathered = 0;
        }
    
        return new SoilProfile(top, sub, weathered, kind);
      }

  private static double smooth(double value) {
    double t=Math.clamp(value,0,1);
    return t*t*t*(t*(t*6-15)+10);
  }

    /** Exact legacy horizon calculation for material revisions 0/1. */
    private static SoilProfile legacyProfile(TerrainEngine.Sample s,Geology.Column g,
        double slope,boolean outcrop,Kind kind) {
      int top = slope > 1.6 ? 0 : kind == Kind.GRAVEL ? 1 : s.rain() > .7 ? 2 : 1;
      int sub = (int) Math.clamp(1 + s.rain() * 3 + s.sediment() * 2 - slope * 2, 0, 6);
      int weathered;
      if (slope > 2.2) {
        weathered = 0;
      } else if (outcrop && top + sub == 0 && !s.wet()) {
        weathered = 0;
      } else {
        weathered = g.permeability() > .4 ? 2 : 1;
      }
      return new SoilProfile(top, sub, weathered, kind);
    }
  }
