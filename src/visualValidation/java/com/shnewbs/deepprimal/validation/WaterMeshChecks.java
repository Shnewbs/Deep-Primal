package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.engine.*;
import com.shnewbs.deepprimal.mixin.client.LevelRendererAccessor;
import com.shnewbs.deepprimal.settings.*;
import com.shnewbs.deepprimal.settings.mixin.client.CompileTaskAccess;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.chunk.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;

/** Native mesh progress under every-frame water invalidation; deliberately visual-only volumes. */
@Mod(value = "deepprimal", dist = Dist.CLIENT)
public final class WaterMeshChecks {
  private int stage, ticks, frames, meshChanges;
  private boolean channelCodecsChecked;
  private long began, coalesced;
  private Object mesh;
  private net.minecraft.client.gui.screens.Screen warning;
  private volatile boolean prepared;
  private static final int Y = 512;

  public WaterMeshChecks() {
    NeoForge.EVENT_BUS.register(this);
    if (Boolean.getBoolean("deepprimal.validationWaterShape"))
      System.out.println("WATER_SHAPE_CONTROLLER_READY filling=" + Boolean.getBoolean("deepprimal.validationFilling"));
  }

  private static void require(boolean b, String m) {
    if (!b) throw new AssertionError(m);
  }

  private SectionRenderDispatcher.RenderSection section(Minecraft mc) {
    var view = ((LevelRendererAccessor) mc.levelRenderer).deepprimal$viewArea();
    if (view == null) return null;
    for (var s : view.sections) if (s.getSectionNode() == SectionPos.asLong(0, Y >> 4, 0)) return s;
    return null;
  }

  @SubscribeEvent
  public void tick(ClientTickEvent.Post event) {
    if (!Boolean.getBoolean("deepprimal.validationFilling")) return;
    if (!channelCodecsChecked) {
      OrganicChannelCodecChecks.main(new String[0]);
      channelCodecsChecked = true;
    }
    var mc = Minecraft.getInstance();
    if (Boolean.getBoolean("deepprimal.validationWaterShape")) {
      WaterShapeChecks.tick(mc);
      return;
    }
    if (Boolean.getBoolean("deepprimal.validationOceanWater")) {
      OceanWaterChecks.tick(mc);
      return;
    }
    if (Boolean.getBoolean("deepprimal.validationAnimatedWater")) {
      AnimatedWaterChecks.tick(mc);
      return;
    }
    if (began == 0) began = System.nanoTime();
    require(
        System.nanoTime() - began < 240_000_000_000L,
        "Water mesh validation timeout stage=" + stage);
    if (mc.level == null || mc.player == null) {
      if (mc.screen != warning
          && (mc.screen
                  instanceof
                  net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen
              || mc.screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen))
        for (var child : mc.screen.children())
          if (child instanceof Button b
              && (b.getMessage().getString().equals("Proceed")
                  || b.getMessage().getString().equals("I know what I'm doing!"))) {
            warning = mc.screen;
            b.onPress(null);
            break;
          }
      return;
    }
    if (Boolean.getBoolean("deepprimal.validationGuiOrientation")) {
      GuiOrientationChecks.tick(mc);
      return;
    }
    ticks++;
    if (stage == 0) {
      WaterVisualConfig.INTERPOLATE.set(false);
      if ("Vulkan".equals(System.getProperty("deepprimal.graphicsBackend")))
        require(
            RenderSystem.getDevice().getBackendName().equals("Vulkan"),
            "Requested Vulkan actually runs");
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(false);
      ClientPerformanceConfig.ADAPTIVE_WORK.set(false);
      LodConfig.ENABLED.set(false);
      mc.options.prioritizeChunkUpdates().set(PrioritizeChunkUpdates.NONE);
      mc.options.hideGui = true;
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var server = mc.getSingleplayerServer();
                var src = server.createCommandSourceStack();
                for (String cmd :
                    new String[] {
                      "gamemode spectator @a",
                      "time set noon",
                      "weather clear",
                      "tp @a 8 521 22 180 28"
                    }) server.getCommands().performPrefixedCommand(src, cmd);
                prepared = true;
              });
      stage = 1;
      ticks = 0;
      return;
    }
    if (stage == 1 && prepared && ticks > 80 && mc.level.hasChunk(0, 0) && section(mc) != null) {
      // Client-only fixture isolates rendering from network batching and authoritative fluid tests.
      for (int z = 0; z < 16; z++)
        for (int x = 0; x < 16; x++) {
          mc.level.setBlock(new BlockPos(x, Y - 1, z), Blocks.STONE.defaultBlockState(), 2 | 16);
          for (int y = Y; y < Y + 5; y++)
            mc.level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2 | 16);
          if (x > 1 && x < 14 && z > 1 && z < 14)
            mc.level.setBlock(
                new BlockPos(x, Y, z),
                EngineRegistry.WATER_BLOCK.get().defaultBlockState(),
                2 | 16);
        }
      stage = 2;
      ticks = 0;
      return;
    }
    if (stage == 2 && ticks > 80 && section(mc).getSectionMesh().hasTranslucentGeometry()) {
      var s = section(mc);
      var cache = new RenderRegionCache();
      var task = s.createCompileTask(cache);
      var access = (CompileTaskAccess) task;
      s.setDirty(false);
      long before = VisualPerformance.chunkRebuildsCoalesced;
      s.rebuildSectionAsync(cache);
      s.setNotDirty();
      require(
          s.isDirty()
              && !access.deepprimal$cancelled().get()
              && VisualPerformance.chunkRebuildsCoalesced == before + 1,
          "In-flight work and newer dirtiness survive coalescing");
      s.setDirty(true);
      s.rebuildSectionAsync(cache);
      s.setNotDirty();
      require(
          access.deepprimal$cancelled().get() && !s.isDirty(), "Player edits bypass coalescing");
      coalesced = VisualPerformance.chunkRebuildsCoalesced;
      mesh = s.getSectionMesh();
      stage = 3;
      ticks = 0;
    }
    if (stage == 3 && frames >= 160 && meshChanges >= 3) {
      stage = 4;
      ticks = 0;
    }
    if (stage == 4 && ticks > 30) {
      var s = section(mc);
      require(!s.isDirty(), "Latest changes eventually compile after invalidation stops");
      try {
        Path out = Path.of(System.getProperty("deepprimal.validationCapture"));
        Files.createDirectories(out);
        String validation = "notApplicable";
        if (RenderSystem.getDevice().getBackendName().equals("Vulkan")) {
          var stats =
              (java.util.Map<?, ?>)
                  Class.forName("net.vulkanmod.vulkan.VulkanStatistics")
                      .getMethod("snapshot")
                      .invoke(null);
          require(
              ((Number) stats.get("validationErrors")).intValue() == 0,
              "No Vulkan validation errors");
          validation = "0";
        }
        String result =
            "WATER_MESH_NATIVE_PASS backend="
                + RenderSystem.getDevice().getBackendName()
                + " continuousFrames="
                + frames
                + " meshChanges="
                + meshChanges
                + " coalesced="
                + (VisualPerformance.chunkRebuildsCoalesced - coalesced)
                + " controlledCoalesces=1 dirtyRetained=true playerBypass=true latestCompiled=true"
                + " validationErrors="
                + validation
                + "\n";
        Files.writeString(out.resolve("water-mesh-result.txt"), result);
        System.out.print(result);
        Screenshot.grab(
            out.toFile(), "water-mesh.png", mc.getMainRenderTarget(), 1, m -> mc.execute(mc::stop));
        stage = 5;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @SubscribeEvent
  public void frame(RenderFrameEvent.Post event) {
    if (Boolean.getBoolean("deepprimal.validationAnimatedWater")) {
      AnimatedWaterChecks.frame();
      return;
    }
    if (!Boolean.getBoolean("deepprimal.validationFilling") || stage != 3) return;
    var mc = Minecraft.getInstance();
    var s = section(mc);
    if (s == null) return;
    Object now = s.getSectionMesh();
    if (now != mesh) {
      meshChanges++;
      mesh = now;
    }
    int amount = 1 + (frames++ % 16);
    for (int z = 2; z < 14; z++)
      for (int x = 2; x < 14; x++)
        mc.level.setBlock(
            new BlockPos(x, Y, z),
            EngineRegistry.WATER_BLOCK
                .get()
                .defaultBlockState()
                .setValue(EngineFluid.UNITS, amount),
            2 | 16);
    s.setDirty(false);
  }
}
