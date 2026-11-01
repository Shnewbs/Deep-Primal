package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Invalid view bounds must not admit optional visuals; finite geometry is unchanged. */
public final class VisualBudgetOutsideChecks {
  private static long checks;
  private VisualBudgetOutsideChecks() {}

  private static void check(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }

  private static boolean outside(double[] a, double[] p, int radius) {
    return VisualBudget.outside(a[0], a[1], a[2], a[3], a[4], a[5],
        p[0], p[1], p[2], radius);
  }

  private static boolean originalFinite(double[] a, double[] p, int radius) {
    if (radius <= 0) return false;
    double dx = Math.max(Math.max(a[0] - p[0], p[0] - a[3]), 0);
    double dy = Math.max(Math.max(a[1] - p[1], p[1] - a[4]), 0);
    double dz = Math.max(Math.max(a[2] - p[2], p[2] - a[5]), 0);
    return dx * dx + dy * dy + dz * dz > (double) radius * radius;
  }

  public static void main(String[] args) {
    double[] bounds = {-2, -3, -4, 2, 3, 4};
    double[] camera = {0, 0, 0};
    check(!outside(bounds, camera, 1), "inside AABB");
    check(!outside(bounds, new double[]{3, 0, 0}, 1), "inclusive face contact");
    check(outside(bounds, new double[]{3.00001, 0, 0}, 1), "beyond face");
    check(!outside(bounds, new double[]{3, 4, 0}, 2), "diagonal near radius");
    check(outside(bounds, new double[]{4, 5, 0}, 2), "diagonal outside");
    for (int i = 0; i < 9; i++) {
      for (double bad : new double[]{Double.NaN, Double.POSITIVE_INFINITY,
          Double.NEGATIVE_INFINITY}) {
        double[] a = bounds.clone();
        double[] p = camera.clone();
        if (i < 6) a[i] = bad;
        else p[i - 6] = bad;
        check(outside(a, p, 16), "invalid coordinate must be culled: index=" + i);
        check(!outside(a, p, 0), "unlimited radius preserves existing behavior");
      }
    }
    for (int axis = 0; axis < 3; axis++) {
      double[] reversed = bounds.clone();
      reversed[axis] = reversed[axis + 3] + 1;
      check(outside(reversed, camera, 16), "inverted AABB axis=" + axis);
      check(!outside(reversed, camera, -1), "disabled radius remains unlimited");
    }
    Random random = new Random(20261101L);
    for (int i = 0; i < 250_000; i++) {
      double[] a = new double[6];
      double[] p = new double[3];
      for (int axis = 0; axis < 3; axis++) {
        a[axis] = (random.nextInt(100000) - 50000) * 0.25;
        a[axis + 3] = a[axis] + random.nextInt(500) * 0.25;
        p[axis] = (random.nextInt(100000) - 50000) * 0.25;
      }
      int radius = random.nextInt(1026) - 2;
      check(outside(a, p, radius) == originalFinite(a, p, radius),
          "finite geometry parity at " + i);
    }
    for (int i = 0; i < 100_000; i++) {
      double[] a = {-10, -10, -10, 10, 10, 10};
      double[] p = {(random.nextInt(1001) - 500) * 0.1,
          (random.nextInt(1001) - 500) * 0.1,
          (random.nextInt(1001) - 500) * 0.1};
      int radius = random.nextInt(100) + 1;
      double dx = Math.max(0, Math.abs(p[0]) - 10);
      double dy = Math.max(0, Math.abs(p[1]) - 10);
      double dz = Math.max(0, Math.abs(p[2]) - 10);
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (Math.abs(dist - radius) > 1e-8) {
        check(outside(a, p, radius) == (dist > radius), "independent distance oracle");
      }
    }
    System.out.println("VISUAL_OUTSIDE_PASS checks=" + checks
        + " invalidInputsRejected=true finiteParity=true independentOracle=true");
  }
}
