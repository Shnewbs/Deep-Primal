// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;

public class VkConst {
   public static int of(AddressMode addressMode) {
      return switch (addressMode) {
         case REPEAT -> 0;
         case CLAMP_TO_EDGE -> 2;
      };
   }

   public static int of(FilterMode filterMode) {
      return switch (filterMode) {
         case NEAREST -> 0;
         case LINEAR -> 1;
      };
   }
}
