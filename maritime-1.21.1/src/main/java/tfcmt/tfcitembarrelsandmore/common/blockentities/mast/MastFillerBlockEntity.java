package tfcmt.tfcitembarrelsandmore.common.blockentities.mast;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomHost;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastCell;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastRakeable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLink;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkRegistry;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigLinkable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.RigSlot;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.YardHost;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.BoomGrowth;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.MastAnchorBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.MastFillerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.RigLinking;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.YardGrowth;

public class MastFillerBlockEntity
extends BlockEntity
implements MastCell,
MastRakeable,
YardHost,
BoomHost,
RigLinkable {
    private int offsetBelowAnchor = 1;
    private boolean hasYard = false;
    private Direction.Axis yardAxis = Direction.Axis.X;
    private Direction yardFaceBias = Direction.NORTH;
    private int yardArmLength = 0;
    private TIBAMBlocks.Wood yardWood = TIBAMBlocks.Wood.OAK;
    private int yardYawSteps = 0;
    private boolean hasBoom = false;
    private Direction boomFace = Direction.NORTH;
    private int boomLength = 0;
    private TIBAMBlocks.Wood boomWood = TIBAMBlocks.Wood.OAK;
    private int boomYawSteps = 0;
    private UUID rigUUID = UUID.randomUUID();
    private final Map<RigSlot, Set<RigLink>> rigLinks = new EnumMap<RigSlot, Set<RigLink>>(RigSlot.class);

    public MastFillerBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.MAST_FILLER.get(), pos, state);
    }

    public void init(int offsetBelowAnchor) {
        this.offsetBelowAnchor = offsetBelowAnchor;
        this.setChanged();
    }

    public int getOffsetBelowAnchor() {
        return this.offsetBelowAnchor;
    }

    public BlockPos anchorPos() {
        return this.worldPosition.below(this.offsetBelowAnchor);
    }

    private MastBlockEntity anchorBE() {
        MastBlockEntity mastBE;
        BlockEntity blockEntity;
        return this.level != null && (blockEntity = this.level.getBlockEntity(this.anchorPos())) instanceof MastBlockEntity ? (mastBE = (MastBlockEntity)blockEntity) : null;
    }

    @Override
    public int getWidthPx() {
        MastBlockEntity anchor = this.anchorBE();
        return anchor != null ? MastBlockEntity.widthForOffset(this.offsetBelowAnchor, anchor.getTotalHeight()) : 4;
    }

    @Override
    public TIBAMBlocks.Wood getWood() {
        MastBlockEntity anchor = this.anchorBE();
        return anchor != null ? anchor.getWood() : TIBAMBlocks.Wood.OAK;
    }

    @Override
    public Direction getRakeFace() {
        MastBlockEntity anchor = this.anchorBE();
        return anchor != null ? anchor.getRakeFace() : Direction.NORTH;
    }

    @Override
    public int getRakeSteps() {
        MastBlockEntity anchor = this.anchorBE();
        return anchor != null ? anchor.getRakeSteps() : 0;
    }

    @Override
    public int getOffsetAboveAnchor() {
        return this.offsetBelowAnchor;
    }

    @Override
    public boolean hasYard() {
        return this.hasYard;
    }

    @Override
    public Direction.Axis getYardAxis() {
        return this.yardAxis;
    }

    @Override
    public Direction getYardFaceBias() {
        return this.yardFaceBias;
    }

    @Override
    public int getYardArmLength() {
        return this.yardArmLength;
    }

    @Override
    public int getYawSteps() {
        return this.yardYawSteps;
    }

    @Override
    public void setYawSteps(int yawSteps) {
        this.yardYawSteps = yawSteps;
        this.setChanged();
        this.syncToClient();
    }

    @Override
    public TIBAMBlocks.Wood getYardWood() {
        return this.yardWood;
    }

    @Override
    public void initYard(Direction.Axis axis, Direction faceBias, int armLength, TIBAMBlocks.Wood wood) {
        this.hasYard = true;
        this.yardAxis = axis;
        this.yardFaceBias = faceBias;
        this.yardArmLength = armLength;
        this.yardWood = wood;
        this.setChanged();
        this.syncToClient();
    }

    @Override
    public void setYardArmLength(int armLength) {
        this.yardArmLength = armLength;
        this.setChanged();
        this.syncToClient();
    }

    @Override
    public void clearYard() {
        this.hasYard = false;
        this.yardArmLength = 0;
        this.yardYawSteps = 0;
        RigLinking.clearLinks(this, RigSlot.YARD_LEFT);
        RigLinking.clearLinks(this, RigSlot.YARD_RIGHT);
        this.setChanged();
        this.syncToClient();
    }

    @Override
    public boolean hasBoom() {
        return this.hasBoom;
    }

    @Override
    public Direction getBoomFace() {
        return this.boomFace;
    }

    @Override
    public int getBoomLength() {
        return this.boomLength;
    }

    @Override
    public int getBoomYawSteps() {
        return this.boomYawSteps;
    }

    @Override
    public void setBoomYawSteps(int yawSteps) {
        this.boomYawSteps = yawSteps;
        this.setChanged();
        this.syncToClient();
    }

    @Override
    public TIBAMBlocks.Wood getBoomWood() {
        return this.boomWood;
    }

    @Override
    public void initBoom(Direction face, int length, TIBAMBlocks.Wood wood) {
        this.hasBoom = true;
        this.boomFace = face;
        this.boomLength = length;
        this.boomWood = wood;
        this.setChanged();
        this.syncToClient();
    }

    @Override
    public void setBoomLength(int length) {
        this.boomLength = length;
        this.setChanged();
        this.syncToClient();
    }

    @Override
    public void clearBoom() {
        this.hasBoom = false;
        this.boomLength = 0;
        this.boomYawSteps = 0;
        RigLinking.clearLinks(this, RigSlot.BOOM_END);
        RigLinking.clearLinks(this, RigSlot.BOOM_END_PORT);
        this.setChanged();
        this.syncToClient();
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
        if (this.allowsMultipleLinks(slot)) {
            this.rigLinks.computeIfAbsent(slot, s -> new LinkedHashSet()).add(link);
        } else {
            this.rigLinks.put(slot, new LinkedHashSet<RigLink>(Set.of(link)));
        }
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
        return false;
    }

    private void syncToClient() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void setRemoved() {
        if (this.level != null && !this.level.isClientSide && this.level.hasChunk(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4) && !(this.level.getBlockState(this.worldPosition).getBlock() instanceof MastFillerBlock)) {
            YardGrowth.breakYardIfAny(this.level, this.worldPosition, this);
            BoomGrowth.breakBoomIfAny(this.level, this.worldPosition, this);
            BlockPos anchorPos = this.anchorPos();
            BlockEntity blockEntity = this.level.getBlockEntity(anchorPos);
            if (blockEntity instanceof MastBlockEntity) {
                MastBlockEntity mastBE = (MastBlockEntity)blockEntity;
                if (this.level.getBlockState(anchorPos).getBlock() instanceof MastAnchorBlock) {
                    int oldTop = mastBE.getTotalHeight() - 1;
                    for (int offset = this.offsetBelowAnchor + 1; offset <= oldTop; ++offset) {
                        this.level.removeBlock(anchorPos.above(offset), false);
                    }
                    mastBE.shrinkTo(this.offsetBelowAnchor);
                }
            }
        }
        super.setRemoved();
    }

    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("offset", this.offsetBelowAnchor);
        tag.putBoolean("hasYard", this.hasYard);
        tag.putString("yardAxis", this.yardAxis.getName());
        tag.putString("yardFaceBias", this.yardFaceBias.getName());
        tag.putInt("yardArmLength", this.yardArmLength);
        tag.putString("yardWood", this.yardWood.name());
        tag.putInt("yardYawSteps", this.yardYawSteps);
        tag.putBoolean("hasBoom", this.hasBoom);
        tag.putString("boomFace", this.boomFace.getName());
        tag.putInt("boomLength", this.boomLength);
        tag.putString("boomWood", this.boomWood.name());
        tag.putInt("boomYawSteps", this.boomYawSteps);
        RigLinkable.saveRigData(tag, this.rigUUID, this.rigLinks);
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.offsetBelowAnchor = tag.getInt("offset");
        if (this.offsetBelowAnchor <= 0) {
            this.offsetBelowAnchor = 1;
        }
        this.hasYard = tag.getBoolean("hasYard");
        Direction.Axis loadedAxis = Direction.Axis.byName((String)tag.getString("yardAxis"));
        this.yardAxis = loadedAxis != null ? loadedAxis : Direction.Axis.X;
        Direction loadedBias = Direction.byName((String)tag.getString("yardFaceBias"));
        this.yardFaceBias = loadedBias != null ? loadedBias : Direction.NORTH;
        this.yardArmLength = tag.getInt("yardArmLength");
        try {
            this.yardWood = TIBAMBlocks.Wood.valueOf(tag.getString("yardWood"));
        }
        catch (IllegalArgumentException e) {
            this.yardWood = TIBAMBlocks.Wood.OAK;
        }
        this.yardYawSteps = tag.getInt("yardYawSteps");
        this.hasBoom = tag.getBoolean("hasBoom");
        Direction loadedFace = Direction.byName((String)tag.getString("boomFace"));
        this.boomFace = loadedFace != null ? loadedFace : Direction.NORTH;
        this.boomLength = tag.getInt("boomLength");
        try {
            this.boomWood = TIBAMBlocks.Wood.valueOf(tag.getString("boomWood"));
        }
        catch (IllegalArgumentException e) {
            this.boomWood = TIBAMBlocks.Wood.OAK;
        }
        this.boomYawSteps = tag.getInt("boomYawSteps");
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
        this.handleUpdateTag(pkt.getTag(), this.level.registryAccess());
    }
}

