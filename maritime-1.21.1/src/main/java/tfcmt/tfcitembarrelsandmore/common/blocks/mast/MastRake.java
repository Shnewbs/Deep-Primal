package tfcmt.tfcitembarrelsandmore.common.blocks.mast;

import com.mojang.math.Axis;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastFillerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.BoomGrowth;

public final class MastRake {
    public static final float DEGREES_PER_RAKE_STEP = 15.0f;
    public static final int MAX_RAKE_STEPS = 6;

    private MastRake() {
    }

    public static InteractionResult tryRake(Level level, BlockPos anchorPos, Player player, ItemStack held, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(anchorPos);
        if (!(blockEntity instanceof MastBlockEntity)) {
            return InteractionResult.PASS;
        }
        MastBlockEntity anchor = (MastBlockEntity)blockEntity;
        if (!Helpers.isItem((ItemStack)held, (TagKey)net.minecraft.tags.ItemTags.create(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "tools/hammer")))) {
            return InteractionResult.PASS;
        }
        return player.isShiftKeyDown() ? MastRake.tryUnrake(anchor) : MastRake.tryIncreaseRake(level, anchorPos, anchor, player, hit);
    }

    private static InteractionResult tryIncreaseRake(Level level, BlockPos anchorPos, MastBlockEntity anchor, Player player, BlockHitResult hit) {
        Direction face;
        Direction clicked = hit.getDirection();
        if (clicked.getAxis() == Direction.Axis.Y) {
            return InteractionResult.PASS;
        }
        int currentSteps = anchor.getRakeSteps();
        Direction direction = face = currentSteps > 0 ? anchor.getRakeFace() : clicked.getOpposite();
        if (currentSteps > 0 && clicked.getOpposite() != face) {
            return InteractionResult.PASS;
        }
        int newSteps = currentSteps + 1;
        if (newSteps > 6) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.mast.max_rake"), true);
            return InteractionResult.FAIL;
        }
        if (currentSteps == 0) {
            MastRake.popBoomsFromColumn(level, anchorPos, anchor, player);
        }
        anchor.setRake(face, newSteps);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult tryUnrake(MastBlockEntity anchor) {
        int currentSteps = anchor.getRakeSteps();
        if (currentSteps == 0) {
            return InteractionResult.PASS;
        }
        anchor.setRake(anchor.getRakeFace(), currentSteps - 1);
        return InteractionResult.SUCCESS;
    }

    private static void popBoomsFromColumn(Level level, BlockPos anchorPos, MastBlockEntity anchor, Player player) {
        if (anchor.hasBoom()) {
            BoomGrowth.detachBoom(level, anchorPos, anchor, player);
        }
        int totalHeight = anchor.getTotalHeight();
        for (int offset = 1; offset < totalHeight; ++offset) {
            MastFillerBlockEntity fillerBE;
            BlockPos cellPos = anchorPos.above(offset);
            BlockEntity blockEntity = level.getBlockEntity(cellPos);
            if (!(blockEntity instanceof MastFillerBlockEntity) || !(fillerBE = (MastFillerBlockEntity)blockEntity).hasBoom()) continue;
            BoomGrowth.detachBoom(level, cellPos, fillerBE, player);
        }
    }

    public static Direction.Axis pitchAxis(Direction rakeFace) {
        return rakeFace.getAxis() == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z;
    }

    private static float signedDegrees(Direction rakeFace, int rakeSteps) {
        int sign = rakeFace.getAxis() == Direction.Axis.Z ? rakeFace.getStepZ() : -rakeFace.getStepX();
        return (float)(sign * rakeSteps) * 15.0f;
    }

    public static Quaternionf rotation(Direction rakeFace, int rakeSteps) {
        float degrees = MastRake.signedDegrees(rakeFace, rakeSteps);
        return MastRake.pitchAxis(rakeFace) == Direction.Axis.X ? Axis.XP.rotationDegrees(degrees) : Axis.ZP.rotationDegrees(degrees);
    }

    public static Vec3 displacement(Direction rakeFace, int rakeSteps, double restYBlocks) {
        if (rakeSteps == 0) {
            return Vec3.ZERO;
        }
        double theta = Math.toRadians(MastRake.signedDegrees(rakeFace, rakeSteps));
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        return MastRake.pitchAxis(rakeFace) == Direction.Axis.X ? new Vec3(0.0, restYBlocks * (cos - 1.0), restYBlocks * sin) : new Vec3(-restYBlocks * sin, restYBlocks * (cos - 1.0), 0.0);
    }
}

