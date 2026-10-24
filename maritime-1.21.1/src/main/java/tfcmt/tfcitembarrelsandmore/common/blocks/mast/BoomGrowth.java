package tfcmt.tfcitembarrelsandmore.common.blocks.mast;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.TIBAMItems;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomFillerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomHost;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastFillerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigSlot;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.RigLinking;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.YardGrowth;
import tfcmt.tfcitembarrelsandmore.common.items.BoomItem;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public final class BoomGrowth {
    public static final int ITEMS_PER_GROWTH = 1;

    private BoomGrowth() {
    }

    public static InteractionResult tryGrow(Level level, BlockPos hostPos, Player player, ItemStack held, BlockHitResult hit) {
        Direction face;
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(hostPos);
        if (!(blockEntity instanceof BoomHost)) {
            return InteractionResult.PASS;
        }
        BoomHost host = (BoomHost)blockEntity;
        Item item = held.getItem();
        if (!(item instanceof BoomItem)) {
            return InteractionResult.PASS;
        }
        BoomItem boomItem = (BoomItem)item;
        int totalHeight = BoomGrowth.mastTotalHeightAt(level, hostPos);
        if (totalHeight <= 0) {
            return InteractionResult.PASS;
        }
        if (BoomGrowth.mastRakeStepsAt(level, hostPos) != 0) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.boom.mast_raked"), true);
            return InteractionResult.FAIL;
        }
        if (host.hasBoom()) {
            if (boomItem.getWood() != host.getBoomWood()) {
                return InteractionResult.PASS;
            }
            face = host.getBoomFace();
        } else {
            Direction clicked = hit.getDirection();
            if (clicked.getAxis() == Direction.Axis.Y) {
                return InteractionResult.PASS;
            }
            face = clicked;
        }
        int newLength = host.getBoomLength() + 1;
        if (newLength > YardGrowth.maxArmLength(totalHeight)) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.boom.max_length"), true);
            return InteractionResult.FAIL;
        }
        Item item2 = held.getItem();
        if (!player.getAbilities().instabuild && !BoomGrowth.hasEnough(player, item2, 1)) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.boom.not_enough_items", (Object[])new Object[]{1}), true);
            return InteractionResult.FAIL;
        }
        BlockPos armPos = hostPos.relative(face, newLength);
        if (!level.getBlockState(armPos).isAir()) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.boom.obstructed"), true);
            return InteractionResult.FAIL;
        }
        if (!player.getAbilities().instabuild) {
            BoomGrowth.consume(player, item2, 1);
        }
        if (!host.hasBoom()) {
            host.initBoom(face, 0, boomItem.getWood());
        }
        host.setBoomLength(newLength);
        BoomGrowth.placeArmBlock(level, hostPos, face, newLength);
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult tryRotate(BoomHost host, RigSlot slot, Player player) {
        if (!host.hasBoom()) {
            return InteractionResult.PASS;
        }
        if (slot != RigSlot.BOOM_END && slot != RigSlot.BOOM_END_PORT) {
            throw new IllegalArgumentException("tryRotate() only makes sense for BOOM_END/BOOM_END_PORT, got " + String.valueOf((Object)slot));
        }
        int direction = slot == RigSlot.BOOM_END ? 1 : -1;
        int next = host.getBoomYawSteps() + direction;
        if (next > 4 || next < -4) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.boom.max_angle"), true);
            return InteractionResult.FAIL;
        }
        host.setBoomYawSteps(next);
        return InteractionResult.SUCCESS;
    }

    private static void placeArmBlock(Level level, BlockPos hostPos, Direction face, int distance) {
        BlockPos armPos = hostPos.relative(face, distance);
        level.setBlock(armPos, ((Block)TIBAMBlocks.BOOM_FILLER.get()).defaultBlockState(), 3);
        BlockEntity blockEntity = level.getBlockEntity(armPos);
        if (blockEntity instanceof BoomFillerBlockEntity) {
            BoomFillerBlockEntity filler = (BoomFillerBlockEntity)blockEntity;
            filler.init(face, distance);
        }
    }

    private static int mastTotalHeightAt(Level level, BlockPos hostPos) {
        MastFillerBlockEntity fillerBE;
        BlockEntity blockEntity = level.getBlockEntity(hostPos);
        if (blockEntity instanceof MastBlockEntity) {
            MastBlockEntity mastBE = (MastBlockEntity)blockEntity;
            return mastBE.getTotalHeight();
        }
        BlockEntity blockEntity2 = level.getBlockEntity(hostPos);
        if (blockEntity2 instanceof MastFillerBlockEntity && (blockEntity2 = level.getBlockEntity((fillerBE = (MastFillerBlockEntity)blockEntity2).anchorPos())) instanceof MastBlockEntity) {
            MastBlockEntity anchorBE = (MastBlockEntity)blockEntity2;
            return anchorBE.getTotalHeight();
        }
        return 0;
    }

    private static int mastRakeStepsAt(Level level, BlockPos hostPos) {
        MastFillerBlockEntity fillerBE;
        BlockEntity blockEntity = level.getBlockEntity(hostPos);
        if (blockEntity instanceof MastBlockEntity) {
            MastBlockEntity mastBE = (MastBlockEntity)blockEntity;
            return mastBE.getRakeSteps();
        }
        BlockEntity blockEntity2 = level.getBlockEntity(hostPos);
        if (blockEntity2 instanceof MastFillerBlockEntity && (blockEntity2 = level.getBlockEntity((fillerBE = (MastFillerBlockEntity)blockEntity2).anchorPos())) instanceof MastBlockEntity) {
            MastBlockEntity anchorBE = (MastBlockEntity)blockEntity2;
            return anchorBE.getRakeSteps();
        }
        return 0;
    }

    public static void breakBoomIfAny(Level level, BlockPos hostPos, BoomHost host) {
        if (!host.hasBoom()) {
            return;
        }
        if (host instanceof RigLinkable) {
            RigLinkable linkableHost = (RigLinkable)((Object)host);
            RigLinking.clearLinks(linkableHost, RigSlot.BOOM_END);
            RigLinking.clearLinks(linkableHost, RigSlot.BOOM_END_PORT);
        }
        Direction face = host.getBoomFace();
        int length = host.getBoomLength();
        for (int d = 1; d <= length; ++d) {
            level.removeBlock(hostPos.relative(face, d), false);
        }
    }

    public static void detachBoom(Level level, BlockPos hostPos, BoomHost host, Player player) {
        if (!host.hasBoom()) {
            return;
        }
        int length = host.getBoomLength();
        TIBAMBlocks.Wood wood = host.getBoomWood();
        BoomGrowth.breakBoomIfAny(level, hostPos, host);
        host.clearBoom();
        if (!player.getAbilities().instabuild) {
            int chunk;
            Item item = (Item)TIBAMItems.BOOMS.get((Object)wood).get();
            int maxStack = item.getDefaultInstance().getMaxStackSize();
            Vec3 dropPos = VSCompat.spawnPosToWorld(level, hostPos, (double)hostPos.getX() + 0.5, (double)hostPos.getY() + 0.5, (double)hostPos.getZ() + 0.5);
            for (int remaining = length * 1; remaining > 0; remaining -= chunk) {
                chunk = Math.min(remaining, maxStack);
                ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, new ItemStack((ItemLike)item, chunk));
                entity.setDefaultPickUpDelay();
                level.addFreshEntity((Entity)entity);
            }
        }
    }

    public static void trimOversizedBooms(Level level, BlockPos anchorPos, int newTotalHeight) {
        int cap = YardGrowth.maxArmLength(newTotalHeight);
        for (int offset = 0; offset < newTotalHeight; ++offset) {
            BoomHost host;
            BlockPos cellPos = anchorPos.above(offset);
            BlockEntity blockEntity = level.getBlockEntity(cellPos);
            if (!(blockEntity instanceof BoomHost) || !(host = (BoomHost)blockEntity).hasBoom() || host.getBoomLength() <= cap) continue;
            BoomGrowth.trimBoomTo(level, cellPos, host, cap);
        }
    }

    private static void trimBoomTo(Level level, BlockPos hostPos, BoomHost host, int newLength) {
        Direction face = host.getBoomFace();
        int oldLength = host.getBoomLength();
        for (int d = newLength + 1; d <= oldLength; ++d) {
            level.removeBlock(hostPos.relative(face, d), false);
        }
        if (newLength <= 0) {
            host.clearBoom();
        } else {
            host.setBoomLength(newLength);
        }
    }

    private static boolean hasEnough(Player player, Item item, int amount) {
        int total = 0;
        for (ItemStack s : player.getInventory().items) {
            if (s.getItem() != item) continue;
            total += s.getCount();
        }
        for (ItemStack s : player.getInventory().offhand) {
            if (s.getItem() != item) continue;
            total += s.getCount();
        }
        return total >= amount;
    }

    private static void consume(Player player, Item item, int amount) {
        int remaining = amount;
        remaining = BoomGrowth.drain((List<ItemStack>)player.getInventory().items, item, remaining);
        BoomGrowth.drain((List<ItemStack>)player.getInventory().offhand, item, remaining);
    }

    private static int drain(List<ItemStack> list, Item item, int remaining) {
        for (int i = 0; i < list.size() && remaining > 0; ++i) {
            ItemStack s = list.get(i);
            if (s.getItem() != item) continue;
            int take = Math.min(remaining, s.getCount());
            s.shrink(take);
            remaining -= take;
        }
        return remaining;
    }
}

