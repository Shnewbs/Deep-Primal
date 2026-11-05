package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Defend the float-exact GPU corner packet against malformed IDs and ages. */
public final class WaterFlowFieldPacketAdmissionChecks {
  private static long checks;
  private static void check(boolean condition, String reason) {
    checks++;
    if (!condition) throw new AssertionError(reason + " check=" + checks);
  }

  private static int sanitized(int raw) {
    int result = 0;
    for (int shift = 0; shift < 20; shift += 5) {
      int id = (raw >>> shift) & 31;
      result |= (id <= 16 ? id : 16) << shift;
    }
    return result;
  }

  private static void packet(int raw, int age) {
    int word = sanitized(raw), safeAge = Math.clamp(age, 0, 100);
    float targets = WaterFlowField.targets(raw, age);
    float sources = WaterFlowField.sources(raw, age);
    check(Float.isFinite(targets) && Float.isFinite(sources), "nonfinite GPU packet");
    int to = Math.round(-targets) - 2, from = Math.round(sources);
    check((to & 0xfffff) == word && (from & 0xfffff) == word,
        "invalid corner ID or high bits corrupted packed directions raw=" + raw + " age=" + age);
    check((((to >>> 20) & 15) | (((from >>> 20) & 7) << 4)) == safeAge,
        "fourth corner age corrupted raw=" + raw + " age=" + age);
    for (int shift = 0; shift < 20; shift += 5) {
      check(((to >>> shift) & 31) <= 16 && ((from >>> shift) & 31) <= 16,
          "unsupported GPU flow direction");
    }
  }

  public static void main(String[] args) {
    // Exhaust all four five-bit lanes, including the 15 malformed values in each lane.
    for (int a = 0; a < 32; a++) for (int b = 0; b < 32; b++)
      for (int c = 0; c < 32; c++) for (int d = 0; d < 32; d++) {
        int word = a | (b << 5) | (c << 10) | (d << 15);
        packet(word, 15);
      }
    Random random = new Random(0x153c0ffeL);
    for (int i = 0; i < 100_000; i++) {
      packet(random.nextInt(), random.nextInt());
      int valid = random.nextInt(17) | (random.nextInt(17) << 5)
          | (random.nextInt(17) << 10) | (random.nextInt(17) << 15);
      int age = random.nextInt(101);
      // Valid packets retain the exact legacy float bit patterns.
      check(Float.floatToRawIntBits(WaterFlowField.targets(valid, age))
              == Float.floatToRawIntBits(-(valid | ((age & 15) << 20)) - 2f),
          "valid target packet changed");
      check(Float.floatToRawIntBits(WaterFlowField.sources(valid, age))
              == Float.floatToRawIntBits(valid | ((age >>> 4) << 20)),
          "valid source packet changed");
      check(WaterFlowField.heightClock(-50, age) == ((-50 & 255) | (age << 8)),
          "valid height clock changed");
    }
    for (int age : new int[] {Integer.MIN_VALUE, -1, 0, 100, 101, 127, 128, 255, 256,
        Integer.MAX_VALUE}) {
      packet(0xffffffff, age);
      check((WaterFlowField.heightClock(-50, age) >>> 8) == Math.clamp(age, 0, 100),
          "malformed height clock age not bounded");
    }
    System.out.println("WATER_FLOW_PACKET_ADMISSION_PASS checks=" + checks
        + " exhaustiveWords=1048576 randomizedPairs=100000");
  }
}
