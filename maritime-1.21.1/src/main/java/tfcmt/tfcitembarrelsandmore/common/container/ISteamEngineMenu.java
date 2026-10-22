package tfcmt.tfcitembarrelsandmore.common.container;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public interface ISteamEngineMenu {
    public int getWaterAmount();

    public int getWaterCapacity();

    public int getSteam();

    public int getMaxSteam();

    public int getPsi();

    public boolean isRunning();

    public int getBleederTier();

    public int getThrottle();

    public FluidTank getWaterTank();

    public BlockPos getEnginePos();

    public void setOptimisticThrottle(int var1);

    public void setOptimisticBleederTier(int var1);
}

