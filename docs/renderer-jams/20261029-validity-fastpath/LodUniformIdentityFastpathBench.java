package com.shnewbs.deepprimal.lod.client;

import java.util.Arrays;
import java.util.Locale;
import net.minecraft.world.phys.AABB;

/** Paired resident page CPU-only microbenchmark, not game FPS or GPU timings. */
public final class LodUniformIdentityFastpathBench {
  private static volatile long consumed;
  private LodUniformIdentityFastpathBench() {}

  private static AABB box(double x) { return new AABB(x, 0, -16, x + 16, 100, 16); }

  private static AABB[][] scene(String kind) {
    AABB[][] levels = new AABB[4][];
    for (int d = 0; d < levels.length; d++) {
      AABB[] items = new AABB[64];
      AABB shared = box(-16);
      AABB other = box(32);
      AABB invalid = new AABB(Double.NaN, 0, 0, 1, 1, 1);
      for (int i = 0; i < items.length; i++) {
        items[i] = switch (kind) {
          case "uniform-shared" -> shared;
          case "uniform-values" -> box(-16);
          case "alternating" -> (i & 1) == 0 ? shared : other;
          case "mixed-late" -> i < 60 ? box(-16) : box(32);
          case "unique" -> box(i * 20.0);
          case "alternating-invalid" -> (i & 1) == 0 ? shared : invalid;
          case "invalid-uniform" -> invalid;
          default -> throw new IllegalArgumentException(kind);
        };
      }
      levels[d] = items;
    }
    return levels;
  }

  private static long execute(boolean newCode, AABB[][] levels, int repetitions) {
    long checksum = 0;
    for (int i = 0; i < repetitions; i++) {
      AABB[] pages = new AABB[levels.length];
      long[][] one = new long[levels.length][], two = new long[levels.length][];
      if (newCode) LodPublishedBounds.populate(levels, pages, one, two);
      else LodPublishedBoundsJam43.populate(levels, pages, one, two);
      for (int j = 0; j < pages.length; j++) {
        if (pages[j] != null) checksum += Double.doubleToRawLongBits(pages[j].minX);
        checksum += one[j].length + two[j].length;
      }
    }
    consumed = checksum;
    return checksum;
  }

  private static long percentile(long[] values, double quantile) {
    return values[Math.min(values.length - 1, (int)Math.ceil(quantile * values.length) - 1)];
  }

  public static void main(String[] args) {
    String kind = args.length > 0 ? args[0] : "uniform-values";
    AABB[][] pages = scene(kind);
    for (int i = 0; i < 1500; i++) {
      execute(false, pages, 8);
      execute(true, pages, 8);
    }
    int samples = 251, repeat = 64;
    long[] baseline = new long[samples], candidate = new long[samples];
    for (int sample = 0; sample < samples; sample++) {
      boolean flip = (sample & 1) == 1;
      long t = System.nanoTime();
      execute(flip, pages, repeat);
      long first = System.nanoTime() - t;
      t = System.nanoTime();
      execute(!flip, pages, repeat);
      long second = System.nanoTime() - t;
      baseline[sample] = flip ? second : first;
      candidate[sample] = flip ? first : second;
    }
    Arrays.sort(baseline);
    Arrays.sort(candidate);
    System.out.printf(Locale.ROOT,
        "UNIFORM_IDENTITY_FASTPATH_BENCH shape=%s samples=%d pagesPerSample=%d old[p50=%d,p95=%d,p99=%d]ns new[p50=%d,p95=%d,p99=%d]ns checksum=%d%n",
        kind, samples, repeat, percentile(baseline,.50), percentile(baseline,.95), percentile(baseline,.99),
        percentile(candidate,.50), percentile(candidate,.95), percentile(candidate,.99), consumed);
  }
}
