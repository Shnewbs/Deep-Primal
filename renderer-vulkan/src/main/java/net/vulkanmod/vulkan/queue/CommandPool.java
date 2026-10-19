// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.queue;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.LongBuffer;
import java.util.ArrayDeque;
import java.util.List;
import net.vulkanmod.vulkan.Vulkan;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;

public class CommandPool {
   long id;
   private final List<CommandPool.CommandBuffer> commandBuffers = new ObjectArrayList<>();
   private final java.util.Queue<CommandPool.CommandBuffer> availableCmdBuffers = new ArrayDeque<>();

   CommandPool(int queueFamilyIndex) {
      this.createCommandPool(queueFamilyIndex);
   }

   public void createCommandPool(int queueFamily) {
      try (MemoryStack stack = MemoryStack.stackPush()) {
         VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack);
         poolInfo.sType(39);
         poolInfo.queueFamilyIndex(queueFamily);
         poolInfo.flags(2);
         LongBuffer pCommandPool = stack.mallocLong(1);
         if (VK10.vkCreateCommandPool(Vulkan.getVkDevice(), poolInfo, null, pCommandPool) != 0) {
            throw new RuntimeException("Failed to create command pool");
         }

         this.id = pCommandPool.get(0);
      }
   }

   public CommandPool.CommandBuffer getCommandBuffer() {
      CommandPool.CommandBuffer var2;
      try (MemoryStack stack = MemoryStack.stackPush()) {
         var2 = this.getCommandBuffer(stack);
      }

      return var2;
   }

   public CommandPool.CommandBuffer getCommandBuffer(MemoryStack stack) {
      if (this.availableCmdBuffers.isEmpty()) {
         this.allocateCommandBuffers(stack);
      }

      return this.availableCmdBuffers.poll();
   }

   private void allocateCommandBuffers(MemoryStack stack) {
      int size = 10;
      VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
      allocInfo.sType$Default();
      allocInfo.level(0);
      allocInfo.commandPool(this.id);
      allocInfo.commandBufferCount(10);
      PointerBuffer pCommandBuffer = stack.mallocPointer(10);
      VK10.vkAllocateCommandBuffers(Vulkan.getVkDevice(), allocInfo, pCommandBuffer);
      VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
      fenceInfo.sType$Default();
      fenceInfo.flags(1);
      VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(stack);
      semaphoreCreateInfo.sType$Default();

      for (int i = 0; i < 10; i++) {
         LongBuffer pFence = stack.mallocLong(1);
         VK10.vkCreateFence(Vulkan.getVkDevice(), fenceInfo, null, pFence);
         LongBuffer pSemaphore = stack.mallocLong(1);
         VK10.vkCreateSemaphore(Vulkan.getVkDevice(), semaphoreCreateInfo, null, pSemaphore);
         VkCommandBuffer vkCommandBuffer = new VkCommandBuffer(pCommandBuffer.get(i), Vulkan.getVkDevice());
         CommandPool.CommandBuffer commandBuffer = new CommandPool.CommandBuffer(this, vkCommandBuffer, pFence.get(0), pSemaphore.get(0));
         this.commandBuffers.add(commandBuffer);
         this.availableCmdBuffers.add(commandBuffer);
      }
   }

   public void addToAvailable(CommandPool.CommandBuffer commandBuffer) {
      this.availableCmdBuffers.add(commandBuffer);
   }

   public void cleanUp() {
      for (CommandPool.CommandBuffer commandBuffer : this.commandBuffers) {
         VK10.vkDestroyFence(Vulkan.getVkDevice(), commandBuffer.fence, null);
         VK10.vkDestroySemaphore(Vulkan.getVkDevice(), commandBuffer.semaphore, null);
      }

      VK10.vkResetCommandPool(Vulkan.getVkDevice(), this.id, 1);
      VK10.vkDestroyCommandPool(Vulkan.getVkDevice(), this.id, null);
   }

   public long getId() {
      return this.id;
   }

   public static class CommandBuffer {
      public final CommandPool commandPool;
      public final VkCommandBuffer handle;
      public final long fence;
      public final long semaphore;
      boolean submitted;
      boolean recording;

      public CommandBuffer(CommandPool commandPool, VkCommandBuffer handle, long fence, long semaphore) {
         this.commandPool = commandPool;
         this.handle = handle;
         this.fence = fence;
         this.semaphore = semaphore;
      }

      public VkCommandBuffer getHandle() {
         return this.handle;
      }

      public long getFence() {
         return this.fence;
      }

      public long getSemaphore() {
         return this.semaphore;
      }

      public boolean isSubmitted() {
         return this.submitted;
      }

      public boolean isRecording() {
         return this.recording;
      }

      public void begin(MemoryStack stack) {
         VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
         beginInfo.sType(42);
         beginInfo.flags(1);
         VK10.vkBeginCommandBuffer(this.handle, beginInfo);
         this.recording = true;
      }

      public long submitCommands(MemoryStack stack, VkQueue queue, boolean useSemaphore) {
         long fence = this.fence;
         VK10.vkEndCommandBuffer(this.handle);
         VK10.vkResetFences(Vulkan.getVkDevice(), this.fence);
         VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
         submitInfo.sType(4);
         submitInfo.pCommandBuffers(stack.pointers(this.handle));
         if (useSemaphore) {
            submitInfo.pSignalSemaphores(stack.longs(this.semaphore));
         }

         VK10.vkQueueSubmit(queue, submitInfo, fence);
         this.recording = false;
         this.submitted = true;
         return fence;
      }

      public void reset() {
         this.submitted = false;
         this.recording = false;
         this.commandPool.addToAvailable(this);
      }
   }
}
