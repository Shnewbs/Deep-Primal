package tfcmt.tfcitembarrelsandmore.compat.jei;

import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import tfcmt.tfcitembarrelsandmore.common.recipes.TIBAMRecipes;
import tfcmt.tfcitembarrelsandmore.compat.jei.ExpellerPressJeiCategory;
import tfcmt.tfcitembarrelsandmore.compat.jei.GrinderJeiCategory;

@JeiPlugin
public class TIBAMJeiPlugin
implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "jei_plugin");

    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new IRecipeCategory[]{new GrinderJeiCategory(guiHelper), new ExpellerPressJeiCategory(guiHelper)});
    }

    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        List grinderRecipes = manager.getAllRecipesFor((RecipeType)TIBAMRecipes.GRINDER.get());
        registration.addRecipes(GrinderJeiCategory.RECIPE_TYPE, grinderRecipes);
        List pressRecipes = manager.getAllRecipesFor((RecipeType)TIBAMRecipes.EXPELLER_PRESS.get());
        registration.addRecipes(ExpellerPressJeiCategory.RECIPE_TYPE, pressRecipes);
    }
}

