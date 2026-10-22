package tfcmt.tfcitembarrelsandmore.common.items;

import java.util.List;
import javax.annotation.Nullable;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.items.SizedItem;

public class AirHoseItem
extends SizedItem {
    public AirHoseItem() {
        super(Size.NORMAL, Weight.LIGHT);
    }

    public static int getRange(int hoseCount) {
        if (hoseCount <= 0) {
            return 0;
        }
        int perItem = (Integer)TIBAMConfig.SERVER.compressorHoseRangePerItem.get();
        int maxRange = (Integer)TIBAMConfig.SERVER.compressorHoseMaxRange.get();
        return Math.min(maxRange, perItem * hoseCount);
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add((Component)Component.translatable((String)"tooltip.tfc_item_barrels_and_more.air_hose.range", (Object[])new Object[]{TIBAMConfig.SERVER.compressorHoseRangePerItem.get(), TIBAMConfig.SERVER.compressorHoseMaxRange.get()}).withStyle(ChatFormatting.GRAY));
    }
}

