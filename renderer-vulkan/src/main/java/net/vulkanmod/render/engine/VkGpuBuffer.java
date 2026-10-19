// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.buffers.GpuBuffer;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.MemoryType;
import net.vulkanmod.vulkan.memory.MemoryTypes;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import org.jetbrains.annotations.Nullable;
public class VkGpuBuffer extends GpuBuffer {
   protected boolean closed;
   @Nullable
   protected final Supplier<String> label;
   Buffer buffer;
   int offset;
   private record ViewKey(int format, long offset, long length) {}
   private final java.util.LinkedHashMap<ViewKey, Long> views = new java.util.LinkedHashMap<>(16, .75f, true);
   public long texelView(int format, long offset, long length) {
      var key = new ViewKey(format, offset, length); var cached = views.get(key); if (cached != null) return cached;
      try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
         var info = org.lwjgl.vulkan.VkBufferViewCreateInfo.calloc(stack).sType$Default()
            .buffer(buffer.getId()).format(format).offset(offset).range(length);
         var result = stack.mallocLong(1);
         int status = org.lwjgl.vulkan.VK10.vkCreateBufferView(net.vulkanmod.vulkan.device.DeviceManager.vkDevice, info, null, result);
         if (status != 0) throw new IllegalStateException("Cannot create texel buffer view: " + status);
         long view = result.get(0); views.put(key, view);
         if (views.size() > 128) { var oldest = views.pollFirstEntry(); retireView(oldest.getValue()); }
         return view;
      }
   }
   private static void retireView(long view) {
      MemoryManager.getInstance().addFrameOp(() -> org.lwjgl.vulkan.VK10.vkDestroyBufferView(net.vulkanmod.vulkan.device.DeviceManager.vkDevice, view, null));
   }


   protected VkGpuBuffer(VkDebugLabel debugLabel, @Nullable Supplier<String> supplier, int usage, long size) {
      super(usage, size);
      this.label = supplier;
      int vkUsage = 0;
      if ((usage & 16) != 0) {
         vkUsage |= 1;
      }

      if ((usage & 8) != 0) {
         vkUsage |= 2;
      }

      if ((usage & 32) != 0) {
         vkUsage |= 128;
      }

      if ((usage & 64) != 0) {
         vkUsage |= 64;
      }

      if ((usage & 128) != 0) {
         vkUsage |= 16;
      }

      if ((usage & 256) != 0) {
         vkUsage |= 4;
      }

      boolean mappable = (usage & 1) != 0 | (usage & 2) != 0 | (usage & 4) != 0;
      MemoryType memoryType = mappable ? MemoryTypes.HOST_MEM : MemoryTypes.GPU_MEM;
      this.buffer = new Buffer(supplier == null ? "Deep Primal buffer" : supplier.get(), vkUsage | 3, memoryType);
      this.buffer.createBuffer(this.size());
   }

   protected VkGpuBuffer(Buffer buffer) {
      super(buffer.usage, buffer.getBufferSize());
      this.label = null;
      this.buffer = buffer;
   }

   @Override
   public boolean isClosed() {
      return this.closed;
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         if (views.isEmpty()) MemoryManager.getInstance().addToFreeable(this.buffer);
         else {
            var retired = java.util.List.copyOf(views.values()); views.clear();
            MemoryManager.getInstance().addFrameOp(() -> {
               for (long view : retired) org.lwjgl.vulkan.VK10.vkDestroyBufferView(net.vulkanmod.vulkan.device.DeviceManager.vkDevice, view, null);
               MemoryManager.getInstance().addToFreeable(this.buffer);
            });
         }
      }
   }

   public Buffer getBuffer() {
      return this.buffer;
   }

   public void setOffset(int offset) {
      this.offset = offset;
   }

   public static int bufferUsageToGlEnum(int i) {
      boolean stream = (i & 4) != 0;
      if ((i & 2) != 0) {
         return stream ? 35040 : 35044;
      } else if ((i & 1) != 0) {
         return stream ? 35041 : 35045;
      } else {
         return 35044;
      }
   }

   public static VkGpuBuffer from(Buffer buffer) {
      return new VkGpuBuffer(buffer);
   }
public static class MappedView implements com.mojang.blaze3d.buffers.GpuBuffer.MappedView {
      private final int target;
      private final ByteBuffer data;

      protected MappedView(int i, ByteBuffer byteBuffer) {
         this.target = i;
         this.data = byteBuffer;
      }

      @Override
      public ByteBuffer data() {
         return this.data;
      }

      @Override
      public void close() {
      }
   }
}
