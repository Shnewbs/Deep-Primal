package com.shnewbs.deepprimal.core;

import java.util.Arrays;

/** Bounded elapsed-nanosecond window. Sort/copy only on an explicit diagnostic read. */
public final class RollingTiming {
  public record Snapshot(int samples, long p50Nanos, long p95Nanos, long p99Nanos, long maxNanos) {}
  private final long[] values;
  private int size, next;

  public RollingTiming(int capacity) {
    if (capacity < 1 || capacity > 4096) throw new IllegalArgumentException("Timing capacity");
    values = new long[capacity];
  }

  public synchronized void record(long nanos) {
    if (nanos < 0) throw new IllegalArgumentException("Negative duration");
    values[next] = nanos;
    next = (next + 1) % values.length;
    size = Math.min(values.length, size + 1);
  }

  public synchronized void reset() {
    size = next = 0;
  }

  public synchronized Snapshot snapshot() {
    if (size == 0) return new Snapshot(0, 0, 0, 0, 0);
    long[] sorted = Arrays.copyOf(values, size);
    Arrays.sort(sorted);
    return new Snapshot(size, sorted[(size * 50 + 99) / 100 - 1],
        sorted[(size * 95 + 99) / 100 - 1], sorted[(size * 99 + 99) / 100 - 1], sorted[size - 1]);
  }
}
