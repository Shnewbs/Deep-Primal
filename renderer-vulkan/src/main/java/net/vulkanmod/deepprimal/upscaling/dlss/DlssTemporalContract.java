package net.vulkanmod.deepprimal.upscaling.dlss;

/** Allocation-free validation shared by Java and native-bound DLSS frame contracts. */
final class DlssTemporalContract {
  private DlssTemporalContract() {}

  static void validate(DlssFrame.Image color, DlssFrame.Image depth,
      DlssFrame.Image motion, DlssFrame.Image output,
      float jitterX, float jitterY, float motionScaleX, float motionScaleY,
      float frameTimeMillis, float preExposure) {
    if (!sameSize(color, depth) || !sameSize(color, motion))
      throw new IllegalArgumentException("DLSS inputs must share the render resolution");
    if (output.width() < color.width() || output.height() < color.height())
      throw new IllegalArgumentException("DLSS output must not be smaller than scene inputs");
    if (aliases(color, depth) || aliases(color, motion) || aliases(color, output)
        || aliases(depth, motion) || aliases(depth, output) || aliases(motion, output))
      throw new IllegalArgumentException("DLSS inputs and output require distinct image/view handles");
    if (motion.format() != 83 && motion.format() != 103)
      throw new IllegalArgumentException("DLSS motion vectors require RG16F or RG32F");
    if (depth.format() != 124 && depth.format() != 125 && depth.format() != 126)
      throw new IllegalArgumentException("DLSS depth requires D16, D24, or D32F");
    if (!Float.isFinite(jitterX) || !Float.isFinite(jitterY)
        || !Float.isFinite(motionScaleX) || !Float.isFinite(motionScaleY)
        || motionScaleX == 0 || motionScaleY == 0
        || !Float.isFinite(frameTimeMillis) || frameTimeMillis <= 0
        || !Float.isFinite(preExposure) || preExposure <= 0)
      throw new IllegalArgumentException("Invalid DLSS temporal constants");
  }

  private static boolean aliases(DlssFrame.Image a, DlssFrame.Image b) {
    return a.image() == b.image() || a.view() == b.view();
  }

  private static boolean sameSize(DlssFrame.Image a, DlssFrame.Image b) {
    return a.width() == b.width() && a.height() == b.height();
  }
}
