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
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.BoomHost;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastBlockEntity;
import tfcmt.tfcitembarrelsandmore.common.blockentities.mast.MastCell;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.BoomFillerBlock;
import tfcmt.tfcitembarrelsandmore.common.blocks.mast.BoomGrowth;

public class BoomFillerBlockEntity
extends BlockEntity
implements MastCell {
    private Direction face = Direction.NORTH;
    private int distanceFromHost = 1;

    public BoomFillerBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.BOOM_FILLER.get(), pos, state);
    }

    public void init(Direction face, int distanceFromHost) {
        this.face = face;
        this.distanceFromHost = distanceFromHost;
        this.setChanged();
    }

    public Direction getFace() {
        return this.face;
    }

    public int getDistanceFromHost() {
        return this.distanceFromHost;
    }

    public int getYawSteps() {
        BoomHost host = this.host();
        return host != null ? host.getBoomYawSteps() : 0;
    }

    public BlockPos hostPos() {
        return this.worldPosition.relative(this.face.getOpposite(), this.distanceFromHost);
    }

    @Nullable
    private BoomHost host() {
        BoomHost h;
        BlockEntity blockEntity;
        return this.level != null && (blockEntity = this.level.getBlockEntity(this.hostPos())) instanceof BoomHost ? (h = (BoomHost)blockEntity) : null;
    }

    @Override
    public int getWidthPx() {
        BoomHost host = this.host();
        int length = host != null ? host.getBoomLength() : this.distanceFromHost;
        return MastBlockEntity.widthForOffset(this.distanceFromHost, length + 1);
    }

    @Override
    public TIBAMBlocks.Wood getWood() {
        BoomHost host = this.host();
        return host != null ? host.getBoomWood() : TIBAMBlocks.Wood.OAK;
    }

    public void setRemoved() {
        BoomHost host;
        BlockEntity blockEntity;
        BlockPos hostPos;
        if (this.level != null && !this.level.isClientSide && this.level.hasChunk(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4) && !(this.level.getBlockState(this.worldPosition).getBlock() instanceof BoomFillerBlock) && this.level.hasChunk((hostPos = this.hostPos()).getX() >> 4, hostPos.getZ() >> 4) && (blockEntity = this.level.getBlockEntity(hostPos)) instanceof BoomHost && (host = (BoomHost)blockEntity).hasBoom()) {
            BoomGrowth.breakBoomIfAny(this.level, hostPos, host);
            host.clearBoom();
        }
        super.setRemoved();
    }

    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("face", this.face.getName());
        tag.putInt("distance", this.distanceFromHost);
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        Direction loadedFace = Direction.byName((String)tag.getString("face"));
        this.face = loadedFace != null ? loadedFace : Direction.NORTH;
        this.distanceFromHost = tag.getInt("distance");
        if (this.distanceFromHost <= 0) {
            this.distanceFromHost = 1;
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

