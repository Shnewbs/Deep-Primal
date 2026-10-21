package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.lod.client.VoxelLodRenderer;
import com.shnewbs.deepprimal.settings.*;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Exercise real GPU, chunk/edit events and visible settings widgets. Not distributed. */
@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class IntegratedClientChecks {
  private int stage, ticks, tabCapture;
  private net.minecraft.client.gui.screens.Screen acceptedWarning;
  private long start, oldRevision;
  private volatile double ground;
  private final StringBuilder record = new StringBuilder();

  public IntegratedClientChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  private static void require(boolean v, String message) {
    if (!v) throw new AssertionError(message);
  }

  private void button(Minecraft mc, String label) {
    for (var child : mc.screen.children())
      if (child instanceof Button b && b.getMessage().getString().replace("› ", "").equals(label)) {
        b.onPress(null);
        return;
      }
    throw new AssertionError("Missing button: " + label);
  }

  private EditBox field(Minecraft mc, String label) {
    for (int n = 0; n < 12; n++) {
      for (var child : mc.screen.children())
        if (child instanceof EditBox b && b.getMessage().getString().equals(label)) return b;
      mc.screen.mouseScrolled(mc.screen.width - 30, 100, 0, -1);
    }
    throw new AssertionError("Missing field: " + label);
  }

  private void capture(Minecraft mc, String name) {
    var out = Path.of(System.getProperty("deepprimal.validationCapture"));
    out.toFile().mkdirs();
    Screenshot.grab(
        out.toFile(),
        name + ".png",
        mc.getMainRenderTarget(),
        1,
        m -> System.out.println("INTEGRATED_CAPTURE " + name));
  }

  @SubscribeEvent
  public void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("deepprimal.validationEngine")) return;
    var mc = Minecraft.getInstance();
    if (mc.level == null || mc.player == null) {
      if (mc.screen != acceptedWarning
          && (mc.screen
                  instanceof
                  net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen
              || mc.screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen))
        for (var child : mc.screen.children())
          if (child instanceof Button b
              && (b.getMessage().getString().equals("Proceed")
                  || b.getMessage().getString().equals("I know what I'm doing!"))) {
            acceptedWarning = mc.screen;
            b.onPress(null);
            break;
          }
      return;
    }
    if (Boolean.getBoolean("deepprimal.validationEngineScenes")) {
      EngineSceneChecks.tick(mc);
      return;
    }
    if (stage < 4 && mc.screen != null) return;
    if (start == 0) start = System.nanoTime();
    if (System.nanoTime() - start > 300_000_000_000L)
      throw new AssertionError(
          "Engine client validation timed out at stage "
              + stage
              + " LOD "
              + VoxelLodRenderer.INSTANCE.status
              + " uploaded "
              + VoxelLodRenderer.INSTANCE.uploadedChunks);
    mc.player.getAbilities().flying = true;
    ticks++;
    if (stage == 0) {
      if ("Vulkan".equals(System.getProperty("deepprimal.graphicsBackend")))
        require(
            com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName().equals("Vulkan"),
            "Requested Vulkan must actually run");
      LodConfig.ENABLED.set(false);
      require(
          com.shnewbs.deepprimal.client.render.HorizonRenderer.farDistance() == 0,
          "LOD off does not activate old macro horizon");
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(false);
      ClientPerformanceConfig.ADAPTIVE_WORK.set(false);
      LodConfig.ENABLED.set(true);
      if (Boolean.getBoolean("deepprimal.validationEngineReopen")) {
        require(
            LodConfig.RADIUS.get() == 208 && ClientPerformanceConfig.TARGET_FPS.get() == 88,
            "Settings survive full restart");
        record.append("SETTINGS_RESTART_PASS radius=208 targetFps=88\n");
      }
      mc.options.hideGui = true;
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var server = mc.getSingleplayerServer();
                var source = server.createCommandSourceStack();
                server.getCommands().performPrefixedCommand(source, "time set noon");
                server.getCommands().performPrefixedCommand(source, "weather clear");
                server.getCommands().performPrefixedCommand(source, "gamemode creative @a");
                for (var p : server.getPlayerList().getPlayers()) {
                  p.getAbilities().flying = true;
                  p.onUpdateAbilities();
                }
                var generator =
                    (com.shnewbs.deepprimal.engine.EngineChunkGenerator)
                        server.overworld().getChunkSource().getGenerator();
                ground = generator.geography().sample(0, 0).ground();
                server
                    .getCommands()
                    .performPrefixedCommand(source, "tp @a 0 " + (ground + 65) + " 0 35 30");
              });
      stage = 1;
      ticks = 0;
      return;
    }
    var lod = VoxelLodRenderer.INSTANCE;
    if (Boolean.getBoolean("deepprimal.validationLodHotEdits") && (stage == 1 || stage == 2))
      lod.dirty(0, 0);
    if (ticks % 100 == 0)
      System.out.println(
          "INTEGRATED_PROGRESS stage="
              + stage
              + " ticks="
              + ticks
              + " captured="
              + lod.capturedChunks
              + " uploaded="
              + lod.uploadedChunks
              + " visible="
              + lod.visibleChunks
              + " triangles="
              + lod.drawnTriangles
              + " sourceLoaded="
              + mc.level.hasChunk(0, 0)
              + " status="
              + lod.status);
    if (stage == 3 && ticks == 200) capture(mc, "lod-wait-diagnostic");
    if (stage == 1 && ticks > 100 && lod.snapshotRevision(0, 0) > 0 && lod.uploadedChunks > 12) {
      if (Boolean.getBoolean("deepprimal.validationLodHotEdits"))
        record.append("LOD_HOT_UPDATES_PROGRESS_PASS continuousInvalidation=true\n");
      capture(mc, "terrain-native");
      oldRevision = lod.snapshotRevision(0, 0);
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var l = mc.getSingleplayerServer().overworld();
                int y = (int) Math.ceil(ground) + 1;
                var roof =
                    l.getBlockState(new BlockPos(4, y + 13, 4)).is(Blocks.ORANGE_WOOL)
                        ? Blocks.RED_WOOL
                        : Blocks.ORANGE_WOOL;
                for (int z = 4; z <= 11; z++)
                  for (int x = 4; x <= 11; x++) {
                    for (int dy = 0; dy < 14; dy++) {
                      boolean frame = (x == 4 || x == 11) && (z == 4 || z == 11);
                      if (frame || dy == 0 || dy == 13)
                        l.setBlock(
                            new BlockPos(x, y + dy, z),
                            dy == 13
                                ? roof.defaultBlockState()
                                : Blocks.OAK_PLANKS.defaultBlockState(),
                            3);
                    }
                  }
              });
      stage = 2;
      ticks = 0;
      return;
    }
    if (stage == 2 && lod.snapshotRevision(0, 0) > oldRevision && ticks > 80) {
      record
          .append("LOD_EDIT_PASS before=")
          .append(oldRevision)
          .append(" after=")
          .append(lod.snapshotRevision(0, 0))
          .append('\n');
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var server = mc.getSingleplayerServer();
                server
                    .getCommands()
                    .performPrefixedCommand(
                        server.createCommandSourceStack(),
                        "tp @a 8 " + (ground + 28) + " 120 180 8");
              });
      stage = 3;
      ticks = 0;
      return;
    }
    if (stage == 3
        && ticks > 120
        && lod.visibleChunks > 0
        && lod.drawnTriangles > 0
        && (mc.level
                    .getChunkSource()
                    .getChunk(0, 0, net.minecraft.world.level.chunk.status.ChunkStatus.FULL, false)
                == null
            || mc.level
                    .getChunkSource()
                    .getChunk(0, 0, net.minecraft.world.level.chunk.status.ChunkStatus.FULL, false)
                instanceof net.minecraft.world.level.chunk.EmptyLevelChunk)) {
      capture(mc, "lod-unloaded-player-build");
      record
          .append("LOD_NATIVE_GPU_PASS captured=")
          .append(lod.capturedChunks)
          .append(" uploaded=")
          .append(lod.uploadedChunks)
          .append(" visible=")
          .append(lod.visibleChunks)
          .append(" triangles=")
          .append(lod.drawnTriangles)
          .append(" sourceChunkUnloaded=true residentBytes=")
          .append(lod.residentBytes)
          .append(" diskReads=")
          .append(lod.diskReads)
          .append('\n');
      if (Boolean.getBoolean("deepprimal.validationEngineReopen")) {
        // Drop only renderer state; restore the distant source page from its persisted snapshot.
        // This avoids mistaking a newly received native chunk for a disk restore.
        lod.reset();
        stage = 7;
        ticks = 0;
        return;
      }
      settings(mc);
      return;
    }
    if (stage == 7 && ticks > 20 && lod.diskReads > 0 && lod.snapshotRevision(0, 0) > 0) {
      record.append("LOD_DISK_RESTART_PASS cacheReloadAfterRestart=true\n");
      settings(mc);
      return;
    }

    if (stage == 4 && ticks > 12) {
      String[] tabs = {"General", "Quality", "Performance", "LOD", "Water", "Graphics API"};
      if (tabCapture < tabs.length) {
        button(mc, tabs[tabCapture]);
        stage = 5;
        ticks = 0;
        return;
      }
      try {
        record
            .append("INTEGRATED_CLIENT_PASS backend=")
            .append(com.mojang.blaze3d.systems.RenderSystem.getDevice().getBackendName())
            .append(" engineFluidCells=")
            .append(com.shnewbs.deepprimal.client.render.SmoothWaterRenderer.renderedCells)
            .append('\n');
        Files.writeString(
            Path.of(System.getProperty("deepprimal.validationCapture"))
                .resolve("client-result.txt"),
            record);
        System.out.print(record);
        stage = 6;
        mc.stop();
      } catch (Exception failure) {
        throw new RuntimeException(failure);
      }
    } else if (stage == 5 && ticks > 12) {
      capture(mc, "settings-" + tabCapture);
      tabCapture++;
      stage = 4;
      ticks = 0;
    }
  }

  private void settings(Minecraft mc) {
    LodConfig.RADIUS.set(173);
    ClientPerformanceConfig.TARGET_FPS.set(77);
    LodConfig.SPEC.save();
    ClientPerformanceConfig.SPEC.save();
    mc.options.hideGui = false;
    mc.setScreen(new PrimalGraphicsScreen(null));
    button(mc, "LOD");
    require(
        field(mc, "LOD radius (chunks)").getValue().equals("173"),
        "Saved settings initially displayed");
    field(mc, "LOD radius (chunks)").setValue("208");
    button(mc, "Apply");
    require(LodConfig.RADIUS.get() == 208, "Apply writes LOD setting");
    button(mc, "General");
    field(mc, "Target FPS").setValue("88");
    button(mc, "Apply");
    require(ClientPerformanceConfig.TARGET_FPS.get() == 88, "Apply writes performance setting");
    button(mc, "LOD");
    require(field(mc, "LOD radius (chunks)").getValue().equals("208"), "Tab restore");
    mc.setScreen(new PrimalGraphicsScreen(null));
    button(mc, "LOD");
    require(
        field(mc, "LOD radius (chunks)").getValue().equals("208"),
        "Screen reopen restores saved value");
    mc.screen.resize(340, 240);
    require(field(mc, "LOD radius (chunks)").getValue().equals("208"), "Resize preserves staging");
    mc.screen.resize(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    field(mc, "LOD radius (chunks)").setValue("209");
    button(mc, "Revert");
    require(
        field(mc, "LOD radius (chunks)").getValue().equals("208"), "Revert restores saved setting");
    if (!net.neoforged.fml.ModList.get().isLoaded("deepprimal_vulkan")) {
      try {
        RendererPreferences.save(RendererPreferences.Backend.Vulkan);
      } catch (java.io.IOException e) {
        throw new RuntimeException(e);
      }
      mc.setScreen(new PrimalGraphicsScreen(null));
      button(mc, "Graphics API");
      button(mc, "Vulkan");
      button(mc, "Apply");
      require(
          RendererPreferences.savedBackend() == RendererPreferences.Backend.OpenGL,
          "Lean installer can clear a stale saved Vulkan request");
      record.append("LEAN_BACKEND_PREFERENCE_RECOVERY_PASS\n");
    }
    record.append("SETTINGS_APPLY_REVERT_REOPEN_PASS radius=208 targetFps=88\n");
    stage = 4;
    ticks = 0;
    tabCapture = 0;
    return;
  }
}
