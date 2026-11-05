// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import java.util.OptionalDouble;
import net.vulkanmod.vulkan.texture.SamplerManager;
public class VkSampler extends GpuSampler {
   private final AddressMode addressModeU;
   private final AddressMode addressModeV;
   private final FilterMode minFilter;
   private final FilterMode magFilter;
   private final int maxAnisotropy;
   private final float maxLod;
   private boolean closed;
   private final long id;

   public VkSampler(AddressMode addressModeU, AddressMode addressModeV, FilterMode minFilter, FilterMode magFilter, int maxAnisotropy, OptionalDouble maxLod) {
      this.addressModeU = addressModeU;
      this.addressModeV = addressModeV;
      this.minFilter = minFilter;
      this.magFilter = magFilter;
      var device = net.vulkanmod.vulkan.device.DeviceManager.device;
      int limit = device.availableFeatures.features().samplerAnisotropy()
         ? (int)net.vulkanmod.vulkan.device.DeviceManager.deviceProperties.limits().maxSamplerAnisotropy() : 1;
      this.maxAnisotropy = Math.clamp(maxAnisotropy, 1, Math.max(1, limit));
      this.maxLod = maxLod.isPresent() ? (float)maxLod.getAsDouble() : 1000.0F;
      this.id = SamplerManager.getSampler(
         VkConst.of(addressModeU), VkConst.of(addressModeV), VkConst.of(minFilter), VkConst.of(magFilter), 1, this.maxLod, this.maxAnisotropy > 1, this.maxAnisotropy, -1
      );
   }

   public long getId() {
      return this.id;
   }

   @Override
   public AddressMode getAddressModeU() {
      return this.addressModeU;
   }

   @Override
   public AddressMode getAddressModeV() {
      return this.addressModeV;
   }

   @Override
   public FilterMode getMinFilter() {
      return this.minFilter;
   }

   @Override
   public FilterMode getMagFilter() {
      return this.magFilter;
   }

   @Override
   public int getMaxAnisotropy() {
      return this.maxAnisotropy;
   }

   @Override
   public OptionalDouble getMaxLod() {
      return maxLod == 1000.0F ? OptionalDouble.empty() : OptionalDouble.of(maxLod);
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
      }
   }

   public boolean isClosed() {
      return this.closed;
   }
}
