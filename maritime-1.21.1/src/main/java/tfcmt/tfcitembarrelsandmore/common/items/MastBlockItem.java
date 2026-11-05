package tfcmt.tfcitembarrelsandmore.common.items;

import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.items.SizedBlockItem;

public class MastBlockItem
extends SizedBlockItem {
    private final TIBAMBlocks.Wood wood;

    public MastBlockItem(Block block, Item.Properties properties, TIBAMBlocks.Wood wood, Size size, Weight weight) {
        super(block, properties, size, weight);
        this.wood = wood;
    }

    public TIBAMBlocks.Wood getWood() {
        return this.wood;
    }
}

