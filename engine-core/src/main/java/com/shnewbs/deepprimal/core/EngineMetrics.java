package com.shnewbs.deepprimal.core;

import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

/**
 * Production metrics collection for the terrain/water engine.
 * Thread-safe, low-overhead counters for monitoring engine health.
 */
public final class EngineMetrics {
    // Terrain generation metrics
    public static final LongAdder TERRAIN_SAMPLES = new LongAdder();
    public static final LongAdder TERRAIN_COLUMNS = new LongAdder();
    public static final LongAdder WATERSHED_QUERIES = new LongAdder();
    public static final LongAdder BIOME_QUERIES = new LongAdder();
    public static final LongAdder CAVE_CHECKS = new LongAdder();

    // Water dynamics metrics
    public static final LongAdder WATER_SAMPLES = new LongAdder();
    public static final LongAdder WATER_CORNER_PACKETS = new LongAdder();
    public static final LongAdder WATER_PACKET_WRITES = new LongAdder();
    public static final LongAdder WATER_PACKET_RETIREMENTS = new LongAdder();
    public static final LongAdder WATER_PACKET_REBASES = new LongAdder();

    // Cache metrics
    public static final LongAdder CACHE_HITS = new LongAdder();
    public static final LongAdder CACHE_MISSES = new LongAdder();
    public static final LongAdder CACHE_EVICTIONS = new LongAdder();

    // Performance timing (nanoseconds)
    public static final AtomicLong TERRAIN_GEN_TIME_NS = new AtomicLong(0);
    public static final AtomicLong WATER_SIM_TIME_NS = new AtomicLong(0);
    public static final AtomicLong CHUNK_MATERIALIZE_TIME_NS = new AtomicLong(0);

    // Error tracking
    public static final AtomicInteger EXCEPTIONS = new AtomicInteger(0);
    public static final AtomicInteger ASSERTION_FAILURES = new AtomicInteger(0);

    // Memory pressure
    public static final AtomicLong HEAP_USED_MB = new AtomicLong(0);
    public static final AtomicLong NON_HEAP_USED_MB = new AtomicLong(0);

    private static final LongSupplier HEAP_USED = () -> (
        java.lang.management.ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024)
    );
    private static final LongSupplier NON_HEAP_USED = () -> (
        java.lang.management.ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed() / (1024 * 1024)
    );

    /**
     * Record terrain sample timing.
     */
    public static void recordTerrainSample(long nanos) {
        TERRAIN_SAMPLES.increment();
        TERRAIN_GEN_TIME_NS.addAndGet(nanos);
    }

    /**
     * Record water simulation timing.
     */
    public static void recordWaterSim(long nanos) {
        WATER_SAMPLES.increment();
        WATER_SIM_TIME_NS.addAndGet(nanos);
    }

    /**
     * Record chunk materialization timing.
     */
    public static void recordChunkMaterialize(long nanos) {
        CHUNK_MATERIALIZE_TIME_NS.addAndGet(nanos);
    }

    /**
     * Update memory metrics.
     */
    public static void updateMemoryMetrics() {
        HEAP_USED_MB.set(HEAP_USED.getAsLong());
        NON_HEAP_USED_MB.set(NON_HEAP_USED.getAsLong());
    }

    /**
     * Get cache hit rate as percentage (0-100).
     */
    public static double getCacheHitRate() {
        long hits = CACHE_HITS.sum();
        long misses = CACHE_MISSES.sum();
        long total = hits + misses;
        return total > 0 ? (hits * 100.0 / total) : 100.0;
    }

    /**
     * Get average terrain generation time in microseconds.
     */
    public static double getAvgTerrainGenTimeUs() {
        long count = TERRAIN_SAMPLES.sum();
        return count > 0 ? (TERRAIN_GEN_TIME_NS.get() / 1000.0 / count) : 0.0;
    }

    /**
     * Get average water simulation time in microseconds.
     */
    public static double getAvgWaterSimTimeUs() {
        long count = WATER_SAMPLES.sum();
        return count > 0 ? (WATER_SIM_TIME_NS.get() / 1000.0 / count) : 0.0;
    }

    /**
     * Get average chunk materialization time in microseconds.
     */
    public static double getAvgChunkMaterializeTimeUs() {
        // This would need a counter for chunks materialized
        return CHUNK_MATERIALIZE_TIME_NS.get() / 1000.0;
    }

    /**
     * Record an exception.
     */
    public static void recordException() {
        EXCEPTIONS.incrementAndGet();
    }

    /**
     * Record an assertion failure.
     */
    public static void recordAssertionFailure() {
        ASSERTION_FAILURES.incrementAndGet();
    }

    /**
     * Get a snapshot of all metrics as a string.
     */
    public static String snapshot() {
        updateMemoryMetrics();
        return String.format(
            "Engine Metrics:\n" +
            "  Terrain: samples=%d, avg=%.2f us\n" +
            "  Water: samples=%d, avg=%.2f us, packets=%d, writes=%d, retirements=%d, rebases=%d\n" +
            "  Cache: hits=%d, misses=%d, hitRate=%.1f%%, evictions=%d\n" +
            "  Chunk: materializeTime=%.2f us\n" +
            "  Errors: exceptions=%d, assertions=%d\n" +
            "  Memory: heap=%d MB, non-heap=%d MB",
            TERRAIN_SAMPLES.sum(), getAvgTerrainGenTimeUs(),
            WATER_SAMPLES.sum(), getAvgWaterSimTimeUs(),
            WATER_CORNER_PACKETS.sum(), WATER_PACKET_WRITES.sum(),
            WATER_PACKET_RETIREMENTS.sum(), WATER_PACKET_REBASES.sum(),
            CACHE_HITS.sum(), CACHE_MISSES.sum(), getCacheHitRate(),
            CACHE_EVICTIONS.sum(),
            getAvgChunkMaterializeTimeUs(),
            EXCEPTIONS.get(), ASSERTION_FAILURES.get(),
            HEAP_USED_MB.get(), NON_HEAP_USED_MB.get()
        );
    }

    /**
     * Reset all metrics (for testing or periodic reporting).
     */
    public static void reset() {
        TERRAIN_SAMPLES.reset();
        TERRAIN_COLUMNS.reset();
        WATERSHED_QUERIES.reset();
        BIOME_QUERIES.reset();
        CAVE_CHECKS.reset();
        WATER_SAMPLES.reset();
        WATER_CORNER_PACKETS.reset();
        WATER_PACKET_WRITES.reset();
        WATER_PACKET_RETIREMENTS.reset();
        WATER_PACKET_REBASES.reset();
        CACHE_HITS.reset();
        CACHE_MISSES.reset();
        CACHE_EVICTIONS.reset();
        TERRAIN_GEN_TIME_NS.set(0);
        WATER_SIM_TIME_NS.set(0);
        CHUNK_MATERIALIZE_TIME_NS.set(0);
        EXCEPTIONS.set(0);
        ASSERTION_FAILURES.set(0);
    }

    private EngineMetrics() {}
}