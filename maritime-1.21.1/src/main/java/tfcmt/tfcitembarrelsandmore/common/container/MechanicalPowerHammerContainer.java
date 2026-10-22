package tfcmt.tfcitembarrelsandmore.common.container;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import tfcmt.tfcitembarrelsandmore.common.TIBAMContainerTypes;
import tfcmt.tfcitembarrelsandmore.common.blockentities.MechanicalPowerHammerBlockEntity;

public class MechanicalPowerHammerContainer
extends BlockEntityContainer<MechanicalPowerHammerBlockEntity> {
    private static final TagKey<Item> HAMMER_HEADS = TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "hammer_heads"));
    public static final int ANVIL_X = 7;
    public static final int ANVIL_Y = 8;
    public static final int HAMMER_X = 7;
    public static final int HAMMER_Y = 28;
    public static final int DIE_X = 7;
    public static final int DIE_Y = 48;
    public static final int RECIPE_LIST_X = 32;
    public static final int RECIPE_LIST_Y = 10;
    public static final int RECIPE_LIST_W = 133;
    public static final int RECIPE_LIST_H = 58;

    public static MechanicalPowerHammerContainer create(MechanicalPowerHammerBlockEntity be, Inventory playerInv, int windowId) {
        return (MechanicalPowerHammerContainer)new MechanicalPowerHammerContainer(windowId, playerInv, be).init(playerInv, 0);
    }

    public static MechanicalPowerHammerContainer createForType(MenuType<?> menuType, MechanicalPowerHammerBlockEntity be, Inventory playerInv, int windowId) {
        return (MechanicalPowerHammerContainer)new MechanicalPowerHammerContainer(menuType, windowId, playerInv, be).init(playerInv, 0);
    }

    public MechanicalPowerHammerContainer(int windowId, Inventory playerInv, MechanicalPowerHammerBlockEntity be) {
        super((MenuType)TIBAMContainerTypes.MECHANICAL_POWER_HAMMER.get(), windowId, (InventoryBlockEntity)be);
    }

    public MechanicalPowerHammerContainer(MenuType<?> menuType, int windowId, Inventory playerInv, MechanicalPowerHammerBlockEntity be) {
        super(menuType, windowId, (InventoryBlockEntity)be);
    }

    protected void addContainerSlots() {
        ItemStackHandler inv = ((MechanicalPowerHammerBlockEntity)this.blockEntity).getHammerInventory();
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)inv, 0, 7, 8));
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)inv, 1, 7, 28));
        this.addSlot((Slot)new SlotItemHandler((IItemHandler)inv, 2, 7, 48));
    }

    protected boolean moveStack(ItemStack stack, int slotIndex) {
        if (slotIndex < this.containerSlots) {
            return this.moveItemStackTo(stack, this.containerSlots, this.slots.size(), true);
        }
        if (Helpers.isItem((ItemStack)stack, (TagKey)TFCTags.Items.ANVILS)) {
            return this.moveItemStackTo(stack, 0, 1, false);
        }
        if (Helpers.isItem((ItemStack)stack, HAMMER_HEADS)) {
            return this.moveItemStackTo(stack, 1, 2, false);
        }
        return this.moveItemStackTo(stack, 2, 3, false);
    }
}

