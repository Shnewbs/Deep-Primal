package com.shnewbs.deepprimal.validation;

import java.nio.file.*;
import java.util.concurrent.RejectedExecutionException;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Force a failed completion after rendering stops; the following completion must still run. */
@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class VulkanShutdownChecks {
  public VulkanShutdownChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void stopping(ClientStoppingEvent event) throws Exception {
    String destination = System.getenv("DEEPPRIMAL_SHUTDOWN_RECORD");
    if (destination == null) return;
    if (!com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName().equals("Vulkan"))
      throw new AssertionError("Shutdown fixture requires actual Vulkan");
    var type = Class.forName("net.vulkanmod.vulkan.memory.MemoryManager");
    Object memory = type.getMethod("getInstance").invoke(null);
    var add = type.getMethod("addFrameOp", Runnable.class);
    add.invoke(
        memory,
        (Runnable)
            () -> {
              throw new RejectedExecutionException(
                  "Native fixture: readback executor already stopped");
            });
    add.invoke(
        memory,
        (Runnable)
            () -> {
              try {
                Files.writeString(
                    Path.of(destination),
                    "VULKAN_SHUTDOWN_CONTINUATION_PASS rejectedCompletion=true"
                        + " followingCompletion=true\n");
              } catch (java.io.IOException failure) {
                throw new java.io.UncheckedIOException(failure);
              }
            });
    System.out.println("VULKAN_SHUTDOWN_FAILURE_QUEUED");
  }
}
