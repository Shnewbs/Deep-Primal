package com.shnewbs.deepprimal.core;

import java.nio.*;
import java.util.*;

/** Known-input state flags, mixed ages, offset slices and read-only classification. */
final class WaterRetirementStateChecks {
  static void run() {
    Random random = new Random(0x442026); long cases = 0;
    for (int trial = 0; trial < 10000; trial++) for (long now : new long[]{0, 99, 100, 65535, 65536, 131072}) {
      byte[] raw = new byte[720]; random.nextBytes(raw);
      ByteBuffer b = ByteBuffer.wrap(raw).order(ByteOrder.nativeOrder());
      int expected = 0, begin = (trial % 2) * 144, end = begin + 432;
      for (int q = 0; q < 720; q += 144) {
        int[] ages = new int[4]; long pairs = 0, power = 1;
        for (int n = 0; n < 4; n++) {
          int to = random.nextInt(17), from = (trial & 3) == 0 ? to : random.nextInt(17);
          ages[n] = switch ((trial + n + q) % 5) { case 0 -> 0; case 1 -> 99; case 2 -> 100; case 3 -> 65535; default -> random.nextInt(65536); };
          pairs += (to * 17L + from) * power; power *= 289;
          if (q >= begin && q < end && to != from) expected |= ages[n] < 100 ? 1 : 2;
        }
        for (int v = 0; v < 4; v++) WaterCornerPacket.write(b, q + v * 36, pairs,
            (int)(now - ages[0]) & 65535, (int)(now - ages[1]) & 65535,
            (int)(now - ages[2]) & 65535, (int)(now - ages[3]) & 65535, true);
      }
      byte[] before = raw.clone(); b.position(13);
      if (WaterCornerPacket.retirementState(b, begin, end, now) != expected) throw new AssertionError("Known state differs");
      if (WaterCornerPacket.needsRetirement(b, begin, end, now) != ((expected & 2) != 0)) throw new AssertionError("Expiry gate differs");
      if (b.position() != 13 || !Arrays.equals(raw, before)) throw new AssertionError("Read-only state changed data/position");
      var retired = ByteBuffer.wrap(raw.clone()).order(ByteOrder.nativeOrder()); WaterCornerPacket.retire(retired, begin, end, now);
      if (WaterCornerPacket.retirementState(retired, begin, end, now) != (expected & 1)) throw new AssertionError("Retired state differs");
      cases++;
    }
    ByteBuffer legacy = ByteBuffer.allocate(144).order(ByteOrder.nativeOrder());
    if (WaterCornerPacket.retirementState(legacy, 0, 144, 100) != 0) throw new AssertionError("Legacy data polled");
    for (int[] range : new int[][]{{-144,0},{0,288},{1,144},{0,1},{144,0}}) {
      try { WaterCornerPacket.retirementState(legacy,range[0],range[1],0); throw new AssertionError("Invalid range accepted"); }
      catch (IllegalArgumentException expected) { }
    }
    System.out.println("WATER_RETIREMENT_STATE_CORE_PASS cases=" + cases + " mixedAges=true readOnly=true");
  }
  public static void main(String[] args) { run(); }
}
