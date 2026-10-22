package tfcmt.tfcitembarrelsandmore.common.container;

import net.minecraft.core.BlockPos;

public interface ICompressorMenu {
    public int getCompressedAir();

    public int getMaxAir();

    public int getPsi();

    public int getBleederRate();

    public float getCurrentRpm();

    public BlockPos getEnginePos();

    public void setOptimisticBleederRate(int var1);
}

