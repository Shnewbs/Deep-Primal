package com.shnewbs.deepprimal.lod.client;

import java.util.Arrays;
import java.util.Random;
import net.minecraft.world.phys.AABB;

/** Synthetic CPU-only selection microbench. No GPU, pixels, or FPS claims. */
public final class LodPageFrustumAdaptiveBench {
  private static volatile long blackhole;
  private static final int PAGES = 4096, PARTS = 6, MEASURED = 301;
  private record Page(AABB union, AABB[] parts) {}
  private static boolean visible(AABB box) {
    return box.minX < 32 && box.maxX > -32 && box.minZ < 32 && box.maxZ > -32;
  }
  private static Page[] scene(boolean allVisible) {
    var pages = new Page[PAGES];
    var r = new Random(0xA11C05EL);
    for (int i = 0; i < PAGES; i++) {
      boolean onscreen = allVisible || i % 5 == 0;
      AABB[] parts = new AABB[PARTS];
      for (int j = 0; j < PARTS; j++) {
        double x = onscreen ? r.nextInt(16) - 8 : 100 + r.nextInt(80);
        double z = onscreen ? r.nextInt(16) - 8 : 100 + r.nextInt(80);
        parts[j] = new AABB(x, -4, z, x + 2, 4, z + 2);
      }
      pages[i] = new Page(new AABB(onscreen ? -8 : 100, -4,
          onscreen ? -8 : 100, onscreen ? 10 : 182, 4, onscreen ? 10 : 182), parts);
    }
    return pages;
  }
  private static long selection(Page[] pages, LodPageFrustumAdaptiveGate gate, boolean adaptive) {
    boolean coarse = !adaptive || gate.shouldTestPages();
    long count = 0;
    int tested = 0, avoided = 0;
    for (Page page : pages) {
      if (coarse) {
        tested++;
        if (!visible(page.union)) {
          avoided += page.parts.length;
          continue;
        }
      }
      for (AABB part : page.parts) if (visible(part)) count++;
    }
    if (adaptive) gate.completed(coarse, tested, avoided);
    return count;
  }
  private static String report(Page[] scene, boolean allVisible, int pass) {
    var adaptiveGate = new LodPageFrustumAdaptiveGate();
    for (int i = 0; i < 500; i++) {
      blackhole = selection(scene, adaptiveGate, true);
      blackhole = selection(scene, adaptiveGate, false);
    }
    long[] fixed = new long[MEASURED], adapted = new long[MEASURED];
    for (int i = 0; i < MEASURED; i++) {
      if ((i & 1) == 0) {
        long t = System.nanoTime();
        blackhole = selection(scene, adaptiveGate, false);
        fixed[i] = System.nanoTime() - t;
        t = System.nanoTime();
        blackhole = selection(scene, adaptiveGate, true);
        adapted[i] = System.nanoTime() - t;
      } else {
        long t = System.nanoTime();
        blackhole = selection(scene, adaptiveGate, true);
        adapted[i] = System.nanoTime() - t;
        t = System.nanoTime();
        blackhole = selection(scene, adaptiveGate, false);
        fixed[i] = System.nanoTime() - t;
      }
    }
    Arrays.sort(fixed);
    Arrays.sort(adapted);
    return "LOD_ADAPT_CPU scene=" + (allVisible ? "all_visible" : "80pct_offscreen")
        + " pass=" + pass + " pages=" + PAGES + " parts=" + PARTS
        + " fixed_p50_ns=" + fixed[MEASURED / 2]
        + " adapted_p50_ns=" + adapted[MEASURED / 2]
        + " fixed_p95_ns=" + fixed[(int) (MEASURED * .95)]
        + " adapted_p95_ns=" + adapted[(int) (MEASURED * .95)]
        + " fixed_p99_ns=" + fixed[(int) (MEASURED * .99)]
        + " adapted_p99_ns=" + adapted[(int) (MEASURED * .99)];
  }
  public static void main(String[] args) {
    for (boolean allVisible : new boolean[]{true, false}) {
      var pages = scene(allVisible);
      for (int pass = 0; pass < 2; pass++)
        System.out.println(report(pages, allVisible, pass));
    }
  }
}
