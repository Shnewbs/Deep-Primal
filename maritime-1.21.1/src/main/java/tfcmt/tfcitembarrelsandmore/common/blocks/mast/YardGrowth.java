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
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastFillerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigSlot;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.YardFillerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.YardHost;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.RigLinking;
import tfcmt.tfcitembarrelsandmore.common.items.YardItem;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public final class YardGrowth {
    public static final int ITEMS_PER_GROWTH = 2;
    public static final int MAX_YAW_STEPS = 4;
    public static final float DEGREES_PER_YAW_STEP = 20.0f;

    private YardGrowth() {
    }

    public static InteractionResult tryGrow(Level level, BlockPos hostPos, Player player, ItemStack held, BlockHitResult hit) {
        Direction faceBias;
        Direction.Axis axis;
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(hostPos);
        if (!(blockEntity instanceof YardHost)) {
            return InteractionResult.PASS;
        }
        YardHost host = (YardHost)blockEntity;
        Item item = held.getItem();
        if (!(item instanceof YardItem)) {
            return InteractionResult.PASS;
        }
        YardItem yardItem = (YardItem)item;
        int totalHeight = YardGrowth.mastTotalHeightAt(level, hostPos);
        if (totalHeight <= 0) {
            return InteractionResult.PASS;
        }
        if (host.hasYard()) {
            if (yardItem.getWood() != host.getYardWood()) {
                return InteractionResult.PASS;
            }
            axis = host.getYardAxis();
            faceBias = host.getYardFaceBias();
        } else {
            Direction clicked = hit.getDirection();
            if (clicked.getAxis() == Direction.Axis.Y) {
                return InteractionResult.PASS;
            }
            axis = YardGrowth.otherHorizontalAxis(clicked.getAxis());
            faceBias = clicked;
        }
        int newArmLength = host.getYardArmLength() + 1;
        if (newArmLength > YardGrowth.maxArmLength(totalHeight)) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.yard.max_length"), true);
            return InteractionResult.FAIL;
        }
        Item item2 = held.getItem();
        if (!player.getAbilities().instabuild && !YardGrowth.hasEnough(player, item2, 2)) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.yard.not_enough_items", (Object[])new Object[]{2}), true);
            return InteractionResult.FAIL;
        }
        Direction posDir = YardGrowth.positiveDirectionFor(axis);
        BlockPos posArmPos = hostPos.relative(posDir, newArmLength);
        BlockPos negArmPos = hostPos.relative(posDir.getOpposite(), newArmLength);
        if (!level.getBlockState(posArmPos).isAir() || !level.getBlockState(negArmPos).isAir()) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.yard.obstructed"), true);
            return InteractionResult.FAIL;
        }
        if (!player.getAbilities().instabuild) {
            YardGrowth.consume(player, item2, 2);
        }
        if (!host.hasYard()) {
            host.initYard(axis, faceBias, 0, yardItem.getWood());
        }
        host.setYardArmLength(newArmLength);
        YardGrowth.placeArmBlock(level, hostPos, posDir, newArmLength);
        YardGrowth.placeArmBlock(level, hostPos, posDir.getOpposite(), newArmLength);
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult tryRotate(YardHost host, RigSlot slot, Player player) {
        if (!host.hasYard()) {
            return InteractionResult.PASS;
        }
        if (slot != RigSlot.YARD_LEFT && slot != RigSlot.YARD_RIGHT) {
            throw new IllegalArgumentException("tryRotate() only makes sense for YARD_LEFT/YARD_RIGHT, got " + String.valueOf((Object)slot));
        }
        int direction = slot == RigSlot.YARD_RIGHT ? 1 : -1;
        int next = host.getYawSteps() + direction;
        if (next > 4 || next < -4) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.yard.max_angle"), true);
            return InteractionResult.FAIL;
        }
        host.setYawSteps(next);
        return InteractionResult.SUCCESS;
    }

    public static int maxArmLength(int mastTotalHeight) {
        return mastTotalHeight / 2;
    }

    public static Direction positiveDirectionFor(Direction.Axis axis) {
        return axis == Direction.Axis.Z ? Direction.SOUTH : Direction.EAST;
    }

    private static Direction.Axis otherHorizontalAxis(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
    }

    public static double faceOffsetBlocks(YardHost host, int mastWidthPx) {
        if (!host.hasYard()) {
            return 0.0;
        }
        int yardRootWidthPx = MastBlockEntity.widthForOffset(0, host.getYardArmLength() + 1);
        return (double)(mastWidthPx + yardRootWidthPx) / 2.0 / 16.0;
    }

    private static void placeArmBlock(Level level, BlockPos hostPos, Direction dir, int distance) {
        BlockPos armPos = hostPos.relative(dir, distance);
        level.setBlock(armPos, ((Block)TIBAMBlocks.YARD_FILLER.get()).defaultBlockState(), 3);
        BlockEntity blockEntity = level.getBlockEntity(armPos);
        if (blockEntity instanceof YardFillerBlockEntity) {
            YardFillerBlockEntity filler = (YardFillerBlockEntity)blockEntity;
            int signedOffset = dir == YardGrowth.positiveDirectionFor(dir.getAxis()) ? distance : -distance;
            filler.init(dir.getAxis(), signedOffset);
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

    public static void breakYardIfAny(Level level, BlockPos hostPos, YardHost host) {
        if (!host.hasYard()) {
            return;
        }
        if (host instanceof RigLinkable) {
            RigLinkable linkableHost = (RigLinkable)((Object)host);
            RigLinking.clearLinks(linkableHost, RigSlot.YARD_LEFT);
            RigLinking.clearLinks(linkableHost, RigSlot.YARD_RIGHT);
        }
        Direction posDir = YardGrowth.positiveDirectionFor(host.getYardAxis());
        int arm = host.getYardArmLength();
        for (int d = 1; d <= arm; ++d) {
            level.removeBlock(hostPos.relative(posDir, d), false);
            level.removeBlock(hostPos.relative(posDir.getOpposite(), d), false);
        }
    }

    public static void detachYard(Level level, BlockPos hostPos, YardHost host, Player player) {
        if (!host.hasYard()) {
            return;
        }
        int armLength = host.getYardArmLength();
        TIBAMBlocks.Wood wood = host.getYardWood();
        YardGrowth.breakYardIfAny(level, hostPos, host);
        host.clearYard();
        if (!player.getAbilities().instabuild) {
            int chunk;
            Item item = (Item)TIBAMItems.YARDS.get((Object)wood).get();
            int maxStack = item.getDefaultInstance().getMaxStackSize();
            Vec3 dropPos = VSCompat.spawnPosToWorld(level, hostPos, (double)hostPos.getX() + 0.5, (double)hostPos.getY() + 0.5, (double)hostPos.getZ() + 0.5);
            for (int remaining = armLength * 2; remaining > 0; remaining -= chunk) {
                chunk = Math.min(remaining, maxStack);
                ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, new ItemStack((ItemLike)item, chunk));
                entity.setDefaultPickUpDelay();
                level.addFreshEntity((Entity)entity);
            }
        }
    }

    public static void trimOversizedYards(Level level, BlockPos anchorPos, int newTotalHeight) {
        int cap = YardGrowth.maxArmLength(newTotalHeight);
        for (int offset = 0; offset < newTotalHeight; ++offset) {
            YardHost host;
            BlockPos cellPos = anchorPos.above(offset);
            BlockEntity blockEntity = level.getBlockEntity(cellPos);
            if (!(blockEntity instanceof YardHost) || !(host = (YardHost)blockEntity).hasYard() || host.getYardArmLength() <= cap) continue;
            YardGrowth.trimYardTo(level, cellPos, host, cap);
        }
    }

    private static void trimYardTo(Level level, BlockPos hostPos, YardHost host, int newArmLength) {
        Direction posDir = YardGrowth.positiveDirectionFor(host.getYardAxis());
        int oldArmLength = host.getYardArmLength();
        for (int d = newArmLength + 1; d <= oldArmLength; ++d) {
            level.removeBlock(hostPos.relative(posDir, d), false);
            level.removeBlock(hostPos.relative(posDir.getOpposite(), d), false);
        }
        if (newArmLength <= 0) {
            host.clearYard();
        } else {
            host.setYardArmLength(newArmLength);
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
        remaining = YardGrowth.drain((List<ItemStack>)player.getInventory().items, item, remaining);
        YardGrowth.drain((List<ItemStack>)player.getInventory().offhand, item, remaining);
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

