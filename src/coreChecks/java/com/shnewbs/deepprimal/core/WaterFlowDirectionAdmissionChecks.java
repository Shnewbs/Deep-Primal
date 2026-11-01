package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Prevent invalid captured direction IDs from poisoning shared-corner shader history. */
public final class WaterFlowDirectionAdmissionChecks {
  private static long checks;
  private static void check(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message + " check=" + checks);
  }

  private static final class Reference {
    int from, target, pending;
    long started, revision = Long.MIN_VALUE;
    Reference(int initial, long now) { from = target = pending = initial; started = now; }
    void update(int next, long now, long version) {
      if (version < revision) return;
      revision = version;
      pending = next;
      if ((from == target || now - started >= 100) && next != target) {
        from = target;
        target = next;
        started = now;
      }
    }
    int age(long now) { return (int)Math.clamp(now - started, 0, 100); }
    boolean active(long now) { return from != target && age(now) < 100; }
    boolean queued(long now) { return active(now) && pending != target; }
  }

  public static void main(String[] args) {
    for (int invalid : new int[] {Integer.MIN_VALUE, -100, -1, 17, 31, 32, 255, 256, Integer.MAX_VALUE}) {
      var t = new WaterFlowTransition(invalid, 100);
      check(t.source() == 16 && t.target() == 16, "invalid initial must fall back to still water: " + invalid);
      t.target(4, 120, 10);
      check(t.target() == 4 && t.source() == 16, "valid update after invalid initial");
      t.target(invalid, 140, Long.MAX_VALUE);
      check(t.target() == 4 && t.source() == 16 && !t.queued(140), "invalid update changed blend: " + invalid);
      t.target(8, 160, 11);
      check(t.target() == 4 && t.queued(160), "invalid update advanced revision or erased queued endpoint");
      t.target(8, 240, 12);
      check(t.source() == 4 && t.target() == 8, "queued valid endpoint did not advance");
    }

    // Every valid direction, including the still-water material, retains old semantics.
    Random random = new Random(0x5a3a91L);
    for (int trial = 0; trial < 1000; trial++) {
      int initial = random.nextInt(17);
      long now = 1000;
      var t = new WaterFlowTransition(initial, now);
      var ref = new Reference(initial, now);
      long version = 0;
      for (int i = 0; i < 200; i++) {
        now += random.nextInt(150);
        int next = random.nextInt(17);
        version += random.nextInt(3) - 1;
        t.target(next, now, version);
        ref.update(next, now, version);
        check(t.source() == ref.from, "valid source parity");
        check(t.target() == ref.target, "valid target parity");
        check(t.age(now) == ref.age(now), "valid age parity");
        check(t.active(now) == ref.active(now), "valid active parity");
        check(t.queued(now) == ref.queued(now), "valid queued parity");
      }
    }
    System.out.println("WATER_FLOW_DIRECTION_ADMISSION_PASS checks=" + checks + " randomizedValidUpdates=200000 invalidCases=9");
  }
}
