package tfcmt.tfcitembarrelsandmore.common.items;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.items.SizedItem;

public class WindGaugeItem
extends SizedItem {
    private static final double CALM_THRESHOLD = 0.05;
    private static final double LIGHT_AIR_THRESHOLD = 0.25;
    private static final double LIGHT_BREEZE_THRESHOLD = 0.45;
    private static final double MODERATE_BREEZE_THRESHOLD = 0.65;
    private static final double STRONG_BREEZE_THRESHOLD = 0.85;
    private static final String[] COMPASS_KEYS = new String[]{"n", "ne", "e", "se", "s", "sw", "w", "nw"};

    public WindGaugeItem() {
        super(Size.SMALL, Weight.LIGHT);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            player.displayClientMessage(WindGaugeItem.buildReading(level, player), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, (boolean)level.isClientSide);
    }

    private static Component buildReading(Level level, Player player) {
        Vec2 wind = Climate.get(level).getWind(level, player.blockPosition());
        double intensity = wind.length();
        if (intensity < 0.05) {
            return Component.translatable((String)"message.tfc_item_barrels_and_more.wind_gauge.becalmed").withStyle(ChatFormatting.GRAY);
        }
        String directionKey = WindGaugeItem.directionKeyFor(wind);
        String strengthKey = WindGaugeItem.strengthKeyFor(intensity);
        double speed = intensity * (Double)TIBAMConfig.COMMON.windBridgeSpeedScale.get();
        String speedText = String.format(Locale.ROOT, "%.1f", speed);
        return Component.translatable((String)"message.tfc_item_barrels_and_more.wind_gauge.reading", (Object[])new Object[]{Component.translatable((String)("message.tfc_item_barrels_and_more.wind_gauge.dir." + directionKey)), Component.translatable((String)("message.tfc_item_barrels_and_more.wind_gauge." + strengthKey)), speedText}).withStyle(ChatFormatting.AQUA);
    }

    private static String directionKeyFor(Vec2 towardVector) {
        double fromX = -towardVector.x;
        double fromZ = -towardVector.y;
        double bearing = Math.toDegrees(Math.atan2(fromX, -fromZ));
        if (bearing < 0.0) {
            bearing += 360.0;
        }
        int index = (int)Math.round(bearing / 45.0) % 8;
        return COMPASS_KEYS[index];
    }

    private static String strengthKeyFor(double intensity) {
        if (intensity < 0.05) {
            return "becalmed";
        }
        if (intensity < 0.25) {
            return "light_air";
        }
        if (intensity < 0.45) {
            return "light_breeze";
        }
        if (intensity < 0.65) {
            return "moderate_breeze";
        }
        if (intensity < 0.85) {
            return "strong_breeze";
        }
        return "gale";
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add((Component)Component.translatable((String)"tooltip.tfc_item_barrels_and_more.wind_gauge.desc").withStyle(ChatFormatting.GRAY));
    }
}

