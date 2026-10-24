package tfcmt.tfcitembarrelsandmore.common;

import net.minecraft.util.RandomSource;
import tfcmt.tfcitembarrelsandmore.common.PistonPressure;

public final class SteamEquilibriumModel {
    private SteamEquilibriumModel() {
    }

    public static ProductionResult tickProduction(int steamHeartbeatCounter, float effectiveBoilerTemp, float steamProductionStepPerTick, float steamProductionAccumulator, int steam, int maxSteam, float boilerTemp, float minSteamTemp, float equilibriumCeiling, float equilibriumExponent, float tempApproachRate, float steamApproachRate, boolean hasWater) {
        int steamWhole;
        if (++steamHeartbeatCounter >= 20) {
            steamHeartbeatCounter = 0;
            effectiveBoilerTemp += (boilerTemp - effectiveBoilerTemp) * tempApproachRate;
            float equilibriumPsi = 0.0f;
            if (hasWater) {
                float tempNorm = (effectiveBoilerTemp - minSteamTemp) / (equilibriumCeiling - 100.0f);
                tempNorm = Math.max(0.0f, Math.min(1.0f, tempNorm));
                equilibriumPsi = 100.0f * (float)Math.pow(tempNorm, equilibriumExponent);
            }
            float equilibriumSteam = equilibriumPsi / 100.0f * (float)maxSteam;
            float stepAmount = (equilibriumSteam - (float)steam) * steamApproachRate;
            steamProductionStepPerTick = stepAmount / 20.0f;
        }
        if ((steamWhole = (int)(steamProductionAccumulator += steamProductionStepPerTick)) != 0) {
            steamProductionAccumulator -= (float)steamWhole;
            steam = Math.max(0, Math.min(maxSteam, steam + steamWhole));
        }
        return new ProductionResult(steamHeartbeatCounter, effectiveBoilerTemp, steamProductionStepPerTick, steamProductionAccumulator, steam);
    }

    public static BleederResult tickBleeder(int bleederTier, int prePsi, int steam, int maxSteam, float bleederOpenMargin, float minDropPsi, float maxDropPsi, RandomSource random) {
        int triggerBase;
        if (bleederTier <= 0) {
            return new BleederResult(steam, -1.0f);
        }
        PistonPressure[] tiers = PistonPressure.values();
        PistonPressure selected = tiers[bleederTier];
        boolean isTopTier = bleederTier >= tiers.length - 1;
        int n = triggerBase = isTopTier ? selected.getMin() : selected.getMax();
        if (bleederOpenMargin < 0.0f) {
            bleederOpenMargin = 1.0f + random.nextFloat();
        }
        if ((float)prePsi >= (float)triggerBase + bleederOpenMargin) {
            float dropPsi = minDropPsi + random.nextFloat() * (maxDropPsi - minDropPsi);
            int dropMb = Math.round(dropPsi / 100.0f * (float)maxSteam);
            steam = Math.max(0, steam - dropMb);
            bleederOpenMargin = -1.0f;
        }
        return new BleederResult(steam, bleederOpenMargin);
    }

    public record ProductionResult(int steamHeartbeatCounter, float effectiveBoilerTemp, float steamProductionStepPerTick, float steamProductionAccumulator, int steam) {
    }

    public record BleederResult(int steam, float bleederOpenMargin) {
    }
}

