package com.shnewbs.deepprimal.settings;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.shnewbs.deepprimal.core.UpscalingPolicy;
import com.shnewbs.deepprimal.graphics.upscaling.Fsr1Upscaler;
import java.nio.ByteOrder;
import java.util.OptionalInt;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

/**
 * Scene-only upscaling. The window's native main target is never resized or replaced.
 * A scoped getMainRenderTarget override covers world, hand, outlines and post effects;
 * it is cleared before GUI rendering and in the outer render method's finally block.
 */
public final class UpscalingRuntime {
  private static final RenderPipeline LINEAR = RenderPipeline.builder()
      .withLocation(id("upscaling/linear"))
      .withVertexShader(id("core/fsr_fullscreen"))
      .withFragmentShader(id("core/upscale_linear"))
      .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
      .withSampler("Source").withUniform("UpscaleSize", UniformType.UNIFORM_BUFFER)
      .withCull(false).build();

  private static RenderTarget world, display, frameTarget;
  private static Fsr1Upscaler fsr;
  private static GpuBuffer linearSize;
  private static GameRenderState state;
  private static GlobalSettingsUniform globals;
  private static DeltaTracker delta;
  private static int savedWidth, savedHeight, outlineWidth, outlineHeight;
  private static boolean linearReady;
  private static volatile boolean reconfigure;
  private static volatile UpscalingPolicy.Mode active = UpscalingPolicy.Mode.NONE;
  private static volatile double scale = 1;
  private static volatile String failure = "";
  private static UpscalingPolicy.Mode frameMode = UpscalingPolicy.Mode.NONE;
  private static double frameScale = 1;
  private static long completedFrames, fsrFrames;
  private static int sourceWidth, sourceHeight, outputWidth, outputHeight;

  /** Functional counters only; these are not GPU timing or performance measurements. */
  public record Diagnostics(long completedFrames, long fsrFrames, int sourceWidth, int sourceHeight,
      int outputWidth, int outputHeight, boolean worldAllocated, boolean fsrAllocated,
      boolean worldTargetBound, UpscalingPolicy.Mode activeMode) {}

  public static Diagnostics diagnostics() {
    return new Diagnostics(completedFrames, fsrFrames, sourceWidth, sourceHeight,
        outputWidth, outputHeight, world != null, fsr != null, frameTarget != null, active);
  }

  private static Identifier id(String path) {
    return Identifier.fromNamespaceAndPath("deepprimal_graphics", path);
  }

  public static UpscalingPolicy.Mode activeMode() { return active; }
  public static double activeScale() { return scale; }
  public static RenderTarget frameTarget() { return frameTarget; }

  public static String unavailableReason(UpscalingPolicy.Mode requested) {
    if (requested == UpscalingPolicy.Mode.NONE) return "";
    if (!RendererOwnership.owned()) return "Rendering is managed by " + RendererOwnership.owner();
    if (requested == UpscalingPolicy.Mode.FSR_31 || requested == UpscalingPolicy.Mode.FSR_41)
      return UpscalingPolicy.unavailableReason(requested, false);
    if (requested == UpscalingPolicy.Mode.DLSS)
      return "DLSS is unavailable: this renderer still needs jitter and motion vectors for moving "
          + "objects, plus the NVIDIA NGX runtime. Native resolution remains active.";
    return failure;
  }

  public static void requestReconfigure() {
    reconfigure = true;
    active = UpscalingPolicy.Mode.NONE;
    scale = 1;
    failure = "";
  }

  /** Called by the enclosing render guard before any world work. */
  public static void beginFrame(Minecraft mc) {
    RenderSystem.assertOnRenderThread();
    restoreTarget(mc);
    if (reconfigure) {
      releaseResources();
      reconfigure = false;
    }
    if (mc.level == null) {
      releaseResources();
      nativeStatus();
    }
  }

  public static void beginWorld(Minecraft mc, GameRenderState renderState,
      GlobalSettingsUniform uniforms, DeltaTracker tracker) {
    RenderSystem.assertOnRenderThread();
    var requested = UpscalingConfig.requestedMode();
    var nativeTarget = mc.getMainRenderTarget();
    if (requested == UpscalingPolicy.Mode.NONE || !unavailableReason(requested).isEmpty()
        || renderState.windowRenderState.isMinimized) {
      nativeStatus();
      resizeOutlines(mc, nativeTarget.width, nativeTarget.height);
      return;
    }
    double requestedScale = UpscalingConfig.requestedScale();
    try {
      // Compile before redirecting the main target: failure preserves a native world frame.
      if (!linearReady) {
        if (!RenderSystem.getDevice().precompilePipeline(LINEAR, null).isValid())
          throw new IllegalStateException("The spatial presentation shader did not compile");
        linearReady = true;
      }
      if (requested == UpscalingPolicy.Mode.FSR) {
        if (fsr == null) fsr = new Fsr1Upscaler();
        if (!fsr.isReady()) throw new IllegalStateException(fsr.failureReason());
      }
      int width = UpscalingPolicy.dimension(nativeTarget.width, requestedScale);
      int height = UpscalingPolicy.dimension(nativeTarget.height, requestedScale);
      ensureWorldTarget(width, height);
      resizeOutlines(mc, width, height);
      RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
          world.getColorTexture(), renderState.guiRenderState.clearColorOverride,
          world.getDepthTexture(), 1.0);
      // Save all state before changing it so exceptional world renders always restore it.
      display = nativeTarget;
      state = renderState;
      globals = uniforms;
      delta = tracker;
      savedWidth = state.windowRenderState.width;
      savedHeight = state.windowRenderState.height;
      state.windowRenderState.width = width;
      state.windowRenderState.height = height;
      updateGlobals(mc, width, height);
      frameMode = requested;
      frameScale = requestedScale;
      frameTarget = world;
    } catch (RuntimeException | LinkageError error) {
      restoreTarget(mc);
      fail(error);
      releaseResources();
      resizeOutlines(mc, nativeTarget.width, nativeTarget.height);
    }
  }

  /** After world post effects, before the native depth clear and GUI draw. */
  public static void finishWorld(Minecraft mc) {
    if (frameTarget == null) return;
    var destination = display;
    var mode = frameMode;
    double completedScale = frameScale;
    restoreTarget(mc);
    try {
      if (mode == UpscalingPolicy.Mode.FSR)
        fsr.upscale(world.getColorTextureView(), destination.getColorTextureView(),
            UpscalingConfig.sharpness());
      else linearBlit(world, destination);
      active = mode;
      scale = completedScale;
      completedFrames++;
      if (mode == UpscalingPolicy.Mode.FSR) fsrFrames++;
      sourceWidth = world.width;
      sourceHeight = world.height;
      outputWidth = destination.width;
      outputHeight = destination.height;
    } catch (RuntimeException | LinkageError error) {
      fail(error);
      // Preserve this already-rendered scene; the following frame renders at native size.
      linearBlit(world, destination);
    }
  }

  /** Must also execute when renderLevel, post-processing, or a third-party callback throws. */
  public static void restoreTarget(Minecraft mc) {
    frameTarget = null;
    if (state != null) {
      state.windowRenderState.width = savedWidth;
      state.windowRenderState.height = savedHeight;
      try {
        updateGlobals(mc, savedWidth, savedHeight);
      } finally {
        state = null;
        globals = null;
        delta = null;
        display = null;
      }
    }
  }

  public static void resized() {
    outlineWidth = outlineHeight = 0;
    nativeStatus();
  }

  /** ShaderManager calls this only after its common GPU pipeline cache has been reset. */
  public static void shadersReloaded() {
    requestReconfigure();
  }

  private static void updateGlobals(Minecraft mc, int width, int height) {
    var options = state.optionsRenderState;
    globals.update(width, height, options.glintStrength,
        mc.level == null ? 0 : mc.level.getGameTime(), delta, options.menuBackgroundBlurriness,
        state.levelRenderState.cameraRenderState.pos,
        options.textureFiltering == TextureFilteringMethod.RGSS);
  }

  private static void resizeOutlines(Minecraft mc, int width, int height) {
    if (outlineWidth == width && outlineHeight == height) return;
    mc.levelRenderer.resize(width, height);
    outlineWidth = width;
    outlineHeight = height;
  }

  private static void ensureWorldTarget(int width, int height) {
    if (world != null && world.width == width && world.height == height) return;
    RenderTarget replacement = new RenderTarget("Deep Primal upscaling scene", true) {};
    try {
      replacement.createBuffers(width, height);
    } catch (RuntimeException | Error error) {
      replacement.destroyBuffers();
      throw error;
    }
    if (world != null) world.destroyBuffers();
    world = replacement;
  }

  private static void linearBlit(RenderTarget source, RenderTarget destination) {
    try (var stack = MemoryStack.stackPush()) {
      var bytes = stack.calloc(16).order(ByteOrder.nativeOrder());
      bytes.putFloat(0, destination.width);
      bytes.putFloat(4, destination.height);
      if (linearSize == null)
        linearSize = RenderSystem.getDevice().createBuffer(() -> "Spatial output size",
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, bytes);
      else RenderSystem.getDevice().createCommandEncoder().writeToBuffer(linearSize.slice(), bytes);
    }
    try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
        () -> "Deep Primal spatial upscaling", destination.getColorTextureView(), OptionalInt.empty())) {
      pass.setPipeline(LINEAR);
      pass.setUniform("UpscaleSize", linearSize);
      pass.bindTexture("Source", source.getColorTextureView(),
          RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
      pass.draw(0, 3);
    }
  }

  private static void nativeStatus() { active = UpscalingPolicy.Mode.NONE; scale = 1; }

  private static void fail(Throwable error) {
    failure = "Upscaling failed; native resolution restored: "
        + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
    nativeStatus();
    com.mojang.logging.LogUtils.getLogger().warn(failure, error);
  }

  private static void releaseResources() {
    if (world != null) { world.destroyBuffers(); world = null; }
    if (fsr != null) { fsr.close(); fsr = null; }
    if (linearSize != null) { linearSize.close(); linearSize = null; }
    linearReady = false;
  }

  public static void close() {
    RenderSystem.assertOnRenderThread();
    restoreTarget(Minecraft.getInstance());
    releaseResources();
    nativeStatus();
  }

  private UpscalingRuntime() {}
}
