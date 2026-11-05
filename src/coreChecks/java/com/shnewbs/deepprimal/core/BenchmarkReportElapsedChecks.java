package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.BenchmarkReport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/** Elapsed telemetry must not produce negative/infinite throughput or orphan files. */
public final class BenchmarkReportElapsedChecks {
  private BenchmarkReportElapsedChecks() {}
  private static int checks;

  private static BenchmarkReport report(double elapsed) {
    return new BenchmarkReport(new int[]{1, 2}, new int[]{3, 4},
        new long[]{0L, 1_000_000L}, elapsed, 0L, 0L, 1L, "test", 0, 256,
        new SampleStats(1, 1, 1, 1, 1), 0L, 0L, "test");
  }

  public static void main(String[] args) throws Exception {
    Path parent = Files.createTempDirectory("deepprimal-bench-elapsed-");
    try {
      for (double elapsed : new double[]{0.0, -0.0, -1.0, Double.NaN,
          Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MIN_VALUE}) {
        BenchmarkReport report = report(elapsed);
        try {
          report.summary();
          throw new AssertionError("Invalid elapsed accepted by summary: " + elapsed);
        } catch (IllegalArgumentException expected) { checks++; }
        Path output = parent.resolve("invalid-" + checks);
        try {
          report.write(output);
          throw new AssertionError("Invalid elapsed written: " + elapsed);
        } catch (IllegalArgumentException expected) { checks++; }
        if (Files.exists(output)) throw new AssertionError("Orphan directory: " + output);
        checks++;
      }
      for (double elapsed : new double[]{0.000001, 1.0, 120.0, Double.MAX_VALUE}) {
        String summary = report(elapsed).summary();
        if (summary.contains("Infinity") || summary.contains("NaN") || summary.contains("throughput=-"))
          throw new AssertionError("Invalid throughput in summary: " + summary);
        checks++;
      }
      Path valid = parent.resolve("valid");
      report(1.0).write(valid);
      try (Stream<Path> files = Files.list(valid)) {
        if (files.count() != 2) throw new AssertionError("Valid report files missing");
        checks++;
      }
      try (Stream<Path> files = Files.list(valid)) {
        for (Path file : files.toList()) Files.delete(file);
      }
      Files.delete(valid);
      System.out.println("BENCHMARK_ELAPSED_PASS checks=" + checks
          + " invalidElapsedRejected=7 noOrphans=true validReports=true");
    } finally {
      Files.delete(parent);
    }
  }
}
