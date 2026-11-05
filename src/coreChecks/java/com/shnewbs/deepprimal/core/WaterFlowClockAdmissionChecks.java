package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Boundary and parity regression for signed-long animation clocks. */
public final class WaterFlowClockAdmissionChecks {
  private static long checks;
  private static void check(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message);
  }
  public static void main(String[] args) {
    WaterFlowTransition extreme = new WaterFlowTransition(0, Long.MIN_VALUE + 10);
    extreme.target(1, Long.MIN_VALUE + 10, 1);
    check(extreme.age(Long.MIN_VALUE + 10) == 0, "start boundary");
    check(extreme.age(Long.MIN_VALUE + 109) == 99, "before finish");
    check(extreme.age(Long.MIN_VALUE + 110) == 100, "finish boundary");
    check(extreme.age(Long.MAX_VALUE - 10) == 100, "overflow elapsed saturates");
    check(!extreme.active(Long.MAX_VALUE - 10), "overflow must not remain active");
    extreme.target(2, Long.MAX_VALUE - 10, 2);
    check(extreme.source() == 1 && extreme.target() == 2, "expired transition must retarget");
    check(extreme.age(Long.MAX_VALUE - 10) == 0, "retarget starts at zero");
    check(extreme.age(Long.MIN_VALUE + 10) == 0, "backward clock saturates to zero");

    WaterFlowTransition queue = new WaterFlowTransition(0, Long.MIN_VALUE + 10);
    queue.target(1, Long.MIN_VALUE + 10, 1);
    queue.target(2, Long.MIN_VALUE + 50, 2);
    check(queue.queued(Long.MIN_VALUE + 50), "queued valid direction");
    queue.target(2, Long.MAX_VALUE - 10, 3);
    check(queue.target() == 2 && queue.source() == 1, "queued target released at elapsed overflow");
    queue.target(99, Long.MAX_VALUE - 9, Long.MAX_VALUE);
    queue.target(3, Long.MAX_VALUE - 9, 4);
    check(queue.target() == 2, "invalid version cannot supersede valid history");

    // Exact ordinary-domain parity with the pre-fix age expression.
    Random random = new Random(20261101L);
    for (int i = 0; i < 250_000; i++) {
      long start = random.nextInt(1_000_001) - 500_000L;
      long now = start + random.nextInt(1_001) - 500L;
      WaterFlowTransition transition = new WaterFlowTransition(0, start);
      transition.target(1, start, 1);
      int legacy = Math.clamp(now - start, 0, WaterFlowTransition.MILLIS);
      check(transition.age(now) == legacy, "ordinary elapsed parity");
      check(transition.active(now) == (legacy < WaterFlowTransition.MILLIS), "ordinary active parity");
    }
    System.out.println("PASS: " + checks + " water flow clock checks");
  }
  private WaterFlowClockAdmissionChecks() {}
}
