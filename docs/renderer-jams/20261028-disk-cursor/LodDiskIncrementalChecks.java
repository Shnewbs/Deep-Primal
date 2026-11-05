package com.shnewbs.deepprimal.lod.client;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

/** Production-excluded LOD worker-cursor/immutable queue-consumer regression. */
public final class LodDiskIncrementalChecks {
  private static long checks;
  private LodDiskIncrementalChecks() {}

  private static void require(boolean ok, String description) {
    checks++;
    if (!ok) throw new AssertionError(description);
  }

  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private static int x(long key) { return (int) (key >> 32); }
  private static int z(long key) { return (int) key; }

  private static Comparator<Long> exact(int cameraX, int cameraZ) {
    return Comparator.<Long>comparingLong(k -> Math.max(
          Math.abs((long) x(k) - cameraX), Math.abs((long) z(k) - cameraZ)))
        .thenComparing(k -> {
          var dx = BigInteger.valueOf((long) x(k) - cameraX);
          var dz = BigInteger.valueOf((long) z(k) - cameraZ);
          return dx.multiply(dx).add(dz.multiply(dz));
        })
        .thenComparingInt(LodDiskIncrementalChecks::x)
        .thenComparingInt(LodDiskIncrementalChecks::z);
  }

  private static LodDiskNearestScan.Completion drain(LodDiskNearestScan.Cursor cursor)
      throws InterruptedException {
    long deadline = System.nanoTime() + 20_000_000_000L;
    long before = cursor.examined();
    long lastTurn = cursor.turns();
    while (cursor.completed() == null && System.nanoTime() < deadline) {
      cursor.advance();
      // Never permit more than one unobserved turn; each turn has a strict entry cap.
      long turnDeadline = System.nanoTime() + 2_000_000_000L;
      while (cursor.completed() == null && cursor.turns() == lastTurn
          && System.nanoTime() < turnDeadline) Thread.sleep(1);
      long current = cursor.examined();
      long delta = current - before;
      require(delta >= 0 && delta <= LodDiskNearestScan.ENTRIES_PER_TURN,
          "one worker grant must inspect at most 256 entries: " + delta);
      before = current;
      lastTurn = cursor.turns();
    }
    require(cursor.completed() != null, "cursor must finish after bounded grants");
    require(cursor.awaitStopped(3000), "cursor closed its stream and terminated");
    return cursor.completed();
  }

  private static void test(Path directory) throws Exception {
    ArrayList<Long> inputs = new ArrayList<>();
    for (int i = 0; i < 8204; i++) {
      int px = 8000 + i, pz = 20000 + (i % 317);
      Files.createFile(directory.resolve(px + "_" + pz + ".plod"));
      inputs.add(key(px, pz));
    }
    Files.createFile(directory.resolve("-1_-1.plod"));
    inputs.add(key(-1, -1));
    Files.createFile(directory.resolve("01_0.plod"));
    Files.createFile(directory.resolve("bad_file.plod"));
    Files.createFile(directory.resolve("2147483648_1.plod"));

    // The original bug sorts an immutable list, causing zero disk enqueues.
    List<Long> expected = LodDiskNearestScan.scan(directory, -1, -1, 8192);
    try {
      expected.sort(exact(-1, -1));
      throw new AssertionError("expected immutable scanner output");
    } catch (UnsupportedOperationException required) { checks++; }

    var cursor = LodDiskNearestScan.start(directory, -1, -1, 8192);
    var result = drain(cursor);
    require(result.failure() == null, "scanner reported no I/O error");
    require(result.examined() == 8208, "every matching filename consumes one bounded scan entry");
    require(result.turns() >= 33, "thirty-three bounded turns");
    require(result.keys().equals(expected), "cursor matches exact synchronous scan");
    require(result.keys().get(0) == key(-1, -1), "late near file arrives in output");
    try {
      result.keys().sort(Comparator.naturalOrder());
      throw new AssertionError("expected immutable cursor output");
    } catch (UnsupportedOperationException required) { checks++; }

    // The actual renderer consumer now uses reconcile() rather than sort().
    LinkedHashSet<Long> oldQueue = new LinkedHashSet<>();
    for (int i = 0; i < 8192; i++)
      oldQueue.add(key(-200000 - i, 300000 + i));
    List<Long> nearest = LodDiskNearestScan.reconcile(oldQueue, result.keys(), -1, -1, 8192);
    require(nearest.size() == 8192, "queue strictly bounded after full-cap merge");
    require(nearest.contains(key(-1, -1)), "new near page displaces old far recovery");
    require(new HashSet<>(nearest).size() == nearest.size(), "merge never duplicates a page");
    ArrayList<Long> all = new ArrayList<>(oldQueue);
    all.addAll(result.keys());
    all.sort(exact(-1, -1));
    for (int i = 0; i < 8192; i++)
      require(nearest.get(i).equals(all.get(i)), "exact nearest union at " + i);
    LinkedHashSet<Long> admission = new LinkedHashSet<>();
    admission.addAll(nearest);
    require(admission.size() == 8192, "immutable list reaches production-shaped queue");
    require(admission.contains(key(-1, -1)), "late-near key was admitted");

    // Cancel during a paused multi-turn scan. The scan owns and closes its iterator.
    var abandoned = LodDiskNearestScan.start(directory, 0, 0, 8192);
    abandoned.advance();
    long deadline = System.nanoTime() + 3_000_000_000L;
    while (abandoned.turns() == 0 && System.nanoTime() < deadline) Thread.sleep(1);
    require(abandoned.turns() > 0 && abandoned.examined() <= 256,
        "first scan turn is bounded before cancellation");
    abandoned.cancel();
    require(abandoned.awaitStopped(3000), "cancelled index thread closes and terminates");
    require(abandoned.completed() == null, "cancelled index never publishes a stale result");

    // Explicit long-travel reindex around a new camera; selection must not stay pinned.
    var traveled = LodDiskNearestScan.start(directory, 16000, 20000, 8);
    var moved = drain(traveled);
    require(moved.failure() == null && moved.keys().size() == 8,
        "camera retarget selected eight pages");
    ArrayList<Long> travelExpected = new ArrayList<>(inputs);
    travelExpected.sort(exact(16000, 20000));
    for (int i = 0; i < 8; i++)
      require(moved.keys().get(i).equals(travelExpected.get(i)), "retarget rank " + i);
    require(!moved.keys().contains(key(-1, -1)), "distant old camera does not pin first near");

    // Signed-int extremes and duplicate old/new queue entries.
    LinkedHashSet<Long> extremes = new LinkedHashSet<>();
    Random random = new Random(0x1917bL);
    for (int i = 0; i < 8192; i++)
      extremes.add(key(random.nextInt(), random.nextInt()));
    ArrayList<Long> incoming = new ArrayList<>(extremes);
    incoming.add(key(Integer.MIN_VALUE, Integer.MAX_VALUE));
    try {
      LodDiskNearestScan.reconcile(extremes, incoming, Integer.MAX_VALUE,
          Integer.MIN_VALUE, 8192);
      throw new AssertionError("oversized incoming should fail closed");
    } catch (IllegalArgumentException expectedError) { checks++; }
    incoming.remove(incoming.size() - 1);
    var same = LodDiskNearestScan.reconcile(extremes, incoming,
        Integer.MAX_VALUE, Integer.MIN_VALUE, 8192);
    require(same.size() == extremes.size(), "dedup all repeated entries");
    var sorted = new ArrayList<>(extremes);
    sorted.sort(exact(Integer.MAX_VALUE, Integer.MIN_VALUE));
    require(same.equals(sorted), "extreme ranking matches BigInteger full sort");

    var missing = LodDiskNearestScan.start(directory.resolve("no-dir"), 0, 0, 8);
    var empty = drain(missing);
    require(empty.failure() == null && empty.keys().isEmpty(), "missing folder is empty");
  }

  public static void main(String[] args) throws Exception {
    Path root = Files.createTempDirectory("lod-incremental-check-");
    try {
      test(root);
      System.out.println("LOD_DISK_INCREMENTAL_PASS assertions=" + checks
          + " entriesPerTurn=" + LodDiskNearestScan.ENTRIES_PER_TURN
          + " maxKeys=" + LodDiskNearestScan.MAX_KEYS);
    } finally {
      try (var paths = Files.walk(root)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
          Files.deleteIfExists(path);
      }
    }
  }
}
