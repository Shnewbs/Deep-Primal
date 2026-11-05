package net.vulkanmod.deepprimal.upscaling.dlss;

import java.nio.file.Path;

/** Standalone provider/input checks. No Minecraft classes, mock NVIDIA GPU, or native frame claim. */
public final class DlssProviderChecks {
  private static int passed;

  public static void main(String[] args) {
    if (args.length > 0) System.setProperty("deepprimal.dlss.library", args[0]);
    var scene = new DlssFrame.Image(1, 2, 37, 1280, 720);
    var depth = new DlssFrame.Image(3, 4, 126, 1280, 720);
    var motion = new DlssFrame.Image(5, 6, 83, 1280, 720);
    var output = new DlssFrame.Image(7, 8, 37, 1920, 1080);
    var valid = new DlssFrame(9, scene, depth, motion, output, .25f, -.25f, 1, 1, 16.6f, 1, false);
    check(valid.output().width() == 1920, "Valid separate temporal inputs");
    rejects(() -> new DlssFrame(0, scene, depth, motion, output, 0, 0, 1, 1, 16, 1, false));
    rejects(() -> new DlssFrame.Image(0, 2, 37, 1280, 720));
    rejects(() -> new DlssFrame(9, scene, depth, motion,
        new DlssFrame.Image(1, 99, 37, 1920, 1080), 0, 0, 1, 1, 16, 1, false));
    rejects(() -> new DlssFrame(9, scene, depth,
        new DlssFrame.Image(5, 6, 37, 1280, 720), output, 0, 0, 1, 1, 16, 1, false));
    rejects(() -> new DlssFrame(9, scene, depth,
        new DlssFrame.Image(5, 6, 83, 1281, 720), output, 0, 0, 1, 1, 16, 1, false));
    rejects(() -> new DlssFrame(9, scene,
        new DlssFrame.Image(3, 4, 37, 1280, 720), motion, output, 0, 0, 1, 1, 16, 1, false));
    rejects(() -> new DlssFrame(9, scene, depth, motion, output, Float.NaN, 0, 1, 1, 16, 1, false));
    rejects(() -> new DlssFrame(9, scene, depth, motion, output, 0, 0, 0, 1, 16, 1, false));
    rejects(() -> new DlssFrame(9, scene, depth, motion, output, 0, 0, 1, 1, 0, 1, false));
    rejects(() -> new DlssFrame(9, scene, depth, motion, output, 0, 0, 1, 1, 16, Float.POSITIVE_INFINITY, false));
    rejects(() -> new DlssVulkanProvider.RenderSize(0, 0, 0, 0, 1920, 1080));

    var missingMotion = new DlssVulkanProvider.InputSupport(true, true, true, false);
    check(!missingMotion.complete(), "Camera/depth alone never enables DLSS");
    check(DlssVulkanProvider.unavailableReason(missingMotion).contains("moving objects"),
        "Temporal input gate runs before optional native loading");
    var noJitter = new DlssVulkanProvider.InputSupport(true, false, true, true);
    check(!noJitter.complete() && noJitter.unavailableReason().contains("jitter"), "Projection jitter gate");

    if (args.length == 0) {
      var readyInputs = new DlssVulkanProvider.InputSupport(true, true, true, true);
      check(DlssVulkanProvider.unavailableReason(readyInputs).contains("not installed"),
          "Missing native runtime safely remains unavailable");
    } else {
      check(DlssNative.availability().available(), "Native JNI library loads");
      check(DlssNative.abiVersion() == 1, "Native JNI ABI matches Java");
      rejects(() -> DlssNative.optimalSettings(99999, 1920, 1080, 2));
      rejects(() -> DlssNative.close(99999));
      var noDevice = DlssVulkanProvider.queryRequirements(0, 0, Path.of("."), Path.of("."));
      check(!noDevice.supported(), "No adapter is never reported as DLSS support");
    }
    System.out.println("DLSS_PROVIDER_CHECKS_PASS checks=" + passed
        + " native=" + (args.length > 0) + " temporal_gpu_dispatch_tested=false");
  }

  private static void check(boolean value, String label) {
    if (!value) throw new AssertionError(label);
    passed++;
  }

  private static void rejects(Runnable action) {
    try { action.run(); }
    catch (IllegalArgumentException | IllegalStateException expected) { passed++; return; }
    throw new AssertionError("Invalid DLSS input was accepted");
  }

  private DlssProviderChecks() {}
}
