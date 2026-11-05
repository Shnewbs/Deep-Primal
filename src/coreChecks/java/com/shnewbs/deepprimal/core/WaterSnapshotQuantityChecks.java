package com.shnewbs.deepprimal.core;

import java.util.Arrays;
import java.util.Random;

/** Independent captured-byte quantity admission regression; no platform dependencies. */
public final class WaterSnapshotQuantityChecks {
  private static long checks;
  private WaterSnapshotQuantityChecks() {}

  private static void check(boolean value, String message) {
    checks++;
    if (!value) throw new AssertionError(message);
  }

  private static int admitted(int rawUnsigned) {
    return rawUnsigned <= 16 ? rawUnsigned : 0;
  }

  public static void main(String[] args) {
    byte[] units = new byte[WaterPatchMesher.CELLS];
    byte[] solids = new byte[units.length];
    byte[] flows = new byte[units.length];
    int[] lights = new int[units.length];
    int[] colors = new int[256];
    var s = new WaterPatchMesher.Snapshot(units, solids, lights, colors, flows);
    int[][] locations = {{0,0,0},{7,4,8},{15,15,15},{-1,-1,-1},{16,16,16}};
    for (int[] p : locations) {
      int idx = WaterPatchMesher.index(p[0],p[1],p[2]);
      for (int raw = 0; raw < 256; raw++) {
        units[idx] = (byte) raw;
        check(s.water(p[0],p[1],p[2]) == admitted(raw),
            "invalid snapshot quantity: raw=" + raw + " at=" + Arrays.toString(p));
        check((units[idx] & 255) == raw, "source quantity mutated");
      }
      units[idx] = 0;
    }
    var old = new WaterPatchMesher.Snapshot(units, solids, lights, colors);
    check(old.direction(0,0,0) == 16, "legacy null flow changed");
    Random random = new Random(0xD148ABCDL);
    for (int i = 0; i < 200_000; i++) {
      int x = random.nextInt(18) - 1;
      int y = random.nextInt(18) - 1;
      int z = random.nextInt(18) - 1;
      int raw = random.nextInt(256);
      int idx = WaterPatchMesher.index(x,y,z);
      units[idx] = (byte) raw;
      check(s.water(x,y,z) == admitted(raw), "seeded malformed quantity: " + raw);
      check((units[idx] & 255) == raw, "seeded source mutated");
      units[idx] = 0;
    }
    for (int amount = 0; amount <= 16; amount++) {
      units[WaterPatchMesher.index(7,4,8)] = (byte) amount;
      check(s.water(7,4,8) == amount, "valid water quantity changed");
    }
    System.out.println("WATER_SNAPSHOT_QUANTITY_PASS checks=" + checks);
  }
}
