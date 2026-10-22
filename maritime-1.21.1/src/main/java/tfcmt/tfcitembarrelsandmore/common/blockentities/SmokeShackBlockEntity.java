package tfcmt.tfcitembarrelsandmore.common.blockentities;

import javax.annotation.Nonnull;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodTrait;
import net.dries007.tfc.common.component.food.FoodTraits;
import net.dries007.tfc.common.component.food.IFood;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMFoodTraits;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.SmokeShackBlock;
import tfcmt.tfcitembarrelsandmore.common.container.SmokeShackContainer;
import tfcmt.tfcitembarrelsandmore.common.recipes.SmokeShackRecipe;

public class SmokeShackBlockEntity
extends TickableInventoryBlockEntity<ItemStackHandler>
implements MenuProvider {
    public static final int SLOT_A = 0;
    public static final int SLOT_B = 1;
    public static final int INVENTORY_SIZE = 2;
    public static final float WARMTH_THRESHOLD = 50.0f;
    public static final float BRIGHT_RED_THRESHOLD = 1200.0f;
    public static final int SMOKE_TICKS = 6000;
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.smoke_shack");
    private static final TagKey<Item> TAG_MEATS = TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc", "foods/meats"));
    private int smokeTimer = 0;
    private final IItemHandler topCap = this.inventory;

    public SmokeShackBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.SMOKE_SHACK.get(), pos, state, self -> new ItemStackHandler(2){

            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (!stack.is(TAG_MEATS)) {
                    return false;
                }
                IFood food = FoodCapability.get((ItemStack)stack);
                if (food == null) {
                    return false;
                }
                return !food.hasTrait(TIBAMFoodTraits.SMOKED);
            }

            protected void onContentsChanged(int slot) {
                self.setChanged();
                self.markForSync();
            }
        }, NAME.getString());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SmokeShackBlockEntity be) {
        float temp = be.getBoilerTemperature();
        boolean hasFood = be.hasAnyFood();
        if (temp < 50.0f) {
            be.setLit(level, pos, state, false);
            be.checkForLastTickSync();
            return;
        }
        if (!hasFood) {
            be.setLit(level, pos, state, false);
            be.smokeTimer = 0;
            be.checkForLastTickSync();
            return;
        }
        be.setLit(level, pos, state, true);
        ++be.smokeTimer;
        if (be.smokeTimer >= 6000) {
            be.processSlot(0, temp, level);
            be.processSlot(1, temp, level);
            be.smokeTimer = 0;
        }
        be.setChanged();
        be.markForSync();
        be.checkForLastTickSync();
    }

    private void processSlot(int slot, float temp, Level level) {
        ItemStack stack = ((ItemStackHandler)this.inventory).getStackInSlot(slot);
        if (stack.isEmpty()) {
            return;
        }
        IFood food = FoodCapability.get((ItemStack)stack);
        if (food == null) {
            return;
        }
        SmokeShackRecipe recipe = SmokeShackRecipe.getRecipe(level, stack);
        if (recipe != null && recipe.hasCookedResult()) {
            long creationDate = food.getCreationDate();
            ItemStack cooked = recipe.assemble(stack);
            cooked.setCount(stack.getCount());
            FoodCapability.setCreationDate((ItemStack)cooked, (long)creationDate);
            ((ItemStackHandler)this.inventory).setStackInSlot(slot, cooked);
            food = FoodCapability.get((ItemStack)cooked);
            if (food == null) {
                return;
            }
        }
        if (temp >= 1200.0f) {
            FoodCapability.applyTrait(this.inventory.getStackInSlot(slot), FoodTraits.WOOD_GRILLED);
        } else {
            FoodCapability.applyTrait(this.inventory.getStackInSlot(slot), TIBAMFoodTraits.SMOKED);
        }
    }

    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return SmokeShackContainer.create(this, playerInv, windowId);
    }

    public Component getDisplayName() {
        return NAME;
    }

    private boolean hasAnyFood() {
        return !((ItemStackHandler)this.inventory).getStackInSlot(0).isEmpty() || !((ItemStackHandler)this.inventory).getStackInSlot(1).isEmpty();
    }

    public float getBoilerTemperature() {
        if (this.level == null || this.level.isClientSide) {
            return 0.0f;
        }
        BlockEntity blockEntity = this.level.getBlockEntity(this.worldPosition.below());
        if (blockEntity instanceof BoilerBlockEntity) {
            BoilerBlockEntity boiler = (BoilerBlockEntity)blockEntity;
            return boiler.getTemperature();
        }
        return 0.0f;
    }

    private void setLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        if (level.isClientSide) {
            return;
        }
        if ((Boolean)state.getValue((Property)SmokeShackBlock.LIT) == lit) {
            return;
        }
        level.setBlock(pos, (BlockState)state.setValue((Property)SmokeShackBlock.LIT, Boolean.valueOf(lit)), 2);
    }





    public ItemStackHandler getInventory() {
        return (ItemStackHandler)this.inventory;
    }

    public int getSmokeTimer() {
        return this.smokeTimer;
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.smokeTimer = tag.getInt("SmokeTimer");
    }

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("SmokeTimer", this.smokeTimer);
    }

    public net.neoforged.neoforge.items.IItemHandler maritimeItems(net.minecraft.core.Direction side) { return side == net.minecraft.core.Direction.UP ? this.topCap : null; }
}

