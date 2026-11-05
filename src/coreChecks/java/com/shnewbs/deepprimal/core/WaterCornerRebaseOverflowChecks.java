package com.shnewbs.deepprimal.core;

import java.nio.ByteBuffer;
import java.util.Random;

/** Regression: signed-long rebase overflow must not revive expired queued water corners. */
public final class WaterCornerRebaseOverflowChecks {
  private static long checks;
  private static void check(boolean condition, String why) {
    checks++;
    if (!condition) throw new AssertionError(why);
  }
  private static ByteBuffer packet() {
    ByteBuffer b = ByteBuffer.allocate(144);
    for (int v = 0; v < 4; v++)
      WaterCornerPacket.write(b, v * 36, 0, 0, 0, 0, 0, false);
    return b;
  }
  private static void test(int corner, long queuedAt, long now, int expectedAge) {
    ByteBuffer queued = packet(), current = packet();
    WaterCornerPacket.corner(queued, 0, corner, 1, 0, 0, queuedAt);
    WaterCornerPacket.rebaseCorners(current, 0, queued, 0, 1 << corner, queuedAt, now);
    check(WaterCornerPacket.target(current, 0, corner) == 1,
        "target retained corner " + corner);
    check(WaterCornerPacket.source(current, 0, corner) == (expectedAge == 100 ? 1 : 0),
        "source retired corner " + corner + " expected age " + expectedAge);
    check(WaterCornerPacket.age(current, 0, corner, now) == expectedAge,
        "age corner " + corner);
  }
  public static void main(String[] args) {
    for (int n = 0; n < 4; n++) {
      test(n, Long.MIN_VALUE + 10, Long.MAX_VALUE - 10, 100);
      test(n, Long.MIN_VALUE + 10, Long.MIN_VALUE + 60, 50);
      test(n, Long.MIN_VALUE + 10, Long.MIN_VALUE + 110, 100);
      test(n, 1000, 999, 0);
      test(n, 1000, 1099, 99);
      test(n, 1000, 1100, 100);
    }
    Random r = new Random(20261101);
    for (int i = 0; i < 10_000; i++) {
      long queuedAt = r.nextInt(1_000_001) - 500_000L;
      long now = queuedAt + r.nextInt(301) - 100L;
      int expected = (int) Math.max(0, Math.min(100, now - queuedAt));
      test(i % 4, queuedAt, now, expected);
    }
    System.out.println("WATER_CORNER_REBASE_OVERFLOW_PASS checks=" + checks);
  }
  private WaterCornerRebaseOverflowChecks() {}
}
