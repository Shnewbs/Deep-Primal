// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.shader.descriptor;

import org.lwjgl.system.MemoryUtil;

public class ManualUBO extends UBO {
   private long srcPtr;
   private int srcSize;
   private boolean update = true;

   public ManualUBO(int binding, int type, int size) {
      super("manual UBO: %d".formatted(binding), binding, type, size * 4, null);
   }

   @Override
   public void update(long ptr) {
      if (this.update) {
         MemoryUtil.memCopy(this.srcPtr, ptr, this.srcSize);
      }
   }

   public void setSrc(long ptr, int size) {
      this.srcPtr = ptr;
      this.srcSize = size;
   }

   @Override
   public void setUpdate(boolean update) {
      this.update = update;
   }
}
