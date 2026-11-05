package com.shnewbs.deepprimal.core;

/** Creation-time contract, serialized with the generator; never a live global worldgen switch. */
public record TerrainOptions(boolean caves, boolean riverTunnels, boolean dunes,
    boolean glaciers, boolean debris, double mountainScale, double polarDistance) {
  public static final TerrainOptions DEFAULT = new TerrainOptions(true, true, true, true, true, 1.3, 16000);
  public TerrainOptions {
    if (!Double.isFinite(mountainScale) || mountainScale < .5 || mountainScale > 2
        || !Double.isFinite(polarDistance) || polarDistance < 4000 || polarDistance > 1000000)
      throw new IllegalArgumentException("Terrain option outside supported bounds");
  }
}
