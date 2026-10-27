package com.shnewbs.deepprimal.graphics.upscaling;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.OptionalInt;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

/**
 * AMD FidelityFX Super Resolution 1: the official FP32 EASU and RCAS kernels,
 * dispatched through Minecraft's GPU API on either supported renderer.
 *
 * <p>The caller provides SDR scene color before native-resolution GUI composition.
 * Input/output attachments remain caller-owned. All methods run on the render thread.
 * This class performs spatial upscaling only; it does not implement temporal FSR or
 * frame generation. See shaders/upscaling/vendor for the pinned source and MIT notices.
 */
public final class Fsr1Upscaler implements AutoCloseable {
  private static final RenderPipeline EASU = pipeline("fsr_easu", "FsrEasu");
  private static final RenderPipeline RCAS = pipeline("fsr_rcas", "FsrRcas");

  private GpuBuffer easuConstants;
  private GpuBuffer rcasConstants;
  private GpuTexture intermediate;
  private GpuTextureView intermediateView;
  private int inputWidth, inputHeight, outputWidth, outputHeight;
  private float previousSharpness = Float.NaN;
  private boolean shadersReady;
  private boolean closed;
  private String failureReason;

  private static RenderPipeline pipeline(String name, String uniform) {
    return RenderPipeline.builder()
        .withLocation(id("upscaling/" + name))
        .withVertexShader(id("core/fsr_fullscreen"))
        .withFragmentShader(id("core/" + name))
        .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
        .withSampler("Source")
        .withUniform(uniform, UniformType.UNIFORM_BUFFER)
        .withCull(false)
        .build();
  }

  private static Identifier id(String path) {
    return Identifier.fromNamespaceAndPath("deepprimal_graphics", path);
  }

  /**
   * Compile both real shaders before advertising support. Failures are latched until
   * resource reload calls {@link #invalidateShaders()}, avoiding a retry every frame.
   */
  public boolean isReady() {
    RenderSystem.assertOnRenderThread();
    if (closed || failureReason != null) return false;
    if (shadersReady) return true;
    try {
      var device = RenderSystem.getDevice();
      if (!device.precompilePipeline(EASU, null).isValid()
          || !device.precompilePipeline(RCAS, null).isValid()) {
        failureReason = "The renderer could not compile the FSR 1 shaders";
        return false;
      }
      shadersReady = true;
      return true;
    } catch (RuntimeException | LinkageError error) {
      failureReason = describe(error);
      return false;
    }
  }

  public String failureReason() {
    return closed ? "FSR 1 resources have been closed" : failureReason;
  }

  /** Call after the renderer's shader cache has been invalidated by resource reload. */
  public void invalidateShaders() {
    RenderSystem.assertOnRenderThread();
    if (closed) return;
    shadersReady = false;
    failureReason = null;
  }

  /**
   * Upscale an SDR color image into the display-size attachment. Sharpness is a
   * continuous 0..1 RCAS weight; zero skips sharpening and its intermediate texture.
   * The two attachments must not alias. The method leaves no render pass open.
   *
   * @throws IllegalStateException if real shader support or GPU resources are unavailable
   */
  public void upscale(GpuTextureView source, GpuTextureView target, float sharpness) {
    RenderSystem.assertOnRenderThread();
    if (source == null || target == null || source.isClosed() || target.isClosed())
      throw new IllegalArgumentException("FSR 1 requires two open texture views");
    if (source.texture() == target.texture())
      throw new IllegalArgumentException("FSR 1 input and output must not alias");
    if (source.texture().getFormat() != TextureFormat.RGBA8
        || target.texture().getFormat() != TextureFormat.RGBA8)
      throw new IllegalArgumentException("FSR 1 requires SDR RGBA8 scene color");
    if (!Float.isFinite(sharpness))
      throw new IllegalArgumentException("FSR 1 sharpness must be finite");
    int inWidth = source.getWidth(0), inHeight = source.getHeight(0);
    int outWidth = target.getWidth(0), outHeight = target.getHeight(0);
    if (inWidth < 1 || inHeight < 1 || outWidth < inWidth || outHeight < inHeight)
      throw new IllegalArgumentException("FSR 1 requires positive, non-downscaling extents");
    if (!isReady()) throw new IllegalStateException(failureReason());
    float weight = Math.clamp(sharpness, 0.0f, 1.0f);
    try {
      prepareConstants(inWidth, inHeight, outWidth, outHeight, weight);
      if (weight == 0.0f) {
        draw(EASU, "FSR 1 EASU", "FsrEasu", easuConstants, source, target);
      } else {
        ensureIntermediate(outWidth, outHeight);
        draw(EASU, "FSR 1 EASU", "FsrEasu", easuConstants, source, intermediateView);
        draw(RCAS, "FSR 1 RCAS", "FsrRcas", rcasConstants, intermediateView, target);
      }
    } catch (RuntimeException | LinkageError error) {
      failureReason = describe(error);
      releaseResources();
      throw error;
    }
  }

  private void prepareConstants(int inWidth, int inHeight, int outWidth, int outHeight,
      float sharpness) {
    var device = RenderSystem.getDevice();
    if (easuConstants == null || inWidth != inputWidth || inHeight != inputHeight
        || outWidth != outputWidth || outHeight != outputHeight) {
      try (var stack = MemoryStack.stackPush()) {
        ByteBuffer bytes = stack.malloc(64).order(ByteOrder.nativeOrder());
        // These are the four bit-pattern vectors generated by AMD FsrEasuCon.
        // GLSL receives float vec4s for both backends, then floatBitsToUint restores
        // the API's uvec4 constants without numerical conversion.
        float scaleX = inWidth * (1.0f / outWidth), scaleY = inHeight * (1.0f / outHeight);
        float invX = 1.0f / inWidth, invY = 1.0f / inHeight;
        bytes.putFloat(scaleX).putFloat(scaleY)
            .putFloat(0.5f * scaleX - 0.5f).putFloat(0.5f * scaleY - 0.5f);
        bytes.putFloat(invX).putFloat(invY).putFloat(invX).putFloat(-invY);
        bytes.putFloat(-invX).putFloat(2.0f * invY)
            .putFloat(invX).putFloat(2.0f * invY);
        bytes.putFloat(0.0f).putFloat(4.0f * invY).putFloat(0.0f).putFloat(0.0f);
        bytes.flip();
        if (easuConstants == null)
          easuConstants = device.createBuffer(() -> "FSR 1 EASU constants",
              GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, bytes);
        else device.createCommandEncoder().writeToBuffer(easuConstants.slice(), bytes);
      }
      inputWidth = inWidth;
      inputHeight = inHeight;
      outputWidth = outWidth;
      outputHeight = outHeight;
    }
    if (sharpness > 0.0f && (rcasConstants == null || sharpness != previousSharpness)) {
      try (var stack = MemoryStack.stackPush()) {
        ByteBuffer bytes = stack.calloc(16).order(ByteOrder.nativeOrder());
        bytes.putFloat(0, sharpness);
        if (rcasConstants == null)
          rcasConstants = device.createBuffer(() -> "FSR 1 RCAS constants",
              GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, bytes);
        else device.createCommandEncoder().writeToBuffer(rcasConstants.slice(), bytes);
      }
      previousSharpness = sharpness;
    }
  }

  private void ensureIntermediate(int width, int height) {
    if (intermediateView != null && intermediateView.getWidth(0) == width
        && intermediateView.getHeight(0) == height) return;
    // Allocate the replacement before retiring the previous attachment. If either
    // allocation fails, release the partially created texture as well.
    var device = RenderSystem.getDevice();
    GpuTexture replacement = device.createTexture("FSR 1 EASU output",
        GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING,
        TextureFormat.RGBA8, width, height, 1, 1);
    GpuTextureView replacementView;
    try {
      replacementView = device.createTextureView(replacement);
    } catch (RuntimeException | Error error) {
      replacement.close();
      throw error;
    }
    if (intermediateView != null) intermediateView.close();
    if (intermediate != null) intermediate.close();
    intermediate = replacement;
    intermediateView = replacementView;
  }

  private static void draw(RenderPipeline pipeline, String label, String uniform,
      GpuBuffer constants, GpuTextureView source, GpuTextureView target) {
    var sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
    try (var pass = RenderSystem.getDevice().createCommandEncoder()
        .createRenderPass(() -> label, target, OptionalInt.empty())) {
      pass.setPipeline(pipeline);
      pass.setUniform(uniform, constants);
      pass.bindTexture("Source", source, sampler);
      pass.draw(0, 3);
    }
  }

  private static String describe(Throwable error) {
    String message = error.getMessage();
    return "FSR 1 is unavailable: " + (message == null ? error.getClass().getSimpleName() : message);
  }

  private void releaseResources() {
    if (intermediateView != null) { intermediateView.close(); intermediateView = null; }
    if (intermediate != null) { intermediate.close(); intermediate = null; }
    if (easuConstants != null) { easuConstants.close(); easuConstants = null; }
    if (rcasConstants != null) { rcasConstants.close(); rcasConstants = null; }
    inputWidth = inputHeight = outputWidth = outputHeight = 0;
    previousSharpness = Float.NaN;
  }

  @Override
  public void close() {
    RenderSystem.assertOnRenderThread();
    if (closed) return;
    closed = true;
    shadersReady = false;
    releaseResources();
  }
}
