package tfcmt.tfcitembarrelsandmore.common.items;

import java.util.List;
import javax.annotation.Nullable;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import tfcmt.tfcitembarrelsandmore.common.entities.ThrownSoundingLineEntity;
import tfcmt.tfcitembarrelsandmore.common.items.SizedItem;

public class SoundingLineItem
extends SizedItem {
    public SoundingLineItem() {
        super(new Item.Properties().stacksTo(1), Size.SMALL, Weight.LIGHT);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
        if (!level.isClientSide) {
            ThrownSoundingLineEntity line = new ThrownSoundingLineEntity(level, player);
            line.setItem(stack);
            line.shootFromRotation((Entity)player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            level.addFreshEntity((Entity)line);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, (boolean)level.isClientSide);
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add((Component)Component.translatable((String)"tooltip.tfc_item_barrels_and_more.sounding_line.desc").withStyle(ChatFormatting.GRAY));
    }
}

