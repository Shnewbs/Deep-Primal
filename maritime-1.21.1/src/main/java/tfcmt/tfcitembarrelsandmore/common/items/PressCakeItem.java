package tfcmt.tfcitembarrelsandmore.common.items;

import net.dries007.tfc.common.component.size.IItemSize;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PressCakeItem
extends Item
implements IItemSize {
    public PressCakeItem() {
        super(new Item.Properties());
    }

    public Size getSize(ItemStack stack) {
        return Size.SMALL;
    }

    public Weight getWeight(ItemStack stack) {
        return Weight.LIGHT;
    }
}

