package com.shnewbs.deepprimal.core.density;

import java.util.BitSet;
import java.util.Objects;

/** Exact, bounded solid occupancy for one chunk column footprint. Not a Minecraft adapter. */
public final class DensityChunk {
  public static final int WIDTH = 16, MAX_HEIGHT = 2048;
  private static final int COLUMNS = WIDTH * WIDTH;

  @FunctionalInterface
  public interface SolidVoxelConsumer {
    void accept(int x, int y, int z);
  }

  private final int minY, height;
  private final BitSet solid;
  private final int[] surfaceExclusive;

  private DensityChunk(int minY, int height, BitSet solid, int[] surfaceExclusive) {
    this.minY = minY;
    this.height = height;
    this.solid = solid;
    this.surfaceExclusive = surfaceExclusive;
  }

  /** Samples each voxel exactly and retains only occupancy and the top-exclusive surface. */
  public static DensityChunk sample(DensityGraph graph, int originX, int minY, int originZ, int height) {
    return sample((DensityField) graph, originX, minY, originZ, height);
  }

  public static DensityChunk sample(DensityField graph, int originX, int minY, int originZ, int height) {
    Objects.requireNonNull(graph, "graph");
    if (height < 1 || height > MAX_HEIGHT) throw new IllegalArgumentException("chunk height");
    if (Math.floorMod(originX, WIDTH) != 0 || Math.floorMod(originZ, WIDTH) != 0)
      throw new IllegalArgumentException("chunk origin must align to 16-block boundaries");
    checkAxis(originX, WIDTH);
    checkAxis(originZ, WIDTH);
    checkAxis(minY, height);

    int cells = Math.multiplyExact(COLUMNS, height);
    BitSet solid = new BitSet(cells);
    int[] surfaces = new int[COLUMNS];
    DensityField sampler = graph instanceof DensityGraph program ? program.newSampler()::sample : graph;
    for (int z = 0; z < WIDTH; z++) {
      for (int x = 0; x < WIDTH; x++) {
        int column = (z * WIDTH + x) * height;
        int surface = minY;
        for (int dy = 0; dy < height; dy++) {
          if (sampler.sample(originX + x, minY + dy, originZ + z) > 0.0) {
            solid.set(column + dy);
            surface = minY + dy + 1;
          }
        }
        surfaces[z * WIDTH + x] = surface;
      }
    }
    return new DensityChunk(minY, height, solid, surfaces);
  }

  /**
   * Opt-in V8 exact-occupancy preflight, deliberately not enabled by native worldgen.
   *
   * <p>Certified all-solid/all-air boxes replace only the individual density calls.
   * Any uncertain cell uses the same job-owned exact sampler as {@link #sample}.
   * Every vertical slab is at most 64 cells high; the number of interval queries
   * is capped independently for each slab. The output keeps the existing z/x/y
   * column-major bits and top-exclusive surface semantics, including cave roofs.
   * The planner never reads or writes a Minecraft world, persisted profile or water.
   * No performance benefit is implied until native profiling proves one.
   */
  public static Planned samplePlanned(DensityGraph graph, int originX, int minY,
                                      int originZ, int height, int queriesPerSlab) {
    Objects.requireNonNull(graph, "graph");
    if (height < 1 || height > MAX_HEIGHT) throw new IllegalArgumentException("chunk height");
    if (Math.floorMod(originX, WIDTH) != 0 || Math.floorMod(originZ, WIDTH) != 0)
      throw new IllegalArgumentException("chunk origin must align to 16-block boundaries");
    checkAxis(originX, WIDTH);
    checkAxis(originZ, WIDTH);
    checkAxis(minY, height);
    if (queriesPerSlab < 0 || queriesPerSlab > DensityVolumePlan.MAX_QUERIES)
      throw new IllegalArgumentException("classification budget must be 0..256");

    var bounds = new DensityVolumeBounds(graph);
    var sampler = graph.newSampler();
    BitSet occupied = new BitSet(COLUMNS * height);
    long exact = 0, certifiedSolid = 0, certifiedEmpty = 0;
    int queries = 0;
    for (int band = 0; band < height; band += 64) {
      int sizeY = Math.min(64, height - band);
      var plan = DensityVolumePlan.plan(bounds, originX, minY + band, originZ,
                                        WIDTH, sizeY, WIDTH, queriesPerSlab);
      queries += plan.queries();
      exact += plan.uncertainCells();
      certifiedSolid += plan.solidCells();
      certifiedEmpty += plan.emptyCells();
      for (var tile : plan.tiles()) {
        int startY = tile.y() - minY;
        for (int worldZ = tile.z(); worldZ < tile.z() + tile.sizeZ(); worldZ++) {
          int localZ = worldZ - originZ;
          for (int worldX = tile.x(); worldX < tile.x() + tile.sizeX(); worldX++) {
            int column = (localZ * WIDTH + worldX - originX) * height;
            switch (tile.occupancy()) {
              case SOLID -> occupied.set(column + startY, column + startY + tile.sizeY());
              case EMPTY -> { /* All air: no authoritative density calls. */ }
              case UNCERTAIN -> {
                for (int localY = 0; localY < tile.sizeY(); localY++) {
                  if (sampler.sample(worldX, tile.y() + localY, worldZ) > 0.0)
                    occupied.set(column + startY + localY);
                }
              }
            }
          }
        }
      }
    }
    // Reconstruct the exact legacy top-exclusive heightmap after all slabs, not
    // from an interpolated surface estimate or a slab-local top. Caves and holes
    // retain the highest actual positive voxel, even across slab boundaries.
    int[] surfaces = new int[COLUMNS];
    for (int column = 0; column < COLUMNS; column++) {
      int first = column * height;
      int top = occupied.previousSetBit(first + height - 1);
      surfaces[column] = top < first ? minY : minY + top - first + 1;
    }
    return new Planned(new DensityChunk(minY, height, occupied, surfaces),
                       exact, certifiedSolid, certifiedEmpty, queries);
  }

  /** Source-only planning statistics, not a measured time or FPS claim. */
  public record Planned(DensityChunk chunk, long exactSamples, long solidCertificates,
                        long airCertificates, int classificationQueries) {
    public long totalCells() { return exactSamples + solidCertificates + airCertificates; }
  }

  public int minY() {
    return minY;
  }

  public int maxYExclusive() {
    return minY + height;
  }

  public boolean solid(int x, int y, int z) {
    if (x < 0 || x >= WIDTH || z < 0 || z >= WIDTH || y < minY || y >= maxYExclusive())
      throw new IllegalArgumentException("voxel outside density chunk");
    return solid.get((z * WIDTH + x) * height + y - minY);
  }

  /** Returns the first air Y above the highest solid voxel, or minY for an empty column. */
  public int surfaceExclusive(int x, int z) {
    if (x < 0 || x >= WIDTH || z < 0 || z >= WIDTH)
      throw new IllegalArgumentException("column outside density chunk");
    return surfaceExclusive[z * WIDTH + x];
  }

  public int solidCount() {
    return solid.cardinality();
  }

  /** Visits only solid voxels in deterministic z, x, then y order without exposing storage. */
  public void forEachSolid(SolidVoxelConsumer consumer) {
    Objects.requireNonNull(consumer, "consumer");
    for (int voxel = solid.nextSetBit(0); voxel >= 0; voxel = solid.nextSetBit(voxel + 1)) {
      int column = voxel / height;
      consumer.accept(column % WIDTH, minY + voxel % height, column / WIDTH);
    }
  }

  public int payloadBytes() {
    return (solid.size() + 7) / 8 + surfaceExclusive.length * Integer.BYTES;
  }

  private static void checkAxis(int origin, int count) {
    long end = (long) origin + count - 1;
    if (origin < -DensityGraph.WORLD_LIMIT || end > DensityGraph.WORLD_LIMIT)
      throw new IllegalArgumentException("chunk coordinates outside density graph domain");
  }
}
