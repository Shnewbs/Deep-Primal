package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Regression for externally mutated LOD samples after publication to a shared tile cache. */
public final class HorizonSamplesOwnershipChecks {
  private static long checks;
  private static void check(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }

  public static void run() {
    Random random = new Random(20261102);
    for (int detail = 0; detail <= 2; detail++) {
      int side = HorizonGeometry.cells(detail) + 1;
      short[] heights = new short[side * side];
      int[] colors = new int[heights.length];
      for (int i = 0; i < heights.length; i++) {
        heights[i] = (short) random.nextInt(65536);
        colors[i] = random.nextInt();
      }
      short[] expectedHeights = heights.clone();
      int[] expectedColors = colors.clone();
      HorizonSamples published = new HorizonSamples(detail, heights, colors);
      check(published.payloadBytes() == 6L * heights.length, "Tile byte budget changed");
      // The original job buffers may be reused immediately after publication.
      for (int i = 0; i < heights.length; i++) {
        heights[i] ^= 0x55;
        colors[i] ^= 0x55aa55aa;
      }
      short[] a = published.heights();
      int[] b = published.colors();
      for (int i = 0; i < a.length; i++) {
        check(a[i] == expectedHeights[i], "Producer changed cached heights");
        check(b[i] == expectedColors[i], "Producer changed cached colors");
      }
      // A network encoder/renderer must not be able to corrupt the cache either.
      a[0] ^= 0x1;
      b[0] ^= 0x1;
      check(published.heights()[0] == expectedHeights[0], "Consumer changed cached height");
      check(published.colors()[0] == expectedColors[0], "Consumer changed cached color");
      check(published.heights() != published.heights(), "Height getter leaked ownership");
      check(published.colors() != published.colors(), "Color getter leaked ownership");
      check(published.forDetail(detail) == published, "Same-detail reuse allocated a tile");
      for (int target = detail; target <= 2; target++) {
        HorizonSamples down = published.forDetail(target);
        int targetSide = HorizonGeometry.cells(target) + 1;
        int ratio = (side - 1) / (targetSide - 1);
        short[] actualHeights = down.heights();
        int[] actualColors = down.colors();
        for (int z = 0; z < targetSide; z++) for (int x = 0; x < targetSide; x++) {
          int source = z * ratio * side + x * ratio, dest = z * targetSide + x;
          check(actualHeights[dest] == expectedHeights[source], "Downsampled height changed");
          check(actualColors[dest] == expectedColors[source], "Downsampled color changed");
        }
        actualHeights[0] ^= 0x1;
        actualColors[0] ^= 0x1;
        check(down.heights()[0] == expectedHeights[0], "Derived height aliased consumer");
        check(down.colors()[0] == expectedColors[0], "Derived color aliased consumer");
      }
    }
    System.out.println("HORIZON_SNAPSHOT_OWNERSHIP_PASS assertions=" + checks
        + " producerAliasing=false consumerAliasing=false downsamplingStable=true");
  }
  public static void main(String[] args) { run(); }
  private HorizonSamplesOwnershipChecks() {}
}
