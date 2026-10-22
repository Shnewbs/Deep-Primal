package tfcmt.tfcitembarrelsandmore.common.container;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
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
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerTankBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.container.ISteamEngineMenu;

public class BoilerTankContainer
extends BlockEntityContainer<BoilerTankBlockEntity>
implements ISteamEngineMenu {
    public static final int WATER_IN_X = 143;
    public static final int WATER_IN_Y = 8;
    public static final int WATER_OUT_X = 143;
    public static final int WATER_OUT_Y = 44;
    private int optimisticThrottle = -1;
    private int optimisticBleederTier = -1;

    public static BoilerTankContainer create(BoilerTankBlockEntity be, Inventory playerInv, int windowId) {
        return (BoilerTankContainer)new BoilerTankContainer(windowId, playerInv, be).init(playerInv, 0);
    }

    public BoilerTankContainer(int windowId, Inventory playerInv, BoilerTankBlockEntity be) {
        super((MenuType)TIBAMContainerTypes.BOILER_TANK.get(), windowId, (InventoryBlockEntity)be);
    }

    protected void addContainerSlots() {
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((BoilerTankBlockEntity)this.blockEntity).getInventory(), 0, 143, 8){

            public boolean mayPlace(ItemStack stack) {
                return FluidUtil.getFluidContained((ItemStack)stack).map(f -> f.getFluid().is(FluidTags.WATER)).orElse(false);
            }
        });
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((BoilerTankBlockEntity)this.blockEntity).getInventory(), 1, 143, 44){

            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addDataSlots((ContainerData)((BoilerTankBlockEntity)this.blockEntity).getSyncableData());
    }

    protected boolean moveStack(ItemStack stack, int slotIndex) {
        if (slotIndex < this.containerSlots) {
            return this.moveItemStackTo(stack, this.containerSlots, this.slots.size(), true);
        }
        boolean isWater = FluidUtil.getFluidContained((ItemStack)stack).map(f -> f.getFluid().is(FluidTags.WATER)).orElse(false);
        if (isWater) {
            return this.moveItemStackTo(stack, 0, 1, false);
        }
        return true;
    }

    @Override
    public int getWaterAmount() {
        return ((BoilerTankBlockEntity)this.blockEntity).getWaterAmount();
    }

    @Override
    public int getWaterCapacity() {
        return ((BoilerTankBlockEntity)this.blockEntity).getWaterCapacity();
    }

    @Override
    public int getSteam() {
        return ((BoilerTankBlockEntity)this.blockEntity).getSteam();
    }

    @Override
    public int getMaxSteam() {
        return ((BoilerTankBlockEntity)this.blockEntity).getMaxSteam();
    }

    @Override
    public int getPsi() {
        return ((BoilerTankBlockEntity)this.blockEntity).getPsi();
    }

    @Override
    public boolean isRunning() {
        return ((BoilerTankBlockEntity)this.blockEntity).isRunning();
    }

    @Override
    public int getBleederTier() {
        return this.optimisticBleederTier >= 0 ? this.optimisticBleederTier : ((BoilerTankBlockEntity)this.blockEntity).getBleederTier();
    }

    @Override
    public int getThrottle() {
        return this.optimisticThrottle >= 0 ? this.optimisticThrottle : ((BoilerTankBlockEntity)this.blockEntity).getThrottle();
    }

    @Override
    public BlockPos getEnginePos() {
        return ((BoilerTankBlockEntity)this.blockEntity).getBlockPos();
    }

    public BoilerTankBlockEntity.TankTier getTier() {
        return ((BoilerTankBlockEntity)this.blockEntity).getTier();
    }

    @Override
    public FluidTank getWaterTank() {
        return ((BoilerTankBlockEntity)this.blockEntity).getWaterTank();
    }

    @Override
    public void setOptimisticThrottle(int value) {
        this.optimisticThrottle = value;
    }

    @Override
    public void setOptimisticBleederTier(int value) {
        this.optimisticBleederTier = value;
    }
}

