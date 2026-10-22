package tfcmt.tfcitembarrelsandmore.common.blocks.mast;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomHost;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.CleatBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLink;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkRegistry;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigSlot;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.YardHost;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.BoomGrowth;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.RigLinking;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.YardGrowth;

public class CleatBlock
extends Block
implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape SHAPE = Shapes.create((double)0.3125, (double)0.3125, (double)0.3125, (double)0.6875, (double)0.6875, (double)0.6875);

    public CleatBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.getStateDefinition().any()).setValue((Property)FACING, Direction.NORTH));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, context.getClickedFace());
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CleatBlockEntity(pos, state);
    }

    public InteractionResult legacyUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof CleatBlockEntity)) {
            return InteractionResult.PASS;
        }
        CleatBlockEntity cleat = (CleatBlockEntity)blockEntity;
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof LeadItem) {
            return RigLinking.tryLinkOrUnlink(cleat, RigSlot.CLEAT_LINE, player, held);
        }
        return CleatBlock.pull(cleat, player);
    }

    private static InteractionResult pull(CleatBlockEntity cleat, Player player) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Set<RigLink> links = cleat.getLinks(RigSlot.CLEAT_LINE);
        if (links.isEmpty()) {
            player.displayClientMessage((Component)Component.translatable((String)"tfc_item_barrels_and_more.cleat.nothing_linked"), true);
            return InteractionResult.FAIL;
        }
        boolean anySucceeded = false;
        for (RigLink link : links) {
            BoomHost boomHost;
            YardHost yardHost;
            boolean isBoomEnd;
            RigLinkable target = RigLinkRegistry.find(link.targetUuid());
            if (target == null) continue;
            RigSlot slot = link.targetSlot();
            boolean isYardEnd = slot == RigSlot.YARD_LEFT || slot == RigSlot.YARD_RIGHT;
            boolean bl = isBoomEnd = slot == RigSlot.BOOM_END || slot == RigSlot.BOOM_END_PORT;
            if (isYardEnd && target instanceof YardHost && YardGrowth.tryRotate(yardHost = (YardHost)((Object)target), slot, player) == InteractionResult.SUCCESS) {
                anySucceeded = true;
                continue;
            }
            if (!isBoomEnd || !(target instanceof BoomHost) || BoomGrowth.tryRotate(boomHost = (BoomHost)((Object)target), slot, player) != InteractionResult.SUCCESS) continue;
            anySucceeded = true;
        }
        return anySucceeded ? InteractionResult.SUCCESS : InteractionResult.FAIL;
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

