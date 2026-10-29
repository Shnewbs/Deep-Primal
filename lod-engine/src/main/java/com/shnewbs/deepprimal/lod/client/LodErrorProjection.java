package com.shnewbs.deepprimal.lod.client;

/**
 * LOD projected error is measured in pixels of the *world* render target, not
 * the native window. Spatial/FSR 1 reconstructs a smaller world target before
 * the native-resolution GUI; using window pixels would over-refine distant LOD.
 * A missing or invalid scene target retains the exact native calculation.
 */
final class LodErrorProjection {
  private LodErrorProjection() {}

  static int effectiveHeight(int nativeWindowHeight, int worldTargetHeight) {
    return worldTargetHeight > 0 ? worldTargetHeight : nativeWindowHeight;
  }

  static double focalPixels(int effectiveHeight, double fovDegrees) {
    // Preserve the original arithmetic and exceptional-FOV behavior bit for bit.
    return effectiveHeight / (2 * Math.tan(Math.toRadians(fovDegrees) / 2));
  }
}
