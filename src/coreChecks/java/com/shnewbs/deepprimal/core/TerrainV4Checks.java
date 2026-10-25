package com.shnewbs.deepprimal.core;

final class TerrainV4Checks {
  static void run() {
    permeabilitySubset();
    int banks = 0, ores = 0, rocks = 0;
    for (long seed : new long[] {20261020, -914}) {
      var t = new TerrainEngine(seed, 4); var replay = new TerrainEngine(seed, 4);
      var geology = new Geology(t.noise);
      var seen = new java.util.HashSet<Geology.Rock>();
      int sites = 0;
      for (var p : t.waterways(0, 0).probes()) {
        if (!p.source() || p.stage() < 110 || sites++ >= 12) continue;
        for (int z = (int) p.z() - 4; z < p.z() + 4; z++)
          for (int x = (int) p.x() - 4; x < p.x() + 4; x++) {
            var a = t.column(x, z);
            if (!a.equals(replay.column(x, z))) throw new AssertionError("V4 deterministic seam replay");
            if (!a.wet()) continue;
            for (int[] v : new int[][] {{1,0},{-1,0},{0,1},{0,-1}}) {
              var b = t.column(x+v[0], z+v[1]);
              if (!b.wet()) {
                if (Math.floor(b.ground()) + 1 < a.water()) throw new AssertionError("V4 open river bank");
                banks++;
              }
            }
          }
      }
      for (int z = -256; z < 256; z += 4) for (int x = -256; x < 256; x += 4) {
        var c = geology.column(x, z, t.column(x,z));
        for (int y = -110; y < 190; y += 3) {
          seen.add(c.rock(y)); if (c.ore(y, 200) != Geology.Ore.NONE) ores++;
        }
        var adjacent = geology.column(x+1, z, t.column(x+1,z));
        if (Math.floorDiv(x,32) == Math.floorDiv(x+1,32) && c.veinY() != adjacent.veinY())
          throw new AssertionError("Ore center changes between columns");
      }
      rocks += seen.size();
    }
    if (banks < 20 || ores < 100 || rocks < 6) throw new AssertionError("V4 coverage " + banks + "/" + ores + "/" + rocks);
    System.out.println("TERRAIN_V4_PASS banks="+banks+" oreSamples="+ores+" rockSuites="+rocks+" deterministic=true");
  }
  private static void permeabilitySubset() {
    var random = new java.util.Random(261017);
    var seen = new java.util.HashSet<Double>();
    int cases = 0;
    for (long seed : new long[] {0, 73, 20261020, -914}) {
      var terrain = new TerrainEngine(seed, 4);
      var geology = new Geology(terrain.noise);
      var sample = terrain.sample(0, 0);
      for (int i = 0; i < 10000; i++) {
        int x = i < 10 ? -65 + i * 16 : random.nextInt(-30000000, 30000001);
        int z = i < 10 ? 65 - i * 16 : random.nextInt(-30000000, 30000001);
        double fault = terrain.noise.field(x, z, 1400, 1802);
        int province = Math.clamp((int) ((terrain.noise.field(x, z, 2300, 1803) + 1) * 1.5), 0, 2);
        double expected = province == 0 ? .65 : province == 1 ? .16 : .30;
        if (Math.abs(fault) < .07) expected = Math.max(expected, .8);
        if (Double.doubleToLongBits(geology.permeability(x, z)) != Double.doubleToLongBits(expected)
            || Double.doubleToLongBits(geology.column(x, z, sample).permeability())
                != Double.doubleToLongBits(expected))
          throw new AssertionError("Permeability subset changed legacy geography at " + x + "," + z);
        seen.add(expected);
        cases++;
      }
    }
    if (seen.size() != 4) throw new AssertionError("Missing province/fault permeability coverage " + seen);
    System.out.println("GEOLOGY_PERMEABILITY_SUBSET_PASS cases=" + cases
        + " bitExact=true provincesAndFaults=true negativeDistantCoordinates=true");
  }
}
