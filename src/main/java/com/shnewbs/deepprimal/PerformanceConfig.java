package com.shnewbs.deepprimal;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Runtime diagnostics only. Saved terrain geometry is controlled by the world preset. */
public final class PerformanceConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MAX_BENCH_RADIUS;
    public static final ModConfigSpec.IntValue REQUEST_TIMEOUT_SECONDS;
    public static final ModConfigSpec.IntValue PAUSE_ABOVE_TICK_MS;
    public static final ModConfigSpec.IntValue SAMPLE_CACHE_ENTRIES;
    public static final ModConfigSpec.BooleanValue LOG_SAMPLES;
    static {
        var b = new ModConfigSpec.Builder();
        b.comment("Bounded operator diagnostics. These options do not change saved terrain.");
        MAX_BENCH_RADIUS = b.comment("Maximum benchmark radius in chunks; one request is in flight.")
            .defineInRange("maxBenchmarkRadius", 4, 0, 8);
        REQUEST_TIMEOUT_SECONDS = b.comment("Release a stuck benchmark request after this many elapsed seconds.")
            .defineInRange("requestTimeoutSeconds", 120, 5, 600);
        PAUSE_ABOVE_TICK_MS = b.comment("Pause issuing benchmark work after a slow tick; 0 disables this pause.")
            .defineInRange("pauseAboveTickMilliseconds", 50, 0, 1000);
        SAMPLE_CACHE_ENTRIES = b.comment("LRU limit for repeated diagnostic sampler queries; 0 disables caching.")
            .defineInRange("sampleCacheEntries", 1024, 0, 16384);
        LOG_SAMPLES = b.comment("Log each sampler query in addition to its command response.")
            .define("logSamplerQueries", false);
        SPEC = b.build();
    }
    private PerformanceConfig() {}
}
