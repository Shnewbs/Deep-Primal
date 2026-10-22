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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.compat.create.TFCSpeedTelegraphOutputBlockEntity;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class TFCSpeedTelegraphOutputBlock
extends DeviceBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public TFCSpeedTelegraphOutputBlock(ExtendedProperties properties) {
        super(properties, DeviceBlock.InventoryRemoveBehavior.DROP);
        this.registerDefaultState((BlockState)((BlockState)this.getStateDefinition().any()).setValue((Property)FACING, Direction.EAST));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, ctx.getHorizontalDirection());
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != TIBAMBlockEntities.TFC_SPEED_TELEGRAPH_OUTPUT.get()) {
            return null;
        }
        return level.isClientSide ? (lvl, pos, st, be) -> {
            if (be instanceof TFCSpeedTelegraphOutputBlockEntity) {
                TFCSpeedTelegraphOutputBlockEntity out = (TFCSpeedTelegraphOutputBlockEntity)be;
                TFCSpeedTelegraphOutputBlockEntity.clientTick(lvl, pos, st, out);
            }
        } : (lvl, pos, st, be) -> {
            if (be instanceof TFCSpeedTelegraphOutputBlockEntity) {
                TFCSpeedTelegraphOutputBlockEntity out = (TFCSpeedTelegraphOutputBlockEntity)be;
                TFCSpeedTelegraphOutputBlockEntity.serverTick(lvl, pos, st, out);
            }
        };
    }

    public InteractionResult legacyUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TFCSpeedTelegraphOutputBlockEntity) {
            TFCSpeedTelegraphOutputBlockEntity outputBE = (TFCSpeedTelegraphOutputBlockEntity)blockEntity;
            return outputBE.onPlayerInteract(state, level, pos, player, hand, hit);
        }
        return InteractionResult.PASS;
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

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity blockEntity;
        if (!state.is(newState.getBlock()) && !level.isClientSide && (blockEntity = level.getBlockEntity(pos)) instanceof TFCSpeedTelegraphOutputBlockEntity) {
            TFCSpeedTelegraphOutputBlockEntity outputBE = (TFCSpeedTelegraphOutputBlockEntity)blockEntity;
            outputBE.notifyInputOnBreak(level);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        BlockEntity blockEntity;
        if (!level.isClientSide && (blockEntity = level.getBlockEntity(pos)) instanceof TFCSpeedTelegraphOutputBlockEntity) {
            TFCSpeedTelegraphOutputBlockEntity outputBE = (TFCSpeedTelegraphOutputBlockEntity)blockEntity;
            outputBE.onNeighbourChanged();
        }
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

