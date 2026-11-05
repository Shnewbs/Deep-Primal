package com.shnewbs.deepprimal.core;

import java.util.Arrays;

/** Platform-neutral calculations; timings are explicitly request-to-ready, not CPU time. */
public record SampleStats(int count, double medianMs, double p95Ms, double p99Ms, double maxMs) {
    public static SampleStats of(long[] nanos) {
        return of(nanos, nanos.length);
    }

    /** Snapshot only the initialized prefix of a tick ring, with one defensive copy. */
    static SampleStats of(long[] nanos, int count) {
        if (count == 0) throw new IllegalArgumentException("No samples");
        if (count < 0 || count > nanos.length) throw new IllegalArgumentException("Invalid sample count");
        // TickWindow's initialized prefix is contiguous until its first wrap;
        // once full, all slots are valid regardless of their chronological order.
        long[] sorted = Arrays.copyOf(nanos, count);
        // Validate the copied values so callers cannot race validation and sorting.
        for (long nanosSample : sorted) {
            if (nanosSample < 0) throw new IllegalArgumentException("Negative duration");
        }
        Arrays.sort(sorted);
        return new SampleStats(sorted.length, percentile(sorted, .5), percentile(sorted, .95),
            percentile(sorted, .99), sorted[sorted.length - 1] / 1_000_000.0);
    }

    private static double percentile(long[] sorted, double fraction) {
        return sorted[Math.max(0, (int) Math.ceil(sorted.length * fraction) - 1)] / 1_000_000.0;
    }
}
