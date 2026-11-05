package com.shnewbs.deepprimal.validation;

import com.mojang.serialization.JsonOps;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.TerrainShapeV8;
import com.shnewbs.deepprimal.engine.DensityChunkMaterializer;
import com.shnewbs.deepprimal.engine.DensityGraphCodec;
import com.shnewbs.deepprimal.engine.GroundMaterials;
import com.shnewbs.deepprimal.engine.GroundSurfaceProfile;
import java.nio.file.Files;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;

final class GroundSurfaceNativeChecks {
  private static final List<GroundSurfaceProfile.Material> MATERIALS =
      List.of(GroundSurfaceProfile.Material.DIRT,
          GroundSurfaceProfile.Material.GRASS, GroundSurfaceProfile.Material.PODZOL);

  static void run(ServerLevel level, boolean reopen)
      throws java.io.IOException, java.security.NoSuchAlgorithmException {
    GroundPackageChecks.verify(
        GroundMaterials.class, GroundSurfaceProfile.class, DensityChunkMaterializer.class);
    GroundSurfaceProfileChecks.run();
    var profile = new GroundSurfaceProfile(
        1, MATERIALS, List.of(.35, .7), .04, 96, Long.MAX_VALUE, 4, "eroded", 0, 1);
    var file = level.getServer().getWorldPath(LevelResource.ROOT)
        .resolve("ground-surface-validation-profile.json");
    if (reopen) {
      var saved = GroundSurfaceProfile.CODEC.parse(JsonOps.INSTANCE,
          com.google.gson.JsonParser.parseString(Files.readString(file))).getOrThrow();
      require(saved.equals(profile), "Saved surface snapshot identity");
      profile = saved;
    } else {
      Files.writeString(file, GroundSurfaceProfile.CODEC.encodeStart(JsonOps.INSTANCE, profile)
          .getOrThrow().toString());
    }
    long blocks = 0;
    for (long seed : new long[] {20261020, Long.MIN_VALUE}) {
      var graphFile = file.resolveSibling("ground-density-validation-" + seed + ".json");
      DensityGraph.Definition definition;
      if (reopen) {
        definition = DensityGraphCodec.CODEC.parse(JsonOps.INSTANCE,
            com.google.gson.JsonParser.parseString(Files.readString(graphFile))).getOrThrow();
      } else {
        definition = TerrainShapeV8.definition(false);
        Files.writeString(graphFile,
            DensityGraphCodec.CODEC.encodeStart(JsonOps.INSTANCE, definition).getOrThrow().toString());
      }
      var graph = definition.compile(seed);
      var compiled = profile.compile(seed, graph);
      var digest = java.security.MessageDigest.getInstance("SHA-256");
      var stateBytes = new java.util.IdentityHashMap<net.minecraft.world.level.block.state.BlockState, byte[]>();
      var positions = List.of(new ChunkPos(-1, -1), new ChunkPos(0, -1));
      var forward = new java.util.ArrayList<ProtoChunk>();
      for (var position : positions) {
        var chunk = chunk(level, position);
        var density = DensityChunkMaterializer.fill(compiled, chunk);
        var cursor = new BlockPos.MutableBlockPos();
        for (int z = 0; z < 16; z++)
          for (int x = 0; x < 16; x++) {
            int worldX = position.getMinBlockX() + x, worldZ = position.getMinBlockZ() + z;
            var column = DensityChunkMaterializer.baseColumn(compiled, worldX, worldZ, level);
            for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
              var state = chunk.getBlockState(cursor.set(worldX, y, worldZ));
              digest.update(stateBytes.computeIfAbsent(state,
                  s -> s.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
              digest.update((byte) 0);
              require(state.equals(column.getBlock(y)), "Surface base-column parity " + cursor);
              if (y < level.getMinY() + 2) {
                require(state.is(Blocks.BEDROCK), "Surface bedrock floor " + cursor);
              } else {
                require(state.isAir() != density.solid(x, y, z), "Surface occupancy " + cursor);
              }
              blocks++;
            }
            int expected = Math.max(level.getMinY() + 2, density.surfaceExclusive(x, z));
            require(chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, worldX, worldZ) + 1 == expected
                && chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, worldX, worldZ) + 1 == expected,
                "Coated heightmaps");
          }
        forward.add(chunk);
      }
      String fingerprint = java.util.HexFormat.of().formatHex(digest.digest());
      var digestFile = graphFile.resolveSibling("ground-surface-validation-" + seed + ".sha256");
      if (reopen) {
        require(Files.readString(digestFile).equals(fingerprint), "Saved graph/surface block replay");
      } else {
        Files.writeString(digestFile, fingerprint);
      }
      System.out.println("GROUND_SURFACE_FINGERPRINT seed=" + seed + " sha256=" + fingerprint
          + " reopened=" + reopen);
      for (int i = positions.size() - 1; i >= 0; i--) {
        var reversed = chunk(level, positions.get(i));
        DensityChunkMaterializer.fill(compiled, reversed);
        var expected = forward.get(i);
        var cursor = new BlockPos.MutableBlockPos();
        for (int z = 0; z < 16; z++)
          for (int x = 0; x < 16; x++)
            for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
              cursor.set(positions.get(i).getMinBlockX() + x, y, positions.get(i).getMinBlockZ() + z);
              require(reversed.getBlockState(cursor).equals(expected.getBlockState(cursor)),
                  "Generation-order surface replay " + cursor);
              blocks++;
            }
      }
    }

    var slab = slab().compile(1);
    var thin = new GroundSurfaceProfile(1, MATERIALS, List.of(.35, .7), 0, 96, 1, 16, "y", -1, 1);
    var coated = chunk(level, new ChunkPos(-1, 0));
    DensityChunkMaterializer.fill(thin.compile(1, slab), coated);
    require(coated.getBlockState(new BlockPos(-1, 73, 0)).is(GroundMaterials.GRASS.get()),
        "Thin roof surface material");
    require(coated.getBlockState(new BlockPos(-1, 72, 0)).is(GroundMaterials.DIRT.get())
        && coated.getBlockState(new BlockPos(-1, 71, 0)).is(GroundMaterials.DIRT.get()),
        "Thin connected soil layer");
    for (int y = 60; y <= 70; y++)
      require(coated.getBlockState(new BlockPos(-1, y, 0)).isAir(), "Roof air gap remains air");
    require(coated.getBlockState(new BlockPos(-1, 59, 0)).is(Blocks.STONE),
        "No soil leaked through cave gap");
    var slabColumn = DensityChunkMaterializer.baseColumn(thin.compile(1, slab), -1, 0, level);
    for (int y = 58; y <= 75; y++)
      require(coated.getBlockState(new BlockPos(-1, y, 0)).equals(slabColumn.getBlock(y)),
          "Thin-roof column parity");
    for (double constant : new double[] {-1, 1}) {
      var graph = new DensityGraph.Definition(1, "out",
          List.of(DensityGraph.Node.constant("out", constant))).compile(1);
      var uniform = new GroundSurfaceProfile(
          1, MATERIALS, List.of(.35, .7), 0, 96, 1, 16, "out", -2, 2).compile(1, graph);
      var chunk = chunk(level, new ChunkPos(0, 0));
      DensityChunkMaterializer.fill(uniform, chunk);
      var column = DensityChunkMaterializer.baseColumn(uniform, 0, 0, level);
      for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
        var state = chunk.getBlockState(new BlockPos(0, y, 0));
        require(state.equals(column.getBlock(y)), "Uniform column parity at " + y
            + " density=" + constant + " actual=" + state + " expected=" + column.getBlock(y));
        require(!state.is(GroundMaterials.DIRT.get()) && !state.is(GroundMaterials.GRASS.get())
            && !state.is(GroundMaterials.PODZOL.get()), "No soil on floor or truncated ceiling");
      }
    }
    try {
      var graph = slab().compile(1);
      profile.compile(1, graph);
      throw new AssertionError("Missing channel accepted");
    } catch (IllegalArgumentException expected) {
      // Profiles may only sample a named channel present in their density graph.
    }
    System.out.println("GROUND_SURFACE_NATIVE_PASS blocks=" + blocks
        + " seeds=2 adjacentChunks=2 reverseOrder=true baseColumns=true heightmaps=true"
        + " thinRoof=true caveGap=true bedrock=true ceiling=true snapshotReopen=" + reopen);
  }

  private static DensityGraph.Definition slab() {
    return new DensityGraph.Definition(1, "out", List.of(
        DensityGraph.Node.operation("y", DensityGraph.Op.Y),
        DensityGraph.Node.constant("negative", -1),
        DensityGraph.Node.operation("negative_y", DensityGraph.Op.MULTIPLY, "y", "negative"),
        DensityGraph.Node.constant("roof", 74),
        DensityGraph.Node.operation("below_roof", DensityGraph.Op.ADD, "roof", "negative_y"),
        DensityGraph.Node.constant("bottom", -70),
        DensityGraph.Node.operation("above_bottom", DensityGraph.Op.ADD, "y", "bottom"),
        DensityGraph.Node.operation("slab", DensityGraph.Op.MIN, "below_roof", "above_bottom"),
        DensityGraph.Node.constant("floor", 60),
        DensityGraph.Node.operation("below_floor", DensityGraph.Op.ADD, "floor", "negative_y"),
        DensityGraph.Node.operation("out", DensityGraph.Op.MAX, "slab", "below_floor")));
  }

  private static ProtoChunk chunk(ServerLevel level, ChunkPos position) {
    return new ProtoChunk(position, UpgradeData.EMPTY, level,
        PalettedContainerFactory.create(level.registryAccess()), null);
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  private GroundSurfaceNativeChecks() {}
}
