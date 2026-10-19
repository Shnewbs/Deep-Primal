// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlShaderModule;
import com.mojang.blaze3d.opengl.VertexArrayCache.VertexArray;
import com.mojang.logging.LogUtils;
import java.util.Set;
import org.slf4j.Logger;
public class VkDebugLabel {
   private static final Logger LOGGER = LogUtils.getLogger();

   public void applyLabel(VkGpuBuffer glBuffer) {
   }

   public void applyLabel(VkGpuTexture glTexture) {
   }

   public void applyLabel(GlShaderModule glShaderModule) {
   }

   public void applyLabel(GlProgram glProgram) {
   }

   public void applyLabel(VertexArray vertexArray) {
   }

   public static VkDebugLabel create(boolean bl, Set<String> set) {
      return new VkDebugLabel();
   }

   public boolean exists() {
      return true;
   }
}
