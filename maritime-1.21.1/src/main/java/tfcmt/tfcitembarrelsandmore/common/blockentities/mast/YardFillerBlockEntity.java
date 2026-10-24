package tfcmt.tfcitembarrelsandmore.common.blockentities.mast;

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
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastCell;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastRakeable;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.YardHost;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.YardFillerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.YardGrowth;

public class YardFillerBlockEntity
extends BlockEntity
implements MastCell {
    private Direction.Axis axis = Direction.Axis.X;
    private int signedOffset = 1;

    public YardFillerBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.YARD_FILLER.get(), pos, state);
    }

    public void init(Direction.Axis axis, int signedOffset) {
        this.axis = axis;
        this.signedOffset = signedOffset;
        this.setChanged();
    }

    public Direction.Axis getAxis() {
        return this.axis;
    }

    public int getDistanceFromHost() {
        return Math.abs(this.signedOffset);
    }

    public int getSignedOffset() {
        return this.signedOffset;
    }

    public int getYawSteps() {
        YardHost host = this.host();
        return host != null ? host.getYawSteps() : 0;
    }

    public Direction getHostRakeFace() {
        Direction direction;
        YardHost yardHost = this.host();
        if (yardHost instanceof MastRakeable) {
            MastRakeable hostCell = (MastRakeable)((Object)yardHost);
            direction = hostCell.getRakeFace();
        } else {
            direction = Direction.NORTH;
        }
        return direction;
    }

    public int getHostRakeSteps() {
        int n;
        YardHost yardHost = this.host();
        if (yardHost instanceof MastRakeable) {
            MastRakeable hostCell = (MastRakeable)((Object)yardHost);
            n = hostCell.getRakeSteps();
        } else {
            n = 0;
        }
        return n;
    }

    public int getHostOffsetAboveAnchor() {
        int n;
        YardHost yardHost = this.host();
        if (yardHost instanceof MastRakeable) {
            MastRakeable hostCell = (MastRakeable)((Object)yardHost);
            n = hostCell.getOffsetAboveAnchor();
        } else {
            n = 0;
        }
        return n;
    }

    public BlockPos hostPos() {
        Direction posDir = YardGrowth.positiveDirectionFor(this.axis);
        return this.worldPosition.relative(posDir, -this.signedOffset);
    }

    @Nullable
    private YardHost host() {
        YardHost h;
        BlockEntity blockEntity;
        return this.level != null && (blockEntity = this.level.getBlockEntity(this.hostPos())) instanceof YardHost ? (h = (YardHost)blockEntity) : null;
    }

    @Override
    public int getWidthPx() {
        YardHost host = this.host();
        int armLength = host != null ? host.getYardArmLength() : this.getDistanceFromHost();
        return MastBlockEntity.widthForOffset(this.getDistanceFromHost(), armLength + 1);
    }

    @Override
    public TIBAMBlocks.Wood getWood() {
        YardHost host = this.host();
        return host != null ? host.getYardWood() : TIBAMBlocks.Wood.OAK;
    }

    public Direction getFaceBias() {
        YardHost host = this.host();
        return host != null ? host.getYardFaceBias() : Direction.NORTH;
    }

    public double getPerpendicularOffsetBlocks() {
        YardHost host = this.host();
        if (!(host instanceof MastCell)) {
            return 0.0;
        }
        MastCell hostCell = (MastCell)((Object)host);
        return YardGrowth.faceOffsetBlocks(host, hostCell.getWidthPx());
    }

    public void setRemoved() {
        YardHost host;
        BlockEntity blockEntity;
        BlockPos hostPos;
        if (this.level != null && !this.level.isClientSide && this.level.hasChunk(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4) && !(this.level.getBlockState(this.worldPosition).getBlock() instanceof YardFillerBlock) && this.level.hasChunk((hostPos = this.hostPos()).getX() >> 4, hostPos.getZ() >> 4) && (blockEntity = this.level.getBlockEntity(hostPos)) instanceof YardHost && (host = (YardHost)blockEntity).hasYard()) {
            YardGrowth.breakYardIfAny(this.level, hostPos, host);
            host.clearYard();
        }
        super.setRemoved();
    }

    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("axis", this.axis.getName());
        tag.putInt("offset", this.signedOffset);
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        Direction.Axis loadedAxis = Direction.Axis.byName((String)tag.getString("axis"));
        this.axis = loadedAxis != null ? loadedAxis : Direction.Axis.X;
        this.signedOffset = tag.getInt("offset");
        if (this.signedOffset == 0) {
            this.signedOffset = 1;
        }
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public void handleUpdateTag(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        this.loadAdditional(tag, registries);
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.handleUpdateTag(pkt.getTag(), this.level.registryAccess());
    }
}

