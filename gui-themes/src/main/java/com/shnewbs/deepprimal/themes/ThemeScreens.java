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
        if(ThemeRecipes.narrowVisible(s)&&e.getButton()==0){
            for(var child:s.children())if(child instanceof RecipeBackButton button&&button.isMouseOver(e.getMouseX(),e.getMouseY())){
                button.onPress(null);e.setCanceled(true);return;
            }
        }
        if(!ThemeHud.enabled()||!(s instanceof ThemeInventoryScreen inv)||ThemeRecipes.narrowVisible(s)||e.getButton()!=0)return;
        double mx=e.getMouseX(),my=e.getMouseY();
        if(mx>=inv.getLeftPos()&&mx<inv.getLeftPos()+inv.getImageWidth()&&my>=inv.getTopPos()&&my<inv.getTopPos()+inv.getImageHeight())return;
        if(ThemeHud.hint(true,s.width,s.height).filter(h->h.contains(mx,my)).isPresent()){ThemeActions.journal(s);e.setCanceled(true);}
        else if(ThemeHud.hint(false,s.width,s.height).filter(h->h.contains(mx,my)).isPresent()){ThemeActions.hearth();e.setCanceled(true);}
    }
    /** Rebuild open native/custom containers when a live theme switch changes their layout. */
    static void refreshOpenScreen(){
        var mc=Minecraft.getInstance();var s=mc.screen;
        if(mc.player==null)return;
        if(s instanceof ThemeInventoryScreen||s!=null&&s.getClass()==InventoryScreen.class)mc.setScreen(new InventoryScreen(mc.player));
        else if(s instanceof ThemeFurnaceScreen f){
            var menu=f.getMenu();var inventory=mc.player.getInventory();
            if(menu instanceof net.minecraft.world.inventory.SmokerMenu m)mc.setScreen(new SmokerScreen(m,inventory,f.getTitle()));
            else if(menu instanceof net.minecraft.world.inventory.BlastFurnaceMenu m)mc.setScreen(new BlastFurnaceScreen(m,inventory,f.getTitle()));
            else if(menu instanceof net.minecraft.world.inventory.FurnaceMenu m)mc.setScreen(new FurnaceScreen(m,inventory,f.getTitle()));
        }else if(s instanceof ThemeCraftingScreen c)mc.setScreen(new CraftingScreen(c.getMenu(),mc.player.getInventory(),c.getTitle()));
        else if(ThemeMod.active().custom()&&(s!=null&&(s.getClass()==FurnaceScreen.class||s.getClass()==SmokerScreen.class||s.getClass()==BlastFurnaceScreen.class||s.getClass()==CraftingScreen.class)))mc.setScreen(s);
    }
    @SubscribeEvent public void updateRecipeBack(ScreenEvent.Render.Pre e){
        for(var child:e.getScreen().children())if(child instanceof RecipeBackButton button)
            button.visible=button.active=ThemeRecipes.narrowVisible(e.getScreen());
    }
    @SubscribeEvent public void recipeBack(ScreenEvent.Render.Post e){
        var s=e.getScreen();if(!ThemeRecipes.narrowVisible(s))return;
        for(var child:s.children())if(child instanceof RecipeBackButton button)
            button.extractRenderState(e.getGuiGraphics(),e.getMouseX(),e.getMouseY(),0);
    }
    @SubscribeEvent public void init(ScreenEvent.Init.Post event) {
        var screen=event.getScreen();
        if(ThemeRecipes.custom(screen))event.addListener(new RecipeBackButton(screen));
        if((screen instanceof TitleScreen&&!(screen instanceof ThemeTitleScreen))||screen instanceof OptionsScreen)
            event.addListener(Button.builder(Component.literal("Themes"),b->Minecraft.getInstance().setScreen(new ThemeScreen(screen))).bounds(4,4,58,20).build());
    }
    private static final class RecipeBackButton extends Button {
        RecipeBackButton(net.minecraft.client.gui.screens.Screen screen){
            super(screen.width/2-60,screen.height-23,120,18,Component.literal("Back"),b->ThemeRecipes.close(screen),DEFAULT_NARRATION);
            visible=active=ThemeRecipes.narrowVisible(screen);
        }
        @Override protected void extractContents(net.minecraft.client.gui.GuiGraphicsExtractor g,int mx,int my,float delta){
            extractDefaultSprite(g);
            extractDefaultLabel(g.textRendererForWidget(this,net.minecraft.client.gui.GuiGraphicsExtractor.HoveredTextEffects.NONE));
        }
    }
}
