package com.shnewbs.deepprimal.core.lod;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Exact per-voxel and boundary oracle for accelerated immutable LOD column lookups. */
public final class ColumnarChunkLookupChecks {
  private static final List<String> PALETTE = List.of("air", "stone", "water", "vegetation");
  private static long checks;
  private static volatile int sink;

  private static void require(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }

  private static void invalid(Runnable call, String message) {
    try {
      call.run();
    } catch (IndexOutOfBoundsException expected) {
      checks++;
      return;
    }
    throw new AssertionError(message);
  }

  private static int[] column(Random rng, int height, int mode, int column) {
    int[] ids = new int[height];
    switch (mode) {
      case 0 -> Arrays.fill(ids, column % PALETTE.size());
      case 1 -> { for (int y = 0; y < height; y++) ids[y] = (column + y) % PALETTE.size(); }
      case 2 -> { for (int y = 0; y < height; y++) ids[y] = (column / 3 + y / 7) % PALETTE.size(); }
      case 3 -> {
        for (int y = 0; y < height;) {
          int id = rng.nextInt(PALETTE.size());
          int end = Math.min(height, y + 1 + rng.nextInt(13));
          Arrays.fill(ids, y, end, id);
          y = end;
        }
      }
      default -> throw new AssertionError(mode);
    }
    return ids;
  }

  private static void scenario(int height, int minY, int mode, long seed) {
    Random rng = new Random(seed);
    ColumnarChunk.Builder builder = new ColumnarChunk.Builder(-17, 23, minY, height, 42);
    int[][] expected = new int[256][];
    for (int c = 0; c < 256; c++) {
      expected[c] = column(rng, height, mode, c);
      builder.column(expected[c]);
    }
    ColumnarChunk chunk = builder.finish(PALETTE);
    require(chunk.height == height && chunk.minY == minY, "metadata");
    for (int c = 0; c < 256; c++) {
      int x = c % 16, z = c / 16;
      int[] ys = {0, height / 2, height - 1};
      for (int y : ys)
        require(chunk.state(x, minY + y, z).equals(PALETTE.get(expected[c][y])),
            "boundary height=" + height + " mode=" + mode + " column=" + c + " y=" + y);
      for (int i = 0; i < 12; i++) {
        int y = rng.nextInt(height);
        require(chunk.state(x, minY + y, z).equals(PALETTE.get(expected[c][y])),
            "random height=" + height + " mode=" + mode + " column=" + c + " y=" + y);
      }
    }
    // Exhaustively probe thin-roof / alternating-water scenes at the maximum supported height.
    if (height == 4096) for (int c = 0; c < 8; c++) for (int y = 0; y < height; y++)
      require(chunk.state(c, minY + y, 0).equals(PALETTE.get(expected[c][y])),
          "full height=" + height + " mode=" + mode + " column=" + c + " y=" + y);
    invalid(() -> chunk.state(-1, minY, 0), "negative local x");
    invalid(() -> chunk.state(16, minY, 0), "oversized local x");
    invalid(() -> chunk.state(0, minY, -1), "negative local z");
    invalid(() -> chunk.state(0, minY, 16), "oversized local z");
    if (minY > Integer.MIN_VALUE) invalid(() -> chunk.state(0, minY - 1, 0), "below world");
    invalid(() -> chunk.state(0, minY + height, 0), "above world");
  }

  private static void benchmark() {
    int height = 4096;
    var b = new ColumnarChunk.Builder(0, 0, -384, height, 1);
    for (int c = 0; c < 256; c++) {
      int[] ids = new int[height];
      for (int y = 0; y < height; y++) ids[y] = (c + y) & 3;
      b.column(ids);
    }
    ColumnarChunk chunk = b.finish(PALETTE);
    int[] ys = new int[65536];
    Random rng = new Random(206L);
    for (int i = 0; i < ys.length; i++) ys[i] = -384 + rng.nextInt(height);
    for (int j = 0; j < 2; j++) for (int i = 0; i < ys.length; i++)
      sink += chunk.state(i & 15, ys[i], (i >>> 4) & 15).length();
    long start = System.nanoTime();
    for (int j = 0; j < 5; j++) for (int i = 0; i < ys.length; i++)
      sink += chunk.state(i & 15, ys[i], (i >>> 4) & 15).length();
    System.out.println("LOD_COLUMN_LOOKUP_BENCH nanos=" + (System.nanoTime() - start)
        + " queries=" + (5 * ys.length) + " sink=" + sink);
  }

  public static void main(String[] args) {
    for (int height : new int[]{1, 2, 7, 64, 257})
      for (int minY : new int[]{-384, 0, 320, Integer.MAX_VALUE - height})
        for (int mode = 0; mode < 4; mode++)
          scenario(height, minY, mode, 0x206L + height * 17L + minY + mode);
    for (int minY : new int[]{-384, Integer.MAX_VALUE - 4096})
      for (int mode = 0; mode < 4; mode++)
        scenario(4096, minY, mode, 0x4096L + minY + mode);
    System.out.println("LOD_COLUMN_LOOKUP_PASS assertions=" + checks + " scenarios=88");
    if (args.length > 0 && "--bench".equals(args[0])) benchmark();
  }

  private ColumnarChunkLookupChecks() {}
}
