package net.vulkanmod.deepprimal.upscaling.dlss;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Optional, real NVIDIA NGX Vulkan Super Resolution provider. This class is not a promise
 * that a renderer has temporal inputs: callers must satisfy {@link InputSupport} before
 * opening it. No frame generation, DLAA UI, or ray reconstruction is implied.
 *
 * <p>All instance calls belong to the creating render thread. Evaluate outside a render pass
 * after transitioning all sampled inputs, including depth, to SHADER_READ_ONLY_OPTIMAL
 * and the storage output to GENERAL. The renderer must
 * insert appropriate write-to-read barriers and restore its descriptor/pipeline state after
 * NGX dispatch. Output/HUD composition happens at native resolution afterward.
 */
public final class DlssVulkanProvider implements AutoCloseable {
  public enum Quality {
    ULTRA_QUALITY(4), QUALITY(2), BALANCED(1), PERFORMANCE(0);
    private final int ngxValue;
    Quality(int ngxValue) { this.ngxValue = ngxValue; }
  }

  /** These represent implemented renderer inputs, not user switches or GPU properties. */
  public record InputSupport(boolean sceneWithoutUi, boolean jitteredProjection,
      boolean sampledDepth, boolean fullSceneMotionVectors) {
    public String unavailableReason() {
      if (!sceneWithoutUi) return "DLSS requires scene color before HUD and menu composition";
      if (!jitteredProjection) return "DLSS requires a temporal jittered camera path";
      if (!sampledDepth) return "DLSS requires a sampled scene-depth target";
      if (!fullSceneMotionVectors) return "DLSS requires motion vectors for camera and moving objects";
      return "";
    }
    public boolean complete() { return unavailableReason().isEmpty(); }
  }

  public record Device(long instance, long physicalDevice, long device,
      long getInstanceProcAddr, long getDeviceProcAddr) {
    public Device {
      if (instance == 0 || physicalDevice == 0 || device == 0
          || getInstanceProcAddr == 0 || getDeviceProcAddr == 0)
        throw new IllegalArgumentException("Live Vulkan device and dispatch pointers are required");
    }
  }

  public record Requirements(boolean supported, String reason, int supportFlags,
      int minimumArchitecture) {}

  public record RenderSize(int width, int height, int minWidth, int minHeight,
      int maxWidth, int maxHeight) {
    public RenderSize {
      if (minWidth < 1 || minHeight < 1 || width < minWidth || height < minHeight
          || width > maxWidth || height > maxHeight)
        throw new IllegalArgumentException("NGX returned an unsupported quality/resolution");
    }
  }

  public static String unavailableReason(InputSupport inputs) {
    Objects.requireNonNull(inputs, "Temporal input support");
    if (!inputs.complete()) return inputs.unavailableReason();
    return DlssNative.availability().reason();
  }

  /** Query before instance creation, then merge these with the renderer's own extensions. */
  public static List<String> requiredInstanceExtensions(Path dataPath, Path runtimePath) {
    requireNative();
    return List.of(DlssNative.extensions(0, 0, normalized(dataPath), normalized(runtimePath), true));
  }

  /** Query after physical-device selection and before logical-device creation. */
  public static List<String> requiredDeviceExtensions(long instance, long physicalDevice,
      Path dataPath, Path runtimePath) {
    requireNative();
    if (instance == 0 || physicalDevice == 0) throw new IllegalArgumentException("Missing Vulkan adapter");
    return List.of(DlssNative.extensions(instance, physicalDevice,
        normalized(dataPath), normalized(runtimePath), false));
  }

  /** Uses NGX's per-adapter check, never a vendor string or PCI vendor ID. */
  public static Requirements queryRequirements(long instance, long physicalDevice,
      Path dataPath, Path runtimePath) {
    var available = DlssNative.availability();
    if (!available.available()) return new Requirements(false, available.reason(), -1, 0);
    if (instance == 0 || physicalDevice == 0)
      return new Requirements(false, "DLSS requires an initialized Vulkan adapter", -1, 0);
    try {
      int[] values = DlssNative.requirements(instance, physicalDevice,
          normalized(dataPath), normalized(runtimePath));
      if (values == null || values.length != 2)
        return new Requirements(false, "DLSS provider returned invalid requirements", -1, 0);
      int flags = values[0];
      String reason = flags == 0 ? ""
          : (flags & 2) != 0 ? "DLSS requires a newer NVIDIA driver"
          : (flags & 4) != 0 ? "DLSS is not supported by this graphics adapter"
          : (flags & 8) != 0 ? "DLSS requires a newer operating system"
          : "NVIDIA NGX did not report DLSS support";
      return new Requirements(flags == 0, reason, flags, values[1]);
    } catch (RuntimeException | LinkageError error) {
      return new Requirements(false, "NVIDIA NGX feature query failed: " + error.getMessage(), -1, 0);
    }
  }

  private final Thread owner = Thread.currentThread();
  private final Runnable drainGpu;
  // Session-owned scratch: JNI copies both arrays synchronously before returning.
  private final DlssPackedFrame frameArgs = new DlssPackedFrame();
  private long context;
  private boolean poisoned;
  private boolean resetHistory = true;
  private int renderWidth, renderHeight, outputWidth, outputHeight;

  /**
   * Opens NGX after required extensions were enabled. drainGpu must submit or discard all
   * recording command buffers and wait for their completion before native resource release.
   * Supplying a no-op while work references this feature is an invalid renderer integration.
   */
  public DlssVulkanProvider(Device device, InputSupport inputs, Path dataPath, Path runtimePath,
      Runnable drainGpu) throws IOException {
    Objects.requireNonNull(device, "Vulkan device");
    this.drainGpu = Objects.requireNonNull(drainGpu, "GPU completion boundary");
    String missing = unavailableReason(inputs);
    if (!missing.isEmpty()) throw new IllegalStateException(missing);
    Files.createDirectories(Objects.requireNonNull(dataPath, "NGX data path"));
    if (!Files.isDirectory(Objects.requireNonNull(runtimePath, "NGX runtime path")))
      throw new IllegalArgumentException("NVIDIA runtime directory is missing");
    Requirements support = queryRequirements(device.instance(), device.physicalDevice(), dataPath, runtimePath);
    if (!support.supported()) throw new IllegalStateException(support.reason());
    context = DlssNative.open(device.instance(), device.physicalDevice(), device.device(),
        device.getInstanceProcAddr(), device.getDeviceProcAddr(), normalized(dataPath), normalized(runtimePath));
    if (context == 0) throw new IllegalStateException("NVIDIA NGX initialization returned no context");
  }

  public RenderSize optimalSize(int width, int height, Quality quality) {
    requireUsable();
    if (width < 1 || height < 1) throw new IllegalArgumentException("Invalid output resolution");
    int[] s = DlssNative.optimalSettings(context, width, height,
        Objects.requireNonNull(quality, "DLSS quality").ngxValue);
    if (s == null || s.length != 6) throw new IllegalStateException("Invalid NGX optimal size");
    return new RenderSize(s[0], s[1], s[2], s[3], s[4], s[5]);
  }

  /** Create once per session. Close after GPU completion before changing size/quality. */
  public void create(long commandBuffer, int width, int height, Quality quality,
      boolean hdrColor, boolean invertedDepth) {
    requireUsable();
    if (commandBuffer == 0) throw new IllegalArgumentException("Missing recording command buffer");
    if (renderWidth != 0) throw new IllegalStateException("Recreate the DLSS session after GPU completion");
    RenderSize input = optimalSize(width, height, quality);
    if (input.width() > width || input.height() > height)
      throw new IllegalStateException("NGX returned an invalid supersampling ratio");
    try {
      DlssNative.create(context, commandBuffer, input.width(), input.height(),
          width, height, quality.ngxValue, hdrColor, invertedDepth);
      renderWidth = input.width(); renderHeight = input.height();
      outputWidth = width; outputHeight = height;
      resetHistory = true;
    } catch (RuntimeException | LinkageError failure) {
      poisoned = true;
      throw failure;
    }
  }

  /** Mark cuts, teleports, world changes, FOV discontinuities, and temporal-input changes. */
  public void resetHistory() {
    requireUsable();
    resetHistory = true;
  }

  /** On failure the session is latched off; callers restore native rendering and retire it. */
  public void evaluate(DlssFrame frame) {
    requireUsable();
    Objects.requireNonNull(frame, "DLSS frame");
    if (renderWidth == 0) throw new IllegalStateException("Create the DLSS feature first");
    if (frame.color().width() != renderWidth || frame.color().height() != renderHeight
        || frame.output().width() != outputWidth || frame.output().height() != outputHeight)
      throw new IllegalArgumentException("DLSS frame dimensions differ from the created feature");
    frameArgs.fill(frame);
    try {
      DlssNative.evaluate(context, frame.commandBuffer(), frameArgs.images, frameArgs.formats,
          frame.jitterX(), frame.jitterY(), frame.motionScaleX(), frame.motionScaleY(),
          frame.frameTimeMillis(), frame.preExposure(), resetHistory || frame.resetHistory());
      resetHistory = false;
    } catch (RuntimeException | LinkageError failure) {
      poisoned = true;
      throw failure;
    }
  }

  public boolean failed() { return poisoned; }

  @Override
  public void close() {
    requireOwner();
    if (context == 0) return;
    drainGpu.run();
    // Keep the handle if the synchronization boundary throws; it still owns GPU resources.
    DlssNative.close(context);
    context = 0;
  }

  private void requireOwner() {
    if (Thread.currentThread() != owner) throw new IllegalStateException("DLSS render-thread ownership violated");
  }

  private void requireUsable() {
    requireOwner();
    if (context == 0 || poisoned) throw new IllegalStateException("DLSS session is closed or failed");
  }

  private static void requireNative() {
    var available = DlssNative.availability();
    if (!available.available()) throw new IllegalStateException(available.reason());
  }

  private static String normalized(Path path) {
    return Objects.requireNonNull(path).toAbsolutePath().normalize().toString();
  }
}
