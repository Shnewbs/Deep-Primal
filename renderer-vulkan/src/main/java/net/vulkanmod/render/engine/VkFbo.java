// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.render.engine;

import net.vulkanmod.gl.VkGlFramebuffer;

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
  }

  protected void close() {
    VkGlFramebuffer.deleteFramebuffer(this.glId);
  }
}
