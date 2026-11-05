package tfcmt.tfcitembarrelsandmore.common;

import net.dries007.tfc.common.component.food.FoodTrait;
import net.dries007.tfc.common.component.food.FoodTraits;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TIBAMFoodTraits {
    public static final DeferredRegister<FoodTrait> TRAITS = DeferredRegister.create(FoodTraits.KEY, "tfc_item_barrels_and_more");
    public static final DeferredHolder<FoodTrait, FoodTrait> SMOKED = TRAITS.register("smoked", () -> new FoodTrait(() -> 0.5, "tfc_item_barrels_and_more.tooltip.food_trait.smoked"));
    public static final DeferredHolder<FoodTrait, FoodTrait> CURED = TRAITS.register("cured", () -> new FoodTrait(() -> 0.25, "tfc_item_barrels_and_more.tooltip.food_trait.cured"));
    private TIBAMFoodTraits() {}
}
