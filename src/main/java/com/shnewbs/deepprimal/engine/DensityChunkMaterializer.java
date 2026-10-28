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
    return fillPlannedRaw(graph, chunk, queriesPerSlab, true);
  }

  /**
   * Planned V8 terrain with the same surface/soil layering as the exact preview.
   * This overload remains explicit-call-only: it does not register a V8 codec,
   * change default generation, or rewrite existing worlds. Certified occupancy
   * can skip density evaluations; the already-materialized chunk supplies every
   * soil-layer solid/air decision without a second density-graph pass.
   *
   * <p>The raw section write and the subsequent topsoil overlay share one final
   * heightmap priming. This avoids publishing transient raw-stone heightmaps
   * before painting without affecting world-height/bedrock and cave checks.
   */
  public static DensityChunk.Planned fillPlannedSurface(
      GroundSurfaceProfile.Compiled profile, ChunkAccess chunk, int queriesPerSlab) {
    Objects.requireNonNull(profile, "profile");
    Objects.requireNonNull(chunk, "chunk");
    var origin = chunk.getPos();
    int minY = chunk.getMinY(), height = chunk.getHeight();
    // Finish sampling and resolve every required surface block BEFORE the first
    // native write. Density evaluation, graph channel lookup and material
    // registry access can fail on a malformed preview/profile or near the
    // coordinate boundary; a failed preflight must not leave a half-painted
    // chunk (or a partially updated heightmap) behind.
    var planned = DensityChunk.samplePlanned(profile.graph(), origin.getMinBlockX(),
        minY, origin.getMinBlockZ(), height, queriesPerSlab);
    var density = planned.chunk();
    var surfaces = new BlockState[DensityChunk.WIDTH * DensityChunk.WIDTH];
    boolean needsSoil = false;
    for (int z = 0; z < DensityChunk.WIDTH; z++)
      for (int x = 0; x < DensityChunk.WIDTH; x++) {
        int top = density.surfaceExclusive(x, z) - 1;
        if (top < minY + 2 || top == chunk.getMaxY()) continue;
        surfaces[z * DensityChunk.WIDTH + x] = Objects.requireNonNull(
            profile.surface(origin.getMinBlockX() + x, origin.getMinBlockZ() + z),
            "V8 surface material");
        if (profile.soilDepth() > 1 && top > minY + 2 && density.solid(x, top - 1, z))
          needsSoil = true;
      }
    BlockState soil = needsSoil ? Objects.requireNonNull(
        GroundMaterials.DIRT.get().defaultBlockState(), "V8 subsurface material") : null;

    // An explicit V8 preview can target an already populated ProtoChunk.
    // Save its original states as each unique cell is first touched. Native
    // setters and heightmap priming can fail *after* preflight succeeds; in
    // that event restore all touched cells in reverse order, then re-prime
    // maps from the restored blocks. No mutation reaches V1-V7 paths.
    var undo = new V8WriteUndo(chunk);
    try {
      // Avoid transient raw-stone heightmap updates before the final paint.
      writePlannedRaw(planned, chunk, false, undo);
      var pos = new BlockPos.MutableBlockPos();
      for (int z = 0; z < DensityChunk.WIDTH; z++)
        for (int x = 0; x < DensityChunk.WIDTH; x++) {
          BlockState surface = surfaces[z * DensityChunk.WIDTH + x];
          if (surface == null) continue;
          int top = density.surfaceExclusive(x, z) - 1;
          int worldX = origin.getMinBlockX() + x, worldZ = origin.getMinBlockZ() + z;
          for (int depth = 0; depth < profile.soilDepth() && top - depth >= minY + 2; depth++) {
            int y = top - depth;
            if (!density.solid(x, y, z)) break;
            undo.write(pos.set(worldX, y, worldZ), depth == 0 ? surface : soil);
          }
        }
      Heightmap.primeHeightmaps(chunk,
          EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
      return planned;
    } catch (RuntimeException | Error failure) {
      // In-memory block-state recovery is best effort. Native callbacks,
      // scheduled updates and persistent I/O are NOT transactional: a caller
      // must discard/recreate a failed chunk if a restoration fails or native
      // side effects escaped. Suppress recovery errors on the root cause.
      undo.rollback(failure);
      throw failure;
    }
  }

  /**
   * Materialize a versioned, seed-bound V8 snapshot prepared once for this world.
   * The level's vertical dimensions must match the saved snapshot before any
   * block or heightmap mutation; missing or incompatible profiles fail closed.
   * This explicit path does not register V8 or affect V1-V7 worlds.
   */
  public static DensityChunk.Planned fillSavedSnapshot(
      V8GeneratorSnapshot.Prepared prepared, ChunkAccess chunk, int queriesPerSlab) {
    Objects.requireNonNull(prepared, "prepared");
    Objects.requireNonNull(chunk, "chunk");
    prepared.requireHeight(chunk.getMinY(), chunk.getHeight());
    return fillPlannedSurface(prepared.surface(), chunk, queriesPerSlab);
  }

  private static DensityChunk.Planned fillPlannedRaw(
      DensityGraph graph, ChunkAccess chunk, int queriesPerSlab, boolean primeMaps) {
    Objects.requireNonNull(graph, "graph");
    Objects.requireNonNull(chunk, "chunk");
    int minY = chunk.getMinY(), height = chunk.getHeight();
    var origin = chunk.getPos();
    var planned = DensityChunk.samplePlanned(graph, origin.getMinBlockX(), minY,
        origin.getMinBlockZ(), height, queriesPerSlab);
    return writePlannedRaw(planned, chunk, primeMaps);
  }

  private static DensityChunk.Planned writePlannedRaw(
      DensityChunk.Planned planned, ChunkAccess chunk, boolean primeMaps) {
    return writePlannedRaw(planned, chunk, primeMaps, null);
  }

  private static DensityChunk.Planned writePlannedRaw(
      DensityChunk.Planned planned, ChunkAccess chunk, boolean primeMaps, V8WriteUndo undo) {
    int minY = chunk.getMinY();
    var origin = chunk.getPos();
    // The painted surface path primes the final map once after all writes.
    // Avoid redundant per-voxel unprimed map updates in that path.
    var floor = primeMaps ? chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG) : null;
    var surface = primeMaps ? chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG) : null;
    var pos = new BlockPos.MutableBlockPos();
    for (var section : DensitySectionLayout.project(planned.chunk())) {
      int baseY = section.baseY();
      for (int y = section.minCoveredY(); y < section.maxCoveredYExclusive(); y++) {
        int sectionY = y - baseY;
        for (int z = 0; z < DensityChunk.WIDTH; z++)
          for (int x = 0; x < DensityChunk.WIDTH; x++) {
            var state = material(section.solidLocal(x, sectionY, z), minY, y);
            pos.set(origin.getMinBlockX() + x, y, origin.getMinBlockZ() + z);
            if (undo == null) chunk.setBlockState(pos, state);
            else undo.write(pos, state);
            if (primeMaps && !state.isAir()) {
              floor.update(x, y, z, state);
              surface.update(x, y, z, state);
            }
          }
      }
    }
    if (primeMaps) Heightmap.primeHeightmaps(chunk,
        EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
    return planned;
  }

  /**
   * Bounded synchronous V8 block-write recovery. A cell is snapshotted only
   * once even when the soil pass repaints it. Raw writes cover at most
   * 256 * DensityChunk.MAX_HEIGHT cells; the journal is fixed-size, and no
   * neighbor, cache, filesystem or mutable shared output is accessed.
   *
   * <p>This is not a general Minecraft transaction. It repairs block-state
   * and derived heightmap values for recoverable setter exceptions, but it
   * cannot undo arbitrary world callbacks, lighting notifications or I/O.
   * Native callers must treat unrecoverable failures as discarded chunks.
   */
  private static final class V8WriteUndo {
    private final ChunkAccess chunk;
    private final int minY, originX, originZ;
    private final BlockState[] before;
    private final boolean[] seen;
    private final int[] touched;
    private int count;

    V8WriteUndo(ChunkAccess chunk) {
      this.chunk = chunk;
      this.minY = chunk.getMinY();
      this.originX = chunk.getPos().getMinBlockX();
      this.originZ = chunk.getPos().getMinBlockZ();
      int height = chunk.getHeight();
      if (height < 1 || height > DensityChunk.MAX_HEIGHT)
        throw new IllegalArgumentException("V8 undo height outside 1..2048");
      int length = DensityChunk.WIDTH * DensityChunk.WIDTH * height;
      this.before = new BlockState[length];
      this.seen = new boolean[length];
      this.touched = new int[length];
    }

    void write(BlockPos pos, BlockState state) {
      int x = pos.getX() - originX, y = pos.getY() - minY, z = pos.getZ() - originZ;
      if (x < 0 || x >= DensityChunk.WIDTH || z < 0 || z >= DensityChunk.WIDTH
          || y < 0 || y >= chunk.getHeight())
        throw new IllegalArgumentException("V8 write outside destination chunk");
      int index = (y * DensityChunk.WIDTH + z) * DensityChunk.WIDTH + x;
      if (!seen[index]) {
        // Snapshot before calling a setter which may mutate and then throw.
        before[index] = Objects.requireNonNull(chunk.getBlockState(pos),
            "original V8 destination block state");
        seen[index] = true;
        touched[count++] = index;
      }
      chunk.setBlockState(pos, state);
    }

    void rollback(Throwable failure) {
      if (count == 0) return; // A failed first read did not touch blocks or maps.
      var pos = new BlockPos.MutableBlockPos();
      for (int i = count - 1; i >= 0; i--) {
        int index = touched[i];
        int x = index % DensityChunk.WIDTH;
        int z = (index / DensityChunk.WIDTH) % DensityChunk.WIDTH;
        int y = index / (DensityChunk.WIDTH * DensityChunk.WIDTH) + minY;
        try {
          chunk.setBlockState(pos.set(originX + x, y, originZ + z), before[index]);
        } catch (RuntimeException | Error recoveryFailure) {
          if (recoveryFailure != failure) failure.addSuppressed(recoveryFailure);
        }
      }
      try {
        Heightmap.primeHeightmaps(chunk,
            EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
      } catch (RuntimeException | Error mapFailure) {
        if (mapFailure != failure) failure.addSuppressed(mapFailure);
      }
    }
  }

  /**
   * Read-only V8 base-height query, suitable for a future native generator's
   * WORLD_SURFACE_WG and OCEAN_FLOOR_WG paths. This experimental density material
   * uses only air, stone/deepslate and bedrock: both of those maps therefore
   * agree. It is not yet wired into the registered V1-V7 generator.
   *
   * <p>Search from the top to retain cave roofs and floating terrain. The lower
   * two cells are bedrock regardless of the density sign, even for an all-air
   * graph or a one-cell-tall test world. No chunk/world write or neighbor read.
   */
  public static int baseHeight(
      DensityGraph graph, int x, int z, LevelHeightAccessor level) {
    Objects.requireNonNull(graph, "graph");
    Objects.requireNonNull(level, "level");
    int minY = level.getMinY(), height = level.getHeight();
    checkColumnBounds(x, z, minY, height);
    var sampler = graph.newSampler();
    for (int dy = height - 1; dy >= 2; dy--) {
      int y = minY + dy;
      if (sampler.sample(x, y, z) > 0.0) return y + 1;
    }
    return minY + Math.min(2, height);
  }

  /** Constant-time base height from the already-certified exact V8 chunk. */
  public static int baseHeight(DensityChunk.Planned planned, int localX, int localZ) {
    Objects.requireNonNull(planned, "planned");
    DensityChunk chunk = Objects.requireNonNull(planned.chunk(), "planned.chunk");
    int height = chunk.maxYExclusive() - chunk.minY();
    return Math.max(chunk.minY() + Math.min(2, height),
        chunk.surfaceExclusive(localX, localZ));
  }

  /**
   * Read-only base column from a previously calculated V8 plan. Avoids a second
   * density-sampler pass when a native caller already owns the exact chunk.
   * Coordinates are chunk-local; bedrock and empty caves retain the same states
   * as {@link #fillPlanned} and {@link #baseColumn(DensityGraph,int,int,LevelHeightAccessor)}.
   */
  public static NoiseColumn baseColumn(DensityChunk.Planned planned, int localX, int localZ) {
    Objects.requireNonNull(planned, "planned");
    DensityChunk chunk = Objects.requireNonNull(planned.chunk(), "planned.chunk");
    int minY = chunk.minY(), height = chunk.maxYExclusive() - minY;
    var states = new BlockState[height];
    for (int dy = 0; dy < height; dy++) {
      int y = minY + dy;
      states[dy] = material(chunk.solid(localX, y, localZ), minY, y);
    }
    return new NoiseColumn(minY, states);
  }

  private static void checkColumnBounds(int x, int z, int minY, int height) {
    if (height < 1 || height > DensityChunk.MAX_HEIGHT)
      throw new IllegalArgumentException("V8 column height outside 1..2048");
    long maxY = (long) minY + height - 1;
    if (Math.abs((long) x) > DensityGraph.WORLD_LIMIT
        || Math.abs((long) z) > DensityGraph.WORLD_LIMIT
        || minY < -DensityGraph.WORLD_LIMIT || maxY > DensityGraph.WORLD_LIMIT)
      throw new IllegalArgumentException("V8 base column outside density graph domain");
  }

  public static NoiseColumn baseColumn(
      DensityGraph graph, int x, int z, LevelHeightAccessor level) {
    Objects.requireNonNull(graph, "graph");
    Objects.requireNonNull(level, "level");
    int minY = level.getMinY(), height = level.getHeight();
    checkColumnBounds(x, z, minY, height);
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
