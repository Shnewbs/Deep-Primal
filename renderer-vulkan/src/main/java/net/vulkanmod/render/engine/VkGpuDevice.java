// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBuffer.Usage;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.GpuDebugOptions;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.CommandEncoderBackend;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import net.vulkanmod.gl.VkGlTexture;
import net.vulkanmod.interfaces.shader.ExtendedRenderPipeline;
import net.vulkanmod.render.shader.ShaderLoadUtil;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.SPIRVUtils;
import net.vulkanmod.vulkan.shader.converter.GLSLParser;
import net.vulkanmod.vulkan.shader.converter.Lexer;
import net.vulkanmod.vulkan.shader.descriptor.ImageDescriptor;
import net.vulkanmod.vulkan.shader.descriptor.UBO;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class VkGpuDevice implements GpuDeviceBackend {
  private static final Logger LOGGER = LogUtils.getLogger();
  private final VkCommandEncoder encoder;
  private final VkDebugLabel debugLabels;
  private final int maxSupportedTextureSize;
  private final int uniformOffsetAlignment;
  private final ShaderSource defaultShaderSource;
  private final Set<String> enabledExtensions = new HashSet<>();
  private final Map<VkGpuDevice.ShaderCompilationKey, String> shaderSrcCache = new HashMap<>();

  private static VkGpuDevice current;
  private final Map<RenderPipeline, PipelineBinding> bindings = new IdentityHashMap<>();

  public ExtendedRenderPipeline binding(RenderPipeline pipeline) {
    return bindings.computeIfAbsent(pipeline, unused -> new PipelineBinding());
  }

  private static final class PipelineBinding implements ExtendedRenderPipeline {
    private GraphicsPipeline pipeline;

    public GraphicsPipeline getPipeline() {
      return pipeline;
    }

    public void setPipeline(GraphicsPipeline value) {
      pipeline = value;
    }
  }

  private final Map<RenderPipeline, GraphicsPipeline> compiledPipelines = new IdentityHashMap<>();

  public static VkGpuDevice current() {
    return current;
  }

  public void clearPipelineCache() {
    if (Renderer.getInstance() != null && Renderer.isRecording())
      Renderer.getInstance().flushCmds();
    Vulkan.waitIdle();
    for (var entry : compiledPipelines.entrySet()) {
      ExtendedRenderPipeline.of(entry.getKey()).setPipeline(null);
      entry.getValue().cleanUp();
    }
    compiledPipelines.clear();
    bindings.clear();
    shaderSrcCache.clear();
  }

  public VkGpuDevice(
      long windowHandle, ShaderSource defaultShaderSource, GpuDebugOptions debugOptions) {
    VRenderSystem.setWindow(windowHandle);
    VRenderSystem.initRenderer();
    this.debugLabels =
        VkDebugLabel.create(
            debugOptions.useLabels()
                || Vulkan.ENABLE_VALIDATION_LAYERS
                || Boolean.getBoolean("deepprimal.vulkan.debug"));
    this.maxSupportedTextureSize = VRenderSystem.maxSupportedTextureSize();
    this.uniformOffsetAlignment =
        (int) DeviceManager.deviceProperties.limits().minUniformBufferOffsetAlignment();
    this.defaultShaderSource = defaultShaderSource;
    this.encoder = new VkCommandEncoder(this);
    current = this;
    this.enabledExtensions.addAll(DeviceManager.enabledExtensions);
  }

  public VkDebugLabel debugLabels() {
    return this.debugLabels;
  }

  @Override
  public CommandEncoderBackend createCommandEncoder() {
    return this.encoder;
  }

  @Override
  public GpuSampler createSampler(
      AddressMode addressMode,
      AddressMode addressMode2,
      FilterMode filterMode,
      FilterMode filterMode2,
      int maxAnisotropy,
      OptionalDouble maxLod) {
    return new VkSampler(addressMode, addressMode2, filterMode, filterMode2, maxAnisotropy, maxLod);
  }

  public static boolean supportsTextureFormat(TextureFormat format) {
    int nativeFormat = VkGpuTexture.vkFormat(format);
    return net.vulkanmod.vulkan.device.ImageCapabilities.supports(
        nativeFormat, 7 | (VulkanImage.isDepthFormat(nativeFormat) ? 32 : 16));
  }

  @Override
  public GpuTexture createTexture(
      @Nullable Supplier<String> supplier,
      int usage,
      TextureFormat textureFormat,
      int width,
      int height,
      int layers,
      int mipLevels) {
    return this.createTexture(
        this.debugLabels.exists() && supplier != null ? supplier.get() : null,
        usage,
        textureFormat,
        width,
        height,
        layers,
        mipLevels);
  }

  @Override
  public GpuTexture createTexture(
      @Nullable String string,
      int usage,
      TextureFormat textureFormat,
      int width,
      int height,
      int layers,
      int mipLevels) {
    if (mipLevels < 1) {
      throw new IllegalArgumentException("mipLevels must be at least 1");
    } else {
      if (!supportsTextureFormat(textureFormat))
        throw new UnsupportedOperationException(
            "Texture format unsupported by Vulkan device: " + textureFormat);
      int id = VkGlTexture.genTextureId();
      if (string == null) {
        string = String.valueOf(id);
      }

      int format = VkGpuTexture.vkFormat(textureFormat);
      int viewType = VkGpuTexture.vkImageViewType(usage);
      boolean depthFormat = VulkanImage.isDepthFormat(format);
      int attachmentUsage = depthFormat ? 32 : 16;
      VulkanImage texture =
          VulkanImage.builder(width, height)
              .setName(string)
              .setFormat(format)
              .setArrayLayers(layers)
              .setMipLevels(mipLevels)
              .addUsage(attachmentUsage)
              .setViewType(viewType)
              .createVulkanImage();
      VkGlTexture vGlTexture = VkGlTexture.getTexture(id);
      vGlTexture.setVulkanImage(texture);
      VkGlTexture.bindTexture(id);
      VkGpuTexture glTexture =
          new VkGpuTexture(
              usage, string, textureFormat, width, height, layers, mipLevels, id, vGlTexture);
      this.debugLabels.applyLabel(glTexture);
      return glTexture;
    }
  }

  public VkGpuTexture gpuTextureFromVulkanImage(VulkanImage image) {
    int id = VkGlTexture.genTextureId();
    VkGlTexture glTexture = VkGlTexture.getTexture(id);
    glTexture.setVulkanImage(image);
    TextureFormat textureFormat = VkGpuTexture.textureFormat(image.format);
    VkGpuTexture gpuTexture =
        new VkGpuTexture(
            15,
            image.name,
            textureFormat,
            image.width,
            image.height,
            1,
            image.mipLevels,
            id,
            glTexture);
    this.debugLabels.applyLabel(gpuTexture);
    return gpuTexture;
  }

  @Override
  public GpuTextureView createTextureView(GpuTexture gpuTexture) {
    return this.createTextureView(gpuTexture, 0, gpuTexture.getMipLevels());
  }

  @Override
  public GpuTextureView createTextureView(GpuTexture gpuTexture, int startLevel, int levels) {
    if (gpuTexture.isClosed()) {
      throw new IllegalArgumentException("Can't create texture view with closed texture");
    } else if (startLevel >= 0 && startLevel + levels <= gpuTexture.getMipLevels()) {
      if (!(gpuTexture instanceof VkGpuTexture))
        throw new IllegalArgumentException("Texture belongs to another graphics backend");

      return new VkTextureView((VkGpuTexture) gpuTexture, startLevel, levels);
    } else {
      throw new IllegalArgumentException(
          levels
              + " mip levels starting from "
              + startLevel
              + " would be out of range for texture with only "
              + gpuTexture.getMipLevels()
              + " mip levels");
    }
  }

  @Override
  public GpuBuffer createBuffer(@Nullable Supplier<String> supplier, @Usage int usage, long size) {
    if (size <= 0L) {
      throw new IllegalArgumentException("Buffer size must be greater than zero");
    } else {
      return new VkGpuBuffer(this.debugLabels, supplier, usage, size);
    }
  }

  @Override
  public GpuBuffer createBuffer(
      @Nullable Supplier<String> supplier, int usage, ByteBuffer byteBuffer) {
    if (!byteBuffer.hasRemaining()) {
      throw new IllegalArgumentException("Buffer source must not be empty");
    } else {
      VkGpuBuffer glBuffer =
          new VkGpuBuffer(this.debugLabels, supplier, usage, byteBuffer.remaining());
      this.encoder.writeToBuffer(glBuffer.slice(), byteBuffer);
      return glBuffer;
    }
  }

  @Override
  public String getImplementationInformation() {
    return "Vulkan " + Vulkan.getDevice().vkVersion + ", " + Vulkan.getDevice().vendorIdString;
  }

  @Override
  public List<String> getLastDebugMessages() {
    return Collections.emptyList();
  }

  @Override
  public boolean isDebuggingEnabled() {
    return false;
  }

  @Override
  public String getRenderer() {
    return DeviceManager.device.deviceName;
  }

  @Override
  public String getVendor() {
    return Vulkan.getDevice().vendorIdString;
  }

  @Override
  public String getBackendName() {
    return "Vulkan";
  }

  @Override
  public String getVersion() {
    return Vulkan.getDevice().vkVersion;
  }

  @Override
  public int getMaxTextureSize() {
    return this.maxSupportedTextureSize;
  }

  @Override
  public int getUniformOffsetAlignment() {
    return this.uniformOffsetAlignment;
  }

  @Override
  public List<String> getEnabledExtensions() {
    return new ArrayList<>(this.enabledExtensions);
  }

  @Override
  public int getMaxSupportedAnisotropy() {
    return DeviceManager.device.availableFeatures.features().samplerAnisotropy()
        ? Math.max(1, (int) DeviceManager.deviceProperties.limits().maxSamplerAnisotropy())
        : 1;
  }

  @Override
  public void close() {
    this.clearPipelineCache();
    net.vulkanmod.Initializer.LOGGER.info(
        "VULKAN_OPERATION_COUNTS {}", net.vulkanmod.vulkan.VulkanStatistics.snapshot());
    Vulkan.cleanUp();
    current = null;
  }

  @Override
  public void setVsync(boolean enabled) {
    Renderer.getInstance().getSwapChain().setVsync(enabled);
  }

  @Override
  public void presentFrame() {
    Renderer.getInstance().endFrame();
    net.vulkanmod.deepprimal.Startup.firstFrame();
  }

  @Override
  public boolean isZZeroToOne() {
    return true;
  }

  protected String getCachedShaderSrc(
      Identifier resourceLocation,
      ShaderType shaderType,
      ShaderDefines shaderDefines,
      ShaderSource shaderSourceGetter) {
    VkGpuDevice.ShaderCompilationKey shaderCompilationKey =
        new VkGpuDevice.ShaderCompilationKey(resourceLocation, shaderType, shaderDefines);
    return this.shaderSrcCache.computeIfAbsent(
        shaderCompilationKey,
        compilationKey -> {
          String shaderExtension =
              switch (shaderType) {
                case VERTEX -> ".vsh";
                case FRAGMENT -> ".fsh";
              };
          String shaderName = resourceLocation.getPath() + shaderExtension;
          if (ShaderLoadUtil.REMAPPED_SHADERS.contains(shaderName)) {
            String src = ShaderLoadUtil.getShaderSource(resourceLocation, shaderType);
            if (src == null) {
              throw new RuntimeException("shader: (%s) not found.".formatted(src));
            } else {
              return src;
            }
          } else {
            return shaderSourceGetter.get(compilationKey.id, compilationKey.type);
          }
        });
  }

  @Override
  public CompiledRenderPipeline precompilePipeline(
      RenderPipeline renderPipeline, @Nullable ShaderSource shaderSourceGetter) {
    shaderSourceGetter = shaderSourceGetter == null ? this.defaultShaderSource : shaderSourceGetter;

    try {
      this.compilePipeline(renderPipeline, shaderSourceGetter);
    } catch (Exception var4) {
      throw new RuntimeException(
          "Caught exception compiling pipeline: %s".formatted(renderPipeline.toString()), var4);
    }

    return new VkGpuDevice.VkRenderPipeline(renderPipeline);
  }

  public void compilePipeline(RenderPipeline renderPipeline) {
    this.compilePipeline(renderPipeline, this.defaultShaderSource);
  }

  private void compilePipeline(RenderPipeline renderPipeline, ShaderSource shaderSrcGetter) {
    if (compiledPipelines.containsKey(renderPipeline)) return;
    String locationPath = renderPipeline.getLocation().getPath();
    String configName;
    if (locationPath.contains("core")) {
      configName = locationPath.split("/")[1];
    } else {
      configName = locationPath;
    }

    Pipeline.Builder builder = new Pipeline.Builder(renderPipeline.getVertexFormat(), configName);
    ExtendedRenderPipeline extPipeline = ExtendedRenderPipeline.of(renderPipeline);
    Identifier vertexShaderLocation = renderPipeline.getVertexShader();
    Identifier fragmentShaderLocation = renderPipeline.getFragmentShader();
    ShaderDefines shaderDefines = renderPipeline.getShaderDefines();
    String vshSrc =
        this.getCachedShaderSrc(
            vertexShaderLocation, ShaderType.VERTEX, shaderDefines, shaderSrcGetter);
    String fshSrc =
        this.getCachedShaderSrc(
            fragmentShaderLocation, ShaderType.FRAGMENT, shaderDefines, shaderSrcGetter);
    vshSrc = GlslPreprocessor.injectDefines(vshSrc, shaderDefines);
    fshSrc = GlslPreprocessor.injectDefines(fshSrc, shaderDefines);
    Lexer lexer = new Lexer(vshSrc);
    GLSLParser parser = new GLSLParser();
    parser.setVertexFormat(renderPipeline.getVertexFormat());

    try {
      parser.parse(lexer, GLSLParser.Stage.VERTEX);
      lexer = new Lexer(fshSrc);
      parser.parse(lexer, GLSLParser.Stage.FRAGMENT);
    } catch (Exception var21) {
      throw new RuntimeException(
          "Caught exception while parsing: %s".formatted(renderPipeline.toString()), var21);
    }

    UBO[] ubos = parser.createUBOs();
    List<ImageDescriptor> samplers = parser.getSamplerList();
    String vshProcessed = parser.getOutput(GLSLParser.Stage.VERTEX);
    String fshProcessed = parser.getOutput(GLSLParser.Stage.FRAGMENT);
    builder.setUniforms(List.of(ubos), samplers);
    builder.setShaderSrc(SPIRVUtils.ShaderKind.VERTEX_SHADER, vshProcessed);
    builder.setShaderSrc(SPIRVUtils.ShaderKind.FRAGMENT_SHADER, fshProcessed);

    GraphicsPipeline pipeline;
    try {
      pipeline = builder.createGraphicsPipeline();
      pipeline.setStencilTest(renderPipeline.getStencilTest());
    } catch (Exception var20) {
      var20.printStackTrace();
      throw new RuntimeException("Exception while compiling pipeline %s".formatted(renderPipeline));
    }

    extPipeline.setPipeline(pipeline);
    compiledPipelines.put(renderPipeline, pipeline);
  }

  record ShaderCompilationKey(Identifier id, ShaderType type, ShaderDefines defines) {
    @Override
    public String toString() {
      String string = this.id + " (" + this.type + ")";
      return !this.defines.isEmpty() ? string + " with " + this.defines : string;
    }
  }

  private static class VkRenderPipeline implements CompiledRenderPipeline {
    final RenderPipeline renderPipeline;

    public VkRenderPipeline(RenderPipeline renderPipeline) {
      this.renderPipeline = renderPipeline;
    }

    @Override
    public boolean isValid() {
      return true;
    }
  }
}
