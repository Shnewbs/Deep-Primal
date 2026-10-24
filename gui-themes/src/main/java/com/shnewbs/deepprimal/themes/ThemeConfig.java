package com.shnewbs.deepprimal.themes;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThemeConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<Theme> THEME;
    public static final ModConfigSpec.BooleanValue TITLE_BRANDING, INTEGRATION_HUD;
    public static final ModConfigSpec.DoubleValue DAY_X, DAY_Y;
    static {
        var b = new ModConfigSpec.Builder();
        THEME = b.comment("Select VANILLA or one of the eight Deep Primal themes. Live reloads while the game is open.")
                .defineEnum("theme", Theme.VANILLA);
        TITLE_BRANDING = b.comment("Show Deep Primal title branding when using a custom theme.").define("titleBranding", true);
        INTEGRATION_HUD = b.comment("Show the themed health, hunger, progression and action HUD. Thirst and Hearth require a companion provider.").define("integrationHud", true);
        b.push("hudLayout");
        DAY_X = b.comment("Day/weather horizontal position within the available screen area: 0 = left, 1 = right.").defineInRange("dayX", 1.0, 0.0, 1.0);
        DAY_Y = b.comment("Day/weather vertical position within the available screen area: 0 = top, 1 = bottom.").defineInRange("dayY", 0.0, 0.0, 1.0);
        b.pop();
        SPEC = b.build();
    }
    private ThemeConfig() {}
}
