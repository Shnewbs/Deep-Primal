package com.shnewbs.deepprimal.core;

import java.util.Arrays;
import java.util.Random;

/** Regression: benchmark/tick percentile diagnostics must never accept negative elapsed time. */
public final class SampleStatsDurationChecks {
    private SampleStatsDurationChecks() {}

    public static void main(String[] args) {
        int checks = 0;
        for (long[] invalid : new long[][] {
                {-1L}, {0L, -1L}, {-1L, 0L}, {1L, 2L, -1L},
                {Long.MAX_VALUE, -1L}, {Long.MIN_VALUE, 0L, 1L}}) {
            long[] copy = invalid.clone();
            try {
                SampleStats.of(invalid);
                throw new AssertionError("Negative duration accepted: " + Arrays.toString(invalid));
            } catch (IllegalArgumentException expected) {
                if (!Arrays.equals(copy, invalid)) throw new AssertionError("Mutated rejected input");
            }
            checks++;
        }
        try {
            SampleStats.of(new long[0]);
            throw new AssertionError("Empty sample accepted");
        } catch (IllegalArgumentException expected) {
            checks++;
        }
        Random random = new Random(20261101L);
        for (int trial = 0; trial < 20000; trial++) {
            int size = 1 + random.nextInt(128);
            long[] nanos = new long[size];
            for (int i = 0; i < size; i++) {
                nanos[i] = switch (random.nextInt(12)) {
                    case 0 -> 0L;
                    case 1 -> Long.MAX_VALUE;
                    case 2 -> 1L;
                    default -> random.nextLong() & Long.MAX_VALUE;
                };
            }
            long[] unchanged = nanos.clone();
            long[] sorted = nanos.clone();
            Arrays.sort(sorted);
            SampleStats s = SampleStats.of(nanos);
            if (s.count() != size || s.medianMs() != ms(sorted[rank(size, 50)])
                    || s.p95Ms() != ms(sorted[rank(size, 95)])
                    || s.p99Ms() != ms(sorted[rank(size, 99)])
                    || s.maxMs() != ms(sorted[size - 1])
                    || !Arrays.equals(nanos, unchanged)) {
                throw new AssertionError("Nonnegative parity trial " + trial);
            }
            checks += 6;
        }
        // A valid zero-duration observation remains a zero-duration observation.
        SampleStats zero = SampleStats.of(new long[] {0, 0, 0});
        if (zero.medianMs() != 0 || zero.maxMs() != 0) throw new AssertionError("Zero sample");
        checks += 2;
        System.out.println("SAMPLE_STATS_DURATION_PASS checks=" + checks
                + " randomizedArrays=20000 negativeCases=6 inputImmutable=true");
    }

    private static int rank(int count, int percent) {
        return (count * percent + 99) / 100 - 1;
    }

    private static double ms(long nanos) { return nanos / 1_000_000.0; }
}
