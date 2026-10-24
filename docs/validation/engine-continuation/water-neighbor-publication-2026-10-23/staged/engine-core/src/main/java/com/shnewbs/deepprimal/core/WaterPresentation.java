package com.shnewbs.deepprimal.core;

import java.nio.ByteBuffer;

/** Rebase accepted presentation bytes without touching geometry, light or volume. */
public final class WaterPresentation {
  private static int age(int value, long elapsed) { return (int)Math.min(100, value + Math.min(elapsed, 100)); }
  private static int offset(int value, long elapsed, int limit) {
    return (int)Math.max(-limit, value - Math.min(elapsed, limit));
  }
  public static void rebase(ByteBuffer b, long elapsed) {
    if (elapsed < 0 || b.limit() % 144 != 0) throw new IllegalArgumentException("Invalid water clock/payload");
    for (int p = 0; p < b.limit(); p += 36) {
      if (b.getFloat(p + 16) < -1) {
        int to = Math.round(-b.getFloat(p + 16)) - 2, from = Math.round(b.getFloat(p + 20));
        int d = ((to >>> 20) & 15) | (((from >>> 20) & 7) << 4);
        int a = b.getShort(p + 26) & 65535;
        b.putShort(p + 26, (short)((offset((byte)a, elapsed, 100) & 255) | (age(a >>> 8, elapsed) << 8)));
        for (int at = 28; at <= 30; at += 2) {
          int v = b.getShort(p + at) & 65535;
          b.putShort(p + at, (short)((v & 255) | (age(v >>> 8, elapsed) << 8)));
        }
        b.putFloat(p + 16, WaterFlowField.targets(to & 1048575, age(d, elapsed)));
        b.putFloat(p + 20, WaterFlowField.sources(from & 1048575, age(d, elapsed)));
      } else {
        b.putShort(p + 26, (short)offset(b.getShort(p + 26), elapsed, 32768));
        int sky = b.getShort(p + 30) & 65535;
        if ((sky & 32768) != 0) b.putShort(p + 30, (short)((sky & 255) | 32768 | (age((sky >>> 8) & 127, elapsed) << 8)));
      }
      b.put(p + 34, (byte)offset(b.get(p + 34), elapsed, 100));
    }
  }
  public static boolean matches(ByteBuffer b, int quad, int n, int target, int source, int desiredAge, long elapsed) {
    int to = Math.round(-b.getFloat(quad + 16)) - 2, from = Math.round(b.getFloat(quad + 20));
    int oldTarget = (to >>> (n * 5)) & 31, oldSource = (from >>> (n * 5)) & 31;
    int encodedAge = n == 3 ? ((to >>> 20) & 15) | (((from >>> 20) & 7) << 4)
        : (b.getShort(quad + (n == 0 ? 26 : n == 1 ? 28 : 30)) & 65535) >>> 8;
    int currentAge = age(encodedAge, elapsed);
    if (oldTarget != target) return false;
    return oldSource == oldTarget || currentAge >= 100 ? source == target
        : oldSource == source && currentAge == desiredAge;
  }
  public static void corner(ByteBuffer b, int quad, int n, int target, int source, int age) {
    if (quad < 0 || quad % 144 != 0 || quad + 144 > b.limit() || n < 0 || n > 3 || target < 0 || target > 16 || source < 0 || source > 16 || age < 0 || age > 100)
      throw new IllegalArgumentException("Invalid corner state");
    int mask = 31 << (n * 5);
    for (int v = 0; v < 4; v++) {
      int p = quad + v * 36;
      int to = Math.round(-b.getFloat(p + 16)) - 2, from = Math.round(b.getFloat(p + 20));
      to = (to & ~mask) | (target << (n * 5)); from = (from & ~mask) | (source << (n * 5));
      int d = n == 3 ? age : ((to >>> 20) & 15) | (((from >>> 20) & 7) << 4);
      b.putFloat(p + 16, WaterFlowField.targets(to & 1048575, d));
      b.putFloat(p + 20, WaterFlowField.sources(from & 1048575, d));
      if (n < 3) {
        int at = p + (n == 0 ? 26 : n == 1 ? 28 : 30);
        b.putShort(at, (short)((b.getShort(at) & 255) | (age << 8)));
      }
    }
  }
  private WaterPresentation() {}
}
