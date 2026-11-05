// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.memory.buffer;

public class BufferSlice {
   Buffer buffer;
   long offset;
   int size;

   public void set(Buffer buffer, long offset, int size) {
      this.buffer = buffer;
      this.offset = offset;
      this.size = size;
   }

   public Buffer getBuffer() {
      return this.buffer;
   }

   public long getOffset() {
      return this.offset;
   }

   public int getSize() {
      return this.size;
   }
}
