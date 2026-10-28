package com.shnewbs.deepprimal.lod.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

/** Actual source-level I/O and late-world-save retry regression, not a Minecraft client test. */
public final class LodDiskRetryRegression {
  private static long checks;
  private LodDiskRetryRegression() {}
  private static void check(boolean condition, String why) {
    checks++;
    if (!condition) throw new AssertionError(why);
  }
  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }
  private static int pump(LodDiskIndexService service, Path folder,
      LinkedHashSet<Long> queue, long target, boolean expectFailure) throws Exception {
    int failures = 0;
    for (int tick = 0; tick < 1400; tick++) {
      if (service.tick(folder, 0, 0, queue, true, unused -> {})) failures++;
      if (expectFailure && failures > 0 && !service.scanning()) return failures;
      if (!expectFailure && queue.contains(target)) return failures;
      Thread.sleep(1);
    }
    throw new AssertionError("same-camera scan did not recover before deadline");
  }
  public static void main(String[] args) throws Exception {
    Path root = Files.createTempDirectory("deepprimal-lod-retry-");
    try {
      Path late = root.resolve("late-world-save");
      var queue = new LinkedHashSet<Long>();
      var service = new LodDiskIndexService();
      for (int i = 0; i < 200 && (!service.started() || service.scanning()); i++) {
        check(!service.tick(late, 0, 0, queue, true, unused -> {}), "missing folder should be silent");
        Thread.sleep(1);
      }
      check(service.started() && !service.scanning(), "initial absent-folder scan finished");
      Files.createDirectory(late);
      Files.createFile(late.resolve("-3_5.plod"));
      check(pump(service, late, queue, key(-3, 5), false) == 0, "late folder silently retried");
      check(queue.contains(key(-3, 5)), "valid page recovered without camera move");
      service.reset();

      Path unreadable = Files.createDirectory(root.resolve("recoverable-io"));
      Files.createFile(unreadable.resolve("7_-8.plod"));
      queue.clear();
      Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(unreadable);
      try {
        Files.setPosixFilePermissions(unreadable, Set.of());
        int first = pump(service, unreadable, queue, key(7, -8), true);
        check(first == 1, "first inaccessible directory reports one failure");
      } finally {
        Files.setPosixFilePermissions(unreadable, permissions);
      }
      check(pump(service, unreadable, queue, key(7, -8), false) == 0,
          "restored folder has no additional I/O errors");
      check(queue.contains(key(7, -8)), "transient I/O recovers same camera");
      service.reset();
      System.out.println("LOD_DISK_RETRY_REGRESSION_PASS assertions=" + checks);
    } finally {
      try (var paths = Files.walk(root)) {
        for (Path file : paths.sorted(Comparator.reverseOrder()).toList())
          Files.deleteIfExists(file);
      }
    }
  }
}
