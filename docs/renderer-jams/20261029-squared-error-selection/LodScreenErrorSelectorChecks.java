package com.shnewbs.deepprimal.lod.client;

import java.util.Arrays;
import java.util.Random;

/** Source-excluded deterministic parity and CPU-only measurement; not a native GPU test. */
public final class LodScreenErrorSelectorChecks {
  private static long assertions;
  private static volatile long checksum;

  private static void check(boolean condition, String message) {
    assertions++;
    if (!condition) throw new AssertionError(message + " at assertion " + assertions);
  }

  private static int original(int initial, double dx, double dz, double focal, double error) {
    double dist = Math.max(1, Math.hypot(dx, dz));
    int d = initial;
    while (d < 2 && (1 << (d + 1)) * focal / dist < error * .85) d++;
    while (d > 0 && (1 << d) * focal / dist > error * 1.15) d--;
    return d;
  }

  private static void parity(LodScreenErrorSelector selector, int prev,
      double dx, double dz, double focal, double error) {
    int expected = original(prev, dx, dz, focal, error);
    int actual = selector.select(prev, dx, dz);
    check(actual == expected, "LOD mismatch old=" + expected + " new=" + actual
        + " prev=" + prev + " dx=" + dx + " dz=" + dz
        + " focal=" + focal + " error=" + error);
  }

  private static long trial(boolean fast, int frames, int count,
      double[] dx, double[] dz, int[] previous, double focal, int error) {
    var selector = new LodScreenErrorSelector(focal, error);
    long sum = 0;
    long started = System.nanoTime();
    for (int frame = 0; frame < frames; frame++) {
      for (int i = 0; i < count; i++) {
        int d = fast ? selector.select(previous[i], dx[i], dz[i])
            : original(previous[i], dx[i], dz[i], focal, error);
        sum += d;
      }
    }
    checksum = sum;
    return System.nanoTime() - started;
  }

  private static long median(long[] times) {
    Arrays.sort(times);
    return times[times.length / 2];
  }

  public static void main(String[] args) {
    Random random = new Random(0x249ACED);
    long cases = 0;
    for (int error = 1; error <= 12; error++) {
      for (double focal : new double[] {240, 360, 540, 720, 1080, 2160}) {
        var selector = new LodScreenErrorSelector(focal, error);
        for (int i = 0; i < 12000; i++) {
          double dx = (random.nextDouble() - .5) * 200000;
          double dz = (random.nextDouble() - .5) * 200000;
          int previous = i % 3;
          parity(selector, previous, dx, dz, focal, error);
          cases++;
        }
        // Tests the precise and adjacent transition values, including x/z rotated axes.
        for (double base : new double[] {2 * focal / (error * .85),
            4 * focal / (error * .85), 2 * focal / (error * 1.15),
            4 * focal / (error * 1.15)}) {
          for (int step = -200; step <= 200; step++) {
            double v = base * (1 + step * 1.0e-12);
            for (int prev = 0; prev < 3; prev++) {
              parity(selector, prev, v, 0, focal, error);
              parity(selector, prev, 0, -v, focal, error);
              cases += 2;
            }
          }
        }
      }
    }
    for (double abnormal : new double[] {-1, 0, Double.NaN,
        Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
      var badFocal = new LodScreenErrorSelector(abnormal, 3);
      var badError = new LodScreenErrorSelector(720, abnormal);
      for (int previous = 0; previous <= 2; previous++) {
        parity(badFocal, previous, 100, 200, abnormal, 3);
        parity(badError, previous, 100, 200, 720, abnormal);
        cases += 2;
      }
    }
    for (double coordinate : new double[] {Double.NaN, Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY, Double.MAX_VALUE, 0, -0.0}) {
      var selector = new LodScreenErrorSelector(720, 3);
      for (int previous = 0; previous <= 2; previous++) {
        parity(selector, previous, coordinate, 500, 720, 3);
        cases++;
      }
    }

    double[] dx = new double[4096], dz = new double[4096];
    int[] previous = new int[4096];
    for (int i = 0; i < dx.length; i++) {
      dx[i] = (random.nextDouble() - .5) * 18000;
      dz[i] = (random.nextDouble() - .5) * 18000;
      previous[i] = random.nextInt(3);
    }
    for (int i = 0; i < 100; i++) {
      trial(false, 1, 4096, dx, dz, previous, 720, 3);
      trial(true, 1, 4096, dx, dz, previous, 720, 3);
    }
    long[] oldSamples = new long[301], newSamples = new long[301];
    for (int i = 0; i < oldSamples.length; i++) {
      if ((i & 1) == 0) {
        oldSamples[i] = trial(false, 1, 4096, dx, dz, previous, 720, 3);
        newSamples[i] = trial(true, 1, 4096, dx, dz, previous, 720, 3);
      } else {
        newSamples[i] = trial(true, 1, 4096, dx, dz, previous, 720, 3);
        oldSamples[i] = trial(false, 1, 4096, dx, dz, previous, 720, 3);
      }
    }
    System.out.println("LOD_ERROR_SQUARED_PASS cases=" + cases + " assertions=" + assertions
        + " oldMedianNanos=" + median(oldSamples) + " newMedianNanos="
        + median(newSamples) + " fixturePages=4096 pairedSamples=301 checksum=" + checksum);
  }
}
