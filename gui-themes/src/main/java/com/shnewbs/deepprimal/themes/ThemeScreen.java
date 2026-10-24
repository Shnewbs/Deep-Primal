package com.shnewbs.deepprimal.themes;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.EnumMap;

public final class ThemeScreen extends Screen {
    private final Screen parent;
    private Theme selected = ThemeConfig.THEME.get();
    private Button apply, layoutButton;
    private final EnumMap<Theme,ThemeButton> choices=new EnumMap<>(Theme.class);
    private int left, top, panelWidth;
    public ThemeScreen(Screen parent) { super(Component.literal("Deep Primal Themes")); this.parent = parent; }
    @Override protected void init() {
        panelWidth = Math.min(420, width - 16);
        left = (width - panelWidth) / 2;
        top = Math.max(28, (height - 210) / 2);
        choices.clear();
        int cell = (panelWidth - 8) / 3;
        for (Theme t : Theme.values()) {
            int i = t.ordinal();
            var choice=new ThemeButton(left+(i%3)*(cell+4),top+(i/3)*24,cell,20,t.displayName,b->{selected=t;updateApply();});
            choice.setTooltip(Tooltip.create(Component.literal(t.description)));
            choices.put(t,addRenderableWidget(choice));
        }
        int actionWidth = (panelWidth - 8) / 3;
        apply = addRenderableWidget(Button.builder(Component.literal("Apply theme"), b -> {
            ThemeConfig.THEME.set(selected); ThemeConfig.SPEC.save(); updateApply();
        }).bounds(left, top + 166, actionWidth, 20).build());
        var layout = addRenderableWidget(Button.builder(Component.literal("HUD layout"), b -> minecraft.setScreen(new ThemeHudEditorScreen(this)))
            .bounds(left + actionWidth + 4, top + 166, actionWidth, 20).build());
        layout.setTooltip(Tooltip.create(Component.literal("Move the day and weather display. Apply a custom theme first.")));
        layout.active = ThemeMod.active().custom();
        layoutButton = layout;
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
            .bounds(left + 2 * (actionWidth + 4), top + 166, actionWidth, 20).build());
        updateApply();
    }
    private void updateApply() {
        if (apply != null) apply.active = !ThemeMod.reloading() && selected != ThemeConfig.THEME.get();
        if (layoutButton != null) layoutButton.active = !ThemeMod.reloading() && ThemeMod.active().custom();
        choices.forEach((theme,button)->button.selected=theme==selected);
    }
    @Override public void tick() { updateApply(); }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.text(font,title,width/2-font.width(title)/2,top-20,0xffffffff,false);
        g.fill(left, top + 76, left + panelWidth, top + 160, selected.accent);
        g.fill(left + 1, top + 77, left + panelWidth - 1, top + 159, selected.surface);
        if(selected.custom()) ThemeArt.panel(g,selected,left,top+76,panelWidth,84);
        g.text(font,selected.displayName,width/2-font.width(selected.displayName)/2,top+83,selected.text,false);
        int sx = width / 2 - 80;
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) {
            int x = sx + col * 18, y = top + 100 + row * 18;
            g.fill(x, y, x + 16, y + 16, selected.accent);
            g.fill(x + 1, y + 1, x + 15, y + 15, selected.well);
            if(selected.custom())ThemeArt.well(g,selected,x,y,16,16);
        }
        String status = ThemeMod.reloading() ? "Applying theme..." :
                ThemeMod.lastError() != null ? ThemeMod.lastError() : "Active: " + ThemeMod.active().displayName;
        g.text(font,status,width/2-font.width(status)/2,top+193,0xffffffff,false);
    }
}
