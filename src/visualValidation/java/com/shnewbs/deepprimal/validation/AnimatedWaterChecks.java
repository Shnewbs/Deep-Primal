package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.engine.*;
import com.shnewbs.deepprimal.mixin.client.LevelRendererAccessor;
import com.shnewbs.deepprimal.settings.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.*;
import net.minecraft.world.level.block.Blocks;

/** Real GPU water updates without rebuilding the terrain section. Not shipped. */
public final class AnimatedWaterChecks {
  private static int stage, ticks, samples;
  private static long began, uploads, avoided;
  private static volatile boolean prepared;
  private static volatile boolean reloaded;
  private static Object terrain;
  private static net.minecraft.client.gui.screens.Screen warning;
  private static float last = Float.NaN;
  private static boolean lowCaptured, highCaptured;
  private static final int Y = 512;

  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  private static net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection section(
      Minecraft mc) {
    var view = ((LevelRendererAccessor) mc.levelRenderer).deepprimal$viewArea();
    if (view == null) return null;
    for (var s : view.sections) if (s.getSectionNode() == SectionPos.asLong(0, Y >> 4, 0)) return s;
    return null;
  }

  private static void pool(Minecraft mc, int units) {
    for (int z = 2; z < 14; z++)
      for (int x = 2; x < 14; x++)
        mc.level.setBlock(
            new BlockPos(x, Y, z),
            units == 0
                ? Blocks.AIR.defaultBlockState()
                : EngineRegistry.WATER_BLOCK
                    .get()
                    .defaultBlockState()
                    .setValue(EngineFluid.UNITS, units),
            2 | 16);
  }

  public static void tick(Minecraft mc) {
    if (began == 0) began = System.nanoTime();
    require(System.nanoTime() - began < 240_000_000_000L, "Animated water timeout stage=" + stage);
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
    var r = AnimatedWaterRenderer.INSTANCE;
    ticks++;
    if (stage == 0) {
      if ("Vulkan".equals(System.getProperty("deepprimal.graphicsBackend")))
        require(
            RenderSystem.getDevice().getBackendName().equals("Vulkan"), "Actual Vulkan backend");
      WaterVisualConfig.INTERPOLATE.set(true);
      WaterVisualConfig.SMOOTH.set(true);
      LodConfig.ENABLED.set(false);
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(false);
      ClientPerformanceConfig.ADAPTIVE_WORK.set(false);
      mc.options.hideGui = true;
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var s = mc.getSingleplayerServer();
                var src = s.createCommandSourceStack();
                for (String cmd :
                    new String[] {
                      "gamemode spectator @a",
                      "time set noon",
                      "weather clear",
                      "tp @a 8 521 22 180 28"
                    }) s.getCommands().performPrefixedCommand(src, cmd);
                prepared = true;
              });
      stage = 1;
      ticks = 0;
      return;
    }
    if (stage == 1 && prepared && ticks > 70 && section(mc) != null) {
      for (int z = 0; z < 16; z++)
        for (int x = 0; x < 16; x++) {
          mc.level.setBlock(new BlockPos(x, Y - 1, z), Blocks.STONE.defaultBlockState(), 2 | 16);
          for (int y = Y; y < Y + 5; y++)
            mc.level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2 | 16);
        }
      pool(mc, 2);
      stage = 2;
      ticks = 0;
      return;
    }
    if (stage == 2 && ticks > 80 && Math.abs(r.displayedHeight(8, Y, 8) - .125f) < .001) {
      terrain = section(mc).getSectionMesh();
      uploads = r.uploads;
      avoided = r.terrainInvalidationsAvoided;
      pool(mc, 16);
      stage = 3;
      ticks = 0;
      return;
    }
    if (stage == 3 && ticks > 30 && Math.abs(r.displayedHeight(8, Y, 8) - 1) < .001) {
      require(
          r.uploads > uploads && r.terrainInvalidationsAvoided >= avoided + 144,
          "Water updates use independent uploads");
      require(section(mc).getSectionMesh() == terrain, "Water height edits do not rebuild terrain");
      require(
          samples >= 2,
          "Distinct intermediate displayed heights sampled across frames: " + samples);
      pool(mc, 0);
      stage = 4;
      ticks = 0;
      return;
    }
    if (stage == 4 && ticks > 40) {
      require(r.displayedHeight(8, Y, 8) < .001, "Dry transition reaches zero");
      WaterVisualConfig.INTERPOLATE.set(false);
      pool(mc, 16);
      stage = 5;
      ticks = 0;
      return;
    }
    if (stage == 5 && ticks > 80) {
      require(
          !AnimatedWaterRenderer.ownsWater()
              && r.residentBytes == 0
              && section(mc).getSectionMesh().hasTranslucentGeometry(),
          "Fallback rebuilds native water and releases GPU pages");
      WaterVisualConfig.INTERPOLATE.set(true);
      stage = 6;
      ticks = 0;
      return;
    }
    if (stage == 6 && ticks > 80 && r.residentBytes > 0) {
      require(
          AnimatedWaterRenderer.ownsWater()
              && !section(mc).getSectionMesh().hasTranslucentGeometry(),
          "Re-enable replaces fallback without duplicate water");
      mc.reloadResourcePacks().thenRun(() -> reloaded = true);
      stage = 7;
      ticks = 0;
      return;
    }
    if (stage == 7 && reloaded && ticks > 80 && r.residentBytes > 0) {
      try {
        String validation = "notApplicable";
        if (RenderSystem.getDevice().getBackendName().equals("Vulkan")) {
          var stats =
              (java.util.Map<?, ?>)
                  Class.forName("net.vulkanmod.vulkan.VulkanStatistics")
                      .getMethod("snapshot")
                      .invoke(null);
          require(
              ((Number) stats.get("validationErrors")).intValue() == 0, "Vulkan validation clean");
          validation = "0";
        }
        Path out = Path.of(System.getProperty("deepprimal.validationCapture"));
        Files.createDirectories(out);
        String result =
            "ANIMATED_WATER_NATIVE_PASS backend="
                + RenderSystem.getDevice().getBackendName()
                + " intermediateFrames="
                + samples
                + " uploads="
                + r.uploads
                + " terrainInvalidationsAvoided="
                + r.terrainInvalidationsAvoided
                + " dry=true fallback=true reenable=true resourceReload=true validationErrors="
                + validation
                + "\n";
        Files.writeString(out.resolve("animated-water-result.txt"), result);
        System.out.print(result);
        Screenshot.grab(
            out.toFile(),
            "animated-water.png",
            mc.getMainRenderTarget(),
            1,
            m -> mc.execute(mc::stop));
        stage = 8;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void frame() {
    if (stage != 3) return;
    float h = AnimatedWaterRenderer.INSTANCE.displayedHeight(8, Y, 8);
    if (h > .1251 && h < .9999 && h != last) {
      samples++;
      last = h;
    }
    String name = null;
    if (h > .1251 && h < .55 && !lowCaptured) {
      name = "water-intermediate-low.png";
      lowCaptured = true;
    }
    if (h >= .55 && h < .9999 && !highCaptured) {
      name = "water-intermediate-high.png";
      highCaptured = true;
    }
    if (name != null) {
      var mc = Minecraft.getInstance();
      var out = Path.of(System.getProperty("deepprimal.validationCapture"));
      out.toFile().mkdirs();
      Screenshot.grab(out.toFile(), name, mc.getMainRenderTarget(), 1, m -> {});
    }
  }
}
