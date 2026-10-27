package net.vulkanmod.deepprimal.upscaling.dlss;

import java.nio.file.Files;
import java.nio.file.Path;

/** JNI is optional and is never loaded from GPU vendor/name detection. */
final class DlssNative {
  record Availability(boolean available, String reason) {}

  private static Availability availability;

  static synchronized Availability availability() {
    if (availability != null) return availability;
    String configured = System.getProperty("deepprimal.dlss.library", "").strip();
    if (configured.isEmpty())
      return availability = new Availability(false, "NVIDIA DLSS native provider is not installed");
    try {
      Path file = Path.of(configured).toAbsolutePath().normalize();
      if (!Files.isRegularFile(file))
        return availability = new Availability(false, "NVIDIA DLSS native provider file is missing");
      System.load(file.toString());
      if (abiVersion() != 1)
        return availability = new Availability(false, "NVIDIA DLSS provider version is incompatible");
      return availability = new Availability(true, "");
    } catch (LinkageError | RuntimeException error) {
      return availability = new Availability(false,
          "NVIDIA DLSS native provider could not load (" + error.getClass().getSimpleName() + ")");
    }
  }

  static native int abiVersion();
  static native String[] extensions(long instance, long physicalDevice,
      String dataPath, String runtimePath, boolean instanceExtensions);
  static native int[] requirements(long instance, long physicalDevice,
      String dataPath, String runtimePath);
  static native long open(long instance, long physicalDevice, long device,
      long getInstanceProcAddr, long getDeviceProcAddr, String dataPath, String runtimePath);
  static native int[] optimalSettings(long context, int outputWidth, int outputHeight, int quality);
  static native void create(long context, long commandBuffer, int renderWidth, int renderHeight,
      int outputWidth, int outputHeight, int quality, boolean hdr, boolean invertedDepth);
  static native void evaluate(long context, long commandBuffer, long[] images, int[] formats,
      float jitterX, float jitterY, float motionScaleX, float motionScaleY,
      float frameTimeMillis, float preExposure, boolean reset);
  static native void close(long context);

  private DlssNative() {}
}
