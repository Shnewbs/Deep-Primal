package com.shnewbs.deepprimal.core;

import java.util.EnumMap;
import java.util.EnumSet;

final class ExposedRockChecks {
  private ExposedRockChecks() {}

  static void run() {
    var rocks = new EnumMap<Geology.Lithology, EnumSet<Geology.Rock>>(Geology.Lithology.class);
    for (var type : Geology.Lithology.values()) rocks.put(type, EnumSet.noneOf(Geology.Rock.class));
    long samples = 0, bandTransitions = 0;
    for (long seed : new long[] {0, 73, 20261020}) {
      var terrain = new TerrainEngine(seed, 7);
      var geology = new Geology(terrain.noise);
      var replay = new Geology(terrain.noise);
      for (int z = -4096; z <= 4096; z += 128)
        for (int x = -4096; x <= 4096; x += 128) {
          var column = geology.column(x, z, terrain.column(x, z));
          var type = geology.lithology(column);
          var strata = geology.outcropColumn(x, z, column);
          var repeated = replay.outcropColumn(x, z, column);
          Geology.Rock previous = null;
          for (int y = 80; y <= 320; y += 2) {
            var rock = strata.rock(y);
            if (rock != repeated.rock(y))
              throw new AssertionError("Outcrop rock replay changed");
            rocks.get(type).add(rock);
            if (previous != null && previous != rock) bandTransitions++;
            previous = rock;
            samples++;
          }
        }
    }
    for (var type : Geology.Lithology.values())
      if (rocks.get(type).size() < 2)
        throw new AssertionError("Insufficient " + type + " outcrop facies: " + rocks.get(type));
    if (samples < 100_000 || bandTransitions < 10_000)
      throw new AssertionError("Insufficient layered outcrop coverage");
    System.out.println("EXPOSED_ROCK_PASS samples=" + samples + " bandTransitions="
        + bandTransitions + " facies=" + rocks + " replay=true");
  }
}
