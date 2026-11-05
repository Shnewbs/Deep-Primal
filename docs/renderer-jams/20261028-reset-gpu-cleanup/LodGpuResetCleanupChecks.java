package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Dependency-free renderer cleanup checks; does not exercise a native GPU. */
public final class LodGpuResetCleanupChecks {
  private static long assertions;
  private static void check(boolean yes, String message) {
    assertions++;
    if (!yes) throw new AssertionError(message + " (#" + assertions + ")");
  }

  public static void main(String[] args) {
    LodGpuResetCleanup clean = new LodGpuResetCleanup();
    List<Integer> closed = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      final int n = i;
      clean.release(() -> closed.add(n));
    }
    clean.throwIfFailed();
    check(closed.equals(List.of(0, 1, 2, 3, 4, 5, 6)), "normal closes in order");

    RuntimeException first = new IllegalStateException("first pending GPU close failed");
    Error last = new AssertionError("last cached page close failed");
    clean = new LodGpuResetCleanup();
    closed.clear();
    clean.release(() -> { closed.add(0); throw first; });
    clean.release(() -> closed.add(1));
    clean.release(() -> { closed.add(2); throw last; });
    clean.release(() -> closed.add(3));
    check(closed.equals(List.of(0, 1, 2, 3)), "all releases attempted after failures");
    try { clean.throwIfFailed(); throw new AssertionError("expected first failure"); }
    catch (RuntimeException received) {
      check(received == first, "original pending failure identity preserved");
      check(received.getSuppressed().length == 1 && received.getSuppressed()[0] == last,
          "later GPU failure suppressed");
    }

    Error original = new AssertionError("device teardown");
    clean = new LodGpuResetCleanup();
    clean.release(() -> { throw original; });
    clean.release(() -> { throw original; });
    try { clean.throwIfFailed(); throw new AssertionError("expected Error"); }
    catch (Error received) {
      check(received == original, "Error preserved without translation");
      check(received.getSuppressed().length == 0, "same throwable cannot self suppress");
    }

    Random random = new Random(0x4c4f445245534554L);
    for (int sequence = 0; sequence < 2000; sequence++) {
      final int count = 1 + random.nextInt(100);
      final int[] attempted = {0};
      final List<Throwable> failures = new ArrayList<>();
      LodGpuResetCleanup accumulator = new LodGpuResetCleanup();
      for (int i = 0; i < count; i++) {
        int failureType = random.nextInt(5);
        final Throwable thrown = switch (failureType) {
          case 0 -> new IllegalStateException("page-" + i);
          case 1 -> new AssertionError("buffer-" + i);
          default -> null;
        };
        if (thrown != null) failures.add(thrown);
        accumulator.release(() -> {
          attempted[0]++;
          if (thrown instanceof Error error) throw error;
          if (thrown instanceof RuntimeException error) throw error;
        });
      }
      check(attempted[0] == count, "no skipped close in randomized sequence");
      if (failures.isEmpty()) {
        accumulator.throwIfFailed();
        check(true, "no false cleanup failure");
      } else {
        Throwable seen = null;
        try { accumulator.throwIfFailed(); } catch (RuntimeException | Error failure) { seen = failure; }
        check(seen == failures.getFirst(), "first failure remains root");
        check(seen.getSuppressed().length == failures.size() - 1, "every later failure recorded");
        for (int i = 1; i < failures.size(); i++)
          check(seen.getSuppressed()[i - 1] == failures.get(i), "suppression order preserved");
      }
    }
    System.out.println("LOD_GPU_RESET_CLEANUP_PASS assertions=" + assertions);
  }
}
