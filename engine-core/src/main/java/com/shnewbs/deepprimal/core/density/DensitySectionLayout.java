package com.shnewbs.deepprimal.core.density;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immutable, opt-in V8 projection from exact chunk occupancy to 16-cubed native
 * section coordinates. This class does not write Minecraft blocks or enable V8.
 *
 * <p>The packed index is {@code (localY << 8) | (localZ << 4) | localX}; X
 * changes fastest, then Z, then Y. Unlike the source chunk's column-major bits,
 * this is the native section palette index. Section indices and partial coverage
 * use floor division, including around negative world Y and nonaligned slices.
 * Empty sections share an internal immutable zero array and allocate no 512-byte
 * bitmap until a caller explicitly requests a copy of its packed words.
 */
public final class DensitySectionLayout {
  public static final int SIDE = 16;
  public static final int CELLS = SIDE * SIDE * SIDE;
  public static final int WORDS = CELLS / Long.SIZE;
  private static final long[] EMPTY = new long[WORDS];

  @FunctionalInterface
  public interface SolidBlockConsumer {
    /** X and Z are chunk-local; Y is an absolute world block coordinate. */
    void accept(int localX, int worldY, int localZ);
  }

  private final int sectionY;
  private final int baseY;
  private final int minCoveredY;
  private final int maxCoveredYExclusive;
  private final long[] occupancy;
  private final int solidCount;

  private DensitySectionLayout(int sectionY, int minCoveredY, int maxCoveredYExclusive,
                               long[] occupancy, int solidCount) {
    this.sectionY = sectionY;
    this.baseY = sectionY * SIDE;
    this.minCoveredY = minCoveredY;
    this.maxCoveredYExclusive = maxCoveredYExclusive;
    this.occupancy = occupancy;
    this.solidCount = solidCount;
  }

  /**
   * Produces one immutable section for every section intersected by the source
   * chunk, including empty sections. Reads solid cells once; never resamples a
   * density graph, changes its seed, or looks up/forces a world chunk.
   *
   * <p>At the maximum 2048-block source height, at most 129 sections are returned
   * and at most 129 * 512 bytes of bitmap words are allocated, before object
   * overhead. Empty sections do not allocate their own bitmap words.
   */
  public static List<DensitySectionLayout> project(DensityChunk chunk) {
    Objects.requireNonNull(chunk, "chunk");
    int minY = chunk.minY();
    int maxY = chunk.maxYExclusive();
    int first = Math.floorDiv(minY, SIDE);
    int last = Math.floorDiv(maxY - 1, SIDE);
    int count = last - first + 1;
    long[][] bits = new long[count][];
    int[] counts = new int[count];

    chunk.forEachSolid((x, y, z) -> {
      int slot = Math.floorDiv(y, SIDE) - first;
      if (bits[slot] == null) bits[slot] = new long[WORDS];
      int index = (Math.floorMod(y, SIDE) << 8) | (z << 4) | x;
      bits[slot][index >>> 6] |= 1L << (index & 63);
      counts[slot]++;
    });

    var result = new ArrayList<DensitySectionLayout>(count);
    for (int i = 0; i < count; i++) {
      int sectionY = first + i;
      int baseY = sectionY * SIDE;
      result.add(new DensitySectionLayout(sectionY, Math.max(minY, baseY),
          Math.min(maxY, baseY + SIDE), bits[i] == null ? EMPTY : bits[i], counts[i]));
    }
    return List.copyOf(result);
  }

  /** Mathematical section coordinate, not truncating division toward zero. */
  public int sectionY() { return sectionY; }
  public int baseY() { return baseY; }
  public int minCoveredY() { return minCoveredY; }
  public int maxCoveredYExclusive() { return maxCoveredYExclusive; }
  public int solidCount() { return solidCount; }
  public boolean empty() { return solidCount == 0; }

  /**
   * Reads a section-local cell. An unrepresented portion of a partial edge
   * section is air, so native callers can examine all 4096 palette positions.
   */
  public boolean solidLocal(int x, int y, int z) {
    if ((x | y | z) < 0 || x >= SIDE || y >= SIDE || z >= SIDE)
      throw new IllegalArgumentException("local section coordinate outside 0..15");
    int index = (y << 8) | (z << 4) | x;
    return (occupancy[index >>> 6] & (1L << (index & 63))) != 0;
  }

  /** Defensive copy in native X-fastest section-palette bit order. */
  public long[] packedWords() { return occupancy.clone(); }

  /** Iterate in deterministic native section-palette Y, Z, X order. */
  public void forEachSolid(SolidBlockConsumer consumer) {
    Objects.requireNonNull(consumer, "consumer");
    if (solidCount == 0) return;
    for (int word = 0; word < WORDS; word++) {
      long remaining = occupancy[word];
      while (remaining != 0) {
        int index = word * Long.SIZE + Long.numberOfTrailingZeros(remaining);
        consumer.accept(index & 15, baseY + (index >>> 8), (index >>> 4) & 15);
        remaining &= remaining - 1;
      }
    }
  }
}
