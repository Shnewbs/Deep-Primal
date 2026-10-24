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
    DensityGraph.Sampler sampler = graph.newSampler();
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
