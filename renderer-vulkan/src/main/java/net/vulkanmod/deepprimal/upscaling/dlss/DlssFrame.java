package net.vulkanmod.deepprimal.upscaling.dlss;

import java.util.Objects;

/**
 * Explicit DLSS Super Resolution input contract. Images belong to the renderer, not NGX.
 * Motion vectors must include moving geometry, be unjittered current-to-previous motion, and
 * become render-pixel units after applying motionScaleX/Y. Camera-only reconstruction does
 * not satisfy this contract for a scene containing animated objects.
 * All four image handles and all four view handles must be pairwise distinct. Separate
 * handles may still alias device memory; the calling renderer must prevent that and
 * provide correct Vulkan layouts, barriers and GPU lifetime synchronization.
 */
public record DlssFrame(
    long commandBuffer,
    Image color,
    Image depth,
    Image motionVectors,
    Image output,
    float jitterX,
    float jitterY,
    float motionScaleX,
    float motionScaleY,
    float frameTimeMillis,
    float preExposure,
    boolean resetHistory) {

  public record Image(long image, long view, int format, int width, int height) {
    public Image {
      if (image == 0 || view == 0) throw new IllegalArgumentException("Missing Vulkan image/view");
      if (width < 1 || height < 1 || format < 1)
        throw new IllegalArgumentException("Invalid Vulkan image metadata");
    }
  }

  public DlssFrame {
    if (commandBuffer == 0) throw new IllegalArgumentException("Missing recording command buffer");
    Objects.requireNonNull(color, "Scene color");
    Objects.requireNonNull(depth, "Scene depth");
    Objects.requireNonNull(motionVectors, "Scene motion vectors");
    Objects.requireNonNull(output, "Output");
    DlssTemporalContract.validate(color, depth, motionVectors, output,
        jitterX, jitterY, motionScaleX, motionScaleY, frameTimeMillis, preExposure);
  }
}
