package com.shnewbs.deepprimal.core;

import java.util.Arrays;

/** Platform-neutral calculations; timings are explicitly request-to-ready, not CPU time. */
public record SampleStats(int count, double medianMs, double p95Ms, double p99Ms, double maxMs) {
    public static SampleStats of(long[] nanos) {
        if (nanos.length == 0) throw new IllegalArgumentException("No samples");
        // Negative elapsed time is invalid telemetry, not a fast completion.
        // Reject the entire sample before publishing misleading percentiles.
        for (long nanosSample : nanos) {
            if (nanosSample < 0) throw new IllegalArgumentException("Negative duration");
        }
        long[] sorted = nanos.clone();
        Arrays.sort(sorted);
        return new SampleStats(sorted.length, percentile(sorted, .5), percentile(sorted, .95),
            percentile(sorted, .99), sorted[sorted.length - 1] / 1_000_000.0);
    }

    private static double percentile(long[] sorted, double fraction) {
        return sorted[Math.max(0, (int) Math.ceil(sorted.length * fraction) - 1)] / 1_000_000.0;
    }
}
