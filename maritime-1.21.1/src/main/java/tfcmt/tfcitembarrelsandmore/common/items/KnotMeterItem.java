package tfcmt.tfcitembarrelsandmore.common.items;

import java.util.List;
import javax.annotation.Nullable;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.items.SizedItem;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.PropellerListenerSet;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.PropellerPhysicsListener;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.WaterPaddleListenerSet;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.WaterPaddlePhysicsListener;

public class KnotMeterItem
extends SizedItem {
    private static final double KNOTS_PER_MPS = 1.9438444924406;
    private static final double UNDERPOWERED_THRESHOLD = 0.9;
    private static final double OVERPOWERED_THRESHOLD = 1.15;

    public KnotMeterItem() {
        super(Size.SMALL, Weight.LIGHT);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.displayClientMessage(KnotMeterItem.buildReading(level, player), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, (boolean)level.isClientSide);
    }

    private static Component buildReading(Level level, Player player) {
        if (!VSCompat.isLoaded()) {
            return Component.translatable((String)"message.tfc_item_barrels_and_more.knot_meter.no_vessel").withStyle(ChatFormatting.GRAY);
        }
        LoadedServerShip ship = VSCompat.getShipAt(level, player.blockPosition());
        if (ship == null) {
            return Component.translatable((String)"message.tfc_item_barrels_and_more.knot_meter.no_vessel").withStyle(ChatFormatting.GRAY);
        }
        Vector3dc velocity = ship.getVelocity();
        double speedMps = Math.sqrt(velocity.x() * velocity.x() + velocity.z() * velocity.z());
        double speedKnots = speedMps * 1.9438444924406;
        double installedRatedTonnage = KnotMeterItem.installedRatedTonnage(ship);
        if (installedRatedTonnage <= 0.0) {
            return Component.translatable((String)"message.tfc_item_barrels_and_more.knot_meter.no_propulsion", (Object[])new Object[]{String.format("%.1f", speedKnots)}).withStyle(ChatFormatting.AQUA);
        }
        double massTons = KnotMeterItem.massTonsFrom(ship);
        double capMps = (Double)TIBAMConfig.COMMON.maxShipSpeedMps.get();
        double ratio = massTons > 0.0 ? installedRatedTonnage / massTons : 0.0;
        double shipTopSpeedMps = capMps * Math.min(1.0, ratio);
        double shipTopSpeedKnots = shipTopSpeedMps * 1.9438444924406;
        String verdictKey = KnotMeterItem.verdictKeyFor(ratio);
        return Component.translatable((String)"message.tfc_item_barrels_and_more.knot_meter.reading", (Object[])new Object[]{String.format("%.1f", speedKnots), String.format("%.1f", shipTopSpeedKnots), Component.translatable((String)("message.tfc_item_barrels_and_more.knot_meter." + verdictKey))}).withStyle(ChatFormatting.AQUA);
    }

    private static double installedRatedTonnage(LoadedServerShip ship) {
        WaterPaddleListenerSet paddles;
        double total = 0.0;
        PropellerListenerSet propellers = (PropellerListenerSet)ship.getAttachment(PropellerListenerSet.class);
        if (propellers != null) {
            for (PropellerPhysicsListener listener : propellers.values()) {
                total += (listener.getType() == PropellerPhysicsListener.PropellerType.BRASS ? (Double)TIBAMConfig.COMMON.brassRatedTonnage.get() : (Double)TIBAMConfig.COMMON.copperRatedTonnage.get()).doubleValue();
            }
        }
        if ((paddles = (WaterPaddleListenerSet)ship.getAttachment(WaterPaddleListenerSet.class)) != null) {
            double paddleRated = (Double)TIBAMConfig.COMMON.paddleRatedTonnage.get();
            for (WaterPaddlePhysicsListener listener : paddles.values()) {
                total += paddleRated * listener.getThrustMultiplier();
            }
        }
        return total;
    }

    private static double massTonsFrom(LoadedServerShip ship) {
        return ship.getInertiaData().getMass() / 1000.0;
    }

    private static String verdictKeyFor(double ratio) {
        if (ratio < 0.9) {
            return "underpowered";
        }
        if (ratio < 1.15) {
            return "well_powered";
        }
        return "overpowered";
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Level level = context.level();
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add((Component)Component.translatable((String)"tooltip.tfc_item_barrels_and_more.knot_meter.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add((Component)Component.translatable((String)"tooltip.tfc_item_barrels_and_more.knot_meter.unit").withStyle(ChatFormatting.DARK_GRAY));
    }
}

