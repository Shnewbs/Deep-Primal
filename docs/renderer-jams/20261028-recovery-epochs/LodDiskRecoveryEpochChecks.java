package com.shnewbs.deepprimal.lod.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

/** Source-level independent ownership and fixed-camera rediscovery regression. */
public final class LodDiskRecoveryEpochChecks {
  private static long checks;
  private LodDiskRecoveryEpochChecks() {}
  private static void require(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }
  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }
  private static void finish(LodDiskIndexService service, Path folder,
      LinkedHashSet<Long> queue, List<Long> forgotten, int limit) throws Exception {
    for (int i = 0; i < limit; i++) {
      require(!service.tick(folder, 0, 0, queue, true, forgotten::add), "disk I/O failure");
      require(queue.size() <= LodDiskNearestScan.MAX_KEYS, "queue exceeded cap");
      if (service.started() && !service.scanning()) return;
      Thread.sleep(1);
    }
    throw new AssertionError("bounded scan did not finish");
  }
  private static void removeTree(Path path) throws Exception {
    try (var walk = Files.walk(path)) {
      for (Path file : walk.sorted(Comparator.reverseOrder()).toList())
        Files.deleteIfExists(file);
    }
  }
  public static void main(String[] args) throws Exception {
    Path root = Files.createTempDirectory("lod-epoch-");
    try {
      // A snapshot's farthest key is removed and requeued with a *new* reason
      // while the background scanner ranks a newly present near page.
      Path one = Files.createDirectory(root.resolve("one"));
      Files.createFile(one.resolve("0_0.plod"));
      LodDiskIndexService service = new LodDiskIndexService();
      LinkedHashSet<Long> queue = new LinkedHashSet<>();
      ArrayList<Long> forgotten = new ArrayList<>();
      for (int i = 0; i < 8192; i++) queue.add(key(100000 + i, 4));
      long renewed = key(108191, 4);
      require(!service.tick(one, 0, 0, queue, true, forgotten::add), "start scan");
      require(queue.remove(renewed), "remove prior recovery");
      require(queue.add(renewed), "enqueue renewed recovery");
      service.recoveryQueued(renewed);
      finish(service, one, queue, forgotten, 2000);
      require(queue.size() == 8192, "protected owner and closest disk page both fit");
      require(queue.contains(renewed), "post-start renewed owner discarded");
      require(!forgotten.contains(renewed), "post-start renewed owner forgotten");
      require(queue.contains(key(0, 0)), "closest new disk page starved by renewed owner");
      require(forgotten.size() == 1, "one old lower-priority page must be shed");
      require(!forgotten.contains(key(0, 0)), "new near page should never be evicted");
      service.reset();

      // When the first 256 discards are already applied, a new recovery for a
      // still-pending far key must survive the remaining bounded apply turns.
      Path many = Files.createDirectory(root.resolve("many"));
      for (int i = 0; i < 8192; i++) Files.createFile(many.resolve(i + "_0.plod"));
      queue.clear();
      forgotten.clear();
      for (int i = 0; i < 8192; i++) queue.add(key(150000 + i, 0));
      renewed = key(158191, 0);
      boolean enteredApply = false;
      for (int i = 0; i < 1500; i++) {
        require(!service.tick(many, 0, 0, queue, true, forgotten::add), "scan failed");
        if (!forgotten.isEmpty()) { enteredApply = true; break; }
        Thread.sleep(1);
      }
      require(enteredApply, "expected incremental discards");
      require(queue.remove(renewed), "pending far key still present");
      require(queue.add(renewed), "renewal during deferred apply");
      service.recoveryQueued(renewed);
      finish(service, many, queue, forgotten, 2000);
      require(queue.size() <= 8192, "bounded recovery after multi-turn apply");
      require(queue.contains(renewed), "renewal during apply was discarded");
      require(!forgotten.contains(renewed), "renewal during apply was forgotten");
      require(queue.contains(key(0, 0)), "closest selected file admitted");
      service.reset();

      // A *successful* empty-directory scan also needs paced rediscovery.
      Path empty = Files.createDirectory(root.resolve("empty"));
      queue.clear();
      forgotten.clear();
      finish(service, empty, queue, forgotten, 200);
      require(queue.isEmpty(), "empty directory should have no pages");
      Files.createFile(empty.resolve("-9_6.plod"));
      for (int i = 0; i < 190; i++) {
        require(!service.tick(empty, 0, 0, queue, true, forgotten::add), "unexpected I/O error");
        require(!queue.contains(key(-9, 6)), "empty retry ran before fixed backoff");
      }
      boolean found = false;
      for (int i = 0; i < 500; i++) {
        require(!service.tick(empty, 0, 0, queue, true, forgotten::add), "unexpected rescan error");
        if (queue.contains(key(-9, 6))) { found = true; break; }
        Thread.sleep(1);
      }
      require(found, "new file in successful-empty directory not found at fixed camera");
      service.reset();
      System.out.println("LOD_DISK_RECOVERY_EPOCH_PASS assertions=" + checks);
    } finally {
      removeTree(root);
    }
  }
}
