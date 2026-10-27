package net.vulkanmod.deepprimal.upscaling.dlss;

/** Deterministic subpixel projection offsets for a future temporal upscaling path.
 * Values are in render-pixel units, centered in [-0.5, 0.5). The caller must
 * apply them to projection, preserve unjittered matrices for motion vectors,
 * and reset history on cuts, resize, teleport and world transitions.
 * This utility alone does not enable DLSS or produce motion vectors.
 */
public final class TemporalJitter {
  private TemporalJitter() {}
  public record Offset(float xPixels, float yPixels) {
    public float xNdc(int renderWidth) {
      if (renderWidth <= 0) throw new IllegalArgumentException("Invalid width");
      return 2f * xPixels / renderWidth;
    }
    public float yNdc(int renderHeight) {
      if (renderHeight <= 0) throw new IllegalArgumentException("Invalid height");
      return -2f * yPixels / renderHeight;
    }
  }
  /** Periodic Halton(2,3) sequence, avoiding unbounded frame-counter overflow. */
  public static Offset at(long frameIndex, int period) {
    if (frameIndex < 0 || period < 1 || period > 1024)
      throw new IllegalArgumentException("Invalid frame index or jitter period");
    int index = (int)(frameIndex % period) + 1;
    return new Offset((float)(radicalInverse(index, 2) - 0.5),
        (float)(radicalInverse(index, 3) - 0.5));
  }
  private static double radicalInverse(int index, int base) {
    double result = 0, factor = 1.0 / base;
    while (index > 0) {
      result += (index % base) * factor;
      index /= base;
      factor /= base;
    }
    return result;
  }
}
