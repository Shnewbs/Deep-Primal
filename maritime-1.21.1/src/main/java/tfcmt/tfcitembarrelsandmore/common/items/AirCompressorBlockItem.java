package tfcmt.tfcitembarrelsandmore.common.items;

import java.util.List;
import javax.annotation.Nullable;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.items.SizedBlockItem;

public class AirCompressorBlockItem
extends SizedBlockItem {
    private static final String NBT_COMPRESSED_AIR = "CompressedAir";

    public AirCompressorBlockItem(Block block, Item.Properties properties, Size size, Weight weight) {
        super(block, properties, size, weight);
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (!tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.has(stack) || !tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).contains(NBT_COMPRESSED_AIR)) {
            return;
        }
        int storedAir = tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).getInt(NBT_COMPRESSED_AIR);
        int maxAir = (Integer)TIBAMConfig.SERVER.compressorMaxAir.get();
        int psi = maxAir > 0 ? Math.min(100, storedAir * 100 / maxAir) : 0;
        tooltip.add((Component)Component.literal((String)("Compressed air: " + storedAir + " (" + psi + " PSI)")).withStyle(ChatFormatting.GRAY));
    }
}

