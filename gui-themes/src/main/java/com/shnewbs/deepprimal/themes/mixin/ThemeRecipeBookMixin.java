package com.shnewbs.deepprimal.themes.mixin;
import com.shnewbs.deepprimal.themes.ThemeRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
@Mixin(RecipeBookComponent.class)
abstract class ThemeRecipeBookMixin {
    @ModifyConstant(method={"initVisuals","isOffsetNextToMainGUI"},constant=@Constant(intValue=86))
    private int deepprimalThemes$bookOffset(int old){return ThemeRecipes.custom(Minecraft.getInstance().screen)?128:old;}
}
