package com.shnewbs.deepprimal.themes;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ThemeScreen extends Screen {
    private final Screen parent;
    private Theme selected = ThemeConfig.THEME.get();
    private Button apply;
    private int left, top, panelWidth;
    public ThemeScreen(Screen parent) { super(Component.literal("Deep Primal Themes")); this.parent = parent; }
    @Override protected void init() {
        panelWidth = Math.min(420, width - 16);
        left = (width - panelWidth) / 2;
        top = Math.max(28, (height - 210) / 2);
        int cell = (panelWidth - 8) / 3;
        for (Theme t : Theme.values()) {
            int i = t.ordinal();
            addRenderableWidget(Button.builder(Component.literal(t.displayName), b -> { selected = t; updateApply(); })
                .bounds(left + (i % 3) * (cell + 4), top + (i / 3) * 24, cell, 20)
                .tooltip(Tooltip.create(Component.literal(t.description))).build());
        }
        apply = addRenderableWidget(Button.builder(Component.literal("Apply theme"), b -> {
            ThemeConfig.THEME.set(selected); ThemeConfig.SPEC.save(); updateApply();
        }).bounds(left, top + 166, (panelWidth - 4) / 2, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
            .bounds(left + (panelWidth + 4) / 2, top + 166, (panelWidth - 4) / 2, 20).build());
        updateApply();
    }
    private void updateApply() { if (apply != null) apply.active = !ThemeMod.reloading() && selected != ThemeConfig.THEME.get(); }
    @Override public void tick() { updateApply(); }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(font, title, width / 2, top - 20, 0xffffffff);
        g.fill(left, top + 76, left + panelWidth, top + 160, selected.accent);
        g.fill(left + 1, top + 77, left + panelWidth - 1, top + 159, selected.surface);
        if(selected.custom()) ThemeArt.panel(g,selected,left,top+76,panelWidth,84);
        g.centeredText(font, Component.literal(selected.displayName), width / 2, top + 83, selected.text);
        int sx = width / 2 - 62;
        for (int row = 0; row < 4; row++) for (int col = 0; col < 9; col++) {
            int x = sx + col * 14, y = top + 98 + row * 13 + (row == 3 ? 3 : 0);
            g.fill(x, y, x + 12, y + 12, selected.accent);
            g.fill(x + 1, y + 1, x + 11, y + 11, selected.well);
            if(selected.custom())ThemeArt.well(g,selected,x,y,12,12);
        }
        String status = ThemeMod.reloading() ? "Applying theme..." :
                ThemeMod.lastError() != null ? ThemeMod.lastError() : "Active: " + ThemeMod.active().displayName;
        g.centeredText(font, Component.literal(status), width / 2, top + 193, 0xffffffff);
    }
}
