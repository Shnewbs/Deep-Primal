package com.shnewbs.deepprimal.core;

import java.util.EnumSet;

final class SoilTextureChecks {
  static void run() {
    double[][] centers = {{95, 3, 2}, {82, 12, 6}, {65, 25, 10}, {40, 40, 20},
        {20, 65, 15}, {5, 90, 5}, {55, 15, 30}, {30, 35, 35}, {10, 55, 35},
        {55, 5, 40}, {5, 45, 50}, {20, 20, 60}};
    var found = EnumSet.noneOf(SoilTexture.class);
    int checks = 0;
    for (int i = 0; i < centers.length; i++) {
      var p = centers[i];
      if (SoilTexture.classify(p[0], p[1], p[2]) != SoilTexture.values()[i])
        throw new AssertionError("Texture reference point " + i);
      checks++;
    }
    for (int sand = 0; sand <= 1000; sand++)
      for (int clay = 0; clay <= 1000 - sand; clay++) {
        found.add(SoilTexture.classify(sand / 10., (1000 - sand - clay) / 10., clay / 10.));
        checks++;
      }
    if (found.size() != 12) throw new AssertionError("All twelve texture classes must be reachable");
    for (double[] bad : new double[][] {{-1, 51, 50}, {101, 0, -1}, {0, 0, 0},
        {20, 20, 20}, {Double.NaN, 0, 100}, {0, Double.POSITIVE_INFINITY, 0}}) {
      try {
        SoilTexture.classify(bad[0], bad[1], bad[2]);
        throw new AssertionError("Invalid soil fractions accepted");
      } catch (IllegalArgumentException expected) {
        checks++;
      }
    }
    System.out.println("SOIL_TEXTURE_PASS checks=" + checks + " classes=12 triangleStep=.1 invalidRejected=true");
    distribution();
  }

  private static void distribution() {
    var counts = new java.util.EnumMap<SoilTexture, Integer>(SoilTexture.class);
    int columns = 0;
    for (long seed : new long[] {20261020, Long.MIN_VALUE, 7331}) {
      var engine = new TerrainEngine(seed, 7);
      var geology = new Geology(engine.noise);
      for (int gz = -32; gz <= 32; gz++)
        for (int gx = -32; gx <= 32; gx++) {
          int x = gx * 257 + 13, z = gz * 257 - 7;
          var sample = engine.column(x, z);
          var parent = geology.column(x, z, sample);
          var profile = SoilProfile.at(sample, parent);
          columns++;
          if (!sample.wet() && profile.depth() > 0 && profile.kind() != SoilProfile.Kind.SAND
              && profile.kind() != SoilProfile.Kind.FROZEN && engine.biome(x, z) != 9)
            counts.merge(SoilTexture.at(engine.noise, x, z, sample, parent), 1, Integer::sum);
        }
    }
    System.out.println("SOIL_DISTRIBUTION columns=" + columns + " seeds=3 selectableDrySoils=" + counts);
    if (counts.size() != SoilTexture.values().length)
      throw new AssertionError("All twelve soil textures must occur on selectable dry ground: " + counts);
  }

  private SoilTextureChecks() {}
}
