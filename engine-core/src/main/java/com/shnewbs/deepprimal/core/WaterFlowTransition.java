package com.shnewbs.deepprimal.core;

/** Two texture endpoints and one latest request. Never retarget an unfinished blend. */
public final class WaterFlowTransition {
  public static final int MILLIS = 100;
  private int from, target, pending;
  private long started, revision = Long.MIN_VALUE;
  public WaterFlowTransition(int initial, long now) {
    from = target = pending = initial;
    started = now;
  }
  public void target(int next, long now, long version) {
    if (version < revision) return;
    revision = version;
    pending = next;
    if (next == 16 || target == 16) {
      from = target = next;
      started = now;
    } else if ((from == target || now - started >= MILLIS) && next != target) {
      from = target;
      target = next;
      started = now;
    }
  }
  public int target() { return target; }
  public int source() { return from; }
  public int age(long now) { return (int)Math.clamp(now - started, 0, MILLIS); }
  public boolean active(long now) { return from < 16 && target < 16 && from != target && age(now) < MILLIS; }
  public boolean queued(long now) { return active(now) && pending != target; }
}
