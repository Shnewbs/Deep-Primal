package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Deterministic standalone tests of production probe and original RollingTiming API. */
public final class LodGpuUploadProbeChecks {
  private static long assertions;

  private static void check(boolean truth, String what) {
    assertions++;
    if (!truth) throw new AssertionError(what);
  }

  private static void equal(long actual, long expected, String what) {
    check(actual == expected, what + " expected=" + expected + " actual=" + actual);
  }

  private static long percentile(List<Long> values, int pct) {
    if (values.isEmpty()) return 0;
    var list = new ArrayList<>(values);
    Collections.sort(list);
    return list.get((list.size() * pct + 99) / 100 - 1);
  }

  private static void assertPercentiles(com.shnewbs.deepprimal.core.RollingTiming.Snapshot s,
      List<Long> values, String what) {
    equal(s.samples(), values.size(), what + " samples");
    equal(s.p50Nanos(), percentile(values,50), what + " p50");
    equal(s.p95Nanos(), percentile(values,95), what + " p95");
    equal(s.p99Nanos(), percentile(values,99), what + " p99");
    equal(s.maxNanos(), percentile(values,100), what + " max");
  }

  private static void shouldReject(Runnable run, String reason) {
    boolean rejected = false;
    try { run.run(); } catch (IllegalArgumentException expected) { rejected = true; }
    check(rejected, reason);
  }

  public static void main(String[] args) {
    var empty = new LodGpuUploadProbe(1);
    var s = empty.snapshot();
    equal(s.acceptedParts(),0,"new accepted");
    equal(s.failedParts(),0,"new failed");
    assertPercentiles(s.cpuNanos(),List.of(),"new time");
    assertPercentiles(s.acceptedBytes(),List.of(),"new bytes");
    shouldReject(() -> new LodGpuUploadProbe(0),"zero capacity");
    shouldReject(() -> new LodGpuUploadProbe(4097),"high capacity");
    shouldReject(() -> empty.accepted(-1,0,0),"negative elapsed");
    shouldReject(() -> empty.accepted(0,-1,0),"negative accepted bytes");
    shouldReject(() -> empty.accepted(0,0,-1),"negative deadline");
    shouldReject(() -> empty.failed(-1),"negative failure");
    empty.accepted(0,0,0);
    empty.accepted(1,Long.MAX_VALUE,0);
    empty.accepted(2,1,0);
    s = empty.snapshot();
    equal(s.acceptedParts(),3,"success count");
    equal(s.overTimeBudgetParts(),2,"strict greater than time budget");
    equal(s.peakPartBytes(),Long.MAX_VALUE,"largest accepted part");
    equal(s.acceptedByteTotal(),Long.MAX_VALUE,"saturated total");
    equal(s.cpuNanos().samples(),1,"window size one");
    equal(s.cpuNanos().p99Nanos(),2,"latest time only");
    equal(s.acceptedBytes().p99Nanos(),1,"latest bytes only");
    empty.reset();
    equal(empty.snapshot().acceptedByteTotal(),0,"reset saturated total");

    final int pages = 300, perPage = 257, capacity = 240;
    var random = new Random(0x5EEDC0DEL);
    long accepted = 0, failed = 0, over = 0, total = 0, peak = 0;
    var probe = new LodGpuUploadProbe(capacity);
    var times = new ArrayList<Long>();
    var sizes = new ArrayList<Long>();
    for (int page = 0; page < pages; page++) {
      for (int part = 0; part < perPage; part++) {
        long elapsed = random.nextInt(100_000);
        if (random.nextInt(17) == 0) {
          probe.failed(elapsed);
          failed++;
        } else {
          long bytes = random.nextInt(90_000);
          long deadline = random.nextInt(100_000);
          probe.accepted(elapsed,bytes,deadline);
          accepted++;
          if (elapsed > deadline) over++;
          total += bytes;
          peak = Math.max(peak,bytes);
          times.add(elapsed);
          sizes.add(bytes);
          if (times.size() > capacity) { times.remove(0); sizes.remove(0); }
        }
        if ((part & 31) == 0) {
          s = probe.snapshot();
          equal(s.acceptedParts(),accepted,"accepted count");
          equal(s.failedParts(),failed,"failure count");
          equal(s.overTimeBudgetParts(),over,"time budget violations");
          equal(s.acceptedByteTotal(),total,"byte total");
          equal(s.peakPartBytes(),peak,"largest allocation");
          assertPercentiles(s.cpuNanos(),times,"time rolling");
          assertPercentiles(s.acceptedBytes(),sizes,"bytes rolling");
        }
      }
    }
    s = probe.snapshot();
    assertPercentiles(s.cpuNanos(),times,"final time");
    assertPercentiles(s.acceptedBytes(),sizes,"final bytes");
    equal(s.acceptedParts()+s.failedParts(),(long)pages*perPage,"attempt conservation");
    probe.reset();
    s = probe.snapshot();
    equal(s.acceptedParts(),0,"reset accepted");
    equal(s.failedParts(),0,"reset failed");
    equal(s.overTimeBudgetParts(),0,"reset overshoot");
    equal(s.peakPartBytes(),0,"reset peak bytes");
    assertPercentiles(s.cpuNanos(),List.of(),"reset times");
    assertPercentiles(s.acceptedBytes(),List.of(),"reset sizes");
    System.out.println("LOD_GPU_UPLOAD_PROBE_PASS assertions=" + assertions
        + " attempts=" + ((long)pages*perPage) + " accepted=" + accepted + " failed=" + failed
        + " window=" + capacity);
  }
}
