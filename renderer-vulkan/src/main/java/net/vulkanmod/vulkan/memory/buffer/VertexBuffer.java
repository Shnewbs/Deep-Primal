// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.memory.buffer;

import net.vulkanmod.vulkan.memory.MemoryType;
import net.vulkanmod.vulkan.memory.MemoryTypes;

public class VertexBuffer extends Buffer {
   public VertexBuffer(int size) {
      this(size, MemoryTypes.HOST_MEM);
   }

   public VertexBuffer(int size, MemoryType type) {
      super("Vertex buffer", 128, type);
      this.createBuffer(size);
   }
}
