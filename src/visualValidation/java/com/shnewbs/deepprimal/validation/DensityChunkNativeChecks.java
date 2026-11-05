package com.shnewbs.deepprimal.validation;

import com.shnewbs.deepprimal.core.density.DensityChunk;
import com.shnewbs.deepprimal.core.density.TerrainShapeV8;
import com.shnewbs.deepprimal.engine.DensityChunkMaterializer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.Heightmap;

/** Optional dedicated-server parity check for isolated V8 native chunk materialization. */
final class DensityChunkNativeChecks {
  static void run(ServerLevel level) {
    var graph = TerrainShapeV8.definition(false).compile(20261020L);
    var chunk = new ProtoChunk(
        new ChunkPos(-2, 3), UpgradeData.EMPTY, level,
        PalettedContainerFactory.create(level.registryAccess()), null);
    var density = DensityChunkMaterializer.fill(graph, chunk);
    var pos = new BlockPos.MutableBlockPos();
    long blockChecks = 0, columnChecks = 0;
    for (int z = 0; z < DensityChunk.WIDTH; z++)
      for (int x = 0; x < DensityChunk.WIDTH; x++) {
        int worldX = chunk.getPos().getMinBlockX() + x;
        int worldZ = chunk.getPos().getMinBlockZ() + z;
        var base = DensityChunkMaterializer.baseColumn(graph, worldX, worldZ, level);
        for (int y = chunk.getMinY(); y < chunk.getMaxY(); y++) {
          var state = chunk.getBlockState(pos.set(worldX, y, worldZ));
          if (!state.equals(base.getBlock(y))) throw new AssertionError("V8 base-column mismatch " + pos);
          if (y < chunk.getMinY() + 2) {
            if (!state.is(Blocks.BEDROCK)) throw new AssertionError("V8 bedrock barrier " + pos);
          } else if (state.isAir() == density.solid(x, y, z)) {
            throw new AssertionError("V8 density occupancy mismatch " + pos);
          }
          blockChecks++;
        }
        int expectedSurface = density.surfaceExclusive(x, z);
        if (chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, worldX, worldZ) + 1 != expectedSurface
            || chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, worldX, worldZ) + 1 != expectedSurface)
          throw new AssertionError("V8 heightmap mismatch at " + worldX + "," + worldZ);
        columnChecks++;
      }
    System.out.println("DENSITY_CHUNK_NATIVE_PASS blocks=" + blockChecks + " columns=" + columnChecks
        + " baseColumnParity=true occupancyExact=true bedrockBarrier=true heightmaps=true");
  }

  private DensityChunkNativeChecks() {}
}
