package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Diagnostic Java21 component cost only; no game, driver, heap profile or FPS. */
public final class PendingGpuBytesMicrobench {
  private static volatile long sink;
  private record Part(long bytes) {}
  private static long old(List<Part>[] levels) {
    long result = 0;
    for (List<Part> list : levels)
      for (int i = 0; i < list.size(); i++) result += list.get(i).bytes();
    return result;
  }
  private static long percentile(long[] ns, double fraction) {
    Arrays.sort(ns);
    return ns[(int) Math.ceil(fraction * ns.length) - 1];
  }
  @SuppressWarnings("unchecked")
  private static void sample(int count) {
    List<Part>[] levels = new List[] {new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
    PendingGpuBytes ledger = new PendingGpuBytes();
    for (int i = 0; i < count; i++) {
      Part part = new Part(16384 + i);
      ledger.append(levels[i % 3], part, part.bytes());
    }
    if (old(levels) != ledger.bytes()) throw new AssertionError("different checksum");
    for (int i = 0; i < 30000; i++) {
      sink = old(levels);
      sink = ledger.bytes();
    }
    int samples = 8000;
    long[] original = new long[samples], updated = new long[samples];
    for (int i = 0; i < samples; i++) {
      long a = System.nanoTime(); sink = (i & 1) == 0 ? old(levels) : ledger.bytes();
      long b = System.nanoTime(); sink = (i & 1) == 0 ? ledger.bytes() : old(levels);
      long c = System.nanoTime();
      if ((i & 1) == 0) {original[i] = b - a; updated[i] = c - b;}
      else {updated[i] = b - a; original[i] = c - b;}
    }
    System.out.println("PENDING_GPU_BENCH parts=" + count + " oldP50ns=" + percentile(original.clone(), .5)
        + " oldP95ns=" + percentile(original.clone(), .95)
        + " oldP99ns=" + percentile(original.clone(), .99)
        + " newP50ns=" + percentile(updated.clone(), .5)
        + " newP95ns=" + percentile(updated.clone(), .95)
        + " newP99ns=" + percentile(updated.clone(), .99) + " checksum=" + sink);
  }
  public static void main(String[] args) {sample(512); sample(4096);}
}
