package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Production-excluded Java smoke of the exact render-thread batching primitives. */
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
    int apiCalls = 0, prepared = 0;
    for (int index = 0; index < n;) {
      int take = Math.min(limit, n - index);
      var draws = batches.begin(take);
      try {
        for (int j = 0; j < take; j++) {
          draws.add(new Draw(index++));
          prepared++;
        }
        check(draws.size() == take && draws.size() <= limit, "batch bounds");
        for (Draw draw : draws) visited.add(draw.sequence());
        apiCalls++;
      } finally { batches.end(); }
      check(batches.retainedReferences() == 0, "no draw references after submit");
    }
    check(prepared == n && visited.size() == n, "all prepared and submitted");
    for (int i = 0; i < n; i++) check(visited.get(i) == i, "ordered lossless draw sequence");
    check(apiCalls == (n + (long) limit - 1) / limit, "exact API batch count");
    check(batches.reservedSlots() == (n == 0 ? 0 : limit), "bounded scratch slots");
    var reused = batches.begin(1);
    try { reused.add(new Draw(17)); check(reused.get(0).sequence() == 17, "reuse"); }
    finally { batches.end(); }
    check(batches.retainedReferences() == 0, "reused batch scrubbed");
    batches.reset();
    check(batches.reservedSlots() == 0 && batches.retainedReferences() == 0,
        "world reset releases scratch");
  }

  private static void failures() {
    var batches = new BoundedIndexedDrawBatches<Draw>(4);
    Throwable submitFault = new IllegalArgumentException("native-API");
    try {
      var first = batches.begin(4);
      try {
        first.add(new Draw(0));
        throw (IllegalArgumentException) submitFault;
      } finally { batches.end(); }
    } catch (IllegalArgumentException ex) {
      check(ex == submitFault, "original API failure propagated");
    }
    check(batches.retainedReferences() == 0, "API failure scrubbed");
    Throwable prepareFault = new IllegalStateException("packing");
    try {
      var first = batches.begin(4);
      try {
        first.add(new Draw(0));
        throw (IllegalStateException) prepareFault;
      } finally { batches.end(); }
    } catch (IllegalStateException ex) {
      check(ex == prepareFault, "original packing failure propagated");
    }
    check(batches.retainedReferences() == 0, "packing failure scrubbed");
    try { batches.begin(0); throw new AssertionError("expected zero-count rejection"); }
    catch (IllegalArgumentException ex) { check(true, "empty batch rejected"); }
    try { batches.begin(5); throw new AssertionError("expected limit rejection"); }
    catch (IllegalArgumentException ex) { check(true, "oversized batch rejected"); }
    try { batches.begin(-1); throw new AssertionError("expected negative rejection"); }
    catch (IllegalArgumentException ex) { check(true, "negative batch rejected"); }
    check(batches.retainedReferences() == 0, "invalid input leaves scratch clean");
    var live = batches.begin(1);
    live.add(new Draw(3));
    try { batches.begin(1); throw new AssertionError("expected nested rejection"); }
    catch (IllegalStateException ex) { check(true, "nested batching rejected"); }
    try { batches.reset(); throw new AssertionError("expected reset rejection"); }
    catch (IllegalStateException ex) { check(true, "reset during submit rejected"); }
    batches.end();
    check(batches.retainedReferences() == 0, "nesting error cleanup");
    try { batches.end(); throw new AssertionError("expected double-end rejection"); }
    catch (IllegalStateException ex) { check(true, "double end rejected"); }
    batches.reset();
    check(batches.reservedSlots() == 0, "release after failed batch");
    try { new BoundedIndexedDrawBatches<>(0); throw new AssertionError("expected constructor guard"); }
    catch (IllegalArgumentException ex) { check(true, "zero limit rejected"); }
    var recovered = batches.begin(2);
    try { recovered.add(new Draw(4)); check(recovered.size() == 1, "recovery"); }
    finally { batches.end(); }
    check(batches.retainedReferences() == 0, "recovery cleanup");
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
    parity(4096, 131072);
    failures();
    System.out.println("BOUNDED_INDEXED_DRAW_BATCHES_PASS scenarios=" + (6*12+240+1)
        + " assertions=" + assertions);
  }
}
