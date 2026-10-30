package net.vulkanmod.deepprimal.upscaling.dlss;

/**
 * Reusable JNI argument arrays for one render-thread-owned DLSS session.
 * Native evaluate copies both arrays synchronously using Get*ArrayRegion before
 * returning, so their contents may be replaced only after that call finishes.
 * Never share this mutable packet across Vulkan devices, threads or sessions.
 */
final class DlssPackedFrame {
  final long[] images = new long[8];
  final int[] formats = new int[4];

  void fill(DlssFrame frame) {
    DlssFrame.Image color = frame.color();
    DlssFrame.Image depth = frame.depth();
    DlssFrame.Image motion = frame.motionVectors();
    DlssFrame.Image output = frame.output();
    images[0] = color.image(); images[1] = color.view();
    images[2] = depth.image(); images[3] = depth.view();
    images[4] = motion.image(); images[5] = motion.view();
    images[6] = output.image(); images[7] = output.view();
    formats[0] = color.format();
    formats[1] = depth.format();
    formats[2] = motion.format();
    formats[3] = output.format();
  }
}
