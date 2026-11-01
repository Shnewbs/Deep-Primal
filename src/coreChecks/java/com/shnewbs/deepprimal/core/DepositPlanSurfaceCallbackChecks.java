package com.shnewbs.deepprimal.core;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/** Unused terrain-height callbacks must not run for absolute/geological ore. */
public final class DepositPlanSurfaceCallbackChecks {
  private static void need(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
  public static void main(String[] args) {
    var noise = new TerrainNoise(0x167a11L);
    int assertions = 0;
    for (var shape : DepositPlan.Shape.values()) {
      for (var reference : DepositPlan.Reference.values()) {
        var plan = new DepositPlan(noise, List.of(new DepositPlan.Definition(
            "test:surface_callback", shape, reference, 32, 1, 1, 5, 5,
            72, 16, 72, Set.of(Geology.Rock.values()),
            List.of(new DepositPlan.Content(7, 1)))));
        int expected = plan.chunk(0, 0, (x, z) -> 250).candidates();
        need(expected > 0, "test must contain candidates");
        assertions++;
        AtomicInteger calls = new AtomicInteger();
        if (reference == DepositPlan.Reference.DEPTH) {
          int actual = plan.chunk(0, 0, (x, z) -> {
            calls.incrementAndGet(); return 250;
          }).candidates();
          need(actual == expected && calls.get() > 0,
              "DEPTH must still consult surface height");
        } else {
          int actual = plan.chunk(0, 0, (x, z) -> {
            calls.incrementAndGet();
            throw new IllegalStateException("unused surface callback executed");
          }).candidates();
          need(actual == expected && calls.get() == 0,
              reference + " must not consult unused surface height");
        }
        assertions++;
      }
    }
    System.out.println("DEPOSIT_SURFACE_CALLBACK_PASS assertions=" + assertions);
  }
  private DepositPlanSurfaceCallbackChecks() {}
}
