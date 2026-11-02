package com.shnewbs.deepprimal.core.density;

import java.util.List;
import java.util.Random;

/** Exact old interval-multiplication oracle for allocation-free V8 occupancy preflights. */
public final class DensityVolumeBoundsAllocationChecks {
  private static long assertions;

  private static void check(boolean condition, String message) {
    assertions++;
    if (!condition) throw new AssertionError(message);
  }

  private static void reject(Runnable runnable) {
    try {
      runnable.run();
      throw new AssertionError("invalid coordinate admitted");
    } catch (IllegalArgumentException expected) {
      assertions++;
    }
  }

  /** Legacy four-product loop, including NaN handling and outward rounding. */
  private static DensityVolumeBounds.Occupancy legacy(
      double minX, double maxX, double minY, double maxY) {
    double a = Math.clamp(minX, -100, 100);
    double b = Math.clamp(maxX, -100, 100);
    double c = Math.clamp(minY, -100, 100);
    double d = Math.clamp(maxY, -100, 100);
    double[] products = {a * c, a * d, b * c, b * d};
    double lo = Double.POSITIVE_INFINITY, hi = Double.NEGATIVE_INFINITY;
    for (double product : products) {
      if (Double.isNaN(product)) return DensityVolumeBounds.Occupancy.UNCERTAIN;
      lo = Math.min(lo, product);
      hi = Math.max(hi, product);
    }
    lo = Math.nextDown(lo);
    hi = Math.nextUp(hi);
    if (lo > 0) return DensityVolumeBounds.Occupancy.SOLID;
    if (hi <= 0) return DensityVolumeBounds.Occupancy.EMPTY;
    return DensityVolumeBounds.Occupancy.UNCERTAIN;
  }

  public static void main(String[] args) {
    var graph = new DensityGraph.Definition(1, "density", List.of(
        DensityGraph.Node.operation("x", DensityGraph.Op.X),
        DensityGraph.Node.operation("y", DensityGraph.Op.Y),
        DensityGraph.Node.bounded("cx", DensityGraph.Op.CLAMP, "x", -100, 100),
        DensityGraph.Node.bounded("cy", DensityGraph.Op.CLAMP, "y", -100, 100),
        DensityGraph.Node.operation("density", DensityGraph.Op.MULTIPLY, "cx", "cy")))
        .compile(0);
    var bounds = new DensityVolumeBounds(graph);
    Random random = new Random(0xD312B00DL);
    for (int i = 0; i < 100_000; i++) {
      int x1 = random.nextInt(1001) - 500, x2 = random.nextInt(1001) - 500;
      int y1 = random.nextInt(1001) - 500, y2 = random.nextInt(1001) - 500;
      int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
      int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
      var expected = legacy(minX, maxX, minY, maxY);
      var actual = bounds.classify(minX, minY, 0, maxX, maxY, 0);
      check(actual == expected, "interval sign parity at case " + i);
    }
    reject(() -> bounds.classify(Double.NaN, 0, 0, 1, 1, 1));
    reject(() -> bounds.classify(0, Double.POSITIVE_INFINITY, 0, 1, 1, 1));
    reject(() -> bounds.classify(0, 0, 0, Double.NEGATIVE_INFINITY, 1, 1));
    reject(() -> bounds.classify(0, 0, 0, 30_000_001, 1, 1));
    reject(() -> bounds.classify(1, 0, 0, 0, 1, 1));
    System.out.println("DENSITY_BOUNDS_ALLOCATION_PARITY_PASS assertions=" + assertions);
  }

  private DensityVolumeBoundsAllocationChecks() {}
}
