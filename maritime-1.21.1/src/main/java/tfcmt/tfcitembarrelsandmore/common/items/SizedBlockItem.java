package tfcmt.tfcitembarrelsandmore.common.items;

import net.dries007.tfc.common.component.size.IItemSize;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class SizedBlockItem
extends BlockItem
implements IItemSize {
    private final Size size;
    private final Weight weight;

    public SizedBlockItem(Block block, Item.Properties properties, Size size, Weight weight) {
        super(block, properties);
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

