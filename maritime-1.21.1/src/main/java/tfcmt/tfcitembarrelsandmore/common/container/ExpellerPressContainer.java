package tfcmt.tfcitembarrelsandmore.common.container;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import tfcmt.tfcitembarrelsandmore.common.TIBAMContainerTypes;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExpellerPressBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.items.PressScrewItem;

public class ExpellerPressContainer
extends BlockEntityContainer<ExpellerPressBlockEntity> {
    private static final int INPUT_X = 89;
    private static final int INPUT_Y = 37;
    private static final int SCREW_X = 113;
    private static final int SCREW_Y = 37;
    private static final int OUTPUT_X = 137;
    private static final int OUTPUT_Y = 37;
    public static final int DATA_FLUID_AMOUNT = 0;
    public static final int DATA_FLUID_CAPACITY = 1;
    public static final int DATA_PRESS_PROGRESS = 2;
    public static final int DATA_COUNT = 3;
    public static final int FLUID_BAR_X = 8;
    public static final int FLUID_BAR_Y = 20;
    public static final int FLUID_BAR_WIDTH = 16;
    public static final int FLUID_BAR_HEIGHT = 50;
    private ContainerData syncedData;

    public static ExpellerPressContainer create(ExpellerPressBlockEntity press, Inventory playerInv, int windowId) {
        return (ExpellerPressContainer)new ExpellerPressContainer(windowId, playerInv, press).init(playerInv, 12);
    }

    public ExpellerPressContainer(int windowId, Inventory playerInv, ExpellerPressBlockEntity press) {
        super((MenuType)TIBAMContainerTypes.EXPELLER_PRESS.get(), windowId, press);
    }

    protected void addContainerSlots() {
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((ExpellerPressBlockEntity)this.blockEntity).getInventory(), 0, 89, 37));
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((ExpellerPressBlockEntity)this.blockEntity).getInventory(), 1, 113, 37));
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((ExpellerPressBlockEntity)this.blockEntity).getInventory(), 2, 137, 37){

            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.syncedData = new ContainerData(){

            public int get(int index) {
                return switch (index) {
                    case 0 -> ((ExpellerPressBlockEntity)ExpellerPressContainer.this.blockEntity).getOutputTank().getFluidAmount();
                    case 1 -> ((ExpellerPressBlockEntity)ExpellerPressContainer.this.blockEntity).getOutputTank().getCapacity();
                    case 2 -> (int)(((ExpellerPressBlockEntity)ExpellerPressContainer.this.blockEntity).getPressProgress() * 10.0f);
                    default -> 0;
                };
            }

            public void set(int index, int value) {
            }

            public int getCount() {
                return 3;
            }
        };
        this.addDataSlots(this.syncedData);
    }

    protected boolean moveStack(ItemStack stack, int slotIndex) {
        if (slotIndex < this.containerSlots) {
            return this.moveItemStackTo(stack, this.containerSlots, this.slots.size(), true);
        }
        if (stack.getItem() instanceof PressScrewItem) {
            return this.moveItemStackTo(stack, 1, 2, false);
        }
        return this.moveItemStackTo(stack, 0, 1, false);
    }

    public int getFluidAmount() {
        return this.syncedData.get(0);
    }

    public int getFluidCapacity() {
        return this.syncedData.get(1);
    }

    public float getPressProgressF() {
        return (float)this.syncedData.get(2) / 10.0f;
    }

    public int getPressProgress() {
        return this.syncedData.get(2);
    }

    public FluidStack getOutputFluid() {
        return ((ExpellerPressBlockEntity)this.blockEntity).getOutputTank().getFluid().copy();
    }
}

