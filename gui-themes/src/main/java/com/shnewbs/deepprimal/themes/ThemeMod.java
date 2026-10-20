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
import net.neoforged.neoforge.client.event.ClientStartedEvent;
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

    public ThemeMod(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ThemeConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new ThemeScreen(parent));
        bus.addListener(ThemePacks::register);
        bus.addListener((RegisterGuiLayersEvent e) -> e.registerAboveAll(Identifier.fromNamespaceAndPath(ID, "integration"), ThemeHud::render));
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new ThemeScreens());
    }
    public static Theme active() { return active; }
    public static boolean reloading() { return reloading; }
    public static String lastError() { return lastError; }
    static Theme requestedTheme() { return loading != null ? loading : ThemeConfig.THEME.get(); }

    @SubscribeEvent public void started(ClientStartedEvent e) { active = ThemeConfig.THEME.get(); started = true; }
    @SubscribeEvent public void tick(ClientTickEvent.Post e) {
        var mc = Minecraft.getInstance();
        if (!started || reloading || mc.getOverlay() != null || ThemeConfig.THEME.get() == active) return;
        Theme target = ThemeConfig.THEME.get();
        reloading = true; lastError = null;
        reload(target, false);
    }
    private static void reload(Theme target, boolean recovering) {
        var mc = Minecraft.getInstance();
        loading = target;
        try {
            mc.getResourcePackRepository().reload();
            mc.reloadResourcePacks().whenComplete((unused, error) -> mc.execute(() -> {
                if (error == null) {
                    active = target; loading = null; reloading = false;
                } else failed(error, recovering);
            }));
        } catch (Exception error) { failed(error, recovering); }
    }
    private static void failed(Throwable error, boolean recovering) {
        LogUtils.getLogger().error("Deep Primal theme reload failed", error);
        lastError = "Theme reload failed; see logs/latest.log";
        ThemeConfig.THEME.set(active); ThemeConfig.SPEC.save();
        if (!recovering) reload(active, true);
        else { loading = null; reloading = false; }
    }
}
