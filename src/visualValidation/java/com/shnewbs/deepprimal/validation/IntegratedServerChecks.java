package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.lod.*;
import com.shnewbs.deepprimal.engine.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Native integration fixture: excluded from release JAR. */
@Mod(value = "deepprimal", dist = Dist.DEDICATED_SERVER)
public final class IntegratedServerChecks {
  private boolean started, done;

  public IntegratedServerChecks() {
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void start(net.neoforged.neoforge.event.server.ServerStartedEvent e) {
    started = true;
  }

  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  @SubscribeEvent
  public void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e) {
    String mode = System.getenv("DEEPPRIMAL_ENGINE_CHECKS");
    if (mode == null || !started || done) return;
    done = true;
    var server = e.getServer();
    var level = server.overworld();
    Path out = Path.of(System.getenv("DEEPPRIMAL_ENGINE_RECORD"));
    try {
      if (mode.equals("legacy")) {
        require(
            level.getChunkSource().getGenerator()
                instanceof net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator,
            "Legacy saved generator preserved");
        require(
            level.getMinY() == -128 && level.getHeight() == 768, "Legacy saved height preserved");
        Files.writeString(out, "LEGACY_REOPEN_PASS generatorRetained=true heightRetained=true\n");
        System.out.print(Files.readString(out));
        server.halt(false);
        return;
      }
      require(
          level.getChunkSource().getGenerator() instanceof EngineChunkGenerator,
          "Default world uses our chunk generator");
      var generator = (EngineChunkGenerator) level.getChunkSource().getGenerator();
      require(generator.getBiomeSource() instanceof RegionBiomeSource, "Independent biome source");
      require(
          net.neoforged.fml.ModList.get().isLoaded("deepprimal_graphics")
              && net.neoforged.fml.ModList.get().isLoaded("deepprimal_performance"),
          "Original modules retained");
      var stored = new BlockPos(80, 500, 8);
      if (mode.startsWith("reopen")) {
        require(
            level.getMinY() == (mode.equals("reopen_deep") ? -384 : -128)
                && level.getHeight() == (mode.equals("reopen_deep") ? 1024 : 768),
            "Saved custom generator height survives creation-config change");
        var state = level.getBlockState(stored);
        require(
            state.is(EngineRegistry.WATER_BLOCK.get())
                && state.getValue(EngineFluid.UNITS) == 7
                && state.getValue(EngineFluid.FLOW) == 3,
            "Fluid saved/reopened");
        var saved =
            new LodStore(
                    server
                        .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                        .resolve("deepprimal-validation-lod"))
                .read(5, 0);
        require(saved.state(0, 500, 8).contains("units=7"), "LOD snapshot reopened");
        Files.writeString(
            out,
            "INTEGRATED_REOPEN_PASS ownGenerator=true minY="
                + level.getMinY()
                + " height="
                + level.getHeight()
                + " units=7 flow=3 snapshot=true\n");
      } else {
        long startedAt = System.nanoTime();
        for (int z = -1; z <= 1; z++)
          for (int x = -1; x <= 1; x++) {
            var chunk = level.getChunk(x, z);
            require(
                chunk.getMinY() == (mode.equals("deep") ? -384 : -128)
                    && chunk.getHeight() == (mode.equals("deep") ? 1024 : 768),
                "Saved dimension bounds");
            for (int zz = 0; zz < 16; zz += 5)
              for (int xx = 0; xx < 16; xx += 5)
                require(
                    chunk
                        .getBlockState(new BlockPos(x * 16 + xx, chunk.getMinY(), z * 16 + zz))
                        .is(Blocks.BEDROCK),
                    "Solid floor");
          }
        double generationMs = (System.nanoTime() - startedAt) / 1e6;
        for (int z = 3; z <= 13; z++)
          for (int x = 75; x <= 85; x++) {
            level.setBlock(new BlockPos(x, 499, z), Blocks.STONE.defaultBlockState(), 2);
            level.setBlock(
                new BlockPos(x, 500, z),
                (x == 75 || x == 85 || z == 3 || z == 13)
                    ? Blocks.STONE.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(),
                2);
          }
        level.setBlock(
            new BlockPos(78, 500, 8), EngineRegistry.WATER_BLOCK.get().defaultBlockState(), 2);
        for (int i = 0; i < 25; i++)
          for (int z = 4; z < 13; z++)
            for (int x = 76; x < 85; x++) WaterRuntime.tick(level, new BlockPos(x, 500, z));
        int total = 0;
        for (int z = 4; z < 13; z++)
          for (int x = 76; x < 85; x++) total += WaterRuntime.units(level, new BlockPos(x, 500, z));
        require(total == 16, "Native transport conserves volume");
        var imported = new BlockPos(76, 500, 4);
        require(WaterRuntime.units(level, imported) == 0, "Native import fixture starts empty");
        var nativeState =
            Blocks.WATER
                .defaultBlockState()
                .setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL, 4);
        boolean legacyFinite = com.shnewbs.deepprimal.water.RiverConfig.FINITE_VOLUME.get();
        com.shnewbs.deepprimal.water.RiverConfig.FINITE_VOLUME.set(true);
        try {
          level.setBlock(imported, nativeState, 2);
          ((net.minecraft.world.level.material.FlowingFluid)
                  net.minecraft.world.level.material.Fluids.FLOWING_WATER)
              .tick(level, imported, nativeState, nativeState.getFluidState());
        } finally {
          com.shnewbs.deepprimal.water.RiverConfig.FINITE_VOLUME.set(legacyFinite);
        }
        int afterImport = 0;
        for (int z = 4; z < 13; z++)
          for (int x = 76; x < 85; x++)
            afterImport += WaterRuntime.units(level, new BlockPos(x, 500, z));
        require(
            afterImport == 24,
            "Native source conversion bypasses legacy solver and preserves eight imported units");
        // Freeze one measured cell so persistence is checked independently of scheduled transport.
        for (var d : net.minecraft.core.Direction.values())
          level.setBlock(stored.relative(d), Blocks.STONE.defaultBlockState(), 2);
        level.setBlock(
            stored,
            EngineRegistry.WATER_BLOCK
                .get()
                .defaultBlockState()
                .setValue(EngineFluid.UNITS, 7)
                .setValue(EngineFluid.FLOW, 3),
            2);
        var chunk = level.getChunk(5, 0);
        var builder = new ColumnarChunk.Builder(5, 0, chunk.getMinY(), chunk.getHeight(), 1);
        var ids = new HashMap<net.minecraft.world.level.block.state.BlockState, Integer>();
        var palette = new ArrayList<String>();
        for (int z = 0; z < 16; z++)
          for (int x = 0; x < 16; x++) {
            int[] column = new int[chunk.getHeight()];
            for (int y = 0; y < column.length; y++) {
              var state = chunk.getBlockState(new BlockPos(80 + x, chunk.getMinY() + y, z));
              column[y] =
                  ids.computeIfAbsent(
                      state,
                      s -> {
                        palette.add((s.isAir() ? "00000000|" : "ff777777|") + s);
                        return palette.size() - 1;
                      });
            }
            builder.column(column);
          }
        var page = builder.finish(palette);
        var store =
            new LodStore(
                server
                    .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                    .resolve("deepprimal-validation-lod"));
        store.write(page);
        require(
            store.read(5, 0).state(0, 500, 8).contains("units=7"), "Actual native chunk snapshot");
        var mesh = LodMesher.build(page);
        require(mesh.bytes() > 0, "Native voxel reduction");
        server.saveAllChunks(false, true, true);
        Files.writeString(
            out,
            "INTEGRATED_NATIVE_PASS ownGenerator=true ownBiomes=true modulesRetained=true"
                + " loadedNineChunksMs="
                + generationMs
                + " minY="
                + level.getMinY()
                + " height="
                + level.getHeight()
                + " nativeImportWithLegacyEnabled=true waterUnits="
                + total
                + " lodBytes="
                + mesh.bytes()
                + "\n");
      }
      var source = server.createCommandSourceStack();
      server.getCommands().performPrefixedCommand(source, "deepprimal info");
      server.getCommands().performPrefixedCommand(source, "deepprimal sample 0 0");
      System.out.print(Files.readString(out));
      server.halt(false);
    } catch (Throwable failure) {
      try {
        Files.writeString(out, "INTEGRATED_NATIVE_FAIL " + failure + "\n");
      } catch (Exception ignored) {
      }
      throw new RuntimeException(failure);
    }
  }
}
