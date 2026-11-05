package tfcmt.tfcitembarrelsandmore.common.blockentities;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.blockentities.rotation.RotatingBlockEntity;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.rotation.NetworkAction;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.Rotation;
import net.dries007.tfc.util.rotation.SinkNode;
import net.dries007.tfc.util.rotation.SourceNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfcmt.tfcitembarrelsandmore.common.PistonPressure;
import tfcmt.tfcitembarrelsandmore.common.RotationNetworkWalker;
import tfcmt.tfcitembarrelsandmore.common.SinkTollRegistry;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blockentities.BoilerTankBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.TripleExpansionEngineBlock;
import tfcmt.tfcitembarrelsandmore.common.network.TIBAMNetwork;
import tfcmt.tfcitembarrelsandmore.compat.create.EngineSoundInstance;
import tfcmt.tfcitembarrelsandmore.compat.valkyrienskies.VSCompat;

public class TripleExpansionEngineBlockEntity
extends TickableInventoryBlockEntity<ItemStackHandler>
implements RotatingBlockEntity,
EngineSoundInstance.SteamEngineAccessor {
    private static final Logger LOGGER = LogManager.getLogger((String)"TIBAM/TripleEngine");
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.triple_expansion_engine");
    private static final int INVENTORY_SIZE = 0;
    private boolean running = false;
    private transient boolean soundStarted = false;
    private float prevSpeed = Float.NaN;
    private float lastSyncedSpeed = Float.NaN;
    private float steamDrainAccumulator = 0.0f;
    private int tollHeartbeatCounter = 0;
    private float currentTollRate = 0.0f;
    private float tollAccumulator = 0.0f;
    private boolean invalidInNetwork = false;
    private final SourceNode sourceNode;

    public void markAsInvalidInNetwork() {
        this.invalidInNetwork = true;
    }

    public boolean isInvalidInNetwork() {
        return this.invalidInNetwork;
    }

    public Node getRotationNode() {
        return this.sourceNode;
    }

    public TripleExpansionEngineBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.TRIPLE_EXPANSION_ENGINE.get(), pos, state, self -> new ItemStackHandler(0), NAME.getString());
        Direction facing;
        Direction output = facing = (Direction)state.getValue((Property)TripleExpansionEngineBlock.FACING);
        this.sourceNode = new SourceNode(pos.relative(output), Node.ofAxis((Direction.Axis)output.getAxis()), output.getOpposite(), 0.0f){};
    }

    protected void onLoadAdditional() {
        this.performNetworkAction(NetworkAction.ADD_SOURCE);
    }

    protected void onUnloadAdditional() {
        this.performNetworkAction(NetworkAction.REMOVE);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TripleExpansionEngineBlockEntity engine) {
        boolean running;
        Rotation.Tickable tickable = engine.sourceNode.rotation();
        if (tickable != null) {
            tickable.tick();
        }
        if ((running = ((Boolean)state.getValue((Property)TripleExpansionEngineBlock.RUNNING)).booleanValue()) && !engine.soundStarted) {
            EngineSoundInstance.startFor(engine, level, pos, TripleExpansionEngineBlock.RUNNING);
            engine.soundStarted = true;
        }
        if (!running) {
            engine.soundStarted = false;
        }
    }

    public void tickFromTank(BoilerTankBlockEntity tank, int psi, int throttle, Level level, BlockPos pos, BlockState state) {
        boolean shouldRun;
        this.destroyIfInvalid(level, pos);
        float baseSpeed = TIBAMConfig.baseSpeedRadPerTick();
        float maxMultiplier = (float)((Double)TIBAMConfig.SERVER.engineMaxSpeedMultiplier.get()).doubleValue();
        float drainAtWork = (float)((Double)TIBAMConfig.SERVER.tankSteamDrainAtWorking.get()).doubleValue();
        float drainAtMax = (float)((Double)TIBAMConfig.SERVER.tankSteamDrainAtMax.get()).doubleValue();
        PistonPressure tier = PistonPressure.getTier(psi);
        float throttleFraction = (float)throttle / 100.0f;
        boolean bl = shouldRun = throttle > 0 && tier != null && tier.hasOutput();
        if (shouldRun) {
            float drainAmount = TripleExpansionEngineBlockEntity.computeSteamDrain(psi, drainAtWork, drainAtMax) * throttleFraction;
            this.steamDrainAccumulator += drainAmount;
            int wholeDrain = 0;
            if (this.steamDrainAccumulator >= 1.0f) {
                wholeDrain = (int)this.steamDrainAccumulator;
                this.steamDrainAccumulator -= (float)wholeDrain;
                tank.drainSteam(wholeDrain);
            }
            float targetSpeed = baseSpeed * ((float)psi / 100.0f) * maxMultiplier * throttleFraction;
            if (!Float.isNaN(this.prevSpeed) && targetSpeed < this.prevSpeed) {
                this.performNetworkAction(NetworkAction.REMOVE);
                this.sourceNode.rotation().setSpeed(targetSpeed);
                this.performNetworkAction(NetworkAction.ADD_SOURCE);
            } else {
                this.sourceNode.rotation().setSpeed(targetSpeed);
            }
            this.prevSpeed = targetSpeed;
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                if (Math.abs(targetSpeed - this.lastSyncedSpeed) > 1.0E-4f) {
                    TIBAMNetwork.syncTripleEngineSpeed(pos, targetSpeed, serverLevel);
                    this.lastSyncedSpeed = targetSpeed;
                }
            }
        } else {
            this.sourceNode.rotation().setSpeed(0.0f);
            this.steamDrainAccumulator = 0.0f;
            this.prevSpeed = Float.NaN;
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                if (this.lastSyncedSpeed != 0.0f) {
                    TIBAMNetwork.syncTripleEngineSpeed(pos, 0.0f, serverLevel);
                    this.lastSyncedSpeed = 0.0f;
                }
            }
        }
        this.running = shouldRun;
        if ((Boolean)state.getValue((Property)TripleExpansionEngineBlock.RUNNING) != shouldRun) {
            level.setBlock(pos, (BlockState)state.setValue((Property)TripleExpansionEngineBlock.RUNNING, Boolean.valueOf(shouldRun)), 2);
            this.performNetworkAction(NetworkAction.UPDATE);
        }
        ++this.tollHeartbeatCounter;
        if (this.tollHeartbeatCounter >= 20) {
            this.tollHeartbeatCounter = 0;
            this.currentTollRate = 0.0f;
            for (SinkNode sink : RotationNetworkWalker.findSinks(level, this.sourceNode.pos(), this.sourceNode.network())) {
                BlockPos sinkPos = sink.pos();
                BlockEntity sinkBe = level.getBlockEntity(sinkPos);
                if (sinkBe == null || !SinkTollRegistry.isWorking(level, sinkPos, sinkBe, (Node)sink)) continue;
                this.currentTollRate += (float)SinkTollRegistry.getTier(level.getBlockState(sinkPos).getBlock()).getRate();
            }
        }
        if (this.currentTollRate > 0.0f) {
            this.tollAccumulator += this.currentTollRate / 20.0f;
            int tollWhole = (int)this.tollAccumulator;
            if (tollWhole != 0) {
                this.tollAccumulator -= (float)tollWhole;
                tank.drainSteam(tollWhole);
            }
        }
        this.setChanged();
        this.markForSync();
        if (LOGGER.isDebugEnabled() && level.getGameTime() % 100L == 0L) {
            LOGGER.debug("[tripleEngine] pos={} psi={} throttle={} running={}", (Object)pos, (Object)psi, (Object)throttle, (Object)shouldRun);
        }
    }

    public void tickIdle(Level level, BlockPos pos, BlockState state) {
        this.destroyIfInvalid(level, pos);
        this.sourceNode.rotation().setSpeed(0.0f);
        this.steamDrainAccumulator = 0.0f;
        this.prevSpeed = Float.NaN;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (this.lastSyncedSpeed != 0.0f) {
                TIBAMNetwork.syncTripleEngineSpeed(pos, 0.0f, serverLevel);
                this.lastSyncedSpeed = 0.0f;
            }
        }
        this.running = false;
        if (((Boolean)state.getValue((Property)TripleExpansionEngineBlock.RUNNING)).booleanValue()) {
            level.setBlock(pos, (BlockState)state.setValue((Property)TripleExpansionEngineBlock.RUNNING, Boolean.valueOf(false)), 2);
            this.performNetworkAction(NetworkAction.UPDATE);
        }
        this.setChanged();
        this.markForSync();
    }

    private static float computeSteamDrain(int psi, float drainAtWorking, float drainAtMax) {
        int WORKING_PSI = 45;
        if (psi <= 0) {
            return 0.0f;
        }
        if (psi >= 100) {
            return drainAtMax;
        }
        if (psi <= 45) {
            return drainAtWorking * ((float)psi / 45.0f);
        }
        float t = (float)(psi - 45) / 55.0f;
        return drainAtWorking + t * (drainAtMax - drainAtWorking);
    }

    public void dropRod(Level level, BlockPos pos, BlockState state) {
        Direction backFace = ((Direction)state.getValue((Property)TripleExpansionEngineBlock.FACING)).getOpposite();
        double rx = (double)pos.getX() + 0.5 + (double)backFace.getStepX() * 0.6;
        double ry = (double)pos.getY() + 0.5;
        double rz = (double)pos.getZ() + 0.5 + (double)backFace.getStepZ() * 0.6;
        Vec3 dropPos = VSCompat.spawnPosToWorld(level, pos, rx, ry, rz);
        ItemStack rod = new ItemStack((ItemLike)TFCItems.METAL_ITEMS.get(Metal.CAST_IRON).get(Metal.ItemType.ROD).get());
        level.addFreshEntity((Entity)new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, rod));
    }

    public void setClientSpeed(float speed) {
        this.sourceNode.rotation().setSpeed(speed);
    }

    public boolean isRunning() {
        return this.running;
    }

    @Override
    @Nullable
    public Rotation getSourceRotation() {
        return this.sourceNode.rotation();
    }

    public float getRotationAngle(float partialTick) {
        return RotatingBlockEntity.super.getRotationAngle(partialTick);
    }



    @Nullable
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return null;
    }

    public Component getDisplayName() {
        return NAME;
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.invalidInNetwork = tag.getBoolean("InvalidInNetwork");
        this.sourceNode.rotation().loadFromTag(tag);
    }

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("InvalidInNetwork", this.invalidInNetwork);
        this.sourceNode.rotation().saveToTag(tag);
    }
}

