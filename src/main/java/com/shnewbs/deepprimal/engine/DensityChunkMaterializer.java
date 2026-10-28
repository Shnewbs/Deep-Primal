package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.density.DensityChunk;
import com.shnewbs.deepprimal.core.density.DensityGraph;
import com.shnewbs.deepprimal.core.density.DensitySectionLayout;
import java.util.EnumSet;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Isolated native adapter for exact V8 occupancy; not registered as a world generator. */
public final class DensityChunkMaterializer {
  /** Explicit material-preview path; keeps air, density signs and the bedrock floor unchanged. */
  public static DensityChunk fill(GroundSurfaceProfile.Compiled profile, ChunkAccess chunk) {
    Objects.requireNonNull(profile, "profile");
    var density = fill(profile.graph(), chunk);
    var origin = chunk.getPos();
    var pos = new BlockPos.MutableBlockPos();
    for (int z = 0; z < DensityChunk.WIDTH; z++)
      for (int x = 0; x < DensityChunk.WIDTH; x++) {
        int top = density.surfaceExclusive(x, z) - 1;
        if (top < chunk.getMinY() + 2 || top == chunk.getMaxY()) continue;
        int worldX = origin.getMinBlockX() + x, worldZ = origin.getMinBlockZ() + z;
        var surface = profile.surface(worldX, worldZ);
        for (int depth = 0; depth < profile.soilDepth() && top - depth >= chunk.getMinY() + 2; depth++) {
          int y = top - depth;
          if (!density.solid(x, y, z)) break;
          chunk.setBlockState(pos.set(worldX, y, worldZ),
              depth == 0 ? surface : GroundMaterials.DIRT.get().defaultBlockState());
        }
      }
    Heightmap.primeHeightmaps(
        chunk, EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
    return density;
  }

  public static DensityChunk fill(DensityGraph graph, ChunkAccess chunk) {
    Objects.requireNonNull(graph, "graph");
    Objects.requireNonNull(chunk, "chunk");
    int minY = chunk.getMinY(), height = chunk.getHeight();
    var origin = chunk.getPos();
    var density = DensityChunk.sample(graph, origin.getMinBlockX(), minY, origin.getMinBlockZ(), height);
    var floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
    var surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
    var pos = new BlockPos.MutableBlockPos();
    for (int z = 0; z < DensityChunk.WIDTH; z++)
      for (int x = 0; x < DensityChunk.WIDTH; x++)
        for (int y = minY; y < minY + height; y++) {
          var state = material(density.solid(x, y, z), minY, y);
          chunk.setBlockState(pos.set(origin.getMinBlockX() + x, y, origin.getMinBlockZ() + z), state);
          if (!state.isAir()) {
            floor.update(x, y, z, state);
            surface.update(x, y, z, state);
          }
        }
    Heightmap.primeHeightmaps(
        chunk, EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
    return density;
  }

  /**
   * Explicitly opted-in V8 native-section bridge. The active V1-V7 generators
   * continue to use their existing path; this does not register a V8 codec.
   *
   * <p>The planned chunk is exact: certified regions skip density evaluations,
   * while every uncertain voxel is sampled. Every section position is written,
   * including air and the two-block bedrock floor, so reusing a nonempty test
   * chunk cannot leave stale blocks. The section layout's signed-Y and palette
   * ordering are authoritative for the write cursor; it does not read or force
   * neighbor chunks. Heightmaps are primed after every block is materialized.
   * The returned statistics count density evaluations, not native write cost.
   */
  public static DensityChunk.Planned fillPlanned(
      DensityGraph graph, ChunkAccess chunk, int queriesPerSlab) {
    Objects.requireNonNull(graph, "graph");
    Objects.requireNonNull(chunk, "chunk");
    int minY = chunk.getMinY(), height = chunk.getHeight();
    var origin = chunk.getPos();
    var planned = DensityChunk.samplePlanned(graph, origin.getMinBlockX(), minY,
        origin.getMinBlockZ(), height, queriesPerSlab);
    var floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
    var surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
    var pos = new BlockPos.MutableBlockPos();
    for (var section : DensitySectionLayout.project(planned.chunk())) {
      int baseY = section.baseY();
      for (int y = section.minCoveredY(); y < section.maxCoveredYExclusive(); y++) {
        int sectionY = y - baseY;
        for (int z = 0; z < DensityChunk.WIDTH; z++)
          for (int x = 0; x < DensityChunk.WIDTH; x++) {
            var state = material(section.solidLocal(x, sectionY, z), minY, y);
            chunk.setBlockState(pos.set(origin.getMinBlockX() + x, y,
                origin.getMinBlockZ() + z), state);
            if (!state.isAir()) {
              floor.update(x, y, z, state);
              surface.update(x, y, z, state);
            }
          }
      }
    }
    Heightmap.primeHeightmaps(chunk,
        EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
    return planned;
  }

  public static NoiseColumn baseColumn(
      DensityGraph graph, int x, int z, LevelHeightAccessor level) {
    Objects.requireNonNull(graph, "graph");
    Objects.requireNonNull(level, "level");
    int minY = level.getMinY(), height = level.getHeight();
    var states = new BlockState[height];
    var sampler = graph.newSampler();
    for (int dy = 0; dy < height; dy++) {
      int y = minY + dy;
      states[dy] = material(sampler.sample(x, y, z) > 0.0, minY, y);
    }
    return new NoiseColumn(minY, states);
  }

  public static NoiseColumn baseColumn(
      GroundSurfaceProfile.Compiled profile, int x, int z, LevelHeightAccessor level) {
    Objects.requireNonNull(profile, "profile");
    var base = baseColumn(profile.graph(), x, z, level);
    int minY = level.getMinY(), height = level.getHeight();
    var states = new BlockState[height];
    int top = -1;
    for (int dy = 0; dy < height; dy++) {
      states[dy] = base.getBlock(minY + dy);
      if (!states[dy].isAir()) top = dy;
    }
    if (top >= 2 && top < height - 1) {
      var surface = profile.surface(x, z);
      for (int depth = 0; depth < profile.soilDepth() && top - depth >= 2; depth++) {
        int dy = top - depth;
        if (states[dy].isAir()) break;
        states[dy] = depth == 0 ? surface : GroundMaterials.DIRT.get().defaultBlockState();
      }
    }
    return new NoiseColumn(minY, states);
  }

  private static BlockState material(boolean solid, int minY, int y) {
    if (y < minY + 2) return Blocks.BEDROCK.defaultBlockState();
    if (!solid) return Blocks.AIR.defaultBlockState();
    return (y < 0 ? Blocks.DEEPSLATE : Blocks.STONE).defaultBlockState();
  }

  private DensityChunkMaterializer() {}
}
