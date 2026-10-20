package com.shnewbs.deepprimal.themes;

import com.shnewbs.deepprimal.themes.api.ThemeIntegration;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

final class ThemeHud {
    static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        var mc = Minecraft.getInstance();
        if (!ThemeMod.active().custom() || !ThemeConfig.INTEGRATION_HUD.get() || mc.player == null || mc.player.isSpectator() || mc.options.hideGui) return;
        ThemeIntegration.provider().survival().ifPresent(state -> {
            var theme = ThemeMod.active();
            int x = g.guiWidth() / 2 - 91, y = g.guiHeight() - 79;
            g.fill(x, y, x + 182, y + 18, theme.well);
            g.text(mc.font, "Thirst " + Math.round(state.thirst() * 100) + "%", x + 4, y + 3, theme.text);
            g.text(mc.font, "Level " + state.level(), x + 112, y + 3, theme.text);
            g.fill(x, y + 15, x + Math.round(182 * state.levelProgress()), y + 18, theme.accent);
        });
    }
}
