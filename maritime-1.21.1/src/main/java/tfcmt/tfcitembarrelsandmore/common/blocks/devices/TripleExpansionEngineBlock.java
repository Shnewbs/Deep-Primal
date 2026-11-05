package tfcmt.tfcitembarrelsandmore.common.blocks.devices;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerTankBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.TripleExpansionEngineBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.EngineFillerBlock;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class TripleExpansionEngineBlock
extends DeviceBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LINKED = BooleanProperty.create((String)"linked");
    public static final BooleanProperty RUNNING = BooleanProperty.create((String)"running");
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<Direction, VoxelShape>(Direction.class);

    public TripleExpansionEngineBlock(ExtendedProperties properties) {
        super(properties, DeviceBlock.InventoryRemoveBehavior.DROP);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue((Property)FACING, Direction.NORTH)).setValue((Property)LINKED, Boolean.valueOf(false))).setValue((Property)RUNNING, Boolean.valueOf(false)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING, LINKED, RUNNING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, ctx.getHorizontalDirection().getOpposite());
    }

    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = (Direction)state.getValue((Property)FACING);
        for (BlockPos fillerPos : new BlockPos[]{pos.above(), pos.relative(facing), pos.above().relative(facing)}) {
            if (level.getBlockState(fillerPos).canBeReplaced()) continue;
            return false;
        }
        return super.canSurvive(state, level, pos);
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) {
            return;
        }
        Direction facing = (Direction)state.getValue((Property)FACING);
        BlockState filler = (BlockState)((Block)TIBAMBlocks.ENGINE_FILLER.get()).defaultBlockState().setValue((Property)EngineFillerBlock.FACING, facing);
        level.setBlock(pos.above(), (BlockState)filler.setValue(EngineFillerBlock.ROLE, EngineFillerBlock.Role.ABOVE), 3);
        level.setBlock(pos.relative(facing), (BlockState)filler.setValue(EngineFillerBlock.ROLE, EngineFillerBlock.Role.HORIZONTAL), 3);
        level.setBlock(pos.above().relative(facing), (BlockState)filler.setValue(EngineFillerBlock.ROLE, EngineFillerBlock.Role.DIAGONAL), 3);
    }

    public InteractionResult legacyUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.getOrDefault(state.getValue((Property)FACING), Shapes.block());
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.getOrDefault(state.getValue((Property)FACING), Shapes.block());
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
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            BlockEntity blockEntity;
            if (((Boolean)state.getValue((Property)LINKED)).booleanValue() && (blockEntity = level.getBlockEntity(pos)) instanceof TripleExpansionEngineBlockEntity) {
                TripleExpansionEngineBlockEntity engine = (TripleExpansionEngineBlockEntity)blockEntity;
                engine.dropRod(level, pos, state);
            }
            Direction facing = (Direction)state.getValue((Property)FACING);
            BlockPos tankPos = pos.above().relative(facing, 2);
            if (!(level.getBlockEntity(tankPos) instanceof BoilerTankBlockEntity)) {
                tankPos = pos.above().relative(facing, 1);
            }
            BlockState tankState = level.getBlockState(tankPos);
            BlockEntity blockEntity2 = level.getBlockEntity(tankPos);
            if (blockEntity2 instanceof BoilerTankBlockEntity) {
                BoilerTankBlockEntity tank = (BoilerTankBlockEntity)blockEntity2;
                tank.onNeighbourChanged(tankState, level, tankPos);
            }
            for (BlockPos fillerPos : new BlockPos[]{pos.above(), pos.relative(facing), pos.above().relative(facing)}) {
                if (!(level.getBlockState(fillerPos).getBlock() instanceof EngineFillerBlock)) continue;
                level.setBlock(fillerPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != TIBAMBlockEntities.TRIPLE_EXPANSION_ENGINE.get()) {
            return null;
        }
        return level.isClientSide ? (lvl, pos, st, be) -> TripleExpansionEngineBlockEntity.clientTick(lvl, pos, st, (TripleExpansionEngineBlockEntity)be) : null;
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rng) {
        if (!((Boolean)state.getValue((Property)RUNNING)).booleanValue()) {
            return;
        }
        Direction facing = (Direction)state.getValue((Property)FACING);
        Direction back = facing.getOpposite();
        double cx = (double)pos.getX() + 0.5 + (double)back.getStepX() * 0.25;
        double cy = (double)pos.getY() + 2.0;
        double cz = (double)pos.getZ() + 0.5 + (double)back.getStepZ() * 0.25;
        if (rng.nextInt(3) == 0) {
            level.addParticle((ParticleOptions)ParticleTypes.CLOUD, cx + rng.nextDouble() * 0.4 - 0.2, cy, cz + rng.nextDouble() * 0.4 - 0.2, 0.0, 0.05, 0.0);
        }
        if (rng.nextInt(5) == 0) {
            level.addParticle((ParticleOptions)ParticleTypes.CLOUD, cx + rng.nextDouble() * 0.6 - 0.3, cy, cz + rng.nextDouble() * 0.6 - 0.3, 0.0, 0.03, 0.0);
        }
    }

    static {
        SHAPES.put(Direction.SOUTH, Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)32.0, (double)32.0));
        SHAPES.put(Direction.NORTH, Block.box((double)0.0, (double)0.0, (double)-16.0, (double)16.0, (double)32.0, (double)16.0));
        SHAPES.put(Direction.EAST, Block.box((double)0.0, (double)0.0, (double)0.0, (double)32.0, (double)32.0, (double)16.0));
        SHAPES.put(Direction.WEST, Block.box((double)-16.0, (double)0.0, (double)0.0, (double)16.0, (double)32.0, (double)16.0));
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

