package com.shnewbs.deepprimal.core;

import java.math.BigInteger;
import java.util.Random;

/** Native 8-unit water admission, conservation and extreme-elevation regression checks. */
public final class FluidVolumeAdmissionChecks {
  private static long assertions;

  private static void check(boolean condition, String message) {
    assertions++;
    if (!condition) throw new AssertionError(message);
  }

  private static int expectedPressure(int fromY, int from, int toY, int to) {
    // Independent unbounded integer oracle, including the full signed int-Y range.
    BigInteger head = BigInteger.valueOf(fromY).subtract(BigInteger.valueOf(toY))
        .multiply(BigInteger.valueOf(8)).add(BigInteger.valueOf(from - to));
    BigInteger possible = head.divide(BigInteger.TWO);
    return possible.max(BigInteger.ZERO).min(BigInteger.valueOf(from))
        .min(BigInteger.valueOf(8 - to)).intValueExact();
  }

  private static void transfer(int from, int to, int moved, int expected, String mode) {
    check(moved == expected, mode + " incorrect transfer");
    check(moved >= 0 && moved <= from && moved <= 8 - to, mode + " invalid admission");
    check(from - moved + to + moved == from + to, mode + " changed total volume");
  }

  private static void rejected(int source, int target) {
    int count = 0;
    try { FluidVolume.down(source, target); } catch (IllegalArgumentException expected) { count++; }
    try { FluidVolume.sideways(source, target); } catch (IllegalArgumentException expected) { count++; }
    try { FluidVolume.pressure(Integer.MIN_VALUE, source, Integer.MAX_VALUE, target); }
    catch (IllegalArgumentException expected) { count++; }
    check(count == 3, "malformed native amounts accepted: " + source + "," + target);
  }

  public static void main(String[] args) {
    int[] heights = {Integer.MIN_VALUE, -1000000, -1, 0, 1, 64, 1000000, Integer.MAX_VALUE};
    for (int source = 0; source <= 8; source++) {
      for (int target = 0; target <= 8; target++) {
        transfer(source, target, FluidVolume.down(source, target),
            Math.min(source, 8 - target), "down");
        transfer(source, target, FluidVolume.sideways(source, target),
            source > target ? (source - target) / 2 : 0, "sideways");
        for (int fromY : heights) for (int toY : heights)
          transfer(source, target, FluidVolume.pressure(fromY, source, toY, target),
              expectedPressure(fromY, source, toY, target), "pressure");
      }
    }
    var random = new Random(20261102L);
    for (int i = 0; i < 120000; i++) {
      int source = random.nextInt(9), target = random.nextInt(9);
      int fromY = random.nextInt(), toY = random.nextInt();
      transfer(source, target, FluidVolume.pressure(fromY, source, toY, target),
          expectedPressure(fromY, source, toY, target), "random pressure");
    }
    for (int[] bad : new int[][] {
        {-1, 0}, {9, 0}, {0, -1}, {0, 9}, {Integer.MIN_VALUE, 8},
        {8, Integer.MAX_VALUE}, {Integer.MAX_VALUE, Integer.MIN_VALUE}})
      rejected(bad[0], bad[1]);
    for (int units = 1; units <= 8; units++)
      check(FluidVolume.nativeLevel(units) == 8 - units, "native level parity");
    System.out.println("FLUID_VOLUME_ADMISSION_PASS assertions=" + assertions
        + " randomizedPressure=120000 invalidPairs=7 validNativeParity=true");
  }

  private FluidVolumeAdmissionChecks() {}
}
