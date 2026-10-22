package tfcmt.tfcitembarrelsandmore.common.curios;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CuriosCompat {
    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        final ItemStack stack = (ItemStack)event.getObject();
        boolean isItemBarrel = TIBAMBlocks.ITEM_BARRELS.values().stream().anyMatch(ro -> ((Block)ro.get()).asItem() == stack.getItem());
        if (!isItemBarrel) {
            return;
        }
        final LazyOptional lazyOptional = LazyOptional.of(() -> new ICurio(){

            public ItemStack getStack() {
                return stack;
            }

            public boolean canEquip(SlotContext slotContext) {
                if (!slotContext.identifier().equals("back")) {
                    return false;
                }
                CompoundTag tag = tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack);
                if (tag == null) {
                    return false;
                }
                if (!tag.contains("BlockEntityTag")) {
                    return false;
                }
                return tag.getCompound("BlockEntityTag").getBoolean("Sealed");
            }
        });
        event.addCapability(ResourceLocation.fromNamespaceAndPath("tfc_item_barrels_and_more", "curio_barrel"), new ICapabilityProvider(){

            @Nonnull
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return CuriosCapability.ITEM.orEmpty(cap, lazyOptional);
            }
        });
    }
}

