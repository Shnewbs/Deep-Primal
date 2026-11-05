package com.shnewbs.deepprimal.lod.client;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;

public final class LodDiskNearestScanChecks {
  private static long checks;
  private LodDiskNearestScanChecks() {}

  private static void require(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message);
  }

  private static long key(int x, int z) {
    return ((long)x << 32) ^ (z & 0xffffffffL);
  }

  private static int x(long key) { return (int)(key >> 32); }
  private static int z(long key) { return (int)key; }

  private static Comparator<Long> exact(int cx, int cz) {
    return Comparator.<Long>comparingLong(k -> {
          long dx = Math.abs((long)x(k) - cx);
          long dz = Math.abs((long)z(k) - cz);
          return Math.max(dx, dz);
        })
        .thenComparing(k -> {
          BigInteger dx = BigInteger.valueOf((long)x(k) - cx);
          BigInteger dz = BigInteger.valueOf((long)z(k) - cz);
          return dx.multiply(dx).add(dz.multiply(dz));
        })
        .thenComparingInt(LodDiskNearestScanChecks::x)
        .thenComparingInt(LodDiskNearestScanChecks::z);
  }

  public static void main(String[] args) throws Exception {
    Path root = Files.createTempDirectory("lod-nearest-check-");
    try {
      Path missing = root.resolve("no-folder");
      require(LodDiskNearestScan.scan(missing, 0, 0, 5).isEmpty(), "missing directory");
      for (int limit : new int[] {-1, 0, 8193, Integer.MAX_VALUE}) {
        try {
          LodDiskNearestScan.scan(root, 0, 0, limit);
          throw new AssertionError("invalid limit admitted: " + limit);
        } catch (IllegalArgumentException expected) { checks++; }
      }
      // Populate 8,204 far pages first. The final nearby page MUST win despite
      // a directory scan that previously stopped after the first 8,192 names.
      ArrayList<Long> keys = new ArrayList<>();
      for (int n = 0; n < 8204; n++) {
        int px = 9000 + n, pz = 10000 + (n % 257);
        Files.createFile(root.resolve(px + "_" + pz + ".plod"));
        keys.add(key(px, pz));
      }
      Files.createFile(root.resolve("0_0.plod"));
      keys.add(key(0, 0));
      for (String invalid : new String[] {
          "01_0.plod", "-0_0.plod", "0_+0.plod", "0_0_0.plod",
          "2147483648_0.plod", "0_-2147483649.plod", "a_b.plod",
          "0_0.plodx", "0_0.PLOD"})
        Files.createFile(root.resolve(invalid));
      for (int limit : new int[] {1, 7, 512, 8192}) {
        List<Long> found = LodDiskNearestScan.scan(root, 0, 0, limit);
        require(found.size() == limit, "size " + limit);
        require(found.get(0) == key(0, 0), "late nearest file missing");
        ArrayList<Long> expected = new ArrayList<>(keys);
        expected.sort(exact(0, 0));
        for (int i = 0; i < found.size(); i++)
          require(found.get(i).equals(expected.get(i)), "nearest priority at " + i);
      }
      // Extreme signed coordinates test the unsigned squared-distance high bits.
      Path extremes = Files.createDirectory(root.resolve("extremes"));
      ArrayList<Long> extremeKeys = new ArrayList<>();
      Random random = new Random(0x4f724ab1L);
      for (int i = 0; i < 700; i++) {
        int px = random.nextInt(), pz = random.nextInt();
        Path path = extremes.resolve(px + "_" + pz + ".plod");
        if (Files.exists(path)) continue;
        Files.createFile(path);
        extremeKeys.add(key(px, pz));
      }
      for (int[] camera : new int[][] {
          {Integer.MAX_VALUE, Integer.MIN_VALUE},
          {Integer.MIN_VALUE, Integer.MAX_VALUE},
          {0, 0}, {-43772, 99812}}) {
        ArrayList<Long> expected = new ArrayList<>(extremeKeys);
        expected.sort(exact(camera[0], camera[1]));
        List<Long> found = LodDiskNearestScan.scan(extremes, camera[0], camera[1], 70);
        for (int i = 0; i < found.size(); i++)
          require(found.get(i).equals(expected.get(i)), "extreme exact rank at " + i);
      }
      Thread.currentThread().interrupt();
      try {
        LodDiskNearestScan.scan(root, 0, 0, 8192);
        throw new AssertionError("cancelled scan accepted");
      } catch (CancellationException expected) {
        require(Thread.currentThread().isInterrupted(), "cancel should retain interrupt flag");
      } finally {
        Thread.interrupted();
      }
      System.out.println("LOD_DISK_NEAREST_PASS assertions=" + checks
          + " latePrefix=8205 extremes=" + extremeKeys.size()
          + " maxRetained=8192");
    } finally {
      try (var paths = Files.walk(root)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
          Files.deleteIfExists(path);
      }
    }
  }
}
