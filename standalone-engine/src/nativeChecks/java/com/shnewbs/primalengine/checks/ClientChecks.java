package com.shnewbs.primalengine.checks;

import com.shnewbs.primalengine.PrimalEngine;
import com.shnewbs.primalengine.client.SmoothWaterRenderer;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = PrimalEngine.ID, dist = Dist.CLIENT)
public final class ClientChecks {
  private int ticks, stage;
  private long started;

  public ClientChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
    if (System.getProperty("primalengine.capture") == null) return;
    var mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) {
      if (mc.screen
              instanceof
              net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen
          || mc.screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen)
        for (var child : mc.screen.children())
          if (child instanceof net.minecraft.client.gui.components.Button b
              && (b.getMessage().getString().equals("Proceed")
                  || b.getMessage().getString().equals("I know what I'm doing!"))) {
            b.onPress(null);
            break;
          }
      return;
    }
    mc.options.hideGui = true;
    if (mc.screen != null) return;
    if (started == 0) started = System.nanoTime();
    if (System.nanoTime() - started > 180_000_000_000L)
      throw new AssertionError("Custom water mesher timed out");
    if (stage == 0) {
      var server = mc.getSingleplayerServer();
      server.execute(
          () -> {
            var source = server.createCommandSourceStack();
            server.getCommands().performPrefixedCommand(source, "time set noon");
            server.getCommands().performPrefixedCommand(source, "weather clear");
            server.getCommands().performPrefixedCommand(source, "gamemode creative @a");
            for (var player : server.getPlayerList().getPlayers()) {
              player.getAbilities().flying = true;
              player.onUpdateAbilities();
            }
            var generator =
                (com.shnewbs.primalengine.ContinentalGenerator)
                    server.overworld().getChunkSource().getGenerator();
            double viewY = Math.min(600, generator.geography().sample(0, 0).ground() + 70);
            server.getCommands().performPrefixedCommand(source, "tp @a 0 " + viewY + " 0 35 35");
          });
      stage = 1;
      ticks = 0;
      return;
    }
    mc.player.getAbilities().flying = true;
    if (stage == 1 && ++ticks > 120) {
      capture(mc, "continental-final");
      stage = 2;
      ticks = 0;
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var server = mc.getSingleplayerServer();
                var g =
                    (com.shnewbs.primalengine.ContinentalGenerator)
                        server.overworld().getChunkSource().getGenerator();
                int coast = 0;
                for (int x = 0; x < 4000; x += 16)
                  if (g.geography().uncarved(x, 0) < 96) {
                    coast = x;
                    break;
                  }
                var level = server.overworld();
                // Disposable sloped-volume basin exercises the curved path independently of the
                // sea.
                for (int dz = -3; dz <= 3; dz++)
                  for (int dx = -3; dx <= 3; dx++) {
                    var p = new net.minecraft.core.BlockPos(coast + dx, 110, dz);
                    level.setBlock(
                        p,
                        net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),
                        2 | 16);
                    if (Math.abs(dx) == 3 || Math.abs(dz) == 3)
                      level.setBlock(
                          p.above(),
                          net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),
                          2 | 16);
                    else
                      level.setBlock(
                          p.above(),
                          PrimalEngine.WATER_BLOCK
                              .get()
                              .defaultBlockState()
                              .setValue(
                                  com.shnewbs.primalengine.EngineFluid.UNITS,
                                  dx == 0 && dz == 0 ? 8 : 16),
                          2 | 16);
                  }
                server
                    .getCommands()
                    .performPrefixedCommand(
                        server.createCommandSourceStack(),
                        "tp @a " + (coast + 24) + " 135 0 135 35");
              });
      return;
    }
    if (stage == 2
        && ++ticks > 180
        && SmoothWaterRenderer.renderedCells > 0
        && SmoothWaterRenderer.curvedCells > 0) {
      capture(mc, "coast-final");
      System.out.println(
          "ENGINE_CLIENT_PASS customMesherCells="
              + SmoothWaterRenderer.renderedCells
              + " curvedCells="
              + SmoothWaterRenderer.curvedCells
              + " backend="
              + com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName());
      stage = 3;
      ticks = 0;
      return;
    }
    if (stage == 3 && ++ticks > 30) mc.stop();
  }

  private static void capture(Minecraft mc, String name) {
    Path root = Path.of(System.getProperty("primalengine.capture"));
    Screenshot.grab(
        root.toFile(),
        name + ".png",
        mc.getMainRenderTarget(),
        1,
        message -> System.out.println("ENGINE_CAPTURE_SAVED " + name));
  }
}
