package com.shnewbs.deepprimal.core.density;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Deterministic, work-bounded V8 occupancy planning for an integer voxel box.
 *
 * <p>Only SOLID and EMPTY certificates from {@link DensityVolumeBounds} permit
 * skipping exact density samples. UNCERTAIN tiles must be sampled exactly by a
 * future native consumer. This planner does not change saved profiles, the
 * terrain generator, or any V1-V7 world. Its limited budget is an admission cap,
 * not a runtime speed guarantee.
 */
public final class DensityVolumePlan {
  public static final int MAX_QUERIES = 256;

  /** Inclusive integer origin and positive dimensions; occupancy covers every cell. */
  public record Tile(int x, int y, int z, int sizeX, int sizeY, int sizeZ,
                     DensityVolumeBounds.Occupancy occupancy) {
    public long cells() { return (long)sizeX * sizeY * sizeZ; }
  }

  /** Exact coverage counts. A plan is never proof of performance or saved topology. */
  public record Result(List<Tile> tiles, long solidCells, long emptyCells,
                       long uncertainCells, int queries) {
    public long totalCells() { return solidCells + emptyCells + uncertainCells; }
  }

  /**
   * Partition a box without scanning its voxels. Every emitted tile is disjoint,
   * and the emitted tiles cover the original box, even after budget exhaustion.
   * The Y axis wins split ties so vertically stratified terrain is cheap to test.
   * A zero query budget produces one all-UNCERTAIN tile.
   */
  public static Result plan(DensityVolumeBounds bounds, int x, int y, int z,
                            int sizeX, int sizeY, int sizeZ, int queryBudget) {
    Objects.requireNonNull(bounds, "bounds");
    if (queryBudget < 0 || queryBudget > MAX_QUERIES)
      throw new IllegalArgumentException("query budget must be 0..256");
    validateAxis(x, sizeX);
    validateAxis(y, sizeY);
    validateAxis(z, sizeZ);

    var pending = new ArrayDeque<Tile>();
    pending.push(new Tile(x, y, z, sizeX, sizeY, sizeZ,
        DensityVolumeBounds.Occupancy.UNCERTAIN));
    var done = new ArrayList<Tile>();
    long solid = 0, empty = 0, uncertain = 0;
    int queries = 0;
    while (!pending.isEmpty()) {
      Tile tile = pending.pop();
      DensityVolumeBounds.Occupancy state = DensityVolumeBounds.Occupancy.UNCERTAIN;
      if (queries < queryBudget) {
        state = bounds.classifyCells(tile.x(), tile.y(), tile.z(),
                                     tile.sizeX(), tile.sizeY(), tile.sizeZ());
        queries++;
      }
      if (state == DensityVolumeBounds.Occupancy.UNCERTAIN && tile.cells() > 1
          && queries < queryBudget) {
        if (canSplit(tile, bounds.coordinateMask())) {
          split(tile, pending, bounds.coordinateMask());
          continue;
        }
      }
      Tile emitted = new Tile(tile.x(), tile.y(), tile.z(),
          tile.sizeX(), tile.sizeY(), tile.sizeZ(), state);
      done.add(emitted);
      switch (state) {
        case SOLID -> solid += emitted.cells();
        case EMPTY -> empty += emitted.cells();
        case UNCERTAIN -> uncertain += emitted.cells();
      }
    }
    return new Result(List.copyOf(done), solid, empty, uncertain, queries);
  }

  private static void validateAxis(int origin, int count) {
    if (count < 1 || count > 64 || Math.abs((long)origin) > DensityGraph.WORLD_LIMIT
        || Math.abs((long)origin + count - 1L) > DensityGraph.WORLD_LIMIT)
      throw new IllegalArgumentException("cell axis outside 1..64 or world bounds");
  }

  private static boolean canSplit(Tile tile, int mask) {
    return ((mask & 2) != 0 && tile.sizeY() > 1)
        || ((mask & 1) != 0 && tile.sizeX() > 1)
        || ((mask & 4) != 0 && tile.sizeZ() > 1);
  }

  private static void split(Tile tile, ArrayDeque<Tile> pending, int mask) {
    int sx = tile.sizeX(), sy = tile.sizeY(), sz = tile.sizeZ();
    // Y-first preserves whole horizontal slabs through height thresholds.
    // Do not subdivide axes that the compiled density definition never reads.
    if ((mask & 2) != 0 && sy > 1) {
      int lower = sy / 2;
      pending.push(new Tile(tile.x(), tile.y() + lower, tile.z(), sx, sy - lower, sz,
          DensityVolumeBounds.Occupancy.UNCERTAIN));
      pending.push(new Tile(tile.x(), tile.y(), tile.z(), sx, lower, sz,
          DensityVolumeBounds.Occupancy.UNCERTAIN));
    } else if ((mask & 1) != 0 && sx > 1) {
      int lower = sx / 2;
      pending.push(new Tile(tile.x() + lower, tile.y(), tile.z(), sx - lower, sy, sz,
          DensityVolumeBounds.Occupancy.UNCERTAIN));
      pending.push(new Tile(tile.x(), tile.y(), tile.z(), lower, sy, sz,
          DensityVolumeBounds.Occupancy.UNCERTAIN));
    } else {
      int lower = sz / 2;
      pending.push(new Tile(tile.x(), tile.y(), tile.z() + lower, sx, sy, sz - lower,
          DensityVolumeBounds.Occupancy.UNCERTAIN));
      pending.push(new Tile(tile.x(), tile.y(), tile.z(), sx, sy, lower,
          DensityVolumeBounds.Occupancy.UNCERTAIN));
    }
  }

  private DensityVolumePlan() {}
}
