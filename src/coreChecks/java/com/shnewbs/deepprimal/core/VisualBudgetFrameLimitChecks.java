package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Deterministic native-unlimited/background-frame-limit contract checks. */
public final class VisualBudgetFrameLimitChecks {
  private VisualBudgetFrameLimitChecks() {}

  public static void main(String[] args) {
    int checks = 0;
    for (int nativeLimit : new int[] {0, 1, 5, 20, 60, 144, 260}) {
      for (int background : new int[] {0, 1, 5, 20, 120}) {
        for (int hidden : new int[] {0, 1, 5, 60}) {
          for (boolean focused : new boolean[] {false, true}) {
            for (boolean minimized : new boolean[] {false, true}) {
              verify(nativeLimit, focused, minimized, background, hidden);
              checks++;
            }
          }
        }
      }
    }
    // An uncapped foreground must not disable the configured background/minimized caps.
    check(VisualBudget.frameLimit(0, false, false, 20, 5) == 20, "Uncapped background");
    check(VisualBudget.frameLimit(0, false, true, 20, 5) == 5, "Uncapped minimized");
    check(VisualBudget.frameLimit(0, true, false, 20, 5) == 0, "Uncapped foreground");
    check(VisualBudget.frameLimit(10, false, false, 20, 5) == 10, "Do not raise native cap");
    check(VisualBudget.frameLimit(144, false, true, 20, 0) == 144, "Disabled minimized override");
    checks += 5;
    Random random = new Random(20261031L);
    for (int i = 0; i < 100_000; i++) {
      verify(random.nextInt(1001), random.nextBoolean(), random.nextBoolean(),
          random.nextInt(241), random.nextInt(121));
      checks++;
    }
    System.out.println("VISUAL_FRAME_LIMIT_PASS checks=" + checks
        + " nativeUnlimitedBackground=true minimized=true noOverride=true");
  }

  private static void verify(int nativeLimit, boolean focused, boolean minimized,
      int background, int hidden) {
    int override = minimized ? hidden : focused ? 0 : background;
    int expected = override <= 0 ? nativeLimit
        : nativeLimit == 0 ? override : Math.min(nativeLimit, override);
    int actual = VisualBudget.frameLimit(nativeLimit, focused, minimized, background, hidden);
    check(actual == expected, "FPS native=" + nativeLimit + " focused=" + focused
        + " minimized=" + minimized + " background=" + background + " hidden=" + hidden
        + " expected=" + expected + " actual=" + actual);
  }

  private static void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
}
