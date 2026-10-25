package com.shnewbs.deepprimal.core;

/** Explicit organic/topsoil/subsoil/weathered horizons above the same parent rock exposed in cuts. */
public record SoilProfile(int topsoil,int subsoil,int weathered,Kind kind) {
  public enum Kind { LOAM, SAND, CLAY, PODZOL, GRAVEL, FROZEN }
  public int depth() { return topsoil+subsoil+weathered; }
  /** Material revisions 0 and 1: exact legacy horizons. */
  public static SoilProfile at(TerrainEngine.Sample s,Geology.Column g) { return at(s,g,s.slope(),false); }

  /**
   * Material revision 2. The world-space slope jitter breaks iso-slope contours, and a
   * profile with no topsoil or subsoil exposes parent rock instead of a weathered ribbon.
   */
  public static SoilProfile at(TerrainNoise noise,int x,int z,TerrainEngine.Sample s,Geology.Column g) {
    return at(s,g,s.slope()+slopeJitter(noise,x,z),true);
  }

  public static double slopeJitter(TerrainNoise noise,int x,int z) {
    return noise.field(x,z,48,34101)*.32+noise.field(x,z,13,34102)*.14;
  }

  private static SoilProfile at(TerrainEngine.Sample s,Geology.Column g,double slope,boolean outcrop) {
    Kind kind=s.temperature()<.23?Kind.FROZEN:s.wet()?(s.speed()>.3?Kind.GRAVEL:s.rain()>.72?Kind.CLAY:Kind.LOAM)
        :s.ground()<101||s.rain()<.35?Kind.SAND:s.temperature()<.42?Kind.PODZOL:s.rain()>.8?Kind.CLAY:Kind.LOAM;
    int top=slope>1.6?0:kind==Kind.GRAVEL?1:s.rain()>.7?2:1;
    int sub=(int)Math.clamp(1+s.rain()*3+s.sediment()*2-slope*2,0,6);
    int weathered=slope>2.2||outcrop&&top+sub==0&&!s.wet()?0:g.permeability()>.4?2:1;
    return new SoilProfile(top,sub,weathered,kind);
  }
}
