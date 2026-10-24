package tfcmt.tfcitembarrelsandmore.common.blocks.mast;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import tfcmt.tfcitembarrelsandmore.client.renderer.mast.MastClientBlockExtensions;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigSlot;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.YardFillerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.YardHost;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.RigLinking;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.YardGrowth;

public class YardFillerBlock
extends Block
implements EntityBlock {
    public YardFillerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public InteractionResult legacyUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof YardFillerBlockEntity)) {
            return InteractionResult.PASS;
        }
        YardFillerBlockEntity filler = (YardFillerBlockEntity)blockEntity;
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof LeadItem) {
            BlockEntity blockEntity2 = level.getBlockEntity(filler.hostPos());
            if (!(blockEntity2 instanceof YardHost)) {
                return InteractionResult.PASS;
            }
            YardHost host = (YardHost)blockEntity2;
            if (filler.getDistanceFromHost() != host.getYardArmLength()) {
                player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.rigging.not_tip"), true);
                return InteractionResult.FAIL;
            }
            if (!(host instanceof RigLinkable)) {
                return InteractionResult.PASS;
            }
            RigLinkable linkableHost = (RigLinkable)((Object)host);
            RigSlot slot = filler.getSignedOffset() > 0 ? RigSlot.YARD_RIGHT : RigSlot.YARD_LEFT;
            return RigLinking.tryLinkOrUnlink(linkableHost, slot, player, held);
        }
        return YardGrowth.tryGrow(level, filler.hostPos(), player, held, hit);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction.Axis axis = Direction.Axis.X;
        int widthPx = 4;
        double perpOffsetPx = 0.0;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof YardFillerBlockEntity) {
            YardFillerBlockEntity filler = (YardFillerBlockEntity)blockEntity;
            if (filler.getYawSteps() != 0) {
                return Shapes.empty();
            }
            axis = filler.getAxis();
            widthPx = filler.getWidthPx();
            perpOffsetPx = YardFillerBlock.perpendicularOffsetPx(axis, filler.getFaceBias(), filler.getPerpendicularOffsetBlocks());
        }
        return YardFillerBlock.shapeFor(axis, widthPx, perpOffsetPx);
    }

    static double perpendicularOffsetPx(Direction.Axis axis, Direction faceBias, double offsetBlocks) {
        int step = axis == Direction.Axis.X ? faceBias.getStepZ() : faceBias.getStepX();
        return (double)step * offsetBlocks * 16.0;
    }

    static VoxelShape shapeFor(Direction.Axis axis, int widthPx, double perpOffsetPx) {
        double half = (double)widthPx / 2.0;
        double c = 8.0 + perpOffsetPx;
        return axis == Direction.Axis.Z ? Block.box((double)(c - half), (double)(8.0 - half), (double)0.0, (double)(c + half), (double)(8.0 + half), (double)16.0) : Block.box((double)0.0, (double)(8.0 - half), (double)(c - half), (double)16.0, (double)(8.0 + half), (double)(c + half));
    }

    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(MastClientBlockExtensions.INSTANCE);
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new YardFillerBlockEntity(pos, state);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return switch (legacyUse(state, level, pos, player, hand, hit)) {
            case SUCCESS -> net.minecraft.world.ItemInteractionResult.SUCCESS;
            case CONSUME -> net.minecraft.world.ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> net.minecraft.world.ItemInteractionResult.CONSUME_PARTIAL;
            case FAIL -> net.minecraft.world.ItemInteractionResult.FAIL;
            default -> net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return legacyUse(state, level, pos, player, InteractionHand.MAIN_HAND, hit);
    }
}

