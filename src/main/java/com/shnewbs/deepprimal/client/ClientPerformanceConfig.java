package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.core.VerticalRange;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientPerformanceConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue UPLOAD_LEVELS, UPLOAD_KIB, UPLOAD_MICROS;
    public static final ModConfigSpec.IntValue ENTITY_LIMIT, BLOCK_ENTITY_LIMIT, PARTICLE_LIMIT, PARTICLE_SPAWNS;
    public static final ModConfigSpec.IntValue UNFOCUSED_FPS, MINIMIZED_FPS;
    public static final ModConfigSpec.BooleanValue PAUSE_BACKGROUND, BATCHED_DRAWS;
    public static final ModConfigSpec.IntValue TARGET_FPS, HORIZON_MEMORY_MIB;
    public static final ModConfigSpec.BooleanValue ADAPTIVE_WORK;
    public static final ModConfigSpec.BooleanValue FAST_VIEW_RELOCATION;
    public static final ModConfigSpec.IntValue VERTICAL_RENDER_DISTANCE;
    public static final ModConfigSpec.IntValue FAR_RENDER_DISTANCE;
    static {
        var builder = new ModConfigSpec.Builder();
        BATCHED_DRAWS = builder.comment("Submit indexed horizon draws through one shared pipeline setup; does not require GPU MultiDrawIndirect.").define("batchHorizonSubmission", true);
        UPLOAD_LEVELS = builder.comment("Maximum cached terrain detail levels uploaded per frame.").defineInRange("meshUploadsPerFrame", 2, 1, 8);
        UPLOAD_KIB = builder.comment("Terrain upload payload budget per frame in KiB; transition closure is separate.").defineInRange("meshUploadKiBPerFrame", 256, 128, 4096);
        UPLOAD_MICROS = builder.comment("Stop starting additional terrain uploads after this CPU-time budget; one upload cannot be preempted.").defineInRange("meshUploadMicrosPerFrame", 1000, 100, 4000);
        ENTITY_LIMIT = builder.comment("Optional visual entity cap in blocks, 0 uses native limits. Players, glowing and leashed entities are exempt.").defineInRange("entityRenderLimit", 0, 0, 512);
        BLOCK_ENTITY_LIMIT = builder.comment("Optional visual block-entity cap in blocks, 0 uses native limits. Global renderers are exempt.").defineInRange("blockEntityRenderLimit", 0, 0, 256);
        PARTICLE_LIMIT = builder.comment("Optional particle admission distance in blocks, 0 uses native limits.").defineInRange("particleRenderLimit", 0, 0, 256);
        PARTICLE_SPAWNS = builder.comment("Optional visual particle admissions per client tick, 0 uses native limits.").defineInRange("particleSpawnsPerTick", 0, 0, 8192);
        UNFOCUSED_FPS = builder.comment("Unfocused FPS ceiling, 0 leaves native behavior; never raises a lower native cap.").defineInRange("unfocusedFps", 20, 0, 120);
        MINIMIZED_FPS = builder.comment("Minimized FPS ceiling, 0 leaves native behavior.").defineInRange("minimizedFps", 5, 0, 60);
        PAUSE_BACKGROUND = builder.comment("Pause optional horizon requests/uploads while the game window is unfocused.").define("pauseHorizonInBackground", true);
        ADAPTIVE_WORK = builder.comment("Back off optional horizon requests under frame or heap pressure.").define("adaptiveHorizonWork", true);
        TARGET_FPS = builder.comment("Optional-work pacing target; simulation distance stays unchanged.").defineInRange("targetFps", 60, 15, 240);
        HORIZON_MEMORY_MIB = builder.comment("Retained horizon vertex and index payload budget in MiB.").defineInRange("horizonMemoryMiB", 32, 1, 256);
        FAST_VIEW_RELOCATION = builder.comment("Move only changed render columns when the camera crosses a section boundary.")
            .define("optimizeViewRelocation", true);
        VERTICAL_RENDER_DISTANCE = builder.comment("Vertical render radius above/below the camera in 16-block sections; 0 renders the full world height.",
            "Limits render sections and meshes, not full-height chunk loading, networking, or server simulation.")
            .defineInRange("verticalRenderDistance", 0, 0, VerticalRange.MAX_RADIUS);
        FAR_RENDER_DISTANCE = builder.comment("Built-in macro-terrain horizon radius in chunks, 0 disables; no full chunks or simulation are requested.")
            .defineInRange("farRenderDistance", 192, 0, 256);
        SPEC = builder.build();
    }
    private ClientPerformanceConfig() {}
}
