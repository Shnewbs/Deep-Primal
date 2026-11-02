package com.shnewbs.deepprimal.core;

import java.util.Random;

/** Regression for malformed/overflowing timing and cache metrics. */
public final class EngineMetricsTimingChecks {
    private static long checks;
    private EngineMetricsTimingChecks() {}
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message + " at check " + checks);
    }
    public static void main(String[] args) {
        EngineMetrics.reset();
        check(EngineMetrics.getCacheHitRate() == 100.0, "empty cache baseline");
        check(EngineMetrics.getAvgTerrainGenTimeUs() == 0, "empty terrain baseline");
        check(EngineMetrics.getAvgWaterSimTimeUs() == 0, "empty water baseline");
        check(EngineMetrics.getAvgChunkMaterializeTimeUs() == 0, "empty chunk baseline");
        for (int channel = 0; channel < 3; channel++) {
            EngineMetrics.reset();
            record(channel, -1);
            record(channel, Long.MIN_VALUE);
            check(count(channel) == 0 && nanos(channel) == 0, "negative duration admitted");
            record(channel, 0);
            check(count(channel) == 1 && nanos(channel) == 0, "zero duration lost");
            record(channel, Long.MAX_VALUE - 10);
            check(count(channel) == 2 && nanos(channel) == Long.MAX_VALUE - 10,
                "near maximum duration changed");
            record(channel, 100);
            check(count(channel) == 3 && nanos(channel) == Long.MAX_VALUE,
                "positive duration overflowed");
            record(channel, Long.MAX_VALUE);
            check(count(channel) == 4 && nanos(channel) == Long.MAX_VALUE,
                "saturation did not persist");
            check(Double.isFinite(avg(channel)) && avg(channel) >= 0,
                "invalid average after overflow");
        }
        EngineMetrics.reset();
        Random random = new Random(20261102L);
        long[] totals = new long[3];
        long[] counts = new long[3];
        for (int i = 0; i < 300_000; i++) {
            int channel = i % 3;
            long duration = random.nextInt(1_000_000) - 10L;
            record(channel, duration);
            if (duration >= 0) {
                counts[channel]++;
                totals[channel] += duration;
            }
            check(count(channel) == counts[channel], "ordinary sample count parity");
            check(nanos(channel) == totals[channel], "ordinary elapsed parity");
        }
        for (int channel = 0; channel < 3; channel++) {
            check(Math.abs(avg(channel) - totals[channel] / 1000.0 / counts[channel]) < 1e-9,
                "ordinary mean parity");
        }
        EngineMetrics.reset();
        EngineMetrics.CACHE_HITS.add(Long.MAX_VALUE);
        EngineMetrics.CACHE_MISSES.add(Long.MAX_VALUE);
        check(EngineMetrics.getCacheHitRate() == 50.0, "cache total overflow");
        EngineMetrics.reset();
        EngineMetrics.CACHE_HITS.add(9);
        EngineMetrics.CACHE_MISSES.add(1);
        check(EngineMetrics.getCacheHitRate() == 90.0, "ordinary cache hit ratio");
        EngineMetrics.reset();
        check(EngineMetrics.TERRAIN_SAMPLES.sum() == 0
            && EngineMetrics.WATER_SAMPLES.sum() == 0
            && EngineMetrics.CHUNKS_MATERIALIZED.sum() == 0,
            "reset did not restore sample counters");
        System.out.println("ENGINE_METRICS_TIMING_PASS checks=" + checks
            + " ordinarySamples=300000 overflowChannels=3 invalidDropped=true cacheRatioSafe=true");
    }
    private static void record(int channel, long nanos) {
        switch (channel) {
            case 0 -> EngineMetrics.recordTerrainSample(nanos);
            case 1 -> EngineMetrics.recordWaterSim(nanos);
            case 2 -> EngineMetrics.recordChunkMaterialize(nanos);
            default -> throw new AssertionError("channel");
        }
    }
    private static long count(int channel) {
        return switch (channel) {
            case 0 -> EngineMetrics.TERRAIN_SAMPLES.sum();
            case 1 -> EngineMetrics.WATER_SAMPLES.sum();
            case 2 -> EngineMetrics.CHUNKS_MATERIALIZED.sum();
            default -> throw new AssertionError("channel");
        };
    }
    private static long nanos(int channel) {
        return switch (channel) {
            case 0 -> EngineMetrics.TERRAIN_GEN_TIME_NS.get();
            case 1 -> EngineMetrics.WATER_SIM_TIME_NS.get();
            case 2 -> EngineMetrics.CHUNK_MATERIALIZE_TIME_NS.get();
            default -> throw new AssertionError("channel");
        };
    }
    private static double avg(int channel) {
        return switch (channel) {
            case 0 -> EngineMetrics.getAvgTerrainGenTimeUs();
            case 1 -> EngineMetrics.getAvgWaterSimTimeUs();
            case 2 -> EngineMetrics.getAvgChunkMaterializeTimeUs();
            default -> throw new AssertionError("channel");
        };
    }
}
