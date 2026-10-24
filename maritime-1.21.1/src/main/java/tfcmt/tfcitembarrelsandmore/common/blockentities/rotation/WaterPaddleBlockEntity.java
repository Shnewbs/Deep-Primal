package tfcmt.tfcitembarrelsandmore.common.blockentities.rotation;

import net.dries007.tfc.common.blockentities.rotation.RotationSinkBlockEntity;
import net.dries007.tfc.util.rotation.NetworkAction;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.Rotation;
import net.dries007.tfc.util.rotation.SinkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import tfcmt.tfcitembarrelsandmore.common.ITollableSink;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blocks.rotation.WaterPaddleBlock;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.PropellerListenerSet;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSRegistration;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.WaterPaddleListenerSet;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.WaterPaddlePhysicsListener;

public class WaterPaddleBlockEntity
extends BlockEntity
implements RotationSinkBlockEntity,
ITollableSink {
    private final SinkNode sinkNode;
    private boolean invalidInNetwork = false;
    private boolean registeredInNetwork = false;
    private static final int MAX_CHAIN_LENGTH = 32;
    private final Direction facing;
    private WaterPaddlePhysicsListener vsListener = null;
    private boolean vsWasRunning = false;
    private int vsResolveTimer = 0;
    private LoadedServerShip previousShip = null;

    public WaterPaddleBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.WATER_PADDLE.get(), pos, state);
        Direction facing = (Direction)state.getValue((Property)WaterPaddleBlock.FACING);
        this.sinkNode = new SinkNode(pos, facing){};
        this.facing = facing;
    }

    public Node getRotationNode() {
        return this.sinkNode;
    }

    public void markAsInvalidInNetwork() {
        this.invalidInNetwork = true;
    }

    public boolean isInvalidInNetwork() {
        return this.invalidInNetwork;
    }

    public void performNetworkAction(NetworkAction action) {
        RotationSinkBlockEntity.super.performNetworkAction(action);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WaterPaddleBlockEntity be) {
        if (VSCompat.isLoaded()) {
            be.tickValkyrienSkies(level, pos, state);
        }
    }

    private void tickValkyrienSkies(Level level, BlockPos pos, BlockState state) {
        boolean running;
        float radPerTick = this.getRotationSpeed();
        double signedRpm = WaterPaddleBlockEntity.rotationSpeedToSignedRpm(radPerTick);
        double maxRpm = (Double)TIBAMConfig.COMMON.paddleMaxRpm.get();
        if (!this.isMirror() && Math.abs(signedRpm) > maxRpm) {
            WaterPaddleBlockEntity.destroyMirrorChain(level, pos);
            this.performNetworkAction(NetworkAction.REMOVE);
            level.destroyBlock(pos, true);
            return;
        }
        if (!VSRegistration.isPaddleRegistered()) {
            return;
        }
        boolean bl = running = signedRpm != 0.0;
        if (!running) {
            if (this.vsWasRunning && this.vsListener != null) {
                this.vsListener.setCurrentSignedRpm(0.0);
            }
            this.vsWasRunning = false;
            return;
        }
        boolean justStarted = !this.vsWasRunning;
        this.vsWasRunning = true;
        ++this.vsResolveTimer;
        if (justStarted || this.vsListener == null || this.vsResolveTimer >= 20) {
            ChainInfo chain;
            this.vsResolveTimer = 0;
            LoadedServerShip ship = VSCompat.getShipAt(level, pos);
            if (this.previousShip != null && (ship == null || ship.getId() != this.previousShip.getId())) {
                WaterPaddleListenerSet oldListenerSet = (WaterPaddleListenerSet)this.previousShip.getAttachment(WaterPaddleListenerSet.class);
                if (oldListenerSet != null) {
                    oldListenerSet.remove(pos.immutable());
                }
                this.previousShip = null;
            }
            if (ship == null) {
                this.vsListener = null;
                return;
            }
            WaterPaddleListenerSet listenerSet = (WaterPaddleListenerSet)ship.getOrPutAttachment(WaterPaddleListenerSet.class, WaterPaddleListenerSet::new);
            this.vsListener = listenerSet.get(pos.immutable());
            if (this.vsListener == null) {
                Direction facing = (Direction)state.getValue((Property)WaterPaddleBlock.FACING);
                this.vsListener = new WaterPaddlePhysicsListener(pos.immutable(), facing.getAxis());
                listenerSet.put(pos.immutable(), this.vsListener);
            }
            this.previousShip = ship;
            PropellerListenerSet propellerSet = (PropellerListenerSet)ship.getAttachment(PropellerListenerSet.class);
            this.vsListener.setPropellersPresent(propellerSet != null && !propellerSet.isEmpty());
            int chainPosition = 1;
            if (this.isMirror() && (chain = this.resolveChain()) != null) {
                chainPosition = chain.position();
            }
            this.vsListener.setThrustMultiplier(WaterPaddleBlockEntity.thrustMultiplierForChainPosition(chainPosition));
        }
        if (this.vsListener != null) {
            this.vsListener.setCurrentSignedRpm(signedRpm);
        }
    }

    private static double rotationSpeedToSignedRpm(float radPerTick) {
        return (double)radPerTick * 20.0 * 60.0 / (Math.PI * 2);
    }

    private float getRotationSpeed() {
        if (this.isMirror()) {
            ChainInfo chain = this.resolveChain();
            return chain != null ? chain.root().getRotationSpeed() : 0.0f;
        }
        Rotation rotation = this.sinkNode.rotation();
        return rotation != null ? this.signForRotation(rotation) * rotation.speed() : 0.0f;
    }

    private float signForRotation(Rotation rotation) {
        float base = rotation.direction().getAxisDirection() == this.facing.getAxisDirection() ? -1.0f : 1.0f;
        return this.facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? -base : base;
    }

    private boolean isMirror() {
        if (this.level == null) {
            return false;
        }
        return this.level.getBlockState(this.worldPosition.relative(this.facing)).getBlock() instanceof WaterPaddleBlock;
    }

    private ChainInfo resolveChain() {
        if (this.level == null) {
            return null;
        }
        WaterPaddleBlockEntity cursor = this;
        int position = 1;
        for (int i = 0; i < 32; ++i) {
            BlockPos neighborPos = cursor.getBlockPos().relative(cursor.facing);
            if (!(this.level.getBlockState(neighborPos).getBlock() instanceof WaterPaddleBlock)) {
                return new ChainInfo(cursor, position);
            }
            BlockEntity blockEntity = this.level.getBlockEntity(neighborPos);
            if (!(blockEntity instanceof WaterPaddleBlockEntity)) {
                return null;
            }
            WaterPaddleBlockEntity neighborPaddle = (WaterPaddleBlockEntity)blockEntity;
            cursor = neighborPaddle;
            ++position;
        }
        return null;
    }

    static double thrustMultiplierForChainPosition(int position) {
        return switch (position) {
            case 1 -> 1.0;
            case 2 -> 1.75;
            case 3 -> 1.5;
            case 4 -> 1.25;
            default -> 1.1;
        };
    }

    private static void destroyMirrorChain(Level level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity blockEntity = level.getBlockEntity(neighborPos);
            if (!(blockEntity instanceof WaterPaddleBlockEntity)) continue;
            WaterPaddleBlockEntity neighbor = (WaterPaddleBlockEntity)blockEntity;
            if (neighbor.facing != dir.getOpposite()) continue;
            WaterPaddleBlockEntity.destroyMirrorChain(level, neighborPos);
            level.destroyBlock(neighborPos, true);
        }
    }

    public void onLoad() {
        super.onLoad();
        if (!this.isMirror()) {
            this.performNetworkAction(NetworkAction.ADD);
            this.registeredInNetwork = true;
        }
    }

    public void onUnloaded() {
        if (this.level == null) {
            return;
        }
        if (!this.level.isClientSide) {
            WaterPaddleListenerSet listenerSet;
            if (this.registeredInNetwork) {
                this.performNetworkAction(NetworkAction.REMOVE);
                this.registeredInNetwork = false;
            }
            if (this.previousShip != null && VSCompat.isLoaded() && VSRegistration.isPaddleRegistered() && (listenerSet = (WaterPaddleListenerSet)this.previousShip.getAttachment(WaterPaddleListenerSet.class)) != null) {
                listenerSet.remove(this.worldPosition.immutable());
            }
        }
        this.vsListener = null;
        this.previousShip = null;
        this.vsWasRunning = false;
        this.vsResolveTimer = 0;
    }

    public void onChunkUnloaded() {
        this.onUnloaded();
        super.onChunkUnloaded();
    }

    public void setRemoved() {
        this.onUnloaded();
        super.setRemoved();
    }

    public void onNeighborChanged() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        boolean mirror = this.isMirror();
        if (mirror) {
            if (this.registeredInNetwork) {
                this.performNetworkAction(NetworkAction.REMOVE);
                this.registeredInNetwork = false;
            }
        } else if (!this.registeredInNetwork) {
            this.performNetworkAction(NetworkAction.ADD);
            this.registeredInNetwork = true;
        } else {
            this.performNetworkAction(NetworkAction.UPDATE);
        }
    }

    public float getRotationAngle(float partialTick) {
        if (this.isMirror()) {
            ChainInfo chain = this.resolveChain();
            return chain != null ? chain.root().getRotationAngle(partialTick) : 0.0f;
        }
        Rotation rotation = this.sinkNode.rotation();
        return rotation != null ? this.signForRotation(rotation) * rotation.angle(partialTick) : 0.0f;
    }

    public boolean isRotating() {
        if (this.isMirror()) {
            ChainInfo chain = this.resolveChain();
            return chain != null && chain.root().isRotating();
        }
        return this.sinkNode.rotation() != null;
    }

    @Override
    public boolean isTollableWorking() {
        return this.isRotating();
    }

    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("invalid", this.invalidInNetwork);
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.invalidInNetwork = tag.getBoolean("invalid");
    }

    private record ChainInfo(WaterPaddleBlockEntity root, int position) {
    }
}

