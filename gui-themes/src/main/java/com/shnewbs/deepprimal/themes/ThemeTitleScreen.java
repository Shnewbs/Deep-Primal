package com.shnewbs.deepprimal.themes;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
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
        var art=ThemeArt.of(builtFor);
        String[] keys={"", "menu.singleplayer", "menu.multiplayer", "menu.options", "menu.quit"};
        String[] labels={"Continue","Singleplayer","Multiplayer","Settings","Quit"};
        for(int i=0;i<5;i++) {
            var rect=art.menuButtons()[i];
            int x=(rect.x()-art.scene().x())*width/art.scene().w();
            int y=(rect.y()-art.scene().y())*height/art.scene().h();
            int w=rect.w()*width/art.scene().w(),h=rect.h()*height/art.scene().h();
            final int index=i;
            var original=i==0?null:originals.stream().filter(b->b.getMessage().getString().equals(Component.translatable(keys[index]).getString())).findFirst().orElse(null);
            var b=new ThemeButton(x,y,w,h,labels[i],ignored->{
                if(index==0) minecraft.createWorldOpenFlows().openWorld(ThemeConfig.LAST_WORLD.get(),()->minecraft.setScreen(this));
                else if(original!=null)original.onPress(null);
            });
            b.active=i==0?!ThemeConfig.LAST_WORLD.get().isBlank():original!=null&&original.active;
            if(i==0&&!b.active)b.setTooltip(Tooltip.create(Component.literal("Play a local world to enable Continue")));
            b.selected=i==0;add(b);
        }
        int x=Math.max(4,width-302),y=height-24;
        add(new ThemeButton(x,y,66,20,"Themes",b->minecraft.setScreen(new ThemeScreen(this))));
        int next=x+70;
        for(String key:new String[]{"fml.menu.mods","menu.online","options.language","options.accessibility.title"}) {
            var original=originals.stream().filter(b->b.getMessage().getString().equals(Component.translatable(key).getString())).findFirst().orElse(null);
            if(original==null){
                if(key.equals("options.language"))add(new ThemeButton(next,y,54,20,"Language",b->minecraft.setScreen(new net.minecraft.client.gui.screens.options.LanguageSelectScreen(this,minecraft.options,minecraft.getLanguageManager()))));
                else if(key.equals("options.accessibility.title"))add(new ThemeButton(next,y,54,20,"Access",b->minecraft.setScreen(new net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen(this,minecraft.options))));
                next+=58;continue;
            }
            String label=switch(key){case "fml.menu.mods"->"Mods";case "menu.online"->"Realms";case "options.language"->"Language";default->"Access";};
            var b=new ThemeButton(next,y,54,20,label,ignored->original.onPress(null));b.active=original.active;add(b);next+=58;
        }
    }
    private void add(ThemeButton b){controls.add(b);addRenderableWidget(b);}
    @Override public void tick(){
        if(!ThemeMod.active().custom()||!ThemeConfig.TITLE_BRANDING.get()){minecraft.setScreen(new TitleScreen());return;}
        if(builtFor!=ThemeMod.active())rebuildWidgets();
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float a){
        if(ThemeMod.active().custom())ThemeArt.region(g,ThemeMod.active(),ThemeArt.of(ThemeMod.active()).scene(),0,0,width,height);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float a){
        for(var b:controls)b.extractRenderState(g,mx,my,a);
    }
}
