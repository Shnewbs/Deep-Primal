package tfcmt.tfcitembarrelsandmore.common.container;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.blocks.devices.SealableDeviceBlock;

import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.slot.CallbackSlot;
import net.dries007.tfc.common.container.Container;
import net.dries007.tfc.common.container.ISlotCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import tfcmt.tfcitembarrelsandmore.common.TIBAMContainerTypes;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ItemBarrelBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ItemBarrelBlock;

public class ItemBarrelContainer
extends BlockEntityContainer<ItemBarrelBlockEntity>
implements ButtonHandlerContainer {
    public static ItemBarrelContainer create(ItemBarrelBlockEntity barrel, Inventory playerInv, int windowId) {
        return (ItemBarrelContainer)new ItemBarrelContainer(windowId, barrel).init(playerInv, 0);
    }

    private ItemBarrelContainer(int windowId, ItemBarrelBlockEntity barrel) {
        super((MenuType)TIBAMContainerTypes.ITEM_BARREL.get(), windowId, barrel);
    }

    public void clicked(int slot, int button, ClickType clickType, Player player) {
        boolean isBarrelSlot = slot >= 0 && slot < 12;
        boolean isSealed = (Boolean)((ItemBarrelBlockEntity)this.blockEntity).getBlockState().getValue((Property)SealableDeviceBlock.SEALED);
        if (isBarrelSlot && isSealed) {
            return;
        }
        super.clicked(slot, button, clickType, player);
    }

    public void onButtonPress(int buttonID, @Nullable CompoundTag extraNBT) {
        Level level = ((ItemBarrelBlockEntity)this.blockEntity).getLevel();
        if (level != null) {
            ItemBarrelBlock.toggleSeal(level, ((ItemBarrelBlockEntity)this.blockEntity).getBlockPos(), ((ItemBarrelBlockEntity)this.blockEntity).getBlockState());
        }
    }

    protected boolean moveStack(ItemStack stack, int slotIndex) {
        if (((Boolean)((ItemBarrelBlockEntity)this.blockEntity).getBlockState().getValue((Property)SealableDeviceBlock.SEALED)).booleanValue()) {
            return true;
        }
        return switch (this.typeOf(slotIndex)) {
            case Container.IndexType.MAIN_INVENTORY, Container.IndexType.HOTBAR -> {
                if (!this.moveItemStackTo(stack, 0, 12, false)) {
                    yield true;
                }
                yield false;
            }
            case Container.IndexType.CONTAINER -> {
                if (!this.moveItemStackTo(stack, this.containerSlots, this.slots.size(), false)) {
                    yield true;
                }
                yield false;
            }
            default -> throw new IncompatibleClassChangeError();
        };
    }

    protected void addContainerSlots() {
        var inventory = this.blockEntity.getInventory();
        {
            for (int y = 0; y < 3; ++y) {
                for (int x = 0; x < 4; ++x) {
                    this.addSlot((Slot)new CallbackSlot((ISlotCallback)this.blockEntity, inventory, y * 4 + x, 25 + x * 18, 19 + y * 18));
                }
            }
        }
    }
}

