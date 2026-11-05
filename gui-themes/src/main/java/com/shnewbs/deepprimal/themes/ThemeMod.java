package com.shnewbs.deepprimal.themes;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ThemeMod.ID, dist = Dist.CLIENT)
public final class ThemeMod {
    public static final String ID = "deepprimal_themes";
    private static Theme active = Theme.VANILLA;
    private static Theme loading;
    private static boolean started, reloading;
    private static String lastError;
    private static int reloadGeneration;

    public ThemeMod(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ThemeConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new ThemeScreen(parent));
        bus.addListener(ThemePacks::register);
        bus.addListener(ThemeActions::register);
        bus.addListener((RegisterGuiLayersEvent e) -> e.registerAbove(net.neoforged.neoforge.client.gui.VanillaGuiLayers.CONTEXTUAL_INFO_BAR,
            Identifier.fromNamespaceAndPath(ID, "integration"), ThemeHud::render));
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new ThemeScreens());
        NeoForge.EVENT_BUS.register(new ThemeHud());
    }
    public static Theme active() { return active; }
    public static boolean reloading() { return reloading; }
    public static String lastError() { return lastError; }
    static Theme requestedTheme() { return loading != null ? loading : ThemeConfig.THEME.get(); }

    @SubscribeEvent public void started(ClientStartedEvent e) { active = ThemeConfig.THEME.get(); started = true; }
    @SubscribeEvent public void tick(ClientTickEvent.Post e) {
        var mc = Minecraft.getInstance();
        if(started)ThemeActions.tick();
        if(started && mc.getOverlay()==null && active.custom() && ThemeConfig.TITLE_BRANDING.get() && mc.screen!=null && mc.screen.getClass()==net.minecraft.client.gui.screens.TitleScreen.class)mc.setScreen(new ThemeTitleScreen());
        if (!started || reloading || mc.getOverlay() != null || ThemeConfig.THEME.get() == active) return;
        Theme target = ThemeConfig.THEME.get();
        reloading = true; lastError = null;
        reload(target, false);
    }
    private static void reload(Theme target, boolean recovering) {
        var mc = Minecraft.getInstance();
        int generation=++reloadGeneration;
        loading = target;
        try {
            mc.getResourcePackRepository().reload();
            mc.reloadResourcePacks().whenComplete((unused, error) -> mc.execute(() -> {
                if(generation!=reloadGeneration)return;
                if (error == null) {
                    active = target; loading = null; reloading = false;
                    if(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen)mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen());
                    else ThemeScreens.refreshOpenScreen();
                } else failed(error, recovering);
            }));
        } catch (Exception error) { failed(error, recovering); }
    }
    /** Minecraft's failed-reload future stays incomplete; cooperate with its recovery path. */
    public static void recoverFromPackFailure() {
        if (!requestedTheme().custom() && !reloading) return;
        reloadGeneration++;
        lastError = "Resource reload failed; restored Vanilla";
        active = Theme.VANILLA; loading = null; reloading = false;
        ThemeConfig.THEME.set(Theme.VANILLA); ThemeConfig.SPEC.save();
        ThemeScreens.refreshOpenScreen();
    }
    private static void failed(Throwable error, boolean recovering) {
        LogUtils.getLogger().error("Deep Primal theme reload failed", error);
        lastError = "Theme reload failed; see logs/latest.log";
        ThemeConfig.THEME.set(Theme.VANILLA); ThemeConfig.SPEC.save();
        active = Theme.VANILLA;
        if (!recovering) reload(Theme.VANILLA, true);
        else { loading = null; reloading = false; }
    }
}
