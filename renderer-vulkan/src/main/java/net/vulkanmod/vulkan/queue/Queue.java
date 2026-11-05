// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.queue;

import java.util.stream.IntStream;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

public abstract class Queue {
  private static VkDevice device;
  private static Queue.QueueFamilyIndices queueFamilyIndices;
  private final VkQueue vkQueue;
  protected CommandPool commandPool;

  public synchronized CommandPool.CommandBuffer beginCommands() {
    CommandPool.CommandBuffer var3;
    try (MemoryStack stack = MemoryStack.stackPush()) {
      CommandPool.CommandBuffer commandBuffer = this.commandPool.getCommandBuffer(stack);
      commandBuffer.begin(stack);
      var3 = commandBuffer;
    }

    return var3;
  }

  Queue(MemoryStack stack, int familyIndex) {
    this(stack, familyIndex, true);
  }

  Queue(MemoryStack stack, int familyIndex, boolean initCommandPool) {
    PointerBuffer pQueue = stack.mallocPointer(1);
    VK10.vkGetDeviceQueue(DeviceManager.vkDevice, familyIndex, 0, pQueue);
    this.vkQueue = new VkQueue(pQueue.get(0), DeviceManager.vkDevice);
    if (initCommandPool) {
      this.commandPool = new CommandPool(familyIndex);
    }
  }

  public long submitCommands(CommandPool.CommandBuffer commandBuffer) {
    return this.submitCommands(commandBuffer, false);
  }

  public synchronized long submitCommands(
      CommandPool.CommandBuffer commandBuffer, boolean useSemaphore) {
    long var4;
    try (MemoryStack stack = MemoryStack.stackPush()) {
      var4 = commandBuffer.submitCommands(stack, this.vkQueue, useSemaphore);
    }

    return var4;
  }

  public VkQueue vkQueue() {
    return this.vkQueue;
  }

  public void cleanUp() {
    if (this.commandPool != null) {
      this.commandPool.cleanUp();
    }
  }

  public void waitIdle() {
    VK10.vkQueueWaitIdle(this.vkQueue);
  }

  public CommandPool getCommandPool() {
    return this.commandPool;
  }

  public static Queue.QueueFamilyIndices getQueueFamilies() {
    if (device != Vulkan.getVkDevice()) {
      device = Vulkan.getVkDevice();
      queueFamilyIndices = null;
    }

    if (queueFamilyIndices == null) {
      queueFamilyIndices = findQueueFamilies(device.getPhysicalDevice());
    }

    return queueFamilyIndices;
  }

  public static QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device) {
    var indices = new QueueFamilyIndices();
    try (var stack = MemoryStack.stackPush()) {
      var count = stack.ints(0);
      VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, count, null);
      var families = VkQueueFamilyProperties.malloc(count.get(0), stack);
      VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, count, families);
      var present = stack.ints(0);
      for (int i = 0; i < families.capacity(); i++) {
        if (families.get(i).queueCount() == 0) continue;
        boolean graphics = (families.get(i).queueFlags() & 1) != 0;
        Vulkan.checkResult(
            KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(
                device, i, Vulkan.getSurface(), present),
            "Cannot query presentation queue");
        boolean presentation = present.get(0) != 0;
        if (graphics && presentation) {
          indices.graphicsFamily = i;
          indices.presentFamily = i;
          return indices;
        }
        if (graphics && indices.graphicsFamily < 0) indices.graphicsFamily = i;
        if (presentation && indices.presentFamily < 0) indices.presentFamily = i;
      }
    }
    return indices;
  }

  public static final class QueueFamilyIndices {
    public int graphicsFamily = -1, presentFamily = -1;

    public boolean isSuitable() {
      return graphicsFamily >= 0 && presentFamily >= 0;
    }

    public int[] unique() {
      return IntStream.of(graphicsFamily, presentFamily).distinct().toArray();
    }
  }
}
