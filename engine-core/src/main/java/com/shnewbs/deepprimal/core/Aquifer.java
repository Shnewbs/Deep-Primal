package com.shnewbs.deepprimal.core;

/** A shared 64x64-column groundwater store. Output is paid for; blocked delivery has no backlog. */
public final class Aquifer {
  public static final long CAPACITY = 1L << 22;
  public static final long START = CAPACITY / 4, STOP = CAPACITY / 8;
  public static final int WET_STEPS = 30;
  /** Storage depth of newly seeded regions: about 2-6 dry MC days of baseflow above STOP. */
  public static final int DEEP = 64, MAX_DEPTH = 64;
  public final WaterAccount ground, snow;
  private final int depth;
  private int wetSteps, dynamicWetSteps;
  private boolean active;
  private long lastTick;
  public Aquifer(WaterAccount.Parcel ground, WaterAccount.Parcel snow, int wetSteps,
      boolean active, long lastTick) {
    this(ground, snow, wetSteps, active, lastTick, 0);
  }
  public Aquifer(WaterAccount.Parcel ground, WaterAccount.Parcel snow, int wetSteps,
      boolean active, long lastTick, int dynamicWetSteps) {
    this(ground, snow, wetSteps, active, lastTick, dynamicWetSteps, 1);
  }
  /** Legacy saves load at depth 1; thresholds stay the same fractions of the deeper store. */
  public Aquifer(WaterAccount.Parcel ground, WaterAccount.Parcel snow, int wetSteps,
      boolean active, long lastTick, int dynamicWetSteps, int depth) {
    if (depth < 1 || depth > MAX_DEPTH) throw new IllegalArgumentException("Aquifer depth");
    this.depth = depth;
    this.ground = new WaterAccount(CAPACITY * depth, ground);
    this.snow = new WaterAccount(CAPACITY * depth, snow);
    if (wetSteps < 0 || wetSteps > WET_STEPS) throw new IllegalArgumentException("Wet age");
    if (dynamicWetSteps < 0 || dynamicWetSteps > WET_STEPS) throw new IllegalArgumentException("Dynamic wet age");
    this.dynamicWetSteps = dynamicWetSteps;
    this.wetSteps = wetSteps; this.active = active; this.lastTick = lastTick;
  }
  public int depth() { return depth; }
  public long capacity() { return CAPACITY * depth; }
  public long stop() { return STOP * depth; }
  public long start() { return START * depth; }
  public int wetSteps() { return wetSteps; }
  public boolean active() { return active; }
  public int dynamicWetSteps() { return dynamicWetSteps; }
  public boolean canSupply(boolean naturalWaterway) {
    return active && ground.water() > stop() && (naturalWaterway || dynamicWetSteps == WET_STEPS);
  }
  public long lastTick() { return lastTick; }
  /** At most one fixed-duration step, even after long unload/offline gaps. */
  public boolean step(long tick) {
    if (tick <= lastTick) return false;
    // Ordered signed timestamps can span more than Long.MAX_VALUE ticks.
    // A negative subtraction here denotes that large gap, not a short interval.
    long elapsed = tick - lastTick;
    if (elapsed >= 0 && elapsed < 20) return false;
    lastTick = tick;
    if (ground.water() <= stop()) dynamicWetSteps = 0;
    else if (dynamicWetSteps < WET_STEPS)
      dynamicWetSteps = ground.water() >= capacity() * 3 / 4 ? dynamicWetSteps + 1 : 0;
    if (ground.water() <= stop()) { active = false; wetSteps = 0; }
    else if (!active) {
      wetSteps = ground.water() >= start() ? Math.min(WET_STEPS, wetSteps + 1) : 0;
      if (wetSteps == WET_STEPS) active = true;
    }
    return true;
  }
  public double head(double datum) {
    return datum - 5 + 8 * ground.water() / (double) capacity();
  }
  /** Planned perched waterways lose their supply head at the same reserve as drought shutdown. */
  public static double naturalDatum(double surface) {
    if (!Double.isFinite(surface)) throw new IllegalArgumentException("Natural waterway surface");
    return surface + 5 - 8 * STOP / (double) CAPACITY;
  }
  /**
   * Long-run regional baseflow in native units per 20-tick interval: mean rain recharge of a
   * 64x64 region at vanilla's mean weather duty (18,000 rain of 114,000 ticks) and half
   * permeability. Never below the 32-unit dry-land quota; still debited from this finite store.
   */
  public static int baseflowUnits(int rainQuantaPerColumnSecond) {
    long quota = Math.max(0, rainQuantaPerColumnSecond) * 4096L * 18_000
        / 114_000 / 2 / WaterAccount.SCALE;
    // Configured recharge can exceed the signed-int output range. Saturate rather
    // than wrapping a positive finite supply quota into a negative discharge.
    return (int) Math.min(Integer.MAX_VALUE, Math.max(32L, quota));
  }
  public int dischargeUnits(double datum, double surface, int receivingCapacity, int rate) {
    // A malformed height must not turn infinity into an unbounded source pressure.
    // All output is still debited from the finite groundwater account by the caller.
    if (!Double.isFinite(datum) || !Double.isFinite(surface)) return 0;
    if (!active || ground.water() <= stop() || head(datum) <= surface || receivingCapacity <= 0) return 0;
    int pressure = (int) Math.ceil((head(datum) - surface) * 2);
    // Multiple outlets can deliver before step() next updates the drought latch.
    return (int) Math.min((ground.water() - stop()) / WaterAccount.SCALE,
        Math.min(receivingCapacity, Math.min(Math.max(0, rate), pressure)));
  }
}
