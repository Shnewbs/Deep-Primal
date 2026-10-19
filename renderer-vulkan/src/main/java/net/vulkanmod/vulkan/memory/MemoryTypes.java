// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.memory;

import java.nio.ByteBuffer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import net.vulkanmod.vulkan.memory.buffer.StagingBuffer;
import net.vulkanmod.vulkan.util.VUtil;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkMemoryHeap;
import org.lwjgl.vulkan.VkMemoryType;

public class MemoryTypes {
  public static MemoryType GPU_MEM;
  public static MemoryType HOST_MEM;

  public static void createMemoryTypes() {
    for (int i = 0; i < DeviceManager.memoryProperties.memoryTypeCount(); i++) {
      VkMemoryType memoryType = DeviceManager.memoryProperties.memoryTypes(i);
      VkMemoryHeap heap = DeviceManager.memoryProperties.memoryHeaps(memoryType.heapIndex());
      int propertyFlags = memoryType.propertyFlags();
      if (propertyFlags == 1) {
        GPU_MEM = new MemoryTypes.DeviceLocalMemory(memoryType, heap);
      }

      if (propertyFlags == 6) {
        HOST_MEM = new MemoryTypes.HostCoherentMemory(memoryType, heap);
      }
    }

    if (GPU_MEM == null || HOST_MEM == null) {
      for (int i = 0; i < DeviceManager.memoryProperties.memoryTypeCount(); i++) {
        VkMemoryType memoryTypex = DeviceManager.memoryProperties.memoryTypes(i);
        VkMemoryHeap heapx = DeviceManager.memoryProperties.memoryHeaps(memoryTypex.heapIndex());
        if ((memoryTypex.propertyFlags() & 7) == 7) {
          GPU_MEM = new MemoryTypes.DeviceMappableMemory(memoryTypex, heapx);
        }

        if ((memoryTypex.propertyFlags() & 6) == 6) {
          HOST_MEM = new MemoryTypes.HostCoherentMemory(memoryTypex, heapx);
        }

        if (GPU_MEM != null && HOST_MEM != null) {
          return;
        }
      }

      GPU_MEM = HOST_MEM;
    }
  }

  public static class DeviceLocalMemory extends MemoryType {
    DeviceLocalMemory(VkMemoryType vkMemoryType, VkMemoryHeap vkMemoryHeap) {
      super(MemoryType.Type.DEVICE_LOCAL, vkMemoryType, vkMemoryHeap);
    }

    @Override
    public void createBuffer(Buffer buffer, long size) {
      MemoryManager.getInstance().createBuffer(buffer, size, 3 | buffer.usage, 1);
    }

    @Override
    public void copyToBuffer(
        Buffer buffer, ByteBuffer src, long size, long srcOffset, long dstOffset) {
      StagingBuffer stagingBuffer = Vulkan.getStagingBuffer();
      stagingBuffer.align(4);
      stagingBuffer.copyBuffer(Math.toIntExact(size), MemoryUtil.memAddress(src) + srcOffset);
      var command =
          net.vulkanmod.render.texture.ImageUploadHelper.INSTANCE
              .getOrStartCommandBuffer()
              .getHandle();
      try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
        var copy =
            org.lwjgl.vulkan.VkBufferCopy.calloc(1, stack)
                .srcOffset(stagingBuffer.getOffset())
                .dstOffset(dstOffset)
                .size(size);
        org.lwjgl.vulkan.VK10.vkCmdCopyBuffer(command, stagingBuffer.getId(), buffer.getId(), copy);
        var barrier =
            org.lwjgl.vulkan.VkBufferMemoryBarrier.calloc(1, stack)
                .sType$Default()
                .buffer(buffer.getId())
                .offset(dstOffset)
                .size(size)
                .srcQueueFamilyIndex(-1)
                .dstQueueFamilyIndex(-1)
                .srcAccessMask(4096)
                .dstAccessMask(org.lwjgl.vulkan.VK10.VK_ACCESS_MEMORY_READ_BIT);
        org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier(
            command,
            4096,
            org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
            0,
            null,
            barrier,
            null);
      }
    }

    @Override
    public boolean mappable() {
      return false;
    }
  }

  static class DeviceMappableMemory extends MemoryTypes.MappableMemory {
    DeviceMappableMemory(VkMemoryType vkMemoryType, VkMemoryHeap vkMemoryHeap) {
      super(MemoryType.Type.DEVICE_LOCAL, vkMemoryType, vkMemoryHeap);
    }

    @Override
    public void createBuffer(Buffer buffer, long size) {
      MemoryManager.getInstance().createBuffer(buffer, size, 3 | buffer.usage, 7);
    }
  }

  static class HostCoherentMemory extends MemoryTypes.MappableMemory {
    HostCoherentMemory(VkMemoryType vkMemoryType, VkMemoryHeap vkMemoryHeap) {
      super(MemoryType.Type.HOST_LOCAL, vkMemoryType, vkMemoryHeap);
    }

    @Override
    public void createBuffer(Buffer buffer, long size) {
      MemoryManager.getInstance().createBuffer(buffer, size, 3 | buffer.usage, 6);
    }
  }

  abstract static class MappableMemory extends MemoryType {
    MappableMemory(MemoryType.Type type, VkMemoryType vkMemoryType, VkMemoryHeap vkMemoryHeap) {
      super(type, vkMemoryType, vkMemoryHeap);
    }

    @Override
    public void copyToBuffer(
        Buffer buffer, ByteBuffer src, long size, long srcOffset, long dstOffset) {
      VUtil.memcpy(src, buffer, size, srcOffset, dstOffset);
    }

    @Override
    public boolean mappable() {
      return true;
    }
  }
}
