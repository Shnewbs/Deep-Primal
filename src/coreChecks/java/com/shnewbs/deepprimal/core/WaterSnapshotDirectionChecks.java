package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Raw byte snapshots must never expose unsupported 5-bit water-material IDs. */
public final class WaterSnapshotDirectionChecks {
  private static long checks;
  private static void check(boolean ok, String reason) {
    checks++;
    if (!ok) throw new AssertionError(reason + " check=" + checks);
  }

  public static void main(String[] args) {
    byte[] units = new byte[WaterPatchMesher.CELLS];
    byte[] solid = new byte[units.length];
    byte[] flow = new byte[units.length];
    int[] lights = new int[units.length];
    int[] colors = new int[256];
    int at = WaterPatchMesher.index(7, 4, 8);
    units[at] = 16;
    var snapshot = new WaterPatchMesher.Snapshot(units, solid, lights, colors, flow);
    for (int raw = 0; raw < 256; raw++) {
      flow[at] = (byte) raw;
      check(snapshot.direction(7, 4, 8) == (raw <= 16 ? raw : 16),
          "invalid raw direction admitted: " + raw);
      check((flow[at] & 255) == raw, "snapshot normalization mutated captured bytes");
    }
    var legacy = new WaterPatchMesher.Snapshot(units, solid, lights, colors);
    check(legacy.direction(7, 4, 8) == 16, "legacy still-water fallback changed");
    Random random = new Random(0x1475A9L);
    for (int i = 0; i < 10000; i++) {
      int raw = random.nextInt(256);
      flow[at] = (byte) raw;
      int actual = snapshot.direction(7, 4, 8);
      check(actual >= 0 && actual <= 16, "invalid snapshot direction escaped");
      check(actual == (raw < 17 ? raw : 16), "snapshot direction semantic mismatch");
    }
    System.out.println("WATER_SNAPSHOT_DIRECTION_PASS checks=" + checks
        + " exhaustiveBytes=256 randomized=10000");
  }
}
