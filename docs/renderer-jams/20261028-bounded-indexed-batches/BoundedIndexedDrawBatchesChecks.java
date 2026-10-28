package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Production-excluded source smoke; no Minecraft or GPU dependency. */
public final class BoundedIndexedDrawBatchesChecks {
  private static long assertions;

  private static void check(boolean ok, String message) {
    assertions++;
    if (!ok) throw new AssertionError(message + " (assertion " + assertions + ")");
  }

  private record Draw(int sequence) {}

  private static void parity(int limit, int n) {
    var batches = new BoundedIndexedDrawBatches<Draw>(limit);
    List<Integer> visited = new ArrayList<>(n);
    var generated = new AtomicInteger();
    var issued = new AtomicInteger();
    batches.submit(n, i -> {
      check(i == generated.getAndIncrement(), "prepare sequence");
      return new Draw(i);
    }, batch -> {
      check(!batch.isEmpty() && batch.size() <= limit, "batch bounds");
      for (Draw draw : batch) visited.add(draw.sequence());
      issued.incrementAndGet();
    });
    check(generated.get() == n, "all prepared");
    check(visited.size() == n, "all submitted");
    for (int i = 0; i < n; i++) check(visited.get(i) == i, "ordered lossless draw sequence");
    check(issued.get() == (n + (long) limit - 1) / limit, "exact API batch count");
    check(batches.retainedReferences() == 0, "no retained draw references");
    check(batches.reservedSlots() == (n == 0 ? 0 : limit), "bounded scratch slots");
    batches.submit(1, i -> new Draw(17), draws -> check(draws.get(0).sequence() == 17, "reuse"));
    check(batches.retainedReferences() == 0, "reused batch scrubbed");
    batches.reset();
    check(batches.reservedSlots() == 0 && batches.retainedReferences() == 0, "world reset releases scratch");
  }

  private static void failures() {
    var batches = new BoundedIndexedDrawBatches<Draw>(4);
    Throwable prepareFault = new IllegalStateException("prepare");
    try {
      batches.submit(8, i -> {
        if (i == 5) throw (IllegalStateException) prepareFault;
        return new Draw(i);
      }, batch -> {});
      throw new AssertionError("expected prepare fault");
    } catch (IllegalStateException ex) {
      check(ex == prepareFault, "original prepare fault propagated");
    }
    check(batches.retainedReferences() == 0, "prepare fault scrubbed");
    Throwable issueFault = new IllegalArgumentException("submit");
    try {
      batches.submit(8, Draw::new, batch -> { throw (IllegalArgumentException) issueFault; });
      throw new AssertionError("expected submit fault");
    } catch (IllegalArgumentException ex) {
      check(ex == issueFault, "original submit fault propagated");
    }
    check(batches.retainedReferences() == 0, "GPU/API submission fault scrubbed");
    try {
      batches.submit(2, i -> null, batch -> {});
      throw new AssertionError("expected null draw fault");
    } catch (NullPointerException ex) {
      check("draw".equals(ex.getMessage()), "bad draw rejected");
    }
    check(batches.retainedReferences() == 0, "null result fault scrubbed");
    try {
      batches.submit(1, Draw::new, batch -> batches.submit(1, Draw::new, ignored -> {}));
      throw new AssertionError("expected nesting guard");
    } catch (IllegalStateException ex) {
      check("nested indexed submission".equals(ex.getMessage()), "nested list reuse rejected");
    }
    check(batches.retainedReferences() == 0, "nesting fault scrubbed");
    try {
      batches.submit(1, Draw::new, batch -> batches.reset());
      throw new AssertionError("expected reset guard");
    } catch (IllegalStateException ex) {
      check("reset during indexed submission".equals(ex.getMessage()), "reset guard");
    }
    check(batches.retainedReferences() == 0, "reset fault scrubbed");
    check(batches.reservedSlots() == 4, "scratch survived non-destructive fault");
    batches.submit(2, Draw::new, batch -> check(batch.size() == 2, "recovery"));
    check(batches.retainedReferences() == 0, "recovery scrubbed");
    try {
      batches.submit(-1, Draw::new, ignored -> {});
      throw new AssertionError("expected negative count rejection");
    } catch (IllegalArgumentException ex) { check(true, "negative count"); }
    for (Runnable bad : List.<Runnable>of(
        () -> batches.submit(1, null, ignored -> {}),
        () -> batches.submit(1, Draw::new, (Consumer<List<Draw>>) null))) {
      try { bad.run(); throw new AssertionError("expected null callback rejection"); }
      catch (NullPointerException ex) { check(true, "null callback"); }
    }
    check(batches.retainedReferences() == 0, "invalid input cannot retain draws");
    try { new BoundedIndexedDrawBatches<>(0); throw new AssertionError("invalid limit"); }
    catch (IllegalArgumentException ex) { check(true, "zero batch rejected"); }
  }

  public static void main(String[] args) {
    for (int limit : new int[]{1, 2, 3, 17, 127, 4096}) {
      for (int count : new int[]{0, 1, 2, 3, 17, 128, 4095, 4096, 4097, 8191, 8192, 8193})
        parity(limit, count);
    }
    var rng = new Random(0x5ad9be31L);
    for (int run = 0; run < 240; run++) {
      int limit = 1 + rng.nextInt(4096);
      int count = rng.nextInt(16000);
      parity(limit, count);
    }
    parity(4096, 131072); // high-load visibility: 32 API batches, not a 131k-entry list
    failures();
    System.out.println("BOUNDED_INDEXED_DRAW_BATCHES_PASS scenarios=" + (6*12+240+1)
        + " assertions=" + assertions);
  }
}
