package tfcmt.tfcitembarrelsandmore.common.blockentities.rotation;

import net.dries007.tfc.common.blockentities.rotation.RotatingBlockEntity;
import net.dries007.tfc.common.blocks.rotation.AxleBlock;
import net.dries007.tfc.util.rotation.AxleNode;
import net.dries007.tfc.util.rotation.NetworkAction;
import net.dries007.tfc.util.rotation.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;

public class CastIronAxleBlockEntity
extends BlockEntity
implements RotatingBlockEntity {
    private Node node;
    private boolean invalid = false;

    public CastIronAxleBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.CAST_IRON_AXLE.get(), pos, state);
        Direction.Axis axis = (Direction.Axis)state.getValue((Property)AxleBlock.AXIS);
        this.node = new AxleNode(pos, Node.ofAxis((Direction.Axis)axis));
    }

    public Node getRotationNode() {
        return this.node;
    }

    public void performNetworkAction(NetworkAction action) {
        RotatingBlockEntity.super.performNetworkAction(action);
    }

    public boolean isInvalidInNetwork() {
        return this.invalid;
    }

    public void markAsInvalidInNetwork() {
        this.invalid = true;
    }

    public void onLoad() {
        super.onLoad();
        this.performNetworkAction(NetworkAction.ADD);
    }

    public void setRemoved() {
        this.performNetworkAction(NetworkAction.REMOVE);
        super.setRemoved();
    }

    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("invalid", this.invalid);
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.invalid = tag.getBoolean("invalid");
    }
}

