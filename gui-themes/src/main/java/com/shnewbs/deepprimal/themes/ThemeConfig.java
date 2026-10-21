package com.shnewbs.deepprimal.themes;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThemeConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<Theme> THEME;
    public static final ModConfigSpec.BooleanValue TITLE_BRANDING, INTEGRATION_HUD;
    public static final ModConfigSpec.ConfigValue<String> LAST_WORLD;
    static {
        var b = new ModConfigSpec.Builder();
        THEME = b.comment("Select VANILLA or one of the eight Deep Primal themes. Live reloads while the game is open.")
                .defineEnum("theme", Theme.VANILLA);
        TITLE_BRANDING = b.comment("Show Deep Primal title branding when using a custom theme.").define("titleBranding", true);
        INTEGRATION_HUD = b.comment("Show the themed health, hunger, progression and action HUD. Thirst and Hearth require a companion provider.").define("integrationHud", true);
        LAST_WORLD = b.comment("Last local save used by the Continue button.").define("lastWorld", "");
        SPEC = b.build();
    }
    private ThemeConfig() {}
}
