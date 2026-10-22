package tfcmt.tfcitembarrelsandmore.common.blockentities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.blockentities.rotation.RotationSinkBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.IntArrayBuilder;
import net.dries007.tfc.util.rotation.NetworkAction;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.Rotation;
import net.dries007.tfc.util.rotation.SinkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import tfcmt.tfcitembarrelsandmore.common.ITollableSink;
import tfcmt.tfcitembarrelsandmore.common.PistonPressure;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMConfig;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.AirCompressorBlock;
import tfcmt.tfcitembarrelsandmore.common.container.AirCompressorContainer;
import tfcmt.tfcitembarrelsandmore.common.items.AirHoseItem;

public class AirCompressorBlockEntity
extends TickableInventoryBlockEntity<ItemStackHandler>
implements RotationSinkBlockEntity,
MenuProvider,
ITollableSink {
    private static final Component NAME = Component.translatable((String)"block.tfc_item_barrels_and_more.air_compressor");
    public static final int HOSE_SLOTS = 6;
    public static final int MAX_CONNECTIONS = 1;
    @javax.annotation.Nullable
    private SinkNode sinkNode = null;
    private boolean invalidInNetwork = false;
    private int compressedAir = 0;
    private int psi = 0;
    private int bleederRate = 0;
    private int productionTickCounter = 0;
    private float bleederTickAccumulator = 0.0f;
    private final Set<UUID> linkedPlayers = new HashSet<UUID>();
    private static final Map<UUID, Set<GlobalPos>> LINKED_COMPRESSORS = new ConcurrentHashMap<UUID, Set<GlobalPos>>();

    public ItemStackHandler getInventory() {
        return (ItemStackHandler)this.inventory;
    }

    public int getTotalHoseCount() {
        int total = 0;
        for (int i = 0; i < ((ItemStackHandler)this.inventory).getSlots(); ++i) {
            total += ((ItemStackHandler)this.inventory).getStackInSlot(i).getCount();
        }
        return total;
    }

    public void markAsInvalidInNetwork() {
        this.invalidInNetwork = true;
    }

    public boolean isInvalidInNetwork() {
        return this.invalidInNetwork;
    }

    public Node getRotationNode() {
        return this.sinkNode;
    }

    public static Set<GlobalPos> getLinkedCompressors(UUID playerUUID) {
        return LINKED_COMPRESSORS.getOrDefault(playerUUID, Collections.emptySet());
    }

    private GlobalPos globalPos() {
        return GlobalPos.of((ResourceKey)this.level.dimension(), (BlockPos)this.worldPosition);
    }

    public AirCompressorBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)TIBAMBlockEntities.AIR_COMPRESSOR.get(), pos, state, self -> new ItemStackHandler(6){

            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() instanceof AirHoseItem;
            }

            protected void onContentsChanged(int slot) {
                self.setChanged();
                self.markForSync();
            }
        }, NAME.getString());
    }

    protected void onLoadAdditional() {
        if (this.level == null) {
            return;
        }
        Direction flywheelFace = (Direction)this.getBlockState().getValue((Property)AirCompressorBlock.FACING);
        this.sinkNode = new SinkNode(this.worldPosition, flywheelFace){};
        this.performNetworkAction(NetworkAction.ADD);
    }

    protected void onUnloadAdditional() {
        if (this.level != null && this.isRemoved()) {
            this.purgeFromLinkRegistry();
        }
        if (this.level == null || this.sinkNode == null) {
            return;
        }
        if (!this.level.isClientSide) {
            this.performNetworkAction(NetworkAction.REMOVE);
        }
        this.sinkNode = null;
    }

    public void onNeighbourChanged() {
        if (this.level == null || this.level.isClientSide || this.sinkNode == null) {
            return;
        }
        this.performNetworkAction(NetworkAction.UPDATE);
    }

    private void purgeFromLinkRegistry() {
        GlobalPos pos = this.globalPos();
        for (UUID uuid : this.linkedPlayers) {
            Set<GlobalPos> set = LINKED_COMPRESSORS.get(uuid);
            if (set == null) continue;
            set.remove(pos);
            if (!set.isEmpty()) continue;
            LINKED_COMPRESSORS.remove(uuid, set);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AirCompressorBlockEntity be) {
        be.tick(level, pos, state);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        int maxAir = (Integer)TIBAMConfig.SERVER.compressorMaxAir.get();
        int airPerPulse = (Integer)TIBAMConfig.SERVER.compressorAirPerPulse.get();
        int pulseRate = (Integer)TIBAMConfig.SERVER.compressorPulseRate.get();
        float minRpm = (float)((Double)TIBAMConfig.SERVER.compressorMinRpm.get()).doubleValue();
        float rpmCeiling = (float)((Double)TIBAMConfig.SERVER.compressorRpmCeiling.get()).doubleValue();
        float bpExponent = (float)((Double)TIBAMConfig.SERVER.boilerBackPressureExponent.get()).doubleValue();
        float rpm = 0.0f;
        if (this.sinkNode != null && this.sinkNode.rotation() != null) {
            rpm = (float)((double)Math.abs(this.sinkNode.rotation().speed()) * 600.0 / Math.PI);
        }
        int n = this.psi = maxAir > 0 ? Math.min(100, this.compressedAir * 100 / maxAir) : 0;
        if (this.psi >= PistonPressure.DANGER.getMin()) {
            float explosionRadius = (float)((Double)TIBAMConfig.SERVER.compressorExplosionRadius.get()).doubleValue();
            Level.ExplosionInteraction interaction = (Boolean)TIBAMConfig.SERVER.compressorExplosionBlockDamage.get() != false ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE;
            level.explode(null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, explosionRadius, false, interaction);
            level.removeBlock(pos, false);
            return;
        }
        if (rpm >= minRpm && this.compressedAir < maxAir) {
            ++this.productionTickCounter;
            if (this.productionTickCounter >= pulseRate) {
                float psiNorm;
                float backPressureFactor;
                this.productionTickCounter = 0;
                float productionFactor = Math.min((rpm - minRpm) / Math.max(1.0f, rpmCeiling - minRpm), 1.0f);
                float effectivePulse = (float)airPerPulse * productionFactor * (backPressureFactor = 1.0f - (float)Math.pow(psiNorm = (float)this.psi / 100.0f, bpExponent));
                int toAdd = Math.max(0, Math.round(effectivePulse));
                if (toAdd > 0) {
                    this.compressedAir = Math.min(this.compressedAir + toAdd, maxAir);
                }
            }
        } else {
            this.productionTickCounter = 0;
        }
        if (this.bleederRate > 0 && this.compressedAir > 0) {
            this.bleederTickAccumulator += (float)this.bleederRate / 20.0f;
            if (this.bleederTickAccumulator >= 1.0f) {
                int drain = (int)this.bleederTickAccumulator;
                this.bleederTickAccumulator -= (float)drain;
                this.compressedAir = Math.max(0, this.compressedAir - drain);
            }
        } else {
            this.bleederTickAccumulator = 0.0f;
        }
        this.setChanged();
        this.markForSync();
        this.checkForLastTickSync();
    }

    public int drainAir(int amount) {
        int actual = Math.min(amount, this.compressedAir);
        this.compressedAir = Math.max(0, this.compressedAir - actual);
        return actual;
    }

    public boolean link(Player player) {
        if (this.level == null) {
            return false;
        }
        UUID uuid = player.getUUID();
        if (this.linkedPlayers.contains(uuid) || this.linkedPlayers.size() >= 1) {
            return false;
        }
        this.linkedPlayers.add(uuid);
        LINKED_COMPRESSORS.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(this.globalPos());
        this.setChanged();
        this.markForSync();
        return true;
    }

    public boolean unlink(Player player) {
        boolean removed = this.linkedPlayers.remove(player.getUUID());
        if (removed) {
            Set<GlobalPos> set = LINKED_COMPRESSORS.get(player.getUUID());
            if (set != null) {
                set.remove(this.globalPos());
                if (set.isEmpty()) {
                    LINKED_COMPRESSORS.remove(player.getUUID(), set);
                }
            }
            this.setChanged();
            this.markForSync();
        }
        return removed;
    }

    public boolean isLinked(Player player) {
        return this.linkedPlayers.contains(player.getUUID());
    }

    public int getLinkCount() {
        return this.linkedPlayers.size();
    }

    public Set<UUID> getLinkedPlayerUUIDs() {
        return Collections.unmodifiableSet(this.linkedPlayers);
    }

    public boolean isPlayerConnected(Player player) {
        if (!this.linkedPlayers.contains(player.getUUID())) {
            return false;
        }
        int range = AirHoseItem.getRange(this.getTotalHoseCount());
        if (range <= 0) {
            return false;
        }
        Vec3 hosePort = this.hosePortPos();
        return player.distanceToSqr(hosePort) <= (double)range * (double)range;
    }

    public Vec3 hosePortPos() {
        return new Vec3((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5);
    }

    public int getPsi() {
        return this.psi;
    }

    public int getCompressedAir() {
        return this.compressedAir;
    }

    @Override
    public boolean isTollableWorking() {
        if (this.sinkNode == null || this.sinkNode.rotation() == null) {
            return false;
        }
        float rpm = (float)((double)Math.abs(this.sinkNode.rotation().speed()) * 600.0 / Math.PI);
        float minRpm = (float)((Double)TIBAMConfig.SERVER.compressorMinRpm.get()).doubleValue();
        int maxAir = (Integer)TIBAMConfig.SERVER.compressorMaxAir.get();
        return rpm >= minRpm && this.compressedAir < maxAir;
    }

    public int getMaxAir() {
        return (Integer)TIBAMConfig.SERVER.compressorMaxAir.get();
    }

    public int getBleederRate() {
        return this.bleederRate;
    }

    public PistonPressure getPressureTier() {
        return PistonPressure.getTier(this.psi);
    }

    public boolean isRotating() {
        return this.sinkNode != null && this.sinkNode.rotation() != null;
    }

    public boolean isConnectedToNetwork() {
        return this.sinkNode != null && this.sinkNode.isConnectedToNetwork();
    }

    @Nullable
    public Rotation getSinkRotation() {
        return this.sinkNode != null ? this.sinkNode.rotation() : null;
    }

    public float getCurrentRpm() {
        if (this.sinkNode == null || this.sinkNode.rotation() == null) {
            return 0.0f;
        }
        return (float)((double)Math.abs(this.sinkNode.rotation().speed()) * 600.0 / Math.PI);
    }

    public void setBleederRate(int value) {
        this.bleederRate = Math.max(0, Math.min(10, value));
        this.bleederTickAccumulator = 0.0f;
        this.setChanged();
        this.markForSync();
    }

    public void setCompressedAir(int value) {
        this.compressedAir = Math.max(0, Math.min(value, this.getMaxAir()));
        this.setChanged();
        this.markForSync();
    }

    public IntArrayBuilder getSyncableData() {
        return new IntArrayBuilder().add(() -> this.compressedAir, v -> {
            this.compressedAir = v;
        }).add(() -> this.psi, v -> {
            this.psi = v;
        }).add(() -> this.bleederRate, v -> {
            this.bleederRate = v;
        }).add(() -> Math.round(this.getCurrentRpm() * 10.0f), v -> {});
    }

    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return AirCompressorContainer.create(this, playerInv, windowId);
    }

    public Component getDisplayName() {
        return NAME;
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.compressedAir = tag.getInt("CompressedAir");
        this.bleederRate = tag.getInt("BleederRate");
        this.invalidInNetwork = tag.getBoolean("InvalidInNetwork");
        this.linkedPlayers.clear();
        ListTag connections = tag.getList("LinkedHoses", 11);
        for (int i = 0; i < connections.size(); ++i) {
            this.linkedPlayers.add(NbtUtils.loadUUID((Tag)connections.get(i)));
        }
        if (this.level != null) {
            for (UUID uuid : this.linkedPlayers) {
                LINKED_COMPRESSORS.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(this.globalPos());
            }
        }
    }

    public void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("CompressedAir", this.compressedAir);
        tag.putInt("BleederRate", this.bleederRate);
        tag.putBoolean("InvalidInNetwork", this.invalidInNetwork);
        ListTag connections = new ListTag();
        for (UUID uuid : this.linkedPlayers) {
            connections.add((Object)NbtUtils.createUUID((UUID)uuid));
        }
        tag.put("LinkedHoses", (Tag)connections);
    }

    @Nonnull
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        this.saveAdditional(tag, registries);
        return tag;
    }
}

