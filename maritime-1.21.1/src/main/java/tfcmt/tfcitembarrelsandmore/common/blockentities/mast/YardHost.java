package tfcmt.tfcitembarrelsandmore.common.blockentities.mast;

import net.minecraft.core.Direction;
import tfcmt.tfcitembarrelsandmore.common.TIBAMBlocks;

public interface YardHost {
    public boolean hasYard();

    public Direction.Axis getYardAxis();

    public Direction getYardFaceBias();

    public int getYardArmLength();

    public int getYawSteps();

    public void setYawSteps(int var1);

    public TIBAMBlocks.Wood getYardWood();

    public void initYard(Direction.Axis var1, Direction var2, int var3, TIBAMBlocks.Wood var4);

    public void setYardArmLength(int var1);

    public void clearYard();
}

