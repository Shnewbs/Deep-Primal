package com.shnewbs.deepprimal.core;

import java.util.Arrays;
import java.util.Random;

/** Exact old nine-slot oracle versus edge-only classification, including GPU order. */
public final class WaterBoundaryIndexFastPathChecks {
  private static long assertions;
  private static void check(boolean condition, String why) {
    assertions++;
    if (!condition) throw new AssertionError(why + " at assertion " + assertions);
  }
  private static int[] legacy(int[] quads) {
    int[] offsets = new int[10];
    for (int entry : quads) for (int slot = 0; slot < 9; slot++)
      if (slot != 4 && WaterBoundaryIndex.mask(entry, slot % 3 - 1, slot / 3 - 1) != 0)
        offsets[slot + 1]++;
    offsets[0] = 10;
    for (int slot = 0; slot < 9; slot++)
      offsets[slot + 1] = Math.addExact(offsets[slot + 1], offsets[slot]);
    int[] entries = new int[offsets[9]];
    System.arraycopy(offsets, 0, entries, 0, 10);
    for (int entry : quads) for (int slot = 0; slot < 9; slot++)
      if (slot != 4 && WaterBoundaryIndex.mask(entry, slot % 3 - 1, slot / 3 - 1) != 0)
        entries[offsets[slot]++] = entry;
    return entries;
  }
  private static void compare(int[] input) {
    int[] unchanged = input.clone();
    int[] expected = input.length == 0 ? new int[10] : legacy(input);
    WaterBoundaryIndex actual = WaterBoundaryIndex.of(input);
    check(actual.retainedBytes() == expected.length * Integer.BYTES, "retained byte size");
    check(Arrays.equals(input, unchanged), "input mutation");
    for (int i = 0; i < expected.length; i++)
      check(actual.entry(i) == expected[i], "header or GPU entry mismatch at " + i);
    for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++) {
      if (dx == 0 && dz == 0) continue;
      int slot = (dz + 1) * 3 + dx + 1;
      check(actual.begin(dx, dz) == expected[slot], "begin mismatch");
      check(actual.end(dx, dz) == expected[slot + 1], "end mismatch");
    }
    if (input.length == 0) check(actual == WaterBoundaryIndex.EMPTY, "empty singleton");
  }
  public static void main(String[] args) {
    compare(new int[0]);
    for (int cell = 0; cell < 256; cell++) {
      compare(new int[] {cell});
      compare(new int[] {cell, cell, cell | Integer.MIN_VALUE, cell | Integer.MAX_VALUE});
    }
    Random random = new Random(0x194B0A7DL);
    for (int trial = 0; trial < 2000; trial++) {
      int length = random.nextInt(513);
      int[] input = new int[length];
      for (int i = 0; i < length; i++) {
        int x = random.nextInt(16), z = random.nextInt(16);
        if ((trial & 3) == 0) x = (i & 1) == 0 ? 0 : 15;
        if ((trial & 3) == 1) z = (i & 1) == 0 ? 0 : 15;
        if ((trial & 3) == 2) { x = (i & 1) == 0 ? 0 : 15; z = (i & 2) == 0 ? 0 : 15; }
        input[i] = (random.nextInt() & ~255) | (z << 4) | x;
      }
      compare(input);
    }
    int[] large = new int[100_000];
    for (int i = 0; i < large.length; i++) large[i] = (random.nextInt() & ~255)
        | ((i & 1) == 0 ? 0 : 15) | (((i & 2) == 0 ? 0 : 15) << 4);
    compare(large);
    System.out.println("WATER_BOUNDARY_FAST_PATH_PASS assertions=" + assertions
        + " randomizedTrials=2000 largeEntries=100000 legacyExact=true stableGpuOrder=true");
  }
  private WaterBoundaryIndexFastPathChecks() {}
}
