package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.core.RollingTiming;
import java.util.Objects;

/**
 * Bounded render-thread LOD page upload observations. A committed page can need several
 * extract frames; wall time includes pauses and pacing, but NOT GPU fence completion.
 * Records only fully committed pages, never a partially uploaded or discarded page.
 */
final class LodPageUploadProbe {
  enum Drop { STALE, MEMORY }

  record Snapshot(RollingTiming.Snapshot wallNanos, RollingTiming.Snapshot uploadFrames,
      RollingTiming.Snapshot committedBytes, long committedPages, long staleDrops,
      long memoryDrops, long peakUploadFrames) {}

  private final RollingTiming wallNanos, uploadFrames, committedBytes;
  private long committedPages, staleDrops, memoryDrops, peakUploadFrames;

  LodPageUploadProbe(int samples) {
    wallNanos = new RollingTiming(samples);
    uploadFrames = new RollingTiming(samples);
    committedBytes = new RollingTiming(samples);
  }

  synchronized void committed(long elapsedNanos, int frames, long bytes) {
    if (elapsedNanos < 0 || frames < 1 || bytes < 0)
      throw new IllegalArgumentException("invalid completed LOD page observation");
    wallNanos.record(elapsedNanos);
    uploadFrames.record(frames);
    committedBytes.record(bytes);
    committedPages = increment(committedPages);
    peakUploadFrames = Math.max(peakUploadFrames, frames);
  }

  synchronized void dropped(Drop reason) {
    switch (Objects.requireNonNull(reason, "reason")) {
      case STALE -> staleDrops = increment(staleDrops);
      case MEMORY -> memoryDrops = increment(memoryDrops);
    }
  }

  synchronized Snapshot snapshot() {
    return new Snapshot(wallNanos.snapshot(), uploadFrames.snapshot(),
        committedBytes.snapshot(), committedPages, staleDrops, memoryDrops, peakUploadFrames);
  }

  synchronized void reset() {
    wallNanos.reset();
    uploadFrames.reset();
    committedBytes.reset();
    committedPages = staleDrops = memoryDrops = peakUploadFrames = 0;
  }

  private static long increment(long n) {
    return n == Long.MAX_VALUE ? n : n + 1;
  }
}
