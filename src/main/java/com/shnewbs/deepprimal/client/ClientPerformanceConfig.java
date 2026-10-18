package com.shnewbs.deepprimal.client;

import com.shnewbs.deepprimal.core.VerticalRange;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientPerformanceConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue FAST_VIEW_RELOCATION;
    public static final ModConfigSpec.IntValue VERTICAL_RENDER_DISTANCE;
    public static final ModConfigSpec.IntValue FAR_RENDER_DISTANCE;
    static {
        var builder = new ModConfigSpec.Builder();
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
