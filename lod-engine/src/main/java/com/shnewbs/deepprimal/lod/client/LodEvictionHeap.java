package com.shnewbs.deepprimal.lod.client;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Allocation-bounded farthest-first order for LOD GPU-cache pressure.
 *
 * <p>The caller's access-ordered map is never mutated by construction. A
 * bottom-up heap is linear in resident page count and each evicted page costs
 * O(log n), unlike sorting every resident page when only one must be removed.
 * All ordering fields are stored in primitive arrays (no per-page wrappers).
 * Preserve the previous stable-sort ordering: do not evict the protected page
 * until all other candidates, choose greater distance first, and break exact
 * distance ties by the source map's iteration order.
 *
 * <p>This chooses victims only. The caller owns GPU resource close, residency
 * accounting, deferred persistent recovery and water/material lifetime rules.
 */
final class LodEvictionHeap {
  private final long[] keys;
  private final double[] squaredDistances;
  private final int[] order;
  private final boolean[] protectedPage;
  private int size;

  LodEvictionHeap(Collection<Long> candidates, Long keep, double cameraX, double cameraZ) {
    int capacity = candidates.size();
    keys = new long[capacity];
    squaredDistances = new double[capacity];
    order = new int[capacity];
    protectedPage = new boolean[capacity];
    for (long key : candidates) {
      int i = size++;
      keys[i] = key;
      double dx = ((int) (key >> 32)) * 16.0 + 8.0 - cameraX;
      double dz = ((int) key) * 16.0 + 8.0 - cameraZ;
      squaredDistances[i] = dx * dx + dz * dz;
      protectedPage[i] = keep != null && key == keep.longValue();
      order[i] = i;
    }
    for (int i = (size >>> 1) - 1; i >= 0; i--) sink(i);
  }

  boolean hasNext() { return size != 0; }

  long next() {
    if (size == 0) throw new NoSuchElementException("no LOD cache eviction candidate");
    long result = keys[0];
    int last = --size;
    if (last != 0) {
      copy(last, 0);
      sink(0);
    }
    return result;
  }

  /** True when a precedes b in the previous stable farthest-first sort. */
  private boolean before(int a, int b) {
    if (protectedPage[a] != protectedPage[b]) return !protectedPage[a];
    int c = Double.compare(squaredDistances[a], squaredDistances[b]);
    return c != 0 ? c > 0 : order[a] < order[b];
  }

  private void sink(int i) {
    while (true) {
      int left = (i << 1) + 1;
      if (left >= size) return;
      int right = left + 1;
      int first = right < size && before(right, left) ? right : left;
      if (!before(first, i)) return;
      swap(first, i);
      i = first;
    }
  }

  private void copy(int from, int to) {
    keys[to] = keys[from];
    squaredDistances[to] = squaredDistances[from];
    protectedPage[to] = protectedPage[from];
    order[to] = order[from];
  }

  private void swap(int a, int b) {
    long key = keys[a];
    double distance = squaredDistances[a];
    boolean protect = protectedPage[a];
    int position = order[a];
    copy(b, a);
    keys[b] = key;
    squaredDistances[b] = distance;
    protectedPage[b] = protect;
    order[b] = position;
  }
}
