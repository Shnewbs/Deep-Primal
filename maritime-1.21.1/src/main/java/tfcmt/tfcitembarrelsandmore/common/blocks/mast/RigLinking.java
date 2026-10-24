package tfcmt.tfcitembarrelsandmore.common.blocks.mast;

import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomHost;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLink;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkRegistry;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigSlot;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.YardHost;

public final class RigLinking {
    private static final String TAG_PENDING = "TibamRigPending";
    private static final String TAG_PENDING_UUID = "Uuid";
    private static final String TAG_PENDING_SLOT = "Slot";

    private RigLinking() {
    }

    public static InteractionResult tryLinkOrUnlink(RigLinkable clickedHost, RigSlot clickedSlot, Player player, ItemStack lead) {
        CompoundTag pending;
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            return RigLinking.tryUnlink(clickedHost, clickedSlot, player);
        }
        if (!clickedHost.allowsMultipleLinks(clickedSlot) && !clickedHost.getLinks(clickedSlot).isEmpty()) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.rigging.already_linked"), true);
            return InteractionResult.FAIL;
        }
        CompoundTag compoundTag = pending = tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(lead) != null ? tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(lead).getCompound(TAG_PENDING) : null;
        if (pending == null || !pending.hasUUID(TAG_PENDING_UUID)) {
            return RigLinking.startPending(clickedHost, clickedSlot, player, lead);
        }
        return RigLinking.completePending(clickedHost, clickedSlot, player, lead, pending);
    }

    private static InteractionResult startPending(RigLinkable clickedHost, RigSlot clickedSlot, Player player, ItemStack lead) {
        CompoundTag pending = new CompoundTag();
        pending.putUUID(TAG_PENDING_UUID, clickedHost.getRigUUID());
        pending.putString(TAG_PENDING_SLOT, clickedSlot.name());
        tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(lead, data -> data.put(TAG_PENDING, (Tag)pending));
        player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.rigging.start_set"), true);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult completePending(RigLinkable clickedHost, RigSlot clickedSlot, Player player, ItemStack lead, CompoundTag pending) {
        RigSlot pendingSlot;
        UUID pendingUuid = pending.getUUID(TAG_PENDING_UUID);
        try {
            pendingSlot = RigSlot.valueOf(pending.getString(TAG_PENDING_SLOT));
        }
        catch (IllegalArgumentException e) {
            RigLinking.clearPending(lead);
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.rigging.pending_broken"), true);
            return InteractionResult.FAIL;
        }
        if (pendingUuid.equals(clickedHost.getRigUUID()) && pendingSlot == clickedSlot) {
            RigLinking.clearPending(lead);
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.rigging.cancelled"), true);
            return InteractionResult.SUCCESS;
        }
        RigLinkable pendingHost = RigLinkRegistry.find(pendingUuid);
        if (pendingHost == null) {
            RigLinking.clearPending(lead);
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.rigging.start_point_gone"), true);
            return InteractionResult.FAIL;
        }
        if (!pendingHost.allowsMultipleLinks(pendingSlot) && !pendingHost.getLinks(pendingSlot).isEmpty()) {
            RigLinking.clearPending(lead);
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.rigging.start_point_taken"), true);
            return InteractionResult.FAIL;
        }
        pendingHost.addLink(pendingSlot, new RigLink(clickedHost.getRigUUID(), clickedSlot));
        clickedHost.addLink(clickedSlot, new RigLink(pendingUuid, pendingSlot));
        RigLinking.clearPending(lead);
        lead.shrink(1);
        player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.rigging.linked"), true);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult tryUnlink(RigLinkable clickedHost, RigSlot clickedSlot, Player player) {
        if (clickedHost.getLinks(clickedSlot).isEmpty()) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.rigging.not_linked"), true);
            return InteractionResult.FAIL;
        }
        RigLinking.clearLinks(clickedHost, clickedSlot);
        player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.rigging.unlinked"), true);
        return InteractionResult.SUCCESS;
    }

    public static void clearLinks(RigLinkable host, RigSlot slot) {
        for (RigLink link : List.copyOf(host.getLinks(slot))) {
            RigLinkable other = RigLinkRegistry.find(link.targetUuid());
            if (other != null) {
                RigLink reciprocal = new RigLink(host.getRigUUID(), slot);
                if (other.getLinks(link.targetSlot()).contains(reciprocal)) {
                    other.removeLink(link.targetSlot(), reciprocal);
                    RigLinking.resetYawIfRotationSlot(other, link.targetSlot());
                }
            }
            host.removeLink(slot, link);
        }
        RigLinking.resetYawIfRotationSlot(host, slot);
    }

    private static void resetYawIfRotationSlot(RigLinkable linkable, RigSlot slot) {
        BoomHost boomHost;
        if (slot == RigSlot.YARD_LEFT || slot == RigSlot.YARD_RIGHT) {
            YardHost yardHost;
            if (linkable instanceof YardHost && (yardHost = (YardHost)((Object)linkable)).hasYard()) {
                yardHost.setYawSteps(0);
            }
        } else if ((slot == RigSlot.BOOM_END || slot == RigSlot.BOOM_END_PORT) && linkable instanceof BoomHost && (boomHost = (BoomHost)((Object)linkable)).hasBoom()) {
            boomHost.setBoomYawSteps(0);
        }
    }

    private static void clearPending(ItemStack lead) {
        if (tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(lead) != null) {
            tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(lead, data -> data.remove(TAG_PENDING));
        }
    }
}

