package com.shnewbs.deepprimal.engine;

import java.util.Random;

/** Isolated exact-method coordinate gate; not a native Minecraft test. */
public final class V8MaterializerCoordinateChecks {
  private static long assertions;
  private static void check(int x, int z) {
    boolean expected = valid(x) && valid(z);
    boolean accepted;
    try {
      DensityChunkMaterializer.check(new ChunkAccess(x, z));
      accepted = true;
    } catch (IllegalArgumentException rejected) {
      accepted = false;
    }
    assertions++;
    if (accepted != expected)
      throw new AssertionError("V8 coordinate admission x=" + x + " z=" + z
          + " accepted=" + accepted + " expected=" + expected);
  }
  private static boolean valid(int coordinate) {
    long origin = (long) coordinate * 16L;
    return origin >= Integer.MIN_VALUE && origin + 15L <= Integer.MAX_VALUE;
  }
  public static void main(String[] args) {
    int[] edges = {Integer.MIN_VALUE, -134217729, -134217728, -134217727,
        -1, 0, 1, 134217726, 134217727, 134217728, Integer.MAX_VALUE};
    for (int x : edges) for (int z : edges) check(x, z);
    Random random = new Random(0x5d4a_125L);
    for (int i = 0; i < 100000; i++) check(random.nextInt(), random.nextInt());
    for (int i = 0; i < 10000; i++) {
      int x = random.nextInt(-134217728, 134217728);
      int z = random.nextInt(-134217728, 134217728);
      check(x, z);
      check(134217728 + random.nextInt(10000), z);
      check(x, -134217729 - random.nextInt(10000));
    }
    System.out.println("V8_MATERIALIZER_COORDINATES_PASS assertions=" + assertions);
  }
}
