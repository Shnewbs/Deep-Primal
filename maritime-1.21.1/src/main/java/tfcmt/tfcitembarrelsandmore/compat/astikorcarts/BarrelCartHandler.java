package tfcmt.tfcitembarrelsandmore.compat.astikorcarts;

import net.dries007.tfc.common.blocks.devices.BarrelBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

import tfcmt.tfcitembarrelsandmore.common.blocks.devices.ItemBarrelBlock;

public class BarrelCartHandler {
    private static final int SLOTS = 4;

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity entity = event.getTarget();
        if (!entity.getClass().getName().equals("tfcastikorcarts.common.entities.carts.TFCSupplyCartEntity")) {
            return;
        }
        Object cart = entity;
        var inventory = inventory(cart);
        if (inventory == null || inventory.getSlots() < 4) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            return;
        }
        Player player = event.getEntity();
        if (!player.isShiftKeyDown()) {
            if (BarrelCartHandler.isBarrelMode(cart)) {
                event.setCanceled(true);
            }
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (BarrelCartHandler.isSealed(held)) {
            for (int i = 0; i < 4; ++i) {
                if (!inventory.getStackInSlot(i).isEmpty()) continue;
                inventory.setStackInSlot(i, held.copyWithCount(1));
                held.shrink(1);
                event.setCanceled(true);
                return;
            }
            event.setCanceled(true);
            return;
        }
        if (held.isEmpty() && BarrelCartHandler.isBarrelMode(cart)) {
            int slot = -1;
            for (int i = 3; i >= 0; --i) {
                if (inventory.getStackInSlot(i).isEmpty()) continue;
                slot = i;
                break;
            }
            if (slot == -1) {
                return;
            }
            ItemStack returned = inventory.extractItem(slot, 1, false);
            if (!player.getInventory().add(returned)) player.drop(returned, false);
            event.setCanceled(true);
        } else if (BarrelCartHandler.isBarrelMode(cart)) {
            event.setCanceled(true);
        }
    }

    public static boolean isBarrelMode(Object cart) {
        var inventory = inventory(cart);
        if (inventory == null || inventory.getSlots() < 4) return false;
        for (int i = 0; i < 4; ++i) {
            if (!BarrelCartHandler.isSealed(inventory.getStackInSlot(i))) continue;
            return true;
        }
        return false;
    }

    public static boolean isSealed(ItemStack stack) {
        BlockItem bi2;
        boolean isTIBAM;
        BlockItem bi;
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        boolean isTFC = item instanceof BlockItem && (bi = (BlockItem)item).getBlock() instanceof BarrelBlock;
        boolean bl = isTIBAM = item instanceof BlockItem && (bi2 = (BlockItem)item).getBlock() instanceof ItemBarrelBlock;
        if (!isTFC && !isTIBAM) {
            return false;
        }
        if (isTFC) return stack.getOrDefault(net.dries007.tfc.common.component.TFCComponents.BARREL.get(), net.dries007.tfc.common.component.block.BarrelComponent.EMPTY).sealedTick() != -1L;
        CompoundTag bet = tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).getCompound("BlockEntityTag");
        if (bet == null) {
            return false;
        }
        return isTFC ? bet.getLong("sealedTick") != 0L : bet.getBoolean("Sealed");
    }
    private static final java.util.Map<Class<?>, java.util.Optional<java.lang.reflect.Field>> INVENTORIES = new java.util.concurrent.ConcurrentHashMap<>();
    public static net.neoforged.neoforge.items.IItemHandlerModifiable inventory(Object cart) {
        var field = INVENTORIES.computeIfAbsent(cart.getClass(), type -> {
            try { return java.util.Optional.of(type.getField("inventory")); }
            catch (ReflectiveOperationException error) { return java.util.Optional.empty(); }
        });
        if (field.isEmpty()) return null;
        try {
            Object value = field.get().get(cart);
            return value instanceof net.neoforged.neoforge.items.IItemHandlerModifiable inventory ? inventory : null;
        } catch (IllegalAccessException error) { return null; }
    }
}

