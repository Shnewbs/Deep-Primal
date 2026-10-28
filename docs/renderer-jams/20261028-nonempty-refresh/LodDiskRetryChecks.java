package com.shnewbs.deepprimal.lod.client;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/** Production-excluded same-camera retry/late-save test of the actual service and worker. */
public final class LodDiskRetryChecks {
  private static long assertions;

  private LodDiskRetryChecks() {}

  private static void require(boolean ok, String message) {
    assertions++;
    if (!ok) throw new AssertionError(message);
  }

  private static long key(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private static void runUntilIdle(LodDiskIndexService service, Path folder,
      LinkedHashSet<Long> queue, AtomicInteger errors) throws Exception {
    for (int tick = 0; tick < 2000; tick++) {
      if (service.tick(folder, 0, 0, queue, true, unused -> {})) errors.incrementAndGet();
      if (service.started() && !service.scanning()) return;
      Thread.sleep(1);
    }
    throw new AssertionError("first disk scan did not finish");
  }

  private static void untilFound(LodDiskIndexService service, Path folder,
      LinkedHashSet<Long> queue, long key, AtomicInteger errors) throws Exception {
    for (int tick = 0; tick < 2000; tick++) {
      if (service.tick(folder, 0, 0, queue, true, unused -> {})) errors.incrementAndGet();
      if (queue.contains(key)) return;
      Thread.sleep(1);
    }
    throw new AssertionError("same-camera retry never found " + key);
  }

  private static void missingThenCreated(Path root) throws Exception {
    Path folder = root.resolve("world-created-later");
    var queue = new LinkedHashSet<Long>();
    var errors = new AtomicInteger();
    var service = new LodDiskIndexService();
    runUntilIdle(service, folder, queue, errors);
    require(errors.get() == 0, "a late save directory is not an I/O failure");
    require(queue.isEmpty(), "no fabricated saved pages");
    Files.createDirectories(folder);
    Files.createFile(folder.resolve("-3_5.plod"));
    untilFound(service, folder, queue, key(-3, 5), errors);
    require(errors.get() == 0, "missing-folder recovery is silent");
    require(queue.size() == 1, "one known, exact persisted key recovered");
    // A successful nonempty scan stays quiet for most of its 400-tick
    // interval, then checks for newly persisted pages without camera motion.
    for (int tick = 0; tick < 390; tick++) {
      require(!service.tick(folder, 0, 0, queue, true, unused -> {}), "post-success failure");
      require(!service.scanning(), "successful scan re-opened directory too soon");
    }
    boolean rescanObserved = false;
    for (int tick = 0; tick < 100; tick++) {
      require(!service.tick(folder, 0, 0, queue, true, unused -> {}), "periodic rescan failed");
      rescanObserved |= service.scanning();
      require(queue.size() == 1, "unchanged directory must preserve recovery queue");
      Thread.sleep(1);
    }
    require(rescanObserved, "nonempty directory did not receive bounded stationary rescan");
    service.reset();
    System.out.println("LOD_DISK_LATE_FOLDER_PASS");
  }

  private static void accessDeniedThenRestored(Path root) throws Exception {
    Path folder = Files.createDirectory(root.resolve("permission-restored"));
    Files.createFile(folder.resolve("13_-21.plod"));
    var service = new LodDiskIndexService();
    var queue = new LinkedHashSet<Long>();
    var errors = new AtomicInteger();
    Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(folder);
    try {
      Files.setPosixFilePermissions(folder, Set.of());
      runUntilIdle(service, folder, queue, errors);
    } finally {
      Files.setPosixFilePermissions(folder, permissions);
    }
    // If the host is privileged and bypassed 000, the check does not fake a failure.
    if (errors.get() == 0) {
      require(queue.contains(key(13, -21)), "privileged host did not encounter a permission error");
      System.out.println("LOD_DISK_PERMISSION_TEST_NOT_APPLICABLE privileged filesystem");
    } else {
      require(errors.get() == 1, "one error report per failed directory scan");
      untilFound(service, folder, queue, key(13, -21), errors);
      require(errors.get() == 1, "restored directory needs no further failure reports");
      require(queue.size() == 1, "recovered exact saved key");
      System.out.println("LOD_DISK_IO_RETRY_PASS");
    }
    service.reset();
  }

  private static void cappedBackoff(Path root) throws Exception {
    Path missing = root.resolve("permanently-absent");
    var service = new LodDiskIndexService();
    var queue = new LinkedHashSet<Long>();
    var errors = new AtomicInteger();
    Field delay = LodDiskIndexService.class.getDeclaredField("retryDelayTicks");
    delay.setAccessible(true);
    boolean[] seen = new boolean[5];
    int[] expected = {20, 40, 80, 160, 320};
    int maximum = 0;
    for (int tick = 0; tick < 1700; tick++) {
      if (service.tick(missing, 0, 0, queue, true, unused -> {})) errors.incrementAndGet();
      int current = delay.getInt(service);
      maximum = Math.max(maximum, current);
      for (int n = 0; n < expected.length; n++) if (current == expected[n]) seen[n] = true;
      require(current <= 320, "backoff cannot exceed 320 client ticks");
      require(queue.isEmpty(), "missing directory cannot invent pages");
      Thread.sleep(1);
    }
    for (int n = 0; n < expected.length; n++)
      require(seen[n], "retry backoff reached " + expected[n]);
    require(maximum == 320, "backoff reaches its finite cap");
    require(errors.get() == 0, "missing directory never logs an I/O error");
    service.reset();
    require(delay.getInt(service) == 0, "world reset clears delay");
    System.out.println("LOD_DISK_BACKOFF_PASS ticks=1700 cap=" + maximum);
  }

  private static void cancellationAndRetarget(Path root) throws Exception {
    Path folder = Files.createDirectory(root.resolve("camera-recovery"));
    Files.createFile(folder.resolve("0_0.plod"));
    var service = new LodDiskIndexService();
    var queue = new LinkedHashSet<Long>();
    var errors = new AtomicInteger();
    runUntilIdle(service, folder, queue, errors);
    require(queue.contains(key(0, 0)), "initial camera page");
    service.reset();
    require(!service.scanning() && !service.started(), "reset clears active owner state");
    Files.createFile(folder.resolve("120_120.plod"));
    for (int n = 0; n < 30; n++) service.reset();
    for (int n = 0; n < 1500; n++) {
      require(!service.tick(folder, 120, 120, queue, true, unused -> {}), "retarget error");
      if (queue.contains(key(120, 120))) break;
      Thread.sleep(1);
    }
    require(queue.contains(key(120, 120)), "retarget after repeated reset uses latest world");
    service.reset();
    System.out.println("LOD_DISK_RESET_PASS");
  }

  public static void main(String[] args) throws Exception {
    Path root = Files.createTempDirectory("lod-disk-retry-final-");
    try {
      missingThenCreated(root);
      accessDeniedThenRestored(root);
      cappedBackoff(root);
      cancellationAndRetarget(root);
      System.out.println("LOD_DISK_RETRY_PASS assertions=" + assertions);
    } finally {
      try (var stream = Files.walk(root)) {
        for (Path file : stream.sorted(Comparator.reverseOrder()).toList())
          Files.deleteIfExists(file);
      }
    }
  }
}