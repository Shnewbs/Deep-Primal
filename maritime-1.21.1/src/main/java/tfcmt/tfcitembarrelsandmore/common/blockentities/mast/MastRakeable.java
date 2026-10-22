package tfcmt.tfcitembarrelsandmore.common.blockentities.mast;

import net.minecraft.core.Direction;

public interface MastRakeable {
    public Direction getRakeFace();

    public int getRakeSteps();

    public int getOffsetAboveAnchor();
}

