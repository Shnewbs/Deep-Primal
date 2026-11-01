package com.shnewbs.deepprimal.core;

import java.math.BigInteger;
import java.util.Random;

/** Signed-long-safe surface presentation timing and ordinary-domain parity. */
public final class WaterSurfaceClockAdmissionChecks {
  private static long checks;
  private static final BigInteger ZERO = BigInteger.ZERO;
  private static final BigInteger HUNDRED = BigInteger.valueOf(100);
  private static final BigInteger OFFSET_MAX = BigInteger.valueOf(32768);

  private static void need(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message + " at assertion " + checks);
  }

  private static void same(float a, float b, String message) {
    need(Float.floatToRawIntBits(a) == Float.floatToRawIntBits(b), message);
  }

  public static void main(String[] args) {
    WaterSurfaceTransition extreme = new WaterSurfaceTransition(0f, Long.MIN_VALUE + 10);
    extreme.target(1f, Long.MIN_VALUE + 10);
    same(extreme.at(Long.MIN_VALUE + 10), 0f, "start frame");
    same(extreme.at(Long.MIN_VALUE + 60), .5f, "midpoint frame");
    same(extreme.at(Long.MAX_VALUE - 10), 1f, "overflow elapsed must finish");
    same(extreme.from(Long.MAX_VALUE - 10), 1f, "finished transition source");
    need(extreme.remaining(Long.MAX_VALUE - 10) == 0, "expired transition cleanup");
    need(extreme.offset(Long.MAX_VALUE - 10) == -32768, "expired offset saturated");
    same(extreme.at(Long.MIN_VALUE), 0f, "backward clock remains at start");
    need(extreme.remaining(Long.MIN_VALUE) == 100, "backward clock cleanup");
    need(extreme.offset(Long.MIN_VALUE) == 0, "backward clock offset");
    extreme.restart(Long.MAX_VALUE - 10);
    same(extreme.at(Long.MAX_VALUE - 10), 1f, "restart at elapsed boundary");

    WaterSurfaceTransition settle = new WaterSurfaceTransition(0f, Long.MAX_VALUE - 400);
    settle.target(1f / 16f, Long.MAX_VALUE - 300, 1);
    settle.target(0f, Long.MAX_VALUE - 5, 2);
    same(settle.target(), 1f / 32f, "reversion midpoint retained near long max");
    need(settle.remaining(Long.MAX_VALUE - 5) == 100,
        "midpoint animation must still need cleanup");
    settle.advance(Long.MAX_VALUE - 1);
    same(settle.target(), 1f / 32f, "settle must not expire from overflow");
    settle.advance(Long.MAX_VALUE);
    same(settle.target(), 0f, "saturated deadline eventually expires");

    WaterSurfaceTransition backwards = new WaterSurfaceTransition(0f, 1000);
    backwards.target(1f / 16f, 1100, 1);
    backwards.target(0f, 1050, 2);
    same(backwards.target(), 0f, "backward versioned update cannot trigger settle");

    // Independently compute elapsed in arbitrary precision for both signed-long extremes.
    Random r = new Random(0x160C10C6L);
    for (int i = 0; i < 200_000; i++) {
      long start = r.nextLong(), now = r.nextLong();
      WaterSurfaceTransition actual = new WaterSurfaceTransition(0f, start);
      actual.target(1f, start);
      BigInteger elapsed = BigInteger.valueOf(now).subtract(BigInteger.valueOf(start)).max(ZERO);
      int age = elapsed.min(HUNDRED).intValueExact();
      same(actual.at(now), age / 100f, "arbitrary-precision animation age");
      need(actual.remaining(now) == 100 - age, "arbitrary-precision remaining");
      need(actual.offset(now) == -elapsed.min(OFFSET_MAX).intValueExact(),
          "arbitrary-precision offset");
      same(actual.from(now), age == 100 ? 1f : 0f, "arbitrary-precision from");
    }

    // Preserve all pre-fix finite, ordinary-time behavior bit for bit.
    Random parity = new Random(0x160FADE5L);
    for (int scenario = 0; scenario < 2_000; scenario++) {
      float initial = parity.nextInt(17) / 16f;
      WaterSurfaceTransition actual = new WaterSurfaceTransition(initial, 0);
      LegacySurface legacy = new LegacySurface(initial, 0);
      long now = 0;
      for (int step = 0; step < 100; step++) {
        now += parity.nextInt(75);
        float next = parity.nextInt(17) / 16f;
        if ((step & 1) == 0) {
          actual.target(next, now, step);
          legacy.target(next, now, step);
        } else {
          actual.target(next, now);
          legacy.target(next, now);
        }
        if ((step % 7) == 0) { actual.advance(now); legacy.advance(now); }
        same(actual.target(), legacy.target(), "ordinary target parity");
        same(actual.at(now), legacy.at(now), "ordinary current frame parity");
        same(actual.at(now + 50), legacy.at(now + 50), "ordinary next frame parity");
        same(actual.from(now + 50), legacy.from(now + 50), "ordinary source parity");
        need(actual.remaining(now) == legacy.remaining(now), "ordinary cleanup parity");
        need(actual.offset(now) == legacy.offset(now), "ordinary offset parity");
      }
    }
    System.out.println("WATER_SURFACE_CLOCK_PASS assertions=" + checks
        + " signedLongScenarios=200000 ordinaryParitySteps=200000");
  }

  private WaterSurfaceClockAdmissionChecks() {}
}
