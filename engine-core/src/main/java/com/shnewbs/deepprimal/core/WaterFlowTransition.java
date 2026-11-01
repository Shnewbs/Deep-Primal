package com.shnewbs.deepprimal.core;

/** Two texture endpoints and one latest request. Never retarget an unfinished blend. */
public final class WaterFlowTransition {
  public static final int MILLIS = 100;
  private int from, target, pending;
  private long started, revision = Long.MIN_VALUE;
  public WaterFlowTransition(int initial, long now) {
    // A malformed captured direction must never become a shader material ID.
    if (!validDirection(initial)) initial = 16;
    from = target = pending = initial;
    started = now;
  }
  private static boolean validDirection(int direction) {
    // IDs 0..15 represent flow vectors; 16 is still water.
    return direction >= 0 && direction <= 16;
  }
  public void target(int next, long now, long version) {
    // Reject before recording the revision. Bad snapshots must not suppress later
    // valid publications, even if the malformed snapshot has a newer version.
    if (!validDirection(next) || version < revision) return;
    revision = version;
    pending = next;
    if ((from == target || now - started >= MILLIS) && next != target) {
      from = target;
      target = next;
      started = now;
    }
  }
  public int target() { return target; }
  public int source() { return from; }
  public int age(long now) { return (int)Math.clamp(now - started, 0, MILLIS); }
  public boolean active(long now) { return from != target && age(now) < MILLIS; }
  public boolean queued(long now) { return active(now) && pending != target; }
}
