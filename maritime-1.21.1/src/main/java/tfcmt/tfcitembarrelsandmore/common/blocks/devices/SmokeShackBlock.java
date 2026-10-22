package tfcmt.tfcitembarrelsandmore.common.blocks.devices;

import java.util.List;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SmokeShackBlockEntity;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class SmokeShackBlock
extends DeviceBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public SmokeShackBlock(ExtendedProperties properties) {
        super(properties, DeviceBlock.InventoryRemoveBehavior.DROP);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue((Property)FACING, (Comparable)Direction.NORTH)).setValue((Property)LIT, (Comparable)Boolean.valueOf(false)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, LIT});
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos below;
        Level level = context.getLevel();
        BlockState belowState = level.getBlockState(below = context.getClickedPos().below());
        Direction facing = belowState.hasProperty((Property)FACING) ? (Direction)belowState.getValue((Property)FACING) : context.getHorizontalDirection().getOpposite();
        return (BlockState)((BlockState)this.defaultBlockState().setValue((Property)FACING, (Comparable)facing)).setValue((Property)LIT, (Comparable)Boolean.valueOf(false));
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SmokeShackBlockEntity) {
            SmokeShackBlockEntity be = (SmokeShackBlockEntity)blockEntity;
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                serverPlayer.openMenu(be, pos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmokeShackBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        if (type != TIBAMBlockEntities.SMOKE_SHACK.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> SmokeShackBlockEntity.serverTick(lvl, pos, st, (SmokeShackBlockEntity)be);
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos changedPos, boolean isMoving) {
    }

    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
            ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, new ItemStack((ItemLike)this));
            entity.setDefaultPickUpDelay();
            level.addFreshEntity((Entity)entity);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player) {
            return List.of();
        }
        return super.getDrops(state, builder);
    }
}

