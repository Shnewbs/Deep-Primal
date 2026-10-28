package com.shnewbs.deepprimal.lod.client;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Random;

/** Isolated CPU capture-priority comparator; never report as game FPS. */
public final class LodCapturePriorityBenchmark {
  private LodCapturePriorityBenchmark() {}
  private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }

  public static void main(String[] args) {
    Random rng = new Random(0xace20261028L);
    for (int size : new int[] {256, 4096}) {
      LinkedHashMap<Long, Long> q = new LinkedHashMap<>();
      for (int n = 0; n < size; n++)
        q.put(key(rng.nextInt(8192) - 4096, rng.nextInt(8192) - 4096), (long) n);
      long check = 0;
      for (int n = 0; n < 6000; n++)
        check ^= LodCapturePriority.choose(q, n % 43 - 21, n % 59 - 29, n % 8 == 7);
      long[] samples = new long[3000];
      for (int n = 0; n < samples.length; n++) {
        long begin = System.nanoTime();
        check ^= LodCapturePriority.choose(q, n % 43 - 21, n % 59 - 29, n % 8 == 7);
        samples[n] = System.nanoTime() - begin;
      }
      Arrays.sort(samples);
      System.out.println("LOD_CAPTURE_CPU_SAMPLE size=" + size + " p50_ns=" + samples[1500]
          + " p95_ns=" + samples[2850] + " p99_ns=" + samples[2970]
          + " check=" + check);
    }
  }
}