package tfcmt.tfcitembarrelsandmore.common.items;

import net.dries007.tfc.common.component.size.IItemSize;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.GrinderBlock;

public class PressScrewItem
extends Item
implements IItemSize {
    private final GrinderBlock.ScrewType screwType;

    public PressScrewItem(int maxDamage, GrinderBlock.ScrewType screwType) {
        super(new Item.Properties().durability(maxDamage));
        this.screwType = screwType;
    }

    public GrinderBlock.ScrewType getScrewType() {
        return this.screwType;
    }

    public Size getSize(ItemStack stack) {
        return Size.SMALL;
    }

    public Weight getWeight(ItemStack stack) {
        return Weight.LIGHT;
    }

    public boolean isValidRepairItem(ItemStack stack, ItemStack repairMaterial) {
        return false;
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}

