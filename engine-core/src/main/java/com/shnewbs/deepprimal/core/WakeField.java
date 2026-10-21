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
    double length = Math.hypot(vx, vz);
    if (!Double.isFinite(x + y + z + length + strength) || length <= 0) return;
    int i = next;
    next = (next + 1) % CAPACITY;
    this.x[i] = x;
    this.y[i] = y;
    this.z[i] = z;
    dx[i] = (float) (vx / length);
    dz[i] = (float) (vz / length);
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
