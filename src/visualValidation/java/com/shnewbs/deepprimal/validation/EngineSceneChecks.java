package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.engine.EngineChunkGenerator;
import com.shnewbs.deepprimal.settings.*;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/** Native fixed-seed coast, river and ridge captures; no generated presentation images. */
final class EngineSceneChecks {
  private static int stage, ticks;
  private static volatile boolean waiting, screenshotPending;

  private static void move(Minecraft mc, int view) {
    waiting = true;
    ticks = 0;
    mc.getSingleplayerServer()
        .execute(
            () -> {
              var server = mc.getSingleplayerServer();
              var g = (EngineChunkGenerator) server.overworld().getChunkSource().getGenerator();
              double x, z, y, yaw, pitch;
              if (view == 0) {
                int coast = 128;
                while (coast < 4000 && g.geography().raw(coast, 0) > 96) coast += 16;
                x = coast - 56;
                z = 0;
                y = Math.max(142, g.geography().sample(x, z).ground() + 35);
                yaw = 270;
                pitch = 24;
              } else if (view == 1) {
                x = -240;
                z = -624 + 65;
                y = g.geography().sample(-240, -624).water() + 55;
                yaw = 180;
                pitch = 36;
              } else {
                x = -528;
                z = -816;
                y = g.geography().sample(x, z).ground() + 60;
                yaw = 35;
                pitch = 30;
              }
              var source = server.createCommandSourceStack();
              server.getCommands().performPrefixedCommand(source, "time set noon");
              server.getCommands().performPrefixedCommand(source, "weather clear");
              server.getCommands().performPrefixedCommand(source, "gamemode creative @a");
              for (var p : server.getPlayerList().getPlayers()) {
                p.getAbilities().flying = true;
                p.onUpdateAbilities();
              }
              server
                  .getCommands()
                  .performPrefixedCommand(
                      source, "tp @a " + x + " " + y + " " + z + " " + yaw + " " + pitch);
              waiting = false;
            });
  }

  private static boolean loaded(Minecraft mc, int x, int z) {
    var c = mc.level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
    return c != null && !(c instanceof net.minecraft.world.level.chunk.EmptyLevelChunk);
  }

  static void tick(Minecraft mc) {
    if (stage == 4) {
      if (screenshotPending || ++ticks < 30) return;
      mc.disconnectWithSavingScreen();
      mc.stop();
      stage = 5;
      return;
    }
    if (stage == 5 || mc.screen != null) return;
    mc.options.hideGui = true;
    mc.player.getAbilities().flying = true;
    if (stage == 0) {
      if ("Vulkan".equals(System.getProperty("deepprimal.graphicsBackend"))
          && !com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName().equals("Vulkan"))
        throw new AssertionError("Scene backend fell back");
      LodConfig.ENABLED.set(false);
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(false);
      ClientPerformanceConfig.ADAPTIVE_WORK.set(false);
      mc.options.renderDistance().set(8);
      mc.options.save();
      mc.options.broadcastOptions();
      mc.levelRenderer.allChanged();
      move(mc, 0);
      stage = 1;
      return;
    }
    if (waiting) return;
    ticks++;
    var p = mc.player.chunkPosition();
    boolean ready =
        loaded(mc, p.x() + 5, p.z())
            && loaded(mc, p.x() - 5, p.z())
            && loaded(mc, p.x(), p.z() + 5)
            && loaded(mc, p.x(), p.z() - 5);
    if (ticks % 100 == 0)
      System.out.println(
          "ENGINE_SCENE_PROGRESS scene=" + stage + " ticks=" + ticks + " ready=" + ready);
    if (ticks > 800) throw new AssertionError("Scene readiness timed out");
    if (ticks < 200 || !ready || (!mc.levelRenderer.hasRenderedAllSections() && ticks < 650))
      return;
    String name = new String[] {"coast", "river", "ridge"}[stage - 1];
    Path out = Path.of(System.getProperty("deepprimal.validationCapture"));
    out.toFile().mkdirs();
    screenshotPending = true;
    Screenshot.grab(
        out.toFile(),
        "scene-" + name + ".png",
        mc.getMainRenderTarget(),
        1,
        m -> {
          screenshotPending = false;
          System.out.println("ENGINE_SCENE_CAPTURE " + name);
        });
    if (stage < 3) {
      move(mc, stage);
      stage++;
    } else {
      try {
        String report =
            "INTEGRATED_CLIENT_PASS scenes=coast,river,ridge backend="
                + com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName()
                + " fluidCells="
                + com.shnewbs.deepprimal.client.render.SmoothWaterRenderer.renderedCells
                + "\n";
        Files.writeString(out.resolve("scene-result.txt"), report);
        System.out.print(report);
        ticks = 0;
        stage = 4;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private EngineSceneChecks() {}
}
