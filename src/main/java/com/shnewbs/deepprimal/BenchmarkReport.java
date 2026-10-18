package com.shnewbs.deepprimal;

import com.shnewbs.deepprimal.core.SampleStats;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

/** Immutable report snapshot: the writer thread never reads a live world or player. */
public record BenchmarkReport(int[] xs, int[] zs, long[] requestNanos, double elapsedSeconds,
        long heapEndBytes, long peakObservedHeapBytes, long seed, String dimension, int minY, int maxY,
        SampleStats ticks, long gcCount, long gcMillis, String version) {
    public BenchmarkReport {
        if (xs.length != zs.length || xs.length != requestNanos.length)
            throw new IllegalArgumentException("Mismatched benchmark arrays");
        xs = xs.clone(); zs = zs.clone(); requestNanos = requestNanos.clone();
    }
    @Override public int[] xs() { return xs.clone(); }
    @Override public int[] zs() { return zs.clone(); }
    @Override public long[] requestNanos() { return requestNanos.clone(); }

    public String summary() {
        SampleStats s = SampleStats.of(requestNanos);
        return String.format(Locale.ROOT,
            "BENCH COMPLETE count=%d median=%.2fms p95=%.2fms p99=%.2fms max=%.2fms elapsed=%.2fs throughput=%.2f/s heapEndMiB=%d peakObservedHeapMiB=%d tickP95=%.2fms tickMax=%.2fms gcCount=%d gcMillis=%d seed=%d dimension=%s",
            s.count(), s.medianMs(), s.p95Ms(), s.p99Ms(), s.maxMs(), elapsedSeconds,
            s.count() / elapsedSeconds, heapEndBytes / 1048576, peakObservedHeapBytes / 1048576,
            ticks.p95Ms(), ticks.maxMs(), gcCount, gcMillis, seed, dimension);
    }
    public void write(Path directory) throws Exception {
        Files.createDirectories(directory);
        String stamp = System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
        StringBuilder csv = new StringBuilder("chunk_x,chunk_z,request_to_ready_ms\n");
        for (int i = 0; i < requestNanos.length; i++) csv.append(xs[i]).append(',').append(zs[i])
            .append(',').append(requestNanos[i] / 1e6).append('\n');
        Files.writeString(directory.resolve(stamp + ".csv"), csv);
        Files.writeString(directory.resolve(stamp + ".txt"), summary() + "\n"
            + "Request latency includes tick polling, queueing, neighbor generation and I/O; not pure generator CPU.\n"
            + "Tick timings measure event Pre-to-Post; heap peak is observed at ticks, not an exact allocation peak.\n"
            + "Java=" + System.getProperty("java.version") + " processors=" + Runtime.getRuntime().availableProcessors()
            + " minY=" + minY + " maxY=" + maxY + " concurrency=1 version=" + version + "\n");
    }
}
