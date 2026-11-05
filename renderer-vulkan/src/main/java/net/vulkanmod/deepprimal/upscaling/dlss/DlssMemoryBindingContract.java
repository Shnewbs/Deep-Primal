package net.vulkanmod.deepprimal.upscaling.dlss;

/**
 * Fail-closed preflight for the four renderer-owned Vulkan image allocations
 * passed to NGX. This checks caller-supplied provenance; it cannot establish
 * that the metadata matches vkBindImageMemory or prove GPU layout/lifetime.
 * The native bridge independently enforces the same disjoint-range contract.
 */
final class DlssMemoryBindingContract {
  private DlssMemoryBindingContract() {}

  static void validate(DlssFrame frame) {
    validate(frame.color().memoryBinding(), frame.depth().memoryBinding(),
        frame.motionVectors().memoryBinding(), frame.output().memoryBinding());
  }

  static void validate(DlssFrame.MemoryBinding color, DlssFrame.MemoryBinding depth,
      DlssFrame.MemoryBinding motion, DlssFrame.MemoryBinding output) {
    if (!color.available() || !depth.available() || !motion.available() || !output.available())
      throw new IllegalArgumentException("DLSS requires valid Vulkan allocation provenance for every image");
    if (overlap(color, depth) || overlap(color, motion) || overlap(color, output)
        || overlap(depth, motion) || overlap(depth, output) || overlap(motion, output))
      throw new IllegalArgumentException("DLSS image memory bindings must be disjoint");
  }

  private static boolean overlap(DlssFrame.MemoryBinding a, DlssFrame.MemoryBinding b) {
    // validate() established positive sizes and overflow-safe signed offsets.
    return a.memory() == b.memory()
        && a.offset() < b.offset() + b.size()
        && b.offset() < a.offset() + a.size();
  }
}
