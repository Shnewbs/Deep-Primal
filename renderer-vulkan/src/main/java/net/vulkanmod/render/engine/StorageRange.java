package net.vulkanmod.render.engine;

/** Vulkan reports maxStorageBufferRange as uint32, even when its Java int is negative. */
public final class StorageRange {
  public static boolean accepts(long bytes, int deviceLimit) {
    return bytes >= 0 && bytes <= Integer.toUnsignedLong(deviceLimit);
  }
  private StorageRange() {}
}
