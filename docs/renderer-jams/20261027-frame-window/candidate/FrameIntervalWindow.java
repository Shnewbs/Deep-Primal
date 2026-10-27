package com.shnewbs.deepprimal.core;

/** Bounded CPU frame intervals, including waits/limiting; never GPU execution time.
 * Context changes discard the crossing interval and all earlier samples.
 */
public final class FrameIntervalWindow {
  private final RollingTiming intervals;
  private Object world;
  private int width, height, fpsLimit, fullDistance, verticalDistance, lodDistance;
  private boolean focused, initialized;
  private long previous, generation;

  public FrameIntervalWindow(int capacity) {
    intervals = new RollingTiming(capacity);
  }

  /** Call on every observed frame, before publishing diagnostics. Returns true on a reset.
   * World uses identity so reconnects to the same dimension start a new window.
   */
  public synchronized boolean observe(long now, Object world, int width, int height,
      boolean focused, int fpsLimit, int fullDistance, int verticalDistance, int lodDistance) {
    boolean changed = !initialized || this.world != world || this.width != width
        || this.height != height || this.focused != focused || this.fpsLimit != fpsLimit
        || this.fullDistance != fullDistance || this.verticalDistance != verticalDistance
        || this.lodDistance != lodDistance || now - previous < 0;
    if (changed) {
      intervals.reset();
      generation++;
      this.world = world;
      this.width = width;
      this.height = height;
      this.focused = focused;
      this.fpsLimit = fpsLimit;
      this.fullDistance = fullDistance;
      this.verticalDistance = verticalDistance;
      this.lodDistance = lodDistance;
      initialized = true;
    } else intervals.record(now - previous);
    previous = now;
    return changed;
  }

  public synchronized RollingTiming.Snapshot snapshot() { return intervals.snapshot(); }
  public synchronized long generation() { return generation; }

  /** Release the world reference and discard an observation gap, e.g. dashboard disabled. */
  public synchronized void clear() {
    intervals.reset();
    world = null;
    initialized = false;
  }
}
