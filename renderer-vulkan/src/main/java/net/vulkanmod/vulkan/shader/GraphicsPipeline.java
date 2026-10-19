// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormatElement.Type;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.List;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfoKHR;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo.Buffer;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public class GraphicsPipeline extends Pipeline {
  private final Object2LongMap<PipelineState> graphicsPipelines = new Object2LongOpenHashMap<>();
  private final VertexFormat vertexFormat;
  private net.neoforged.neoforge.client.stencil.StencilTest stencil;

  public void setStencilTest(
      java.util.Optional<net.neoforged.neoforge.client.stencil.StencilTest> test) {
    stencil = test.orElse(null);
  }

  private static void stencilFace(
      org.lwjgl.vulkan.VkStencilOpState target,
      net.neoforged.neoforge.client.stencil.StencilPerFaceTest face,
      net.neoforged.neoforge.client.stencil.StencilTest test) {
    target
        .failOp(face.fail().ordinal())
        .depthFailOp(face.depthFail().ordinal())
        .passOp(face.pass().ordinal())
        .compareOp(
            PipelineState.DepthState.decodeDepthFun(
                PipelineState.DepthState.encodeDepthFun(
                    com.mojang.blaze3d.opengl.GlConst.toGl(face.compare()))))
        .compareMask(test.readMask())
        .writeMask(test.writeMask())
        .reference(test.referenceValue());
  }

  private final GraphicsPipeline.VertexInputDescription vertexInputDescription;
  private boolean destroyed;
  private long vertShaderModule = 0L;
  private long fragShaderModule = 0L;

  GraphicsPipeline(Pipeline.Builder builder) {
    super(builder.name);
    this.buffers = builder.UBOs;
    this.imageDescriptors = builder.imageDescriptors;
    this.pushConstants = builder.pushConstants;
    this.vertexFormat = builder.vertexFormat;
    this.vertexInputDescription = new GraphicsPipeline.VertexInputDescription(this.vertexFormat);
    try {
      this.createDescriptorSetLayout();
      this.createPipelineLayout();
      this.createShaderModules(builder);
      if (builder.renderPass != null) {
        this.graphicsPipelines.computeIfAbsent(PipelineState.DEFAULT, this::createGraphicsPipeline);
      }

      this.createDescriptorSets(Renderer.getFramesNum());
      PIPELINES.add(this);
    } catch (RuntimeException | Error failure) {
      try {
        cleanUp();
      } catch (Throwable cleanup) {
        failure.addSuppressed(cleanup);
      }
      throw failure;
    }
  }

  public long getHandle(PipelineState state) {
    return this.graphicsPipelines.computeIfAbsent(state, this::createGraphicsPipeline);
  }

  private long createGraphicsPipeline(PipelineState state) {
    long var26;
    try (MemoryStack stack = MemoryStack.stackPush()) {
      ByteBuffer entryPoint = stack.UTF8("main");
      Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
      VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
      vertShaderStageInfo.sType(18);
      vertShaderStageInfo.stage(1);
      vertShaderStageInfo.module(this.vertShaderModule);
      vertShaderStageInfo.pName(entryPoint);
      VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);
      fragShaderStageInfo.sType(18);
      fragShaderStageInfo.stage(16);
      fragShaderStageInfo.module(this.fragShaderModule);
      fragShaderStageInfo.pName(entryPoint);
      VkPipelineVertexInputStateCreateInfo vertexInputInfo =
          VkPipelineVertexInputStateCreateInfo.calloc(stack);
      vertexInputInfo.sType(19);
      if (this.vertexInputDescription != null) {
        vertexInputInfo.pVertexBindingDescriptions(this.vertexInputDescription.bindingDescriptions);
        vertexInputInfo.pVertexAttributeDescriptions(
            this.vertexInputDescription.attributeDescriptions);
      }

      int topology = PipelineState.AssemblyRasterState.decodeTopology(state.assemblyRasterState);
      VkPipelineInputAssemblyStateCreateInfo inputAssembly =
          VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
      inputAssembly.sType(20);
      inputAssembly.topology(topology);
      inputAssembly.primitiveRestartEnable(false);
      VkPipelineViewportStateCreateInfo viewportState =
          VkPipelineViewportStateCreateInfo.calloc(stack);
      viewportState.sType(22);
      viewportState.viewportCount(1);
      viewportState.scissorCount(1);
      int polygonMode =
          PipelineState.AssemblyRasterState.decodePolygonMode(state.assemblyRasterState);
      int cullMode = PipelineState.AssemblyRasterState.decodeCullMode(state.assemblyRasterState);
      VkPipelineRasterizationStateCreateInfo rasterizer =
          VkPipelineRasterizationStateCreateInfo.calloc(stack);
      rasterizer.sType(23);
      rasterizer.depthClampEnable(false);
      rasterizer.rasterizerDiscardEnable(false);
      rasterizer.polygonMode(polygonMode);
      rasterizer.lineWidth(1.0F);
      rasterizer.cullMode(cullMode);
      rasterizer.frontFace(0);
      rasterizer.depthBiasEnable(true);
      VkPipelineMultisampleStateCreateInfo multisampling =
          VkPipelineMultisampleStateCreateInfo.calloc(stack);
      multisampling.sType(24);
      multisampling.sampleShadingEnable(false);
      multisampling.rasterizationSamples(1);
      VkPipelineDepthStencilStateCreateInfo depthStencil =
          VkPipelineDepthStencilStateCreateInfo.calloc(stack);
      depthStencil.sType(25);
      depthStencil.depthTestEnable(PipelineState.DepthState.depthTest(state.depthState_i));
      depthStencil.depthWriteEnable(PipelineState.DepthState.depthMask(state.depthState_i));
      depthStencil.depthCompareOp(PipelineState.DepthState.decodeDepthFun(state.depthState_i));
      depthStencil.depthBoundsTestEnable(false);
      depthStencil.minDepthBounds(0.0F);
      depthStencil.maxDepthBounds(1.0F);
      depthStencil.stencilTestEnable(stencil != null);
      if (stencil != null) {
        stencilFace(depthStencil.front(), stencil.front(), stencil);
        stencilFace(depthStencil.back(), stencil.back(), stencil);
      }
      org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment =
          VkPipelineColorBlendAttachmentState.calloc(1, stack);
      colorBlendAttachment.colorWriteMask(state.colorMask_i);
      if (PipelineState.BlendState.enable(state.blendState_i)) {
        colorBlendAttachment.blendEnable(true);
        colorBlendAttachment.srcColorBlendFactor(
            PipelineState.BlendState.getSrcRgbFactor(state.blendState_i));
        colorBlendAttachment.dstColorBlendFactor(
            PipelineState.BlendState.getDstRgbFactor(state.blendState_i));
        colorBlendAttachment.colorBlendOp(PipelineState.BlendState.blendOp(state.blendState_i));
        colorBlendAttachment.srcAlphaBlendFactor(
            PipelineState.BlendState.getSrcAlphaFactor(state.blendState_i));
        colorBlendAttachment.dstAlphaBlendFactor(
            PipelineState.BlendState.getDstAlphaFactor(state.blendState_i));
        colorBlendAttachment.alphaBlendOp(PipelineState.BlendState.blendOp(state.blendState_i));
      } else {
        colorBlendAttachment.blendEnable(false);
      }

      VkPipelineColorBlendStateCreateInfo colorBlending =
          VkPipelineColorBlendStateCreateInfo.calloc(stack);
      colorBlending.sType(26);
      colorBlending.logicOpEnable(PipelineState.LogicOpState.enable(state.logicOp_i));
      colorBlending.logicOp(PipelineState.LogicOpState.decodeFun(state.logicOp_i));
      colorBlending.pAttachments(colorBlendAttachment);
      colorBlending.blendConstants(stack.floats(0.0F, 0.0F, 0.0F, 0.0F));
      VkPipelineDynamicStateCreateInfo dynamicStates =
          VkPipelineDynamicStateCreateInfo.calloc(stack);
      dynamicStates.sType(27);
      if (topology != 1 && polygonMode != 1) {
        dynamicStates.pDynamicStates(stack.ints(3, 0, 1));
      } else {
        dynamicStates.pDynamicStates(stack.ints(3, 0, 1, 2));
      }

      org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo.Buffer pipelineInfo =
          VkGraphicsPipelineCreateInfo.calloc(1, stack);
      pipelineInfo.sType(28);
      pipelineInfo.pStages(shaderStages);
      pipelineInfo.pVertexInputState(vertexInputInfo);
      pipelineInfo.pInputAssemblyState(inputAssembly);
      pipelineInfo.pViewportState(viewportState);
      pipelineInfo.pRasterizationState(rasterizer);
      pipelineInfo.pMultisampleState(multisampling);
      pipelineInfo.pDepthStencilState(depthStencil);
      pipelineInfo.pColorBlendState(colorBlending);
      pipelineInfo.pDynamicState(dynamicStates);
      pipelineInfo.layout(this.pipelineLayout);
      pipelineInfo.basePipelineHandle(0L);
      pipelineInfo.basePipelineIndex(-1);
      VkPipelineRenderingCreateInfoKHR renderingInfo =
          VkPipelineRenderingCreateInfoKHR.calloc(stack);
      renderingInfo.sType(1000044002);
      Framebuffer framebuffer;
      if (state.renderPass != null) {
        framebuffer = state.renderPass.getFramebuffer();
      } else {
        framebuffer = Renderer.getInstance().getMainPass().getMainFramebuffer();
      }

      renderingInfo.pColorAttachmentFormats(stack.ints(framebuffer.getFormat()));
      renderingInfo.depthAttachmentFormat(framebuffer.getDepthFormat());
      if (framebuffer.getDepthAttachment() != null
          && (framebuffer.getDepthAttachment().aspect & 4) != 0)
        renderingInfo.stencilAttachmentFormat(framebuffer.getDepthFormat());
      pipelineInfo.pNext(renderingInfo);
      org.lwjgl.vulkan.VkPipelineCreationFeedback feedback = null;
      if (DeviceManager.enabledExtensions.contains("VK_EXT_pipeline_creation_feedback")) {
        feedback =
            org.lwjgl.vulkan.VkPipelineCreationFeedback.create(
                stack.ncalloc(
                    org.lwjgl.vulkan.VkPipelineCreationFeedback.ALIGNOF,
                    1,
                    org.lwjgl.vulkan.VkPipelineCreationFeedback.SIZEOF));
        var info =
            org.lwjgl.vulkan.VkPipelineCreationFeedbackCreateInfo.calloc(stack)
                .sType$Default()
                .pPipelineCreationFeedback(feedback);
        renderingInfo.pNext(info.address());
      }
      LongBuffer pGraphicsPipeline = stack.mallocLong(1);
      Vulkan.checkResult(
          VK10.vkCreateGraphicsPipelines(
              DeviceManager.vkDevice, PIPELINE_CACHE, pipelineInfo, null, pGraphicsPipeline),
          "Failed to create graphics pipeline " + this.name);
      var26 = pGraphicsPipeline.get(0);
      Vulkan.setDebugLabel(stack, VK10.VK_OBJECT_TYPE_PIPELINE, var26, name);
      net.vulkanmod.vulkan.VulkanStatistics.pipelineCreates++;
      if (feedback != null && (feedback.flags() & 1) != 0) {
        net.vulkanmod.vulkan.VulkanStatistics.pipelineFeedbackValid++;
        net.vulkanmod.vulkan.VulkanStatistics.pipelineCreationNanos += feedback.duration();
        if ((feedback.flags() & 2) != 0) net.vulkanmod.vulkan.VulkanStatistics.pipelineCacheHits++;
      }
    }

    return var26;
  }

  private void createShaderModules(Pipeline.Builder builder) {
    try (var vertex =
            SPIRVUtils.compileShader(
                name + ".vsh",
                builder.shadersSrc.get(SPIRVUtils.ShaderKind.VERTEX_SHADER),
                SPIRVUtils.ShaderKind.VERTEX_SHADER);
        var fragment =
            SPIRVUtils.compileShader(
                name + ".fsh",
                builder.shadersSrc.get(SPIRVUtils.ShaderKind.FRAGMENT_SHADER),
                SPIRVUtils.ShaderKind.FRAGMENT_SHADER)) {
      vertShaderModule = createShaderModule(vertex.bytecode());
      fragShaderModule = createShaderModule(fragment.bytecode());
    }
  }

  @Override
  public void cleanUp() {
    if (destroyed) return;
    destroyed = true;
    VK10.vkDestroyShaderModule(DeviceManager.vkDevice, this.vertShaderModule, null);
    VK10.vkDestroyShaderModule(DeviceManager.vkDevice, this.fragShaderModule, null);
    this.vertexInputDescription.cleanUp();
    this.destroyDescriptorSets();
    this.graphicsPipelines.forEach(
        (state, pipeline) -> VK10.vkDestroyPipeline(DeviceManager.vkDevice, pipeline, null));
    this.graphicsPipelines.clear();
    VK10.vkDestroyDescriptorSetLayout(DeviceManager.vkDevice, this.descriptorSetLayout, null);
    VK10.vkDestroyPipelineLayout(DeviceManager.vkDevice, this.pipelineLayout, null);
    PIPELINES.remove(this);
    Renderer.getInstance().removeUsedPipeline(this);
  }

  private static org.lwjgl.vulkan.VkVertexInputBindingDescription.Buffer getBindingDescription(
      VertexFormat vertexFormat) {
    org.lwjgl.vulkan.VkVertexInputBindingDescription.Buffer bindingDescription =
        VkVertexInputBindingDescription.calloc(1);
    bindingDescription.binding(0);
    bindingDescription.stride(vertexFormat.getVertexSize());
    bindingDescription.inputRate(0);
    return bindingDescription;
  }

  private static org.lwjgl.vulkan.VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(
      VertexFormat vertexFormat) {
    List<VertexFormatElement> elements = vertexFormat.getElements();
    int size = elements.size();
    org.lwjgl.vulkan.VkVertexInputAttributeDescription.Buffer attributeDescriptions =
        VkVertexInputAttributeDescription.calloc(size);
    int offset = 0;

    for (int i = 0; i < size; i++) {
      VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(i);
      posDescription.binding(0);
      posDescription.location(i);
      VertexFormatElement formatElement = elements.get(i);
      Type type = formatElement.type();
      int count = formatElement.count();

      offset =
          switch (type) {
            case FLOAT -> {
              switch (count) {
                case 1:
                  posDescription.format(100);
                  posDescription.offset(offset);
                  yield 4;
                case 2:
                  posDescription.format(103);
                  posDescription.offset(offset);
                  yield 8;
                case 3:
                  posDescription.format(106);
                  posDescription.offset(offset);
                  yield 12;
                default:
                  throw new IllegalStateException("Unexpected value: " + count);
              }
            }
            case UBYTE -> {
              switch (count) {
                case 4:
                  if (formatElement.normalized()) {
                    posDescription.format(37);
                  } else {
                    posDescription.format(41);
                  }

                  posDescription.offset(offset);
                  yield 4;
                default:
                  throw new IllegalStateException("Unexpected value: " + count);
              }
            }
            case BYTE -> {
              switch (count) {
                case 3:
                  if (formatElement.normalized()) {
                    posDescription.format(38);
                  } else {
                    posDescription.format(42);
                  }

                  posDescription.offset(offset);
                  yield 4;
                case 4:
                  if (formatElement.normalized()) {
                    posDescription.format(38);
                  } else {
                    posDescription.format(42);
                  }

                  posDescription.offset(offset);
                  yield 4;
                default:
                  throw new IllegalStateException("Unexpected value: " + count);
              }
            }
            case USHORT -> {
              switch (count) {
                case 2:
                  posDescription.format(81);
                  posDescription.offset(offset);
                  yield 4;
                case 4:
                  posDescription.format(95);
                  posDescription.offset(offset);
                  yield 8;
                default:
                  throw new IllegalStateException("Unexpected value: " + count);
              }
            }
            case SHORT -> {
              switch (count) {
                case 1:
                  posDescription.format(75);
                  posDescription.offset(offset);
                  yield 2;
                case 2:
                  posDescription.format(82);
                  posDescription.offset(offset);
                  yield 8;
                case 3:
                default:
                  throw new IllegalStateException("Unexpected value: " + count);
                case 4:
                  posDescription.format(96);
                  posDescription.offset(offset);
                  yield 4;
              }
            }
            case UINT -> {
              switch (count) {
                case 1:
                  posDescription.format(98);
                  posDescription.offset(offset);
                  yield 4;
                default:
                  throw new IllegalStateException("Unexpected value: " + count);
              }
            }
            case INT -> {
              switch (count) {
                case 1:
                  posDescription.format(99);
                  posDescription.offset(offset);
                  yield 4;
                default:
                  throw new IllegalStateException("Unexpected value: " + count);
              }
            }
          };
      posDescription.offset(vertexFormat.getOffset(formatElement));
    }

    return attributeDescriptions.rewind();
  }

  static class VertexInputDescription {
    final org.lwjgl.vulkan.VkVertexInputAttributeDescription.Buffer attributeDescriptions;
    final org.lwjgl.vulkan.VkVertexInputBindingDescription.Buffer bindingDescriptions;

    VertexInputDescription(VertexFormat vertexFormat) {
      if (vertexFormat != DefaultVertexFormat.EMPTY) {
        this.bindingDescriptions = GraphicsPipeline.getBindingDescription(vertexFormat);
        this.attributeDescriptions = GraphicsPipeline.getAttributeDescriptions(vertexFormat);
      } else {
        this.bindingDescriptions = null;
        this.attributeDescriptions = null;
      }
    }

    void cleanUp() {
      if (this.bindingDescriptions != null) {
        MemoryUtil.memFree(this.bindingDescriptions);
        MemoryUtil.memFree(this.attributeDescriptions);
      }
    }
  }
}
