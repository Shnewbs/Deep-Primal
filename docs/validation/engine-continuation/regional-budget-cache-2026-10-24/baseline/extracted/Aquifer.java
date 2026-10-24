package com.shnewbs.deepprimal.core;

/** A shared 64x64-column groundwater store. Output is paid for; blocked delivery has no backlog. */
public final class Aquifer {
  public static final long CAPACITY = 1L << 22;
  public static final long START = CAPACITY / 4, STOP = CAPACITY / 8;
  public static final int WET_STEPS = 30;
  public final WaterAccount ground, snow;
  private int wetSteps, dynamicWetSteps;
  private boolean active;
  private long lastTick;
  public Aquifer(WaterAccount.Parcel ground, WaterAccount.Parcel snow, int wetSteps,
      boolean active, long lastTick) {
    this(ground, snow, wetSteps, active, lastTick, 0);
  }
  public Aquifer(WaterAccount.Parcel ground, WaterAccount.Parcel snow, int wetSteps,
      boolean active, long lastTick, int dynamicWetSteps) {
    this.ground = new WaterAccount(CAPACITY, ground);
    this.snow = new WaterAccount(CAPACITY, snow);
    if (wetSteps < 0 || wetSteps > WET_STEPS) throw new IllegalArgumentException("Wet age");
    if (dynamicWetSteps < 0 || dynamicWetSteps > WET_STEPS) throw new IllegalArgumentException("Dynamic wet age");
    this.dynamicWetSteps = dynamicWetSteps;
    this.wetSteps = wetSteps; this.active = active; this.lastTick = lastTick;
  }
  public int wetSteps() { return wetSteps; }
  public boolean active() { return active; }
  public int dynamicWetSteps() { return dynamicWetSteps; }
  public boolean canSupply(boolean naturalWaterway) { return active && (naturalWaterway || dynamicWetSteps == WET_STEPS); }
  public long lastTick() { return lastTick; }
  /** At most one fixed-duration step, even after long unload/offline gaps. */
  public boolean step(long tick) {
    if (tick <= lastTick || tick - lastTick < 20) return false;
    lastTick = tick;
    if (ground.water() <= STOP) dynamicWetSteps = 0;
    else if (dynamicWetSteps < WET_STEPS)
      dynamicWetSteps = ground.water() >= CAPACITY * 3 / 4 ? dynamicWetSteps + 1 : 0;
    if (ground.water() <= STOP) { active = false; wetSteps = 0; }
    else if (!active) {
      wetSteps = ground.water() >= START ? Math.min(WET_STEPS, wetSteps + 1) : 0;
      if (wetSteps == WET_STEPS) active = true;
    }
    return true;
  }
  public double head(double datum) {
    return datum - 5 + 8 * ground.water() / (double) CAPACITY;
  }
  public int dischargeUnits(double datum, double surface, int receivingCapacity, int rate) {
    if (!active || head(datum) <= surface || receivingCapacity <= 0) return 0;
    int pressure = (int) Math.ceil((head(datum) - surface) * 2);
    return (int) Math.min(ground.water() / WaterAccount.SCALE,
        Math.min(receivingCapacity, Math.min(Math.max(0, rate), pressure)));
  }
}
