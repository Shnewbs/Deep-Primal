// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.memory.buffer;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class StagingBuffers {
   final Stack<StagingBuffer> availableBuffers = new ObjectArrayList<>();
   ObjectArrayList<StagingBuffer>[] usedBuffersByFrame;
   StagingBuffer currentBuffer;
   boolean inFrame = false;

   public void updateFrameCount(int frames) {
      if (this.usedBuffersByFrame != null) {
         ObjectArrayList<StagingBuffer>[] i = this.usedBuffersByFrame;
         int var3 = i.length;

         for (int var4 = 0; var4 < var3; var4++) {
            for (StagingBuffer buffer : i[var4]) {
               this.availableBuffers.push(buffer);
            }
         }
      } else {
         for (int i = 0; i < frames + 1; i++) {
            this.availableBuffers.push(new StagingBuffer());
         }
      }

      this.currentBuffer = null;
      this.usedBuffersByFrame = new ObjectArrayList[frames];

      for (int i = 0; i < frames; i++) {
         this.usedBuffersByFrame[i] = new ObjectArrayList<>();
      }
   }

   public StagingBuffer getStagingBuffer() {
      if (this.currentBuffer == null) {
         if (this.availableBuffers.isEmpty()) {
            this.availableBuffers.push(new StagingBuffer());
         }

         this.currentBuffer = this.availableBuffers.pop();
      }

      if (!this.inFrame) {
         System.nanoTime();
      }

      return this.currentBuffer;
   }

   public void beginFrame(int frame) {
      ObjectArrayList<StagingBuffer> usedBuffers = this.usedBuffersByFrame[frame];

      for (StagingBuffer buffer : usedBuffers) {
         buffer.reset();
         this.availableBuffers.push(buffer);
      }

      usedBuffers.clear();
      if (this.currentBuffer != null) {
         usedBuffers.push(this.currentBuffer);
      }

      if (this.availableBuffers.isEmpty()) {
         this.availableBuffers.push(new StagingBuffer());
      }

      this.currentBuffer = this.availableBuffers.pop();
      this.inFrame = true;
   }

   public void endFrame(int frame) {
      this.usedBuffersByFrame[frame].push(this.currentBuffer);
      this.currentBuffer = null;
      this.inFrame = false;
   }

   public void free() {
      ObjectArrayList<StagingBuffer>[] var1 = this.usedBuffersByFrame;
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; var3++) {
         for (StagingBuffer buffer : var1[var3]) {
            buffer.scheduleFree();
         }
      }

      while (!this.availableBuffers.isEmpty()) {
         this.availableBuffers.pop().scheduleFree();
      }
   }
}
