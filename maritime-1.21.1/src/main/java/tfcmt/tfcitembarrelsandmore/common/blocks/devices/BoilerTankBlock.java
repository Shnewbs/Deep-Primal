package tfcmt.tfcitembarrelsandmore.common.blocks.devices;

import java.util.List;
import javax.annotation.Nullable;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerTankBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.LargeSinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.TripleExpansionEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.FumeDuctBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.LargeSinglePistonEngineBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.TripleExpansionEngineBlock;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class BoilerTankBlock
extends DeviceBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final String NBT_FLUID = "TankFluid";

    public BoilerTankBlock(ExtendedProperties properties) {
        super(properties, DeviceBlock.InventoryRemoveBehavior.DROP);
        this.registerDefaultState((BlockState)((BlockState)this.getStateDefinition().any()).setValue((Property)FACING, Direction.NORTH));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos below;
        Level level = ctx.getLevel();
        BlockState belowState = level.getBlockState(below = ctx.getClickedPos().below());
        Direction facing = belowState.getBlock() instanceof BoilerBlock && belowState.hasProperty((Property)BoilerBlock.FACING) ? (Direction)belowState.getValue((Property)BoilerBlock.FACING) : ctx.getHorizontalDirection().getOpposite();
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, facing);
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockEntity blockEntity;
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.has(stack) && tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).contains(NBT_FLUID) && (blockEntity = level.getBlockEntity(pos)) instanceof BoilerTankBlockEntity) {
            BoilerTankBlockEntity tank = (BoilerTankBlockEntity)blockEntity;
            FluidStack fluid = FluidStack.parseOptional(level.registryAccess(), tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.get(stack).getCompound(NBT_FLUID));
            tank.getWaterTank().fill(fluid, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public InteractionResult legacyUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BoilerTankBlockEntity) {
            BoilerTankBlockEntity tank = (BoilerTankBlockEntity)blockEntity;
            return tank.onPlayerInteract(state, level, pos, player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    public ItemStack getCloneItemStack(BlockState state, HitResult target, net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) {
        BoilerTankBlockEntity tank;
        FluidStack fluid;
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BoilerTankBlockEntity && !(fluid = (tank = (BoilerTankBlockEntity)blockEntity).getWaterTank().getFluid()).isEmpty()) {
            tfcmt.tfcitembarrelsandmore.common.MaritimeItemData.update(stack, data -> data.put(NBT_FLUID, (Tag)fluid.saveOptional(level.registryAccess())));
        }
        return stack;
    }

    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
            ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, this.getCloneItemStack(state, null, (net.minecraft.world.level.LevelReader)level, pos, player));
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
        BlockPos[] blockPosArray;
        if (!state.is(newState.getBlock()) && !level.isClientSide && (level.getBlockEntity(pos)) instanceof BoilerTankBlockEntity tank) {
            for (BlockPos candidate : tank.getCandidateEnginePositions(state, pos)) {
                TickableInventoryBlockEntity engineBe;
                BlockEntity blockEntity;
                BlockState stopped;
                BlockState s = level.getBlockState(candidate);
                if (s.is((Block)TIBAMBlocks.TRIPLE_EXPANSION_ENGINE.get()) && ((Boolean)s.getValue((Property)TripleExpansionEngineBlock.LINKED)).booleanValue()) {
                    stopped = (BlockState)s.setValue((Property)TripleExpansionEngineBlock.LINKED, Boolean.valueOf(false));
                    level.setBlock(candidate, stopped, 2);
                    blockEntity = level.getBlockEntity(candidate);
                    if (!(blockEntity instanceof TripleExpansionEngineBlockEntity)) continue;
                    engineBe = (TripleExpansionEngineBlockEntity)blockEntity;
                    ((TripleExpansionEngineBlockEntity)engineBe).tickIdle(level, candidate, stopped);
                    continue;
                }
                if (!s.is((Block)TIBAMBlocks.LARGE_SINGLE_PISTON_ENGINE.get()) || !((Boolean)s.getValue((Property)LargeSinglePistonEngineBlock.LINKED)).booleanValue()) continue;
                stopped = (BlockState)s.setValue((Property)LargeSinglePistonEngineBlock.LINKED, Boolean.valueOf(false));
                level.setBlock(candidate, stopped, 2);
                blockEntity = level.getBlockEntity(candidate);
                if (!(blockEntity instanceof LargeSinglePistonEngineBlockEntity)) continue;
                engineBe = (LargeSinglePistonEngineBlockEntity)blockEntity;
                ((LargeSinglePistonEngineBlockEntity)engineBe).tickIdle(level, candidate, stopped);
            }
            int rodsToDrop = tank.getLinkedEngineCount();
            for (int i = 0; i < rodsToDrop; ++i) {
                ItemStack rod = tank.getLinkRodStack();
                Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
                level.addFreshEntity((Entity)new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, rod));
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rng) {
        BlockState below = level.getBlockState(pos.below());
        boolean fumeHoodOnTop = level.getBlockState(pos.above()).getBlock() instanceof FumeDuctBlock;
        if (!fumeHoodOnTop && below.getBlock() instanceof BoilerBlock && ((Boolean)below.getValue((Property)BoilerBlock.LIT)).booleanValue()) {
            Vec3 drift = BoilerBlock.computeWindDrift(level, pos);
            boolean steelTank = state.is((Block)TIBAMBlocks.STEEL_BOILER_TANK.get());
            double spawnY = (double)pos.getY() + (steelTank ? 0.95 : 0.4);
            int passes = (Integer)TIBAMConfig.COMMON.boilerSmokeMultiplier.get();
            for (int i = 0; i < passes; ++i) {
                level.addParticle((ParticleOptions)TFCParticles.SMOKE_4.get(), (double)pos.getX() + 0.5, spawnY, (double)pos.getZ() + 0.5, drift.x, 0.07, drift.z);
            }
        }
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != TIBAMBlockEntities.BOILER_TANK.get() && type != TIBAMBlockEntities.STEEL_BOILER_TANK.get()) {
            return null;
        }
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof BoilerTankBlockEntity) {
                BoilerTankBlockEntity tank = (BoilerTankBlockEntity)be;
                BoilerTankBlockEntity.serverTick(lvl, pos, st, tank);
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

