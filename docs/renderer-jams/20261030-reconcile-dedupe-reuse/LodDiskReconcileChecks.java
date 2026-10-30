package com.shnewbs.deepprimal.lod.client;

import java.util.LinkedHashSet;
import java.util.List;

final class LodDiskReconcileChecks {
  public static void main(String[] args) {
    LinkedHashSet<Long> existing = new LinkedHashSet<>();
    existing.add(pack(0, 0));
    existing.add(pack(2, 0));
    existing.add(pack(2, 0));
    List<Long> incoming = List.of(pack(2, 0), pack(1, 0), pack(-1, 0));
    List<Long> result = LodDiskNearestScan.reconcile(existing, incoming, 0, 0, 8);
    require(result.equals(List.of(pack(0, 0), pack(-1, 0), pack(1, 0), pack(2, 0))),
        "stable nearest-first dedupe");
    require(LodDiskNearestScan.reconcile(new LinkedHashSet<>(), List.of(), 0, 0, 8).isEmpty(),
        "empty fast path");
    System.out.println("PASS reconcile dedupe reuse");
  }

  private static long pack(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private static void require(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }
}
