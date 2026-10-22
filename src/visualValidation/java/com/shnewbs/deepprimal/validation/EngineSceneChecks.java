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
  private static String[] names = {"coast", "river", "ridge"};
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
              if (g.geography().revision >= 3 && view > 0 && view < 5) {
                var region = g.geography().waterways(0, 0);
                double targetX, targetZ, level;
                if (view == 1 || view == 2) {
                  var line = region.centerline();
                  com.shnewbs.deepprimal.core.WatershedRegion.Probe chosen = null;
                  double score = -Double.MAX_VALUE;
                  for (int n = 0; n < line.size(); n += 16) {
                    var root = line.get(n);
                    var mid = line.get(n + 7);
                    if (mid.stage() < 112 || (view == 1 && !root.source())) continue;
                    var actual = g.geography().sample(mid.x(), mid.z());
                    if (actual.speed() == 0 || Math.abs(actual.water() - mid.stage()) > .2)
                      continue;
                    double radius = mid.radius() + 4;
                    int dry = 0;
                    for (int k = 0; k < 8; k++) {
                      double angle = k * Math.PI / 4;
                      if (!g.geography()
                          .sample(
                              mid.x() + Math.cos(angle) * radius,
                              mid.z() + Math.sin(angle) * radius)
                          .wet()) dry++;
                    }
                    if (dry < 6) continue;
                    double merit = view == 1 ? -mid.area() : mid.area();
                    if (merit > score) {
                      score = merit;
                      chosen = mid;
                    }
                  }
                  if (chosen == null) throw new AssertionError("No genuine flowing scene");
                  targetX = chosen.x();
                  targetZ = chosen.z();
                  level = g.geography().sample(targetX, targetZ).water();
                } else {
                  var p =
                      region.lakeProbes().stream()
                          .filter(a -> a.area() < 30000)
                          .max(java.util.Comparator.comparingDouble(a -> a.area()))
                          .orElseThrow();
                  targetX = view == 3 ? p.x() : p.outletX();
                  targetZ = view == 3 ? p.z() : p.outletZ();
                  level = p.level();
                }
                double distance = view == 1 ? 30 : 65;
                if (view == 1 || view == 2) {
                  var flow = g.geography().sample(targetX, targetZ);
                  x = targetX - flow.flowX() * distance + flow.flowZ() * distance * .3;
                  z = targetZ - flow.flowZ() * distance - flow.flowX() * distance * .3;
                  yaw = Math.toDegrees(Math.atan2(x - targetX, targetZ - z));
                } else {
                  x = targetX;
                  z = targetZ + distance;
                  yaw = 180;
                }
                y = Math.max(level + distance * .65, g.geography().sample(x, z).ground() + 24);
                pitch = Math.toDegrees(Math.atan2(y - level, Math.hypot(x - targetX, z - targetZ)));
                System.out.println(
                    "ENGINE_SCENE_TARGET name="
                        + names[view]
                        + " x="
                        + targetX
                        + " z="
                        + targetZ
                        + " level="
                        + level);
              } else if (view == 0) {
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
    if (stage == names.length + 1) {
      if (screenshotPending || ++ticks < 30) return;
      mc.disconnectWithSavingScreen();
      mc.stop();
      stage = names.length + 2;
      return;
    }
    if (stage == names.length + 2 || mc.screen != null) return;
    mc.options.hideGui = true;
    mc.player.getAbilities().flying = true;
    if (stage == 0) {
      if ("Vulkan".equals(System.getProperty("deepprimal.graphicsBackend"))
          && !com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName().equals("Vulkan"))
        throw new AssertionError("Scene backend fell back");
      if (mc.getSingleplayerServer().overworld().getChunkSource().getGenerator()
              instanceof EngineChunkGenerator g
          && g.geography().revision >= 3)
        names = new String[] {"coast", "headwater", "river", "lake", "outlet", "ridge"};
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
    String name = names[stage - 1];
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
    if (stage < names.length) {
      move(mc, stage);
      stage++;
    } else {
      try {
        String report =
            "INTEGRATED_CLIENT_PASS scenes="
                + String.join(",", names)
                + " backend="
                + com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName()
                + " fluidCells="
                + com.shnewbs.deepprimal.client.render.SmoothWaterRenderer.renderedCells
                + "\n";
        Files.writeString(out.resolve("scene-result.txt"), report);
        System.out.print(report);
        ticks = 0;
        stage = names.length + 1;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private EngineSceneChecks() {}
}
