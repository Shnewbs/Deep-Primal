// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.shader.descriptor;

import net.vulkanmod.vulkan.texture.VTextureSelector;
import net.vulkanmod.vulkan.texture.VulkanImage;

public class ImageDescriptor implements Descriptor {
  private final int descriptorType;
  private final int binding;
  public final String qualifier;
  public final String name;
  public final int imageIdx;
  public boolean useSampler;
  public boolean isReadOnlyLayout;
  private int layout;
  private int mipLevel = -1;
  private long storageBuffer, storageRange;
  private int storageOffset;

  public boolean isStorageBuffer() {
    return descriptorType == 7;
  }

  public long storageBuffer() {
    return storageBuffer;
  }

  public long storageRange() {
    return storageRange;
  }

  public int storageOffset() {
    return storageOffset;
  }

  public void setStorageBuffer(long buffer, long range, int offset) {
    storageBuffer = buffer;
    storageRange = range;
    storageOffset = offset;
  }

  public ImageDescriptor(int binding, String type, String name, int imageIdx, int descriptorType) {
    this.binding = binding;
    this.qualifier = type;
    this.name = name;
    this.imageIdx = imageIdx;
    if (this.imageIdx == -1) {
      throw new IllegalArgumentException();
    } else {
      this.descriptorType = descriptorType;
      boolean isStorageImage = this.isStorageImage();
      this.useSampler = !isStorageImage;
      this.setLayout(isStorageImage ? 1 : 5);
    }
  }

  @Override
  public int getBinding() {
    return this.binding;
  }

  @Override
  public int getType() {
    return this.descriptorType;
  }

  @Override
  public int getStages() {
    return 63;
  }

  public void setLayout(int layout) {
    this.layout = layout;
    this.isReadOnlyLayout = layout == 5;
  }

  public int getLayout() {
    return this.layout;
  }

  public void setMipLevel(int mipLevel) {
    this.mipLevel = mipLevel;
  }

  public int getMipLevel() {
    return this.mipLevel;
  }

  public VulkanImage getImage() {
    return VTextureSelector.getImage(this.imageIdx);
  }

  public long getImageView(VulkanImage image) {
    long view;
    if (this.mipLevel == -1) {
      view = image.getImageView();
    } else {
      view = image.getLevelImageView(this.mipLevel);
    }

    return view;
  }

  public boolean isStorageImage() {
    return this.descriptorType == 3;
  }

  public static class State {
    long imageView;
    long sampler;

    public State(long imageView, long sampler) {
      this.set(imageView, sampler);
    }

    public void set(long imageView, long sampler) {
      this.imageView = imageView;
      this.sampler = sampler;
    }

    public boolean isCurrentState(long imageView, long sampler) {
      return this.imageView == imageView && this.sampler == sampler;
    }
  }
}
