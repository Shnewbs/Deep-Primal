package tfcmt.tfcitembarrelsandmore.common.blocks.devices;

import java.util.List;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.blockentities.ExpellerPressBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.GrinderBlock;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class ExpellerPressBlock
extends DeviceBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<GrinderBlock.ScrewType> SCREW = EnumProperty.create((String)"screw", GrinderBlock.ScrewType.class);
    public static final BooleanProperty ROTATING = BooleanProperty.create((String)"rotating");
    private static final String NBT_FLUID = "PressFluid";

    public ExpellerPressBlock(ExtendedProperties properties) {
        super(properties, DeviceBlock.InventoryRemoveBehavior.DROP);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue((Property)FACING, (Comparable)Direction.NORTH)).setValue(SCREW, GrinderBlock.ScrewType.NONE)).setValue((Property)ROTATING, (Comparable)Boolean.valueOf(false)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, SCREW, ROTATING});
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue((Property)FACING, (Comparable)context.getHorizontalDirection().getOpposite())).setValue(SCREW, GrinderBlock.ScrewType.NONE)).setValue((Property)ROTATING, (Comparable)Boolean.valueOf(false));
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockEntity blockEntity;
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.has(stack) && tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).contains(NBT_FLUID) && (blockEntity = level.getBlockEntity(pos)) instanceof ExpellerPressBlockEntity) {
            ExpellerPressBlockEntity press = (ExpellerPressBlockEntity)blockEntity;
            FluidStack fluid = FluidStack.loadFluidStackFromNBT((CompoundTag)tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).getCompound(NBT_FLUID));
            press.getOutputTank().fill(fluid, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ExpellerPressBlockEntity) {
            ExpellerPressBlockEntity press = (ExpellerPressBlockEntity)blockEntity;
            ((ServerPlayer)player).openMenu(press, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExpellerPressBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != TIBAMBlockEntities.EXPELLER_PRESS.get()) {
            return null;
        }
        return level.isClientSide ? (lvl, pos, st, be) -> ExpellerPressBlockEntity.clientTick(lvl, pos, st, (ExpellerPressBlockEntity)be) : (lvl, pos, st, be) -> ExpellerPressBlockEntity.serverTick(lvl, pos, st, (ExpellerPressBlockEntity)be);
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos changedPos, boolean isMoving) {
        BlockEntity blockEntity;
        if (!level.isClientSide && (blockEntity = level.getBlockEntity(pos)) instanceof ExpellerPressBlockEntity) {
            ExpellerPressBlockEntity press = (ExpellerPressBlockEntity)blockEntity;
            press.onNeighbourChanged();
        }
    }

    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    }

    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        ExpellerPressBlockEntity press;
        FluidStack fluid;
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ExpellerPressBlockEntity && !(fluid = (press = (ExpellerPressBlockEntity)blockEntity).getOutputTank().getFluid()).isEmpty()) {
            tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(stack, data -> data.put(NBT_FLUID, (Tag)fluid.writeToNBT(new CompoundTag())));
        }
        return stack;
    }

    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
            ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, this.getCloneItemStack(state, null, (BlockGetter)level, pos, player));
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

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity blockEntity;
        if (!newState.is((Block)this) && !level.isClientSide && (blockEntity = level.getBlockEntity(pos)) instanceof ExpellerPressBlockEntity) {
            ExpellerPressBlockEntity press = (ExpellerPressBlockEntity)blockEntity;
            press.onUnloaded();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}

