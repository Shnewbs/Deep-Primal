package com.shnewbs.deepprimal.core;

/** Fixed-size surface trails. Admission replaces the oldest slot; every stamp expires. */
public final class WakeField {
  public static final int CAPACITY = 128, LIFETIME = 40;
  private final double[] x = new double[CAPACITY],
      y = new double[CAPACITY],
      z = new double[CAPACITY];
  private final float[] dx = new float[CAPACITY],
      dz = new float[CAPACITY],
      strength = new float[CAPACITY];
  private final int[] age = new int[CAPACITY];
  private int next;

  public WakeField() {
    java.util.Arrays.fill(age, LIFETIME);
  }

  public void add(double x, double y, double z, double vx, double vz, double strength) {
    // Checking a sum rejects finite positions whenever its intermediate sum overflows.
    if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
        || !Double.isFinite(vx) || !Double.isFinite(vz) || !Double.isFinite(strength)) return;
    double length = Math.hypot(vx, vz);
    if (length == 0) return;
    double unitX, unitZ;
    if (Double.isInfinite(length)) {
      // hypot may overflow for two finite, nonzero velocities; scale before normalizing.
      double scale = Math.max(Math.abs(vx), Math.abs(vz));
      double scaledX = vx / scale, scaledZ = vz / scale;
      double scaledLength = Math.hypot(scaledX, scaledZ);
      unitX = scaledX / scaledLength;
      unitZ = scaledZ / scaledLength;
    } else {
      unitX = vx / length;
      unitZ = vz / length;
    }
    int i = next;
    next = (next + 1) % CAPACITY;
    this.x[i] = x;
    this.y[i] = y;
    this.z[i] = z;
    dx[i] = (float) unitX;
    dz[i] = (float) unitZ;
    this.strength[i] = (float) Math.clamp(strength, 0, 1);
    age[i] = 0;
  }

  public void tick() {
    for (int i = 0; i < CAPACITY; i++) if (age[i] < LIFETIME) age[i]++;
  }

  public boolean alive(int i) {
    return age[i] < LIFETIME;
  }

  public double x(int i) {
    return x[i];
  }

  public double y(int i) {
    return y[i];
  }

  public double z(int i) {
    return z[i];
  }

  public float dx(int i) {
    return dx[i];
  }

  public float dz(int i) {
    return dz[i];
  }

  public float spread(int i) {
    return .25f + age[i] * .035f;
  }

  public float alpha(int i) {
    return strength[i] * (1 - age[i] / (float) LIFETIME) * .55f;
  }
}
