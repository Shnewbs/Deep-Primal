package com.shnewbs.deepprimal.lod.client;

import java.util.Random;

/** Pure-Java cache/selector parity. No Minecraft GPU or native frame validation. */
public final class LodProjectionCacheChecks {
  private static long assertions;

  private static void check(boolean ok, String description) {
    assertions++;
    if (!ok) throw new AssertionError("assertion " + assertions + ": " + description);
  }

  private static void cacheInvalidation() {
    LodProjectionSelectorCache cache = new LodProjectionSelectorCache();
    LodScreenErrorSelector native1080 = cache.get(1080, 70.0, 5.0);
    for (int i = 0; i < 10000; i++)
      check(cache.get(1080, 70.0, 5.0) == native1080, "stable native frame reused");
    LodScreenErrorSelector fsr720 = cache.get(720, 70.0, 5.0);
    check(fsr720 != native1080, "spatial frame height invalidates selector");
    check(cache.get(720, 70.0, 5.0) == fsr720, "stable FSR frame reused");
    LodScreenErrorSelector resized = cache.get(900, 70.0, 5.0);
    check(resized != fsr720, "resize invalidates selector");
    LodScreenErrorSelector changedFov = cache.get(900, 90.0, 5.0);
    check(changedFov != resized, "FOV invalidates selector");
    LodScreenErrorSelector changedError = cache.get(900, 90.0, 8.0);
    check(changedError != changedFov, "error threshold invalidates selector");
    LodScreenErrorSelector returnedNative = cache.get(1080, 70.0, 5.0);
    check(returnedNative != changedError, "returning to native invalidates selector");
    check(returnedNative != native1080, "different intervening state was rebuilt");
    LodScreenErrorSelector zero = cache.get(720, +0.0, 5.0);
    check(cache.get(720, -0.0, 5.0) != zero, "signed zero FOV uses raw bit keys");
    LodScreenErrorSelector minusZeroError = cache.get(720, 70, -0.0);
    check(cache.get(720, 70, +0.0) != minusZeroError, "signed zero error uses raw bit keys");
    LodScreenErrorSelector beforeReset = cache.get(1080, 70, 5);
    cache.reset();
    check(cache.get(1080, 70, 5) != beforeReset, "world unload drops selector");
  }

  private static void parity() {
    Random random = new Random(0x52656e6465723231L);
    LodProjectionSelectorCache cache = new LodProjectionSelectorCache();
    int[] heights = {0, -1, 1, 540, 720, 1080, 1440, 2160, 4320};
    double[] fovs = {0, -0.0, 30, 50, 70, 90, 110, 179, 180, 360, Double.NaN,
        Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
    double[] errors = {0, -0.0, 1, 4, 5, 8, 12, Double.NaN, Double.POSITIVE_INFINITY};
    for (int i = 0; i < 300000; i++) {
      int height = heights[random.nextInt(heights.length)];
      double fov = fovs[random.nextInt(fovs.length)];
      double error = errors[random.nextInt(errors.length)];
      int previous = random.nextInt(5) - 1;
      double dx = (random.nextDouble() - 0.5) * 20000;
      double dz = (random.nextDouble() - 0.5) * 20000;
      LodScreenErrorSelector baseline = new LodScreenErrorSelector(
          LodErrorProjection.focalPixels(height, fov), error);
      LodScreenErrorSelector cached = cache.get(height, fov, error);
      check(cached.select(previous, dx, dz) == baseline.select(previous, dx, dz),
          "screen error parity across native/scaled/exceptional inputs");
      check(cache.get(height, fov, error) == cached, "same key must reuse thresholds");
    }
    for (int height : new int[] {1080, 720, 1080, 540, 900, 1440, 1080}) {
      var selector = cache.get(height, 70, 5);
      for (int i = 0; i < 20000; i++) {
        double distance = 1.0 + i * 0.25;
        var baseline = new LodScreenErrorSelector(
            LodErrorProjection.focalPixels(height, 70), 5);
        check(selector.select(0, distance, distance / 2)
            == baseline.select(0, distance, distance / 2), "FSR/native threshold parity");
      }
    }
  }

  public static void main(String[] args) {
    cacheInvalidation();
    parity();
    System.out.println("LOD_PROJECTION_CACHE_PASS assertions=" + assertions);
  }

  private LodProjectionCacheChecks() {}
}
