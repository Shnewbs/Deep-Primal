package com.shnewbs.deepprimal.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Production health checks for the terrain/water engine.
 * Runs validation checks to ensure engine correctness at runtime.
 */
public final class EngineHealthCheck {
    private static final List<HealthCheck> CHECKS = new ArrayList<>();
    private static volatile boolean enabled = true;

    public static void register(String name, Supplier<CheckResult> check) {
        CHECKS.add(new HealthCheck(name, check));
    }

    public static void setEnabled(boolean enabled) {
        EngineHealthCheck.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Run all registered health checks.
     * @return List of check results, empty if all passed.
     */
    public static List<CheckResult> runAll() {
        if (!enabled) return List.of();
        List<CheckResult> results = new ArrayList<>();
        for (HealthCheck check : CHECKS) {
            try {
                CheckResult result = check.check.get();
                if (!result.passed()) {
                    results.add(result);
                }
            } catch (Exception e) {
                results.add(new CheckResult(check.name, false, "Check threw exception: " + e.getMessage(), e));
                EngineMetrics.recordException();
            }
        }
        return results;
    }

    /**
     * Run health checks and throw if any fail (for startup validation).
     */
    public static void runOrThrow() {
        List<CheckResult> failures = runAll();
        if (!failures.isEmpty()) {
            StringBuilder sb = new StringBuilder("Engine health checks failed:\n");
            for (CheckResult r : failures) {
                sb.append("  ").append(r.name()).append(": ").append(r.message());
                if (r.exception() != null) {
                    sb.append(" (" ).append(r.exception().getClass().getSimpleName()).append(")");
                }
                sb.append("\n");
            }
            throw new IllegalStateException(sb.toString());
        }
    }

    /**
     * Initialize default engine health checks.
     */
    public static void initDefaultChecks() {
        // Terrain continuity check
        register("terrain_continuity", () -> {
            TerrainEngine engine = new TerrainEngine(12345L, 7, TerrainOptions.DEFAULT);
            double prev = engine.raw(0, 0);
            for (int i = 1; i < 100; i++) {
                double curr = engine.raw(i * 10, 0);
                if (Double.isNaN(curr) || Double.isInfinite(curr)) {
                    return CheckResult.fail("terrain_continuity", "NaN/Infinity at x=" + i * 10);
                }
                if (Math.abs(curr - prev) > 500) { // Sudden cliff > 500 blocks
                    return CheckResult.fail("terrain_continuity", "Discontinuity: " + prev + " -> " + curr);
                }
                prev = curr;
            }
            return CheckResult.pass("terrain_continuity");
        });

        // Water level consistency
        register("water_levels", () -> {
            TerrainEngine engine = new TerrainEngine(12345L, 7, TerrainOptions.DEFAULT);
            for (int x = -50; x <= 50; x += 10) {
                for (int z = -50; z <= 50; z += 10) {
                    var sample = engine.sample(x, z);
                    if (sample.wet()) {
                        if (sample.water() < TerrainEngine.SEA - 10 || sample.water() > TerrainEngine.SEA + 200) {
                            return CheckResult.fail("water_levels", "Water level out of range: " + sample.water() + " at " + x + "," + z);
                        }
                    }
                }
            }
            return CheckResult.pass("water_levels");
        });

        // Watershed ordering (no cycles)
        register("watershed_ordering", () -> {
            TerrainEngine engine = new TerrainEngine(12345L, 7, TerrainOptions.DEFAULT);
            try {
                engine.verify(0, 0);
            } catch (AssertionError e) {
                return CheckResult.fail("watershed_ordering", "Drainage order violation: " + e.getMessage());
            }
            return CheckResult.pass("watershed_ordering");
        });

        // Biome validity
        register("biome_validity", () -> {
            TerrainEngine engine = new TerrainEngine(12345L, 7, TerrainOptions.DEFAULT);
            for (int x = -100; x <= 100; x += 20) {
                for (int z = -100; z <= 100; z += 20) {
                    int biome = engine.biome(x, z);
                    if (biome < 0 || biome > 50) { // Valid biome range
                        return CheckResult.fail("biome_validity", "Invalid biome ID: " + biome + " at " + x + "," + z);
                    }
                }
            }
            return CheckResult.pass("biome_validity");
        });

        // Water corner packet round-trip
        register("water_packet_roundtrip", () -> {
            java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocateDirect(144 * 4);
            long now = System.currentTimeMillis();
            
            // Write a packet
            WaterCornerPacket.write(buf, 0, 0x123456789ABCDEFL, 100, 200, 300, 400, false);
            
            // Read it back
            int to0 = WaterCornerPacket.target(buf, 0, 0);
            int from0 = WaterCornerPacket.source(buf, 0, 0);
            int age0 = WaterCornerPacket.age(buf, 0, 0, now);
            
            if (to0 != 0x12 || from0 != 0x34 || age0 != 100) {
                return CheckResult.fail("water_packet_roundtrip", "Round-trip mismatch: to=" + to0 + ", from=" + from0 + ", age=" + age0);
            }
            
            return CheckResult.pass("water_packet_roundtrip");
        });

        // Memory pressure check
        register("memory_pressure", () -> {
            EngineMetrics.updateMemoryMetrics();
            long heapMb = EngineMetrics.HEAP_USED_MB.get();
            if (heapMb > 2048) { // > 2GB heap
                return CheckResult.fail("memory_pressure", "High heap usage: " + heapMb + " MB");
            }
            return CheckResult.pass("memory_pressure");
        });

        // Cache effectiveness
        register("cache_effectiveness", () -> {
            double hitRate = EngineMetrics.getCacheHitRate();
            if (hitRate < 50.0) { // Less than 50% hit rate
                return CheckResult.fail("cache_effectiveness", "Low cache hit rate: " + String.format("%.1f", hitRate) + "%");
            }
            return CheckResult.pass("cache_effectiveness");
        });
    }

    private record HealthCheck(String name, Supplier<CheckResult> check) {}

    public record CheckResult(
        String name,
        boolean passed,
        String message,
        Exception exception
    ) {
        public static CheckResult pass(String name) {
            return new CheckResult(name, true, "OK", null);
        }

        public static CheckResult fail(String name, String message) {
            return new CheckResult(name, false, message, null);
        }
    }

    private EngineHealthCheck() {}
}