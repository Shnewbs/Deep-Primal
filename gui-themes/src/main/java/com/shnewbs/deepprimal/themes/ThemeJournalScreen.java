package com.shnewbs.deepprimal.themes;

import com.shnewbs.deepprimal.themes.api.ThemeIntegration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A standalone field record backed by current player/world values, not mock data. */
final class ThemeJournalScreen extends Screen {
    private final Screen parent;
    private int x,y;
    ThemeJournalScreen(Screen parent){super(Component.literal("Journal"));this.parent=parent;}
    @Override protected void init(){
        x=(width-238)/2;y=(height-190)/2;
        addRenderableWidget(new ThemeButton(x+12,y+162,100,20,"Back",b->onClose()));
        var hearth=addRenderableWidget(new ThemeButton(x+126,y+162,100,20,"Hearth",b->ThemeActions.hearth()));
        hearth.active=ThemeIntegration.provider().hearth().map(ThemeIntegration.Action::enabled).orElse(false);
        if(!hearth.active)hearth.setTooltip(Tooltip.create(Component.literal("Hearth travel is unavailable")));
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        var t=ThemeMod.active();if(!t.custom()){onClose();return;}
        ThemeArt.panel(g,t,x,y,238,190);
        g.centeredText(font,"Journal",width/2,y+14,t.text);
        var p=minecraft.player;
        if(p!=null){
            g.text(font,p.getName(),x+28,y+40,t.text,false);
            g.text(font,"Health  "+Math.round(p.getHealth())+" / "+Math.round(p.getMaxHealth()),x+28,y+62,t.text,false);
            g.text(font,"Hunger  "+p.getFoodData().getFoodLevel()+" / 20",x+28,y+80,t.text,false);
            g.text(font,"Day "+(minecraft.level.getOverworldClockTime()/24000+1),x+28,y+98,t.text,false);
            ThemeIntegration.provider().survival().ifPresent(s->{
                g.text(font,"Thirst  "+Math.round(s.thirst()*100)+"%",x+28,y+116,t.text,false);
                g.text(font,"Level "+s.level(),x+28,y+134,t.text,false);
            });
        }
        super.extractRenderState(g,mx,my,delta);
    }
}
