package com.shnewbs.deepprimal.lod.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;

/** Real-directory regressions for bounded stationary rediscovery after a successful nonempty scan. */
public final class LodDiskNonemptyRefreshChecks {
  private static long assertions;
  private LodDiskNonemptyRefreshChecks() {}

  private static void require(boolean correct, String why) {
    assertions++;
    if (!correct) throw new AssertionError(why);
  }

  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private static void scanComplete(LodDiskIndexService service, Path folder, int x, int z,
      LinkedHashSet<Long> queue, ArrayList<Long> forgotten, int maxTicks) throws Exception {
    for (int tick = 0; tick < maxTicks; tick++) {
      require(!service.tick(folder, x, z, queue, true, forgotten::add), "disk scan failed");
      require(queue.size() <= LodDiskNearestScan.MAX_KEYS, "recovery exceeded 8192 keys");
      if (service.started() && !service.scanning()) return;
      Thread.sleep(1);
    }
    throw new AssertionError("disk cursor did not finish after " + maxTicks + " ticks");
  }

  private static void stationaryFind(LodDiskIndexService service, Path folder,
      LinkedHashSet<Long> queue, ArrayList<Long> forgotten, long target, int maxTicks)
      throws Exception {
    for (int tick = 0; tick < maxTicks; tick++) {
      require(!service.tick(folder, 0, 0, queue, true, forgotten::add), "background rescan failed");
      require(queue.size() <= LodDiskNearestScan.MAX_KEYS, "rescan queue exceeded cap");
      if (queue.contains(target)) return;
      Thread.sleep(1);
    }
    throw new AssertionError("stationary camera did not rediscover persisted " + target);
  }

  private static void deleteTree(Path root) throws Exception {
    try (var walk = Files.walk(root)) {
      for (Path path : walk.sorted(Comparator.reverseOrder()).toList())
        Files.deleteIfExists(path);
    }
  }

  public static void main(String[] args) throws Exception {
    Path root = Files.createTempDirectory("lod-nonempty-refresh-");
    try {
      Path small = Files.createDirectory(root.resolve("small"));
      Files.createFile(small.resolve("100_0.plod"));
      LodDiskIndexService service = new LodDiskIndexService();
      LinkedHashSet<Long> queue = new LinkedHashSet<>();
      ArrayList<Long> forgotten = new ArrayList<>();
      scanComplete(service, small, 0, 0, queue, forgotten, 250);
      require(queue.contains(key(100, 0)), "initial persisted nonempty page missing");
      require(!service.scanning(), "first scan did not release active cursor");

      // A stationary player must discover newer files, even though the folder
      // was already populated. Avoid running a full enumeration each tick.
      Files.createFile(small.resolve("-3_4.plod"));
      for (int tick = 0; tick < 390; tick++) {
        require(!service.tick(small, 0, 0, queue, true, forgotten::add), "quiet poll failed");
        require(!queue.contains(key(-3, 4)), "nonempty refresh ignored cadence");
        require(!service.scanning(), "background worker started before poll interval");
      }
      stationaryFind(service, small, queue, forgotten, key(-3, 4), 400);
      require(queue.contains(key(100, 0)), "refresh evicted a valid existing page");
      require(forgotten.isEmpty(), "refresh unexpectedly discarded a queued page");
      scanComplete(service, small, 0, 0, queue, forgotten, 250);

      // The polling interval is renewed after a successful refresh. Changes in
      // an unrelated file must not cause per-tick directory scans or queue churn.
      Files.createFile(small.resolve("badname.plod"));
      for (int tick = 0; tick < 250; tick++) {
        require(!service.tick(small, 0, 0, queue, true, forgotten::add), "quiet repeat failed");
        require(!service.scanning(), "successful nonempty refresh looped too soon");
        require(queue.size() == 2, "invalid filename changed recovery count");
      }
      Files.createFile(small.resolve("0_0.plod"));
      stationaryFind(service, small, queue, forgotten, key(0, 0), 400);
      scanComplete(service, small, 0, 0, queue, forgotten, 250);
      require(queue.size() == 3, "periodic scan duplicated/replaced valid keys");
      service.reset();

      // At capacity, a newly saved near file must displace the least useful
      // distant queued page and never grow recovery beyond MAX_KEYS.
      Path full = Files.createDirectory(root.resolve("full"));
      Files.createFile(full.resolve("900_0.plod"));
      queue.clear();
      forgotten.clear();
      for (int n = 0; n < LodDiskNearestScan.MAX_KEYS; n++)
        queue.add(key(100000 + n, 1));
      scanComplete(service, full, 0, 0, queue, forgotten, 1800);
      require(queue.size() == LodDiskNearestScan.MAX_KEYS, "initial saturated queue not bounded");
      require(queue.contains(key(900, 0)), "first near persisted page not prioritized");
      int initialForgotten = forgotten.size();
      Files.createFile(full.resolve("-1_0.plod"));
      stationaryFind(service, full, queue, forgotten, key(-1, 0), 1800);
      scanComplete(service, full, 0, 0, queue, forgotten, 1800);
      require(queue.size() == LodDiskNearestScan.MAX_KEYS, "new file exceeded queue cap");
      require(queue.contains(key(900, 0)), "first page lost on subsequent rescan");
      require(forgotten.size() == initialForgotten + 1, "nearest page did not shed one far recovery");
      service.reset();

      System.out.println("LOD_NONEMPTY_REFRESH_PASS assertions=" + assertions
          + " stationary=true cadence=400 cap=" + LodDiskNearestScan.MAX_KEYS);
    } finally {
      deleteTree(root);
    }
  }
}