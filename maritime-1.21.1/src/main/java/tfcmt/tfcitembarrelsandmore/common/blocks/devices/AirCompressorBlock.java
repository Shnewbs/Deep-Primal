package tfcmt.tfcitembarrelsandmore.common.blocks.devices;

import java.util.List;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.blockentities.AirCompressorBlockEntity;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class AirCompressorBlock
extends DeviceBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Shapes.or((VoxelShape)Block.box((double)3.0, (double)0.0, (double)0.0, (double)13.0, (double)14.0, (double)15.0), (VoxelShape[])new VoxelShape[0]);
    private static final String NBT_COMPRESSED_AIR = "CompressedAir";

    public AirCompressorBlock(ExtendedProperties properties) {
        super(properties, DeviceBlock.InventoryRemoveBehavior.DROP);
        this.registerDefaultState((BlockState)((BlockState)this.getStateDefinition().any()).setValue((Property)FACING, (Comparable)Direction.NORTH));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, (Comparable)ctx.getHorizontalDirection().getOpposite());
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof AirCompressorBlockEntity)) {
            return InteractionResult.PASS;
        }
        AirCompressorBlockEntity be = (AirCompressorBlockEntity)blockEntity;
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                AirCompressorBlock.handleLinkToggle(be, player);
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        if (!level.isClientSide) {
            ((ServerPlayer)player).openMenu(be, pos);
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide);
    }

    private static void handleLinkToggle(AirCompressorBlockEntity be, Player player) {
        if (be.isLinked(player)) {
            be.unlink(player);
            player.displayClientMessage((Component)Component.translatable((String)"tibam.air_compressor.hose_unlinked"), true);
            return;
        }
        if (be.link(player)) {
            player.displayClientMessage((Component)Component.translatable((String)"tibam.air_compressor.hose_linked"), true);
        } else {
            player.displayClientMessage((Component)Component.translatable((String)"tibam.air_compressor.hose_occupied"), true);
        }
    }

    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        AirCompressorBlockEntity be;
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AirCompressorBlockEntity && (be = (AirCompressorBlockEntity)blockEntity).getCompressedAir() > 0) {
            tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(stack, data -> data.putInt(NBT_COMPRESSED_AIR, be.getCompressedAir()));
        }
        return stack;
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockEntity blockEntity;
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && (blockEntity = level.getBlockEntity(pos)) instanceof AirCompressorBlockEntity) {
            AirCompressorBlockEntity be = (AirCompressorBlockEntity)blockEntity;
            if (tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.has(stack) && tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).contains(NBT_COMPRESSED_AIR)) {
                be.setCompressedAir(tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).getInt(NBT_COMPRESSED_AIR));
            }
        }
    }

    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            ItemStack drop = this.getCloneItemStack(state, null, (BlockGetter)level, pos, player);
            Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
            ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, drop);
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

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, fromBlock, fromPos, isMoving);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AirCompressorBlockEntity) {
            AirCompressorBlockEntity be = (AirCompressorBlockEntity)blockEntity;
            be.onNeighbourChanged();
        }
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        if (type != TIBAMBlockEntities.AIR_COMPRESSOR.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof AirCompressorBlockEntity) {
                AirCompressorBlockEntity compressor = (AirCompressorBlockEntity)be;
                AirCompressorBlockEntity.serverTick(lvl, pos, st, compressor);
            }
        };
    }
}

