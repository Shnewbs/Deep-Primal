package com.shnewbs.deepprimal.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientPerformanceConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue FAST_VIEW_RELOCATION;
    static {
        var builder = new ModConfigSpec.Builder();
        FAST_VIEW_RELOCATION = builder.comment("Move only changed render columns when the camera crosses a section boundary.")
            .define("optimizeViewRelocation", true);
        SPEC = builder.build();
    }
    private ClientPerformanceConfig() {}
}
