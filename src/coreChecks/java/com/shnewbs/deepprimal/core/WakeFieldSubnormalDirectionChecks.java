package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Independent regression for underflow-safe finite wake direction normalization. */
public final class WakeFieldSubnormalDirectionChecks {
  private static long assertions;
  private static void check(boolean ok, String what) {
    assertions++;
    if (!ok) throw new AssertionError(what);
  }
  private static void near(double actual, double expected, double tolerance, String what) {
    check(Double.isFinite(actual) && Math.abs(actual - expected) <= tolerance,
        what + " actual=" + actual + " expected=" + expected);
  }
  public static void main(String[] args) {
    var field = new WakeField();
    field.add(1, 2, 3, Double.MIN_VALUE, Double.MIN_VALUE, 1);
    near(field.dx(0), Math.sqrt(.5), 1e-7, "minimum positive X");
    near(field.dz(0), Math.sqrt(.5), 1e-7, "minimum positive Z");
    field.add(1, 2, 3, -Double.MIN_VALUE, Double.MIN_VALUE, 1);
    near(field.dx(1), -Math.sqrt(.5), 1e-7, "minimum negative X");
    near(field.dz(1), Math.sqrt(.5), 1e-7, "minimum positive Z opposed");
    field.add(1, 2, 3, Double.MIN_VALUE, 0, 1);
    near(field.dx(2), 1, 0, "subnormal X axis");
    near(field.dz(2), 0, 0, "subnormal Z axis");
    var random = new Random(0x1730C0DEL);
    for (int i = 0; i < 100_000; i++) {
      // Use exact subnormal integer multiples, including both signs.
      long ax = 1 + random.nextInt(100_000), az = 1 + random.nextInt(100_000);
      double vx = (random.nextBoolean() ? 1 : -1) * ax * Double.MIN_VALUE;
      double vz = (random.nextBoolean() ? 1 : -1) * az * Double.MIN_VALUE;
      double scale = Math.max(Math.abs(vx), Math.abs(vz));
      double sx = vx / scale, sz = vz / scale;
      double norm = Math.hypot(sx, sz);
      int slot = (i + 3) % WakeField.CAPACITY;
      field.add(i, 0, -i, vx, vz, .8);
      check(field.alive(slot), "subnormal admission");
      near(field.dx(slot), sx / norm, 1e-7, "subnormal X direction");
      near(field.dz(slot), sz / norm, 1e-7, "subnormal Z direction");
      near(Math.hypot(field.dx(slot), field.dz(slot)), 1, 1e-7, "unit direction");
    }
    // Ordinary finite values must retain the original bitwise output.
    for (int i = 0; i < 50_000; i++) {
      double vx = (random.nextDouble() - .5) * 200;
      double vz = (random.nextDouble() - .5) * 200;
      int slot = (i + 100_003) % WakeField.CAPACITY;
      field.add(i, 80, 0, vx, vz, 1);
      check(Float.floatToRawIntBits(field.dx(slot))
          == Float.floatToRawIntBits((float)(vx / Math.hypot(vx, vz))), "ordinary X parity");
      check(Float.floatToRawIntBits(field.dz(slot))
          == Float.floatToRawIntBits((float)(vz / Math.hypot(vx, vz))), "ordinary Z parity");
    }
    for (int t = 0; t < WakeField.LIFETIME; t++) field.tick();
    for (int i = 0; i < WakeField.CAPACITY; i++) {
      check(!field.alive(i), "expired wake");
      near(field.alpha(i), 0, 0, "expired alpha");
    }
    System.out.println("WAKE_SUBNORMAL_DIRECTION_PASS assertions=" + assertions);
  }
  private WakeFieldSubnormalDirectionChecks() {}
}
