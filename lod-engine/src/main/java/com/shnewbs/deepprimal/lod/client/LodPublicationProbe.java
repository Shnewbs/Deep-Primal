package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.core.RollingTiming;

/**
 * Opt-in CPU publication costs for one completed LOD metadata preparation.
 * Frozen bounds capture is separated from conservative union/reuse-mask work
 * so native two-back ON/OFF comparisons can distinguish extra publication
 * effort from the already reported fine-frustum predicates saved at extract.
 * No clocks or data structures are created here on the telemetry-off path.
 * This is not GPU upload completion, residency, frame time, or FPS.
 */
final class LodPublicationProbe {
  record Snapshot(RollingTiming.Snapshot freezeCpuNanos,
      RollingTiming.Snapshot metadataCpuNanos, RollingTiming.Snapshot totalCpuNanos,
      long preparedPages, long frozenParts, long adjacentAliasBits,
      long twoBackAliasBits) {}

  private final RollingTiming freezeCpuNanos, metadataCpuNanos, totalCpuNanos;
  private long preparedPages, frozenParts, adjacentAliasBits, twoBackAliasBits;

  LodPublicationProbe(int samples) {
    freezeCpuNanos = new RollingTiming(samples);
    metadataCpuNanos = new RollingTiming(samples);
    totalCpuNanos = new RollingTiming(samples);
  }

  /**
   * Called once after bounds/masks were prepared but before the new resident
   * Entry is installed. Failed later GPU retirement does not erase this cost.
   * A zero-length mask array is valid when broadphase is disabled.
   */
  synchronized void prepared(long freezeNanos, long metadataNanos,
      Object[][] levels, long[][] adjacent, long[][] twoBack) {
    if (freezeNanos < 0 || metadataNanos < 0
        || freezeNanos > Long.MAX_VALUE - metadataNanos
        || levels == null || adjacent == null || twoBack == null
        || (adjacent.length != 0 && adjacent.length != levels.length)
        || (twoBack.length != 0 && twoBack.length != levels.length))
      throw new IllegalArgumentException("invalid LOD publication observation");
    long parts = 0;
    for (Object[] level : levels) {
      if (level == null) throw new IllegalArgumentException("null LOD publication level");
      parts += level.length;
    }
    long adjacentBits = maskBits(adjacent);
    long twoBackBits = maskBits(twoBack);
    freezeCpuNanos.record(freezeNanos);
    metadataCpuNanos.record(metadataNanos);
    totalCpuNanos.record(freezeNanos + metadataNanos);
    preparedPages = increment(preparedPages);
    frozenParts = add(frozenParts, parts);
    adjacentAliasBits = add(adjacentAliasBits, adjacentBits);
    twoBackAliasBits = add(twoBackAliasBits, twoBackBits);
  }

  synchronized Snapshot snapshot() {
    return new Snapshot(freezeCpuNanos.snapshot(), metadataCpuNanos.snapshot(),
        totalCpuNanos.snapshot(), preparedPages, frozenParts,
        adjacentAliasBits, twoBackAliasBits);
  }

  synchronized void reset() {
    freezeCpuNanos.reset();
    metadataCpuNanos.reset();
    totalCpuNanos.reset();
    preparedPages = frozenParts = adjacentAliasBits = twoBackAliasBits = 0;
  }

  private static long maskBits(long[][] levels) {
    long count = 0;
    for (long[] words : levels) {
      if (words == null) throw new IllegalArgumentException("null LOD publication mask");
      for (long word : words) count += Long.bitCount(word);
    }
    return count;
  }

  private static long increment(long current) {
    return current == Long.MAX_VALUE ? current : current + 1;
  }

  private static long add(long current, long value) {
    return value > Long.MAX_VALUE - current ? Long.MAX_VALUE : current + value;
  }
}
