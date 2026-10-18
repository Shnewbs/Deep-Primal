package com.shnewbs.deepprimal.diagnostics;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DiagnosticsConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue PORT;
    static {
        var b = new ModConfigSpec.Builder();
        ENABLED = b.comment("Read-only performance dashboard bound to 127.0.0.1 only. Restart to change.")
            .define("localhostDashboard", true);
        PORT = b.comment("Preferred localhost port; uses the next available port when another instance owns it.")
            .defineInRange("dashboardPort", 8787, 1024, 65515);
        SPEC = b.build();
    }
    private DiagnosticsConfig() {}
}
