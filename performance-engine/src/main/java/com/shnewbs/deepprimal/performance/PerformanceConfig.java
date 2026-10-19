package com.shnewbs.deepprimal.performance;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Bounded runtime work and diagnostics. Saved-world profiles control terrain geometry. */
public final class PerformanceConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue MEMORY_GUARD, BOUND_SAVES;
  public static final ModConfigSpec.IntValue SAVE_WRITES;
  public static final ModConfigSpec.IntValue MAX_BENCH_RADIUS;
  public static final ModConfigSpec.IntValue REQUEST_TIMEOUT_SECONDS;
  public static final ModConfigSpec.IntValue PAUSE_ABOVE_TICK_MS;
  public static final ModConfigSpec.IntValue SAMPLE_CACHE_ENTRIES;
  public static final ModConfigSpec.BooleanValue LOG_SAMPLES;
  public static final ModConfigSpec.BooleanValue OPTIMIZE_CHUNK_SENDS;
  public static final ModConfigSpec.IntValue MAX_CHUNK_SENDS;
  public static final ModConfigSpec.BooleanValue PERSISTENT_IDLE;

  static {
    var b = new ModConfigSpec.Builder();
    MEMORY_GUARD =
        b.comment(
                "Conservatively limit effective full-detail distance by Java heap and full world"
                    + " height. Requested values stay saved; effective render and simulation"
                    + " distances are both limited.")
            .define("memoryDistanceGuard", true);
    BOUND_SAVES =
        b.comment(
                "Bound in-flight chunk snapshots, serialization and writes, including"
                    + " pause/shutdown. Applies backpressure; never skips saving.")
            .define("boundChunkSaves", true);
    SAVE_WRITES =
        b.comment(
                "Maximum in-flight chunk writes per dimension. Bulk saves may take longer to"
                    + " preserve memory headroom.")
            .defineInRange("maxConcurrentChunkWrites", 16, 1, 128);
    b.comment(
        "Runtime performance controls and bounded diagnostics. These options do not change saved"
            + " terrain.");
    MAX_BENCH_RADIUS =
        b.comment("Maximum benchmark radius in chunks; one request is in flight.")
            .defineInRange("maxBenchmarkRadius", 4, 0, 8);
    REQUEST_TIMEOUT_SECONDS =
        b.comment("Release a stuck benchmark request after this many elapsed seconds.")
            .defineInRange("requestTimeoutSeconds", 120, 5, 600);
    PAUSE_ABOVE_TICK_MS =
        b.comment("Pause issuing benchmark work after a slow tick; 0 disables this pause.")
            .defineInRange("pauseAboveTickMilliseconds", 50, 0, 1000);
    SAMPLE_CACHE_ENTRIES =
        b.comment("LRU limit for repeated diagnostic sampler queries; 0 disables caching.")
            .defineInRange("sampleCacheEntries", 1024, 0, 16384);
    LOG_SAMPLES =
        b.comment("Log each sampler query in addition to its command response.")
            .define("logSamplerQueries", false);
    OPTIMIZE_CHUNK_SENDS =
        b.comment("Use primitive nearest-first selection and bound local/remote chunk-send bursts.")
            .define("optimizeChunkSelection", true);
    MAX_CHUNK_SENDS =
        b.comment("Maximum chunks in one send batch, also respecting native client feedback.")
            .defineInRange("maxChunkSendsPerTick", 16, 1, 64);
    PERSISTENT_IDLE =
        b.comment(
                "Backport 26.3 persistent-mob idle timers: distant random walking/swimming can"
                    + " stop. Other AI remains active.")
            .define("persistentMobIdleOptimization", true);
    SPEC = b.build();
  }

  private PerformanceConfig() {}
}
