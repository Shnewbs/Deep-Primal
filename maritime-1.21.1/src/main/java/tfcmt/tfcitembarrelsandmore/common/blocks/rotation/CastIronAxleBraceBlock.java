package tfcmt.tfcitembarrelsandmore.common.blocks.rotation;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredHolder;
import tfcmt.tfcitembarrelsandmore.common.blockentities.rotation.CastIronAxleBraceBlockEntity;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class CastIronAxleBraceBlock
extends Block
implements EntityBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final DirectionProperty FACE = BlockStateProperties.FACING;

    public CastIronAxleBraceBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(AXIS, (Comparable)Direction.Axis.Z)).setValue((Property)FACE, (Comparable)Direction.DOWN));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{AXIS, FACE});
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction clickedFace = ctx.getClickedFace();
        Direction mountFace = clickedFace.getOpposite();
        Direction.Axis shaftAxis = ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown() ? Direction.Axis.Y : (clickedFace.getAxis() == Direction.Axis.Y ? ctx.getHorizontalDirection().getAxis() : (clickedFace.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X));
        return (BlockState)((BlockState)this.defaultBlockState().setValue(AXIS, (Comparable)shaftAxis)).setValue((Property)FACE, (Comparable)mountFace);
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        BlockEntity blockEntity;
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide && (blockEntity = level.getBlockEntity(pos)) instanceof CastIronAxleBraceBlockEntity) {
            CastIronAxleBraceBlockEntity brace = (CastIronAxleBraceBlockEntity)blockEntity;
            brace.onNeighborChanged();
        }
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.block();
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CastIronAxleBraceBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack((ItemLike)((DeferredHolder)((Map)TFCItems.METAL_ITEMS.get(Metal.CAST_IRON)).get(Metal.ItemType.ROD)).get()));
    }

    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity be, ItemStack tool) {
        if (!level.isClientSide) {
            ItemStack rod = new ItemStack((ItemLike)((DeferredHolder)((Map)TFCItems.METAL_ITEMS.get(Metal.CAST_IRON)).get(Metal.ItemType.ROD)).get());
            Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
            ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, rod);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity((Entity)entity);
        }
    }
}

