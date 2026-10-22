package tfcmt.tfcitembarrelsandmore.common.rotation;

import java.util.EnumSet;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BraceNode
extends Node {
    public BraceNode(BlockPos pos, EnumSet<Direction> connections) {
        super(pos, connections);
    }

    public Rotation rotation(Rotation sourceRotation, Direction sourceDirection, Direction exitDirection) {
        return Rotation.of((Rotation)sourceRotation, (Direction)exitDirection);
    }
}

