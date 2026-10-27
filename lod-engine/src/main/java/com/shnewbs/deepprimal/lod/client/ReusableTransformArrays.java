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
  // Updated only on the owning render thread. Read through snapshot() at the
  // existing once-per-second diagnostics boundary, not once per draw.
  private long requests, hits, allocations, uncachedOversized, evictions;
  private int lastRequested = -1, peakRequested, peakRetained;
  private final long[] drawCountBuckets = new long[5];

  /** Counts of successfully returned arrays since the last world/session reset. */
  record Snapshot(long requests, long hits, long allocations, long uncachedOversized,
      long evictions, int retainedElements, int retainedArrays, int peakRetainedElements,
      int lastDrawCount, int peakDrawCount, long count0to512, long count513to2048,
      long count2049to8192, long count8193to32768, long countOver32768) {}


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
      if (lengths[slot] == length) {
        record(length, true);
        return (T[]) entries[slot];
      }
    }
    // Validate first: a failed factory must not evict previously valid arrays.
    T[] created = Objects.requireNonNull(factory.apply(length), "transforms");
    if (created.length != length) throw new IllegalArgumentException("nonexact transform array");
    if (length > maxRetainedElements) {
      uncachedOversized++;
      record(length, false);
      return created;
    }
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
    record(length, false);
    return created;
  }

  private void evictOldest() {
    int oldest = (next - filled + entries.length) % entries.length;
    retainedElements -= lengths[oldest];
    evictions++;
    entries[oldest] = null;
    lengths[oldest] = 0;
    filled--;
  }

  private void record(int length, boolean reused) {
    requests++;
    if (reused) hits++;
    else allocations++;
    lastRequested = length;
    peakRequested = Math.max(peakRequested, length);
    peakRetained = Math.max(peakRetained, retainedElements);
    int bucket = length <= 512 ? 0 : length <= 2048 ? 1 : length <= 8192 ? 2
        : length <= 32768 ? 3 : 4;
    drawCountBuckets[bucket]++;
  }

  Snapshot snapshot() {
    return new Snapshot(requests, hits, allocations, uncachedOversized, evictions,
        retainedElements, filled, peakRetained, lastRequested, peakRequested,
        drawCountBuckets[0], drawCountBuckets[1], drawCountBuckets[2],
        drawCountBuckets[3], drawCountBuckets[4]);
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
    requests = hits = allocations = uncachedOversized = evictions = 0;
    lastRequested = -1;
    peakRequested = peakRetained = 0;
    Arrays.fill(drawCountBuckets, 0);
  }
}
