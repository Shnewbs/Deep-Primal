package tfcmt.tfcitembarrelsandmore.common.blocks.mast;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastFillerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.items.MastBlockItem;

public final class MastGrowth {
    public static final int ITEMS_PER_GROWTH = 1;

    private MastGrowth() {
    }

    public static InteractionResult tryGrow(Level level, BlockPos anchorPos, Player player, ItemStack held) {
        MastBlockItem mastItem;
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(anchorPos);
        if (!(blockEntity instanceof MastBlockEntity)) {
            return InteractionResult.PASS;
        }
        MastBlockEntity mastBE = (MastBlockEntity)blockEntity;
        Item item = held.getItem();
        if (!(item instanceof MastBlockItem) || (mastItem = (MastBlockItem)item).getWood() != mastBE.getWood()) {
            return InteractionResult.PASS;
        }
        if (!mastBE.canGrow()) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.mast.max_height"), true);
            return InteractionResult.FAIL;
        }
        BlockPos cellPos = anchorPos.above(mastBE.getTotalHeight());
        if (!level.getBlockState(cellPos).isAir()) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.mast.obstructed"), true);
            return InteractionResult.FAIL;
        }
        Item item2 = held.getItem();
        if (!player.getAbilities().instabuild && !MastGrowth.hasEnough(player, item2, 1)) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.mast.not_enough_items", (Object[])new Object[]{1}), true);
            return InteractionResult.FAIL;
        }
        int newOffset = mastBE.grow();
        if (!player.getAbilities().instabuild) {
            MastGrowth.consume(player, item2, 1);
        }
        level.setBlock(cellPos, ((Block)TIBAMBlocks.MAST_FILLER.get()).defaultBlockState(), 3);
        BlockEntity blockEntity2 = level.getBlockEntity(cellPos);
        if (blockEntity2 instanceof MastFillerBlockEntity) {
            MastFillerBlockEntity filler = (MastFillerBlockEntity)blockEntity2;
            filler.init(newOffset);
        }
        return InteractionResult.SUCCESS;
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
        remaining = MastGrowth.drain((List<ItemStack>)player.getInventory().items, item, remaining);
        MastGrowth.drain((List<ItemStack>)player.getInventory().offhand, item, remaining);
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

