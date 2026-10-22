package tfcmt.tfcitembarrelsandmore.common.blockentities.rotation;

import net.dries007.tfc.common.blockentities.rotation.RotatingBlockEntity;
import net.dries007.tfc.util.rotation.NetworkAction;
import net.dries007.tfc.util.rotation.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.rotation.BraceNode;

public class CastIronAxleBraceBlockEntity
extends BlockEntity
implements RotatingBlockEntity {
    private Node node;

    public CastIronAxleBraceBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.CAST_IRON_AXLE_BRACE.get(), pos, state);
        Direction.Axis axis = (Direction.Axis)state.getValue((Property)BlockStateProperties.AXIS);
        this.node = new BraceNode(pos, Node.ofAxis((Direction.Axis)axis));
    }

    public Node getRotationNode() {
        return this.node;
    }

    public void performNetworkAction(NetworkAction action) {
        RotatingBlockEntity.super.performNetworkAction(action);
    }

    public boolean isInvalidInNetwork() {
        return false;
    }

    public void markAsInvalidInNetwork() {
    }

    public void onNeighborChanged() {
        if (this.level != null) {
            this.performNetworkAction(NetworkAction.UPDATE);
        }
    }

    public void onLoad() {
        super.onLoad();
        if (this.level != null) {
            this.performNetworkAction(NetworkAction.ADD);
        }
    }

    public void setRemoved() {
        if (this.level != null) {
            this.performNetworkAction(NetworkAction.REMOVE);
        }
        super.setRemoved();
    }

    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }
}

