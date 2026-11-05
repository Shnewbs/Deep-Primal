package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Finite extreme corner heights must not overflow the shared water-surface interpolation. */
public final class WaterSurfaceConvexAdmissionChecks {
  private static long checks;
  private static void check(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message);
  }
  private static double legacy(double a, double b, double c, double d, double u, double v) {
    return TerrainNoise.lerp(TerrainNoise.lerp(a, b, u), TerrainNoise.lerp(c, d, u), v);
  }
  public static void main(String[] args) {
    double max = Double.MAX_VALUE;
    double[] extremes = {-max, -max / 2, -1e308, -1, -0.0, 0.0, 1, 1e308, max / 2, max};
    double[] weights = {0, Double.MIN_VALUE, .125, .25, .5, .75, .875, Math.nextDown(1), 1};
    for (double a : extremes) for (double b : extremes)
      for (double c : extremes) for (double d : extremes)
        for (double u : weights) for (double v : weights) {
          double got = WaterTransport.surface(a, b, c, d, 0, u, v);
          check(Double.isFinite(got), "Nonfinite finite-corner surface: " + a + "," + b + "," + c + "," + d + "," + u + "," + v);
          double low = Math.min(Math.min(a, b), Math.min(c, d));
          double high = Math.max(Math.max(a, b), Math.max(c, d));
          check(got >= low && got <= high, "Convex hull violation: "+a+","+b+","+c+","+d+","+u+","+v+" -> "+got+" not in ["+low+","+high+"]");
          double old = legacy(a, b, c, d, u, v);
          if (Double.isFinite(old) && Math.abs(a) < 1e6 && Math.abs(b) < 1e6
              && Math.abs(c) < 1e6 && Math.abs(d) < 1e6 && u > 0 && u < 1 && v > 0 && v < 1)
            check(Double.doubleToRawLongBits(got) == Double.doubleToRawLongBits(old), "Ordinary finite legacy changed");
        }
    Random random = new Random(20261101L);
    for (int i = 0; i < 200_000; i++) {
      double a = random.nextDouble(-1000, 1000), b = random.nextDouble(-1000, 1000);
      double c = random.nextDouble(-1000, 1000), d = random.nextDouble(-1000, 1000);
      double u = random.nextDouble(), v = random.nextDouble();
      double got = WaterTransport.surface(a, b, c, d, random.nextDouble(), u, v);
      check(Double.doubleToRawLongBits(got) == Double.doubleToRawLongBits(legacy(a,b,c,d,u,v)), "Ordinary surface changed");
    }
    // Exact shared edge: the right edge of one tile is the left edge of its neighbor.
    for (int i = 0; i < 100_000; i++) {
      double a = random.nextBoolean() ? max : -max;
      double b = random.nextBoolean() ? max : -max;
      double v = random.nextDouble();
      double left = WaterTransport.surface(0, a, 0, b, 0, 1, v);
      double right = WaterTransport.surface(a, 0, b, 0, 0, 0, v);
      check(Double.doubleToRawLongBits(left) == Double.doubleToRawLongBits(right), "Shared edge diverged");
    }
    // Integer-volume movement and pressure are unaffected by presentation fallback.
    for (int from = 0; from <= 16; from++) for (int to = 0; to <= 16; to++) {
      for (boolean down : new boolean[]{false,true}) {
        var e = WaterTransport.exchange(from,to,down);
        check(e.from()+e.to()==from+to && e.moved()>=0 && e.moved()<=from, "Volume not conserved");
      }
    }
    System.out.println("WATER_SURFACE_CONVEX_PASS assertions="+checks);
  }
  private WaterSurfaceConvexAdmissionChecks() {}
}
