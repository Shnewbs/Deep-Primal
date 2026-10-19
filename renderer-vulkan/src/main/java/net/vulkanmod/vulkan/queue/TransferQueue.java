// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.queue;

import net.vulkanmod.vulkan.Synchronization;
import net.vulkanmod.vulkan.Vulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkBufferCopy.Buffer;

public class TransferQueue extends Queue {
   private static final VkDevice DEVICE = Vulkan.getVkDevice();

   public TransferQueue(MemoryStack stack, int familyIndex) {
      super(stack, familyIndex);
   }

   public long copyBufferCmd(long srcBuffer, long srcOffset, long dstBuffer, long dstOffset, long size) {
      long var14;
      try (MemoryStack stack = MemoryStack.stackPush()) {
         CommandPool.CommandBuffer commandBuffer = this.beginCommands();
         Buffer copyRegion = VkBufferCopy.calloc(1, stack);
         copyRegion.size(size);
         copyRegion.srcOffset(srcOffset);
         copyRegion.dstOffset(dstOffset);
         VK10.vkCmdCopyBuffer(commandBuffer.getHandle(), srcBuffer, dstBuffer, copyRegion);
         this.submitCommands(commandBuffer);
         Synchronization.INSTANCE.addCommandBuffer(commandBuffer);
         var14 = commandBuffer.fence;
      }

      return var14;
   }

   public void uploadBufferImmediate(long srcBuffer, long srcOffset, long dstBuffer, long dstOffset, long size) {
      try (MemoryStack stack = MemoryStack.stackPush()) {
         CommandPool.CommandBuffer commandBuffer = this.beginCommands();
         Buffer copyRegion = VkBufferCopy.calloc(1, stack);
         copyRegion.size(size);
         copyRegion.srcOffset(srcOffset);
         copyRegion.dstOffset(dstOffset);
         VK10.vkCmdCopyBuffer(commandBuffer.getHandle(), srcBuffer, dstBuffer, copyRegion);
         this.submitCommands(commandBuffer);
         VK10.vkWaitForFences(DEVICE, commandBuffer.fence, true, -1L);
         commandBuffer.reset();
      }
   }

   public static void uploadBufferCmd(VkCommandBuffer commandBuffer, long srcBuffer, long srcOffset, long dstBuffer, long dstOffset, long size) {
      try (MemoryStack stack = MemoryStack.stackPush()) {
         Buffer copyRegion = VkBufferCopy.calloc(1, stack);
         copyRegion.size(size);
         copyRegion.srcOffset(srcOffset);
         copyRegion.dstOffset(dstOffset);
         VK10.vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, copyRegion);
      }
   }
}
