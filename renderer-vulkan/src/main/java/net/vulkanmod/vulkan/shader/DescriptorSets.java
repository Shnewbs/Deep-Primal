// VulkanMod 0.6.8+26.1.2; Deep Primal exact descriptor reuse. LGPL-3.0-only.
package net.vulkanmod.vulkan.shader;

import java.nio.IntBuffer;
import net.vulkanmod.vulkan.*;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.buffer.*;
import net.vulkanmod.vulkan.shader.descriptor.*;
import org.lwjgl.system.*;
import org.lwjgl.vulkan.*;

public final class DescriptorSets {
  private static final VkDevice DEVICE = Vulkan.getVkDevice();
  private final Pipeline pipeline;
  private int poolSize = 10, currentIdx = -1;
  private long descriptorPool, currentSet;
  private long[] sets;
  private final DescriptorStateCache states;
  private final IntBuffer dynamicOffsets;
  private boolean closed;

  DescriptorSets(Pipeline pipeline) {
    this.pipeline = pipeline;
    states =
        new DescriptorStateCache(pipeline.buffers.size() + pipeline.imageDescriptors.size() * 3);
    dynamicOffsets = MemoryUtil.memAllocInt(pipeline.buffers.size());
    try (var stack = MemoryStack.stackPush()) {
      createDescriptorPool(stack);
      createDescriptorSets(stack);
    } catch (RuntimeException | Error failure) {
      cleanUp();
      throw failure;
    }
  }

  public void bindSets(VkCommandBuffer command, UniformBuffer uniform, int bindPoint) {
    try (var stack = MemoryStack.stackPush()) {
      updateUniforms(uniform);
      updateDescriptorSet(stack, uniform);
      if (Renderer.getInstance()
          .descriptorBindingChanged(pipeline.pipelineLayout, currentSet, bindPoint, dynamicOffsets))
        VK10.vkCmdBindDescriptorSets(
            command,
            bindPoint,
            pipeline.pipelineLayout,
            0,
            stack.longs(currentSet),
            dynamicOffsets);
    }
  }

  private void updateUniforms(UniformBuffer global) {
    int i = 0;
    for (UBO ubo : pipeline.getBuffers()) {
      if (ubo.getBufferSlice().getBuffer() == null) {
        ubo.setUseGlobalBuffer(true);
        ubo.setUpdate(true);
      }
      int offset;
      if (!ubo.useGlobalBuffer()) offset = (int) ubo.getBufferSlice().getOffset();
      else {
        offset = (int) global.getUsedBytes();
        int size = UniformBuffer.getAlignedSize(ubo.getSize());
        global.checkCapacity(size);
        if (ubo.shouldUpdate()) ubo.update(global.getPointer());
        global.updateOffset(size);
        ubo.getBufferSlice().set(global, offset, size);
      }
      dynamicOffsets.put(i++, offset);
    }
  }

  private void captureState(UniformBuffer fallback) {
    int index = 0;
    for (UBO ubo : pipeline.buffers) {
      var buffer = ubo.getBufferSlice().getBuffer();
      states.state[index++] = (buffer == null ? fallback : buffer).getId();
    }
    for (ImageDescriptor d : pipeline.imageDescriptors) {
      if (d.isStorageBuffer()) {
        states.state[index++] = d.storageBuffer();
        states.state[index++] = d.storageRange();
        states.state[index++] = 0;
      } else {
        var image = java.util.Objects.requireNonNull(d.getImage(), "No image bound: " + d.name);
        if (d.isReadOnlyLayout) image.readOnlyLayout();
        states.state[index++] = d.getImageView(image);
        states.state[index++] = d.useSampler ? image.getSampler() : 0;
        states.state[index++] = d.getLayout();
      }
    }
  }

  private void updateDescriptorSet(MemoryStack stack, UniformBuffer uniform) {
    captureState(uniform);
    int cached = states.find();
    if (cached >= 0) {
      currentSet = sets[cached];
      VulkanStatistics.descriptorReuse++;
      return;
    }
    if (++currentIdx >= poolSize) {
      poolSize = Math.multiplyExact(poolSize, 2);
      states.clear();
      createDescriptorPool(stack);
      createDescriptorSets(stack);
      currentIdx = 0;
    }
    currentSet = sets[currentIdx];
    var writes =
        VkWriteDescriptorSet.calloc(
            pipeline.buffers.size() + pipeline.imageDescriptors.size(), stack);
    int binding = 0, state = 0;
    for (UBO ubo : pipeline.buffers) {
      var info =
          VkDescriptorBufferInfo.calloc(1, stack)
              .buffer(states.state[state++])
              .range(ubo.getSize());
      writes
          .get(binding++)
          .sType$Default()
          .dstSet(currentSet)
          .dstBinding(ubo.getBinding())
          .descriptorType(ubo.getType())
          .descriptorCount(1)
          .pBufferInfo(info);
    }
    for (ImageDescriptor d : pipeline.imageDescriptors) {
      long first = states.state[state++], second = states.state[state++];
      int layout = (int) states.state[state++];
      var write =
          writes
              .get(binding++)
              .sType$Default()
              .dstSet(currentSet)
              .dstBinding(d.getBinding())
              .descriptorType(d.getType())
              .descriptorCount(1);
      if (d.isStorageBuffer())
        write.pBufferInfo(VkDescriptorBufferInfo.calloc(1, stack).buffer(first).range(second));
      else
        write.pImageInfo(
            VkDescriptorImageInfo.calloc(1, stack)
                .imageView(first)
                .sampler(second)
                .imageLayout(layout));
    }
    VK10.vkUpdateDescriptorSets(DEVICE, writes, null);
    states.remember(currentIdx);
    VulkanStatistics.descriptorUpdates++;
  }

  private void createDescriptorSets(MemoryStack stack) {
    var layouts = MemoryUtil.memAllocLong(poolSize);
    try {
      for (int i = 0; i < poolSize; i++) layouts.put(i, pipeline.descriptorSetLayout);
      var info =
          VkDescriptorSetAllocateInfo.calloc(stack)
              .sType$Default()
              .descriptorPool(descriptorPool)
              .pSetLayouts(layouts);
      sets = new long[poolSize];
      Vulkan.checkResult(
          VK10.vkAllocateDescriptorSets(DEVICE, info, sets), "Cannot allocate descriptor sets");
    } finally {
      MemoryUtil.memFree(layouts);
    }
  }

  private void createDescriptorPool(MemoryStack stack) {
    var sizes =
        VkDescriptorPoolSize.calloc(
            pipeline.buffers.size() + pipeline.imageDescriptors.size(), stack);
    int i = 0;
    for (UBO ubo : pipeline.buffers) sizes.get(i++).type(ubo.getType()).descriptorCount(poolSize);
    for (ImageDescriptor d : pipeline.imageDescriptors)
      sizes.get(i++).type(d.getType()).descriptorCount(poolSize);
    var info =
        VkDescriptorPoolCreateInfo.calloc(stack)
            .sType$Default()
            .pPoolSizes(sizes)
            .maxSets(poolSize);
    var out = stack.mallocLong(1);
    Vulkan.checkResult(
        VK10.vkCreateDescriptorPool(DEVICE, info, null, out), "Cannot create descriptor pool");
    if (descriptorPool != 0) {
      long old = descriptorPool;
      MemoryManager.getInstance().addFrameOp(() -> VK10.vkDestroyDescriptorPool(DEVICE, old, null));
    }
    descriptorPool = out.get(0);
  }

  public void resetIdx() {
    currentIdx = -1;
    states.clear();
  }

  public void cleanUp() {
    if (closed) return;
    closed = true;
    VK10.vkDestroyDescriptorPool(DEVICE, descriptorPool, null);
    MemoryUtil.memFree(dynamicOffsets);
  }
}
