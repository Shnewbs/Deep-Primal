package tfcmt.tfcitembarrelsandmore.common.items;

import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.world.item.Item;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.items.SizedItem;

public class BoomItem
extends SizedItem {
    private final TIBAMBlocks.Wood wood;

    public BoomItem(Item.Properties properties, TIBAMBlocks.Wood wood, Size size, Weight weight) {
        super(properties, size, weight);
        this.wood = wood;
    }

    public TIBAMBlocks.Wood getWood() {
        return this.wood;
    }
}

