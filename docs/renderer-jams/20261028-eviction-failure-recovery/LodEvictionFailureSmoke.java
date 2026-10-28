package com.shnewbs.deepprimal.lod.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Source-only fault matrix for best-effort eviction; no Minecraft/GPU acceptance. */
public final class LodEvictionFailureSmoke {
  private static long assertions;

  private static void ok(boolean value, String message) {
    assertions++;
    if (!value) throw new AssertionError(message + " at assertion " + assertions);
  }

  private static final class Page {
    final long bytes;
    final boolean persisted;
    final RuntimeException closeError;
    final boolean errorKind;
    int closes;

    Page(long bytes, boolean persisted, boolean shouldFail, boolean errorKind) {
      this.bytes = bytes;
      this.persisted = persisted;
      this.errorKind = errorKind;
      this.closeError = shouldFail ? new IllegalStateException("close failed") : null;
    }

    void close() {
      closes++;
      if (closeError != null) {
        if (errorKind) throw new AssertionError(closeError.getMessage());
        throw closeError;
      }
    }
  }

  private static final class Session {
    final LinkedHashMap<Long, Page> cache = new LinkedHashMap<>();
    final LinkedHashSet<Long> disk = new LinkedHashSet<>();
    final Set<Long> dirty = new HashSet<>();
    final HashMap<Long, Long> latest = new HashMap<>();
    final List<Long> forgotten = new ArrayList<>();
    long resident;

    // Pure-Java model of VoxelLodRenderer.evictToFit's owned-cache close/recovery
    // branch. Exact source integration is checked separately against the Git blob.
    void evict(List<Long> victims, int stopAt) {
      LodGpuResetCleanup failures = new LodGpuResetCleanup();
      for (long key : victims) {
        if (cache.size() <= stopAt) break;
        Page page = cache.remove(key);
        resident -= page.bytes;
        failures.release(page::close);
        if (page.persisted && !dirty.contains(key) && disk.size() < 8192)
          disk.add(key);
        latest.remove(key);
        forgotten.add(key);
      }
      failures.throwIfFailed();
    }
  }

  private static void testScenario(int seed, boolean firstError) {
    Random rng = new Random(0xBADDFA11L + seed);
    Session s = new Session();
    int pages = 1 + rng.nextInt(48);
    int stopAt = rng.nextInt(pages + 1);
    List<Long> victims = new ArrayList<>();
    Set<Long> expectedRecover = new HashSet<>();
    int failCount = 0, firstFail = -1;
    for (int i = 0; i < pages; i++) {
      boolean fail = rng.nextBoolean() && (i % 3 == 0);
      boolean persisted = rng.nextBoolean();
      long key = i + 1L;
      Page page = new Page(i + 16L, persisted, fail, firstError);
      s.cache.put(key, page);
      s.latest.put(key, 3L);
      s.resident += page.bytes;
      victims.add(key);
      if (i < pages - stopAt) {
        if (persisted) expectedRecover.add(key);
        if (fail) {
          failCount++;
          if (firstFail < 0) firstFail = i;
        }
      }
    }
    // Keep references to all originals for exact close-count checks.
    ArrayList<Page> original = new ArrayList<>(s.cache.values());
    Throwable thrown = null;
    try { s.evict(victims, stopAt); }
    catch (RuntimeException | Error e) { thrown = e; }
    ok(s.cache.size() == stopAt, "eviction target must remain bounded");
    long expectedBytes = 0;
    for (Page page : s.cache.values()) expectedBytes += page.bytes;
    ok(s.resident == expectedBytes, "resident ledger must match retained cache");
    ok(s.disk.equals(expectedRecover), "disk recovery must survive close failure");
    ok(s.forgotten.size() == pages - stopAt, "all evicted metadata must be forgotten");
    for (int i = 0; i < pages; i++)
      ok(original.get(i).closes == (i < pages - stopAt ? 1 : 0), "close every evicted page exactly once");
    ok((thrown != null) == (failCount > 0), "failure propagation must match failed closes");
    if (thrown != null) {
      ok(thrown.getSuppressed().length == failCount - 1, "retain all later close failures");
      ok(firstFail >= 0 && thrown.getMessage().equals("close failed"), "preserve first failure");
      ok(firstError == (thrown instanceof Error), "preserve first Throwable type");
    }
  }

  private static void verifySameErrorDoesNotSuppressItself() {
    LodGpuResetCleanup failures = new LodGpuResetCleanup();
    RuntimeException same = new IllegalStateException("same");
    failures.release(() -> { throw same; });
    failures.release(() -> { throw same; });
    try {
      failures.throwIfFailed();
      throw new AssertionError("expected failure");
    } catch (IllegalStateException result) {
      ok(result == same && result.getSuppressed().length == 0,
          "do not suppress identical Throwable instance onto itself");
    }
  }

  public static void main(String[] args) {
    for (int i = 0; i < 3000; i++) {
      testScenario(i, false);
      testScenario(i, true);
    }
    verifySameErrorDoesNotSuppressItself();
    System.out.println("LOD_EVICTION_FAILURE_SMOKE_PASS scenarios=6000 assertions=" + assertions);
  }

  private LodEvictionFailureSmoke() {}
}
