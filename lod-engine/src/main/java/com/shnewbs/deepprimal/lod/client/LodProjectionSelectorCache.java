package com.shnewbs.deepprimal.lod.client;

/**
 * Render-thread-only LOD selector reuse. Most extracts use the same scene size,
 * FOV and error threshold, so avoid repeated trigonometry and a new selector
 * per frame. A change in the actual world-target height (including FSR on/off,
 * resize and native fallback) rebuilds the thresholds before selecting pages.
 * Raw-bit keys keep exceptional floating-point inputs on the original path.
 */
final class LodProjectionSelectorCache {
  private LodScreenErrorSelector current;
  private int height;
  private long fovBits, errorBits;

  LodScreenErrorSelector get(int effectiveHeight, double fovDegrees, double error) {
    long fov = Double.doubleToRawLongBits(fovDegrees);
    long threshold = Double.doubleToRawLongBits(error);
    if (current == null || height != effectiveHeight || fovBits != fov
        || errorBits != threshold) {
      current = new LodScreenErrorSelector(
          LodErrorProjection.focalPixels(effectiveHeight, fovDegrees), error);
      height = effectiveHeight;
      fovBits = fov;
      errorBits = threshold;
    }
    return current;
  }

  void reset() {
    current = null;
    height = 0;
    fovBits = errorBits = 0;
  }
}
