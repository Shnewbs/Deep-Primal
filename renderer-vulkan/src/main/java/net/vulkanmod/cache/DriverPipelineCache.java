// Deep Primal Vulkan cache implementation. LGPL-3.0-only.
package net.vulkanmod.cache;

import java.nio.*;
import java.util.Arrays;
import net.vulkanmod.Initializer;
import net.vulkanmod.vulkan.device.DeviceManager;
import org.lwjgl.system.*;
import org.lwjgl.vulkan.*;

/**
 * Driver identity and the native header must agree before reuse. Driver rejection retries empty.
 */
public final class DriverPipelineCache {
  private static String key() {
    var d = DeviceManager.deviceProperties;
    var identity = ByteBuffer.allocate(28).order(ByteOrder.LITTLE_ENDIAN);
    identity
        .putInt(d.vendorID())
        .putInt(d.deviceID())
        .putInt(d.driverVersion())
        .put(d.pipelineCacheUUID().duplicate());
    return BinaryCache.key(identity.array());
  }

  private static boolean compatible(byte[] bytes) {
    if (bytes.length < 32) return false;
    var h = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    var d = DeviceManager.deviceProperties;
    if (h.getInt() != 32
        || h.getInt() != 1
        || h.getInt() != d.vendorID()
        || h.getInt() != d.deviceID()) return false;
    byte[] a = new byte[16], b = new byte[16];
    h.get(a);
    d.pipelineCacheUUID().duplicate().get(b);
    return Arrays.equals(a, b);
  }

  public static long create() {
    byte[] bytes = null;
    try {
      bytes = BackendCaches.PIPELINES.read(key());
    } catch (java.io.IOException error) {
      Initializer.LOGGER.warn("Cannot read Vulkan pipeline cache; starting empty", error);
    }
    if (bytes != null && !compatible(bytes)) bytes = null;
    ByteBuffer initial = bytes == null ? null : MemoryUtil.memAlloc(bytes.length).put(bytes).flip();
    try (var stack = MemoryStack.stackPush()) {
      var info = VkPipelineCacheCreateInfo.calloc(stack).sType$Default().pInitialData(initial);
      var out = stack.mallocLong(1);
      int result = VK10.vkCreatePipelineCache(DeviceManager.vkDevice, info, null, out);
      if (result != 0 && initial != null) {
        Initializer.LOGGER.warn("Vulkan driver rejected cached data; retrying empty");
        info.pInitialData(null);
        result = VK10.vkCreatePipelineCache(DeviceManager.vkDevice, info, null, out);
        bytes = null;
      }
      if (result != 0) throw new IllegalStateException("Cannot create pipeline cache: " + result);
      Initializer.LOGGER.info(
          "VULKAN_PIPELINE_CACHE loadedBytes={}", bytes == null ? 0 : bytes.length);
      return out.get(0);
    } finally {
      if (initial != null) MemoryUtil.memFree(initial);
    }
  }

  public static void save(long cache) {
    ByteBuffer data = null;
    try (var stack = MemoryStack.stackPush()) {
      var size = stack.mallocPointer(1);
      if (VK10.vkGetPipelineCacheData(DeviceManager.vkDevice, cache, size, null) != 0) return;
      long length = size.get(0);
      if (length < 32 || length > 16 * 1024 * 1024) return;
      data = MemoryUtil.memAlloc((int) length);
      if (VK10.vkGetPipelineCacheData(DeviceManager.vkDevice, cache, size, data) != 0) return;
      byte[] bytes = new byte[(int) size.get(0)];
      data.get(bytes);
      if (compatible(bytes)) {
        BackendCaches.PIPELINES.write(key(), bytes);
        Initializer.LOGGER.info("VULKAN_PIPELINE_CACHE savedBytes={}", bytes.length);
      }
    } catch (java.io.IOException error) {
      Initializer.LOGGER.warn("Cannot save Vulkan pipeline cache", error);
    } finally {
      if (data != null) MemoryUtil.memFree(data);
    }
  }

  private DriverPipelineCache() {}
}
