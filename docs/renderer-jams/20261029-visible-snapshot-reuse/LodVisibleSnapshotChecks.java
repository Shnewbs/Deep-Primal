package com.shnewbs.deepprimal.lod.client;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Source-excluded pure Java regression; no Minecraft or native GPU assertions. */
public final class LodVisibleSnapshotChecks {
  private static long assertions;

  private static void check(boolean condition, String message) {
    assertions++;
    if (!condition) throw new AssertionError(message + " at assertion " + assertions);
  }

  private static void reject(Runnable action, Class<? extends Throwable> type) {
    try {
      action.run();
    } catch (Throwable failure) {
      check(type.isInstance(failure), "wrong exception type: " + failure);
      return;
    }
    check(false, "expected " + type.getSimpleName());
  }

  private static long measureBytes(List<Object> pool, boolean reuse, int frames, int count) {
    var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    if (!bean.isThreadAllocatedMemorySupported()) return -1;
    bean.setThreadAllocatedMemoryEnabled(true);
    long thread = Thread.currentThread().threadId(); // Java 21+
    long before = bean.getThreadAllocatedBytes(thread);
    if (reuse) {
      var scratch = new LodVisibleSnapshot<Object>();
      for (int f = 0; f < frames; f++) {
        var list = scratch.begin();
        try {
          for (int j = 0; j < count; j++) list.add(pool.get(j));
          List<Object> published = scratch.snapshot();
          if (published.size() != count) throw new AssertionError("snapshot length");
        } finally {
          scratch.finish();
        }
      }
    } else {
      for (int f = 0; f < frames; f++) {
        var list = new ArrayList<Object>();
        for (int j = 0; j < count; j++) list.add(pool.get(j));
        List<Object> published = List.copyOf(list);
        if (published.size() != count) throw new AssertionError("snapshot length");
      }
    }
    return bean.getThreadAllocatedBytes(thread) - before;
  }

  public static void main(String[] args) {
    var collector = new LodVisibleSnapshot<Object>();
    var pool = new ArrayList<Object>();
    for (int i = 0; i < 512; i++) pool.add(new Object());
    Random rng = new Random(0xD33F);
    long observations = 0;
    for (int frame = 0; frame < 10000; frame++) {
      int count = rng.nextInt(513);
      var direct = new ArrayList<Object>();
      var scratch = collector.begin();
      reject(collector::begin, IllegalStateException.class);
      try {
        for (int i = 0; i < count; i++) {
          Object part = pool.get(rng.nextInt(pool.size()));
          direct.add(part);
          scratch.add(part);
        }
        List<Object> published = collector.snapshot();
        List<Object> expected = List.copyOf(direct);
        check(published.equals(expected), "ordered/multi-use mesh parity");
        check(published.size() == count, "same visible count");
        reject(() -> published.add(new Object()), UnsupportedOperationException.class);
        observations += count;
        if (count > 0) {
          Object first = published.get(0);
          scratch.set(0, new Object());
          check(published.get(0) == first, "snapshot must not alias scratch");
        }
      } finally {
        collector.finish();
      }
      check(collector.retainedReferences() == 0, "no meshes retained after render-state publication");
      reject(collector::snapshot, IllegalStateException.class);
    }
    var scratch = collector.begin();
    scratch.add(new Object());
    scratch.add(null); // List.copyOf must fail closed, with finally releasing old GPU references.
    try {
      reject(collector::snapshot, NullPointerException.class);
    } finally {
      collector.finish();
    }
    check(collector.retainedReferences() == 0, "failed snapshot clears mesh references");
    scratch = collector.begin();
    scratch.add(new Object());
    collector.reset();
    check(collector.retainedReferences() == 0, "world reset discards references");
    reject(collector::snapshot, IllegalStateException.class);
    collector.begin();
    collector.finish();

    // Matched steady, identical-source-element allocation only; not a frame-time test.
    measureBytes(pool, false, 1000, 256);
    measureBytes(pool, true, 1000, 256);
    long baseline = measureBytes(pool, false, 10000, 256);
    long candidate = measureBytes(pool, true, 10000, 256);
    System.out.println("VISIBLE_SNAPSHOT_PASS frames=10000 meshObservations=" + observations
        + " assertions=" + assertions + " baselineBytes=" + baseline
        + " candidateBytes=" + candidate + " deltaBytes=" + (baseline - candidate));
  }
}
