package com.shnewbs.deepprimal.lod.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

/** Test the exact production coordinator consuming the scanner's immutable list. */
public final class LodDiskIndexServiceChecks {
  private static long assertions;
  private LodDiskIndexServiceChecks() {}
  private static void require(boolean ok, String cause) {
    assertions++;
    if (!ok) throw new AssertionError(cause);
  }
  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }
  private static void until(LodDiskIndexService service, Path folder, int x, int z,
      LinkedHashSet<Long> queue, List<Long> forgotten, int maximumTicks)
      throws InterruptedException {
    for (int tick = 0; tick < maximumTicks; tick++) {
      require(!service.tick(folder, x, z, queue, true, forgotten::add), "scan error");
      if (service.started() && !service.scanning()) return;
      Thread.sleep(1);
    }
    throw new AssertionError("index service did not complete in " + maximumTicks + " ticks");
  }
  public static void main(String[] args) throws Exception {
    Path root = Files.createTempDirectory("lod-consumer-");
    try {
      for (int n = 0; n < 8204; n++)
        Files.createFile(root.resolve((20000 + n) + "_" + (10000 + n % 123) + ".plod"));
      Files.createFile(root.resolve("-1_-1.plod"));
      var service = new LodDiskIndexService();
      var queue = new LinkedHashSet<Long>();
      ArrayList<Long> dropped = new ArrayList<>();
      for (int n = 0; n < 8192; n++) queue.add(key(-100000 - n, 300000 + n));
      require(!service.tick(root, -1, -1, queue, true, dropped::add), "start index");
      long newlySaved = key(-2, -2);
      queue.removeFirst();
      queue.add(newlySaved);
      until(service, root, -1, -1, queue, dropped, 5000);
      require(queue.contains(newlySaved), "post-start recovery preserved");
      require(queue.size() == 8192, "result queue at cap");
      require(queue.contains(key(-1, -1)), "immutable completed scan admitted the late near page");
      require(dropped.size() > 1000, "distant queue replaced with nearest scan results");
      require(!service.scanning(), "scan completion releases cursor reference");
      // Within the 64-chunk retarget hysteresis, do not start a new scan.
      for (int tick = 0; tick < 64; tick++) {
        require(!service.tick(root, -30, -30, queue, true, dropped::add), "unexpected error");
        require(!service.scanning(), "unnecessary rescan on small movement");
      }
      // Camera movement across the threshold triggers a fresh, independently-ranked scan.
      until(service, root, 20100, 10000, queue, dropped, 5000);
      require(queue.size() == 8192, "retarget queue at cap");
      long nearest = key(20100, 10000 + 100 % 123);
      require(queue.contains(nearest), "retarget includes nearest saved page");
      require(service.started() && !service.scanning(), "completed retarget");

      // Disabling persistence tears down future scans without clearing the caller's queue.
      int beforeDisable = queue.size();
      service.tick(root, 0, 0, queue, false, dropped::add);
      require(!service.started() && !service.scanning(), "disable cancels worker cursor");
      require(queue.size() == beforeDisable, "disable retains queued edit guards");
      until(service, root, -1, -1, queue, dropped, 5000);
      require(queue.contains(key(-1, -1)), "re-enabling persistence can discover old camera");
      service.reset();
      require(!service.scanning(), "world reset cancels cursor");
      System.out.println("LOD_DISK_SERVICE_PASS assertions=" + assertions
          + " capped=" + queue.size() + " dropped=" + dropped.size());
    } finally {
      try (var paths = Files.walk(root)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
          Files.deleteIfExists(path);
      }
    }
  }
}
