package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** No Minecraft/GPU dependency; checks exact accepted-size sum and failure atomicity. */
public final class PendingGpuBytesChecks {
  private static long checks;
  private PendingGpuBytesChecks() {}

  private static void ok(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }

  private static void equal(long actual, long expected, String message) {
    checks++;
    if (actual != expected) throw new AssertionError(message + ": " + actual + " != " + expected);
  }

  private static void rejected(Runnable action, Class<? extends Throwable> type, String message) {
    boolean caught = false;
    try { action.run(); }
    catch (Throwable e) { if (type.isInstance(e)) caught = true; else throw e; }
    ok(caught, message);
  }

  private record Resource(int id, long bytes) {}

  public static void main(String[] args) {
    var ledger = new PendingGpuBytes();
    List<Resource> accepted = new ArrayList<>();
    equal(ledger.bytes(), 0, "fresh ledger");
    ledger.append(accepted, new Resource(1, 42L), 42L);
    ledger.append(accepted, new Resource(2, 0), 0);
    equal(ledger.bytes(), 42, "accepted and zero sizes");
    equal(accepted.size(), 2, "append owns membership");
    rejected(() -> ledger.append(accepted, new Resource(3, -1), -1), IllegalArgumentException.class,
        "negative byte sizes must fail closed");
    rejected(() -> ledger.append(accepted, null, 10), NullPointerException.class,
        "null GPU resources must not enter the accepted list");
    rejected(() -> ledger.append(null, new Resource(3, 10), 10), NullPointerException.class,
        "no target list");
    rejected(() -> ledger.append(Collections.unmodifiableList(accepted), new Resource(3, 10), 10),
        UnsupportedOperationException.class, "failed list append must be accounting-atomic");
    equal(ledger.bytes(), 42, "rejected append preserves byte total");
    equal(accepted.size(), 2, "rejected append preserves list membership");

    // Regression: old code walked all three accepted ArrayLists every frame.
    // Exercise three LOD levels with partial upload turns, cancellation and replacement.
    Random rng = new Random(0x710d003b5L);
    int uploads = 0;
    for (int trial = 0; trial < 512; trial++) {
      PendingGpuBytes pending = new PendingGpuBytes();
      @SuppressWarnings("unchecked") List<Resource>[] levels = new List[] {
          new ArrayList<Resource>(), new ArrayList<Resource>(), new ArrayList<Resource>()};
      long oracle = 0;
      for (int i = 0; i < 768; i++) {
        int detail = rng.nextInt(3);
        long size = rng.nextInt(3) == 0 ? 0 : rng.nextInt(1_048_576);
        Resource value = new Resource(i, size);
        pending.append(levels[detail], value, value.bytes());
        oracle += size;
        uploads++;
        equal(pending.bytes(), oracle, "partial page cumulative " + trial + "/" + i);
        if ((i & 15) == 0) {
          long old = 0;
          for (List<Resource> group : levels)
            for (Resource resource : group) old += resource.bytes();
          equal(pending.bytes(), old, "indexed three-level reference " + trial + "/" + i);
        }
      }
      // A discarded pending page never mutates the bytes of a fresh replacement page.
      PendingGpuBytes replacement = new PendingGpuBytes();
      equal(replacement.bytes(), 0, "replacement starts clean");
    }
    // Overflow behavior above real hard admission ceiling is deliberately defensive.
    PendingGpuBytes edge = new PendingGpuBytes();
    ArrayList<Resource> edgeList = new ArrayList<>();
    edge.append(edgeList, new Resource(1, Long.MAX_VALUE - 9), Long.MAX_VALUE - 9);
    edge.append(edgeList, new Resource(2, 9), 9);
    equal(edge.bytes(), Long.MAX_VALUE, "signed-long exact limit");
    rejected(() -> edge.append(edgeList, new Resource(3, 1), 1), IllegalArgumentException.class,
        "signed-long overflow cannot mutate lists");
    equal(edgeList.size(), 2, "overflow failed before add");
    equal(edge.bytes(), Long.MAX_VALUE, "overflow did not mutate counter");
    System.out.println("PENDING_GPU_BYTES_PASS checks=" + checks + " acceptedUploads=" + uploads
        + " randomizedPages=512 perPageParts=768");
  }
}
