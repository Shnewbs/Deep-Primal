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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import tfcmt.tfcitembarrelsandmore.common.ITollableSink;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blocks.rotation.PropellerBlock;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.PropellerListenerSet;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.PropellerPhysicsListener;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSRegistration;

public class PropellerBlockEntity
extends BlockEntity
implements RotationSinkBlockEntity,
ITollableSink {
    private final SinkNode sinkNode;
    private boolean invalidInNetwork = false;
    private final Direction facing;
    private PropellerPhysicsListener vsListener = null;
    private boolean vsWasRunning = false;
    private int vsResolveTimer = 0;
    private LoadedServerShip previousShip = null;

    public PropellerBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)TIBAMBlockEntities.PROPELLER.get(), pos, state);
        Direction facing = (Direction)state.getValue((Property)PropellerBlock.FACING);
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, PropellerBlockEntity be) {
        if (VSCompat.isLoaded()) {
            be.tickValkyrienSkies(level, pos, state);
        }
    }

    private void tickValkyrienSkies(Level level, BlockPos pos, BlockState state) {
        boolean running;
        double maxRpm;
        PropellerBlock propellerBlock;
        Block block = state.getBlock();
        boolean isBrass = block instanceof PropellerBlock && (propellerBlock = (PropellerBlock)block).requiresCastIron();
        float rawSpeed = this.getRotationSpeed();
        double rpm = PropellerBlockEntity.rotationSpeedToRpm(rawSpeed);
        if (rpm > (maxRpm = (isBrass ? (Double)TIBAMConfig.COMMON.brassMaxRpm.get() : (Double)TIBAMConfig.COMMON.copperMaxRpm.get()).doubleValue())) {
            this.performNetworkAction(NetworkAction.REMOVE);
            level.destroyBlock(pos, true);
            return;
        }
        if (!VSRegistration.isRegistered()) {
            return;
        }
        boolean bl = running = rpm > 0.0;
        if (!running) {
            if (this.vsWasRunning && this.vsListener != null) {
                this.vsListener.setCurrentRpm(0.0);
            }
            this.vsWasRunning = false;
            return;
        }
        boolean justStarted = !this.vsWasRunning;
        this.vsWasRunning = true;
        ++this.vsResolveTimer;
        if (justStarted || this.vsListener == null || this.vsResolveTimer >= 20) {
            this.vsResolveTimer = 0;
            LoadedServerShip ship = VSCompat.getShipAt(level, pos);
            if (this.previousShip != null && (ship == null || ship.getId() != this.previousShip.getId())) {
                PropellerListenerSet oldListenerSet = (PropellerListenerSet)this.previousShip.getAttachment(PropellerListenerSet.class);
                if (oldListenerSet != null) {
                    oldListenerSet.remove(pos.immutable());
                }
                this.previousShip = null;
            }
            if (ship == null) {
                this.vsListener = null;
                return;
            }
            PropellerListenerSet listenerSet = (PropellerListenerSet)ship.getOrPutAttachment(PropellerListenerSet.class, PropellerListenerSet::new);
            this.vsListener = listenerSet.get(pos.immutable());
            if (this.vsListener == null) {
                Direction facing = (Direction)state.getValue((Property)PropellerBlock.FACING);
                PropellerPhysicsListener.PropellerType type = isBrass ? PropellerPhysicsListener.PropellerType.BRASS : PropellerPhysicsListener.PropellerType.COPPER;
                this.vsListener = new PropellerPhysicsListener(pos.immutable(), facing, type);
                listenerSet.put(pos.immutable(), this.vsListener);
            }
            this.previousShip = ship;
        }
        if (this.vsListener != null) {
            double signedRpm = rawSpeed < 0.0f ? -rpm : rpm;
            this.vsListener.setCurrentRpm(signedRpm);
        }
    }

    private static double rotationSpeedToRpm(float radPerTick) {
        return (double)Math.abs(radPerTick) * 20.0 * 60.0 / (Math.PI * 2);
    }

    private float getRotationSpeed() {
        Rotation rotation = this.sinkNode.rotation();
        return rotation != null ? this.signForRotation(rotation) * rotation.speed() : 0.0f;
    }

    private float signForRotation(Rotation rotation) {
        return rotation.direction().getAxisDirection() == this.facing.getAxisDirection() ? -1.0f : 1.0f;
    }

    public void onLoad() {
        super.onLoad();
        this.performNetworkAction(NetworkAction.ADD);
    }

    public void onUnloaded() {
        if (this.level == null) {
            return;
        }
        if (!this.level.isClientSide) {
            PropellerListenerSet listenerSet;
            this.performNetworkAction(NetworkAction.REMOVE);
            if (this.previousShip != null && VSCompat.isLoaded() && VSRegistration.isRegistered() && (listenerSet = (PropellerListenerSet)this.previousShip.getAttachment(PropellerListenerSet.class)) != null) {
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
        this.performNetworkAction(NetworkAction.UPDATE);
    }

    public float getRotationAngle(float partialTick) {
        Rotation rotation = this.sinkNode.rotation();
        return rotation != null ? this.signForRotation(rotation) * rotation.angle(partialTick) : 0.0f;
    }

    public boolean isRotating() {
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
}

