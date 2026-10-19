// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.render.engine;

import net.minecraft.util.ARGB;
import net.vulkanmod.gl.VkGlFramebuffer;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;

public class VkFbo {
   final int glId = net.vulkanmod.gl.VkGlFramebuffer.genFramebufferId();
   final VkTextureView colorAttachmentView;
   final VkGpuTexture depthAttachment;

   protected VkFbo(VkTextureView colorAttachmentView, VkGpuTexture depthAttachment) {
      this.colorAttachmentView = colorAttachmentView;
      this.depthAttachment = depthAttachment;
      VkGlFramebuffer fbo = VkGlFramebuffer.getFramebuffer(this.glId);
      VkGpuTexture colorAttachmentTexture = this.colorAttachmentView.texture();
      fbo.setAttachmentTexture(36064, colorAttachmentTexture.id);
      if (depthAttachment != null) {
         fbo.setAttachmentTexture(36096, depthAttachment.id);
      }

      fbo.setLevel(this.colorAttachmentView.baseMipLevel());
   }

   public void bind() {
      VkGlFramebuffer.bindFramebuffer(36160, this.glId);
      this.clearAttachments();
   }

   protected void clearAttachments() {
      int clear = 0;
      VkGpuTexture colorAttachmentTexture = this.colorAttachmentView.texture();
      if (colorAttachmentTexture.needsClear()) {
         clear |= 16384;
         int clearColor = colorAttachmentTexture.clearColor;
         VRenderSystem.setClearColor(ARGB.redFloat(clearColor), ARGB.greenFloat(clearColor), ARGB.blueFloat(clearColor), ARGB.alphaFloat(clearColor));
         colorAttachmentTexture.needsClear = false;
      }

      if (this.depthAttachment != null && this.depthAttachment.needsClear()) {
         clear |= 256;
         float clearDepth = this.depthAttachment.depthClearValue;
         VRenderSystem.clearDepth(clearDepth);
         this.depthAttachment.needsClear = false;
      }

      if (clear != 0) {
         Renderer.clearAttachments(clear);
      }
   }

   protected void close() {
      VkGlFramebuffer.deleteFramebuffer(this.glId);
   }

   public boolean needsClear() {
      return this.colorAttachmentView.texture().needsClear() || this.depthAttachment != null && this.depthAttachment.needsClear();
   }
}
