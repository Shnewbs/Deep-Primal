package net.vulkanmod.deepprimal.upscaling.dlss;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/** Synthetic JNI boundary fault injection only; does not simulate or validate GPU rendering. */
public final class DlssFailureChecks {
  private static int passed;

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("Pass the TEST-ONLY JNI fault fixture");
    Path fixture = Path.of(args[0]).toAbsolutePath();
    System.setProperty("deepprimal.dlss.library", fixture.toString());
    Path data = Files.createTempDirectory("deepprimal-dlss-fault-checks-");
    try {
      var inputs = new DlssVulkanProvider.InputSupport(true, true, true, true);
      // Synthetic ABI handles are not passed to the production bridge or a GPU.
      var device = new DlssVulkanProvider.Device(1, 2, 3, 4, 5);
      AtomicInteger drains = new AtomicInteger();
      var provider = new DlssVulkanProvider(device, inputs, data, fixture.getParent(), () -> {
        if (drains.incrementAndGet() == 1) throw new IllegalStateException("Injected drain failure");
      });
      provider.create(6, 1920, 1080, DlssVulkanProvider.Quality.QUALITY, false, false);
      provider.evaluate(frame(16)); // The first frame must reset temporal history.
      provider.evaluate(frame(16)); // The following frame must retain temporal history.
      provider.resetHistory();
      provider.evaluate(frame(16)); // A camera cut must reset history again.
      check(!provider.failed(), "Successful synthetic evaluations remain usable");
      rejects(() -> provider.evaluate(frame(17)));
      check(provider.failed(), "Native evaluation failure is latched");
      rejects(() -> provider.evaluate(frame(17))); // Native fixture would now succeed if called.
      rejects(provider::resetHistory);
      rejects(provider::close); // A failed completion boundary must keep the context alive.
      check(drains.get() == 1, "First GPU-drain failure retained ownership");
      provider.close();
      provider.close();
      check(drains.get() == 2, "Retry closes once, repeated close is idempotent");
      rejects(() -> provider.optimalSize(1920, 1080, DlssVulkanProvider.Quality.QUALITY));

      var createFailure = new DlssVulkanProvider(device, inputs, data, fixture.getParent(), drains::incrementAndGet);
      rejects(() -> createFailure.create(6, 1920, 1080,
          DlssVulkanProvider.Quality.PERFORMANCE, false, false));
      check(createFailure.failed(), "Native creation failure is latched");
      rejects(() -> createFailure.create(6, 1920, 1080,
          DlssVulkanProvider.Quality.QUALITY, false, false));
      createFailure.close();
      check(drains.get() == 3, "Failed feature creation is retired through GPU completion boundary");
      System.out.println("DLSS_FAULT_INJECTION_PASS checks=" + passed
          + " backend=synthetic_jni gpu_execution=false");
    } finally {
      Files.deleteIfExists(data);
    }
  }

  private static DlssFrame frame(float frameTime) {
    return new DlssFrame(6, new DlssFrame.Image(1, 2, 37, 960, 540),
        new DlssFrame.Image(3, 4, 126, 960, 540), new DlssFrame.Image(5, 6, 83, 960, 540),
        new DlssFrame.Image(7, 8, 37, 1920, 1080), .25f, -.25f, 1, 1, frameTime, 1, false);
  }

  private static void check(boolean value, String label) {
    if (!value) throw new AssertionError(label);
    passed++;
  }

  private static void rejects(Runnable action) {
    try { action.run(); }
    catch (IllegalStateException expected) { passed++; return; }
    throw new AssertionError("Expected injected/latching failure was not reported");
  }

  private DlssFailureChecks() {}
}
