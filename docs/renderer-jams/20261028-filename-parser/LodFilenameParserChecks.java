package com.shnewbs.deepprimal.lod.client;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/** Exact old-parser equivalence over real filenames, signs, boundary values and malformed forms. */
public final class LodFilenameParserChecks {
  private static long assertions;
  private LodFilenameParserChecks() {}

  private static void require(boolean condition, String why) {
    assertions++;
    if (!condition) throw new AssertionError(why);
  }

  private static Long historicalParser(String filename) {
    if (!filename.endsWith(".plod") || filename.length() > 30) return null;
    String base = filename.substring(0, filename.length() - 5);
    int sep = base.indexOf('_');
    if (sep <= 0 || sep == base.length() - 1 || base.indexOf('_', sep + 1) != -1) return null;
    try {
      int x = Integer.parseInt(base.substring(0, sep));
      int z = Integer.parseInt(base.substring(sep + 1));
      if (!base.equals(x + "_" + z)) return null;
      return ((long) x << 32) ^ (z & 0xffffffffL);
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private static Comparator<Long> ranked(int cameraX, int cameraZ) {
    return Comparator.<Long>comparingLong(key -> Math.max(
        Math.abs((long) (int) (key >> 32) - cameraX),
        Math.abs((long) (int) (long) key - cameraZ)))
        .thenComparing(key -> {
          BigInteger dx = BigInteger.valueOf((long) (int) (key >> 32) - cameraX);
          BigInteger dz = BigInteger.valueOf((long) (int) (long) key - cameraZ);
          return dx.multiply(dx).add(dz.multiply(dz));
        })
        .thenComparingInt(key -> (int) (key >> 32))
        .thenComparingInt(key -> (int) (long) key);
  }

  private static void checkDirectory(Path folder, List<String> filenames) throws Exception {
    HashSet<Long> seen = new HashSet<>();
    ArrayList<Long> valid = new ArrayList<>();
    for (String name : filenames) {
      Long parsed = historicalParser(name);
      if (parsed != null && seen.add(parsed)) valid.add(parsed);
    }
    int[][] cameras = {{0, 0}, {Integer.MIN_VALUE, Integer.MAX_VALUE},
        {Integer.MAX_VALUE, Integer.MIN_VALUE}, {-7231, -4102}, {500000, 100000}};
    for (int[] camera : cameras) {
      valid.sort(ranked(camera[0], camera[1]));
      for (int cap : new int[] {1, 7, 256, 2048, 8192}) {
        List<Long> actual = LodDiskNearestScan.scan(folder, camera[0], camera[1], cap);
        int expected = Math.min(cap, valid.size());
        require(actual.size() == expected, "unexpected retained count for cap=" + cap);
        for (int i = 0; i < actual.size(); i++)
          require(actual.get(i).equals(valid.get(i)), "parser/order mismatch index=" + i);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Path folder = Files.createTempDirectory("lod-canonical-parser-");
    try {
      HashSet<String> names = new HashSet<>();
      for (int x : new int[] {0, 1, -1, 10, -10, Integer.MAX_VALUE, Integer.MIN_VALUE})
        for (int z : new int[] {0, 1, -1, 10, -10, Integer.MAX_VALUE, Integer.MIN_VALUE})
          names.add(x + "_" + z + ".plod");
      names.addAll(List.of("-0_0.plod", "0_-0.plod", "00_0.plod", "0_00.plod",
          "+1_2.plod", "-01_1.plod", "1_-01.plod", "1_+1.plod", "1_1_.plod",
          "1__1.plod", "_1.plod", "1_.plod", "2147483648_0.plod",
          "-2147483649_0.plod", "0_2147483648.plod", "0_-2147483649.plod",
          " 1_1.plod", "1_1 .plod", "1_1_0.plod", "1_1.PLOD",
          "1_1.plodx", "000000000000000000000_1.plod", "1_-.plod"));
      Random random = new Random(0xD33F1234L);
      for (int i = 0; i < 1500; i++) {
        int x = random.nextInt(), z = random.nextInt();
        String sx = Integer.toString(x), sz = Integer.toString(z);
        names.add(sx + "_" + sz + ".plod");
        if (i % 2 == 0) names.add("0" + sx + "_" + sz + ".plod");
        if (i % 3 == 0) names.add(sx + "_" + sz + "0.plod");
        if (i % 5 == 0) names.add(sx + "__" + sz + ".plod");
      }
      ArrayList<String> files = new ArrayList<>(names);
      for (String name : files) Files.createFile(folder.resolve(name));
      checkDirectory(folder, files);
      System.out.println("LOD_FILENAME_PARSER_PASS assertions=" + assertions
          + " fileNames=" + files.size() + " parity=historicalCanonical");
    } finally {
      try (var walk = Files.walk(folder)) {
        for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
      }
    }
  }
}
