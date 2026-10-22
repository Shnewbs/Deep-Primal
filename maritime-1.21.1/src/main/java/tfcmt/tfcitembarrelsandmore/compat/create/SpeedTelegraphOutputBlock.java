package tfcmt.tfcitembarrelsandmore.compat.create;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import tfcmt.tfcitembarrelsandmore.compat.create.CreateCompat;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphOutputBlockEntity;

public class SpeedTelegraphOutputBlock
extends DirectionalKineticBlock
implements IBE<SpeedTelegraphOutputBlockEntity> {
    public SpeedTelegraphOutputBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.getStateDefinition().any()).setValue((Property)FACING, Direction.EAST));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, ctx.getHorizontalDirection());
    }

    public Direction.Axis getRotationAxis(BlockState state) {
        return ((Direction)state.getValue((Property)FACING)).getCounterClockWise().getAxis();
    }

    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == ((Direction)state.getValue((Property)FACING)).getClockWise();
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != CreateCompat.SPEED_TELEGRAPH_OUTPUT_BE.get()) {
            return null;
        }
        return level.isClientSide ? (lvl, pos, st, be) -> {
            if (be instanceof SpeedTelegraphOutputBlockEntity) {
                SpeedTelegraphOutputBlockEntity out = (SpeedTelegraphOutputBlockEntity)be;
                SpeedTelegraphOutputBlockEntity.clientTick(lvl, pos, st, out);
            }
        } : (lvl, pos, st, be) -> {
            if (be instanceof SpeedTelegraphOutputBlockEntity) {
                SpeedTelegraphOutputBlockEntity out = (SpeedTelegraphOutputBlockEntity)be;
                SpeedTelegraphOutputBlockEntity.serverTick(lvl, pos, st, out);
            }
        };
    }

    public InteractionResult legacyUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SpeedTelegraphOutputBlockEntity) {
            SpeedTelegraphOutputBlockEntity outputBE = (SpeedTelegraphOutputBlockEntity)blockEntity;
            return outputBE.onPlayerInteract(state, level, pos, player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity blockEntity;
        if (!state.is(newState.getBlock()) && !level.isClientSide && (blockEntity = level.getBlockEntity(pos)) instanceof SpeedTelegraphOutputBlockEntity) {
            SpeedTelegraphOutputBlockEntity outputBE = (SpeedTelegraphOutputBlockEntity)blockEntity;
            outputBE.notifyInputOnBreak(level);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, fromBlock, fromPos, isMoving);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SpeedTelegraphOutputBlockEntity) {
            SpeedTelegraphOutputBlockEntity outputBE = (SpeedTelegraphOutputBlockEntity)blockEntity;
            outputBE.onNeighbourChanged();
        }
    }

    public Class<SpeedTelegraphOutputBlockEntity> getBlockEntityClass() {
        return SpeedTelegraphOutputBlockEntity.class;
    }

    public BlockEntityType<? extends SpeedTelegraphOutputBlockEntity> getBlockEntityType() {
        return (BlockEntityType)CreateCompat.SPEED_TELEGRAPH_OUTPUT_BE.get();
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.getBlockEntityType().create(pos, state);
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

