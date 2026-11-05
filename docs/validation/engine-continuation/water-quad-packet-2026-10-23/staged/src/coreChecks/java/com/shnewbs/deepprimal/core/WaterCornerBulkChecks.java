package com.shnewbs.deepprimal.core;

import java.nio.*;
import java.util.*;

/** Known-input mask/epoch oracle plus exact differential compatibility with scalar publication. */
final class WaterCornerBulkChecks {
  private static void need(boolean pass, String why) { if (!pass) throw new AssertionError(why); }
  static void run() {
    Random random = new Random(0x20291023);
    long[] clocks = {0, 99, 65520, 65535, 65536, 131071, Long.MAX_VALUE - 1000};
    long cases = 0; int masks = 0;
    for (int trial = 0; trial < 30000; trial++) for (long now : clocks) {
      int q = trial % 2 == 0 ? 0 : 144;
      byte[] raw = new byte[432]; random.nextBytes(raw);
      var actual = ByteBuffer.wrap(raw.clone()).order(ByteOrder.nativeOrder());
      int[] to = new int[4], from = new int[4], oldAges = new int[4], nextTo = new int[4], nextFrom = new int[4], nextAge = new int[4];
      long pairs = 0, power = 1; int mask = trial & 15, targets = 0, sources = 0, ages = 0, expected = 0;
      for (int n = 0; n < 4; n++) {
        to[n] = random.nextInt(17); from[n] = random.nextInt(17); oldAges[n] = random.nextInt(101);
        pairs += (to[n] * 17L + from[n]) * power; power *= 289;
        nextTo[n] = random.nextBoolean() ? to[n] : random.nextInt(17);
        nextFrom[n] = random.nextBoolean() ? from[n] : random.nextInt(17);
        nextAge[n] = random.nextBoolean() ? oldAges[n] : random.nextInt(101);
        if ((mask & (1 << n)) != 0) {
          boolean same = to[n] == nextTo[n] && (from[n] == to[n] || oldAges[n] >= 100
              ? nextFrom[n] == nextTo[n] : from[n] == nextFrom[n] && oldAges[n] == nextAge[n]);
          if (!same) expected |= 1 << n;
        }
        targets |= nextTo[n] << (n * 5); sources |= nextFrom[n] << (n * 5); ages |= nextAge[n] << (n * 8);
      }
      for (int v = 0; v < 4; v++) WaterCornerPacket.write(actual, q + v * 36, pairs,
          (int)(now - oldAges[0]) & 65535, (int)(now - oldAges[1]) & 65535,
          (int)(now - oldAges[2]) & 65535, (int)(now - oldAges[3]) & 65535, random.nextBoolean());
      byte[] before = actual.array().clone();
      var scalar = ByteBuffer.wrap(before.clone()).order(ByteOrder.nativeOrder());
      int scalarMask = 0;
      for (int n = 0; n < 4; n++) if ((mask & (1 << n)) != 0 &&
          !WaterCornerPacket.matches(scalar, q, n, nextTo[n], nextFrom[n], nextAge[n], now)) {
        scalarMask |= 1 << n;
        WaterCornerPacket.corner(scalar, q, n, nextTo[n], nextFrom[n], nextAge[n], now);
      }
      int changed = WaterCornerPacket.changedCorners(actual, q, mask, targets, sources, ages, now);
      need(changed == expected && scalarMask == expected, "Known-input changed mask differs");
      need(Arrays.equals(before, actual.array()), "Read-only changed mask mutated accepted bytes");
      WaterCornerPacket.patchCorners(actual, q, changed, targets, sources, ages, now);
      need(Arrays.equals(actual.array(), scalar.array()), "Bulk/scalar exact bytes differ");
      for (int v = 0; v < 4; v++) {
        int p = q + v * 36;
        for (int n = 0; n < 4; n++) {
          boolean update = (expected & (1 << n)) != 0;
          need(WaterCornerPacket.target(actual,p,n) == (update ? nextTo[n] : to[n]), "Target differs");
          need(WaterCornerPacket.source(actual,p,n) == (update ? nextFrom[n] : from[n]), "Source differs");
          need(WaterCornerPacket.start(actual,p,n) == ((int)(now - (update ? nextAge[n] : oldAges[n])) & 65535), "Start differs");
        }
        need(WaterCornerPacket.light(actual,p,28) == (before[p+28] & 240) && WaterCornerPacket.light(actual,p,30) == (before[p+30] & 240), "Light changed");
        need(WaterCornerPacket.opacityTarget(actual,p) == ((before[p+33] & 64) != 0 ? 127 : 0), "Opacity changed");
      }
      for (int at = 0; at < actual.capacity(); at++) {
        int slot = at - q, within = Math.floorMod(slot,36);
        if (slot < 0 || slot >= 144 || !(within >= 16 && within <= 23) && !(within >= 27 && within <= 31) && within != 33)
          need(actual.get(at) == before[at], "Nonmaterial/outside-quad byte changed");
      }
      masks |= 1 << mask; cases++;
    }
    need(masks == 65535, "Mask coverage incomplete");
    System.out.println("WATER_CORNER_BULK_CORE_PASS cases=" + cases + " masks=16 scalarBytesIdentical=true readOnlyPredicate=true arbitraryVertexOpacityLightPreserved=true nonzeroOffsets=true");
  }
  public static void main(String[] args) { run(); }
}
