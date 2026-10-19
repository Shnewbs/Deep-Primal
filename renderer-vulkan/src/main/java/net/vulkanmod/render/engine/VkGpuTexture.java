// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.render.engine;

import net.vulkanmod.gl.VkState;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.vulkanmod.gl.VkGlTexture;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.jetbrains.annotations.Nullable;
public class VkGpuTexture extends GlTexture {
   protected VkGlTexture glTexture;
   protected final int id;
   private final Int2ReferenceMap<VkFbo> fboCache = new Int2ReferenceOpenHashMap<>();
   protected boolean closed;
   VkTextureView fboView;
   boolean needsClear = false;
   int clearColor = 0;
   float depthClearValue = 1.0F;

   protected VkGpuTexture(int usage, String string, TextureFormat textureFormat, int width, int height, int layers, int mipLevel, int id, VkGlTexture glTexture) {
      super(usage, string, textureFormat, width, height, layers, mipLevel, id);
      this.id = id;
      this.glTexture = glTexture;
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         for (VkFbo fbo : fboCache.values()) fbo.close();
         fboCache.clear();
         if (fboView != null) { fboView.close(); fboView = null; }
         VkState._deleteTexture(this.id);
      }
   }

   @Override
   public boolean isClosed() {
      return this.closed;
   }

   @Override
   public int glId() {
      return this.id;
   }

   public void setClearColor(int clearColor) {
      this.needsClear = true;
      this.clearColor = clearColor;
   }

   public void setDepthClearValue(float depthClearValue) {
      this.needsClear = true;
      this.depthClearValue = depthClearValue;
   }

   public boolean needsClear() {
      return this.needsClear;
   }

   public VkFbo getFbo(@Nullable GpuTexture depthAttachment) {
      int depthAttachmentId = depthAttachment == null ? 0 : ((VkGpuTexture)depthAttachment).id;
      if (this.fboView == null) {
         VkGpuDevice gpuDevice = VkGpuDevice.current();
         this.fboView = (VkTextureView)gpuDevice.createTextureView(this, 0, this.getMipLevels());
      }

      return (VkFbo)this.fboCache.computeIfAbsent(depthAttachmentId, j -> new VkFbo(this.fboView, (VkGpuTexture)depthAttachment));
   }

   public VulkanImage getVulkanImage() {
      return this.glTexture.getVulkanImage();
   }

   public static TextureFormat textureFormat(int format) {
      return switch (format) {
         case 9 -> TextureFormat.RED8;
         case 37, 44 -> TextureFormat.RGBA8;
         case 125, 126 -> TextureFormat.DEPTH32;
         case 129 -> TextureFormat.DEPTH24_STENCIL8;
         case 130 -> TextureFormat.DEPTH32_STENCIL8;
         default -> null;
      };
   }

   public static int vkFormat(TextureFormat textureFormat) {
      return switch (textureFormat) {
         case RGBA8 -> 37;
         case RED8 -> 9;
         case RED8I -> 14;
         case DEPTH32 -> 126;
         case DEPTH24_STENCIL8 -> 129;
         case DEPTH32_STENCIL8 -> 130;
         default -> throw new MatchException(null, null);
      };
   }

   public static int vkImageViewType(int usage) {
      int viewType;
      if ((usage & 16) != 0) {
         viewType = 3;
      } else {
         viewType = 1;
      }

      return viewType;
   }
}
