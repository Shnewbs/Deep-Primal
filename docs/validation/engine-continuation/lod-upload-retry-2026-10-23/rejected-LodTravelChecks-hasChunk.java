package com.shnewbs.deepprimal.validation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.shnewbs.deepprimal.core.lod.LodStore;
import com.shnewbs.deepprimal.lod.client.VoxelLodRenderer;
import com.shnewbs.deepprimal.settings.*;
import java.nio.file.*;
import java.util.concurrent.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Opt-in authoritative travel/edit probe; never included in the mod archive. */
final class LodTravelChecks {
  private static final BlockPos EDIT = new BlockPos(104, 280, -232);
  private static int phase = -1, ticks, samples;
  private static long phaseAt, readsBefore, capturesBefore, peakBytes, peakWater;
  private static boolean unloaded;
  private static CompletableFuture<?> serverWork;
  private static BlockState original;
  private static Path output;
  private static int oldMemory, oldUploads;

  static void start(Minecraft mc, Path dir) {
    if (phase != -1) throw new AssertionError("Travel audit already active");
    if (!RenderSystem.getDevice().getBackendName().equals("Vulkan"))
      throw new AssertionError("Vulkan required");
    output = dir; oldMemory = LodConfig.MEMORY.get(); oldUploads = LodConfig.UPLOADS.get();
    LodConfig.ENABLED.set(true); LodConfig.PERSIST.set(true); LodConfig.MEMORY.set(4);
    LodConfig.UPLOADS.set(4); ClientPerformanceConfig.PAUSE_BACKGROUND.set(false);
    WaterVisualConfig.INTERPOLATE.set(true); WaterVisualConfig.WAVES.set(false);
    readsBefore = VoxelLodRenderer.INSTANCE.diskReads;
    capturesBefore = VoxelLodRenderer.INSTANCE.capturedChunks;
    travel(mc, false); enter(0);
  }

  private static void enter(int next) {
    phase = next; phaseAt = System.nanoTime(); ticks = 0;
    System.out.println("LOD_TRAVEL_PHASE phase=" + next);
  }

  private static void travel(Minecraft mc, boolean away) {
    var server = mc.getSingleplayerServer();
    if (server == null) throw new AssertionError("Integrated server required");
    serverWork = CompletableFuture.runAsync(() -> {
      var source = server.createCommandSourceStack();
      for (String command : new String[] {"gamemode spectator @a", "time set noon", "weather clear",
          away ? "tp @a 616 300 -232 180 33" : "tp @a 104 300 -232 180 33"})
        server.getCommands().performPrefixedCommand(source, command);
    }, server);
    mc.setScreen(null);
  }

  private static boolean positioned(Minecraft mc, boolean away) {
    return serverWork.isDone() && !serverWork.isCompletedExceptionally()
        && Math.abs(mc.player.getX() - (away ? 616 : 104)) < 1
        && mc.level.hasChunk(away ? 38 : 6, -15);
  }

  private static String storedState() throws Exception {
    var f = VoxelLodRenderer.class.getDeclaredField("store"); f.setAccessible(true);
    var store = (LodStore) f.get(VoxelLodRenderer.INSTANCE);
    if (store == null) return "no-store";
    try { return store.read(6, -15).state(8, EDIT.getY(), 8); }
    catch (java.io.IOException missing) { return "not-yet-readable"; }
  }

  static void tick(Minecraft mc) throws Exception {
    if (phase < 0) return;
    ticks++; samples++;
    var lod = VoxelLodRenderer.INSTANCE;
    long water = com.shnewbs.deepprimal.client.render.AnimatedWaterRenderer.INSTANCE.residentBytes;
    peakBytes = Math.max(peakBytes, lod.residentBytes); peakWater = Math.max(peakWater, water);
    if (lod.residentBytes < 0 || lod.residentBytes > 4L * 1048576 || water < 0 || water > 32L * 1048576)
      throw new AssertionError("Actual scene payload budget violated: lod=" + lod.residentBytes + " water=" + water);
    if (System.nanoTime() - phaseAt > 90_000_000_000L)
      throw new AssertionError("LOD travel deadline phase=" + phase + " loaded="
          + mc.level.hasChunk(6, -15) + " revision=" + lod.snapshotRevision(6, -15)
          + " stored=" + storedState() + " bytes=" + lod.residentBytes + " queued=" + lod.queuedChunks);
    if (phase == 0 && positioned(mc, false) && ticks > 20) {
      var server = mc.getSingleplayerServer();
      serverWork = CompletableFuture.runAsync(() -> {
        var level = server.overworld();
        original = level.getBlockState(EDIT);
        if (!original.isAir()) throw new AssertionError("Edit site must be air");
        if (!level.setBlock(EDIT, Blocks.GLASS.defaultBlockState(), 3))
          throw new AssertionError("Authoritative glass edit failed");
      }, server);
      enter(1);
    } else if (phase == 1 && serverWork.isDone()) {
      serverWork.join();
      if (mc.level.getBlockState(EDIT).is(Blocks.GLASS) && ticks % 20 == 0
          && storedState().contains("minecraft:glass")) {
        System.out.println("LOD_TRAVEL_EDIT_PERSISTED revision=" + lod.snapshotRevision(6, -15));
        travel(mc, true); enter(2);
      }
    } else if (phase == 2 && positioned(mc, true) && !mc.level.hasChunk(6, -15)) {
      unloaded = true;
      System.out.println("LOD_TRAVEL_UNLOAD_OBSERVED targetClientChunkAbsent=true");
      enter(3);
    } else if (phase == 3 && ticks >= 200) {
      if (!storedState().contains("minecraft:glass")) throw new AssertionError("Edit lost while away");
      travel(mc, false); enter(4);
    } else if (phase == 4 && positioned(mc, false) && ticks > 100) {
      if (!mc.level.getBlockState(EDIT).is(Blocks.GLASS)) throw new AssertionError("Authoritative edit lost on return");
      if (ticks % 20 == 0 && storedState().contains("minecraft:glass") && lod.snapshotRevision(6, -15) >= 0) {
        Runnable cancel = StartupLifecycleChecks.watchPhase(output, "travel-resource-reload");
        serverWork = mc.reloadResourcePacks().whenComplete((ok, failed) -> cancel.run());
        enter(5);
      }
    } else if (phase == 5 && serverWork.isDone()) {
      serverWork.join();
      if (!mc.level.getBlockState(EDIT).is(Blocks.GLASS) || !storedState().contains("minecraft:glass"))
        throw new AssertionError("Edit lost across resource reload");
      var server = mc.getSingleplayerServer();
      serverWork = CompletableFuture.runAsync(() -> {
        if (!server.overworld().getBlockState(EDIT).is(Blocks.GLASS))
          throw new AssertionError("Server edit mismatch after return");
        server.overworld().setBlock(EDIT, original, 3);
      }, server);
      enter(6);
    } else if (phase == 6 && serverWork.isDone() && ticks % 20 == 0) {
      serverWork.join();
      if (mc.level.getBlockState(EDIT).equals(original) && !storedState().contains("minecraft:glass")) {
        String record = "LOD_TRAVEL_NATIVE_PASS backend=Vulkan authoritativeTeleports=3 clientUnload=" + unloaded
            + " serverBlockEdits=2 diskStateMatches=true reload=true samples=" + samples
            + " peakLodBytes=" + peakBytes + " peakWaterBytes=" + peakWater
            + " diskReadsDelta=" + (lod.diskReads - readsBefore)
            + " capturesDelta=" + (lod.capturedChunks - capturesBefore)
            + " scope=authoritativeTravelAndEditDataNotCompleteVisualCoverage\n";
        Files.writeString(output.resolve("lod-travel-pass.txt"), record); System.out.print(record);
        LodConfig.MEMORY.set(oldMemory); LodConfig.UPLOADS.set(oldUploads); phase = -1;
      }
    }
  }
  private LodTravelChecks() {}
}
