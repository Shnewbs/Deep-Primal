package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.List;

/** Isolated lifecycle fault-injection contract. Not a Minecraft/GPU execution. */
public final class LodResetControlChecks {
  private static long assertions;

  private static void check(boolean passed, String message) {
    assertions++;
    if (!passed) throw new AssertionError(message + " assertion=" + assertions);
  }

  private static final class Rig {
    final int mask;
    final boolean firstError;
    final List<String> attempted = new ArrayList<>();
    final Throwable[] failures = new Throwable[7];
    boolean pending = true, future = true, worker = true, level = true, cache = true;

    Rig(int mask, boolean firstError) {
      this.mask = mask;
      this.firstError = firstError;
    }

    void operation(int index, String name) {
      attempted.add(name);
      if ((mask & (1 << index)) != 0) {
        Throwable fault = (firstError && index == Integer.numberOfTrailingZeros(mask))
            ? new AssertionError("injected-" + name)
            : new IllegalStateException("injected-" + name);
        failures[index] = fault;
        if (fault instanceof Error error) throw error;
        throw (RuntimeException) fault;
      }
    }

    void reset() {
      LodGpuResetCleanup collector = new LodGpuResetCleanup();
      pending = false;
      collector.release(() -> operation(0, "pendingGPU"));
      future = false;
      collector.release(() -> operation(1, "futureCancel"));
      collector.release(() -> operation(2, "diskReset"));
      worker = false;
      collector.release(() -> operation(3, "workerShutdown"));
      collector.release(() -> operation(4, "cachedGPU1"));
      collector.release(() -> operation(5, "cachedGPU2"));
      collector.release(() -> operation(6, "cachedGPU3"));
      cache = false;
      level = false;
      collector.throwIfFailed();
    }
  }

  public static void main(String[] args) {
    List<String> expected = List.of("pendingGPU", "futureCancel", "diskReset",
        "workerShutdown", "cachedGPU1", "cachedGPU2", "cachedGPU3");
    for (boolean error : new boolean[] {false, true}) {
      for (int mask = 0; mask < 128; mask++) {
        Rig rig = new Rig(mask, error);
        Throwable reported = null;
        try { rig.reset(); } catch (RuntimeException | Error failure) { reported = failure; }
        check(rig.attempted.equals(expected), "every release must be attempted mask=" + mask);
        check(!rig.pending && !rig.future && !rig.worker && !rig.level && !rig.cache,
            "ownership cleared mask=" + mask);
        if (mask == 0) {
          check(reported == null, "clean completion");
        } else {
          int first = Integer.numberOfTrailingZeros(mask);
          check(reported == rig.failures[first], "first fault identity");
          Throwable[] suppressed = reported.getSuppressed();
          check(suppressed.length == Integer.bitCount(mask) - 1, "suppression count");
          int seen = 0;
          for (int i = first + 1; i < 7; i++) {
            if ((mask & (1 << i)) == 0) continue;
            check(suppressed[seen++] == rig.failures[i], "suppression order");
          }
        }
      }
    }
    System.out.println("LOD_RESET_CONTROL_PASS assertions=" + assertions + " masks=128 throwableKinds=2");
  }
}
