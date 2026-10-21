package com.shnewbs.deepprimal.themes;

import com.shnewbs.deepprimal.themes.mixin.ThemeSlotAccessor;
import com.shnewbs.deepprimal.themes.mixin.ThemeContainerAccessor;
import com.shnewbs.deepprimal.themes.mixin.ThemeRecipeAccessor;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

/** Uses the real InventoryMenu and native recipe book, drag/drop, tooltips and shift-click. */
public final class ThemeInventoryScreen extends InventoryScreen {
    private final Map<Slot,int[]> originalPositions=new IdentityHashMap<>();
    private ThemeButton inventoryTab,craftingTab,journalTab,close;
    private ImageButton recipes;
    public ThemeInventoryScreen(Player player) {super(player);var a=(ThemeContainerAccessor)(Object)this;a.deepprimalThemes$width(260);a.deepprimalThemes$height(198);}
    @Override protected void init(){
        super.init();
        recipes=children().stream().filter(c->c instanceof ImageButton).map(c->(ImageButton)c).findFirst().orElse(null);
        if(recipes!=null){recipes.visible=false;recipes.active=false;}
        for(var slot:menu.slots)originalPositions.putIfAbsent(slot,new int[]{slot.x,slot.y});
        position(0,229,55);
        for(int i=0;i<4;i++)position(1+i,163+(i%2)*22,44+(i/2)*22);
        for(int i=0;i<4;i++)position(5+i,23,30+i*18);
        for(int i=0;i<27;i++)position(9+i,23+(i%9)*24,107+(i/9)*20);
        for(int i=0;i<9;i++)position(36+i,23+i*24,171);
        position(45,123,84);
        inventoryTab=addRenderableWidget(new ThemeButton(0,0,72,18,"Inventory",b->{if(craftingTab.selected)toggleRecipes();}));inventoryTab.selected=true;
        craftingTab=addRenderableWidget(new ThemeButton(0,0,72,18,"Crafting",b->toggleRecipes()));
        journalTab=addRenderableWidget(new ThemeButton(0,0,72,18,"Journal",b->ThemeActions.journal(this)));
        close=addRenderableWidget(new ThemeButton(0,0,16,18,"X",b->onClose()));
        syncRecipeTabs();positionTabs();
    }
    private void toggleRecipes(){if(recipes!=null){recipes.onPress(null);syncRecipeTabs();positionTabs();}}
    private void syncRecipeTabs(){craftingTab.selected=((ThemeRecipeAccessor)(Object)this).deepprimalThemes$recipeBook().isVisible();inventoryTab.selected=!craftingTab.selected;}
    private void positionTabs(){
        if(inventoryTab==null)return;
        inventoryTab.setPosition(leftPos+12,topPos+6);craftingTab.setPosition(leftPos+86,topPos+6);
        journalTab.setPosition(leftPos+160,topPos+6);close.setPosition(leftPos+234,topPos+6);
    }
    private void position(int i,int x,int y){var a=(ThemeSlotAccessor)(Object)menu.getSlot(i);a.deepprimalThemes$x(x);a.deepprimalThemes$y(y);}
    @Override protected ScreenPosition getRecipeBookButtonPosition(){return new ScreenPosition(leftPos+86,topPos+6);}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){g.text(font,"Crafting",160,31,ThemeMod.active().text,false);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){
        extractTransparentBackground(g);if(ThemeRecipes.narrowVisible(this))return;
        var t=ThemeMod.active();if(!t.custom())return;
        ThemeArt.panel(g,t,leftPos,topPos,imageWidth,imageHeight);
        g.fill(leftPos+44,topPos+29,leftPos+119,topPos+106,0xcc101313);
        g.outline(leftPos+44,topPos+29,75,77,t.accent);
        for(var slot:menu.slots)ThemeArt.well(g,t,leftPos+slot.x-2,topPos+slot.y-2,20,20);
        ThemeArt.arrow(g,t,leftPos+202,topPos+59);
        InventoryScreen.extractEntityInInventoryFollowsMouse(g,leftPos+45,topPos+30,leftPos+118,topPos+106,31,0.0625F,mx,my,minecraft.player);
        g.horizontalLine(leftPos+20,leftPos+239,topPos+165,t.accent);
    }
    @Override public void removed(){
        originalPositions.forEach((s,p)->{var a=(ThemeSlotAccessor)(Object)s;a.deepprimalThemes$x(p[0]);a.deepprimalThemes$y(p[1]);});
        originalPositions.clear();super.removed();
    }
}
