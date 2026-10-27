package com.shnewbs.deepprimal.lod.client;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Small render-thread cache of exact-length transform arrays. The GPU uniform writer
 * requires the array length to match the draw count; a single resizable array would
 * submit stale transforms when visibility changes. Retain a few common counts instead
 * of rebuilding every transform object as the visible part count oscillates.
 *
 * <p>The arrays are only reused after the synchronous uniform-write call has returned.
 * No native buffer or cross-frame GPU ownership is cached here.
 */
final class ReusableTransformArrays<T> {
  private final Object[] entries;
  private final int[] lengths;
  private int filled, next;

  ReusableTransformArrays(int count) {
    if (count < 1 || count > 16) throw new IllegalArgumentException("cache slot count");
    entries = new Object[count];
    lengths = new int[count];
  }

  @SuppressWarnings("unchecked")
  T[] acquire(int length, IntFunction<T[]> factory) {
    if (length < 0) throw new IllegalArgumentException("negative transform count");
    Objects.requireNonNull(factory, "factory");
    for (int i = 0; i < filled; i++)
      if (lengths[i] == length) return (T[]) entries[i];
    T[] created = Objects.requireNonNull(factory.apply(length), "transforms");
    if (created.length != length) throw new IllegalArgumentException("nonexact transform array");
    entries[next] = created;
    lengths[next] = length;
    next = (next + 1) % entries.length;
    if (filled < entries.length) filled++;
    return created;
  }

  void clear() {
    Arrays.fill(entries, null);
    filled = next = 0;
  }
}
