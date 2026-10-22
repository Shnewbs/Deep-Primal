package tfcmt.tfcitembarrelsandmore.common.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.dries007.tfc.common.recipes.ItemRecipe;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.util.collections.IndirectHashCollection;
import net.neoforged.neoforge.fluids.FluidStack;

public final class ExpellerPressRecipe extends ItemRecipe {
    private final FluidStack resultFluid;
    private final int resultFluidMin;
    private final ItemStack resultItem;
    private final int pressTicks;
    public static final IndirectHashCollection<Item, ExpellerPressRecipe> CACHE = IndirectHashCollection.createForRecipe(ItemRecipe::getValidItems, TIBAMRecipes.EXPELLER_PRESS);
    public static final MapCodec<ExpellerPressRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Ingredient.CODEC.fieldOf("ingredient").forGetter(ExpellerPressRecipe::getIngredient),
        FluidStack.CODEC.fieldOf("result_fluid").forGetter(r -> r.resultFluid),
        Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("result_fluid_min", Integer.MAX_VALUE).forGetter(r -> r.resultFluidMin),
        ItemStack.CODEC.optionalFieldOf("result_item", ItemStack.EMPTY).forGetter(r -> r.resultItem),
        Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("press_ticks", 600).forGetter(r -> r.pressTicks)).apply(i, ExpellerPressRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExpellerPressRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, ExpellerPressRecipe::getIngredient,
        FluidStack.STREAM_CODEC, r -> r.resultFluid,
        ByteBufCodecs.VAR_INT, r -> r.resultFluidMin,
        ItemStack.OPTIONAL_STREAM_CODEC, r -> r.resultItem,
        ByteBufCodecs.VAR_INT, r -> r.pressTicks, ExpellerPressRecipe::new);
    public ExpellerPressRecipe(Ingredient ingredient, FluidStack fluid, int min, ItemStack result, int ticks) {
        super(ingredient, ItemStackProvider.of(result));
        resultFluid = fluid.copy(); resultItem = result.copy();
        resultFluidMin = Math.clamp(min, 1, Math.max(1, fluid.getAmount())); pressTicks = Math.max(1, ticks);
    }
    public static ExpellerPressRecipe getRecipe(Level level, ItemStack input) {
        for (var recipe : CACHE.getAll(input.getItem())) if (recipe.matches(input)) return recipe;
        return null;
    }
    public FluidStack getResultFluid() { return resultFluid.copy(); }
    public boolean hasResultItem() { return !resultItem.isEmpty(); }
    public int getResultFluidMin() { return resultFluidMin; }
    public int getPressTicks() { return pressTicks; }
    @Override public RecipeSerializer<?> getSerializer() { return TIBAMRecipes.EXPELLER_PRESS_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return TIBAMRecipes.EXPELLER_PRESS.get(); }
    public static final class Serializer implements RecipeSerializer<ExpellerPressRecipe> {
        @Override public MapCodec<ExpellerPressRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, ExpellerPressRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
