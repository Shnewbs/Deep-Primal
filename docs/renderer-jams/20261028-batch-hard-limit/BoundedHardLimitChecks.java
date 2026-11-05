package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

/** Standalone exact-helper regression; production-excluded. */
public final class BoundedHardLimitChecks {
  private static long assertions;
  private static int cases;
  private static void require(boolean b, String what) {
    assertions++;
    if (!b) throw new AssertionError(what + " @ assertion " + assertions);
  }
  private static void reject(Runnable f, String what) {
    boolean failed = false;
    try { f.run(); } catch (IllegalStateException | IllegalArgumentException ex) { failed = true; }
    require(failed, what);
  }
  private static void parity(int cap, int total) {
    cases++;
    var scratch = new BoundedIndexedDrawBatches<Integer>(cap);
    int cursor = 0, submissions = 0;
    while (cursor < total) {
      int count = Math.min(cap, total - cursor);
      var list = scratch.begin(count);
      try {
        for (int i = 0; i < count; i++) list.add(cursor++);
        require(list.size() == count, "all expected records");
        reject(() -> list.add(-100), "hard cap rejects extra append");
        require(list.size() == count, "failed append atomic");
        for (int i = 0; i < count; i++) require(list.get(i) == cursor-count+i, "ordered output");
        submissions++;
      } finally { scratch.end(); }
      require(scratch.retainedReferences() == 0, "batch scrub");
      reject(() -> list.add(-99), "closed list cannot retain stale handles");
    }
    require(submissions == (total + (long)cap - 1)/cap, "number of API batches");
    require(scratch.reservedSlots() == (total == 0 ? 0 : cap), "bounded retained capacity");
    scratch.reset();
    require(scratch.reservedSlots() == 0, "world reset frees scratch");
  }
  private static void growthPaths() {
    cases++;
    var batch = new BoundedIndexedDrawBatches<Integer>(4);
    var l = batch.begin(3);
    l.add(1);
    l.add(3);
    l.add(1,2);
    require(l.equals(List.of(1,2,3)), "indexed insert preserves order");
    reject(() -> l.add(0,0), "indexed insert over cap");
    reject(() -> l.addAll(List.of(8)), "append collection over cap");
    reject(() -> l.addAll(1,List.of(8)), "insert collection over cap");
    reject(() -> l.subList(0,2).add(8), "sublist over cap");
    ListIterator<Integer> it = l.listIterator();
    it.next();
    reject(() -> it.add(8), "iterator insert over cap");
    reject(() -> l.ensureCapacity(5), "explicit large allocation over cap");
    require(l.equals(List.of(1,2,3)), "rejections preserve existing records");
    batch.end();
    require(batch.retainedReferences() == 0, "all references scrubbed");
    var shortBatch = batch.begin(2);
    shortBatch.addAll(List.of(10,11));
    reject(() -> shortBatch.addAll(List.of(12)), "per-batch short cap");
    require(shortBatch.equals(List.of(10,11)), "successful addAll exact limit");
    shortBatch.remove(0);
    shortBatch.addAll(0,List.of(13));
    require(shortBatch.equals(List.of(13,11)), "insert after removal within cap");
    batch.end();
    require(batch.retainedReferences() == 0, "reuse scrub");
    reject(() -> shortBatch.add(5), "post-end escaped handle cannot append");
    reject(() -> batch.end(), "double end");
    batch.reset();
  }
  private static void inputAndFailure() {
    cases++;
    var batch = new BoundedIndexedDrawBatches<Integer>(2);
    reject(() -> batch.begin(0), "invalid empty count");
    reject(() -> batch.begin(3), "invalid over cap");
    reject(() -> batch.begin(-1), "invalid negative count");
    var l = batch.begin(1);
    l.add(10);
    reject(() -> batch.begin(1), "nested batch");
    reject(batch::reset, "cannot release active references");
    try {
      throw new IllegalStateException("backend fault");
    } catch (IllegalStateException error) {
      require(error.getMessage().equals("backend fault"), "fault retained");
    } finally { batch.end(); }
    require(batch.retainedReferences() == 0, "exception cleanup");
    batch.reset();
  }
  private static void randomized() {
    var random = new Random(0x6a8d1ee217L);
    for (int run=0; run<8000; run++) {
      cases++;
      int cap = 1 + random.nextInt(64);
      int expected = 1 + random.nextInt(cap);
      var batch = new BoundedIndexedDrawBatches<Integer>(cap);
      ArrayList<Integer> original = new ArrayList<>();
      var actual = batch.begin(expected);
      try {
        for (int step=0; step<50; step++) {
          int how = random.nextInt(7);
          int amount = how >= 3 && how <= 4 ? random.nextInt(6) : 1;
          int index = original.isEmpty() ? 0 : random.nextInt(original.size()+1);
          int value = random.nextInt();
          boolean fits = (how == 5 || how == 6) || amount <= expected-original.size();
          if (how == 0) {
            if (fits) {actual.add(value); original.add(value);}
            else reject(() -> actual.add(value), "append cap");
          } else if (how == 1 || how == 2) {
            if (fits) {actual.add(index,value); original.add(index,value);}
            else reject(() -> actual.add(index,value), "position cap");
          } else if (how == 3 || how == 4) {
            var add = Collections.nCopies(amount,value);
            if (fits) {
              if (how == 3) {actual.addAll(add); original.addAll(add);}
              else {actual.addAll(index,add); original.addAll(index,add);}
            } else if (how == 3) reject(() -> actual.addAll(add), "appendAll cap");
            else reject(() -> actual.addAll(index,add), "insertAll cap");
          } else if (how == 5 && !original.isEmpty()) {
            actual.remove(index == original.size() ? 0 : index);
            original.remove(index == original.size() ? 0 : index);
          } else if (how == 6 && !original.isEmpty()) {
            actual.set(index == original.size() ? 0 : index, value);
            original.set(index == original.size() ? 0 : index, value);
          }
          require(actual.equals(original), "randomized exact output and size");
          require(actual.size() <= expected && actual.size() <= cap,"random budget");
        }
      } finally {batch.end();}
      require(batch.retainedReferences() == 0,"random scrub");
    }
  }
  public static void main(String[] args) {
    for (int cap : new int[]{1,2,3,17,127,4096})
      for (int total : new int[]{0,1,2,3,17,128,4095,4096,4097,8192,131072})
        parity(cap,total);
    growthPaths();inputAndFailure();randomized();
    System.out.println("BATCH_HARD_LIMIT_PASS cases=" + cases + " assertions="+assertions);
  }
}
