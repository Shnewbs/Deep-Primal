package tfcmt.tfcitembarrelsandmore.common.container;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import tfcmt.tfcitembarrelsandmore.common.TIBAMContainerTypes;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SmokeShackBlockEntity;

public class SmokeShackContainer
extends BlockEntityContainer<SmokeShackBlockEntity> {
    public static final int SLOT_A_X = 68;
    public static final int SLOT_A_Y = 30;
    public static final int SLOT_B_X = 90;
    public static final int SLOT_B_Y = 30;
    public static final int SMOKE_COL_X = 8;
    public static final int SMOKE_COL_Y = 9;
    public static final int SMOKE_COL_W = 16;
    public static final int SMOKE_COL_H = 50;
    public static final int DATA_TIMER = 0;
    public static final int DATA_MAX_TICKS = 1;
    public static final int DATA_COUNT = 2;
    private final ContainerData syncedData = new ContainerData(){

        public int get(int index) {
            return switch (index) {
                case 0 -> ((SmokeShackBlockEntity)SmokeShackContainer.this.blockEntity).getSmokeTimer();
                case 1 -> 6000;
                default -> 0;
            };
        }

        public void set(int index, int value) {
        }

        public int getCount() {
            return 2;
        }
    };

    public static SmokeShackContainer create(SmokeShackBlockEntity be, Inventory playerInv, int windowId) {
        return (SmokeShackContainer)new SmokeShackContainer(windowId, playerInv, be).init(playerInv, 0);
    }

    public SmokeShackContainer(int windowId, Inventory playerInv, SmokeShackBlockEntity be) {
        super((MenuType)TIBAMContainerTypes.SMOKE_SHACK.get(), windowId, (InventoryBlockEntity)be);
    }

    protected void addContainerSlots() {
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((SmokeShackBlockEntity)this.blockEntity).getInventory(), 0, 68, 30));
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((SmokeShackBlockEntity)this.blockEntity).getInventory(), 1, 90, 30));
        this.addDataSlots(this.syncedData);
    }

    protected boolean moveStack(ItemStack stack, int slotIndex) {
        if (slotIndex < this.containerSlots) {
            return this.moveItemStackTo(stack, this.containerSlots, this.slots.size(), true);
        }
        if (FoodCapability.get((ItemStack)stack) == null) {
            return true;
        }
        return this.moveItemStackTo(stack, 0, 2, false);
    }

    public int getSmokeTimer() {
        return this.syncedData.get(0);
    }

    public int getMaxTicks() {
        return this.syncedData.get(1);
    }

    public float getProgress() {
        int max = this.getMaxTicks();
        return max <= 0 ? 0.0f : (float)this.getSmokeTimer() / (float)max;
    }
}

