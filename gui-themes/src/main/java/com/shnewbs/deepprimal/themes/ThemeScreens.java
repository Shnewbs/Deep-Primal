package com.shnewbs.deepprimal.themes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

final class ThemeScreens {
    @SubscribeEvent public void opening(ScreenEvent.Opening e) {
        var s=e.getNewScreen();var mc=Minecraft.getInstance();
        if(!ThemeMod.active().custom())return;
        if(s!=null&&s.getClass()==TitleScreen.class&&ThemeConfig.TITLE_BRANDING.get())e.setNewScreen(new ThemeTitleScreen());
        else if(s instanceof AbstractFurnaceScreen<?> f && (s.getClass()==FurnaceScreen.class||s.getClass()==SmokerScreen.class||s.getClass()==BlastFurnaceScreen.class))e.setNewScreen(new ThemeFurnaceScreen(f.getMenu(),mc.player.getInventory(),s.getTitle()));
        else if(s instanceof CraftingScreen c && s.getClass()==CraftingScreen.class)e.setNewScreen(new ThemeCraftingScreen(c.getMenu(),mc.player.getInventory(),s.getTitle()));
        else if(s!=null&&s.getClass()==InventoryScreen.class&&mc.player!=null&&!mc.player.hasInfiniteMaterials())e.setNewScreen(new ThemeInventoryScreen(mc.player));
    }
    @SubscribeEvent public void hudClick(ScreenEvent.MouseButtonPressed.Pre e){
        var s=e.getScreen();
        if(ThemeRecipes.narrowVisible(s)&&e.getButton()==0&&e.getMouseX()>=s.width/2-60&&e.getMouseX()<s.width/2+60&&e.getMouseY()>=s.height-23&&e.getMouseY()<s.height-5){ThemeRecipes.close(s);e.setCanceled(true);return;}
        if(!ThemeMod.active().custom()||!ThemeConfig.INTEGRATION_HUD.get()||!(s instanceof ThemeInventoryScreen inv)||e.getButton()!=0||s.width<380)return;
        double mx=e.getMouseX(),my=e.getMouseY();
        if(mx>=inv.getLeftPos()&&mx<inv.getLeftPos()+260&&my>=inv.getTopPos()&&my<inv.getTopPos()+198)return;
        int x=s.width/2-91,y=s.height-25;
        if(my<y||my>=y+23)return;
        if(mx>=x-70&&mx<x-30){ThemeActions.journal(s);e.setCanceled(true);}
        else if(mx>=x+212&&mx<x+252){ThemeActions.hearth();e.setCanceled(true);}
    }
    @SubscribeEvent public void recipeBack(ScreenEvent.Render.Post e){
        var s=e.getScreen();if(!ThemeRecipes.narrowVisible(s))return;
        var g=e.getGuiGraphics();var t=ThemeMod.active();ThemeArt.button(g,t,s.width/2-60,s.height-23,120,18,false,true);
        g.centeredText(Minecraft.getInstance().font,"Back",s.width/2,s.height-18,t.text);
    }
    @SubscribeEvent public void init(ScreenEvent.Init.Post event) {
        var screen=event.getScreen();
        if((screen instanceof TitleScreen&&!(screen instanceof ThemeTitleScreen))||screen instanceof OptionsScreen)
            event.addListener(Button.builder(Component.literal("Themes"),b->Minecraft.getInstance().setScreen(new ThemeScreen(screen))).bounds(4,4,58,20).build());
    }
}
