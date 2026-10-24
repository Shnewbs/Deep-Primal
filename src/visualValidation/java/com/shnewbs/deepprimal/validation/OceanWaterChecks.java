package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.engine.*;
import com.shnewbs.deepprimal.settings.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

/** Multi-chunk, multi-section ocean presentation fixture. Never modifies server water. */
public final class OceanWaterChecks {
  private static int stage, ticks;
  private static long began;
  private static volatile boolean prepared;
  private static volatile int oceanX;
  private static volatile String oceanAudit;
  private static String meshAudit;
  private static long exposedKey;
  private static BlockPos exposed;
  private static net.minecraft.world.level.block.state.BlockState covered;
  private static volatile boolean reloaded;
  private static net.minecraft.client.gui.screens.Screen warning;

  private static boolean delivered(Minecraft mc, int x, int z) {
    var c = mc.level.getChunkSource().getChunk(x, z, false);
    return c != null && !(c instanceof net.minecraft.world.level.chunk.EmptyLevelChunk);
  }

  private static void command(Minecraft mc, String cmd) {
    mc.getSingleplayerServer()
        .execute(
            () -> {
              var s = mc.getSingleplayerServer();
              s.getCommands().performPrefixedCommand(s.createCommandSourceStack(), cmd);
            });
  }

  private static void capture(Minecraft mc, String name) {
    var out = Path.of(System.getProperty("deepprimal.validationCapture"));
    out.toFile().mkdirs();
    Screenshot.grab(out.toFile(), name, mc.getMainRenderTarget(), 1, m -> {});
  }

  private static void fixture(Minecraft mc) {
    var p = new BlockPos.MutableBlockPos();
    for (int z = 0; z < 32; z++)
      for (int x = 0; x < 32; x++) {
        int floor = 493 + z / 4;
        for (int y = 492; y <= 518; y++) {
          var state =
              y <= floor
                  ? Blocks.SAND.defaultBlockState()
                  : y < 514
                      ? EngineRegistry.WATER_BLOCK.get().defaultBlockState()
                      : Blocks.AIR.defaultBlockState();
          mc.level.setBlock(p.set(x, y, z), state, 2 | 16);
        }
      }
  }

  private static String inspect(Minecraft mc) {
    try {
      var r = AnimatedWaterRenderer.INSTANCE;
      var field = r.getClass().getDeclaredField("pages");
      field.setAccessible(true);
      var pages = (java.util.Map<?, ?>) field.get(r);
      if (pages.isEmpty() || r.residentBytes == 0)
        throw new AssertionError("Ocean inspection requires resident GPU water");
      int walls = 0, unloaded = 0;
      for (var page : pages.values()) {
        var type = page.getClass();
        var fx = type.getDeclaredField("x");
        fx.setAccessible(true);
        var fz = type.getDeclaredField("z");
        fz.setAccessible(true);
        var fs = type.getDeclaredField("snapshot");
        fs.setAccessible(true);
        int x = fx.getInt(page), z = fz.getInt(page);
        if (!delivered(mc, x >> 4, z >> 4)) unloaded++;
        var s = (com.shnewbs.deepprimal.core.WaterPatchMesher.Snapshot) fs.get(page);
        if (s == null) continue;
        var data = com.shnewbs.deepprimal.core.WaterPatchMesher.build(s, null, true).vertices();
        for (int i = 0;
            i < data.length;
            i += com.shnewbs.deepprimal.core.WaterPatchMesher.STRIDE * 4) {
          int face = (int) data[i + 10] & 7, cell = (int) data[i + 9];
          if (face < 1 || face > 4) continue;
          int wx = x + (cell & 15) + (face == 2 ? 1 : face == 4 ? -1 : 0);
          int wz = z + ((cell >> 4) & 15) + (face == 3 ? 1 : face == 1 ? -1 : 0);
          if (!delivered(mc, wx >> 4, wz >> 4)) walls++;
        }
      }
      return "unloadedPages=" + unloaded + " unloadedBoundaryFaces=" + walls;
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public static void tick(Minecraft mc) {
    if (stage == 16) return;
    if (began == 0) began = System.nanoTime();
    if (System.nanoTime() - began > 180_000_000_000L)
      throw new AssertionError("Ocean water timeout stage=" + stage);
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
      if ("Vulkan".equals(System.getProperty("deepprimal.graphicsBackend"))
          && !RenderSystem.getDevice().getBackendName().equals("Vulkan"))
        throw new AssertionError("Requested Vulkan backend is not active");
      WaterVisualConfig.INTERPOLATE.set(true);
      WaterVisualConfig.SMOOTH.set(true);
      WaterVisualConfig.WAVES.set(false);
      LodConfig.ENABLED.set(false);
      ClientPerformanceConfig.LEGACY_HORIZON.set(false);
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(false);
      ClientPerformanceConfig.ADAPTIVE_WORK.set(false);
      mc.options.hideGui = true;
      command(mc, "gamemode spectator @a");
      command(mc, "time set noon");
      command(mc, "weather clear");
      command(mc, "tp @a 16 530 30 180 40");
      mc.getSingleplayerServer().execute(() -> prepared = true);
      stage = 1;
      ticks = 0;
    } else if (stage == 1 && prepared && ticks > 80 && delivered(mc, 0, 0) && delivered(mc, 1, 1)) {
      fixture(mc);
      stage = 2;
      ticks = 0;
    } else if (stage == 2
        && ticks > 120
        && AnimatedWaterRenderer.INSTANCE.displayedHeight(8, 513, 8) == 1) {
      capture(mc, "ocean-animated-above.png");
      command(mc, "tp @a 16 505 23 180 0");
      stage = 3;
      ticks = 0;
    } else if (stage == 3 && ticks > 100) {
      capture(mc, "ocean-animated-under.png");
      WaterVisualConfig.INTERPOLATE.set(false);
      stage = 4;
      ticks = 0;
    } else if (stage == 4 && ticks > 100) {
      capture(mc, "ocean-fallback-under.png");
      command(mc, "tp @a 16 530 30 180 40");
      stage = 5;
      ticks = 0;
    } else if (stage == 5 && ticks > 100) {
      capture(mc, "ocean-fallback-above.png");
      stage = 6;
      ticks = 0;
    } else if (stage == 6 && ticks > 20) {
      WaterVisualConfig.INTERPOLATE.set(true);
      prepared = false;
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var s = mc.getSingleplayerServer();
                var g = (EngineChunkGenerator) s.overworld().getChunkSource().getGenerator();
                int x = 128;
                while (x < 16000 && g.geography().sample(x, 0).ground() > 75) x += 32;
                if (x >= 16000) throw new AssertionError("No deep ocean in scene search");
                oceanX = x;
                s.getCommands()
                    .performPrefixedCommand(
                        s.createCommandSourceStack(), "tp @a " + x + " 125 20 180 40");
                prepared = true;
              });
      stage = 7;
      ticks = 0;
    } else if (stage == 7 && prepared && ticks > 150 && delivered(mc, oceanX >> 4, 0)) {
      capture(mc, "generated-animated-above.png");
      meshAudit = inspect(mc);
      command(mc, "tp @a " + oceanX + " 84 16 180 0");
      stage = 8;
      ticks = 0;
    } else if (stage == 8 && ticks > 100) {
      capture(mc, "generated-animated-under.png");
      WaterVisualConfig.INTERPOLATE.set(false);
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var l = mc.getSingleplayerServer().overworld();
                var g = (EngineChunkGenerator) l.getChunkSource().getGenerator();
                int expected = 0, missing = 0, partial = 0;
                var p = new BlockPos.MutableBlockPos();
                for (int z = -16; z < 32; z++)
                  for (int x = oceanX - 24; x < oceanX + 24; x++) {
                    if (!l.hasChunk(x >> 4, z >> 4)) continue;
                    var sample = g.geography().sample(x, z);
                    if (!sample.wet() || sample.water() != 96) continue;
                    for (int y = (int) Math.floor(sample.ground()) + 1; y < 96; y++) {
                      expected++;
                      int n = WaterRuntime.units(l, p.set(x, y, z));
                      if (n == 0) missing++;
                      else if (n < 16) partial++;
                    }
                  }
                oceanAudit =
                    "generatedCells=" + expected + " missing=" + missing + " partial=" + partial;
              });
      stage = 9;
      ticks = 0;
    } else if (stage == 9 && ticks > 100 && oceanAudit != null) {
      capture(mc, "generated-fallback-under.png");
      WaterVisualConfig.INTERPOLATE.set(true);
      WaterVisualConfig.WAVES.set(true);
      LodConfig.ENABLED.set(true);
      stage = 10;
      ticks = 0;
    } else if (stage == 10 && ticks > 150) {
      capture(mc, "generated-all-under.png");
      command(mc, "tp @a " + oceanX + " 125 20 180 40");
      stage = 11;
      ticks = 0;
    } else if (stage == 11 && ticks > 100) {
      capture(mc, "generated-all-above.png");
      stage = 12;
      ticks = 0;
    } else if (stage == 12 && ticks > 20) {
      mc.reloadResourcePacks().thenRun(() -> reloaded = true);
      stage = 13;
      ticks = 0;
    } else if (stage == 13
        && reloaded
        && ticks > 100
        && AnimatedWaterRenderer.INSTANCE.residentBytes > 0) {
      if (!meshAudit.equals("unloadedPages=0 unloadedBoundaryFaces=0")
          || !oceanAudit.endsWith("missing=0 partial=0"))
        throw new AssertionError("Ocean regression: " + oceanAudit + " " + meshAudit);
      try {
        String afterReload = inspect(mc), validation = "notApplicable";
        var renderer = AnimatedWaterRenderer.INSTANCE;
        var hiddenField = renderer.getClass().getDeclaredField("hidden");
        hiddenField.setAccessible(true);
        int hidden = ((java.util.Set<?>) hiddenField.get(renderer)).size();
        if (hidden == 0)
          throw new AssertionError("Generated ocean must exercise hidden-page cache");
        var pageField = renderer.getClass().getDeclaredField("pages");
        pageField.setAccessible(true);
        for (var page : ((java.util.Map<?, ?>) pageField.get(renderer)).values()) {
          var gpu = page.getClass().getDeclaredField("gpu");
          gpu.setAccessible(true);
          var busy = page.getClass().getDeclaredField("busy");
          busy.setAccessible(true);
          var changed = page.getClass().getDeclaredField("changed");
          changed.setAccessible(true);
          if (gpu.get(page) == null && !busy.getBoolean(page) && !changed.getBoolean(page))
            throw new AssertionError("Settled hidden page occupies active storage");
        }
        if (!afterReload.equals("unloadedPages=0 unloadedBoundaryFaces=0"))
          throw new AssertionError("Reload retained invalid ocean mesh: " + afterReload);
        if (RenderSystem.getDevice().getBackendName().equals("Vulkan")) {
          var stats =
              (java.util.Map<?, ?>)
                  Class.forName("net.vulkanmod.vulkan.VulkanStatistics")
                      .getMethod("snapshot")
                      .invoke(null);
          if (((Number) stats.get("validationErrors")).intValue() != 0)
            throw new AssertionError("Ocean Vulkan validation errors: " + stats);
          validation = "0";
        }
        Files.writeString(
            Path.of(System.getProperty("deepprimal.validationCapture"), "ocean-result.txt"),
            "OCEAN_WATER_NATIVE_PASS backend="
                + RenderSystem.getDevice().getBackendName()
                + " hiddenPages="
                + hidden
                + " emptyActivePages=0 animatedAndFallback=true views=9 resourceReload=true"
                + " validationErrors="
                + validation
                + " "
                + oceanAudit
                + " "
                + meshAudit
                + "\n");
        var hiddenKeys = (java.util.Set<Long>) hiddenField.get(renderer);
        exposedKey =
            hiddenKeys.stream()
                .filter(
                    key -> mc.level.getBlockState(center(key)).is(EngineRegistry.WATER_BLOCK.get()))
                .min(
                    java.util.Comparator.comparingDouble(
                        key ->
                            mc.player.distanceToSqr(
                                center(key).getX(), center(key).getY(), center(key).getZ())))
                .orElseThrow();
        exposed = center(exposedKey);
        covered = mc.level.getBlockState(exposed);
        command(
            mc,
            "tp @a "
                + (exposed.getX() + .5)
                + " "
                + (exposed.getY() + 5)
                + " "
                + (exposed.getZ() + .5)
                + " 0 90");
        mc.level.setBlock(exposed, Blocks.AIR.defaultBlockState(), 2 | 16);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      stage = 14;
      ticks = 0;
    } else if (stage == 14 && ticks > 60) {
      try {
        var renderer = AnimatedWaterRenderer.INSTANCE;
        var field = renderer.getClass().getDeclaredField("pages");
        field.setAccessible(true);
        var page = ((java.util.Map<?, ?>) field.get(renderer)).get(exposedKey);
        if (page == null) throw new AssertionError("Hidden page did not return after exposure");
        var gpu = page.getClass().getDeclaredField("gpu");
        gpu.setAccessible(true);
        if (gpu.get(page) == null) throw new AssertionError("Newly exposed liquid has no GPU mesh");
        mc.level.setBlock(exposed, covered, 2 | 16);
        stage = 15;
        ticks = 0;
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    } else if (stage == 15 && ticks > 60) {
      try {
        var renderer = AnimatedWaterRenderer.INSTANCE;
        var field = renderer.getClass().getDeclaredField("hidden");
        field.setAccessible(true);
        if (!((java.util.Set<?>) field.get(renderer)).contains(exposedKey))
          throw new AssertionError("Covered liquid did not release its active page");
        Files.writeString(
            Path.of(System.getProperty("deepprimal.validationCapture"), "ocean-result.txt"),
            "WATER_HIDDEN_PAGE_LIFECYCLE_PASS exposureRestored=true coveredPageReleased=true\n",
            StandardOpenOption.APPEND);
        stage = 16;
        mc.stop();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static BlockPos center(long key) {
    return new BlockPos(
        net.minecraft.core.SectionPos.x(key) * 16 + 8,
        net.minecraft.core.SectionPos.y(key) * 16 + 8,
        net.minecraft.core.SectionPos.z(key) * 16 + 8);
  }
}
