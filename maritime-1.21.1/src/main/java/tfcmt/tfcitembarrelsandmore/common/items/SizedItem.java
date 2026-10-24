package tfcmt.tfcitembarrelsandmore.common.items;

import net.dries007.tfc.common.component.size.IItemSize;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SizedItem
extends Item
implements IItemSize {
    protected final Size size;
    protected final Weight weight;

    public SizedItem(Size size, Weight weight) {
        super(new Item.Properties());
        this.size = size;
        this.weight = weight;
    }

    protected SizedItem(Item.Properties properties, Size size, Weight weight) {
        super(properties);
        this.size = size;
        this.weight = weight;
    }

    public Size getSize(ItemStack stack) {
        return this.size;
    }

    public Weight getWeight(ItemStack stack) {
        return this.weight;
    }
}

