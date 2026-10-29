package com.shnewbs.deepprimal.engine;

import com.shnewbs.deepprimal.core.biome.EcotoneSurfaceField;
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

    // Snapshot the entire destination BEFORE any native setter. A setter
    // callback can change a future V8 cell before the raw pass reaches it;
    // lazy first-touch snapshots would preserve that callback's mutation as
    // the "original" value on rollback. Only the explicit V8 path pays for
    // the bounded prewrite snapshot; V1-V7 paths are unchanged.
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

  /**
   * Explicit, opt-in V8 revision-3 native surface pass for NEW chunks only.
   * Legacy V8 fillSavedSnapshot and V1-V7 generators remain bit-for-bit
   * unchanged. A caller must supply a deterministic absolute-coordinate
   * climate/hydrology producer bound to the same saved world; this method does
   * not register a generator, rewrite existing chunks or create finite water.
   *
   * All climate/material preflight finishes before the first block write.
   * The complete prewrite snapshot/rollback is the existing dev53 mechanism;
   * this is not a transaction for lighting, neighboring chunks or disk I/O.
   * Vegetation-density/patch samples remain read-only decisions for a later
   * native feature pass; no unapproved plants/items are generated here.
   */
  public static V8EcotoneResult fillSavedEcotone(
      V8GeneratorSnapshot.Prepared prepared, ChunkAccess chunk, int queriesPerSlab,
      V8EcotoneColumnPlan.ClimateSource climate) {
    return fillSavedEcotoneInternal(prepared, chunk, queriesPerSlab, climate, null).ecotone();
  }

  /**
   * NEW V8-r6 only: paint already-planned grass and ferns in the same V8
   * raw/soil/heightmap rollback transaction, using a persisted vegetation policy.
   * The paired reference and routed-water authority must match the exact saved
   * world seed, density, climate, and hydrology provenance before any writes.
   * This is explicit-call-only and does not register a native V8 generator.
   */
  public static V8VegetatedResult fillSavedEcotoneVegetated(
      V8GeneratorSnapshot.Prepared prepared, ChunkAccess chunk, int queriesPerSlab,
      V8EcotoneColumnPlan.ClimateSource referenceClimate,
      V8WaterDomainAdmission.Authority authority) {
    Objects.requireNonNull(prepared, "prepared");
    if (prepared.snapshot().version() != V8GeneratorSnapshot.VEGETATION_VERSION)
      throw new IllegalArgumentException("explicit saved V8 revision 6 vegetation required");
    var admitted = V8WaterDomainAdmission.bind(prepared, referenceClimate, authority);
    var written = fillSavedEcotoneInternal(prepared, chunk, queriesPerSlab,
        admitted, prepared.snapshot().vegetationPolicy());
    return new V8VegetatedResult(written.ecotone(), written.vegetation());
  }

  /** Same V8 surface-plan contract for non-writing base-column/vegetation queries. */
  public static V8VegetatedResult planSavedEcotoneVegetated(
      V8GeneratorSnapshot.Prepared prepared, int originWorldX, int originWorldZ,
      int minY, int height, int queriesPerSlab,
      V8EcotoneColumnPlan.ClimateSource referenceClimate,
      V8WaterDomainAdmission.Authority authority) {
    Objects.requireNonNull(prepared, "prepared");
    if (prepared.snapshot().version() != V8GeneratorSnapshot.VEGETATION_VERSION)
      throw new IllegalArgumentException("explicit saved V8 revision 6 vegetation required");
    var preflight = planSavedEcotoneAdmitted(prepared, originWorldX,
        originWorldZ, minY, height, queriesPerSlab, referenceClimate, authority);
    var plants = V8VegetationColumnPlan.build(preflight.columns(), originWorldX,
        originWorldZ, minY, height, prepared.snapshot().seed(),
        prepared.snapshot().vegetationPolicy());
    return new V8VegetatedResult(preflight, plants);
  }

  public record V8VegetatedResult(V8EcotoneResult ecotone,
                                  V8VegetationColumnPlan vegetation) {
    public V8VegetatedResult {
      Objects.requireNonNull(ecotone, "V8 ecotone plan");
      Objects.requireNonNull(vegetation, "V8 vegetation plan");
    }
  }

  private record MaterializedEcotone(V8EcotoneResult ecotone,
                                     V8VegetationColumnPlan vegetation) {}

  private static MaterializedEcotone fillSavedEcotoneInternal(
      V8GeneratorSnapshot.Prepared prepared, ChunkAccess chunk, int queriesPerSlab,
      V8EcotoneColumnPlan.ClimateSource climate, V8VegetationPolicy vegetationPolicy) {
    Objects.requireNonNull(prepared, "prepared");
    Objects.requireNonNull(chunk, "chunk");
    int minY = chunk.getMinY();
    var origin = chunk.getPos();
    // Use the exact same independent, pre-generation plan as base-column
    // queries. Climate, geometry and all cover choices are resolved before
    // native block/heightmap mutation, even if a callback subsequently fails.
    var preflight = planSavedEcotone(prepared, origin.getMinBlockX(),
        origin.getMinBlockZ(), minY, chunk.getHeight(), queriesPerSlab, climate);
    var planned = preflight.terrain();
    var columns = preflight.columns();
    // No second climate or density callback: plants use the exact same planned
    // 16x16 cover columns as native soil writes and independent base queries.
    var plants = vegetationPolicy == null ? null : V8VegetationColumnPlan.build(
        columns, origin.getMinBlockX(), origin.getMinBlockZ(), minY,
        chunk.getHeight(), prepared.snapshot().seed(), vegetationPolicy);

    // Resolve every material before the first mutation: registry failures and
    // hydrology/climate callback failures must leave blocks/heightmaps intact.
    BlockState bare = null, grass = null, podzol = null, snow = null, soil = null;
    for (int z = 0; z < DensityChunk.WIDTH; z++)
      for (int x = 0; x < DensityChunk.WIDTH; x++) {
        var column = columns.column(x, z);
        if (column == null) continue; // river, ocean, air, and bedrock stay raw
        // Snow completely replaces the visible cover. Once snow is cached,
        // do not accidentally resolve an unused grass/podzol/bare registry
        // value on later snow columns (the dev58 else binding did exactly that).
        if (column.cover().snowPatch()) {
          if (snow == null)
            snow = Objects.requireNonNull(Blocks.SNOW_BLOCK.defaultBlockState(), "V8 snow");
        } else switch (column.cover().cover()) {
          case BARE -> { if (bare == null) bare = Objects.requireNonNull(
              GroundMaterials.DIRT.get().defaultBlockState(), "V8 bare surface"); }
          case GRASS -> { if (grass == null) grass = Objects.requireNonNull(
              GroundMaterials.GRASS.get().defaultBlockState(), "V8 grass surface"); }
          case PODZOL -> { if (podzol == null) podzol = Objects.requireNonNull(
              GroundMaterials.PODZOL.get().defaultBlockState(), "V8 podzol surface"); }
        }
        if (column.soilDepth() > 1 && soil == null)
          soil = Objects.requireNonNull(GroundMaterials.DIRT.get().defaultBlockState(), "V8 soil");
      }

    // Resolve all possible feature states before the prewrite snapshot/transaction.
    BlockState fern = null, shortGrass = null;
    if (plants != null) for (int z = 0; z < DensityChunk.WIDTH; z++)
      for (int x = 0; x < DensityChunk.WIDTH; x++) {
        var place = plants.placement(x, z);
        if (place == null) continue;
        if (place.plant() == V8VegetationColumnPlan.Plant.FERN && fern == null)
          fern = Objects.requireNonNull(Blocks.FERN.defaultBlockState(), "V8 fern");
        if (place.plant() == V8VegetationColumnPlan.Plant.SHORT_GRASS && shortGrass == null)
          shortGrass = Objects.requireNonNull(Blocks.SHORT_GRASS.defaultBlockState(), "V8 grass");
      }

    var undo = new V8WriteUndo(chunk);
    try {
      writePlannedRaw(planned, chunk, false, undo);
      var pos = new BlockPos.MutableBlockPos();
      for (int z = 0; z < DensityChunk.WIDTH; z++)
        for (int x = 0; x < DensityChunk.WIDTH; x++) {
          var column = columns.column(x, z);
          if (column == null) continue;
          var cover = column.cover();
          BlockState topState = cover.snowPatch() ? snow : switch (cover.cover()) {
            case BARE -> bare;
            case GRASS -> grass;
            case PODZOL -> podzol;
          };
          int worldX = origin.getMinBlockX() + x, worldZ = origin.getMinBlockZ() + z;
          for (int depth = 0; depth < column.soilDepth()
              && column.topY() - depth >= minY + 2; depth++) {
            int y = column.topY() - depth;
            if (!planned.chunk().solid(x, y, z)) break;
            undo.write(pos.set(worldX, y, worldZ), depth == 0 ? topState : soil);
          }
        }
      // In r6 this pass shares the existing complete prewrite rollback. Never
      // displace another state: callbacks and native water may veto a feature.
      if (plants != null) for (int z = 0; z < DensityChunk.WIDTH; z++)
        for (int x = 0; x < DensityChunk.WIDTH; x++) {
          var place = plants.placement(x, z);
          if (place == null) continue;
          var at = pos.set(origin.getMinBlockX() + x,
              place.y(), origin.getMinBlockZ() + z);
          if (chunk.getBlockState(at).isAir())
            undo.write(at, place.plant() == V8VegetationColumnPlan.Plant.FERN
                ? fern : shortGrass);
        }
      Heightmap.primeHeightmaps(chunk,
          EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
      return new MaterializedEcotone(preflight, plants);
    } catch (RuntimeException | Error failure) {
      undo.rollback(failure);
      throw failure;
    }
  }

  /**
   * Opt-in V8-r4 materialization with an explicitly geometry/seed/climate-bound
   * hydrology observation. All V8 water-mask callbacks are evaluated during
   * preflight, before the native write/heightmap transaction begins. Existing
   * V8-r3/r4 entry points remain byte-for-byte unchanged.
   *
   * <p>The authority is a caller-supplied contract, NOT yet a proven persisted
   * V8 routed-water mask. It neither creates finite water nor edits saved chunks.
   */
  public static V8EcotoneResult fillSavedEcotoneAdmitted(
      V8GeneratorSnapshot.Prepared prepared, ChunkAccess chunk, int queriesPerSlab,
      V8EcotoneColumnPlan.ClimateSource referenceClimate,
      V8WaterDomainAdmission.Authority authority) {
    var admitted = V8WaterDomainAdmission.bind(prepared, referenceClimate, authority);
    return fillSavedEcotone(prepared, chunk, queriesPerSlab, admitted);
  }

  /**
   * Construct the exact materialized V8 revision-3 terrain plan without a
   * native ChunkAccess. Callers may query {@link #baseColumn(V8EcotoneResult,
   * int, int)} before creating a chunk, then hand the same saved snapshot and
   * absolute-coordinate climate producer to {@link #fillSavedEcotone}.
   *
   * <p>This API is explicit-call-only and has a bounded 16x16 footprint; it
   * neither registers V8 as a world generator nor mutates terrain, saved
   * profiles, player edits, hydrology or finite-water constituents. A native
   * caller must still pin the climate producer and policy to the saved world
   * to make independent query/write parity meaningful.
   */
  public static V8EcotoneResult planSavedEcotone(
      V8GeneratorSnapshot.Prepared prepared, int originWorldX, int originWorldZ,
      int minY, int height, int queriesPerSlab,
      V8EcotoneColumnPlan.ClimateSource climate) {
    Objects.requireNonNull(prepared, "prepared");
    Objects.requireNonNull(climate, "climate");
    if (prepared.snapshot().version() < V8GeneratorSnapshot.SURFACE_VERSION
        || prepared.snapshot().version() > V8GeneratorSnapshot.VEGETATION_VERSION)
      throw new IllegalArgumentException("explicit V8 ecotone planning requires revision 3");
    prepared.requireHeight(minY, height);
    if (Math.floorMod(originWorldX, DensityChunk.WIDTH) != 0
        || Math.floorMod(originWorldZ, DensityChunk.WIDTH) != 0)
      throw new IllegalArgumentException("V8 planned terrain origin must be chunk-aligned");
    var planned = DensityChunk.samplePlanned(prepared.surface().graph(),
        originWorldX, minY, originWorldZ, height, queriesPerSlab);
    var columns = V8EcotoneColumnPlan.build(prepared, planned.chunk(),
        originWorldX, originWorldZ, climate);
    return new V8EcotoneResult(planned, columns);
  }

  /**
   * Pre-generation V8-r4 geometry and material query using exactly the same
   * dual-water-mask admission used by {@link #fillSavedEcotoneAdmitted}.
   */
  public static V8EcotoneResult planSavedEcotoneAdmitted(
      V8GeneratorSnapshot.Prepared prepared, int originWorldX, int originWorldZ,
      int minY, int height, int queriesPerSlab,
      V8EcotoneColumnPlan.ClimateSource referenceClimate,
      V8WaterDomainAdmission.Authority authority) {
    var admitted = V8WaterDomainAdmission.bind(prepared, referenceClimate, authority);
    return planSavedEcotone(prepared, originWorldX, originWorldZ, minY, height,
        queriesPerSlab, admitted);
  }

  /** The vegetation sample remains immutable and read-only until feature gates pass. */
  public record V8EcotoneResult(DensityChunk.Planned terrain,
                                V8EcotoneColumnPlan columns) {
    public V8EcotoneResult {
      Objects.requireNonNull(terrain, "terrain");
      Objects.requireNonNull(columns, "columns");
    }
  }

  /**
   * Read-only exact base-column projection of the completed opt-in V8 ecotone
   * material plan. Unlike the generic raw V8 baseColumn this uses the SAME
   * frozen surface samples, snow cover and soil-layer limits as native writes.
   * It never reads neighboring chunks or changes a saved profile.
   *
   * <p>The plan may now be built before native generation using
   * {@link #planSavedEcotone}; actual world-owned climate/hydrology wiring and
   * persisted generator registration are still separate acceptance gates.
   */
  public static NoiseColumn baseColumn(V8EcotoneResult result, int localX, int localZ) {
    Objects.requireNonNull(result, "result");
    var raw = baseColumn(result.terrain(), localX, localZ);
    var column = result.columns().column(localX, localZ);
    if (column == null) return raw;
    var density = result.terrain().chunk();
    int minY = density.minY(), height = density.maxYExclusive() - minY;
    var states = new BlockState[height];
    for (int y = minY; y < density.maxYExclusive(); y++)
      states[y - minY] = raw.getBlock(y);
    BlockState topState = column.cover().snowPatch()
        ? Blocks.SNOW_BLOCK.defaultBlockState() : switch (column.cover().cover()) {
      case BARE -> GroundMaterials.DIRT.get().defaultBlockState();
      case GRASS -> GroundMaterials.GRASS.get().defaultBlockState();
      case PODZOL -> GroundMaterials.PODZOL.get().defaultBlockState();
    };
    BlockState soil = column.soilDepth() > 1
        ? GroundMaterials.DIRT.get().defaultBlockState() : null;
    for (int depth = 0; depth < column.soilDepth()
        && column.topY() - depth >= minY + 2; depth++) {
      int y = column.topY() - depth;
      if (!density.solid(localX, y, localZ)) break;
      states[y - minY] = depth == 0 ? topState : soil;
    }
    return new NoiseColumn(minY, states);
  }

  /** Native-planned base column with the r6 vegetation placements projected. */
  public static NoiseColumn baseColumnVegetated(V8VegetatedResult result,
      int localX, int localZ) {
    Objects.requireNonNull(result, "vegetated V8 plan");
    var base = baseColumn(result.ecotone(), localX, localZ);
    var plant = result.vegetation().placement(localX, localZ);
    if (plant == null) return base;
    var density = result.ecotone().terrain().chunk();
    int minY = density.minY(), maxY = density.maxYExclusive();
    BlockState[] states = new BlockState[maxY - minY];
    for (int y = minY; y < maxY; y++) states[y - minY] = base.getBlock(y);
    if (plant.y() >= minY && plant.y() < maxY
        && states[plant.y() - minY].isAir())
      states[plant.y() - minY] = (plant.plant() == V8VegetationColumnPlan.Plant.FERN
          ? Blocks.FERN : Blocks.SHORT_GRASS).defaultBlockState();
    return new NoiseColumn(minY, states);
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
   * Bounded prewrite V8 recovery. Capture the entire destination *before*
   * the first setter, including cells a synchronous native callback might
   * edit ahead of the planned write cursor. Rollback scans the full snapshot
   * so callback-only cells are restored as well as directly written cells.
   *
   * <p>Memory is one array of at most 256 * DensityChunk.MAX_HEIGHT block
   * references. The extra prewrite reads and failure-path full scan require
   * real native performance acceptance before this opt-in path is enabled.
   * Cross-chunk callbacks, asynchronous events, lighting, block entities and
   * I/O are not transactional. A failed native caller must still abandon its
   * chunk; a suppressed rollback exception is an additional failure signal.
   */
  private static final class V8WriteUndo {
    private final ChunkAccess chunk;
    private final int minY, height, originX, originZ;
    private final BlockState[] before;
    private boolean started;

    V8WriteUndo(ChunkAccess chunk) {
      this.chunk = chunk;
      this.minY = chunk.getMinY();
      this.originX = chunk.getPos().getMinBlockX();
      this.originZ = chunk.getPos().getMinBlockZ();
      this.height = chunk.getHeight();
      if (height < 1 || height > DensityChunk.MAX_HEIGHT)
        throw new IllegalArgumentException("V8 undo height outside 1..2048");
      this.before = new BlockState[DensityChunk.WIDTH * DensityChunk.WIDTH * height];
      var pos = new BlockPos.MutableBlockPos();
      // A snapshot read failure happens before any write, so neither blocks
      // nor heightmaps require recovery. No neighbor chunk is consulted.
      for (int dy = 0; dy < height; dy++)
        for (int z = 0; z < DensityChunk.WIDTH; z++)
          for (int x = 0; x < DensityChunk.WIDTH; x++) {
            int index = (dy * DensityChunk.WIDTH + z) * DensityChunk.WIDTH + x;
            before[index] = Objects.requireNonNull(chunk.getBlockState(
                pos.set(originX + x, minY + dy, originZ + z)),
                "original V8 destination block state");
          }
    }

    void write(BlockPos pos, BlockState state) {
      int x = pos.getX() - originX, y = pos.getY() - minY, z = pos.getZ() - originZ;
      if (x < 0 || x >= DensityChunk.WIDTH || z < 0 || z >= DensityChunk.WIDTH
          || y < 0 || y >= height)
        throw new IllegalArgumentException("V8 write outside destination chunk");
      started = true; // The native setter may mutate, then throw.
      chunk.setBlockState(pos, state);
    }

    void rollback(Throwable failure) {
      if (!started) return;
      var pos = new BlockPos.MutableBlockPos();
      int restoreFailures = 0;
      for (int index = before.length - 1; index >= 0; index--) {
        int x = index % DensityChunk.WIDTH;
        int z = (index / DensityChunk.WIDTH) % DensityChunk.WIDTH;
        int y = index / (DensityChunk.WIDTH * DensityChunk.WIDTH) + minY;
        try {
          pos.set(originX + x, y, originZ + z);
          // Avoid re-triggering native callbacks on cells still unchanged.
          if (!Objects.equals(chunk.getBlockState(pos), before[index]))
            chunk.setBlockState(pos, before[index]);
        } catch (RuntimeException | Error recoveryFailure) {
          restoreFailures++;
          if (restoreFailures <= 8 && recoveryFailure != failure)
            failure.addSuppressed(recoveryFailure);
        }
      }
      try {
        Heightmap.primeHeightmaps(chunk,
            EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
      } catch (RuntimeException | Error mapFailure) {
        if (mapFailure != failure) failure.addSuppressed(mapFailure);
      }
      // Synchronous callbacks during recovery can themselves change another
      // cell. Verify all cells, and report incomplete recovery to the caller
      // instead of silently announcing rollback success.
      int mismatched = 0;
      for (int index = 0; index < before.length; index++) {
        int x = index % DensityChunk.WIDTH;
        int z = (index / DensityChunk.WIDTH) % DensityChunk.WIDTH;
        int y = index / (DensityChunk.WIDTH * DensityChunk.WIDTH) + minY;
        try {
          if (!Objects.equals(chunk.getBlockState(
              pos.set(originX + x, y, originZ + z)), before[index])) mismatched++;
        } catch (RuntimeException | Error readFailure) {
          mismatched++;
          if (mismatched <= 8 && readFailure != failure)
            failure.addSuppressed(readFailure);
        }
      }
      if (mismatched != 0 || restoreFailures != 0)
        failure.addSuppressed(new IllegalStateException(
            "V8 rollback incomplete: " + mismatched + " changed/unreadable cells, "
                + restoreFailures + " restoration failures; discard native chunk"));
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
