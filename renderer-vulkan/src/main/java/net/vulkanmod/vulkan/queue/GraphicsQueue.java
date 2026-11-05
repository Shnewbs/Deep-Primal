// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.queue;

import net.vulkanmod.vulkan.Synchronization;
import org.lwjgl.system.MemoryStack;

public class GraphicsQueue extends Queue {
   public static GraphicsQueue INSTANCE;
   private static CommandPool.CommandBuffer currentCmdBuffer;

   public GraphicsQueue(MemoryStack stack, int familyIndex) {
      super(stack, familyIndex);
   }

   public void startRecording() {
      currentCmdBuffer = this.beginCommands();
   }

   public void endRecordingAndSubmit() {
      long fence = this.submitCommands(currentCmdBuffer);
      Synchronization.INSTANCE.addCommandBuffer(currentCmdBuffer);
      currentCmdBuffer = null;
   }

   public CommandPool.CommandBuffer getCommandBuffer() {
      return currentCmdBuffer != null ? currentCmdBuffer : this.beginCommands();
   }

   public long endIfNeeded(CommandPool.CommandBuffer commandBuffer) {
      return currentCmdBuffer != null ? 0L : this.submitCommands(commandBuffer);
   }
}
