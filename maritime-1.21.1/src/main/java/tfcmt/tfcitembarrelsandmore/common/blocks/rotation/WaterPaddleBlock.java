package tfcmt.tfcitembarrelsandmore.common.blocks.rotation;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.blockentities.rotation.WaterPaddleBlockEntity;

public class WaterPaddleBlock
extends Block
implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final TIBAMBlocks.Wood wood;

    public WaterPaddleBlock(BlockBehaviour.Properties properties, TIBAMBlocks.Wood wood) {
        super(properties);
        this.wood = wood;
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue((Property)FACING, (Comparable)Direction.NORTH));
    }

    public TIBAMBlocks.Wood getWood() {
        return this.wood;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction clicked = ctx.getClickedFace();
        Direction awayFromAxle = clicked.getAxis().isHorizontal() ? clicked : ctx.getHorizontalDirection().getOpposite();
        Direction facing = awayFromAxle.getOpposite();
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, (Comparable)facing);
    }

    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos changedPos, boolean isMoving) {
        if (level.isClientSide) {
            return;
        }
        BlockPos axlePos = pos.relative((Direction)state.getValue((Property)FACING));
        if (!changedPos.equals((Object)axlePos)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof WaterPaddleBlockEntity) {
            WaterPaddleBlockEntity be = (WaterPaddleBlockEntity)blockEntity;
            be.onNeighborChanged();
        }
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaterPaddleBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        if (type != TIBAMBlockEntities.WATER_PADDLE.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof WaterPaddleBlockEntity) {
                WaterPaddleBlockEntity paddle = (WaterPaddleBlockEntity)be;
                WaterPaddleBlockEntity.serverTick(lvl, pos, st, paddle);
            }
        };
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity blockEntity;
        if (!newState.is((Block)this) && (blockEntity = level.getBlockEntity(pos)) instanceof WaterPaddleBlockEntity) {
            WaterPaddleBlockEntity be = (WaterPaddleBlockEntity)blockEntity;
            be.onUnloaded();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}

