package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer;
import com.shnewbs.deepprimal.client.render.HorizonRenderer;
import com.shnewbs.deepprimal.settings.*;
import java.nio.file.*;
import net.minecraft.client.Minecraft;

/** Opt-in scene isolation and observations; these records do not assert complete coverage. */
final class EnabledWaterViewChecks {
  static boolean handle(Minecraft mc, String action, Path output) throws Exception {
    if (action.startsWith("water-view=")) {
      var values = action.substring(11).split(",", -1);
      if (values.length != 3) throw new IllegalArgumentException("animated,waves,lod required");
      for (String value : values)
        if (!value.equals("true") && !value.equals("false"))
          throw new IllegalArgumentException("Explicit boolean required");
      WaterVisualConfig.INTERPOLATE.set(Boolean.parseBoolean(values[0]));
      WaterVisualConfig.WAVES.set(Boolean.parseBoolean(values[1]));
      LodConfig.ENABLED.set(Boolean.parseBoolean(values[2]));
      ClientPerformanceConfig.PAUSE_BACKGROUND.set(false);
      mc.setScreen(null);
      System.out.println("WATER_VIEW_OPTIONS animated=" + values[0] + " waves=" + values[1]
          + " lod=" + values[2]);
      return true;
    }
    if (action.equals("audit-lake-camera")) {
      var server = mc.getSingleplayerServer();
      if (server == null) throw new AssertionError("Integrated audit world required");
      server.execute(() -> {
        var source = server.createCommandSourceStack();
        for (String command : new String[] {"gamemode spectator @a", "time set noon",
            "weather clear", "tp @a 107.68827612381756 176.5875 -226.797007486035 180 33.02386755579665"})
          server.getCommands().performPrefixedCommand(source, command);
      });
      mc.setScreen(null);
      return true;
    }
    if (action.startsWith("water-view-observe=")) {
      String label = action.substring(19);
      if (!label.matches("[a-z0-9-]{1,48}")) throw new IllegalArgumentException("Observation label");
      var water = AnimatedWaterRenderer.INSTANCE;
      if (water.residentBytes < 0 || water.residentBytes > 32L * 1024 * 1024)
        throw new AssertionError("Water payload exceeds existing budget");
      var horizon = HorizonRenderer.INSTANCE;
      var lod = com.shnewbs.deepprimal.lod.client.VoxelLodRenderer.INSTANCE;
      String record = "WATER_VIEW_OBSERVATION label=" + label
          + " backend=" + RenderSystem.getDevice().getBackendName()
          + " animated=" + WaterVisualConfig.INTERPOLATE.get()
          + " waves=" + WaterVisualConfig.WAVES.get() + " lod=" + LodConfig.ENABLED.get()
          + " ownsWater=" + AnimatedWaterRenderer.ownsWater()
          + " waterBytes=" + water.residentBytes + " drawnPages=" + water.drawnPages
          + " pending=" + water.pending() + " loadedChunks=" + mc.level.getChunkSource().getLoadedChunksCount()
          + " horizonTiles=" + horizon.cachedTiles + " visibleTiles=" + horizon.visibleTiles
          + " lodActive=" + com.shnewbs.deepprimal.lod.client.VoxelLodRenderer.active()
          + " lodBytes=" + lod.residentBytes + " lodVisible=" + lod.visibleChunks
          + " lodCaptured=" + lod.capturedChunks + " lodTriangles=" + lod.drawnTriangles
          + " camera=" + mc.player.getX() + "," + mc.player.getY() + "," + mc.player.getZ()
          + "\n";
      Files.writeString(output.resolve(label + "-observation.txt"), record);
      System.out.print(record);
      return true;
    }
    return false;
  }

  private EnabledWaterViewChecks() {}
}
