package com.shnewbs.deepprimal.core;

import com.shnewbs.deepprimal.diagnostics.LocalDashboard;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Production validation and monitoring for engine-core.
 * Runs periodic health checks and reports metrics.
 */
public final class CoreValidation {
    private static final Logger LOGGER = Logger.getLogger(CoreValidation.class.getName());
    private static ScheduledExecutorService scheduler;
    private static volatile boolean running = false;

    /**
     * Start periodic validation and metrics reporting.
     * @param intervalSeconds Interval between validation runs.
     */
    public static void startPeriodicValidation(int intervalSeconds) {
        if (running) return;
        running = true;

        // Run initial validation
        EngineHealthCheck.initDefaultChecks();
        EngineHealthCheck.runOrThrow();
        LOGGER.info("Engine core validation passed at startup");

        // Schedule periodic checks
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "DeepPrimal-CoreValidation");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                runPeriodicValidation();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Periodic validation failed", e);
                EngineMetrics.recordException();
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);

        // Schedule metrics reporting
        scheduler.scheduleAtFixedRate(() -> {
            try {
                reportMetrics();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Metrics reporting failed", e);
            }
        }, 60, 60, TimeUnit.SECONDS); // Every minute
    }

    /**
     * Stop periodic validation.
     */
    public static void stop() {
        running = false;
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private static void runPeriodicValidation() {
        List<EngineHealthCheck.CheckResult> results = EngineHealthCheck.runAll();
        if (!results.isEmpty()) {
            for (EngineHealthCheck.CheckResult r : results) {
                LOGGER.warning("Health check failed: " + r.name() + " - " + r.message());
                LocalDashboard.counter("engine.health_check.failed", 1,
                    java.util.Map.of("check", r.name()));
            }
        } else {
            LocalDashboard.counter("engine.health_check.passed", 1, java.util.Map.of());
        }
    }

    private static void reportMetrics() {
        EngineMetrics.updateMemoryMetrics();

        // Report to local dashboard
        LocalDashboard.gauge("engine.terrain.avg_us", EngineMetrics.getAvgTerrainGenTimeUs());
        LocalDashboard.gauge("engine.water.avg_us", EngineMetrics.getAvgWaterSimTimeUs());
        LocalDashboard.gauge("engine.cache.hit_rate", EngineMetrics.getCacheHitRate());
        LocalDashboard.gauge("engine.memory.heap_mb", EngineMetrics.HEAP_USED_MB.get());
        LocalDashboard.gauge("engine.memory.nonheap_mb", EngineMetrics.NON_HEAP_USED_MB.get());
        LocalDashboard.counter("engine.terrain.samples", EngineMetrics.TERRAIN_SAMPLES.sumThenReset());
        LocalDashboard.counter("engine.water.samples", EngineMetrics.WATER_SAMPLES.sumThenReset());
        LocalDashboard.counter("engine.water.packets", EngineMetrics.WATER_CORNER_PACKETS.sumThenReset());
        LocalDashboard.counter("engine.exceptions", EngineMetrics.EXCEPTIONS.getAndSet(0));

        // Log summary at INFO level occasionally
        if (System.currentTimeMillis() % 300000 < 60000) { // Every 5 minutes
            LOGGER.info(EngineMetrics.snapshot());
        }
    }

    /**
     * Validate a specific world seed for terrain generation correctness.
     * Useful for regression testing.
     */
    public static ValidationReport validateSeed(long seed) {
        TerrainEngine engine = new TerrainEngine(seed, 7, TerrainOptions.DEFAULT);
        StringBuilder issues = new StringBuilder();
        int issueCount = 0;

        // Check terrain range
        for (int x = -1000; x <= 1000; x += 100) {
            for (int z = -1000; z <= 1000; z += 100) {
                double h = engine.raw(x, z);
                if (Double.isNaN(h) || Double.isInfinite(h)) {
                    issues.append(String.format("NaN/Inf at %d,%d: %f\n", x, z, h));
                    issueCount++;
                }
                if (h < -500 || h > 500) {
                    issues.append(String.format("Height out of range at %d,%d: %f\n", x, z, h));
                    issueCount++;
                }
            }
        }

        // Check water consistency
        for (int x = -500; x <= 500; x += 50) {
            for (int z = -500; z <= 500; z += 50) {
                var sample = engine.sample(x, z);
                if (sample.wet()) {
                    if (sample.water() < TerrainEngine.SEA - 20 || sample.water() > TerrainEngine.SEA + 256) {
                        issues.append(String.format("Water level anomaly at %d,%d: %f\n", x, z, sample.water()));
                        issueCount++;
                    }
                }
            }
        }

        // Check biome validity
        for (int x = -200; x <= 200; x += 20) {
            for (int z = -200; z <= 200; z += 20) {
                int biome = engine.biome(x, z);
                if (biome < 0 || biome > 50) {
                    issues.append(String.format("Invalid biome at %d,%d: %d\n", x, z, biome));
                    issueCount++;
                }
            }
        }

        // Check watershed ordering
        try {
            engine.verify(0, 0);
        } catch (AssertionError e) {
            issues.append("Watershed ordering violation: ").append(e.getMessage()).append("\n");
            issueCount++;
        }

        return new ValidationReport(seed, issueCount == 0, issues.toString(), issueCount);
    }

    public record ValidationReport(
        long seed,
        boolean passed,
        String issues,
        int issueCount
    ) {}

    private CoreValidation() {}
}