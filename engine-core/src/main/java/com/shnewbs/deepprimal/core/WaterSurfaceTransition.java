package com.shnewbs.deepprimal.core;

/** Presentation only. Retarget from the currently displayed value, never from the last tick. */
public final class WaterSurfaceTransition {
  public static final int MILLIS = 100;
  private float from, target;
  private long started;
  private long revision = Long.MIN_VALUE;

  public WaterSurfaceTransition(float initial, long now) {
    from = target = initial;
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
    target(next, now);
  }

  public float target() {
    return target;
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
