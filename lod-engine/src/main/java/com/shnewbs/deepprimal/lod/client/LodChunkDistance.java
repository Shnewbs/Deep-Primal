package com.shnewbs.deepprimal.lod.client;

/** Signed chunk-space distances for LOD admission and nearest-page ordering.
 * Promote operands to long BEFORE subtracting: disk page names can span the full
 * signed-int range even when a native client usually remains near the world border.
 */
final class LodChunkDistance {
  private LodChunkDistance() {}

  static long chebyshev(int x, int z, int centerX, int centerZ) {
    long dx = (long) x - centerX;
    long dz = (long) z - centerZ;
    return Math.max(Math.abs(dx), Math.abs(dz));
  }

  /** The squared distance is a double to avoid overflow past Long.MAX_VALUE. */
  static double squared(int x, int z, int centerX, int centerZ) {
    long dx = (long) x - centerX;
    long dz = (long) z - centerZ;
    return (double) dx * dx + (double) dz * dz;
  }
}
