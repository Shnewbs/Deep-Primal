package tfcmt.tfcitembarrelsandmore.common.container;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.util.data.Fuel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import tfcmt.tfcitembarrelsandmore.common.TIBAMContainerTypes;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerBlock;

public class BoilerContainer
extends BlockEntityContainer<BoilerBlockEntity> {
    public static final int FUEL1_X = 53;
    public static final int FUEL1_Y = 20;
    public static final int FUEL2_X = 71;
    public static final int FUEL2_Y = 20;
    public static final int FUEL3_X = 89;
    public static final int FUEL3_Y = 20;
    public static final int ASH_X = 71;
    public static final int ASH_Y = 44;
    public static final int TEMP_BAR_X = 8;
    public static final int TEMP_BAR_Y = 8;
    public static final int TEMP_BAR_WIDTH = 12;
    public static final int TEMP_BAR_HEIGHT = 60;

    public static BoilerContainer create(BoilerBlockEntity boiler, Inventory playerInv, int windowId) {
        return (BoilerContainer)new BoilerContainer(windowId, playerInv, boiler).init(playerInv, 0);
    }

    public BoilerContainer(int windowId, Inventory playerInv, BoilerBlockEntity boiler) {
        super((MenuType)TIBAMContainerTypes.BOILER.get(), windowId, (InventoryBlockEntity)boiler);
    }

    protected void addContainerSlots() {
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((BoilerBlockEntity)this.blockEntity).getInventory(), 0, 53, 20){

            public boolean mayPlace(ItemStack stack) {
                return Fuel.get((ItemStack)stack) != null;
            }
        });
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((BoilerBlockEntity)this.blockEntity).getInventory(), 1, 71, 20){

            public boolean mayPlace(ItemStack stack) {
                return Fuel.get((ItemStack)stack) != null;
            }
        });
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((BoilerBlockEntity)this.blockEntity).getInventory(), 2, 89, 20){

            public boolean mayPlace(ItemStack stack) {
                return Fuel.get((ItemStack)stack) != null;
            }
        });
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)((BoilerBlockEntity)this.blockEntity).getInventory(), 3, 71, 44){

            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addDataSlots((ContainerData)((BoilerBlockEntity)this.blockEntity).getSyncableData());
    }

    protected boolean moveStack(ItemStack stack, int slotIndex) {
        if (slotIndex < this.containerSlots) {
            return this.moveItemStackTo(stack, this.containerSlots, this.slots.size(), true);
        }
        if (Fuel.get((ItemStack)stack) != null) {
            return this.moveItemStackTo(stack, 0, 3, false);
        }
        return true;
    }

    public float getTemperature() {
        return ((BoilerBlockEntity)this.blockEntity).getTemperature();
    }

    public int getAshCount() {
        return ((BoilerBlockEntity)this.blockEntity).getAshCount();
    }

    public boolean isLit() {
        return ((BoilerBlockEntity)this.blockEntity).isLit();
    }

    public boolean isVentBlocked() {
        Level level = ((BoilerBlockEntity)this.blockEntity).getLevel();
        if (level == null) {
            return false;
        }
        return !BoilerBlock.hasOpenVent(level, ((BoilerBlockEntity)this.blockEntity).getBlockPos());
    }
}

