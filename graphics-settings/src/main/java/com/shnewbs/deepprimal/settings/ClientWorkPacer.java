package com.shnewbs.deepprimal.settings;

import com.shnewbs.deepprimal.core.WorkBudget;
import net.minecraft.client.Minecraft;

/** One frame/heap feedback controller shared by optional terrain rendering work. */
public final class ClientWorkPacer {
  private static final WorkBudget BUDGET = new WorkBudget();
  private static long previous;

  public static void observe(long now) {
    var mc = Minecraft.getInstance();
    if (previous != 0 && mc.level != null) {
      var r = Runtime.getRuntime();
      BUDGET.observe(
          (now - previous) / 1e6,
          1000.
              / Math.min(
                  ClientPerformanceConfig.TARGET_FPS.get(),
                  mc.getFramerateLimitTracker().getFramerateLimit()),
          (double) (r.totalMemory() - r.freeMemory()) / r.maxMemory());
    }
    previous = now;
  }

  public static void reset() {
    BUDGET.reset();
    previous = 0;
  }

  public static double fraction() {
    return ClientPerformanceConfig.ADAPTIVE_WORK.get() ? BUDGET.fraction() : 1;
  }

  private ClientWorkPacer() {}
}
