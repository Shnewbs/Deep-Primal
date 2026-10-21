package com.shnewbs.deepprimal.themes;

import com.shnewbs.deepprimal.themes.api.ThemeIntegration;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

final class ThemeHud {
    private static boolean enabled(){var mc=Minecraft.getInstance();return ThemeMod.active().custom()&&ThemeConfig.INTEGRATION_HUD.get()&&mc.player!=null&&!mc.player.isSpectator()&&!mc.options.hideGui;}
    @SubscribeEvent public void before(RenderGuiLayerEvent.Pre e){
        if(!enabled()||!e.getName().getNamespace().equals("minecraft"))return;
        String name=e.getName().getPath();var mc=Minecraft.getInstance();
        if(name.equals("player_health"))mc.gui.leftHeight=75;
        if(name.equals("food_level"))mc.gui.rightHeight=75;
        if(name.equals("player_health")||name.equals("food_level")||name.equals("experience_level")||
                (!mc.player.isPassenger()&&(name.equals("contextual_info_bar")||name.equals("contextual_info_bar_background"))))e.setCanceled(true);
    }
    static void render(GuiGraphicsExtractor g,DeltaTracker delta){
        if(!enabled())return;
        var mc=Minecraft.getInstance();var p=mc.player;var t=ThemeMod.active();var state=ThemeIntegration.provider().survival();
        int x=g.guiWidth()/2-91, y=g.guiHeight()-63;
        if(mc.gameMode.canHurtPlayer()){
            int cell=state.isPresent()?59:89;
            int health=p.hasEffect(MobEffects.POISON)?0xff8dbb36:p.hasEffect(MobEffects.WITHER)?0xff655b76:0xffec303d;
            meter(g,t,"Health",x,y,cell,p.getHealth()/p.getMaxHealth(),health);
            if(p.getAbsorptionAmount()>0)g.text(mc.font,"+"+Math.round(p.getAbsorptionAmount()),x,y-9,0xffffdf51,false);
            meter(g,t,"Hunger",x+cell+4,y,cell,p.getFoodData().getFoodLevel()/20f,0xffee942b);
            if(state.isPresent())meter(g,t,"Thirst",x+126,y,56,state.get().thirst(),0xff21b9ef);
        }
        if(!p.isPassenger()){
            boolean progress=state.isPresent()||!ModList.get().isLoaded("deepprimal");
            if(progress){
                float f=state.map(ThemeIntegration.SurvivalState::levelProgress).orElse(p.experienceProgress);
                int level=state.map(ThemeIntegration.SurvivalState::level).orElse(p.experienceLevel);
                int yy=g.guiHeight()-36;
                ThemeArt.button(g,t,x,yy,182,10,false,true);
                g.fill(x+2,yy+2,x+2+Math.round(178*f),yy+8,t.accent);
                String label="Level "+level;int tw=mc.font.width(label);
                g.fill(g.guiWidth()/2-tw/2-3,yy,g.guiWidth()/2+tw/2+3,yy+10,t.well);
                g.centeredText(mc.font,label,g.guiWidth()/2,yy+1,0xfff6f0de);
            }
        }
        if(g.guiWidth()>=380){
            action(g,t,x-70,g.guiHeight()-25,40,"Journal",ThemeActions.JOURNAL.getTranslatedKeyMessage().getString(),true);
            boolean ready=ThemeIntegration.provider().hearth().map(ThemeIntegration.Action::enabled).orElse(false);
            action(g,t,x+212,g.guiHeight()-25,40,"Hearth",ThemeActions.HEARTH.getTranslatedKeyMessage().getString(),ready);
        }
        if(mc.level!=null){
            String day="Day "+(mc.level.getOverworldClockTime()/24000+1);
            String weather=mc.level.isThundering()?"Thunder":mc.level.isRaining()?"Rain":"Clear";
            int wx=g.guiWidth()-76;
            ThemeArt.button(g,t,wx,5,70,27,false,true);
            g.centeredText(mc.font,day,wx+35,8,t.text);
            g.centeredText(mc.font,weather,wx+35,19,t.text);
        }
    }
    private static void meter(GuiGraphicsExtractor g,Theme t,String label,int x,int y,int w,float fraction,int color){
        var font=Minecraft.getInstance().font;
        g.text(font,label,x,y,0xffffffff,true);
        if(t==Theme.TIMBER_IRON||t==Theme.CLAY_EMBER){
            ThemeArt.button(g,t,x,y+10,w,10,false,true);
            g.fill(x+2,y+12,x+2+Math.round((w-4)*Math.clamp(fraction,0f,1f)),y+18,color);
            if(t==Theme.TIMBER_IRON)for(int i=1;i<5;i++)g.verticalLine(x+i*w/5,y+11,y+18,t.well);
        }else {
            ThemeArt.button(g,t,x,y+10,w,10,false,true);
            int step=Math.min(8,(w-4)/10),start=x+(w-step*10)/2;
            for(int i=0;i<10;i++){
                int xx=start+i*step, yy=y+12;boolean full=fraction*10>=i+0.75f,half=fraction*10>i+0.25f;
                if(label.equals("Thirst")){
                    int c=full?color:half?0xff4489a6:0xff38414a;
                    g.fill(xx+2,yy,xx+3,yy+1,c);g.fill(xx+1,yy+1,xx+3,yy+2,c);g.fill(xx,yy+2,xx+4,yy+5,c);g.fill(xx+1,yy+5,xx+3,yy+6,c);
                }else {
                    String path=label.equals("Health")?"hud/heart/"+(full?"full":half?"half":"container"):"hud/food_"+(full?"full":half?"half":"empty");
                    g.blitSprite(RenderPipelines.GUI_TEXTURED,Identifier.withDefaultNamespace(path),xx,yy,step,7);
                }
            }
        }
    }
    private static void action(GuiGraphicsExtractor g,Theme t,int x,int y,int w,String label,String key,boolean enabled){
        ThemeArt.button(g,t,x,y,w,23,false,enabled);
        g.centeredText(Minecraft.getInstance().font,key,x+w/2,y+3,enabled?t.text:0xff999999);
        g.centeredText(Minecraft.getInstance().font,label,x+w/2,y+13,enabled?t.text:0xff999999);
    }
}
