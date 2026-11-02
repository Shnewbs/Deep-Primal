package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Malformed-coordinate fail-closed and finite-coordinate triangle parity for LOD heights. */
public final class HorizonGeometryFiniteAdmissionChecks {
  private static long assertions;
  private static void check(boolean condition, String why) {
    assertions++;
    if (!condition) throw new AssertionError(why + " assertion=" + assertions);
  }

  public static void run() {
    Random random = new Random(20261102L);
    for (int detail = 0; detail < 3; detail++) {
      int cells = HorizonGeometry.cells(detail);
      short[] heights = new short[HorizonGeometry.points(detail)];
      for (int i = 0; i < heights.length; i++) heights[i] = (short) random.nextInt(65536);
      for (double invalid : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
        for (double valid : new double[] {-1000, -1, 0, 0.25, 256, 512, 1024}) {
          check(HorizonGeometry.sample(heights, cells, invalid, valid) == heights[0], "invalid x");
          check(HorizonGeometry.sample(heights, cells, valid, invalid) == heights[0], "invalid z");
        }
        check(HorizonGeometry.sample(heights, cells, invalid, invalid) == heights[0], "invalid pair");
      }
      for (int i = 0; i < 150_000; i++) {
        double x = random.nextDouble() * 4096 - 2048;
        double z = random.nextDouble() * 4096 - 2048;
        double gx = Math.clamp(x / FarTerrain.TILE_BLOCKS * cells, 0, cells);
        double gz = Math.clamp(z / FarTerrain.TILE_BLOCKS * cells, 0, cells);
        int ix = Math.min(cells - 1, (int) gx), iz = Math.min(cells - 1, (int) gz), width = cells + 1;
        double a = gx - ix, b = gz - iz;
        int h00 = heights[iz * width + ix], h10 = heights[iz * width + ix + 1],
            h01 = heights[(iz + 1) * width + ix], h11 = heights[(iz + 1) * width + ix + 1];
        double expected = a + b <= 1 ? h00 + (h10 - h00) * a + (h01 - h00) * b
            : h11 + (h01 - h11) * (1 - a) + (h10 - h11) * (1 - b);
        check(Double.doubleToLongBits(HorizonGeometry.sample(heights, cells, x, z))
            == Double.doubleToLongBits(expected), "finite triangle parity");
      }
      for (int i = 0; i < 10_000; i++) {
        int x = random.nextInt(cells + 1), z = random.nextInt(cells + 1);
        check(HorizonGeometry.sample(heights, cells, x * 512.0 / cells, z * 512.0 / cells)
            == heights[z * (cells + 1) + x], "exact lattice anchor");
      }
    }
    System.out.println("HORIZON_GEOMETRY_FINITE_PASS assertions=" + assertions);
  }

  public static void main(String[] args) { run(); }
  private HorizonGeometryFiniteAdmissionChecks() {}
}
