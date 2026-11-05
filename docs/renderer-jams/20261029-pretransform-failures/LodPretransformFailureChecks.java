package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.core.RollingTiming;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;

/** Isolated pure-Java source tests, not native GPU or Minecraft validation. */
public final class LodPretransformFailureChecks {
  private static long assertions;
  private static void check(boolean b, String message) {
    assertions++;
    if (!b) throw new AssertionError("assertion " + assertions + ": " + message);
  }
  private static void invalid(Runnable r) {
    boolean rejected = false;
    try { r.run(); } catch (IllegalArgumentException expected) { rejected = true; }
    check(rejected, "negative preparation rejected");
  }
  private static void add(ArrayDeque<Long> d, long x) {
    if (d.size() == 240) d.removeFirst();
    d.addLast(x);
  }
  private static void match(RollingTiming.Snapshot s, ArrayDeque<Long> model) {
    long[] array = model.stream().mapToLong(Long::longValue).toArray();
    Arrays.sort(array);
    int n = array.length;
    check(s.samples() == n, "window sample count");
    check(s.p50Nanos() == (n==0?0:array[(n*50+99)/100-1]), "window p50");
    check(s.p95Nanos() == (n==0?0:array[(n*95+99)/100-1]), "window p95");
    check(s.p99Nanos() == (n==0?0:array[(n*99+99)/100-1]), "window p99");
    check(s.maxNanos() == (n==0?0:array[n-1]), "window maximum");
  }
  public static void main(String[] args) {
    LodSubmissionProbe p = new LodSubmissionProbe(240);
    invalid(() -> p.failedBeforeDraw(-1));
    invalid(() -> p.failedBeforeDraw(Long.MIN_VALUE));
    check(p.snapshot().failedPasses() == 0, "invalid prep unchanged");
    p.failedBeforeDraw(0);
    check(p.snapshot().failedBeforeDrawPasses()==1, "zero pre-draw failure");
    p.reset();
    Random rng = new Random(0x7b5ad100L);
    var prep=new ArrayDeque<Long>(); var draw=new ArrayDeque<Long>(); var total=new ArrayDeque<Long>();
    long success=0, failure=0, early=0, records=0, calls=0;
    int iterations=30000;
    for(int i=0;i<iterations;i++) {
      int mode=rng.nextInt(8);
      long t=rng.nextInt(2000000);
      if(mode==0 || mode==1 || mode==2) {
        p.failedBeforeDraw(t);
        add(prep,t); add(draw,0); add(total,t);
        early++;failure++;
      } else {
        long d=rng.nextInt(1700000), slack=rng.nextInt(73);
        int returned=rng.nextInt(4097), submitted=returned==0?0:1+rng.nextInt(returned);
        boolean completed=mode!=3;
        p.finished(t,d,t+d+slack,returned,submitted,completed);
        add(prep,t); add(draw,d);add(total,t+d+slack);
        records+=returned; calls+=submitted;
        if(completed) success++; else failure++;
      }
      if(i%17==0 || i==iterations-1) {
        var snap=p.snapshot();
        check(snap.completedPasses()==success,"successes");
        check(snap.failedPasses()==failure,"all failures");
        check(snap.failedBeforeDrawPasses()==early,"subset pre-draw failures");
        check(snap.returnedRecords()==records,"successful API draw records only");
        check(snap.returnedApiCalls()==calls,"successful API calls only");
        check(snap.failedBeforeDrawPasses()<=snap.failedPasses(),"failure subset invariant");
        match(snap.transformCpuNanos(),prep);match(snap.drawApiCpuNanos(),draw);
        match(snap.totalCpuNanos(),total);
      }
    }
    p.reset(); var after=p.snapshot();
    check(after.failedBeforeDrawPasses()==0 && after.failedPasses()==0 && after.completedPasses()==0,"world reset counts");
    check(after.totalCpuNanos().samples()==0 && after.transformCpuNanos().samples()==0 && after.drawApiCpuNanos().samples()==0,"world reset samples");
    System.out.println("LOD_PREDRAW_FAIL_PASS iterations="+iterations+" assertions="+assertions);
  }
}
