package com.shnewbs.deepprimal.themes;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Edits a draft layout; only Save writes config. Escape/Cancel discard the draft. */
public final class ThemeHudEditorScreen extends Screen {
    private final Screen parent;
    private double dayX = ThemeConfig.DAY_X.get(), dayY = ThemeConfig.DAY_Y.get();
    private double grabX, grabY;
    private boolean moving;
    private Button badge;

    public ThemeHudEditorScreen(Screen parent) {
        super(Component.literal("HUD layout"));
        this.parent = parent;
    }
    @Override protected void init() {
        moving = false;
        var b = bounds();
        badge = addRenderableWidget(new Button(b.x(), b.y(), b.width(), b.height(),
            Component.literal("Day and weather. Drag to move, or use the arrow keys."), unused -> {}, narration -> narration.get()) {
            @Override protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float delta) {
                ThemeDayBadge.render(g, bounds());
                g.outline(getX()-1, getY()-1, getWidth()+2, getHeight()+2,
                    isFocused() || isHovered() || moving ? 0xffe6ce8a : 0xff96acbb);
            }
        });
        int buttonWidth = Math.min(100, (width - 24) / 3);
        int left = (width - buttonWidth * 3 - 8) / 2, y = height - 72;
        addRenderableWidget(new ThemeButton(left, y, buttonWidth, 20, "Save", button -> {
            ThemeConfig.DAY_X.set(dayX); ThemeConfig.DAY_Y.set(dayY); ThemeConfig.SPEC.save(); onClose();
        }));
        addRenderableWidget(new ThemeButton(left + buttonWidth + 4, y, buttonWidth, 20, "Reset", button -> {
            dayX = 1; dayY = 0; updateBadge(); setFocused(badge);
        }));
        addRenderableWidget(new ThemeButton(left + (buttonWidth + 4) * 2, y, buttonWidth, 20, "Cancel", button -> onClose()));
        setInitialFocus(badge);
    }
    private ThemeDayBadge.Bounds bounds() { return ThemeDayBadge.bounds(width, height, dayX, dayY); }
    private void updateBadge() {
        var b = bounds();
        badge.setPosition(b.x(), b.y()); badge.setWidth(b.width());
    }
    private void moveTo(double x, double y) {
        var b = bounds();
        dayX = ThemeDayBadge.relative(x, width, b.width());
        dayY = ThemeDayBadge.relative(y, height, b.height());
        updateBadge();
    }
    @Override public void tick() { if(!ThemeMod.active().custom()){onClose();return;} updateBadge(); }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        // Toolbar controls are drawn on top and must remain clickable beneath a moved badge.
        for(var child:children())if(child!=badge&&child.isMouseOver(event.x(),event.y())&&child.mouseClicked(event,doubleClick)){
            setFocused(child);return true;
        }
        if (event.button() == 0 && badge.isMouseOver(event.x(), event.y())) {
            var b = bounds(); grabX = event.x() - b.x(); grabY = event.y() - b.y();
            moving = true; setFocused(badge); return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (moving && event.button() == 0) { moveTo(event.x() - grabX, event.y() - grabY); return true; }
        return super.mouseDragged(event, dx, dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if (moving && event.button() == 0) { moving = false; return true; }
        return super.mouseReleased(event);
    }
    @Override public boolean keyPressed(KeyEvent event) {
        if (getFocused() == badge) {
            int dx = event.key() == GLFW.GLFW_KEY_LEFT ? -1 : event.key() == GLFW.GLFW_KEY_RIGHT ? 1 : 0;
            int dy = event.key() == GLFW.GLFW_KEY_UP ? -1 : event.key() == GLFW.GLFW_KEY_DOWN ? 1 : 0;
            if (dx != 0 || dy != 0) {
                var b = bounds(); int step = event.hasShiftDown() ? 10 : 1;
                moveTo(b.x() + dx * step, b.y() + dy * step); return true;
            }
        }
        return super.keyPressed(event);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {
        // Leave the live world and HUD sharp while arranging the badge.
        if (minecraft.level == null) super.extractBackground(g, mx, my, delta);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        String help = "Drag the day/weather display.";
        String keys = "Arrow keys move; Shift moves faster.";
        int helpY = height - 101, helpWidth = Math.max(font.width(help),font.width(keys));
        g.fill(width/2-helpWidth/2-6, helpY-4, width/2+helpWidth/2+6, helpY+23, 0xdd10161b);
        g.text(font, help, (width-font.width(help))/2, helpY, 0xfff5f3ed, false);
        g.text(font, keys, (width-font.width(keys))/2, helpY+11, 0xffc7cdd3, false);
        if (minecraft.level == null) {
            String preview = "Layout preview";
            g.text(font, preview, (width-font.width(preview))/2, 12, 0xffffffff, false);
        }
        super.extractRenderState(g, mx, my, delta);
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
}
