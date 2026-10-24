package com.shnewbs.deepprimal.themes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Shared geometry for the live badge, editor preview and drag hitbox. */
final class ThemeDayBadge {
    static final int MARGIN = 6;
    static final int HEIGHT = 28;
    record Bounds(int x, int y, int width, int height) {}

    static String day() {
        var level = Minecraft.getInstance().level;
        return "Day " + (level == null ? 1 : level.getOverworldClockTime() / 24000 + 1);
    }
    static String weather() {
        var level = Minecraft.getInstance().level;
        return level == null ? "Clear" : level.isThundering() ? "Thunder" : level.isRaining() ? "Rain" : "Clear";
    }
    static int width() {
        var font = Minecraft.getInstance().font;
        return Math.max(68, Math.max(font.width(day()), font.width(weather())) + 16);
    }
    static int travel(int screen, int badge) { return Math.max(0, screen - badge - 2 * MARGIN); }
    static double relative(double pixel, int screen, int badge) {
        int travel = travel(screen, badge);
        return travel == 0 ? 0 : Math.clamp((pixel - MARGIN) / travel, 0.0, 1.0);
    }
    static Bounds bounds(int screenWidth, int screenHeight, double x, double y) {
        int w = Math.min(width(), Math.max(1, screenWidth - 2 * MARGIN));
        int py=MARGIN + (int)Math.round(travel(screenHeight, HEIGHT) * Math.clamp(y, 0.0, 1.0));
        var player=Minecraft.getInstance().player;
        // The default corner anchor must leave native status-effect icons visible.
        if(x==1.0&&y==0.0&&player!=null){
            int occupied=0;
            for(var effect:player.getActiveEffects())if(effect.showIcon())
                occupied=Math.max(occupied,effect.getEffect().value().isBeneficial()?26:52);
            if(occupied>0)py=Math.min(screenHeight-HEIGHT-MARGIN,occupied+MARGIN);
        }
        return new Bounds(MARGIN + (int)Math.round(travel(screenWidth, w) * Math.clamp(x, 0.0, 1.0)),
            py, w, HEIGHT);
    }
    static void render(GuiGraphicsExtractor g, Bounds b) {
        var theme = ThemeMod.active();
        if (theme.custom()) ThemeArt.button(g, theme, b.x, b.y, b.width, b.height, false, true);
        else g.fill(b.x, b.y, b.x + b.width, b.y + b.height, 0xee182027);
        var font = Minecraft.getInstance().font;
        int color = theme.custom() ? theme.text : 0xffeeeeee;
        String day = day(), weather = weather();
        g.text(font, day, b.x + (b.width - font.width(day)) / 2, b.y + 5, color, false);
        g.text(font, weather, b.x + (b.width - font.width(weather)) / 2, b.y + 16, color, false);
    }
    private ThemeDayBadge() {}
}
