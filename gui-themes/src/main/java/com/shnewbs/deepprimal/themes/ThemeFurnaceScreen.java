package com.shnewbs.deepprimal.themes;

import com.shnewbs.deepprimal.themes.mixin.ThemeContainerAccessor;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;

/** Workstation layout uses the server menu's actual fuel, input, output and progress. */
public final class ThemeFurnaceScreen extends AbstractFurnaceScreen<AbstractFurnaceMenu> {
    private final ThemeSlotLayout layout;
    private ImageButton recipes;
    private ThemeButton recipeTab;
    public ThemeFurnaceScreen(AbstractFurnaceMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title,Component.translatable("gui.recipebook.toggleRecipes."+(menu instanceof SmokerMenu?"smokable":menu instanceof BlastFurnaceMenu?"blastable":"smeltable")),
            id("textures/gui/container/furnace.png"),id("container/furnace/lit_progress"),id("container/furnace/burn_progress"),tabs(menu));
        var a=(ThemeContainerAccessor)(Object)this;a.deepprimalThemes$width(260);a.deepprimalThemes$height(210);layout=new ThemeSlotLayout(menu);
    }
    private static Identifier id(String path){return Identifier.withDefaultNamespace(path);}
    private static List<RecipeBookComponent.TabInfo> tabs(AbstractFurnaceMenu m){
        if(m instanceof SmokerMenu)return List.of(new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.SMOKER),new RecipeBookComponent.TabInfo(Items.PORKCHOP,RecipeBookCategories.SMOKER_FOOD));
        if(m instanceof BlastFurnaceMenu)return List.of(new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.BLAST_FURNACE),new RecipeBookComponent.TabInfo(Items.REDSTONE_ORE,RecipeBookCategories.BLAST_FURNACE_BLOCKS),new RecipeBookComponent.TabInfo(Items.IRON_SHOVEL,Items.GOLDEN_LEGGINGS,RecipeBookCategories.BLAST_FURNACE_MISC));
        return List.of(new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.FURNACE),new RecipeBookComponent.TabInfo(Items.PORKCHOP,RecipeBookCategories.FURNACE_FOOD),new RecipeBookComponent.TabInfo(Items.STONE,RecipeBookCategories.FURNACE_BLOCKS),new RecipeBookComponent.TabInfo(Items.LAVA_BUCKET,Items.EMERALD,RecipeBookCategories.FURNACE_MISC));
    }
    private boolean stacked(){return ThemeMod.active()==Theme.BASALT_COPPER||ThemeMod.active()==Theme.RAWHIDE_FLINT||ThemeMod.active()==Theme.RIVERSTONE_MOSS;}
    @Override public void init(){
        super.init();layout.move(0,stacked()?43:107,stacked()?42:48);layout.move(1,43,stacked()?79:48);layout.move(2,209,stacked()?58:48);layout.playerGrid(3);
        recipes=children().stream().filter(c->c instanceof ImageButton).map(c->(ImageButton)c).findFirst().orElse(null);
        if(recipes!=null){recipes.visible=false;recipes.active=false;}
        recipeTab=addRenderableWidget(new ThemeButton(leftPos+184,topPos+8,60,18,"Recipes",b->{if(recipes!=null){recipes.onPress(null);recipeTab.setPosition(leftPos+184,topPos+8);}}));
    }
    @Override protected ScreenPosition getRecipeBookButtonPosition(){return new ScreenPosition(leftPos+184,topPos+8);}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
        var t=ThemeMod.active();g.text(font,title,18,13,t.text,false);
        g.text(font,"Fuel",38,stacked()?66:33,t.text,false);g.text(font,"Input",stacked()?38:100,stacked()?29:33,t.text,false);g.text(font,"Output",200,stacked()?43:33,t.text,false);
        String status=menu.isLit()?"Heating...":"Idle";g.text(font,status,130-font.width(status)/2,87,t.text,false);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){
        extractTransparentBackground(g);if(ThemeRecipes.narrowVisible(this))return;var t=ThemeMod.active();if(!t.custom())return;
        ThemeArt.panel(g,t,leftPos,topPos,imageWidth,imageHeight);
        for(var s:menu.slots)ThemeArt.well(g,t,leftPos+s.x-2,topPos+s.y-2,20,20);
        ThemeArt.arrow(g,t,leftPos+175,topPos+52);
        if(menu.isLit())g.blitSprite(RenderPipelines.GUI_TEXTURED,id("container/furnace/lit_progress"),leftPos+146,topPos+43,17,20);
        ThemeArt.button(g,t,leftPos+72,topPos+74,118,9,false,true);
        g.fill(leftPos+74,topPos+76,leftPos+74+Math.round(114*menu.getBurnProgress()),topPos+81,0xffff9227);
        int fuelY=topPos+(stacked()?97:70);g.fill(leftPos+42,fuelY,leftPos+42+Math.round(18*menu.getLitProgress()),fuelY+2,t.accent);
        g.horizontalLine(leftPos+20,leftPos+239,topPos+177,t.accent);
    }
    @Override public void removed(){layout.restore();super.removed();}
}
