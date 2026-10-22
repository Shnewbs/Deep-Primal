package tfcmt.tfcitembarrelsandmore.common.blockentities.mast;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLink;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkRegistry;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigSlot;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.RigLinking;

public class CleatBlockEntity
extends BlockEntity
implements RigLinkable {
    private UUID rigUUID = UUID.randomUUID();
    private final Map<RigSlot, Set<RigLink>> rigLinks = new EnumMap<RigSlot, Set<RigLink>>(RigSlot.class);

    public CleatBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)TIBAMBlockEntities.CLEAT.get(), pos, state);
    }

    @Override
    public UUID getRigUUID() {
        return this.rigUUID;
    }

    @Override
    public Set<RigLink> getLinks(RigSlot slot) {
        return this.rigLinks.getOrDefault((Object)slot, Set.of());
    }

    @Override
    public void addLink(RigSlot slot, RigLink link) {
        this.rigLinks.computeIfAbsent(slot, s -> new LinkedHashSet()).add(link);
        this.setChanged();
        this.syncToClient();
    }

    @Override
    public void removeLink(RigSlot slot, RigLink link) {
        Set<RigLink> set = this.rigLinks.get((Object)slot);
        if (set != null && set.remove(link) && set.isEmpty()) {
            this.rigLinks.remove((Object)slot);
        }
        this.setChanged();
        this.syncToClient();
    }

    @Override
    public boolean allowsMultipleLinks(RigSlot slot) {
        return slot == RigSlot.CLEAT_LINE;
    }

    public void setRemoved() {
        if (this.level != null && !this.level.isClientSide && this.level.hasChunk(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4)) {
            RigLinking.clearLinks(this, RigSlot.CLEAT_LINE);
        }
        super.setRemoved();
    }

    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        RigLinkable.saveRigData(tag, this.rigUUID, this.rigLinks);
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.rigUUID = RigLinkable.loadRigUUID(tag);
        this.rigLinks.clear();
        this.rigLinks.putAll(RigLinkable.loadRigLinks(tag));
    }

    public void onLoad() {
        super.onLoad();
        if (this.level == null) {
            return;
        }
        if (!this.level.isClientSide) {
            RigLinkRegistry.register(this.rigUUID, this);
        } else {
            RigLinkRegistry.registerClient(this.rigUUID, this);
        }
    }

    private void syncToClient() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public void handleUpdateTag(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        this.loadAdditional(tag, registries);
        if (this.level != null && this.level.isClientSide) {
            RigLinkRegistry.registerClient(this.rigUUID, this);
        }
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.handleUpdateTag(pkt.getTag());
    }
}

