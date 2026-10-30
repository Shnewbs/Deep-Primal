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
    if (!sameSize(color, depth) || !sameSize(color, motionVectors))
      throw new IllegalArgumentException("DLSS inputs must share the render resolution");
    if (output.width() < color.width() || output.height() < color.height())
      throw new IllegalArgumentException("DLSS output must not be smaller than scene inputs");
    // Reject both read/write collisions and collisions between simultaneous sampled inputs.
    // This is allocation-free on the per-frame contract hot path; a distinct VkImageView
    // of the same VkImage is not a distinct rendering resource.
    if (aliases(color, depth) || aliases(color, motionVectors) || aliases(color, output)
        || aliases(depth, motionVectors) || aliases(depth, output)
        || aliases(motionVectors, output))
      throw new IllegalArgumentException("DLSS inputs and output require distinct image/view handles");
    // VK_FORMAT_R16G16_SFLOAT / VK_FORMAT_R32G32_SFLOAT.
    if (motionVectors.format() != 83 && motionVectors.format() != 103)
      throw new IllegalArgumentException("DLSS motion vectors require RG16F or RG32F");
    // This bridge supports sampled depth-only views; combined stencil views are not accepted.
    if (depth.format() != 124 && depth.format() != 125 && depth.format() != 126)
      throw new IllegalArgumentException("DLSS depth requires D16, D24, or D32F");
    if (!Float.isFinite(jitterX) || !Float.isFinite(jitterY)
        || !Float.isFinite(motionScaleX) || !Float.isFinite(motionScaleY)
        || motionScaleX == 0 || motionScaleY == 0
        || !Float.isFinite(frameTimeMillis) || frameTimeMillis <= 0
        || !Float.isFinite(preExposure) || preExposure <= 0)
      throw new IllegalArgumentException("Invalid DLSS temporal constants");
  }

  private static boolean aliases(Image a, Image b) {
    return a.image() == b.image() || a.view() == b.view();
  }

  private static boolean sameSize(Image a, Image b) {
    return a.width() == b.width() && a.height() == b.height();
  }
}
