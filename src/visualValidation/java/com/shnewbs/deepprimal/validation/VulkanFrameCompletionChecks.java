package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryUtil;

/** Deterministic completion at lazy frame startup, using the public encoder's mapping guard. */
final class VulkanFrameCompletionChecks {
  static void run(Minecraft mc, Path output) throws Exception {
    var device = RenderSystem.getDevice();
    if (!device.getBackendName().equals("Vulkan")) throw new AssertionError("Actual Vulkan required");
    var encoderType = Class.forName("net.vulkanmod.render.engine.VkCommandEncoder");
    System.out.println("VALIDATION_CLASS_ORIGIN class=" + encoderType.getName()
        + " location=" + encoderType.getProtectionDomain().getCodeSource().getLocation());
    var rendererType = Class.forName("net.vulkanmod.vulkan.Renderer");
    var managerType = Class.forName("net.vulkanmod.vulkan.memory.MemoryManager");
    Object manager = managerType.getMethod("getInstance").invoke(null);
    var operationsField = managerType.getDeclaredField("frameOps"); operationsField.setAccessible(true);
    var target = mc.getMainRenderTarget();
    int callbacks = 0, guarded = 0;
    for (int iteration = 0; iteration < 8; iteration++) {
      var data = MemoryUtil.memAlloc(16);
      for (int i = 0; i < 16; i++) data.put((byte) (i * 7 + 3));
      data.flip();
      var buffer = device.createBuffer(() -> "Frame completion regression",
          GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, data);
      MemoryUtil.memFree(data);
      var encoder = device.createCommandEncoder();
      boolean[] invoked = {false}, mapped = {false};
      try {
        try (var pass = encoder.createRenderPass(() -> "Completion setup", target.getColorTextureView(),
            OptionalInt.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {}
        device.presentFrame();
        int slot = (Integer) rendererType.getMethod("getCurrentFrame").invoke(null);
        Object[] queues = (Object[]) operationsField.get(manager);
        @SuppressWarnings("unchecked") var operations = (List<Runnable>) queues[slot];
        // This host-visible buffer needs no outstanding GPU work. Queue its completion in
        // the slot whose fence will be waited before lazy beginFrame drains callbacks.
        operations.add(() -> {
          invoked[0] = true;
          try (var view = encoder.mapBuffer(buffer, true, false)) {
            for (int i = 0; i < 16; i++)
              if (view.data().get(i) != (byte) (i * 7 + 3)) throw new AssertionError("Mapped bytes");
            mapped[0] = true;
          } finally {
            System.out.println("VULKAN_FRAME_COMPLETION_OBSERVED invoked=" + invoked[0]
                + " mapped=" + mapped[0]);
          }
        });
        try (var pass = encoder.createRenderPass(() -> "Completion boundary", target.getColorTextureView(),
            OptionalInt.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
          if (!invoked[0] || !mapped[0]) throw new AssertionError("Completion not drained before pass");
          boolean rejected = false;
          try (var unexpected = encoder.mapBuffer(buffer, true, false)) {}
          catch (IllegalStateException expected) { rejected = true; }
          if (!rejected) throw new AssertionError("Mapping inside an open pass must remain rejected");
          guarded++;
        }
        callbacks++;
      } finally { buffer.close(); }
    }
    String record = "VULKAN_FRAME_COMPLETION_NATIVE_PASS iterations=" + callbacks
        + " mappedBytes=128 openPassRejections=" + guarded + " backend=Vulkan\n";
    Files.writeString(output.resolve("frame-completion.txt"), record);
    System.out.print(record);
  }

  private VulkanFrameCompletionChecks() {}
}
