// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.render.texture;

import java.util.HashSet;
import java.util.Set;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

public abstract class SpriteUpdateUtil {
   private static final Set<VulkanImage> transitionedLayouts = new HashSet<>();

   public static void addTransitionedLayout(VulkanImage image) {
      transitionedLayouts.add(image);
   }

   public static void transitionLayouts() {
      if (!transitionedLayouts.isEmpty()) {
         VkCommandBuffer commandBuffer = ImageUploadHelper.INSTANCE.getOrStartCommandBuffer().handle;
         transitionedLayouts.forEach(image -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
               image.readOnlyLayout(stack, commandBuffer);
            }
         });
         transitionedLayouts.clear();
      }
   }
}
