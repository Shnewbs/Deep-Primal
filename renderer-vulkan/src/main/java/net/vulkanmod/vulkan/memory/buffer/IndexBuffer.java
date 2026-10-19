// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.memory.buffer;

import net.vulkanmod.vulkan.memory.MemoryType;

public class IndexBuffer extends Buffer {
   public IndexBuffer.IndexType indexType;

   public IndexBuffer(int size, MemoryType type) {
      this(size, type, IndexBuffer.IndexType.UINT16);
   }

   public IndexBuffer(int size, MemoryType type, IndexBuffer.IndexType indexType) {
      super("Index buffer", 64, type);
      this.indexType = indexType;
      this.createBuffer(size);
   }

   public static enum IndexType {
      UINT16(2, 0),
      UINT32(4, 1);

      public final int size;
      public final int value;

      private IndexType(int size, int value) {
         this.size = size;
         this.value = value;
      }
   }
}
