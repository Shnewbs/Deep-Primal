package tfcmt.tfcitembarrelsandmore.common.blockentities.mast;

import net.minecraft.core.Direction;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;

public interface BoomHost {
    public boolean hasBoom();

    public Direction getBoomFace();

    public int getBoomLength();

    public int getBoomYawSteps();

    public void setBoomYawSteps(int var1);

    public TIBAMBlocks.Wood getBoomWood();

    public void initBoom(Direction var1, int var2, TIBAMBlocks.Wood var3);

    public void setBoomLength(int var1);

    public void clearBoom();
}

