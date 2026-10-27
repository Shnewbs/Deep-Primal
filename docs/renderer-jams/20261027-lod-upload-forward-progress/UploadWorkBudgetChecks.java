package com.shnewbs.deepprimal.lod.client;

import java.util.Random;

/** Isolated unit oracle: GPU device, resident buffer lifetime and render threading untested. */
public final class UploadWorkBudgetChecks {
  private static long checks;
  private static void eq(boolean actual, boolean expected, String label) {
    checks++;
    if (actual != expected) throw new AssertionError(label + " actual=" + actual + " expected=" + expected);
  }
  private static void check(boolean condition, String label) {
    checks++;
    if (!condition) throw new AssertionError(label);
  }
  private static boolean reference(int uploaded, int max, long used, long part,
      long cap, boolean expired) {
    if (uploaded >= max) return false;
    if (uploaded == 0) return true;
    return !expired && used <= cap && part <= cap - used;
  }
  private static void bad(int uploaded, int max, long used, long part, long cap) {
    boolean rejected = false;
    try { UploadWorkBudget.mayStart(uploaded, max, used, part, cap, false); }
    catch (IllegalArgumentException expected) { rejected = true; }
    check(rejected, "invalid argument accepted");
  }
  private static int drain(long[] parts, long limit, int maxParts, boolean expired,
      boolean[] sawOversized) {
    int cursor = 0, frames = 0;
    while (cursor < parts.length) {
      int uploaded = 0;
      long used = 0;
      int before = cursor;
      while (cursor < parts.length && UploadWorkBudget.mayStart(uploaded, maxParts, used,
          parts[cursor], limit, expired)) {
        boolean over = UploadWorkBudget.oversizedFirst(uploaded, parts[cursor], limit);
        if (over) sawOversized[0] = true;
        if (uploaded > 0) check(used <= limit && parts[cursor] <= limit - used,
            "subsequent part exceeds budget");
        used += parts[cursor];
        uploaded++;
        cursor++;
      }
      check(cursor > before, "pending upload never advances");
      frames++;
      check(frames <= parts.length, "unbounded frame count");
    }
    return frames;
  }
  public static void main(String[] args) {
    long cap = 256L * 1024;
    boolean[] oversize = {false};
    int frames = drain(new long[]{128L*1024, cap+1, 8000, cap*2, 1}, cap, 3, false, oversize);
    check(frames == 5 && oversize[0], "oversized items did not finish singly");
    eq(UploadWorkBudget.mayStart(0, 1, 0, cap+1, cap, true), true,
        "first part must advance after missed deadline");
    eq(UploadWorkBudget.mayStart(1, 3, 10, 10, cap, true), false,
        "expired frame allowed additional upload");
    eq(UploadWorkBudget.mayStart(1, 1, 0, 0, cap, false), false, "per-frame count");
    eq(UploadWorkBudget.mayStart(1, 2, cap, 0, cap, false), true, "zero-size next");
    eq(UploadWorkBudget.mayStart(1, 2, cap, 1, cap, false), false, "exact boundary");
    eq(UploadWorkBudget.mayStart(1, 2, Long.MAX_VALUE - 2, 20, Long.MAX_VALUE - 1, false),
        false, "overflow-safe byte budget");
    bad(-1, 1, 0, 0, 1); bad(0, 0, 0, 0, 1); bad(0, 1, -1, 0, 1);
    bad(0, 1, 0, -1, 1); bad(0, 1, 0, 0, -1);
    Random random = new Random(0x5a7cb1);
    for (int i = 0; i < 150_000; i++) {
      int uploaded = random.nextInt(10), max = 1 + random.nextInt(12);
      long budget = random.nextInt(500_000), used = random.nextInt(600_000);
      long next = random.nextInt(750_000);
      boolean expired = random.nextBoolean();
      eq(UploadWorkBudget.mayStart(uploaded, max, used, next, budget, expired),
          reference(uploaded, max, used, next, budget, expired), "random admission " + i);
      eq(UploadWorkBudget.oversizedFirst(uploaded, next, budget),
          uploaded == 0 && next > budget, "oversize accounting " + i);
    }
    for (int i = 0; i < 10_000; i++) {
      long limit = 1 + random.nextInt(100_000);
      long[] parts = new long[1+random.nextInt(9)];
      for (int n = 0; n < parts.length; n++) parts[n] = random.nextInt(400_000);
      drain(parts, limit, 1 + random.nextInt(8), random.nextBoolean(), new boolean[1]);
    }
    System.out.println("LOD_UPLOAD_FORWARD_PROGRESS_PASS checks=" + checks
        + " randomizedAdmissions=150000 randomizedPendingQueues=10000"
        + " nativeUpload=false gpuTest=false buildJava=21");
  }
}
