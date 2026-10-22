package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.core.WaterTransport;
import com.shnewbs.deepprimal.engine.*;
import com.shnewbs.deepprimal.settings.*;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

/** Controlled measured-volume geometry; client-only edits intentionally isolate meshing. */
final class WaterShapeChecks {
  private static int stage, ticks, view;
  private static long began;
  private static volatile boolean prepared, pending;
  private static net.minecraft.client.gui.screens.Screen warning;
  private static final int Y = 512;

  static void tick(Minecraft mc) {
    if (began == 0) began = System.nanoTime();
    if (System.nanoTime() - began > 240_000_000_000L)
      throw new AssertionError("Water shape timeout " + stage);
    if (mc.level == null || mc.player == null) {
      if (mc.screen != warning
          && (mc.screen
                  instanceof
                  net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen
              || mc.screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen))
        for (var c : mc.screen.children())
          if (c instanceof Button b
              && (b.getMessage().getString().equals("Proceed")
                  || b.getMessage().getString().equals("I know what I'm doing!"))) {
            warning = mc.screen;
            b.onPress(null);
            break;
          }
      return;
    }
    ticks++;
    if (stage == 0) {
      String backend = RenderSystem.getDevice().getBackendName();
      if ("Vulkan".equals(System.getProperty("deepprimal.graphicsBackend"))
          && !backend.equals("Vulkan")) throw new AssertionError("Backend fallback");
      boolean before = Boolean.getBoolean("deepprimal.validationOldSurface");
      double kernel = WaterTransport.surface(1, 1, 1, 1, .0625, .5, .5);
      if (kernel != (before ? .0625 : 1))
        throw new AssertionError("Wrong runtime kernel " + kernel);
      System.out.println(
          "WATER_SHAPE_RUNTIME kernel="
              + kernel
              + " source="
              + WaterTransport.class.getProtectionDomain().getCodeSource().getLocation());
      LodConfig.ENABLED.set(false);
      WaterVisualConfig.SMOOTH.set(true);
      WaterVisualConfig.WAVES.set(false);
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(false);
      ClientPerformanceConfig.ADAPTIVE_WORK.set(false);
      mc.options.renderDistance().set(3);
      mc.options.hideGui = true;
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var s = mc.getSingleplayerServer();
                var src = s.createCommandSourceStack();
                for (String command :
                    new String[] {
                      "gamemode spectator @a",
                      "time set noon",
                      "weather clear",
                      "tp @a 15 523 30 180 30"
                    }) s.getCommands().performPrefixedCommand(src, command);
                prepared = true;
              });
      stage = 1;
      ticks = 0;
      return;
    }
    if (stage == 1 && prepared && ticks > 80) {
      surface(mc, 0);
      stage = 2;
      ticks = 0;
      return;
    }
    if (stage == 2 && ticks > 100 && !pending) {
      if (view % 2 == 0 && AnimatedWaterRenderer.INSTANCE.residentBytes == 0)
        throw new AssertionError("No animated GPU geometry");
      if (view % 2 == 1 && AnimatedWaterRenderer.ownsWater())
        throw new AssertionError("Fallback still animated");
      Path out = Path.of(System.getProperty("deepprimal.validationCapture"));
      out.toFile().mkdirs();
      String name = (view < 2 ? "checker" : "slope") + (view % 2 == 0 ? "-animated" : "-fallback");
      pending = true;
      Screenshot.grab(
          out.toFile(),
          name + ".png",
          mc.getMainRenderTarget(),
          1,
          m -> {
            pending = false;
            System.out.println("WATER_SHAPE_CAPTURE " + name);
          });
      view++;
      stage = 3;
      ticks = 0;
      return;
    }
    if (stage == 3 && !pending && ticks > 10) {
      if (view < 4) {
        surface(mc, view);
        stage = 2;
        ticks = 0;
        return;
      }
      try {
        String validation = "notApplicable";
        if (RenderSystem.getDevice().getBackendName().equals("Vulkan")) {
          var stats =
              (java.util.Map<?, ?>)
                  Class.forName("net.vulkanmod.vulkan.VulkanStatistics")
                      .getMethod("snapshot")
                      .invoke(null);
          if (((Number) stats.get("validationErrors")).intValue() != 0)
            throw new AssertionError("Vulkan validation errors");
          validation = "0";
        }
        String report =
            "WATER_SHAPE_NATIVE_PASS backend="
                + RenderSystem.getDevice().getBackendName()
                + " kernel="
                + WaterTransport.surface(1, 1, 1, 1, .0625, .5, .5)
                + " captures=checker-animated,checker-fallback,slope-animated,slope-fallback"
                + " validationErrors="
                + validation
                + "\n";
        Files.writeString(
            Path.of(System.getProperty("deepprimal.validationCapture"))
                .resolve("water-shape-result.txt"),
            report);
        System.out.print(report);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
      stage = 4;
      mc.stop();
    }
  }

  private static void surface(Minecraft mc, int view) {
    WaterVisualConfig.INTERPOLATE.set(view % 2 == 0);
    for (int z = 0; z < 16; z++)
      for (int x = 0; x < 32; x++) {
        mc.level.setBlock(new BlockPos(x, Y - 1, z), Blocks.STONE.defaultBlockState(), 2 | 16);
        for (int y = Y; y < Y + 5; y++)
          mc.level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2 | 16);
        if (x == 0 || x == 31 || z == 0 || z == 15) {
          mc.level.setBlock(new BlockPos(x, Y, z), Blocks.STONE.defaultBlockState(), 2 | 16);
          continue;
        }
        int top = view < 2 ? Y : Y + (x - 1) / 16,
            units = view < 2 ? (((x + z) & 1) == 0 ? 16 : 1) : 1 + (x - 1) % 16;
        for (int y = Y; y <= top; y++)
          mc.level.setBlock(
              new BlockPos(x, y, z),
              EngineRegistry.WATER_BLOCK
                  .get()
                  .defaultBlockState()
                  .setValue(EngineFluid.UNITS, y == top ? units : 16),
              2 | 16);
      }
    mc.levelRenderer.allChanged();
  }
}
