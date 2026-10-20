package com.shnewbs.deepprimal.themes;

import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Development-only, creates a disposable flat world and captures each actual inventory. */
@EventBusSubscriber(modid = ThemeMod.ID, value = Dist.CLIENT)
public final class ThemeValidation {
    private static final Theme[] THEMES = {Theme.BASALT_COPPER, Theme.FIELD_JOURNAL, Theme.RAWHIDE_FLINT,
        Theme.RIVERSTONE_MOSS, Theme.TIMBER_IRON, Theme.CLAY_EMBER, Theme.OBSIDIAN_ASH, Theme.FROST_SLATE, Theme.VANILLA};
    private static int phase, ticks, themeIndex, total;
    private static boolean failed;
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        if (failed) return;
        var mc = Minecraft.getInstance();
        try {
            if (++total > 12000) throw new IllegalStateException("Client harness timed out at phase " + phase);
            ticks++;
            if (mc.getOverlay() != null) return;
            if (phase == 0 && ticks > 80) {
                mc.options.onboardingAccessibilityFinished();
                ThemeConfig.THEME.set(Theme.BASALT_COPPER); ThemeConfig.SPEC.save();
                mc.setScreen(new TitleScreen()); phase=1; ticks=0;
            } else if (phase == 1 && ticks > 80 && !ThemeMod.reloading() && ThemeMod.active() == Theme.BASALT_COPPER) {
                capture("00-title.png");
                mc.createWorldOpenFlows().createFreshLevel("theme-validation-" + System.currentTimeMillis(),
                    new LevelSettings("Theme validation", GameType.SURVIVAL,
                        new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT),
                    new WorldOptions(42L, false, false), WorldPresets::createFlatWorldDimensions, new TitleScreen());
                phase=2; ticks=0;
            } else if (phase == 2 && mc.player != null && mc.level != null && ticks > 120) {
                mc.setScreen(null); phase=3; ticks=0;
            } else if (phase == 3) {
                if (themeIndex == THEMES.length) {
                    Files.writeString(Path.of(mc.gameDirectory.toString(), "theme-validation-success.txt"),
                        "PASS: eight themes and Vanilla restored; all nine inventory resource origins verified; screenshots captured.\n");
                    mc.stop(); failed=true; return;
                }
                ThemeConfig.THEME.set(THEMES[themeIndex]); ThemeConfig.SPEC.save(); phase=4; ticks=0;
            } else if (phase == 4 && ticks > 30 && !ThemeMod.reloading() && ThemeMod.active() == THEMES[themeIndex]) {
                String actual = mc.getResourceManager().getResource(Identifier.withDefaultNamespace("textures/gui/container/inventory.png")).orElseThrow().sourcePackId();
                String expected = THEMES[themeIndex].custom() ? "deepprimal_themes/" + THEMES[themeIndex].id() : "vanilla";
                if (!actual.equals(expected)) throw new IllegalStateException("Expected pack " + expected + ", got " + actual);
                mc.setScreen(new InventoryScreen(mc.player)); phase=5; ticks=0;
            } else if (phase == 5 && ticks > 12) {
                capture(String.format("%02d-%s-inventory.png", themeIndex+1, THEMES[themeIndex].id()));
                mc.setScreen(new ThemeScreen(null)); phase=6; ticks=0;
            } else if (phase == 6 && ticks > 12) {
                capture(String.format("%02d-%s-selector.png", themeIndex+1, THEMES[themeIndex].id()));
                mc.setScreen(null); themeIndex++; phase=3; ticks=0;
            }
        } catch (Throwable error) {
            failed=true; error.printStackTrace();
            try { Files.writeString(Path.of(mc.gameDirectory.toString(), "theme-validation-failure.txt"), error.toString()); } catch (Exception ignored) {}
            mc.stop();
        }
    }
    private static void capture(String filename) {
        var mc=Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory, filename, mc.getMainRenderTarget(), 1, c -> System.out.println("THEME_CAPTURE: " + filename));
    }
}
