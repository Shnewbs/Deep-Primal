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

  public record Image(long image, long view, int format, int width, int height,
      MemoryBinding memoryBinding) {
    public Image {
      if (image == 0 || view == 0) throw new IllegalArgumentException("Missing Vulkan image/view");
      if (width < 1 || height < 1 || format < 1)
        throw new IllegalArgumentException("Invalid Vulkan image metadata");
      Objects.requireNonNull(memoryBinding, "Vulkan memory binding");
    }

    /** Legacy metadata constructor retained for callers that do not yet expose provenance. */
    public Image(long image, long view, int format, int width, int height) {
      this(image, view, format, width, height, MemoryBinding.unavailable());
    }
  }

  /**
   * Authoritative Vulkan allocation provenance for one image binding.
   * A zero memory handle means the caller has not supplied provenance; the native path
   * must keep DLSS unavailable rather than infer safety from VkImage identity.
   */
  public record MemoryBinding(long memory, long offset, long size) {
    public static MemoryBinding unavailable() { return new MemoryBinding(0, 0, 0); }

    public boolean available() {
      return memory != 0 && size > 0 && offset <= Long.MAX_VALUE - size;
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
