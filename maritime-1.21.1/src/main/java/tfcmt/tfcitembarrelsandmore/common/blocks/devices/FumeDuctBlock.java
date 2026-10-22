package tfcmt.tfcitembarrelsandmore.common.blocks.devices;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.dries007.tfc.client.particle.TFCParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blockentities.FumeDuctBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerTankBlock;

public class FumeDuctBlock
extends Block
implements EntityBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty SIDE = BooleanProperty.create((String)"side");
    public static final BooleanProperty NORTH_SIDE = BooleanProperty.create((String)"north_side");
    public static final BooleanProperty SOUTH_SIDE = BooleanProperty.create((String)"south_side");
    public static final BooleanProperty EAST_SIDE = BooleanProperty.create((String)"east_side");
    public static final BooleanProperty WEST_SIDE = BooleanProperty.create((String)"west_side");
    public static final BooleanProperty DOWN_SIDE = BooleanProperty.create((String)"down_side");
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = Map.of(Direction.NORTH, NORTH, Direction.SOUTH, SOUTH, Direction.EAST, EAST, Direction.WEST, WEST, Direction.UP, UP, Direction.DOWN, DOWN);
    private static final Map<Direction, BooleanProperty> SIDE_PROPERTY_BY_DIRECTION = Map.of(Direction.NORTH, NORTH_SIDE, Direction.SOUTH, SOUTH_SIDE, Direction.EAST, EAST_SIDE, Direction.WEST, WEST_SIDE, Direction.DOWN, DOWN_SIDE);
    private static final int MAX_HORIZONTAL_VENT_SCAN_STEPS = 8;
    private static final int MAX_NETWORK_NODES = 4096;
    private static final Direction[] NON_DOWN_DIRECTIONS = new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    public FumeDuctBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue((Property)NORTH, (Comparable)Boolean.valueOf(false))).setValue((Property)SOUTH, (Comparable)Boolean.valueOf(false))).setValue((Property)EAST, (Comparable)Boolean.valueOf(false))).setValue((Property)WEST, (Comparable)Boolean.valueOf(false))).setValue((Property)UP, (Comparable)Boolean.valueOf(false))).setValue((Property)DOWN, (Comparable)Boolean.valueOf(false))).setValue((Property)NORTH_SIDE, (Comparable)Boolean.valueOf(false))).setValue((Property)SOUTH_SIDE, (Comparable)Boolean.valueOf(false))).setValue((Property)EAST_SIDE, (Comparable)Boolean.valueOf(false))).setValue((Property)WEST_SIDE, (Comparable)Boolean.valueOf(false))).setValue((Property)DOWN_SIDE, (Comparable)Boolean.valueOf(false))).setValue((Property)SIDE, (Comparable)Boolean.valueOf(false))).setValue(AXIS, (Comparable)Direction.Axis.Z));
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FumeDuctBlockEntity(pos, state);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{NORTH, SOUTH, EAST, WEST, UP, DOWN, NORTH_SIDE, SOUTH_SIDE, EAST_SIDE, WEST_SIDE, DOWN_SIDE, SIDE, AXIS});
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction clickedFace = ctx.getClickedFace();
        if (clickedFace.getAxis() == Direction.Axis.Y) {
            BlockState state = this.defaultBlockState();
            for (Direction dir : Direction.values()) {
                state = (BlockState)state.setValue((Property)PROPERTY_BY_DIRECTION.get(dir), (Comparable)Boolean.valueOf(FumeDuctBlock.connectsTo(dir, level.getBlockState(pos.relative(dir)))));
            }
            for (Direction dir : SIDE_PROPERTY_BY_DIRECTION.keySet()) {
                BlockState neighbor = level.getBlockState(pos.relative(dir));
                state = (BlockState)state.setValue((Property)SIDE_PROPERTY_BY_DIRECTION.get(dir), (Comparable)Boolean.valueOf(neighbor.getBlock() instanceof FumeDuctBlock && (Boolean)neighbor.getValue((Property)SIDE) != false));
            }
            return state;
        }
        Direction.Axis axis = clickedFace.getAxis();
        BlockState state = (BlockState)((BlockState)this.defaultBlockState().setValue((Property)SIDE, (Comparable)Boolean.valueOf(true))).setValue(AXIS, (Comparable)axis);
        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN) continue;
            state = (BlockState)state.setValue((Property)PROPERTY_BY_DIRECTION.get(dir), (Comparable)Boolean.valueOf(FumeDuctBlock.connectsTo(dir, level.getBlockState(pos.relative(dir)))));
        }
        return state;
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockEntity blockEntity;
        if (((Boolean)state.getValue((Property)SIDE)).booleanValue() && direction == Direction.DOWN) {
            return state;
        }
        boolean connected = FumeDuctBlock.connectsTo(direction, neighborState);
        if (connected != (Boolean)state.getValue((Property)PROPERTY_BY_DIRECTION.get(direction)) && (blockEntity = level.getBlockEntity(pos)) instanceof FumeDuctBlockEntity) {
            FumeDuctBlockEntity duct = (FumeDuctBlockEntity)blockEntity;
            if (connected) {
                duct.relink(direction);
            } else {
                duct.unlink(direction);
            }
        }
        if (!((Boolean)(state = (BlockState)state.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), (Comparable)Boolean.valueOf(connected))).getValue((Property)SIDE)).booleanValue() && SIDE_PROPERTY_BY_DIRECTION.containsKey(direction)) {
            state = (BlockState)state.setValue((Property)SIDE_PROPERTY_BY_DIRECTION.get(direction), (Comparable)Boolean.valueOf(neighborState.getBlock() instanceof FumeDuctBlock && (Boolean)neighborState.getValue((Property)SIDE) != false));
        }
        return state;
    }

    private static boolean connectsTo(Direction direction, BlockState neighborState) {
        Block block = neighborState.getBlock();
        if (block instanceof FumeDuctBlock) {
            if (((Boolean)neighborState.getValue((Property)SIDE)).booleanValue()) {
                return direction != Direction.UP;
            }
            return true;
        }
        return direction == Direction.DOWN && (block instanceof BoilerBlock || block instanceof BoilerTankBlock);
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.block();
    }

    public static boolean anyDuctInNetwork(Level level, BlockPos startPos, Predicate<BlockPos> memberTest) {
        BlockEntity blockEntity = level.getBlockEntity(startPos);
        if (!(blockEntity instanceof FumeDuctBlockEntity)) {
            return false;
        }
        FumeDuctBlockEntity start = (FumeDuctBlockEntity)blockEntity;
        HashSet<BlockPos> visited = new HashSet<BlockPos>();
        ArrayDeque<FumeDuctBlockEntity> queue = new ArrayDeque<FumeDuctBlockEntity>();
        visited.add(start.getBlockPos());
        queue.add(start);
        while (!queue.isEmpty() && visited.size() <= 4096) {
            FumeDuctBlockEntity current = (FumeDuctBlockEntity)((Object)queue.poll());
            if (current.isRemoved()) continue;
            if (memberTest.test(current.getBlockPos())) {
                return true;
            }
            for (BlockEntity neighbor : current.liveNeighbors()) {
                FumeDuctBlockEntity neighborDuct;
                if (!(neighbor instanceof FumeDuctBlockEntity) || !visited.add((neighborDuct = (FumeDuctBlockEntity)neighbor).getBlockPos())) continue;
                queue.add(neighborDuct);
            }
        }
        return false;
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rng) {
        List<Direction> exits = FumeDuctBlock.openVentExits(level, pos);
        if (exits.isEmpty()) {
            return;
        }
        if (!FumeDuctBlock.anyDuctInNetwork(level, pos, memberPos -> BoilerBlock.isFedByLitBoiler(level, memberPos))) {
            return;
        }
        Direction exit = exits.get(0);
        Vec3 drift = BoilerBlock.computeWindDrift(level, pos);
        double x = (double)pos.getX() + 0.5 + (double)exit.getStepX() * 0.4;
        double y = (double)pos.getY() + 0.5 + (double)exit.getStepY() * 0.4;
        double z = (double)pos.getZ() + 0.5 + (double)exit.getStepZ() * 0.4;
        int passes = (Integer)TIBAMConfig.COMMON.boilerSmokeMultiplier.get();
        for (int i = 0; i < passes; ++i) {
            level.addParticle((ParticleOptions)TFCParticles.SMOKE_4.get(), x, y, z, drift.x, 0.07, drift.z);
        }
    }

    public static List<Direction> openVentExits(Level level, BlockPos pos) {
        List<Object> exits;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof FumeDuctBlock)) {
            return List.of();
        }
        boolean side = (Boolean)state.getValue((Property)SIDE);
        Direction[] candidates = side ? NON_DOWN_DIRECTIONS : Direction.values();
        Direction connection = null;
        int connectionCount = 0;
        for (Direction dir : candidates) {
            if (!((Boolean)state.getValue((Property)PROPERTY_BY_DIRECTION.get(dir))).booleanValue()) continue;
            ++connectionCount;
            connection = dir;
        }
        if (connectionCount != 1) {
            exits = List.of();
        } else {
            Direction exit = connection.getOpposite();
            if (side && exit == Direction.DOWN) {
                exits = List.of();
            } else {
                boolean clear = exit.getAxis() == Direction.Axis.Y ? BoilerBlock.isVentClear(level, pos.relative(exit)) : BoilerBlock.isVentClear(level, pos.relative(exit), exit, 8);
                exits = clear ? List.of(exit) : List.of();
            }
        }
        return exits;
    }
}

