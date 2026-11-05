package tfcmt.tfcitembarrelsandmore.compat.create;

import java.util.List;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfcmt.tfcitembarrelsandmore.compat.create.CreateCompat;
import tfcmt.tfcitembarrelsandmore.compat.create.SpeedTelegraphInputBlockEntity;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class SpeedTelegraphInputBlock
extends DeviceBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE_N = Shapes.or((VoxelShape)Block.box((double)3.0, (double)0.0, (double)3.0, (double)12.0, (double)25.0, (double)12.0), (VoxelShape)Block.box((double)3.0, (double)15.0, (double)2.0, (double)12.0, (double)29.0, (double)12.0));
    private static final VoxelShape SHAPE_S = Shapes.or((VoxelShape)Block.box((double)4.0, (double)0.0, (double)4.0, (double)13.0, (double)25.0, (double)13.0), (VoxelShape)Block.box((double)4.0, (double)15.0, (double)4.0, (double)13.0, (double)29.0, (double)14.0));
    private static final VoxelShape SHAPE_E = Shapes.or((VoxelShape)Block.box((double)4.0, (double)0.0, (double)3.0, (double)13.0, (double)25.0, (double)12.0), (VoxelShape)Block.box((double)4.0, (double)15.0, (double)3.0, (double)14.0, (double)29.0, (double)12.0));
    private static final VoxelShape SHAPE_W = Shapes.or((VoxelShape)Block.box((double)3.0, (double)0.0, (double)4.0, (double)12.0, (double)25.0, (double)13.0), (VoxelShape)Block.box((double)2.0, (double)15.0, (double)4.0, (double)12.0, (double)29.0, (double)13.0));

    public SpeedTelegraphInputBlock(ExtendedProperties properties) {
        super(properties, DeviceBlock.InventoryRemoveBehavior.DROP);
        this.registerDefaultState((BlockState)((BlockState)this.getStateDefinition().any()).setValue((Property)FACING, Direction.NORTH));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, ctx.getHorizontalDirection().getOpposite());
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch ((Direction)state.getValue((Property)FACING)) {
            case Direction.SOUTH -> SHAPE_S;
            case Direction.EAST -> SHAPE_E;
            case Direction.WEST -> SHAPE_W;
            default -> SHAPE_N;
        };
    }

    public InteractionResult legacyUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SpeedTelegraphInputBlockEntity) {
            SpeedTelegraphInputBlockEntity inputBE = (SpeedTelegraphInputBlockEntity)blockEntity;
            return inputBE.onPlayerInteract(state, level, pos, player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity blockEntity;
        if (!state.is(newState.getBlock()) && !level.isClientSide && (blockEntity = level.getBlockEntity(pos)) instanceof SpeedTelegraphInputBlockEntity) {
            SpeedTelegraphInputBlockEntity inputBE = (SpeedTelegraphInputBlockEntity)blockEntity;
            inputBE.notifyOutputsOnBreak(level);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
            ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, new ItemStack((ItemLike)this));
            entity.setDefaultPickUpDelay();
            level.addFreshEntity((Entity)entity);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player) {
            return List.of();
        }
        return super.getDrops(state, builder);
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockEntity blockEntity;
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && (blockEntity = level.getBlockEntity(pos)) instanceof SpeedTelegraphInputBlockEntity) {
            SpeedTelegraphInputBlockEntity inputBE = (SpeedTelegraphInputBlockEntity)blockEntity;
            inputBE.onPlacedBy(level, pos, stack);
        }
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != CreateCompat.SPEED_TELEGRAPH_INPUT_BE.get()) {
            return null;
        }
        return level.isClientSide ? (lvl, pos, st, be) -> {
            if (be instanceof SpeedTelegraphInputBlockEntity) {
                SpeedTelegraphInputBlockEntity input = (SpeedTelegraphInputBlockEntity)be;
                SpeedTelegraphInputBlockEntity.clientTick(lvl, pos, st, input);
            }
        } : (lvl, pos, st, be) -> {
            if (be instanceof SpeedTelegraphInputBlockEntity) {
                SpeedTelegraphInputBlockEntity input = (SpeedTelegraphInputBlockEntity)be;
                SpeedTelegraphInputBlockEntity.serverTick(lvl, pos, st, input);
            }
        };
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

