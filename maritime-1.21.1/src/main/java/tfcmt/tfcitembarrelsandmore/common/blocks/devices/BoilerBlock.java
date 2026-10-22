package tfcmt.tfcitembarrelsandmore.common.blocks.devices;

import java.util.List;
import javax.annotation.Nullable;
import net.dries007.tfc.client.ClimateRenderCache;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.BoilerTankBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.FumeDuctBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.SinglePistonEngineBlock;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class BoilerBlock
extends DeviceBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BooleanProperty.create((String)"lit");
    public static final BooleanProperty LINKED = BooleanProperty.create((String)"linked");
    public static final BooleanProperty STEEL_TANK = BooleanProperty.create((String)"steel_tank");
    private static final double VENT_Y = 0.625;
    private static final double VENT_OFFSET = 0.001;
    private static final int MAX_VENT_SCAN_STEPS = 384;

    public BoilerBlock(ExtendedProperties properties) {
        super(properties, DeviceBlock.InventoryRemoveBehavior.DROP);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue((Property)FACING, (Comparable)Direction.NORTH)).setValue((Property)LIT, (Comparable)Boolean.valueOf(false))).setValue((Property)LINKED, (Comparable)Boolean.valueOf(false))).setValue((Property)STEEL_TANK, (Comparable)Boolean.valueOf(false)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING, LIT, LINKED, STEEL_TANK});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)((BlockState)this.defaultBlockState().setValue((Property)FACING, (Comparable)ctx.getHorizontalDirection().getOpposite())).setValue((Property)STEEL_TANK, (Comparable)Boolean.valueOf(BoilerBlock.hasSteelTankAbove(ctx.getLevel(), ctx.getClickedPos())));
    }

    private static boolean hasSteelTankAbove(Level level, BlockPos pos) {
        return level.getBlockState(pos.above()).is((Block)TIBAMBlocks.STEEL_BOILER_TANK.get());
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BoilerBlockEntity) {
            BoilerBlockEntity boiler = (BoilerBlockEntity)blockEntity;
            return boiler.onPlayerInteract(state, level, pos, player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != TIBAMBlockEntities.BOILER.get()) {
            return null;
        }
        return level.isClientSide ? null : (lvl, pos, st, be) -> BoilerBlockEntity.serverTick(lvl, pos, st, (BoilerBlockEntity)be);
    }

    public static boolean hasOpenVent(Level level, BlockPos boilerPos) {
        BlockPos above = boilerPos.above();
        BlockPos center = level.getBlockState(above).getBlock() instanceof BoilerTankBlock ? above.above() : above;
        return BoilerBlock.hasOpenVentAbove(level, center);
    }

    public static boolean hasOpenVentAbove(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof FumeDuctBlock) {
            return BoilerBlock.isVentPositionClear(level, pos);
        }
        if (BoilerBlock.isVentPositionClear(level, pos)) {
            return true;
        }
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                if (dx == 0 && dz == 0 || !BoilerBlock.isVentPositionClear(level, pos.offset(dx, 0, dz))) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean isVentPositionClear(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof FumeDuctBlock) {
            return FumeDuctBlock.anyDuctInNetwork(level, pos, member -> !FumeDuctBlock.openVentExits(level, member).isEmpty());
        }
        return BoilerBlock.isVentClear(level, pos);
    }

    public static boolean isFedByLitBoiler(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.getBlock() instanceof BoilerBlock && ((Boolean)below.getValue((Property)LIT)).booleanValue()) {
            return true;
        }
        if (below.getBlock() instanceof BoilerTankBlock) {
            BlockState belowBelow = level.getBlockState(pos.below(2));
            return belowBelow.getBlock() instanceof BoilerBlock && (Boolean)belowBelow.getValue((Property)LIT) != false;
        }
        return false;
    }

    static boolean isVentClear(Level level, BlockPos pos) {
        return BoilerBlock.isVentClear(level, pos, Direction.UP, 384);
    }

    static boolean isVentClear(Level level, BlockPos pos, Direction direction, int maxSteps) {
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int steps = 0; steps < maxSteps; ++steps) {
            BlockState state = level.getBlockState((BlockPos)cursor);
            if (!state.isAir() && state.getLightBlock((BlockGetter)level, (BlockPos)cursor) > 0) {
                return false;
            }
            cursor.move(direction);
        }
        return true;
    }

    static Vec3 computeWindDrift(Level level, BlockPos pos) {
        Vec3 localDrift;
        Vec3 shipVelocity;
        Vec2 wind = ClimateRenderCache.INSTANCE.getWind();
        float windStrength = wind.length();
        double driftX = (double)(wind.x * windStrength) * 0.05;
        double driftZ = (double)(wind.y * windStrength) * 0.05;
        if (VSCompat.isLoaded() && (shipVelocity = VSCompat.getShipVelocity(level, pos)) != null && (localDrift = VSCompat.worldDirectionToShipLocal(level, pos, new Vec3(driftX -= shipVelocity.x / 20.0, 0.0, driftZ -= shipVelocity.z / 20.0))) != null) {
            driftX = localDrift.x;
            driftZ = localDrift.z;
        }
        return new Vec3(driftX, 0.0, driftZ);
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rng) {
        boolean ventedElsewhere;
        if (!((Boolean)state.getValue((Property)LIT)).booleanValue()) {
            return;
        }
        double cx = (double)pos.getX() + 0.5;
        double cy = (double)pos.getY() + 0.9;
        double cz = (double)pos.getZ() + 0.5;
        Block aboveBlock = level.getBlockState(pos.above()).getBlock();
        boolean bl = ventedElsewhere = aboveBlock instanceof BoilerTankBlock || aboveBlock instanceof FumeDuctBlock;
        if (!ventedElsewhere) {
            if (rng.nextInt(10) == 0) {
                level.addParticle((ParticleOptions)ParticleTypes.FLAME, cx + rng.nextDouble() * 0.2 - 0.1, cy + 0.05, cz + rng.nextDouble() * 0.2 - 0.1, 0.0, 0.02, 0.0);
            }
            Vec3 drift = BoilerBlock.computeWindDrift(level, pos);
            level.addParticle((ParticleOptions)TFCParticles.SMOKE_4.get(), cx + rng.nextDouble() * 0.4 - 0.2, cy + 0.1, cz + rng.nextDouble() * 0.4 - 0.2, drift.x, 0.04, drift.z);
            int extraSmoke = (Integer)TIBAMConfig.COMMON.boilerSmokeMultiplier.get() - 1;
            for (int i = 0; i < extraSmoke; ++i) {
                level.addParticle((ParticleOptions)TFCParticles.SMOKE_4.get(), cx + rng.nextDouble() * 0.4 - 0.2, cy + 0.1, cz + rng.nextDouble() * 0.4 - 0.2, drift.x, 0.04, drift.z);
            }
        }
        if (rng.nextInt(24) == 0) {
            level.playLocalSound(cx, cy, cz, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.6f, 1.0f, false);
        }
        if (rng.nextInt(4) != 0) {
            return;
        }
        Direction facing = (Direction)state.getValue((Property)FACING);
        this.emitFrontVentParticles(level, pos, facing, rng);
    }

    private void emitFrontVentParticles(Level level, BlockPos pos, Direction facing, RandomSource rng) {
        double velZ;
        double velX;
        double wz8;
        double wx8;
        double bx = pos.getX();
        double by = (double)pos.getY() + 0.625;
        double bz = pos.getZ();
        switch (facing) {
            case NORTH: {
                double col8 = 0.5;
                double col9 = 0.4375;
                wx8 = bx + col8;
                wz8 = bz + 0.001;
                double wx9 = bx + col9;
                double wz9 = bz + 0.001;
                velX = 0.0;
                velZ = -0.01;
                break;
            }
            case SOUTH: {
                double col8 = 0.5;
                double col9 = 0.5625;
                wx8 = bx + col8;
                wz8 = bz + 1.0 - 0.001;
                double wx9 = bx + col9;
                double wz9 = bz + 1.0 - 0.001;
                velX = 0.0;
                velZ = 0.01;
                break;
            }
            case EAST: {
                double col8 = 0.5;
                double col9 = 0.5625;
                wx8 = bx + 1.0 - 0.001;
                wz8 = bz + col8;
                double wx9 = bx + 1.0 - 0.001;
                double wz9 = bz + col9;
                velX = 0.01;
                velZ = 0.0;
                break;
            }
            case WEST: {
                double col8 = 0.5;
                double col9 = 0.4375;
                wx8 = bx + 0.001;
                wz8 = bz + col8;
                double wx9 = bx + 0.001;
                double wz9 = bz + col9;
                velX = -0.01;
                velZ = 0.0;
                break;
            }
            default: {
                return;
            }
        }
        double velY = 0.02;
        level.addParticle((ParticleOptions)ParticleTypes.FLAME, wx8, by, wz8, velX, velY, velZ);
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide && fromPos.equals((Object)pos.above())) {
            boolean steelTankNow = BoilerBlock.hasSteelTankAbove(level, pos);
            if ((Boolean)state.getValue((Property)STEEL_TANK) != steelTankNow) {
                level.setBlock(pos, (BlockState)state.setValue((Property)STEEL_TANK, (Comparable)Boolean.valueOf(steelTankNow)), 2);
            }
        }
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
            BoilerBlockEntity.spawnRodDrop(level, pos, state);
            BlockPos enginePos = pos.relative(((Direction)state.getValue((Property)FACING)).getOpposite());
            BlockState engineState = level.getBlockState(enginePos);
            if (engineState.getBlock() instanceof SinglePistonEngineBlock) {
                level.setBlock(enginePos, (BlockState)engineState.setValue((Property)SinglePistonEngineBlock.LINKED, (Comparable)Boolean.valueOf(false)), 2);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}

