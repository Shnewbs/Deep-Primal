package tfcmt.tfcitembarrelsandmore.common.container;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import tfcmt.tfcitembarrelsandmore.common.TIBAMContainerTypes;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.container.ISteamEngineMenu;

public class SinglePistonEngineContainer
extends BlockEntityContainer<SinglePistonEngineBlockEntity>
implements ISteamEngineMenu {
    public static final int WATER_IN_X = 143;
    public static final int WATER_IN_Y = 8;
    public static final int WATER_OUT_X = 143;
    public static final int WATER_OUT_Y = 44;
    private int optimisticThrottle = -1;
    private int optimisticBleederTier = -1;

    public static SinglePistonEngineContainer create(SinglePistonEngineBlockEntity engine, Inventory playerInv, int windowId) {
        return (SinglePistonEngineContainer)new SinglePistonEngineContainer(windowId, playerInv, engine).init(playerInv, 0);
    }

    public SinglePistonEngineContainer(int windowId, Inventory playerInv, SinglePistonEngineBlockEntity engine) {
        super((MenuType)TIBAMContainerTypes.SINGLE_PISTON_ENGINE.get(), windowId, (InventoryBlockEntity)engine);
    }

    protected void addContainerSlots() {
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((SinglePistonEngineBlockEntity)this.blockEntity).getInventory(), 0, 143, 8){

            public boolean mayPlace(ItemStack stack) {
                return FluidUtil.getFluidContained((ItemStack)stack).map(f -> f.getFluid().is(TFCTags.Fluids.ANY_FRESH_WATER)).orElse(false);
            }
        });
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((SinglePistonEngineBlockEntity)this.blockEntity).getInventory(), 1, 143, 44){

            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addDataSlots((ContainerData)((SinglePistonEngineBlockEntity)this.blockEntity).getSyncableData());
    }

    protected boolean moveStack(ItemStack stack, int slotIndex) {
        if (slotIndex < this.containerSlots) {
            return this.moveItemStackTo(stack, this.containerSlots, this.slots.size(), true);
        }
        boolean isWater = FluidUtil.getFluidContained((ItemStack)stack).map(f -> f.getFluid().is(TFCTags.Fluids.ANY_FRESH_WATER)).orElse(false);
        if (isWater) {
            return this.moveItemStackTo(stack, 0, 1, false);
        }
        return true;
    }

    @Override
    public int getWaterAmount() {
        return ((SinglePistonEngineBlockEntity)this.blockEntity).getWaterAmount();
    }

    @Override
    public int getWaterCapacity() {
        return ((SinglePistonEngineBlockEntity)this.blockEntity).getWaterCapacity();
    }

    @Override
    public int getSteam() {
        return ((SinglePistonEngineBlockEntity)this.blockEntity).getSteam();
    }

    @Override
    public int getMaxSteam() {
        return ((SinglePistonEngineBlockEntity)this.blockEntity).getMaxSteam();
    }

    @Override
    public int getPsi() {
        return ((SinglePistonEngineBlockEntity)this.blockEntity).getPsi();
    }

    @Override
    public boolean isRunning() {
        return ((SinglePistonEngineBlockEntity)this.blockEntity).isRunning();
    }

    @Override
    public int getBleederTier() {
        int live = ((SinglePistonEngineBlockEntity)this.blockEntity).getBleederTier();
        if (this.optimisticBleederTier >= 0 && this.optimisticBleederTier != live) {
            return this.optimisticBleederTier;
        }
        this.optimisticBleederTier = -1;
        return live;
    }

    @Override
    public int getThrottle() {
        int live = ((SinglePistonEngineBlockEntity)this.blockEntity).getThrottle();
        if (this.optimisticThrottle >= 0 && this.optimisticThrottle != live) {
            return this.optimisticThrottle;
        }
        this.optimisticThrottle = -1;
        return live;
    }

    @Override
    public BlockPos getEnginePos() {
        return ((SinglePistonEngineBlockEntity)this.blockEntity).getBlockPos();
    }

    @Override
    public FluidTank getWaterTank() {
        return ((SinglePistonEngineBlockEntity)this.blockEntity).getWaterTank();
    }

    @Override
    public void setOptimisticThrottle(int v) {
        this.optimisticThrottle = v;
    }

    @Override
    public void setOptimisticBleederTier(int v) {
        this.optimisticBleederTier = v;
    }
}

