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
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomFillerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomHost;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigSlot;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.BoomGrowth;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.RigLinking;

public class BoomFillerBlock
extends Block
implements EntityBlock {
    public BoomFillerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public InteractionResult legacyUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BoomFillerBlockEntity)) {
            return InteractionResult.PASS;
        }
        BoomFillerBlockEntity filler = (BoomFillerBlockEntity)blockEntity;
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof LeadItem) {
            RigSlot slot;
            BlockEntity blockEntity2 = level.getBlockEntity(filler.hostPos());
            if (!(blockEntity2 instanceof BoomHost)) {
                return InteractionResult.PASS;
            }
            BoomHost host = (BoomHost)blockEntity2;
            int distance = filler.getDistanceFromHost();
            int length = host.getBoomLength();
            if (distance == length) {
                slot = RigSlot.BOOM_END;
            } else if (distance == length - 1) {
                slot = RigSlot.BOOM_END_PORT;
            } else {
                player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.rigging.not_tip"), true);
                return InteractionResult.FAIL;
            }
            if (!(host instanceof RigLinkable)) {
                return InteractionResult.PASS;
            }
            RigLinkable linkableHost = (RigLinkable)((Object)host);
            return RigLinking.tryLinkOrUnlink(linkableHost, slot, player, held);
        }
        return BoomGrowth.tryGrow(level, filler.hostPos(), player, held, hit);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction.Axis axis = Direction.Axis.X;
        int widthPx = 4;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BoomFillerBlockEntity) {
            BoomFillerBlockEntity filler = (BoomFillerBlockEntity)blockEntity;
            if (filler.getYawSteps() != 0) {
                return Shapes.empty();
            }
            axis = filler.getFace().getAxis();
            widthPx = filler.getWidthPx();
        }
        return BoomFillerBlock.shapeFor(axis, widthPx);
    }

    static VoxelShape shapeFor(Direction.Axis axis, int widthPx) {
        double half = (double)widthPx / 2.0;
        return axis == Direction.Axis.Z ? Block.box((double)(8.0 - half), (double)(8.0 - half), (double)0.0, (double)(8.0 + half), (double)(8.0 + half), (double)16.0) : Block.box((double)0.0, (double)(8.0 - half), (double)(8.0 - half), (double)16.0, (double)(8.0 + half), (double)(8.0 + half));
    }

    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(MastClientBlockExtensions.INSTANCE);
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoomFillerBlockEntity(pos, state);
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

