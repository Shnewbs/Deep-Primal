package com.shnewbs.deepprimal.core;

/** Shared deterministic water math. No world scans, platform state or allocations. */
public final class WaterDynamics {
  public static final double[] SPEEDS = {.35, 1, 1.45, 1.9};

  public static double speed(double slope, double catchment, double width) {
    return Math.clamp(.3 + 2.2 * Math.sqrt(Math.max(0, slope) + .0025)
        + .12 * Math.sqrt(Math.max(1, catchment)) / Math.max(4, width * 3), .35, 1.9);
  }

  public static int band(double speed) {
    int best = 0;
    for (int i = 1; i < SPEEDS.length; i++)
      if (Math.abs(speed - SPEEDS[i]) < Math.abs(speed - SPEEDS[best])) best = i;
    return best;
  }

  public static int mix(int a, int b, double amount) {
    double t = Math.clamp(amount, 0, 1);
    int r = (int) Math.round((a >> 16 & 255) * (1-t) + (b >> 16 & 255) * t);
    int g = (int) Math.round((a >> 8 & 255) * (1-t) + (b >> 8 & 255) * t);
    int blue = (int) Math.round((a & 255) * (1-t) + (b & 255) * t);
    return r << 16 | g << 8 | blue;
  }

  public static int palette(int nativeColor, double temperature, double rainfall) {
    int cold = mix(nativeColor, 0x63a9b6, Math.clamp((.5-temperature)*1.5, 0, .8));
    return mix(cold, 0x77724a, Math.clamp((temperature-.8)*rainfall*.65, 0, .7));
  }

  public static double wave(double x, double z, double ticks, double storm) {
    double p = ticks * .045;
    return (Math.sin(x*.095 + z*.031-p) + .45*Math.sin(z*.17-x*.04-p*1.41)
        + .2*Math.sin(x*.29+z*.12-p*1.9)) * (.10 + Math.clamp(storm,0,1)*.15);
  }

  public static double oceanHeight(double base, double continent, double noise, int floor) {
    double offshore = smooth(Math.clamp((-continent-.25)/.35, 0, 1));
    double trench = smooth(Math.clamp((.19-Math.abs(noise))/.19, 0, 1));
    return Math.max(floor+40, base-offshore*(35+trench*(96-floor-80)));
  }

  private static double smooth(double t) { return t*t*(3-2*t); }
  private WaterDynamics() {}
}
