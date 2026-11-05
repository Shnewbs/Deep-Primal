package com.shnewbs.deepprimal.core;

import java.util.ArrayDeque;
import java.util.Random;

public final class FrameIntervalChecks {
  public static void main(String[] args) {
    Object world = new Object();
    var window = new FrameIntervalWindow(512);
    long now = 0;
    if (!observe(window, now, world, 1920, 1080, true, 120, 32, 16, 192)
        || window.snapshot().samples() != 0) throw new AssertionError("First timestamp zero");
    var oracle = new ArrayDeque<Long>();
    var random = new Random(20261027);
    int checks = 0;
    for (int i = 0; i < 10000; i++) {
      long interval = random.nextInt(100000001);
      now += interval;
      if (observe(window, now, world, 1920, 1080, true, 120, 32, 16, 192))
        throw new AssertionError("Stable context reset");
      oracle.addLast(interval);
      if (oracle.size() > 512) oracle.removeFirst();
      if (i % 13 != 0) continue;
      long[] sorted = oracle.stream().mapToLong(Long::longValue).sorted().toArray();
      var s = window.snapshot();
      if (s.samples() != sorted.length || s.p50Nanos() != percentile(sorted, 50)
          || s.p95Nanos() != percentile(sorted, 95) || s.p99Nanos() != percentile(sorted, 99)
          || s.maxNanos() != sorted[sorted.length - 1]) throw new AssertionError("Oracle");
      checks++;
    }
    // Each independent context coordinate changes while the others stay fixed.
    int resets = 0;
    for (int field = 0; field < 8; field++) {
      window.clear();
      observe(window, 100, world, 1920, 1080, true, 120, 32, 16, 192);
      observe(window, 110, world, 1920, 1080, true, 120, 32, 16, 192);
      long before = window.generation();
      Object nextWorld = field == 0 ? new Object() : world;
      int w = field == 1 ? 1280 : 1920, h = field == 2 ? 720 : 1080;
      boolean f = field != 3;
      int fps = field == 4 ? 60 : 120, full = field == 5 ? 16 : 32;
      int vertical = field == 6 ? 8 : 16, lod = field == 7 ? 256 : 192;
      if (!observe(window, 1000000000, nextWorld, w, h, f, fps, full, vertical, lod)
          || window.generation() != before + 1 || window.snapshot().samples() != 0)
        throw new AssertionError("Crossing interval " + field);
      if (observe(window, 1000000007, nextWorld, w, h, f, fps, full, vertical, lod)
          || window.snapshot().p99Nanos() != 7) throw new AssertionError("Next interval");
      resets++;
    }
    window.clear();
    observe(window, Long.MAX_VALUE - 2, null, 0, 0, false, 0, 0, 0, 0);
    if (observe(window, Long.MIN_VALUE + 2, null, 0, 0, false, 0, 0, 0, 0)
        || window.snapshot().p99Nanos() != 5) throw new AssertionError("nanoTime wrap");
    if (!observe(window, Long.MIN_VALUE, null, 0, 0, false, 0, 0, 0, 0)
        || window.snapshot().samples() != 0) throw new AssertionError("Clock reversal");
    window.clear();
    if (!observe(window, 999, world, 1920, 1080, true, 120, 32, 16, 192)
        || window.snapshot().samples() != 0) throw new AssertionError("Observation gap");
    System.out.println("FRAME_INTERVAL_PASS oracleWindows=" + checks
        + " intervals=10000 bounded=512 independentContextResets=" + resets
        + " zeroOrigin=true clockWrap=true clockReversal=true observationGap=true");
  }

  private static boolean observe(FrameIntervalWindow w, long now, Object world, int width,
      int height, boolean focused, int fps, int full, int vertical, int lod) {
    return w.observe(now, world, width, height, focused, fps, full, vertical, lod);
  }
  private static long percentile(long[] sorted, int p) {
    return sorted[(int) Math.ceil(sorted.length * p / 100.0) - 1];
  }
}
