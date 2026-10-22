package tfcmt.tfcitembarrelsandmore.common.blockentities;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlockEntities;
import tfcmt.tfcitembarrelsandmore.common.blocks.devices.FumeDuctBlock;

public class FumeDuctBlockEntity
extends BlockEntity {
    private final Map<Direction, BlockEntity> neighbors = new EnumMap<Direction, BlockEntity>(Direction.class);

    public FumeDuctBlockEntity(BlockPos pos, BlockState state) {
        super(TIBAMBlockEntities.FUME_DUCT.get(), pos, state);
    }

    public void onLoad() {
        super.onLoad();
        if (this.level == null) {
            return;
        }
        BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof FumeDuctBlock)) {
            return;
        }
        for (Direction dir : Direction.values()) {
            if (!((Boolean)state.getValue((Property)FumeDuctBlock.PROPERTY_BY_DIRECTION.get(dir))).booleanValue()) continue;
            this.relink(dir);
            BlockEntity blockEntity = this.neighbors.get(dir);
            if (!(blockEntity instanceof FumeDuctBlockEntity)) continue;
            FumeDuctBlockEntity neighborDuct = (FumeDuctBlockEntity)blockEntity;
            neighborDuct.relink(dir.getOpposite());
        }
    }

    public void relink(Direction direction) {
        if (this.level == null) {
            return;
        }
        BlockEntity neighbor = this.level.getBlockEntity(this.worldPosition.relative(direction));
        if (neighbor instanceof FumeDuctBlockEntity) {
            this.neighbors.put(direction, neighbor);
        } else {
            this.neighbors.remove(direction);
        }
    }

    public void unlink(Direction direction) {
        this.neighbors.remove(direction);
    }

    public Collection<BlockEntity> liveNeighbors() {
        this.neighbors.values().removeIf(BlockEntity::isRemoved);
        return this.neighbors.values();
    }
}

