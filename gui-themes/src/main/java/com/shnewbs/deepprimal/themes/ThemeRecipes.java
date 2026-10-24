package com.shnewbs.deepprimal.themes;
import com.shnewbs.deepprimal.themes.mixin.ThemeRecipeAccessor;
import net.minecraft.client.gui.screens.Screen;
public final class ThemeRecipes {
    public static boolean custom(Screen s){return s instanceof ThemeInventoryScreen||s instanceof ThemeFurnaceScreen||s instanceof ThemeCraftingScreen;}
    public static boolean narrowVisible(Screen s){return custom(s)&&s.width<460&&((ThemeRecipeAccessor)s).deepprimalThemes$recipeBook().isVisible();}
    static void close(Screen s){((ThemeRecipeAccessor)s).deepprimalThemes$recipeBook().toggleVisibility();s.resize(s.width,s.height);}
    private ThemeRecipes(){}
}
