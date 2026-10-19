// Deep Primal Vulkan capability cache. LGPL-3.0-only.
package net.vulkanmod.vulkan.device;

import java.util.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

/**
 * Immutable per-device image capabilities; repeated texture creation does not re-query the driver.
 */
public final class ImageCapabilities {
  private record Key(int format, int type, int tiling, int usage, int flags) {}

  private record Limits(int width, int height, int mips, int layers) {}

  private static final Map<Key, Limits> FORMATS = new HashMap<>();

  private static Limits query(Key k) {
    try (var s = MemoryStack.stackPush()) {
      var p = VkImageFormatProperties.calloc(s);
      int result =
          VK10.vkGetPhysicalDeviceImageFormatProperties(
              DeviceManager.physicalDevice, k.format, k.type, k.tiling, k.usage, k.flags, p);
      if (result == VK10.VK_ERROR_FORMAT_NOT_SUPPORTED) return new Limits(0, 0, 0, 0);
      if (result != 0)
        throw new IllegalStateException("Cannot query image capabilities: " + result);
      return new Limits(
          p.maxExtent().width(), p.maxExtent().height(), p.maxMipLevels(), p.maxArrayLayers());
    }
  }

  public static boolean supports(int format, int usage) {
    return FORMATS.computeIfAbsent(new Key(format, 1, 0, usage, 0), ImageCapabilities::query).mips
        > 0;
  }

  public static void require(
      int format,
      int type,
      int tiling,
      int usage,
      int flags,
      int width,
      int height,
      int layers,
      int mips) {
    var p =
        FORMATS.computeIfAbsent(
            new Key(format, type, tiling, usage, flags), ImageCapabilities::query);
    if (width < 1
        || height < 1
        || layers < 1
        || mips < 1
        || width > p.width
        || height > p.height
        || layers > p.layers
        || mips > p.mips
        || mips > 32 - Integer.numberOfLeadingZeros(Math.max(width, height)))
      throw new UnsupportedOperationException(
          "Image format/usage/dimensions unsupported: format="
              + format
              + " usage="
              + usage
              + " size="
              + width
              + "x"
              + height);
  }

  static void clear() {
    FORMATS.clear();
  }

  private ImageCapabilities() {}
}
