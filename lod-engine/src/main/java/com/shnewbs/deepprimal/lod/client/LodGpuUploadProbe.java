package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.core.RollingTiming;

/**
 * Bounded, renderer-thread-owned observations of accepted LOD GPU-part uploads.
 * Durations include CPU packing, buffer creation and submission, not GPU completion.
 * Unsuccessful attempts are counted separately and do not poison accepted-part percentiles.
 */
final class LodGpuUploadProbe {
  record Snapshot(RollingTiming.Snapshot cpuNanos, RollingTiming.Snapshot acceptedBytes,
      long acceptedParts, long failedParts, long overTimeBudgetParts,
      long peakPartBytes, long acceptedByteTotal) {}

  private final RollingTiming cpuNanos, acceptedBytes;
  private long acceptedParts, failedParts, overTimeBudgetParts, peakPartBytes, acceptedByteTotal;

  LodGpuUploadProbe(int samples) {
    cpuNanos = new RollingTiming(samples);
    acceptedBytes = new RollingTiming(samples);
  }

  synchronized void accepted(long elapsedNanos, long bytes, long timeBudgetNanos) {
    if (elapsedNanos < 0 || bytes < 0 || timeBudgetNanos < 0)
      throw new IllegalArgumentException("negative upload observation");
    cpuNanos.record(elapsedNanos);
    acceptedBytes.record(bytes);
    acceptedParts = increment(acceptedParts);
    if (elapsedNanos > timeBudgetNanos) overTimeBudgetParts = increment(overTimeBudgetParts);
    peakPartBytes = Math.max(peakPartBytes, bytes);
    // Long-lived diagnostic sessions must never wrap into negative counters.
    acceptedByteTotal = bytes > Long.MAX_VALUE - acceptedByteTotal
        ? Long.MAX_VALUE : acceptedByteTotal + bytes;
  }

  synchronized void failed(long elapsedNanos) {
    if (elapsedNanos < 0) throw new IllegalArgumentException("negative failed upload duration");
    failedParts = increment(failedParts);
  }

  synchronized Snapshot snapshot() {
    return new Snapshot(cpuNanos.snapshot(), acceptedBytes.snapshot(), acceptedParts,
        failedParts, overTimeBudgetParts, peakPartBytes, acceptedByteTotal);
  }

  synchronized void reset() {
    cpuNanos.reset();
    acceptedBytes.reset();
    acceptedParts = failedParts = overTimeBudgetParts = peakPartBytes = acceptedByteTotal = 0;
  }

  private static long increment(long n) {
    return n == Long.MAX_VALUE ? n : n + 1;
  }
}
