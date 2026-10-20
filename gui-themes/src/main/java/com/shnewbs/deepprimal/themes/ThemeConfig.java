package com.shnewbs.deepprimal.themes;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThemeConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<Theme> THEME;
    public static final ModConfigSpec.BooleanValue TITLE_BRANDING, INTEGRATION_HUD;
    static {
        var b = new ModConfigSpec.Builder();
        THEME = b.comment("Select VANILLA or one of the eight Deep Primal themes. Live reloads while the game is open.")
                .defineEnum("theme", Theme.VANILLA);
        TITLE_BRANDING = b.comment("Show Deep Primal title branding when using a custom theme.").define("titleBranding", true);
        INTEGRATION_HUD = b.comment("Show survival data and actions only when a companion mod supplies them.").define("integrationHud", true);
        SPEC = b.build();
    }
    private ThemeConfig() {}
}
