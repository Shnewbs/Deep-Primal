package com.shnewbs.deepprimal.core;

/**
 * Presentation only. Retarget from the currently displayed value, never from the last tick.
 * Versioned (surface height) samples settle finite-parcel ping-pong: a change of at most one
 * unit that reverts within {@link #SETTLE_MILLIS} shows the pair's midpoint, so steady through-flow
 * renders as one still surface instead of bobbing 1/16 block per passing parcel. Unchanged
 * recaptures do not extend that hold; a cleanup retarget returns stopped flow to its real height.
 */
public final class WaterSurfaceTransition {
  public static final int MILLIS = 100;
  public static final int SETTLE_MILLIS = 400;
  public static final float SETTLE_BAND = 1 / 16f + 0.0001f;
  private float from, target;
  private float lastRaw, previousRaw = Float.NaN;
  private long rawAt = Long.MIN_VALUE / 2;
  private long settleUntil = Long.MIN_VALUE;
  private long started;
  private long revision = Long.MIN_VALUE;

  public WaterSurfaceTransition(float initial, long now) {
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
