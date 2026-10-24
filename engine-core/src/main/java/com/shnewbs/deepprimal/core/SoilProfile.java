package com.shnewbs.deepprimal.core;

/** Explicit organic/topsoil/subsoil/weathered horizons above the same parent rock exposed in cuts. */
public record SoilProfile(int topsoil,int subsoil,int weathered,Kind kind) {
  public enum Kind { LOAM, SAND, CLAY, PODZOL, GRAVEL, FROZEN }
  public int depth() { return topsoil+subsoil+weathered; }
  public static SoilProfile at(TerrainEngine.Sample s,Geology.Column g) {
    Kind kind=s.temperature()<.23?Kind.FROZEN:s.wet()?(s.speed()>.3?Kind.GRAVEL:s.rain()>.72?Kind.CLAY:Kind.LOAM)
        :s.ground()<101||s.rain()<.35?Kind.SAND:s.temperature()<.42?Kind.PODZOL:s.rain()>.8?Kind.CLAY:Kind.LOAM;
    double slope=s.slope();
    int top=slope>1.6?0:kind==Kind.GRAVEL?1:s.rain()>.7?2:1;
    int sub=(int)Math.clamp(1+s.rain()*3+s.sediment()*2-slope*2,0,6);
    int weathered=slope>2.2?0:g.permeability()>.4?2:1;
    return new SoilProfile(top,sub,weathered,kind);
  }
}
