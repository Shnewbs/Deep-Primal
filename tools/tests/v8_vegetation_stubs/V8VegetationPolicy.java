package com.shnewbs.deepprimal.engine;
public record V8VegetationPolicy(int version, int anchorSpacing, double densityScale) {
  public V8VegetationPolicy {
    if (version!=1 || (anchorSpacing!=4 && anchorSpacing!=8 && anchorSpacing!=16)
        || !Double.isFinite(densityScale) || densityScale<0 || densityScale>1)
      throw new IllegalArgumentException("policy");
  }
}
