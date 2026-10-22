package tfcmt.tfcitembarrelsandmore.common.blocks.devices;

import java.util.List;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.SinglePistonEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerBlock;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class SinglePistonEngineBlock
extends DeviceBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LINKED = BooleanProperty.create((String)"linked");
    public static final BooleanProperty RUNNING = BooleanProperty.create((String)"running");

    public SinglePistonEngineBlock(ExtendedProperties properties) {
        super(properties, DeviceBlock.InventoryRemoveBehavior.DROP);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue((Property)FACING, (Comparable)Direction.NORTH)).setValue((Property)LINKED, (Comparable)Boolean.valueOf(false))).setValue((Property)RUNNING, (Comparable)Boolean.valueOf(false)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING, LINKED, RUNNING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, (Comparable)ctx.getHorizontalDirection().getOpposite());
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SinglePistonEngineBlockEntity) {
            SinglePistonEngineBlockEntity engine = (SinglePistonEngineBlockEntity)blockEntity;
            return engine.onPlayerInteract(state, level, pos, player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != TIBAMBlockEntities.SINGLE_PISTON_ENGINE.get()) {
            return null;
        }
        return level.isClientSide ? (lvl, pos, st, be) -> SinglePistonEngineBlockEntity.clientTick(lvl, pos, st, (SinglePistonEngineBlockEntity)be) : (lvl, pos, st, be) -> SinglePistonEngineBlockEntity.serverTick(lvl, pos, st, (SinglePistonEngineBlockEntity)be);
    }

    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
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

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && ((Boolean)state.getValue((Property)LINKED)).booleanValue()) {
            BlockPos boilerPos;
            BlockEntity blockEntity;
            BlockEntity blockEntity2 = level.getBlockEntity(pos);
            if (blockEntity2 instanceof SinglePistonEngineBlockEntity) {
                SinglePistonEngineBlockEntity engine = (SinglePistonEngineBlockEntity)blockEntity2;
                engine.dropRod(level, pos, state);
            }
            if ((blockEntity = level.getBlockEntity(boilerPos = pos.relative(((Direction)state.getValue((Property)FACING)).getOpposite()))) instanceof BoilerBlockEntity) {
                BoilerBlockEntity boiler = (BoilerBlockEntity)blockEntity;
                level.setBlock(boilerPos, (BlockState)level.getBlockState(boilerPos).setValue((Property)BoilerBlock.LINKED, (Comparable)Boolean.valueOf(false)), 2);
                boiler.setLinkedEngine(null);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rng) {
        if (!((Boolean)state.getValue((Property)RUNNING)).booleanValue()) {
            return;
        }
        double cx = (double)pos.getX() + 0.5;
        double cy = (double)pos.getY() + 1.0;
        double cz = (double)pos.getZ() + 0.5;
        if (rng.nextInt(4) == 0) {
            level.addParticle((ParticleOptions)ParticleTypes.CLOUD, cx + rng.nextDouble() * 0.3 - 0.15, cy, cz + rng.nextDouble() * 0.3 - 0.15, 0.0, 0.04, 0.0);
        }
    }
}

