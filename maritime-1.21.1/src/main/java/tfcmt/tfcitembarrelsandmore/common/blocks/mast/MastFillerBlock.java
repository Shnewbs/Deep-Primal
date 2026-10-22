package tfcmt.tfcitembarrelsandmore.common.blocks.mast;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import tfcmt.tfcitembarrelsandmore.client.renderer.mast.MastClientBlockExtensions;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastFillerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.BoomGrowth;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.MastAnchorBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.MastGrowth;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.YardGrowth;
import tfcmt.tfcitembarrelsandmore.common.items.BoomItem;
import tfcmt.tfcitembarrelsandmore.common.items.YardItem;

public class MastFillerBlock
extends Block
implements EntityBlock {
    public MastFillerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public InteractionResult legacyUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MastFillerBlockEntity)) {
            return InteractionResult.PASS;
        }
        MastFillerBlockEntity filler = (MastFillerBlockEntity)blockEntity;
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof YardItem) {
            return YardGrowth.tryGrow(level, pos, player, held, hit);
        }
        if (held.getItem() instanceof BoomItem) {
            return BoomGrowth.tryGrow(level, pos, player, held, hit);
        }
        return MastGrowth.tryGrow(level, filler.anchorPos(), player, held);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int widthPx = 4;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MastFillerBlockEntity) {
            MastFillerBlockEntity filler = (MastFillerBlockEntity)blockEntity;
            widthPx = filler.getWidthPx();
        }
        return MastAnchorBlock.postShape(widthPx);
    }

    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(MastClientBlockExtensions.INSTANCE);
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MastFillerBlockEntity(pos, state);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
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

