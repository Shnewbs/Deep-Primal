package tfcmt.tfcitembarrelsandmore.compat.jei;

import java.util.Arrays;
import java.util.List;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.TIBAMItems;
import tfcmt.tfcitembarrelsandmore.common.recipes.ExpellerPressRecipe;

public class ExpellerPressJeiCategory
implements IRecipeCategory<ExpellerPressRecipe> {
    public static final RecipeType<ExpellerPressRecipe> RECIPE_TYPE = RecipeType.create((String)"tfc_item_barrels_and_more", (String)"expeller_press", ExpellerPressRecipe.class);
    private static final int WIDTH = 104;
    private static final int HEIGHT = 62;
    private static final int INPUT_X = 4;
    private static final int INPUT_Y = 14;
    private static final int SCREW_X = 4;
    private static final int SCREW_Y = 38;
    private static final int ARROW_X = 28;
    private static final int ARROW_Y = 17;
    private static final int FLUID_X = 56;
    private static final int FLUID_Y = 8;
    private static final int FLUID_W = 18;
    private static final int FLUID_H = 26;
    private static final int OUTPUT_X = 80;
    private static final int OUTPUT_Y = 14;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;
    private final List<ItemStack> screwCycle;

    public ExpellerPressJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(104, 62);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack((ItemLike)TIBAMBlocks.EXPELLER_PRESS.get()));
        this.arrow = guiHelper.drawableBuilder(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/furnace.png"), 176, 14, 24, 16).buildAnimated(60, IDrawableAnimated.StartDirection.LEFT, false);
        this.screwCycle = Arrays.asList(new ItemStack((ItemLike)TIBAMItems.WROUGHT_IRON_PRESS_SCREW.get()), new ItemStack((ItemLike)TIBAMItems.STEEL_PRESS_SCREW.get()), new ItemStack((ItemLike)TIBAMItems.BLACK_STEEL_PRESS_SCREW.get()), new ItemStack((ItemLike)TIBAMItems.BLUE_STEEL_PRESS_SCREW.get()), new ItemStack((ItemLike)TIBAMItems.RED_STEEL_PRESS_SCREW.get()));
    }

    public RecipeType<ExpellerPressRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    public Component getTitle() {
        return Component.translatable((String)"jei.tfc_item_barrels_and_more.category.expeller_press");
    }

    public IDrawable getBackground() {
        return this.background;
    }

    public IDrawable getIcon() {
        return this.icon;
    }

    public void setRecipe(IRecipeLayoutBuilder builder, ExpellerPressRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 15).addIngredients(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.CATALYST, 5, 39).addItemStacks(this.screwCycle);
        if (!recipe.getResultFluid().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 57, 9).addIngredient((IIngredientType)ForgeTypes.FLUID_STACK, (Object)recipe.getResultFluid());
        }
        if (recipe.hasResultItem()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 15).addItemStack(recipe.getResultItem(null));
        }
    }

    public void draw(ExpellerPressRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        ExpellerPressJeiCategory.drawSlotBorder(graphics, 4, 14);
        ExpellerPressJeiCategory.drawSlotBorder(graphics, 4, 38);
        graphics.fill(56, 8, 74, 9, -7631989);
        graphics.fill(56, 8, 57, 34, -7631989);
        graphics.fill(57, 33, 74, 34, -1);
        graphics.fill(73, 9, 74, 34, -1);
        graphics.fill(57, 9, 73, 33, -7631989);
        if (recipe.hasResultItem()) {
            ExpellerPressJeiCategory.drawSlotBorder(graphics, 80, 14);
        }
        this.arrow.draw(graphics, 28, 17);
        if (!recipe.getResultFluid().isEmpty() && mouseX >= 56.0 && mouseX < 74.0 && mouseY >= 8.0 && mouseY < 34.0) {
            int max = recipe.getResultFluid().getAmount();
            int min = recipe.getResultFluidMin();
            if (min < max) {
                graphics.renderTooltip(Minecraft.getInstance().font, (Component)Component.translatable((String)"jei.tfc_item_barrels_and_more.press_yield", (Object[])new Object[]{min, max}), (int)mouseX, (int)mouseY);
            }
        }
    }

    private static void drawSlotBorder(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 1, -7631989);
        graphics.fill(x, y, x + 1, y + 18, -7631989);
        graphics.fill(x + 1, y + 17, x + 18, y + 18, -1);
        graphics.fill(x + 17, y + 1, x + 18, y + 18, -1);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, -7631989);
    }
}

