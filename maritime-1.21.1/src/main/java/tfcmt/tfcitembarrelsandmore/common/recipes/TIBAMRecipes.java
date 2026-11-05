package tfcmt.tfcitembarrelsandmore.common.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import tfcmt.tfcitembarrelsandmore.common.recipes.ExpellerPressRecipe;
import tfcmt.tfcitembarrelsandmore.common.recipes.GrinderRecipe;
import tfcmt.tfcitembarrelsandmore.common.recipes.SmokeShackRecipe;

public class TIBAMRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create((ResourceKey)Registries.RECIPE_TYPE, (String)"tfc_item_barrels_and_more");
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, (String)"tfc_item_barrels_and_more");
    public static final DeferredHolder<RecipeType<?>, RecipeType<GrinderRecipe>> GRINDER = RECIPE_TYPES.register("grinder", () -> new RecipeType<GrinderRecipe>(){

        public String toString() {
            return "tfc_item_barrels_and_more:grinder";
        }
    });
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GrinderRecipe>> GRINDER_SERIALIZER = RECIPE_SERIALIZERS.register("grinder", GrinderRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ExpellerPressRecipe>> EXPELLER_PRESS = RECIPE_TYPES.register("expeller_press", () -> new RecipeType<ExpellerPressRecipe>(){

        public String toString() {
            return "tfc_item_barrels_and_more:expeller_press";
        }
    });
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ExpellerPressRecipe>> EXPELLER_PRESS_SERIALIZER = RECIPE_SERIALIZERS.register("expeller_press", ExpellerPressRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<SmokeShackRecipe>> SMOKE_SHACK = RECIPE_TYPES.register("smoke_shack", () -> new RecipeType<SmokeShackRecipe>(){

        public String toString() {
            return "tfc_item_barrels_and_more:smoke_shack";
        }
    });
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SmokeShackRecipe>> SMOKE_SHACK_SERIALIZER = RECIPE_SERIALIZERS.register("smoke_shack", SmokeShackRecipe.Serializer::new);
}

