package com.shnewbs.deepprimal.lod.client;

import com.shnewbs.deepprimal.core.RollingTiming;

/**
 * Bounded CPU-only timing of LOD submission after transforms were written.
 * Includes failed render passes rather than silently dropping their long tails;
 * returned API calls/records are not GPU-executed draws or indirect dispatches.
 * No clock read or allocation is required while telemetry is disabled.
 */
final class LodSubmissionProbe {
  record Snapshot(RollingTiming.Snapshot transformCpuNanos,
      RollingTiming.Snapshot drawApiCpuNanos, RollingTiming.Snapshot totalCpuNanos,
      long completedPasses, long failedPasses, long returnedRecords,
      long returnedApiCalls) {}

  private final RollingTiming transformCpuNanos, drawApiCpuNanos, totalCpuNanos;
  private long completedPasses, failedPasses, returnedRecords, returnedApiCalls;

  LodSubmissionProbe(int samples) {
    transformCpuNanos = new RollingTiming(samples);
    drawApiCpuNanos = new RollingTiming(samples);
    totalCpuNanos = new RollingTiming(samples);
  }

  /**
   * Called in the draw-pass finally block. The transform stage has completed;
   * an exception while creating/writing transforms is outside this observation.
   * Draw-api duration includes Java call preparation/driver submission and
   * render-pass close, never a GPU fence or measured GPU execution interval.
   */
  synchronized void finished(long transformNanos, long drawNanos, long totalNanos,
      int returnedDrawRecords, int returnedCalls, boolean completed) {
    if (transformNanos < 0 || drawNanos < 0 || totalNanos < 0
        || transformNanos > totalNanos || drawNanos > totalNanos - transformNanos
        || returnedDrawRecords < 0 || returnedCalls < 0
        || (returnedDrawRecords > 0 && returnedCalls == 0)
        || returnedCalls > returnedDrawRecords)
      throw new IllegalArgumentException("invalid LOD submission observation");
    transformCpuNanos.record(transformNanos);
    drawApiCpuNanos.record(drawNanos);
    totalCpuNanos.record(totalNanos);
    if (completed) completedPasses = increment(completedPasses);
    else failedPasses = increment(failedPasses);
    returnedRecords = saturatedAdd(returnedRecords, returnedDrawRecords);
    returnedApiCalls = saturatedAdd(returnedApiCalls, returnedCalls);
  }

  synchronized Snapshot snapshot() {
    return new Snapshot(transformCpuNanos.snapshot(), drawApiCpuNanos.snapshot(),
        totalCpuNanos.snapshot(), completedPasses, failedPasses, returnedRecords,
        returnedApiCalls);
  }

  synchronized void reset() {
    transformCpuNanos.reset();
    drawApiCpuNanos.reset();
    totalCpuNanos.reset();
    completedPasses = failedPasses = returnedRecords = returnedApiCalls = 0;
  }

  private static long increment(long value) {
    return value == Long.MAX_VALUE ? value : value + 1;
  }

  private static long saturatedAdd(long a, long b) {
    return b > Long.MAX_VALUE - a ? Long.MAX_VALUE : a + b;
  }
}
