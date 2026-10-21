package com.shnewbs.primalengine.lod;

import java.util.*;

/** Immutable vertical runs preserve trees, builds, water and air gaps, not just surface height. */
public final class ColumnarChunk {
  public final int x, z, minY, height;
  public final long revision;
  private final List<String> palette;
  private final int[] offsets, runs;

  public ColumnarChunk(
      int x,
      int z,
      int minY,
      int height,
      long revision,
      List<String> palette,
      int[] offsets,
      int[] runs) {
    if (height < 1
        || height > 4096
        || palette.isEmpty()
        || palette.size() > 65536
        || offsets.length != 257
        || runs.length > 256 * height * 3
        || runs.length % 3 != 0) throw new IllegalArgumentException("LOD bounds");
    if ((long) minY + height > Integer.MAX_VALUE || offsets[0] != 0 || offsets[256] != runs.length)
      throw new IllegalArgumentException("LOD extents");
    for (int c = 0; c < 256; c++) {
      if (offsets[c] > offsets[c + 1]
          || offsets[c] < 0
          || offsets[c + 1] > runs.length
          || offsets[c] % 3 != 0
          || offsets[c + 1] % 3 != 0) throw new IllegalArgumentException("LOD offsets");
      int previous = minY;
      for (int j = offsets[c]; j < offsets[c + 1]; j += 3) {
        if (runs[j] != previous
            || runs[j + 1] <= runs[j]
            || runs[j + 1] > minY + height
            || runs[j + 2] < 0
            || runs[j + 2] >= palette.size()) throw new IllegalArgumentException("LOD run");
        previous = runs[j + 1];
      }
      if (previous != minY + height) throw new IllegalArgumentException("Missing vertical data");
    }
    this.x = x;
    this.z = z;
    this.minY = minY;
    this.height = height;
    this.revision = revision;
    this.palette = List.copyOf(palette);
    this.offsets = offsets.clone();
    this.runs = runs.clone();
  }

  public List<String> palette() {
    return palette;
  }

  public int[] offsets() {
    return offsets.clone();
  }

  public int[] runs() {
    return runs.clone();
  }

  public String state(int localX, int y, int localZ) {
    if (localX < 0 || localX > 15 || localZ < 0 || localZ > 15 || y < minY || y >= minY + height)
      throw new IndexOutOfBoundsException();
    int c = localZ * 16 + localX;
    for (int j = offsets[c]; j < offsets[c + 1]; j += 3)
      if (y < runs[j + 1]) return palette.get(runs[j + 2]);
    throw new AssertionError("Missing run");
  }

  public long estimatedBytes() {
    return 64L
        + 4L * (offsets.length + runs.length)
        + palette.stream().mapToLong(s -> 40L + 2L * s.length()).sum();
  }

  public static final class Builder {
    private final int x, z, minY, height;
    private final long revision;
    private int[] runs = new int[4096];
    private int size;
    private final int[] offsets = new int[257];
    private int columns;

    public Builder(int x, int z, int minY, int height, long revision) {
      if (height < 1 || height > 4096) throw new IllegalArgumentException("LOD height");
      this.x = x;
      this.z = z;
      this.minY = minY;
      this.height = height;
      this.revision = revision;
    }

    public int completedColumns() {
      return columns;
    }

    public void column(int[] ids) {
      if (columns >= 256 || ids.length != height) throw new IllegalArgumentException("LOD column");
      offsets[columns] = size;
      int start = 0, last = ids[0];
      for (int y = 1; y <= height; y++)
        if (y == height || ids[y] != last) {
          if (size + 3 > runs.length) runs = Arrays.copyOf(runs, runs.length * 2);
          runs[size++] = minY + start;
          runs[size++] = minY + y;
          runs[size++] = last;
          if (y < height) {
            last = ids[y];
            start = y;
          }
        }
      offsets[++columns] = size;
    }

    public ColumnarChunk finish(List<String> palette) {
      if (columns != 256) throw new IllegalStateException("Incomplete LOD");
      return new ColumnarChunk(
          x, z, minY, height, revision, palette, offsets, Arrays.copyOf(runs, size));
    }
  }
}
