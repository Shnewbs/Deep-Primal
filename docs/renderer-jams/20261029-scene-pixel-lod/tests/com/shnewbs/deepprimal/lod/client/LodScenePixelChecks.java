package com.shnewbs.deepprimal.lod.client;

import java.util.Random;

/** Pure-Java pixel-domain LOD parity/coverage fixture; does not render a GPU frame. */
public final class LodScenePixelChecks {
  private static long checks;

  private static void check(boolean value, String description) {
    checks++;
    if (!value) throw new AssertionError("check " + checks + ": " + description);
  }

  private static void parity() {
    check(LodErrorProjection.effectiveHeight(1080, 0) == 1080, "native without world target");
    check(LodErrorProjection.effectiveHeight(1080, -1) == 1080, "native with invalid world target");
    check(LodErrorProjection.effectiveHeight(1080, 720) == 720, "FSR1 quality target");
    check(LodErrorProjection.effectiveHeight(1080, 540) == 540, "half-height spatial target");
    check(LodErrorProjection.effectiveHeight(1440, 1440) == 1440, "unscaled scene target");
    check(LodErrorProjection.effectiveHeight(1080, 2160) == 2160, "explicit supersampled scene");
    // Native fallback performs exactly the previous focal arithmetic, not a rescaled approximation.
    for (int height : new int[] {1, 720, 1080, 1440, 2160, 4320}) {
      for (int fov = 1; fov <= 179; fov++) {
        double legacy = height / (2 * Math.tan(Math.toRadians(fov) / 2));
        double actual = LodErrorProjection.focalPixels(
            LodErrorProjection.effectiveHeight(height, 0), fov);
        check(Double.doubleToRawLongBits(legacy) == Double.doubleToRawLongBits(actual),
            "native camera projection bit parity " + height + " " + fov);
      }
    }
    for (int fov = 30; fov <= 120; fov++) {
      double nativeFocal = LodErrorProjection.focalPixels(1080, fov);
      double halfFocal = LodErrorProjection.focalPixels(540, fov);
      check(halfFocal == nativeFocal / 2.0, "pixel focal should halve exactly");
    }
  }

  private static void selector() {
    Random random = new Random(0x5363656e65506cL);
    long[] nativeCounts = new long[3];
    long[] halfCounts = new long[3];
    long differing = 0;
    for (int i = 0; i < 200_000; i++) {
      int nativeHeight = new int[] {720, 900, 1080, 1440, 2160}[i % 5];
      int worldHeight = (i % 4 == 0) ? nativeHeight
          : (i % 4 == 1) ? nativeHeight * 2 / 3
          : (i % 4 == 2) ? nativeHeight / 2 : 0;
      int fov = 35 + random.nextInt(90);
      double error = 2.0 + random.nextDouble() * 10.0;
      double dx = (random.nextDouble() - 0.5) * 1600;
      double dz = (random.nextDouble() - 0.5) * 1600;
      int previous = random.nextInt(3);
      double nativeFocal = LodErrorProjection.focalPixels(nativeHeight, fov);
      int baseline = new LodScreenErrorSelector(nativeFocal, error).select(previous, dx, dz);
      int newNative = new LodScreenErrorSelector(LodErrorProjection.focalPixels(
          LodErrorProjection.effectiveHeight(nativeHeight, 0), fov), error)
          .select(previous, dx, dz);
      check(baseline == newNative, "native LOD selector parity");
      int effective = LodErrorProjection.effectiveHeight(nativeHeight, worldHeight);
      int scaled = new LodScreenErrorSelector(LodErrorProjection.focalPixels(effective, fov), error)
          .select(previous, dx, dz);
      check(scaled >= baseline || effective > nativeHeight,
          "scaled scene must not force extra mesh detail");
      if (effective == nativeHeight) check(scaled == baseline, "unscaled scene parity");
      if (effective < nativeHeight && scaled != baseline) differing++;
      nativeCounts[baseline]++;
      halfCounts[scaled]++;
    }
    check(differing > 2000, "actual internal-resolution LOD changes must be observable");
    System.out.println("SCENE_PIXEL_COUNTS native=" + nativeCounts[0] + "," + nativeCounts[1]
        + "," + nativeCounts[2] + " adapted=" + halfCounts[0] + ","
        + halfCounts[1] + "," + halfCounts[2] + " coarserCases=" + differing);
  }

  private static void boundaries() {
    for (int fov : new int[] {30, 70, 90, 110}) {
      for (int previous = 0; previous <= 2; previous++) {
        double nativeFocal = LodErrorProjection.focalPixels(1080, fov);
        double scaledFocal = LodErrorProjection.focalPixels(720, fov);
        for (int bound : new int[] {0, 1, 2, 16, 32, 128, 256, 512, 2048}) {
          int nativeD = new LodScreenErrorSelector(nativeFocal, 5).select(previous, bound, 0);
          int scaledD = new LodScreenErrorSelector(scaledFocal, 5).select(previous, bound, 0);
          check(scaledD >= nativeD, "boundary selection monotone");
        }
      }
    }
  }

  public static void main(String[] args) {
    parity();
    selector();
    boundaries();
    System.out.println("SCENE_PIXEL_PASS checks=" + checks);
  }

  private LodScenePixelChecks() {}
}
