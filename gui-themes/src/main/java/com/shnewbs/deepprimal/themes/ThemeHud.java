package com.shnewbs.deepprimal.themes;

import com.shnewbs.deepprimal.themes.api.ThemeIntegration;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.gui.Gui;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

final class ThemeHud {
    static boolean enabled(){var mc=Minecraft.getInstance();return ThemeMod.active().custom()&&ThemeConfig.INTEGRATION_HUD.get()&&mc.player!=null&&!mc.player.isSpectator()&&!mc.options.hideGui;}
    @SubscribeEvent public void before(RenderGuiLayerEvent.Pre e){
        if(!enabled()||!e.getName().getNamespace().equals("minecraft"))return;
        String name=e.getName().getPath();var mc=Minecraft.getInstance();
        if(name.equals("player_health"))mc.gui.leftHeight=meterTopOffset()+12;
        if(name.equals("food_level"))mc.gui.rightHeight=meterTopOffset()+12;
        if(name.equals("player_health")||name.equals("food_level")||name.equals("experience_level")||
                (!mc.player.isPassenger()&&(name.equals("contextual_info_bar")||name.equals("contextual_info_bar_background"))))e.setCanceled(true);
    }
    private static int meterTopOffset(){
        var mc=Minecraft.getInstance();
        // Riding retains the native jump/context bar. Deep Primal without a
        // progression provider has no bar, so close that unused row.
        boolean hasBar=mc.player.isPassenger()||ThemeIntegration.provider().survival().isPresent()||!ModList.get().isLoaded("deepprimal");
        return hasBar?51:38;
    }
    static void render(GuiGraphicsExtractor g,DeltaTracker delta){
        if(!enabled())return;
        var mc=Minecraft.getInstance();var p=mc.player;var t=ThemeMod.active();var state=ThemeIntegration.provider().survival();
        int center=g.guiWidth()/2, x=center-91;
        if(mc.gameMode.canHurtPlayer()){
            int start=center-(state.isPresent()?137:91),y=g.guiHeight()-meterTopOffset();
            Gui.HeartType heart=p.hasEffect(MobEffects.POISON)?Gui.HeartType.POISIONED:p.hasEffect(MobEffects.WITHER)?Gui.HeartType.WITHERED:p.isFullyFrozen()?Gui.HeartType.FROZEN:Gui.HeartType.NORMAL;
            heart=net.neoforged.neoforge.client.ClientHooks.firePlayerHeartTypeEvent(p,heart);
            g.fill(start,y,start+(state.isPresent()?275:182),y+14,0x70101820);
            meter(g,t,"Health",start,y,p.getHealth()/Math.max(1,p.getMaxHealth()),0xffec303d,heart);
            meter(g,t,"Hunger",start+93,y,p.getFoodData().getFoodLevel()/20f,0xffee942b,heart);
            if(state.isPresent())meter(g,t,"Thirst",start+186,y,state.get().thirst(),0xff21b9ef,heart);
            if(p.getAbsorptionAmount()>0)text(g,"+"+Math.round(p.getAbsorptionAmount()),Math.max(14,start-14),y+3,0xffffdf51);
        }
        if(!p.isPassenger()&&mc.gameMode.canHurtPlayer()&&(state.isPresent()||!ModList.get().isLoaded("deepprimal"))){
            float f=Math.clamp(state.map(ThemeIntegration.SurvivalState::levelProgress).orElse(p.experienceProgress),0f,1f);
            int level=state.map(ThemeIntegration.SurvivalState::level).orElse(p.experienceLevel);
            progression(g,x,g.guiHeight()-35,182,f,level);
        }
        hint(true,g.guiWidth(),g.guiHeight()).ifPresent(h->drawHint(g,h));
        hint(false,g.guiWidth(),g.guiHeight()).ifPresent(h->drawHint(g,h));
        if(mc.level!=null && !(mc.screen instanceof ThemeHudEditorScreen))
            ThemeDayBadge.render(g,ThemeDayBadge.bounds(g.guiWidth(),g.guiHeight(),ThemeConfig.DAY_X.get(),ThemeConfig.DAY_Y.get()));
    }
    /** Slim bevelled frame with a continuous purple fill beneath a centered level plaque. */
    static void progression(GuiGraphicsExtractor g,int x,int y,int width,float progress,int level){
        g.fill(x,y,x+width,y+11,0xff080c10);
        g.outline(x+1,y+1,width-2,9,0xff828b91);
        g.horizontalLine(x+2,x+width-3,y+9,0xff3e484e);
        g.fill(x+3,y+3,x+width-3,y+8,0xff252e34);
        int end=x+3+Math.round((width-6)*Math.clamp(progress,0f,1f));
        if(end>x+3){
            g.fill(x+3,y+3,end,y+4,0xffb9a3ef);
            g.fill(x+3,y+4,end,y+7,0xff7954c8);
            g.fill(x+3,y+7,end,y+8,0xff4e318d);
        }
        String label="Level "+Math.max(0,level);int w=Minecraft.getInstance().font.width(label),center=x+width/2;
        g.fill(center-w/2-4,y+1,center+(w+1)/2+4,y+10,0xff10161b);
        g.verticalLine(center-w/2-4,y+2,y+8,0xff828b91);
        g.verticalLine(center+(w+1)/2+3,y+2,y+8,0xff828b91);
        text(g,label,center,y+2,0xfff5f3ed);
    }
    private static void meter(GuiGraphicsExtractor g,Theme t,String label,int x,int y,float fraction,int color,Gui.HeartType heart){
        fraction=Math.clamp(fraction,0f,1f);
        if(t==Theme.TIMBER_IRON||t==Theme.CLAY_EMBER){
            g.fill(x+4,y+3,x+85,y+11,0xbb101418);
            g.fill(x+5,y+4,x+5+Math.round(79*fraction),y+10,color);
            if(t==Theme.TIMBER_IRON)for(int i=1;i<5;i++)g.verticalLine(x+4+i*81/5,y+3,y+10,t.well);
        }else for(int i=0;i<10;i++){
            int xx=x+4+i*8, yy=y+2;int units=(int)Math.ceil(fraction*20);boolean full=units>=2*(i+1),half=units>2*i;
            if(label.equals("Thirst")){
                int c=full?color:half?0xff4489a6:0xff38414a;
                g.fill(xx+3,yy,xx+4,yy+2,c);g.fill(xx+2,yy+2,xx+5,yy+3,c);
                g.fill(xx+1,yy+3,xx+6,yy+6,c);g.fill(xx+2,yy+6,xx+5,yy+7,c);
            }else{
                Identifier sprite;
                var player=Minecraft.getInstance().player;
                if(label.equals("Health"))sprite=(half?heart:Gui.HeartType.CONTAINER).getSprite(player.level().getLevelData().isHardcore(),!full,false);
                else sprite=FOOD[player.hasEffect(MobEffects.HUNGER)?1:0][full?2:half?1:0];
                g.blitSprite(RenderPipelines.GUI_TEXTURED,sprite,xx,yy,9,9);
            }
        }
    }
    private static final Identifier[][] FOOD={
        {Identifier.withDefaultNamespace("hud/food_empty"),Identifier.withDefaultNamespace("hud/food_half"),Identifier.withDefaultNamespace("hud/food_full")},
        {Identifier.withDefaultNamespace("hud/food_empty_hunger"),Identifier.withDefaultNamespace("hud/food_half_hunger"),Identifier.withDefaultNamespace("hud/food_full_hunger")}
    };
    record Hint(int x,int y,int width,int height,String label){
        boolean contains(double mx,double my){return mx>=x&&mx<x+width&&my>=y&&my<y+height;}
    }
    /** Rendering and clicking share the same bounds, including long rebound keys and offhand clearance. */
    static Optional<Hint> hint(boolean journal,int width,int height){
        if(!enabled())return Optional.empty();
        var mc=Minecraft.getInstance();var provider=ThemeIntegration.provider();
        if(journal&&!provider.journal().map(ThemeIntegration.Action::enabled).orElse(true))return Optional.empty();
        if(!journal&&!provider.hearth().map(ThemeIntegration.Action::enabled).orElse(false))return Optional.empty();
        String key=(journal?ThemeActions.JOURNAL:ThemeActions.HEARTH).getTranslatedKeyMessage().getString();
        String label=key+"  "+(journal?"Journal":"Hearth");int w=mc.font.width(label)+12;
        int clearance=!mc.player.getOffhandItem().isEmpty()||mc.options.attackIndicator().get()==AttackIndicatorStatus.HOTBAR?32:0;
        int x=journal?width/2-99-clearance-w:width/2+99+clearance;
        return x<4||x+w>width-4?Optional.empty():Optional.of(new Hint(x,height-22,w,20,label));
    }
    private static void drawHint(GuiGraphicsExtractor g,Hint h){
        var theme=ThemeMod.active();
        ThemeArt.button(g,theme,h.x,h.y,h.width,h.height,false,true);
        g.text(Minecraft.getInstance().font,h.label,h.x+6,h.y+6,theme.text,false);
    }
    private static void text(GuiGraphicsExtractor g,String label,int center,int y,int color){var font=Minecraft.getInstance().font;g.text(font,label,center-font.width(label)/2,y,color,false);}
}
