package com.shnewbs.deepprimal.themes.mixin;
import com.shnewbs.deepprimal.themes.ThemeRecipes;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
@Mixin(AbstractRecipeBookScreen.class)
abstract class ThemeRecipeScreenMixin {
    @ModifyConstant(method="init",constant=@Constant(intValue=379))
    private int deepprimalThemes$bookWidth(int old){return ThemeRecipes.custom((Screen)(Object)this)?460:old;}
}
