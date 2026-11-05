package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Independent long-grid oracle for the constant-time column surface solver. */
public final class ColumnHeightBoundaryChecks {
  private static long checks;
  private ColumnHeightBoundaryChecks() {}

  private static void check(boolean pass, String label) {
    checks++;
    if (!pass) throw new AssertionError("check=" + checks + " " + label);
  }

  private static int reference(double height, int lower, int upper, int step) {
    // Binary-search integer grid indices, not floating-point height / step.
    long lo = Math.floorDiv((long) lower, step) - 1L;
    long hi = Math.floorDiv((long) upper, step);
    while (lo < hi) {
      long mid = lo + (hi - lo + 1L) / 2L;
      if (height > mid * step) lo = mid;
      else hi = mid - 1L;
    }
    return (int) Math.max((long) lower, lo * step);
  }

  private static void verify(double height, int lower, int upper, int step) {
    int expected = reference(height, lower, upper, step);
    int actual = ColumnHeight.surfaceY(height, lower, upper, step);
    check(actual == expected, "height=" + height + " lower=" + lower
        + " upper=" + upper + " step=" + step + " actual=" + actual
        + " expected=" + expected);
    check(actual >= lower && actual <= upper, "range containment");
  }

  private static void invalid(double height, int lower, int upper, int step) {
    boolean threw = false;
    try { ColumnHeight.surfaceY(height, lower, upper, step); }
    catch (IllegalArgumentException expected) { threw = true; }
    check(threw, "invalid input accepted");
  }

  public static void main(String[] args) {
    // First two expose independent defects in the original implementation.
    verify(Double.MIN_VALUE, -64, 64, 8);
    verify(3e9, Integer.MIN_VALUE, Integer.MIN_VALUE, 3);
    verify(Math.nextUp(2147483000.0), Integer.MIN_VALUE, Integer.MAX_VALUE, 2147483000);
    for (double bad : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
      invalid(bad, -64, 320, 8);
    invalid(0, 1, 0, 1);
    invalid(0, -64, 320, 0);
    invalid(0, -64, 320, -1);

    for (int step : new int[] {1, 2, 3, 7, 8, 16, 64, 97, 1000000, Integer.MAX_VALUE}) {
      for (int lower : new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -384, -1, 0, 632, Integer.MAX_VALUE - 1}) {
        for (int upper : new int[] {Integer.MIN_VALUE, -384, 0, 632, Integer.MAX_VALUE}) {
          if (upper < lower) continue;
          long top = Math.floorDiv((long) upper, step) * step;
          for (double h : new double[] {-Double.MAX_VALUE, -3e9, lower,
              Math.nextUp((double) lower), Math.nextDown((double) top), top,
              Math.nextUp((double) top), upper, Math.nextUp((double) upper), 3e9}) {
            if (Double.isFinite(h)) verify(h, lower, upper, step);
          }
        }
      }
    }

    Random rng = new Random(202610311905L);
    int[] steps = {1, 2, 3, 4, 7, 8, 16, 32, 64, 97, 1024, 1_000_000, 2_147_483_000, Integer.MAX_VALUE};
    for (int i = 0; i < 250_000; i++) {
      int a = rng.nextInt(), b = rng.nextInt();
      int lower = Math.min(a, b), upper = Math.max(a, b);
      int step = i % 4 == 0 ? 1 + rng.nextInt(Integer.MAX_VALUE - 1) : steps[rng.nextInt(steps.length)];
      long grid = (long) rng.nextInt() / step * step;
      double height = switch (i % 10) {
        case 0 -> grid;
        case 1 -> Math.nextUp((double) grid);
        case 2 -> Math.nextDown((double) grid);
        case 3 -> Math.nextUp((double) lower);
        case 4 -> Math.nextDown((double) upper);
        case 5 -> lower;
        case 6 -> upper;
        case 7 -> (rng.nextDouble() - 0.5) * 6e9;
        case 8 -> rng.nextGaussian() * 1e9;
        default -> rng.nextBoolean() ? Double.MAX_VALUE : -Double.MAX_VALUE;
      };
      verify(height, lower, upper, step);
    }
    System.out.println("COLUMN_HEIGHT_BOUNDARY_PASS checks=" + checks);
  }
}
