// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.interfaces.shader;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;

public interface ExtendedRenderPipeline {
   static ExtendedRenderPipeline of(RenderPipeline renderPipeline) {
      return net.vulkanmod.render.engine.VkGpuDevice.current().binding(renderPipeline);
   }

   void setPipeline(GraphicsPipeline var1);

   GraphicsPipeline getPipeline();

}
