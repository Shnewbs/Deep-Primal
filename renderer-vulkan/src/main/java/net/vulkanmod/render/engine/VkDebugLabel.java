// Deep Primal Vulkan debug utilities. LGPL-3.0-only.
package net.vulkanmod.render.engine;

import java.util.function.Supplier;
import net.vulkanmod.vulkan.Vulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

/** Optional native names/groups. Disabled labels do not evaluate suppliers. */
public final class VkDebugLabel {
  private final boolean enabled;

  private VkDebugLabel(boolean enabled) {
    this.enabled = enabled;
  }

  public static VkDebugLabel create(boolean requested) {
    return new VkDebugLabel(requested && Vulkan.debugUtilsEnabled());
  }

  public boolean exists() {
    return enabled;
  }

  public void applyLabel(VkGpuBuffer buffer) {
    if (!enabled || buffer.label == null) return;
    try (var stack = MemoryStack.stackPush()) {
      Vulkan.setDebugLabel(stack, 9, buffer.getBuffer().getId(), buffer.label.get());
    }
  }

  public void applyLabel(VkGpuTexture texture) {
    if (!enabled) return;
    try (var stack = MemoryStack.stackPush()) {
      Vulkan.setDebugLabel(stack, 10, texture.getVulkanImage().getId(), texture.getLabel());
    }
  }

  public void push(VkCommandBuffer command, Supplier<String> label) {
    if (!enabled) return;
    try (var stack = MemoryStack.stackPush()) {
      var info =
          VkDebugUtilsLabelEXT.calloc(stack).sType$Default().pLabelName(stack.UTF8(label.get()));
      EXTDebugUtils.vkCmdBeginDebugUtilsLabelEXT(command, info);
    }
  }

  public void pop(VkCommandBuffer command) {
    if (enabled) EXTDebugUtils.vkCmdEndDebugUtilsLabelEXT(command);
  }
}
