package com.shnewbs.deepprimal.core.biome;
public final class EcotoneSurfaceField {
  public enum Cover { BARE, GRASS, PODZOL }
  public record Sample(double grass, double podzol, double bare,
      double snow, double vegetation, double soilFactor,
      Cover cover, boolean snowPatch, boolean vegetationPatch) {}
}
