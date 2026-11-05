package com.shnewbs.deepprimal.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

/** Public corner mutation APIs must not carry invalid directions into adjacent corners. */
public final class WaterCornerMutationAdmissionChecks {
  private static final long[] POW = {1, 289, 83521, 24137569};
  private static long checks;

  private static void need(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message + " check=" + checks);
  }

  private static ByteBuffer packet(int[] to, int[] from) {
    long pairs = 0;
    for (int n = 0; n < 4; n++) pairs += (to[n] * 17L + from[n]) * POW[n];
    ByteBuffer b = ByteBuffer.allocate(144).order(ByteOrder.nativeOrder());
    for (int v = 0; v < 4; v++)
      WaterCornerPacket.write(b, v * 36, pairs, 100, 200, 300, 400, (v & 1) == 0);
    return b;
  }

  private static void verify(ByteBuffer b, int[] to, int[] from, int changed, int expectedTo,
      int expectedFrom, String operation) {
    for (int v = 0; v < 4; v++) {
      for (int n = 0; n < 4; n++) {
        need(WaterCornerPacket.target(b, v * 36, n) == (n == changed ? expectedTo : to[n]),
            operation + " target corner=" + n + " vertex=" + v);
        need(WaterCornerPacket.source(b, v * 36, n) == (n == changed ? expectedFrom : from[n]),
            operation + " source corner=" + n + " vertex=" + v);
      }
      need(WaterCornerPacket.opacityTarget(b, v * 36) == ((v & 1) == 0 ? 127 : 0),
          operation + " opacity vertex=" + v);
    }
  }

  public static void main(String[] args) {
    Random random = new Random(0x155a11L);
    for (int iteration = 0; iteration < 10000; iteration++) {
      int[] to = new int[4], from = new int[4];
      for (int n = 0; n < 4; n++) {
        to[n] = random.nextInt(17);
        from[n] = random.nextInt(17);
      }
      int lane = iteration & 3;
      int candidateTo = random.nextInt(32), candidateFrom = random.nextInt(32);
      int expectedTo = Math.min(16, candidateTo), expectedFrom = Math.min(16, candidateFrom);
      ByteBuffer direct = packet(to, from);
      WaterCornerPacket.corner(direct, 0, lane, candidateTo, candidateFrom, 0, 500);
      verify(direct, to, from, lane, expectedTo, expectedFrom, "corner");
      ByteBuffer bulk = packet(to, from);
      WaterCornerPacket.patchCorners(bulk, 0, 1 << lane, candidateTo << (lane * 5),
          candidateFrom << (lane * 5), 0, 500);
      verify(bulk, to, from, lane, expectedTo, expectedFrom, "patchCorners");
      for (int v = 0; v < 4; v++)
        need(direct.getLong(v * 36 + 16) == bulk.getLong(v * 36 + 16),
            "direct and bulk pair-packet parity");
    }
    for (int invalid : new int[] {-100, -1, 17, 31, 32, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
      int[] to = {1, 2, 3, 4}, from = {5, 6, 7, 8};
      ByteBuffer b = packet(to, from);
      WaterCornerPacket.corner(b, 0, 0, invalid, invalid, 0, 500);
      verify(b, to, from, 0, 16, 16, "unbounded corner");
    }
    System.out.println("WATER_CORNER_MUTATION_ADMISSION_PASS checks=" + checks);
  }

  private WaterCornerMutationAdmissionChecks() {}
}
