package com.shnewbs.deepprimal.core;

import java.util.Random;

final class HydrologyChecks {
  static void run() {
    var random = new Random(20261020);
    int[] water = new int[4096];
    int expected = 0;
    for (int i = 0; i < water.length; i++) expected += water[i] = random.nextInt(9);
    for (int i = 0; i < 500000; i++) {
      int a = random.nextInt(water.length), b = random.nextInt(water.length);
      if (a == b) continue;
      int moved =
          switch (i % 3) {
            case 0 -> FluidVolume.down(water[a], water[b]);
            case 1 -> FluidVolume.sideways(water[a], water[b]);
            default ->
                FluidVolume.pressure(
                    random.nextInt(1024) - 384, water[a], random.nextInt(1024) - 384, water[b]);
          };
      int before = water[a] + water[b];
      water[a] -= moved;
      water[b] += moved;
      if (water[a] < 0 || water[b] > 8 || water[a] + water[b] != before)
        throw new AssertionError("Water conservation/capacity");
    }
    if (java.util.Arrays.stream(water).sum() != expected) throw new AssertionError("Total volume");
    for (int units = 1; units <= 8; units++)
      if (8 - FluidVolume.nativeLevel(units) != units) throw new AssertionError("Native levels");
    if (FluidVolume.sideways(1, 0) != 0 || FluidVolume.pressure(0, 8, 1, 0) != 0)
      throw new AssertionError("Puddle/head limit");
    FluidVolumeAdmissionChecks.main(new String[0]);
    var wakes = new WakeField();
    for (int i = 0; i < 100000; i++) wakes.add(i, 64, 0, 1, .2, 1);
    int live = 0;
    for (int i = 0; i < WakeField.CAPACITY; i++) if (wakes.alive(i)) live++;
    if (live != 128) throw new AssertionError("Wake capacity");
    for (int i = 0; i < WakeField.LIFETIME; i++) wakes.tick();
    for (int i = 0; i < WakeField.CAPACITY; i++)
      if (wakes.alive(i)) throw new AssertionError("Wake expiration");
    WakeFieldFiniteAdmissionChecks.main(new String[0]);
    WakeFieldSubnormalDirectionChecks.main(new String[0]);
    var stamina = new SwimmingState();
    for (int i = 0; i < 1799; i++)
      if (stamina.tick(true, 1800)) throw new AssertionError("Early exhaustion");
    if (!stamina.tick(true, 1800)) throw new AssertionError("No exhaustion");
    for (int i = 0; i < 450; i++) stamina.tick(false, 1800);
    if (stamina.remaining(1800) != 1800) throw new AssertionError("No recovery");
    System.out.println(
        "HYDROLOGY_PASS randomizedTransfers=500000 conserved="
            + expected
            + " nativeLevels=8 pressure=true stamina=true");
  }
}
