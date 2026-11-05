package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;
import com.shnewbs.deepprimal.core.RollingTiming;

/** Standalone deterministic source-level checks; not a Minecraft or GPU acceptance test. */
public final class LodSubmissionProbeChecks {
  private static long assertions;
  private static void check(boolean ok, String name) {
    assertions++;
    if (!ok) throw new AssertionError("assertion " + assertions + ": " + name);
  }

  private static void expectInvalid(Runnable call) {
    boolean rejected = false;
    try { call.run(); } catch (IllegalArgumentException expected) { rejected = true; }
    check(rejected, "invalid observation rejected");
  }

  private static void timing(RollingTiming.Snapshot got, ArrayDeque<Long> model) {
    long[] expected = model.stream().mapToLong(Long::longValue).toArray();
    Arrays.sort(expected);
    int n = expected.length;
    check(got.samples() == n, "sample size");
    check(got.p50Nanos() == (n == 0 ? 0 : expected[(n * 50 + 99) / 100 - 1]), "p50");
    check(got.p95Nanos() == (n == 0 ? 0 : expected[(n * 95 + 99) / 100 - 1]), "p95");
    check(got.p99Nanos() == (n == 0 ? 0 : expected[(n * 99 + 99) / 100 - 1]), "p99");
    check(got.maxNanos() == (n == 0 ? 0 : expected[n - 1]), "max");
  }

  private static void add(ArrayDeque<Long> d, long n, int cap) {
    if (d.size() == cap) d.removeFirst();
    d.addLast(n);
  }

  public static void main(String[] args) {
    expectInvalid(() -> new LodSubmissionProbe(0));
    expectInvalid(() -> new LodSubmissionProbe(4097));
    LodSubmissionProbe probe = new LodSubmissionProbe(240);
    var initial = probe.snapshot();
    check(initial.completedPasses() == 0 && initial.failedPasses() == 0, "initial empty");
    expectInvalid(() -> probe.finished(-1, 2, 1, 0, 0, false));
    expectInvalid(() -> probe.finished(2, -1, 2, 0, 0, false));
    expectInvalid(() -> probe.finished(2, 1, 2, 0, 0, false));
    expectInvalid(() -> probe.finished(4, 1, 3, 0, 0, true));
    expectInvalid(() -> probe.finished(1, 1, 1, 0, 0, true));
    expectInvalid(() -> probe.finished(1, 2, 3, -1, 0, false));
    expectInvalid(() -> probe.finished(1, 2, 3, 1, 0, false));
    expectInvalid(() -> probe.finished(1, 2, 3, 1, 2, false));
    check(probe.snapshot().totalCpuNanos().samples() == 0, "invalid stats unchanged");

    ArrayDeque<Long> transform = new ArrayDeque<>(), draw = new ArrayDeque<>(), total = new ArrayDeque<>();
    Random random = new Random(0x6ad0a21L);
    long completeCount = 0, failCount = 0, records = 0, calls = 0;
    int scenarios = 20000;
    for (int i = 0; i < scenarios; i++) {
      long prep = random.nextInt(400000), api = random.nextInt(500000);
      long duration = prep + api, adjustment = random.nextInt(7);
      int returned = random.nextInt(4097), count = returned == 0 ? 0 : 1 + random.nextInt(returned);
      boolean completed = random.nextInt(11) != 0;
      probe.finished(prep, api, duration + adjustment, returned, count, completed);
      add(transform, prep, 240); add(draw, api, 240); add(total, duration + adjustment, 240);
      if (completed) completeCount++; else failCount++;
      records += returned; calls += count;
      if (i % 13 == 0 || i == scenarios - 1) {
        var s = probe.snapshot();
        check(s.completedPasses() == completeCount, "completed count");
        check(s.failedPasses() == failCount, "failed count");
        check(s.returnedRecords() == records, "returned records count");
        check(s.returnedApiCalls() == calls, "returned calls count");
        timing(s.transformCpuNanos(), transform);
        timing(s.drawApiCpuNanos(), draw);
        timing(s.totalCpuNanos(), total);
      }
    }
    probe.reset();
    var end = probe.snapshot();
    check(end.completedPasses() == 0 && end.failedPasses() == 0, "reset pass counts");
    check(end.returnedRecords() == 0 && end.returnedApiCalls() == 0, "reset submitted counts");
    check(end.totalCpuNanos().samples() == 0 && end.transformCpuNanos().samples() == 0
        && end.drawApiCpuNanos().samples() == 0, "reset samples");
    probe.finished(0, 0, 0, 0, 0, false);
    check(probe.snapshot().failedPasses() == 1, "zero-duration rejected API counted");
    System.out.println("LOD_SUBMIT_PROBE_PASS scenarios=" + scenarios + " assertions=" + assertions);
  }
}
