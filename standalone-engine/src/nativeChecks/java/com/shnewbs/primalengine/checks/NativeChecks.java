package com.shnewbs.primalengine.checks;

import com.shnewbs.primalengine.*;
import java.nio.file.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Packaged standalone tests. These classes are excluded from the release JAR. */
@Mod(value = PrimalEngine.ID, dist = Dist.DEDICATED_SERVER)
public final class NativeChecks {
  private static final StringBuilder JOURNAL = new StringBuilder();

  public NativeChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  private boolean started, done;

  @SubscribeEvent
  public void start(net.neoforged.neoforge.event.server.ServerStartedEvent e) {
    started = true;
  }

  @SubscribeEvent
  public void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e) {
    String mode = System.getenv("PRIMAL_ENGINE_CHECKS");
    if (mode == null || !started || done) return;
    done = true;
    var server = e.getServer();
    var level = server.overworld();
    try {
      require(
          !ModList.get().isLoaded("deepprimal") && !ModList.get().isLoaded("deepprimal_graphics"),
          "Legacy mod dependency");
      require(
          level.getChunkSource().getGenerator() instanceof ContinentalGenerator, "Wrong generator");
      var g = (ContinentalGenerator) level.getChunkSource().getGenerator();
      var stored = new BlockPos(80, 500, 8);
      if (mode.equals("reopen")) {
        var state = level.getBlockState(stored);
        require(
            state.is(PrimalEngine.WATER_BLOCK.get())
                && state.getValue(EngineFluid.UNITS) == 7
                && state.getValue(EngineFluid.FLOW) == 3,
            "Saved fluid data");
        var storedLod =
            new com.shnewbs.primalengine.lod.LodStore(
                    server
                        .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                        .resolve("primalengine-lod/checks"))
                .read(5, 0);
        require(
            storedLod.state(0, 500, 8).contains("units=7"),
            "Native chunk LOD did not survive reopen");
        record("ENGINE_REOPEN_PASS units=7 flow=3 legacyModsAbsent=true");
      } else {
        long before = System.nanoTime();
        for (int z = -1; z <= 1; z++)
          for (int x = -1; x <= 1; x++) {
            var chunk = level.getChunk(x, z);
            for (int zz = 0; zz < 16; zz += 5)
              for (int xx = 0; xx < 16; xx += 5) {
                var p = new BlockPos(x * 16 + xx, level.getMinY(), z * 16 + zz);
                require(level.getBlockState(p).is(Blocks.BEDROCK), "Missing solid floor");
              }
          }
        var base = new BlockPos(64, 500, 0);
        level.getChunkAt(base);
        level.getChunkAt(stored);
        for (int y = 0; y < 7; y++)
          for (int z = 0; z < 3; z++)
            for (int x = 0; x < 7; x++)
              level.setBlock(
                  base.offset(x, y, z),
                  (y == 0 || x == 0 || x == 6 || z == 0 || z == 2 ? Blocks.STONE : Blocks.AIR)
                      .defaultBlockState(),
                  2 | 16);
        level.setBlock(
            base.offset(2, 5, 1), PrimalEngine.WATER_BLOCK.get().defaultBlockState(), 2 | 16);
        for (int n = 0; n < 20; n++)
          for (int y = 1; y < 7; y++)
            for (int x = 1; x < 6; x++) WaterRuntime.tick(level, base.offset(x, y, 1));
        int volume = 0;
        for (int y = 1; y < 7; y++)
          for (int x = 1; x < 6; x++) volume += WaterRuntime.units(level, base.offset(x, y, 1));
        require(volume == 16, "Native volume lost/created");
        require(WaterRuntime.units(level, base.offset(2, 5, 1)) == 0, "Gravity missing");
        var imported = base.offset(4, 5, 1);
        var nativeState =
            Blocks.WATER
                .defaultBlockState()
                .setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL, 4);
        level.setBlock(imported, nativeState, 2 | 16);
        ((net.minecraft.world.level.material.FlowingFluid)
                net.minecraft.world.level.material.Fluids.FLOWING_WATER)
            .tick(level, imported, nativeState, nativeState.getFluidState());
        int importedTotal = 0;
        for (int yy = 1; yy < 7; yy++)
          for (int xx = 1; xx < 6; xx++)
            importedTotal += WaterRuntime.units(level, base.offset(xx, yy, 1));
        require(importedTotal == 24, "Native input conversion volume");
        for (var d : net.minecraft.core.Direction.values())
          level.setBlock(stored.relative(d), Blocks.STONE.defaultBlockState(), 2 | 16);
        level.setBlock(
            stored,
            PrimalEngine.WATER_BLOCK
                .get()
                .defaultBlockState()
                .setValue(EngineFluid.UNITS, 7)
                .setValue(EngineFluid.FLOW, 3),
            2 | 16);
        require(
            PrimalEngine.WATER_BLOCK
                .get()
                .pickupBlock(null, level, stored, level.getBlockState(stored))
                .isEmpty(),
            "Partial bucket duplicated volume");
        // Stable empty/full bucket exchange uses the engine's own item and state.
        var full = base.offset(3, 4, 1);
        level.setBlock(full, PrimalEngine.WATER_BLOCK.get().defaultBlockState(), 2 | 16);
        var bucket =
            PrimalEngine.WATER_BLOCK
                .get()
                .pickupBlock(null, level, full, level.getBlockState(full));
        require(
            bucket.is(PrimalEngine.BUCKET.get()) && level.getBlockState(full).isAir(),
            "Bucket exchange");
        var capture = new ChunkCapture(level.getChunkAt(stored), 1);
        while (!capture.step(2048)) {}
        var snapshot = capture.finish();
        var lod =
            new com.shnewbs.primalengine.lod.LodStore(
                server
                    .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                    .resolve("primalengine-lod/checks"));
        lod.write(snapshot);
        require(
            lod.read(5, 0).state(0, 500, 8).contains("units=7"),
            "Native voxel snapshot lost water metadata");
        record(
            "ENGINE_LOD_CAPTURE_PASS actualChunk=true units7=true boundedReads2048=true"
                + " memoryEstimate="
                + snapshot.estimatedBytes());
        record(
            "ENGINE_NATIVE_PASS customGenerator=true nineChunks=true bedrock=true volume=16"
                + " gravity=true nativeInputConversion=true fullBucket=true partialBucketSafe=true"
                + " legacyModsAbsent=true milliseconds="
                + (System.nanoTime() - before) / 1_000_000);
      }
    } catch (Throwable failure) {
      record("ENGINE_NATIVE_FAILURE " + failure);
      failure.printStackTrace();
    } finally {
      server.halt(false);
    }
  }

  private static void record(String s) {
    System.out.println(s);
    try {
      Path p = Path.of(System.getenv("PRIMAL_ENGINE_RECORD"));
      Files.createDirectories(p.getParent());
      JOURNAL.append(s).append('\n');
      Path pending = p.resolveSibling(p.getFileName() + ".pending");
      Files.writeString(pending, JOURNAL);
      Files.move(pending, p, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private static void require(boolean ok, String why) {
    if (!ok) throw new AssertionError(why);
  }
}
