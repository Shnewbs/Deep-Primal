package tfcmt.tfcitembarrelsandmore.common;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Component-backed metadata access. Reads are copies; every mutation replaces the component. */
public final class MaritimeItemData {
    private MaritimeItemData() {}
    public static boolean has(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA) || stack.has(DataComponents.BLOCK_ENTITY_DATA);
    }
    public static CompoundTag get(ItemStack stack) {
        CompoundTag result = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        var blockEntity = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntity != null) result.put("BlockEntityTag", blockEntity.copyTag());
        return result;
    }
    public static void set(ItemStack stack, CompoundTag tag) {
        CompoundTag custom = tag.copy();
        if (custom.contains("BlockEntityTag", 10)) {
            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(custom.getCompound("BlockEntityTag")));
            custom.remove("BlockEntityTag");
        }
        if (custom.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
    }
    public static void update(ItemStack stack, Consumer<CompoundTag> update) {
        CompoundTag tag = get(stack);
        update.accept(tag);
        set(stack, tag);
    }
}
