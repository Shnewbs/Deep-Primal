package com.shnewbs.deepprimal.themes;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

/** Original approved menu composition with real, keyboard-accessible Minecraft actions. */
public final class ThemeTitleScreen extends TitleScreen {
    private final List<ThemeButton> controls=new ArrayList<>();
    private Theme builtFor;
    @Override protected void init() {
        super.init();
        var originals=children().stream().filter(c->c instanceof AbstractButton).map(c->(AbstractButton)c).toList();
        clearWidgets();controls.clear();builtFor=ThemeMod.active();
        if(!builtFor.custom())return;
        String[] keys={"menu.singleplayer", "menu.multiplayer", "menu.options", "menu.quit"};
        for(int i=0;i<keys.length;i++) {
            var rect=ThemeMenuArt.button(builtFor,i,width,height);
            final String key=keys[i];
            var original=originals.stream().filter(b->b.getMessage().getString().equals(Component.translatable(key).getString())).findFirst().orElse(null);
            var b=new ThemeButton(rect.x(),rect.y(),rect.w(),rect.h(),Component.translatable(key).getString(),ignored->{
                if(original!=null)original.onPress(null);
            });
            b.textScale(Math.max(1,rect.h()/18f));
            b.active=original!=null&&original.active;
            add(b);
        }
        var footer=new ArrayList<ThemeButton>();
        footer.add(new ThemeButton(0,0,1,1,"Themes",b->minecraft.setScreen(new ThemeScreen(this))));
        for(String key:new String[]{"fml.menu.mods","menu.online","options.language","options.accessibility.title"}) {
            var original=originals.stream().filter(b->b.getMessage().getString().equals(Component.translatable(key).getString())).findFirst().orElse(null);
            String label=switch(key){case "fml.menu.mods"->"Mods";case "menu.online"->"Realms";case "options.language"->"Language";default->"Access";};
            ThemeButton b;
            if(original!=null){b=new ThemeButton(0,0,1,1,label,ignored->original.onPress(null));b.active=original.active;}
            else if(key.equals("options.language"))b=new ThemeButton(0,0,1,1,label,ignored->minecraft.setScreen(new net.minecraft.client.gui.screens.options.LanguageSelectScreen(this,minecraft.options,minecraft.getLanguageManager())));
            else if(key.equals("options.accessibility.title"))b=new ThemeButton(0,0,1,1,label,ignored->minecraft.setScreen(new net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen(this,minecraft.options)));
            else continue;
            footer.add(b);
        }
        for(int i=0;i<footer.size();i++){
            var r=ThemeMenuArt.footerButton(i,footer.size(),width,height);var b=footer.get(i);
            b.setX(r.x());b.setY(r.y());b.setWidth(r.w());b.setHeight(r.h());
            b.textScale(Math.max(1,r.h()/20f));add(b);
        }

    }
    private void add(ThemeButton b){controls.add(b);addRenderableWidget(b);}
    @Override public void tick(){
        if(!ThemeMod.active().custom()||!ThemeConfig.TITLE_BRANDING.get()){minecraft.setScreen(new TitleScreen());return;}
        if(builtFor!=ThemeMod.active())rebuildWidgets();
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float a){
        if(ThemeMod.active().custom())ThemeMenuArt.background(g,ThemeMod.active(),width,height);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float a){
        for(var b:controls)b.extractRenderState(g,mx,my,a);
    }
}
