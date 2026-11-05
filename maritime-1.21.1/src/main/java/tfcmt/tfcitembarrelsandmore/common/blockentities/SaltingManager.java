package tfcmt.tfcitembarrelsandmore.common.blockentities;

import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodTrait;
import net.dries007.tfc.common.component.food.FoodTraits;
import net.dries007.tfc.common.component.food.IFood;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;

public class SaltingManager {
    private static final ResourceLocation ITEM_SALT = ResourceLocation.fromNamespaceAndPath("tfc", "powder/salt");
    private static final TagKey<Item> TAG_MEATS = TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("tfc", "foods/meats"));

    public static void onSeal(InventoryItemHandler inv) {
        for (int i = 0; i < inv.getSlots(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty() || !FoodCapability.has((ItemStack)stack) || FoodCapability.hasTrait((ItemStack)stack, FoodTraits.PRESERVED)) continue;
            FoodCapability.applyTrait((ItemStack)stack, FoodTraits.PRESERVED);
            inv.setStackInSlot(i, stack);
        }
    }

    public static void onUnseal(InventoryItemHandler inv) {
        for (int i = 0; i < inv.getSlots(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty() || !FoodCapability.has((ItemStack)stack) || !FoodCapability.hasTrait((ItemStack)stack, FoodTraits.PRESERVED)) continue;
            FoodCapability.removeTrait((ItemStack)stack, FoodTraits.PRESERVED);
            inv.setStackInSlot(i, stack);
        }
    }

    public static int getInterval() {
        return (Integer)TIBAMConfig.SERVER.saltConsumptionInterval.get();
    }

    public static boolean runSaltMaintenance(InventoryItemHandler inv, int interval) {
        int saltToConsume;
        if (!SaltingManager.hasSaltedMeatAndSaltOnly(inv)) {
            return false;
        }
        int saltableStacks = SaltingManager.countSaltableStacks(inv);
        int saltStacks = SaltingManager.countSaltStacks(inv);
        int totalSalt = SaltingManager.countTotalSalt(inv);
        if (saltableStacks == 0 || totalSalt == 0) {
            return false;
        }
        int n = saltToConsume = saltStacks >= saltableStacks ? saltableStacks : Math.min(3 * saltableStacks, totalSalt);
        if (saltToConsume <= 0) {
            return false;
        }
        SaltingManager.consumeSalt(inv, saltToConsume);
        SaltingManager.preserveWithSalt(inv, interval);
        return true;
    }

    private static void preserveWithSalt(InventoryItemHandler inv, int interval) {
        long currentTime = Calendars.get().getTicks();
        for (int i = 0; i < inv.getSlots(); ++i) {
            IFood food;
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty() || !SaltingManager.isSaltedMeat(stack) || !FoodCapability.hasTrait((ItemStack)stack, FoodTraits.SALTED) || (food = FoodCapability.get((ItemStack)stack)) == null) continue;
            long creation = food.getCreationDate();
            long newCreation = creation + (long)interval;
            FoodCapability.setCreationDate((ItemStack)stack, (long)newCreation);
        }
    }

    private static boolean hasSaltedMeatAndSaltOnly(InventoryItemHandler inv) {
        boolean hasMeat = false;
        boolean hasSalt = false;
        for (int i = 0; i < inv.getSlots(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            boolean meat = SaltingManager.isSaltedMeat(stack);
            boolean salt = SaltingManager.isSalt(stack);
            if (meat) {
                hasMeat = true;
                continue;
            }
            if (salt) {
                hasSalt = true;
                continue;
            }
            return false;
        }
        return hasMeat && hasSalt;
    }

    private static boolean isSaltedMeat(ItemStack stack) {
        return stack.is(TAG_MEATS);
    }

    private static boolean isSalt(ItemStack stack) {
        Item saltItem = (Item)BuiltInRegistries.ITEM.get(ITEM_SALT);
        return saltItem != null && stack.is(saltItem);
    }

    private static int countSaltableStacks(InventoryItemHandler inv) {
        int count = 0;
        for (int i = 0; i < inv.getSlots(); ++i) {
            if (inv.getStackInSlot(i).isEmpty() || !SaltingManager.isSaltedMeat(inv.getStackInSlot(i))) continue;
            ++count;
        }
        return count;
    }

    private static int countSaltStacks(InventoryItemHandler inv) {
        int count = 0;
        for (int i = 0; i < inv.getSlots(); ++i) {
            if (inv.getStackInSlot(i).isEmpty() || !SaltingManager.isSalt(inv.getStackInSlot(i))) continue;
            ++count;
        }
        return count;
    }

    private static int countTotalSalt(InventoryItemHandler inv) {
        int total = 0;
        for (int i = 0; i < inv.getSlots(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty() || !SaltingManager.isSalt(stack)) continue;
            total += stack.getCount();
        }
        return total;
    }

    private static void consumeSalt(InventoryItemHandler inv, int amount) {
        int remaining = amount;
        for (int i = 0; i < inv.getSlots() && remaining > 0; ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty() || !SaltingManager.isSalt(stack)) continue;
            int remove = Math.min(remaining, stack.getCount());
            stack.shrink(remove);
            inv.setStackInSlot(i, stack);
            remaining -= remove;
        }
    }
}

