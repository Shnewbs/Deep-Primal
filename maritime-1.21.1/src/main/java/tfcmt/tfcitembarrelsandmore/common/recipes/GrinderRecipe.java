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

public final class GrinderRecipe extends ItemRecipe {
    public static final IndirectHashCollection<Item, GrinderRecipe> CACHE = IndirectHashCollection.createForRecipe(ItemRecipe::getValidItems, TIBAMRecipes.GRINDER);
    public static final MapCodec<GrinderRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Ingredient.CODEC.fieldOf("ingredient").forGetter(GrinderRecipe::getIngredient),
        ItemStackProvider.CODEC.fieldOf("result").forGetter(GrinderRecipe::getResult)).apply(i, GrinderRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, GrinderRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, GrinderRecipe::getIngredient,
        ItemStackProvider.STREAM_CODEC, GrinderRecipe::getResult, GrinderRecipe::new);
    public GrinderRecipe(Ingredient ingredient, ItemStackProvider result) { super(ingredient, result); }
    public static GrinderRecipe getRecipe(Level level, ItemStack stack) {
        for (var recipe : CACHE.getAll(stack.getItem())) if (recipe.matches(stack)) return recipe;
        return null;
    }
    @Override public RecipeSerializer<?> getSerializer() { return TIBAMRecipes.GRINDER_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return TIBAMRecipes.GRINDER.get(); }
    public static final class Serializer implements RecipeSerializer<GrinderRecipe> {
        @Override public MapCodec<GrinderRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, GrinderRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
