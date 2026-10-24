package tfcmt.tfcitembarrelsandmore.common.curios;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.MaritimeItemData;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public final class CuriosCompat {
    private CuriosCompat() {}
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        Item[] barrels = TIBAMBlocks.ITEM_BARRELS.values().stream().map(entry -> entry.get().asItem()).toArray(Item[]::new);
        event.registerItem(CuriosCapability.ITEM, (stack, context) -> new ICurio() {
            @Override public ItemStack getStack() { return stack; }
            @Override public boolean canEquip(SlotContext slot) {
                return slot.identifier().equals("back") && MaritimeItemData.get(stack).getCompound("BlockEntityTag").getBoolean("Sealed");
            }
        }, barrels);
    }
}
