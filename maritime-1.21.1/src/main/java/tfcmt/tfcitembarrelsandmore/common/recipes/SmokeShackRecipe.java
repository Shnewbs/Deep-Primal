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

public final class SmokeShackRecipe extends ItemRecipe {
    public static final IndirectHashCollection<Item, SmokeShackRecipe> CACHE = IndirectHashCollection.createForRecipe(ItemRecipe::getValidItems, TIBAMRecipes.SMOKE_SHACK);
    private final ItemStack cookedResult;
    public static final MapCodec<SmokeShackRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Ingredient.CODEC.fieldOf("ingredient").forGetter(SmokeShackRecipe::getIngredient),
        ItemStack.CODEC.optionalFieldOf("cooked_result", ItemStack.EMPTY).forGetter(r -> r.cookedResult)).apply(i, SmokeShackRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SmokeShackRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, SmokeShackRecipe::getIngredient,
        ItemStack.OPTIONAL_STREAM_CODEC, r -> r.cookedResult, SmokeShackRecipe::new);
    public SmokeShackRecipe(Ingredient ingredient, ItemStack cookedResult) {
        super(ingredient, ItemStackProvider.of(cookedResult)); this.cookedResult = cookedResult.copy();
    }
    public static SmokeShackRecipe getRecipe(Level level, ItemStack input) {
        for (var recipe : CACHE.getAll(input.getItem())) if (recipe.matches(input)) return recipe;
        return null;
    }
    public boolean hasCookedResult() { return !cookedResult.isEmpty(); }
    @Override public RecipeSerializer<?> getSerializer() { return TIBAMRecipes.SMOKE_SHACK_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return TIBAMRecipes.SMOKE_SHACK.get(); }
    public static final class Serializer implements RecipeSerializer<SmokeShackRecipe> {
        @Override public MapCodec<SmokeShackRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, SmokeShackRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
