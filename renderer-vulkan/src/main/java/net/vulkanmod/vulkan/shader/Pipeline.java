// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.framebuffer.RenderPass;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.buffer.UniformBuffer;
import net.vulkanmod.vulkan.shader.descriptor.ImageDescriptor;
import net.vulkanmod.vulkan.shader.descriptor.UBO;
import net.vulkanmod.vulkan.shader.layout.AlignedStruct;
import net.vulkanmod.vulkan.shader.layout.PushConstants;
import net.vulkanmod.vulkan.shader.layout.Uniform;
import net.vulkanmod.vulkan.util.MappedBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding.Buffer;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

public abstract class Pipeline {
  private static final VkDevice DEVICE = Vulkan.getVkDevice();
  protected static final long PIPELINE_CACHE = createPipelineCache();
  protected static final List<Pipeline> PIPELINES = new ArrayList<>();
  public final String name;
  protected long descriptorSetLayout;
  protected long pipelineLayout;
  protected DescriptorSets[] descriptorSets;
  protected List<UBO> buffers;
  protected List<ImageDescriptor> imageDescriptors;
  protected PushConstants pushConstants;
  private int storageBytes;

  private static long createPipelineCache() {
    return net.vulkanmod.cache.DriverPipelineCache.create();
  }

  public static void destroyPipelineCache() {
    net.vulkanmod.cache.DriverPipelineCache.save(PIPELINE_CACHE);
    VK10.vkDestroyPipelineCache(DEVICE, PIPELINE_CACHE, null);
  }

  public static void recreateDescriptorSets(int frames) {
    PIPELINES.forEach(
        pipeline -> {
          pipeline.destroyDescriptorSets();
          pipeline.createDescriptorSets(frames);
        });
  }

  public Pipeline(String name) {
    this.name = name;
  }

  protected void createDescriptorSetLayout() {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      int bindingsSize = this.buffers.size() + this.imageDescriptors.size();
      Buffer bindings = VkDescriptorSetLayoutBinding.calloc(bindingsSize, stack);

      for (UBO ubo : this.buffers) {
        VkDescriptorSetLayoutBinding uboLayoutBinding = bindings.get(ubo.getBinding());
        uboLayoutBinding.binding(ubo.getBinding());
        uboLayoutBinding.descriptorCount(1);
        uboLayoutBinding.descriptorType(ubo.getType());
        uboLayoutBinding.pImmutableSamplers(null);
        uboLayoutBinding.stageFlags(ubo.getStages());
      }

      for (ImageDescriptor imageDescriptor : this.imageDescriptors) {
        VkDescriptorSetLayoutBinding samplerLayoutBinding =
            bindings.get(imageDescriptor.getBinding());
        samplerLayoutBinding.binding(imageDescriptor.getBinding());
        samplerLayoutBinding.descriptorCount(1);
        samplerLayoutBinding.descriptorType(imageDescriptor.getType());
        samplerLayoutBinding.pImmutableSamplers(null);
        samplerLayoutBinding.stageFlags(imageDescriptor.getStages());
      }

      VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);
      layoutInfo.sType(32);
      layoutInfo.pBindings(bindings);
      LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
      if (VK10.vkCreateDescriptorSetLayout(
              DeviceManager.vkDevice, layoutInfo, null, pDescriptorSetLayout)
          != 0) {
        throw new RuntimeException("Failed to create descriptor set layout");
      }

      this.descriptorSetLayout = pDescriptorSetLayout.get(0);
    }
  }

  protected void createPipelineLayout() {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
      pipelineLayoutInfo.sType(30);
      pipelineLayoutInfo.pSetLayouts(stack.longs(this.descriptorSetLayout));
      for (ImageDescriptor descriptor : imageDescriptors)
        if (descriptor.isStorageBuffer()) storageBytes += 4;
      if (storageBytes > 0 && pushConstants != null)
        throw new IllegalArgumentException("Conflicting push constant layouts: " + name);
      if (storageBytes > DeviceManager.deviceProperties.limits().maxPushConstantsSize())
        throw new IllegalArgumentException("Texel metadata exceeds device limit");
      if (this.pushConstants != null || storageBytes > 0) {
        org.lwjgl.vulkan.VkPushConstantRange.Buffer pushConstantRange =
            VkPushConstantRange.calloc(1, stack);
        pushConstantRange.size(storageBytes > 0 ? storageBytes : this.pushConstants.getSize());
        pushConstantRange.offset(0);
        pushConstantRange.stageFlags(storageBytes > 0 ? 17 : this.pushConstants.stages);
        pipelineLayoutInfo.pPushConstantRanges(pushConstantRange);
      }

      LongBuffer pPipelineLayout = stack.longs(0L);
      if (VK10.vkCreatePipelineLayout(DEVICE, pipelineLayoutInfo, null, pPipelineLayout) != 0) {
        throw new RuntimeException("Failed to create pipeline layout");
      }

      this.pipelineLayout = pPipelineLayout.get(0);
    }
  }

  protected void createDescriptorSets(int frames) {
    this.descriptorSets = new DescriptorSets[frames];

    for (int i = 0; i < frames; i++) {
      this.descriptorSets[i] = new DescriptorSets(this);
    }
  }

  public void scheduleCleanUp() {
    MemoryManager.getInstance().addFrameOp(this::cleanUp);
  }

  public abstract void cleanUp();

  protected void destroyDescriptorSets() {
    if (descriptorSets == null) return;
    for (DescriptorSets sets : descriptorSets) if (sets != null) sets.cleanUp();
    descriptorSets = null;
  }

  public void resetDescriptorPool(int i) {
    if (this.descriptorSets != null) {
      this.descriptorSets[i].resetIdx();
    }
  }

  public PushConstants getPushConstants() {
    return this.pushConstants;
  }

  public void pushStorageOffsets(VkCommandBuffer command) {
    if (storageBytes == 0) return;
    try (var stack = MemoryStack.stackPush()) {
      var offsets = stack.mallocInt(storageBytes / 4);
      for (ImageDescriptor d : imageDescriptors)
        if (d.isStorageBuffer()) offsets.put(d.storageOffset());
      offsets.flip();
      VK10.vkCmdPushConstants(command, pipelineLayout, 17, 0, offsets);
    }
  }

  public long getLayout() {
    return this.pipelineLayout;
  }

  public List<UBO> getBuffers() {
    return this.buffers;
  }

  public UBO getUBO(int binding) {
    return this.getUBO(ubo -> ubo.binding == binding);
  }

  public UBO getUBO(String name) {
    return this.getUBO(ubo -> ubo.name.equals(name));
  }

  public UBO getUBO(Predicate<UBO> fn) {
    UBO ubo = null;

    for (UBO ubo1 : this.buffers) {
      if (fn.test(ubo1)) {
        ubo = ubo1;
      }
    }

    return ubo;
  }

  public ImageDescriptor getImageDescriptor(String name) {
    return this.getImageDescriptor(imageDescriptor -> imageDescriptor.name.equals(name));
  }

  public ImageDescriptor getImageDescriptor(Predicate<ImageDescriptor> fn) {
    ImageDescriptor descriptor = null;

    for (ImageDescriptor descriptor1 : this.imageDescriptors) {
      if (fn.test(descriptor1)) {
        descriptor = descriptor1;
      }
    }

    return descriptor;
  }

  public List<ImageDescriptor> getImageDescriptors() {
    return this.imageDescriptors;
  }

  public void bindDescriptorSets(VkCommandBuffer commandBuffer, int frame) {
    UniformBuffer uniformBuffer = Renderer.getDrawer().getUniformBuffer();
    this.descriptorSets[frame].bindSets(commandBuffer, uniformBuffer, 0);
  }

  public void bindDescriptorSets(
      VkCommandBuffer commandBuffer, UniformBuffer uniformBuffer, int frame) {
    this.descriptorSets[frame].bindSets(commandBuffer, uniformBuffer, 0);
  }

  protected static long createShaderModule(ByteBuffer spirvCode) {
    long var4;
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack);
      createInfo.sType(16);
      createInfo.pCode(spirvCode);
      LongBuffer pShaderModule = stack.mallocLong(1);
      if (VK10.vkCreateShaderModule(DEVICE, createInfo, null, pShaderModule) != 0) {
        throw new RuntimeException("Failed to create shader module");
      }

      var4 = pShaderModule.get(0);
    }

    return var4;
  }

  public static Pipeline.Builder builder(VertexFormat vertexFormat, String path) {
    return new Pipeline.Builder(vertexFormat, path);
  }

  public static class Builder {
    public final VertexFormat vertexFormat;
    public final String name;
    List<UBO> UBOs = new ArrayList<>();
    List<ImageDescriptor> imageDescriptors = new ArrayList<>();
    PushConstants pushConstants;
    int nextBinding;
    Map<SPIRVUtils.ShaderKind, String> shadersSrc = new EnumMap<>(SPIRVUtils.ShaderKind.class);
    RenderPass renderPass;
    Function<Uniform.Info, Supplier<MappedBuffer>> uniformSupplierGetter;

    public Builder(VertexFormat vertexFormat, String name) {
      this.vertexFormat = vertexFormat;
      this.name = name;
    }

    public Builder(VertexFormat vertexFormat) {
      this(vertexFormat, null);
    }

    public Builder(String name) {
      this(null, name);
    }

    public Builder() {
      this(null, null);
    }

    public void setUniforms(List<UBO> UBOs, List<ImageDescriptor> imageDescriptors) {
      this.UBOs = UBOs;
      this.imageDescriptors = imageDescriptors;
    }

    public void setShaderSrc(SPIRVUtils.ShaderKind kind, String src) {
      this.shadersSrc.put(kind, src);
    }

    public void addUBO(UBO ubo) {
      this.UBOs.add(ubo);
    }

    public void addImageDescriptor(ImageDescriptor imageDescriptor) {
      this.imageDescriptors.add(imageDescriptor);
    }

    public Pipeline.Builder applyConfig(PipelineConfig config) {
      for (PipelineConfig.UB ub : config.ubs) {
        this.parseUboNode(ub);
      }

      for (PipelineConfig.ImageDescriptorInfo imageDescriptor : config.imageDescriptors) {
        this.parseImageDescriptor(imageDescriptor);
      }

      if (config.pushConstantsInfo != null) {
        this.parsePushConstantNode(config.pushConstantsInfo);
      }

      return this;
    }

    public void setUniformSupplierGetter(
        Function<Uniform.Info, Supplier<MappedBuffer>> uniformSupplierGetter) {
      this.uniformSupplierGetter = uniformSupplierGetter;
    }

    private void parseUboNode(PipelineConfig.UB ub) {
      int binding = ub.binding;
      int stages = ub.stage;
      AlignedStruct.Builder builder = new AlignedStruct.Builder();
      UBO ubo;
      if (!ub.uniforms.isEmpty()) {
        for (PipelineConfig.Uniform field : ub.uniforms) {
          String name = field.name();
          String type = field.type();
          Uniform.Info uniformInfo = Uniform.createUniformInfo(type, name);
          uniformInfo.setupSupplier();
          if (!uniformInfo.hasSupplier()) {
            if (this.uniformSupplierGetter == null) {
              throw new IllegalStateException(
                  "No uniform supplier found for uniform: (%s:%s)".formatted(type, name));
            }

            Supplier<MappedBuffer> uniformSupplier = this.uniformSupplierGetter.apply(uniformInfo);
            if (uniformSupplier == null) {
              throw new IllegalStateException(
                  "No uniform supplier found for uniform: (%s:%s)".formatted(type, name));
            }

            uniformInfo.setBufferSupplier(uniformSupplier);
          }

          builder.addUniform(uniformInfo);
        }

        ubo = builder.buildUBO(binding, stages);
      } else {
        int size = ub.size;
        if (size <= 0) {
          throw new IllegalStateException("Manual UBO has size <= 0");
        }

        ubo = new UBO("UBO %d".formatted(binding), binding, stages, size, null);
        ubo.setUseGlobalBuffer(false);
      }

      this.UBOs.add(ubo);
    }

    private void parseImageDescriptor(PipelineConfig.ImageDescriptorInfo info) {
      String var3 = info.type();

      int descriptorType =
          switch (var3) {
            case "sampler2D" -> 1;
            case "image2D" -> 3;
            default -> throw new IllegalStateException("Unexpected value: " + info.type());
          };
      this.imageDescriptors.add(
          new ImageDescriptor(
              info.binding(), info.type(), info.name(), info.imageIdx(), descriptorType));
    }

    private void parsePushConstantNode(PipelineConfig.UB ub) {
      AlignedStruct.Builder builder = new AlignedStruct.Builder();
      int stages = ub.stage;

      for (PipelineConfig.Uniform field : ub.uniforms) {
        String name = field.name();
        String type = field.type();
        Uniform.Info uniformInfo = Uniform.createUniformInfo(type, name);
        builder.addUniform(uniformInfo);
      }

      this.pushConstants = builder.buildPushConstant(stages);
    }

    public List<UBO> getUBOs() {
      return this.UBOs;
    }

    public List<ImageDescriptor> getImageDescriptors() {
      return this.imageDescriptors;
    }

    public PushConstants getPushConstants() {
      return this.pushConstants;
    }

    public Map<SPIRVUtils.ShaderKind, String> getShadersSrc() {
      return this.shadersSrc;
    }

    public GraphicsPipeline createGraphicsPipeline() {
      return new GraphicsPipeline(this);
    }
  }
}
