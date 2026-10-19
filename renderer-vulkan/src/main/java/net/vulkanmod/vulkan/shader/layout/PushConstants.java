// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.shader.layout;

import java.util.List;

public class PushConstants extends AlignedStruct {
   public final int stages;

   protected PushConstants(int stages, List<Uniform.Info> infoList, int size) {
      super(infoList, size);
      this.stages = stages;
   }
}
