package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.settings.RendererPreferences;
import java.nio.file.*;
import net.minecraft.client.Minecraft;

/** Read the startup owner's actual state in the game process, independently of host file checks. */
final class StartupLifecycleChecks {
  static void observe(String label, Path output) throws Exception {
    var type = Class.forName("net.vulkanmod.deepprimal.Startup");
    var marker = RendererPreferences.recoveryMarker();
    var markerField = type.getDeclaredField("MARKER"); markerField.setAccessible(true);
    Path ownerMarker = (Path) markerField.get(null);
    var armed = type.getDeclaredField("armed"); armed.setAccessible(true);
    var complete = type.getDeclaredField("complete"); complete.setAccessible(true);
    String record = "STARTUP_LIFECYCLE_OBSERVATION label=" + label
        + " backend=" + RenderSystem.getDevice().getBackendName()
        + " markerPresent=" + Files.exists(marker)
        + " armed=" + armed.getBoolean(null) + " complete=" + complete.getBoolean(null)
        + " marker=" + marker.toAbsolutePath().normalize()
        + " ownerMarker=" + ownerMarker.toAbsolutePath().normalize()
        + " ownerMarkerPresent=" + Files.exists(ownerMarker)
        + " owner=" + type.getProtectionDomain().getCodeSource().getLocation() + "\n";
    Files.createDirectories(output);
    Files.writeString(output.resolve("startup-lifecycle.txt"), record,
        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    System.out.print(record);
  }

  static void rearm(Minecraft mc, Path output) throws Exception {
    var type = Class.forName("net.vulkanmod.deepprimal.Startup");
    if (!RenderSystem.getDevice().getBackendName().equals("Vulkan") || !mc.isGameLoadFinished())
      throw new AssertionError("Loaded Vulkan session required");
    observe("before-rearm", output);
    for (int turn = 0; turn < 32; turn++) {
      type.getMethod("arm").invoke(null);
      if (turn == 0) observe("armed-again", output);
      var complete = type.getDeclaredField("complete"); complete.setAccessible(true);
      if (!Files.exists(RendererPreferences.recoveryMarker()) || complete.getBoolean(null))
        throw new AssertionError("New arm must retain marker and reset completion");
      type.getMethod("firstFrame").invoke(null);
      if (Files.exists(RendererPreferences.recoveryMarker()))
        throw new AssertionError("Successful first frame after rearm must clear recovery marker");
    }
    observe("rearm-first-frame", output);
    String record = "STARTUP_REARM_NATIVE_PASS backend=Vulkan cycles=32 markerCleared=true\n";
    Files.writeString(output.resolve("startup-rearm.txt"), record); System.out.print(record);
  }
  static void watchGpuCheck(Path output) {
    Thread watcher = new Thread(() -> {
      try {
        Thread.sleep(20_000);
        for (var entry : Thread.getAllStackTraces().entrySet()) {
          if (!entry.getKey().getName().equals("Render thread")) continue;
          boolean insideCheck = java.util.Arrays.stream(entry.getValue())
              .anyMatch(frame -> frame.getClassName().contains("GpuBackendChecks"));
          if (!insideCheck) return;
          StringBuilder report = new StringBuilder("GPU_CHECK_WATCHDOG_STACK state=")
              .append(entry.getKey().getState()).append('\n');
          for (var frame : entry.getValue()) report.append("  ").append(frame).append('\n');
          Files.writeString(output.resolve("gpu-watchdog-stack.txt"), report);
          System.out.print(report);
        }
      } catch (Exception failure) { System.out.println("GPU_CHECK_WATCHDOG_CAPTURE_FAILED " + failure); }
    }, "Deep Primal validation GPU watchdog");
    watcher.setDaemon(true); watcher.start();
  }

  private StartupLifecycleChecks() {}
}
