package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.BenchmarkReport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/** Invalid benchmark telemetry must never leave an orphan CSV or report directory. */
public final class BenchmarkReportWriteChecks {
    private BenchmarkReportWriteChecks() {}

    public static void main(String[] args) throws Exception {
        Path parent = Files.createTempDirectory("deepprimal-benchmark-write-");
        Path output = parent.resolve("reports");
        int checks = 0;
        try {
            for (long[] invalid : new long[][] {{-1}, {1, -1}, {-1, 1}, {}}) {
                BenchmarkReport report = report(invalid);
                try {
                    report.write(output);
                    throw new AssertionError("Invalid report was written");
                } catch (IllegalArgumentException expected) {
                    // Reject before creating a directory or a partial CSV.
                }
                if (Files.exists(output)) throw new AssertionError("Invalid report created output directory");
                checks += 2;
            }
            report(new long[] {0, 1_000_000, Long.MAX_VALUE}).write(output);
            if (!Files.isDirectory(output)) throw new AssertionError("Valid report directory missing");
            checks++;
            try (Stream<Path> files = Files.list(output)) {
                var names = files.map(p -> p.getFileName().toString()).toList();
                if (names.size() != 2 || names.stream().filter(s -> s.endsWith(".csv")).count() != 1
                        || names.stream().filter(s -> s.endsWith(".txt")).count() != 1) {
                    throw new AssertionError("Expected one CSV and one text report: " + names);
                }
                checks += 3;
            }
            System.out.println("BENCHMARK_REPORT_WRITE_PASS checks=" + checks
                    + " invalidReports=4 validReports=1 noOrphanFiles=true");
        } finally {
            if (Files.isDirectory(output)) {
                try (Stream<Path> files = Files.list(output)) {
                    for (Path path : files.toList()) Files.delete(path);
                }
                Files.delete(output);
            }
            Files.delete(parent);
        }
    }

    private static BenchmarkReport report(long[] nanos) {
        int[] xs = new int[nanos.length];
        int[] zs = new int[nanos.length];
        return new BenchmarkReport(xs, zs, nanos, 1.0, 0, 0, 1, "test", 0, 256,
                new SampleStats(0, 0, 0, 0, 0), 0, 0, "test");
    }
}
