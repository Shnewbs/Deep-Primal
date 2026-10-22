package tfcmt.tfcitembarrelsandmore.common.container;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import tfcmt.tfcitembarrelsandmore.common.TIBAMContainerTypes;
import tfcmt.tfcitembarrelsandmore.common.blockentities.AirCompressorBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.container.ICompressorMenu;
import tfcmt.tfcitembarrelsandmore.common.items.AirHoseItem;

public class AirCompressorContainer
extends BlockEntityContainer<AirCompressorBlockEntity>
implements ICompressorMenu {
    private static final int HOSE_SLOT_X = 132;
    private static final int HOSE_SLOT_Y = 10;
    private static final int HOSE_SLOT_COLS = 2;
    private static final int HOSE_SLOT_PITCH = 17;
    private int optimisticBleederRate = -1;

    public static AirCompressorContainer create(AirCompressorBlockEntity be, Inventory playerInv, int windowId) {
        return (AirCompressorContainer)new AirCompressorContainer(windowId, playerInv, be).init(playerInv, 0);
    }

    public AirCompressorContainer(int windowId, Inventory playerInv, AirCompressorBlockEntity be) {
        super((MenuType)TIBAMContainerTypes.AIR_COMPRESSOR.get(), windowId, (InventoryBlockEntity)be);
    }

    protected void addContainerSlots() {
        for (int i = 0; i < 6; ++i) {
            int col = i % 2;
            int row = i / 2;
            this.addSlot((Slot)new SlotItemHandler((IItemHandler)((AirCompressorBlockEntity)this.blockEntity).getInventory(), i, 132 + col * 17, 10 + row * 17));
        }
        this.addDataSlots((ContainerData)((AirCompressorBlockEntity)this.blockEntity).getSyncableData());
    }

    protected boolean moveStack(ItemStack stack, int slotIndex) {
        if (slotIndex < this.containerSlots) {
            return this.moveItemStackTo(stack, this.containerSlots, this.slots.size(), true);
        }
        if (stack.getItem() instanceof AirHoseItem) {
            return this.moveItemStackTo(stack, 0, 6, false);
        }
        return true;
    }

    @Override
    public int getCompressedAir() {
        return ((AirCompressorBlockEntity)this.blockEntity).getCompressedAir();
    }

    @Override
    public int getMaxAir() {
        return ((AirCompressorBlockEntity)this.blockEntity).getMaxAir();
    }

    @Override
    public int getPsi() {
        return ((AirCompressorBlockEntity)this.blockEntity).getPsi();
    }

    @Override
    public float getCurrentRpm() {
        return ((AirCompressorBlockEntity)this.blockEntity).getCurrentRpm();
    }

    @Override
    public BlockPos getEnginePos() {
        return ((AirCompressorBlockEntity)this.blockEntity).getBlockPos();
    }

    @Override
    public int getBleederRate() {
        int live = ((AirCompressorBlockEntity)this.blockEntity).getBleederRate();
        if (this.optimisticBleederRate >= 0 && this.optimisticBleederRate != live) {
            return this.optimisticBleederRate;
        }
        this.optimisticBleederRate = -1;
        return live;
    }

    @Override
    public void setOptimisticBleederRate(int v) {
        this.optimisticBleederRate = v;
    }
}

