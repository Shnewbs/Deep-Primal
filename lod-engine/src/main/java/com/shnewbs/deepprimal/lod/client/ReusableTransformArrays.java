package com.shnewbs.deepprimal.lod.client;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Render-thread cache for exact-length transform arrays. The synchronous GPU uniform
 * writer requires an exact draw count, so a single over-sized array is not safe.
 *
 * <p>Both the slot count and sum of retained element counts are bounded. Element
 * counts are an admission proxy, NOT a measured Java-heap byte budget: actual
 * Transform sizes and GPU resource lifetimes must be profiled separately. A draw
 * count larger than the retention budget is constructed but never cached.
 * No native buffer or cross-frame GPU ownership is cached here.
 */
final class ReusableTransformArrays<T> {
  static final int DEFAULT_RETAINED_ELEMENTS = 32768;
  private final Object[] entries;
  private final int[] lengths;
  private final int maxRetainedElements;
  private int filled, next, retainedElements;

  ReusableTransformArrays(int count) {
    this(count, DEFAULT_RETAINED_ELEMENTS);
  }

  ReusableTransformArrays(int count, int maxRetainedElements) {
    if (count < 1 || count > 16) throw new IllegalArgumentException("cache slot count");
    if (maxRetainedElements < 1) throw new IllegalArgumentException("retention elements");
    entries = new Object[count];
    lengths = new int[count];
    this.maxRetainedElements = maxRetainedElements;
  }

  @SuppressWarnings("unchecked")
  T[] acquire(int length, IntFunction<T[]> factory) {
    if (length < 0) throw new IllegalArgumentException("negative transform count");
    Objects.requireNonNull(factory, "factory");
    // Occupied entries form a circular FIFO ending immediately before next.
    for (int i = 0; i < filled; i++) {
      int slot = (next - filled + i + entries.length) % entries.length;
      if (lengths[slot] == length) return (T[]) entries[slot];
    }
    // Validate first: a failed factory must not evict previously valid arrays.
    T[] created = Objects.requireNonNull(factory.apply(length), "transforms");
    if (created.length != length) throw new IllegalArgumentException("nonexact transform array");
    if (length > maxRetainedElements) return created;
    // Subtraction avoids overflowing a sum when the requested count is very large.
    while (filled > 0 &&
        (filled == entries.length || retainedElements > maxRetainedElements - length)) {
      evictOldest();
    }
    entries[next] = created;
    lengths[next] = length;
    retainedElements += length;
    next = (next + 1) % entries.length;
    filled++;
    return created;
  }

  private void evictOldest() {
    int oldest = (next - filled + entries.length) % entries.length;
    retainedElements -= lengths[oldest];
    entries[oldest] = null;
    lengths[oldest] = 0;
    filled--;
  }

  int retainedElements() {
    return retainedElements;
  }

  int retainedArrays() {
    return filled;
  }

  void clear() {
    Arrays.fill(entries, null);
    Arrays.fill(lengths, 0);
    filled = next = retainedElements = 0;
  }
}
