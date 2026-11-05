package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Regression: finite wake inputs must not be rejected due to arithmetic overflow. */
public final class WakeFieldFiniteAdmissionChecks {
  private static long assertions;

  private static void check(boolean condition, String message) {
    assertions++;
    if (!condition) throw new AssertionError(message);
  }

  private static void near(double actual, double expected, double tolerance, String message) {
    check(Double.isFinite(actual) && Math.abs(actual - expected) <= tolerance, message);
  }

  public static void main(String[] args) {
    var field = new WakeField();
    // Previously rejected: adding finite coordinates overflows before the finite check.
    field.add(Double.MAX_VALUE, Double.MAX_VALUE, 0, 1, 0, 1);
    check(field.alive(0), "finite position must be admitted despite sum overflow");
    check(field.x(0) == Double.MAX_VALUE && field.y(0) == Double.MAX_VALUE,
        "finite positions preserved exactly");
    near(field.dx(0), 1, 0, "unit X");
    near(field.dz(0), 0, 0, "unit Z");

    // Previously rejected: hypot overflows although both velocity components are finite.
    field.add(1, 2, 3, Double.MAX_VALUE, Double.MAX_VALUE, 0.75);
    check(field.alive(1), "finite high-magnitude velocity must be admitted");
    near(field.dx(1), Math.sqrt(.5), 1e-7, "scaled unit X");
    near(field.dz(1), Math.sqrt(.5), 1e-7, "scaled unit Z");
    near(field.alpha(1), .75 * .55, 1e-7, "strength preserved");

    field.add(1, 2, 3, -Double.MAX_VALUE, Double.MAX_VALUE, 1);
    check(field.alive(2), "opposed high-magnitude velocity must be admitted");
    near(field.dx(2), -Math.sqrt(.5), 1e-7, "negative scaled unit X");
    near(field.dz(2), Math.sqrt(.5), 1e-7, "positive scaled unit Z");

    // Invalid inputs and zero velocity must not consume a ring slot.
    for (double bad : new double[] {Double.NaN, Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY}) {
      field.add(bad, 0, 0, 1, 0, 1);
      field.add(0, bad, 0, 1, 0, 1);
      field.add(0, 0, bad, 1, 0, 1);
      field.add(0, 0, 0, bad, 1, 1);
      field.add(0, 0, 0, 1, bad, 1);
      field.add(0, 0, 0, 1, 0, bad);
    }
    field.add(0, 0, 0, 0, 0, 1);
    field.add(4, 5, 6, 1, 0, 1);
    check(field.alive(3) && field.x(3) == 4, "invalid input did not advance ring");
    check(!field.alive(4), "unused slot remains empty");

    // Existing finite normalizations retain their exact floating-point values.
    Random random = new Random(0x57a9c3L);
    for (int i = 0; i < 200_000; i++) {
      double vx = (random.nextDouble() - .5) * 20;
      double vz = (random.nextDouble() - .5) * 20;
      double x = (random.nextDouble() - .5) * 60_000_000;
      double z = (random.nextDouble() - .5) * 60_000_000;
      double strength = random.nextDouble() * 1.5 - .25;
      int slot = (i + 4) % WakeField.CAPACITY;
      field.add(x, 80, z, vx, vz, strength);
      check(field.alive(slot), "ordinary slot alive");
      check(Double.doubleToLongBits(field.x(slot)) == Double.doubleToLongBits(x), "X parity");
      check(Double.doubleToLongBits(field.z(slot)) == Double.doubleToLongBits(z), "Z parity");
      check(Float.floatToIntBits(field.dx(slot)) == Float.floatToIntBits((float)(vx / Math.hypot(vx, vz))),
          "X direction parity");
      check(Float.floatToIntBits(field.dz(slot)) == Float.floatToIntBits((float)(vz / Math.hypot(vx, vz))),
          "Z direction parity");
      near(field.alpha(slot), (float)Math.clamp(strength, 0, 1) * .55f, 1e-7,
          "alpha parity");
    }
    for (int t = 0; t < WakeField.LIFETIME; t++) field.tick();
    for (int i = 0; i < WakeField.CAPACITY; i++) {
      check(!field.alive(i), "all stamps expire");
      near(field.alpha(i), 0, 0, "expired alpha");
    }
    System.out.println("WAKE_FINITE_ADMISSION_PASS assertions=" + assertions);
  }

  private WakeFieldFiniteAdmissionChecks() {}
}
