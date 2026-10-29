package com.shnewbs.deepprimal.lod.client;

import java.lang.reflect.Method;
import java.util.Random;

/** Exact float parity with the previously shipped normal-heading remainder path. */
public final class LodPageFrustumNormalAngleChecks {
  private static long checks;
  private static Method method;

  private static void check(boolean success, String name) {
    ++checks;
    if (!success) throw new AssertionError(name + " check=" + checks);
  }

  private static float originalNormal(float a, float b) {
    float delta = Math.abs((a - b) % 360.0f);
    return Math.min(delta, 360.0f - delta);
  }

  private static float actual(float a, float b) throws ReflectiveOperationException {
    return (float) method.invoke(null, a, b);
  }

  private static void parity(float a, float b) throws ReflectiveOperationException {
    float original = originalNormal(a, b);
    float candidate = actual(a, b);
    check(Float.floatToIntBits(original) == Float.floatToIntBits(candidate),
        "normal bit parity " + a + "," + b + " expected " + original + " got " + candidate);
  }

  public static void main(String[] args) throws ReflectiveOperationException {
    method = LodPageFrustumAdaptiveGate.class.getDeclaredMethod(
        "angleDifference", float.class, float.class);
    method.setAccessible(true);
    float[] edges = {0.0f, -0.0f, 8.0f, -8.0f, 8.000001f, -8.000001f,
        180.0f, -180.0f, 360.0f, -360.0f, Math.nextDown(360.0f),
        Math.nextUp(-360.0f), Math.nextUp(0.0f), -Math.nextUp(0.0f),
        1.0f, -1.0f, 359.0f, -359.0f, 352.0f, -352.0f};
    for (float a : edges) for (float b : edges) parity(a, b);
    Random random = new Random(0xC011ED30L);
    for (int i = 0; i < 350_000; i++) {
      float a = random.nextFloat() * 720.0f - 360.0f;
      float b = random.nextFloat() * 720.0f - 360.0f;
      parity(a, b);
      parity(-a, b);
      parity(a, -b);
      check((actual(a, b) > 8.0f) == (originalNormal(a, b) > 8.0f),
          "8-degree gate parity");
    }
    // Out-of-range finite headings must retain the independent double-modulo
    // path introduced in jam 29, never the new bounded float shortcut.
    float max = Float.MAX_VALUE;
    check(actual(-Math.nextDown(max), max) == 104.0f, "finite overflow witness");
    check(actual(max, -max) == 0.0f, "huge opposite headings wrap");
    check(Float.isInfinite(actual(Float.NaN, 0.0f)), "invalid angle conservative");
    System.out.println("NORMAL_ANGLE_PASS assertions=" + checks);
  }
}
