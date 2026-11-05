package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Fail-closed finite-water presentation input checks; no game classes needed. */
public final class WaterSurfaceFiniteChecks {
  private static long checks;
  private static void need(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message + " at check " + checks);
  }
  private static void same(float a, float b, String message) {
    need(Float.floatToRawIntBits(a) == Float.floatToRawIntBits(b), message);
  }
  public static void main(String[] args) {
    for (float bad : new float[]{Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
      var seeded = new WaterSurfaceTransition(bad, 0);
      same(seeded.target(), 0f, "nonfinite constructor input sanitized");
      seeded.target(.25f, 10, 1);
      same(seeded.target(), .25f, "valid sample recovers sanitized initial");
      var t = new WaterSurfaceTransition(.5f, 0);
      t.target(.75f, 10, 1);
      float before = t.at(40);
      int remaining = t.remaining(40);
      t.target(bad, 40, Long.MAX_VALUE);
      same(t.at(40), before, "bad versioned sample changed displayed height");
      same(t.target(), .75f, "bad versioned sample changed endpoint");
      need(t.remaining(40) == remaining, "bad versioned sample changed cleanup");
      t.target(.625f, 50, 2);
      same(t.target(), .625f, "bad versioned sample stole revision authority");
      need(Float.isFinite(t.at(75)), "versioned interpolation recovered");
      t.target(bad, 75);
      same(t.target(), .625f, "bad unversioned sample changed endpoint");
      need(Float.isFinite(t.at(100)), "unversioned interpolation recovered");
    }
    var random = new Random(0x142FACE);
    for (int scenario = 0; scenario < 2000; scenario++) {
      float initial = random.nextInt(17) / 16f;
      var baseline = new BaselineWaterSurfaceTransition(initial, 0);
      var actual = new WaterSurfaceTransition(initial, 0);
      long now = 0;
      for (int step = 0; step < 100; step++) {
        now += random.nextInt(75);
        float next = random.nextInt(17) / 16f;
        if ((step & 1) == 0) {
          baseline.target(next, now, step);
          actual.target(next, now, step);
        } else {
          baseline.target(next, now);
          actual.target(next, now);
        }
        same(actual.target(), baseline.target(), "finite target parity");
        same(actual.at(now), baseline.at(now), "finite interpolation parity");
        same(actual.at(now + 50), baseline.at(now + 50), "finite next-frame parity");
        need(actual.remaining(now) == baseline.remaining(now), "finite cleanup parity");
        need(actual.offset(now) == baseline.offset(now), "finite offset parity");
      }
    }
    System.out.println("WATER_SURFACE_FINITE_PASS assertions=" + checks
        + " finiteParityScenarios=2000 finiteParitySteps=200000"
        + " invalidConstructorSanitized=3 invalidVersionedIgnored=3 invalidUnversionedIgnored=3"
        + " revisionNotPoisoned=true");
  }
  private WaterSurfaceFiniteChecks() {}
}

/**
 * Presentation only. Retarget from the currently displayed value, never from the last tick.
 * Versioned (surface height) samples settle finite-parcel ping-pong: a change of at most one
 * unit that reverts within {@link #SETTLE_MILLIS} shows the pair's midpoint, so steady through-flow
 * renders as one still surface instead of bobbing 1/16 block per passing parcel. Unchanged
 * recaptures do not extend that hold; a cleanup retarget returns stopped flow to its real height.
 */
final class BaselineWaterSurfaceTransition {
  public static final int MILLIS = 100;
  public static final int SETTLE_MILLIS = 400;
  public static final float SETTLE_BAND = 1 / 16f + 0.0001f;
  private float from, target;
  private float lastRaw, previousRaw = Float.NaN;
  private long rawAt = Long.MIN_VALUE / 2;
  private long settleUntil = Long.MIN_VALUE;
  private long started;
  private long revision = Long.MIN_VALUE;

  public BaselineWaterSurfaceTransition(float initial, long now) {
    from = target = lastRaw = initial;
    started = now;
  }

  public float at(long now) {
    float t = Math.clamp((now - started) / (float) MILLIS, 0, 1);
    return from + (target - from) * t;
  }

  public void target(float next, long now) {
    if (Math.abs(next - target) < 0.00001f) return;
    from = at(now);
    target = next;
    started = now;
  }

  public void target(float next, long now, long version) {
    if (version < revision) return;
    revision = version;
    advance(now);
    // Neighboring pages resubmit the same shared corner; that is not a new sample.
    if (Math.abs(next - lastRaw) < 0.00001f) return;
    boolean reverts = Math.abs(next - previousRaw) < 0.00001f
        && Math.abs(next - lastRaw) <= SETTLE_BAND
        && now - rawAt <= SETTLE_MILLIS;
    float shown = reverts ? (next + lastRaw) * .5f : next;
    settleUntil = reverts ? now + SETTLE_MILLIS : Long.MIN_VALUE;
    previousRaw = lastRaw;
    lastRaw = next;
    rawAt = now;
    target(shown, now);
  }

  /** Service presentation expiry even when the page that last changed is no longer visible. */
  public void advance(long now) {
    if (settleUntil != Long.MIN_VALUE && now >= settleUntil) {
      settleUntil = Long.MIN_VALUE;
      target(lastRaw, now);
    }
  }

  public float target() {
    return target;
  }

  /** Cleanup must also wake a visually still midpoint after the last real volume change. */
  public int remaining(long now) {
    long animation = Math.abs(from - target) < .00001f ? 0 : Math.max(0, MILLIS - (now - started));
    return (int) Math.clamp(Math.max(animation, settleUntil == Long.MIN_VALUE ? 0 : settleUntil - now),
        0, Integer.MAX_VALUE);
  }

  public void restart(long now) {
    from = at(now);
    started = now;
  }

  public float from(long now) {
    return now - started >= MILLIS ? target : from;
  }

  public int offset(long now) {
    return (int) Math.clamp(started - now, -32768, 0);
  }
}
