// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan.memory;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.LongBuffer;
import java.util.List;
import java.util.function.Consumer;
import net.vulkanmod.Initializer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import net.vulkanmod.vulkan.texture.VulkanImage;
import net.vulkanmod.vulkan.util.VkResult;
import org.apache.commons.lang3.Validate;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaBudget;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;

public class MemoryManager {
  private static final boolean DEBUG = false;
  public static final long BYTES_IN_MB = 1048576L;
  private static MemoryManager INSTANCE;
  private static final long ALLOCATOR = Vulkan.getAllocator();
  private static final Long2ReferenceOpenHashMap<Buffer.BufferInfo> buffers =
      new Long2ReferenceOpenHashMap<>();
  private static final Long2ReferenceOpenHashMap<VulkanImage> images =
      new Long2ReferenceOpenHashMap<>();
  static int Frames;
  private static long deviceMemory = 0L;
  private static long nativeMemory = 0L;
  private int currentFrame = 0;
  private final ObjectArrayList<Buffer.BufferInfo>[] freeableBuffers;
  private final ObjectArrayList<VulkanImage>[] freeableImages;
  private final ObjectArrayList<Runnable>[] frameOps;
  private ObjectArrayList<StackTraceElement[]>[] stackTraces;

  public static MemoryManager getInstance() {
    return INSTANCE;
  }

  public static void createInstance(int frames) {
    Frames = frames;
    INSTANCE = new MemoryManager();
  }

  MemoryManager() {
    this.freeableBuffers = new ObjectArrayList[Frames];
    this.freeableImages = new ObjectArrayList[Frames];
    this.frameOps = new ObjectArrayList[Frames];

    for (int i = 0; i < Frames; i++) {
      this.freeableBuffers[i] = new ObjectArrayList<>();
      this.freeableImages[i] = new ObjectArrayList<>();
      this.frameOps[i] = new ObjectArrayList<>();
    }
  }

  public synchronized void initFrame(int frame) {
    this.setCurrentFrame(frame);
    this.freeBuffers(frame);
    this.freeImages(frame);
    this.doFrameOps(frame);
  }

  public void setCurrentFrame(int frame) {
    Validate.isTrue(frame < Frames, "Out of bounds frame index", new Object[0]);
    this.currentFrame = frame;
  }

  public void freeAllBuffers() {
    for (int frame = 0; frame < Frames; frame++) doFrameOps(frame);
    for (int frame = 0; frame < Frames; frame++) {
      freeBuffers(frame);
      freeImages(frame);
    }
    for (VulkanImage image : java.util.List.copyOf(images.values())) image.doFree();
    for (Buffer.BufferInfo buffer : java.util.List.copyOf(buffers.values())) freeBuffer(buffer);
    Initializer.LOGGER.info(
        "VULKAN_RESOURCES_RELEASED buffers={} images={}", buffers.size(), images.size());
  }

  public void createBuffer(
      long size, int usage, int properties, LongBuffer pBuffer, PointerBuffer pBufferMemory) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc(stack);
      bufferInfo.sType(12);
      bufferInfo.size(size);
      bufferInfo.usage(usage);
      VmaAllocationCreateInfo allocationInfo = VmaAllocationCreateInfo.calloc(stack);
      allocationInfo.requiredFlags(properties);
      int result =
          Vma.vmaCreateBuffer(ALLOCATOR, bufferInfo, allocationInfo, pBuffer, pBufferMemory, null);
      if (result != 0) {
        Initializer.LOGGER.info(
            String.format("Failed to create buffer with size: %.3f MB", (float) size / 1048576.0F));
        Initializer.LOGGER.info(
            String.format(
                "Tracked Device Memory used: %d/%d MB",
                this.getAllocatedDeviceMemoryMB(), this.getDeviceMemoryMB()));
        Initializer.LOGGER.info(this.getHeapStats());
        throw new RuntimeException(
            "Failed to create buffer: %s".formatted(VkResult.decode(result)));
      }
    }
  }

  public synchronized void createBuffer(Buffer buffer, long size, int usage, int properties) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      LongBuffer pBuffer = stack.mallocLong(1);
      PointerBuffer pAllocation = stack.pointers(0L);
      this.createBuffer(size, usage, properties, pBuffer, pAllocation);
      buffer.setId(pBuffer.get(0));
      buffer.setAllocation(pAllocation.get(0));
      buffer.setBufferSize(size);
      if ((properties & 1) != 0) {
        deviceMemory += size;
      } else {
        nativeMemory += size;
      }

      buffers.putIfAbsent(buffer.getId(), buffer.getBufferInfo());
    }
  }

  public void createImage(
      int width,
      int height,
      int arrayLayers,
      int mipLevels,
      int format,
      int tiling,
      int usage,
      int flags,
      int memProperties,
      LongBuffer pTextureImage,
      PointerBuffer pTextureImageMemory) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc(stack);
      imageInfo.sType(14);
      imageInfo.imageType(1);
      imageInfo.extent().width(width);
      imageInfo.extent().height(height);
      imageInfo.extent().depth(1);
      imageInfo.mipLevels(mipLevels);
      imageInfo.arrayLayers(arrayLayers);
      imageInfo.format(format);
      imageInfo.tiling(tiling);
      imageInfo.initialLayout(0);
      imageInfo.usage(usage);
      imageInfo.samples(1);
      imageInfo.flags(flags);
      net.vulkanmod.vulkan.device.ImageCapabilities.require(
          format,
          imageInfo.imageType(),
          tiling,
          usage,
          flags,
          width,
          height,
          arrayLayers,
          mipLevels);
      VmaAllocationCreateInfo allocationInfo = VmaAllocationCreateInfo.calloc(stack);
      allocationInfo.requiredFlags(memProperties);
      int result =
          Vma.vmaCreateImage(
              ALLOCATOR, imageInfo, allocationInfo, pTextureImage, pTextureImageMemory, null);
      if (result != 0) {
        Initializer.LOGGER.info(
            String.format("Failed to create image with size: %dx%d", width, height));
        throw new RuntimeException("Failed to create image: %s".formatted(VkResult.decode(result)));
      }
    }
  }

  public static void addImage(VulkanImage image) {
    images.putIfAbsent(image.getId(), image);
    deviceMemory = deviceMemory + image.size;
  }

  public static void MapAndCopy(long allocation, Consumer<PointerBuffer> consumer) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      PointerBuffer data = stack.mallocPointer(1);
      Vma.vmaMapMemory(ALLOCATOR, allocation, data);
      consumer.accept(data);
      Vma.vmaUnmapMemory(ALLOCATOR, allocation);
    }
  }

  public long map(long allocation) {
    try (var stack = MemoryStack.stackPush()) {
      var data = stack.mallocPointer(1);
      int result = Vma.vmaMapMemory(ALLOCATOR, allocation, data);
      if (result != 0) throw new IllegalStateException("Cannot map allocation: " + result);
      return data.get(0);
    }
  }

  public static void freeBuffer(long buffer, long allocation) {
    Vma.vmaDestroyBuffer(ALLOCATOR, buffer, allocation);
    buffers.remove(buffer);
  }

  private static void freeBuffer(Buffer.BufferInfo bufferInfo) {
    if (buffers.remove(bufferInfo.id()) == null) return;
    Vma.vmaDestroyBuffer(ALLOCATOR, bufferInfo.id(), bufferInfo.allocation());
    if (bufferInfo.type() == MemoryType.Type.DEVICE_LOCAL) {
      deviceMemory = deviceMemory - bufferInfo.bufferSize();
    } else {
      nativeMemory = nativeMemory - bufferInfo.bufferSize();
    }

    buffers.remove(bufferInfo.id());
  }

  public static void freeImage(long imageId, long allocation) {
    Vma.vmaDestroyImage(ALLOCATOR, imageId, allocation);
    VulkanImage image = images.remove(imageId);
    deviceMemory = deviceMemory - image.size;
  }

  public synchronized void addToFreeable(Buffer buffer) {
    Buffer.BufferInfo bufferInfo = buffer.getBufferInfo();
    this.checkBuffer(bufferInfo);
    this.freeableBuffers[this.currentFrame].add(bufferInfo);
  }

  public synchronized void addToFreeable(VulkanImage image) {
    this.freeableImages[this.currentFrame].add(image);
  }

  public synchronized void addFrameOp(Runnable runnable) {
    this.frameOps[this.currentFrame].add(runnable);
  }

  public void doFrameOps(int frame) {
    for (Runnable runnable : this.frameOps[frame]) {
      runnable.run();
    }

    this.frameOps[frame].clear();
  }

  private void freeBuffers(int frame) {
    List<Buffer.BufferInfo> bufferList = this.freeableBuffers[frame];

    for (Buffer.BufferInfo bufferInfo : bufferList) {
      freeBuffer(bufferInfo);
    }

    bufferList.clear();
  }

  private void freeImages(int frame) {
    List<VulkanImage> bufferList = this.freeableImages[frame];

    for (VulkanImage image : bufferList) {
      image.doFree();
    }

    bufferList.clear();
  }

  private void checkBuffer(Buffer.BufferInfo bufferInfo) {
    if (buffers.get(bufferInfo.id()) == null) {
      throw new RuntimeException("trying to free not present buffer");
    }
  }

  public int getNativeMemoryMB() {
    return this.bytesInMb(nativeMemory);
  }

  public int getAllocatedDeviceMemoryMB() {
    return this.bytesInMb(deviceMemory);
  }

  public int getDeviceMemoryMB() {
    return this.bytesInMb(MemoryTypes.GPU_MEM.vkMemoryHeap.size());
  }

  int bytesInMb(long bytes) {
    return (int) (bytes / 1048576L);
  }

  public String getHeapStats() {
    String var8;
    try (MemoryStack stack = MemoryStack.stackPush()) {
      org.lwjgl.util.vma.VmaBudget.Buffer vmaBudgets =
          VmaBudget.calloc(DeviceManager.memoryProperties.memoryHeapCount(), stack);
      Vma.vmaGetHeapBudgets(ALLOCATOR, vmaBudgets);
      VmaBudget vmaBudget = vmaBudgets.get(MemoryTypes.GPU_MEM.vkMemoryType.heapIndex());
      long usage = vmaBudget.usage();
      long budget = vmaBudget.budget();
      var8 =
          String.format(
              "Device Memory Heap Usage: %d/%dMB", this.bytesInMb(usage), this.bytesInMb(budget));
    }

    return var8;
  }
}
