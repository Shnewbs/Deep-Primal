package com.shnewbs.deepprimal.themes;
import com.shnewbs.deepprimal.themes.mixin.ThemeContainerAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
public final class ThemeCraftingScreen extends CraftingScreen {
    private final ThemeSlotLayout layout;
    public ThemeCraftingScreen(CraftingMenu menu,Inventory inventory,Component title){super(menu,inventory,title);var a=(ThemeContainerAccessor)(Object)this;a.deepprimalThemes$width(260);a.deepprimalThemes$height(198);layout=new ThemeSlotLayout(menu);}
    @Override protected void init(){super.init();layout.move(0,198,58);for(int i=0;i<9;i++)layout.move(i+1,66+(i%3)*20,38+(i/3)*20);layout.playerGrid(10);}
    @Override protected ScreenPosition getRecipeBookButtonPosition(){return new ScreenPosition(leftPos+225,topPos+10);}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){g.text(font,title,18,13,ThemeMod.active().text,false);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){
        extractTransparentBackground(g);if(ThemeRecipes.narrowVisible(this))return;var t=ThemeMod.active();if(!t.custom())return;ThemeArt.panel(g,t,leftPos,topPos,imageWidth,imageHeight);
        for(var s:menu.slots)ThemeArt.well(g,t,leftPos+s.x-2,topPos+s.y-2,20,20);
        ThemeArt.arrow(g,t,leftPos+151,topPos+61);g.horizontalLine(leftPos+20,leftPos+239,topPos+165,t.accent);
    }
    @Override public void removed(){layout.restore();super.removed();}
}
