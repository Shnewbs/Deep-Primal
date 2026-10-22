package tfcmt.tfcitembarrelsandmore.common.blocks.rotation;

import javax.annotation.Nullable;
import net.dries007.tfc.common.blocks.rotation.AxleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
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
import net.minecraft.world.phys.Vec3;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.blockentities.rotation.PropellerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.rotation.CastIronAxleBlock;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class PropellerBlock
extends Block
implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private final boolean requiresCastIron;

    public PropellerBlock(BlockBehaviour.Properties properties, boolean requiresCastIron) {
        super(properties);
        this.requiresCastIron = requiresCastIron;
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue((Property)FACING, Direction.UP));
    }

    public boolean requiresCastIron() {
        return this.requiresCastIron;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction clicked = ctx.getClickedFace();
        if (clicked == Direction.DOWN) {
            clicked = Direction.UP;
        }
        Direction facing = clicked.getOpposite();
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, facing);
    }

    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos changedPos, boolean isMoving) {
        BlockEntity blockEntity;
        BlockState axleState;
        Block axleBlock;
        if (level.isClientSide) {
            return;
        }
        Direction connectionFace = ((Direction)state.getValue((Property)FACING)).getOpposite();
        BlockPos axlePos = pos.relative(connectionFace);
        if (!changedPos.equals((Object)axlePos)) {
            return;
        }
        if (this.requiresCastIron && (axleBlock = (axleState = level.getBlockState(axlePos)).getBlock()) instanceof AxleBlock && !(axleBlock instanceof CastIronAxleBlock)) {
            ItemStack drop = new ItemStack((ItemLike)axleBlock.asItem());
            level.destroyBlock(axlePos, false);
            if (!drop.isEmpty()) {
                Vec3 dropPos = VSCompat.spawnPosToWorld(level, axlePos, (double)axlePos.getX() + 0.5, (double)axlePos.getY() + 0.5, (double)axlePos.getZ() + 0.5);
                ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, drop);
                entity.setDefaultPickUpDelay();
                level.addFreshEntity((Entity)entity);
            }
        }
        if ((blockEntity = level.getBlockEntity(pos)) instanceof PropellerBlockEntity) {
            PropellerBlockEntity be = (PropellerBlockEntity)blockEntity;
            be.onNeighborChanged();
        }
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PropellerBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        if (type != TIBAMBlockEntities.PROPELLER.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof PropellerBlockEntity) {
                PropellerBlockEntity prop = (PropellerBlockEntity)be;
                PropellerBlockEntity.serverTick(lvl, pos, st, prop);
            }
        };
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity blockEntity;
        if (!newState.is((Block)this) && (blockEntity = level.getBlockEntity(pos)) instanceof PropellerBlockEntity) {
            PropellerBlockEntity be = (PropellerBlockEntity)blockEntity;
            be.onUnloaded();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}

