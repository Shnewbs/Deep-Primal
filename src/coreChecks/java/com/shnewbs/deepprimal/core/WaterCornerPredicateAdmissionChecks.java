package com.shnewbs.deepprimal.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;

/** Direction admission must agree between read-only predicates and actual packet publication. */
public final class WaterCornerPredicateAdmissionChecks {
  private static final long[] POW = {1, 289, 83521, 24137569};
  private static long checks;

  private static void need(boolean condition, String reason) {
    checks++;
    if (!condition) throw new AssertionError(reason + " assertion=" + checks);
  }

  private static ByteBuffer packet(int[] to, int[] from, int[] oldAges, long now) {
    long pairs = 0;
    for (int n = 0; n < 4; n++) pairs += (to[n] * 17L + from[n]) * POW[n];
    ByteBuffer b = ByteBuffer.allocate(144).order(ByteOrder.nativeOrder());
    for (int v = 0; v < 4; v++) WaterCornerPacket.write(b, v * 36, pairs,
        (int) (now - oldAges[0]) & 65535, (int) (now - oldAges[1]) & 65535,
        (int) (now - oldAges[2]) & 65535, (int) (now - oldAges[3]) & 65535,
        (v & 1) == 0);
    return b;
  }

  private static int expected(int[] oldTo, int[] oldFrom, int[] oldAges,
      int[] requestedTo, int[] requestedFrom, int[] requestedAges, int mask) {
    int changed = 0;
    for (int n = 0; n < 4; n++) {
      if ((mask & (1 << n)) == 0) continue;
      int to = Math.min(16, requestedTo[n]);
      int from = Math.min(16, requestedFrom[n]);
      boolean same = oldTo[n] == to && (oldFrom[n] == to || oldAges[n] >= 100
          ? from == to : oldFrom[n] == from && oldAges[n] == requestedAges[n]);
      if (!same) changed |= 1 << n;
    }
    return changed;
  }

  private static void run(int[] oldTo, int[] oldFrom, int[] oldAges,
      int[] requestedTo, int[] requestedFrom, int[] requestedAges, int mask, long now) {
    int targets = 0, sources = 0, ages = 0;
    for (int n = 0; n < 4; n++) {
      targets |= requestedTo[n] << (n * 5);
      sources |= requestedFrom[n] << (n * 5);
      ages |= requestedAges[n] << (n * 8);
    }
    ByteBuffer b = packet(oldTo, oldFrom, oldAges, now);
    byte[] before = b.array().clone();
    int expected = expected(oldTo, oldFrom, oldAges, requestedTo, requestedFrom,
        requestedAges, mask);
    int changed = WaterCornerPacket.changedCorners(b, 0, mask, targets, sources, ages, now);
    need(changed == expected, "changed mask differs expected=" + expected + " actual=" + changed);
    need(Arrays.equals(before, b.array()), "read-only predicate mutated packet");
    for (int n = 0; n < 4; n++) {
      int safeTo = Math.min(16, requestedTo[n]);
      int safeFrom = Math.min(16, requestedFrom[n]);
      boolean same = oldTo[n] == safeTo && (oldFrom[n] == safeTo || oldAges[n] >= 100
          ? safeFrom == safeTo : oldFrom[n] == safeFrom && oldAges[n] == requestedAges[n]);
      need(WaterCornerPacket.matches(b, 0, n, requestedTo[n], requestedFrom[n],
          requestedAges[n], now) == same, "scalar predicate mismatch corner=" + n);
    }
    WaterCornerPacket.patchCorners(b, 0, changed, targets, sources, ages, now);
    for (int v = 0; v < 4; v++) {
      for (int n = 0; n < 4; n++) {
        int expectedTo = (changed & (1 << n)) == 0 ? oldTo[n] : Math.min(16, requestedTo[n]);
        int expectedFrom = (changed & (1 << n)) == 0 ? oldFrom[n] : Math.min(16, requestedFrom[n]);
        need(WaterCornerPacket.target(b, v * 36, n) == expectedTo, "target corruption");
        need(WaterCornerPacket.source(b, v * 36, n) == expectedFrom, "source corruption");
      }
      need(WaterCornerPacket.opacityTarget(b, v * 36) == ((v & 1) == 0 ? 127 : 0),
          "vertex opacity changed");
    }
    if (changed == 0) need(Arrays.equals(before, b.array()), "unchanged packet modified");
    // A freshly published transition is stable; expired transitions remain subject to
    // the existing retirement rule (from must equal to after 100 ms).
    boolean unretiredExpired = false;
    for (int n = 0; n < 4; n++) if ((changed & (1 << n)) != 0
        && agesArrayValue(ages, n) >= 100 && Math.min(16, requestedTo[n]) != Math.min(16, requestedFrom[n]))
      unretiredExpired = true;
    if (!unretiredExpired) need(WaterCornerPacket.changedCorners(b, 0, mask, targets, sources, ages, now) == 0,
        "repeat request incorrectly re-admitted");
  }

  private static int agesArrayValue(int ages, int n) { return (ages >>> (n * 8)) & 255; }

  public static void main(String[] args) {
    Random random = new Random(0x156c0deL);
    for (int trial = 0; trial < 50000; trial++) {
      int[] oldTo = new int[4], oldFrom = new int[4], oldAges = new int[4];
      int[] to = new int[4], from = new int[4], ages = new int[4];
      for (int n = 0; n < 4; n++) {
        oldTo[n] = random.nextInt(17);
        oldFrom[n] = random.nextInt(17);
        oldAges[n] = random.nextInt(101);
        to[n] = random.nextInt(32);
        from[n] = random.nextInt(32);
        ages[n] = random.nextInt(101);
      }
      run(oldTo, oldFrom, oldAges, to, from, ages, trial & 15, 65520L + trial * 37L);
    }
    // Ordinary 0..16 packets retain the established scalar/bulk semantics.
    for (int trial = 0; trial < 50000; trial++) {
      int[] oldTo = new int[4], oldFrom = new int[4], oldAges = new int[4];
      int[] to = new int[4], from = new int[4], ages = new int[4];
      for (int n = 0; n < 4; n++) {
        oldTo[n] = random.nextInt(17);
        oldFrom[n] = random.nextInt(17);
        oldAges[n] = random.nextInt(101);
        to[n] = random.nextInt(17);
        from[n] = random.nextInt(17);
        ages[n] = random.nextInt(101);
      }
      run(oldTo, oldFrom, oldAges, to, from, ages, trial & 15, 131071L + trial * 19L);
    }
    for (int invalid = 17; invalid < 32; invalid++) for (int lane = 0; lane < 4; lane++) {
      int[] still = {16, 16, 16, 16}, oldAges = {0, 0, 0, 0};
      int[] to = still.clone(), from = still.clone(), ages = {0, 0, 0, 0};
      to[lane] = invalid;
      from[lane] = invalid;
      run(still, still, oldAges, to, from, ages, 1 << lane, 65535L);
    }
    // The scalar public API accepts signed ints, unlike packed five-bit bulk lanes.
    ByteBuffer stillPacket = packet(new int[]{16, 16, 16, 16},
        new int[]{16, 16, 16, 16}, new int[]{0, 0, 0, 0}, 12345L);
    for (int invalid : new int[]{-100, -1, 17, 31, 32, Integer.MIN_VALUE, Integer.MAX_VALUE})
      for (int lane = 0; lane < 4; lane++)
        need(WaterCornerPacket.matches(stillPacket, 0, lane, invalid, invalid, 0, 12345L),
            "signed scalar admission mismatch");
    System.out.println("WATER_CORNER_PREDICATE_ADMISSION_PASS assertions=" + checks
        + " randomizedMalformed=50000 randomizedValid=50000 invalidLanes=60");
  }

  private WaterCornerPredicateAdmissionChecks() {}
}
