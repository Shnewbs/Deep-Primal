package com.shnewbs.deepprimal.core;

/** Shared height interpolation and stitched tile topology, independent of the game/GPU. */
public final class HorizonGeometry {
  public static int cells(int detail) {
    return 64 >> Math.clamp(detail, 0, 2);
  }

  public static int points(int detail) {
    int n = cells(detail) + 1;
    return n * n;
  }

  public static int detail(double distanceBlocks) {
    return distanceBlocks < 1024 ? 0 : distanceBlocks < 2560 ? 1 : 2;
  }

  public static double sample(short[] heights, int cells, double x, double z) {
    double gx = Math.clamp(x / FarTerrain.TILE_BLOCKS * cells, 0, cells);
    double gz = Math.clamp(z / FarTerrain.TILE_BLOCKS * cells, 0, cells);
    int ix = Math.min(cells - 1, (int) gx), iz = Math.min(cells - 1, (int) gz), width = cells + 1;
    double a = gx - ix, b = gz - iz;
    int h00 = heights[iz * width + ix],
        h10 = heights[iz * width + ix + 1],
        h01 = heights[(iz + 1) * width + ix],
        h11 = heights[(iz + 1) * width + ix + 1];
    // Match the mesh's triangle planes exactly, including saddle-shaped cells.
    return a + b <= 1
        ? h00 + (h10 - h00) * a + (h01 - h00) * b
        : h11 + (h01 - h11) * (1 - a) + (h10 - h11) * (1 - b);
  }

  /** All tile boundaries share the coarsest grid, including negative world coordinates. */
  public static double stitched(short[] heights, int cells, int x, int z) {
    if (x != 0 && z != 0 && x != cells && z != cells) return heights[z * (cells + 1) + x];
    int stride = cells / 16;
    if (x == 0 || x == cells) {
      int lo = z / stride * stride, hi = Math.min(cells, lo + stride);
      return heights[lo * (cells + 1) + x]
          + (heights[hi * (cells + 1) + x] - heights[lo * (cells + 1) + x])
              * (z - lo)
              / (double) stride;
    }
    int lo = x / stride * stride, hi = Math.min(cells, lo + stride);
    return heights[z * (cells + 1) + lo]
        + (heights[z * (cells + 1) + hi] - heights[z * (cells + 1) + lo])
            * (x - lo)
            / (double) stride;
  }

  private HorizonGeometry() {}
}
