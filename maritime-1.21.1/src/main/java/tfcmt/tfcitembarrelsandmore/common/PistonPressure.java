package tfcmt.tfcitembarrelsandmore.common;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum PistonPressure {
    COLD(0, 9, 0x888888, "cold"),
    LOW(10, 29, 0x88AAFF, "low"),
    WORKING(30, 59, 0xFFFFFF, "working"),
    HIGH(60, 84, 16766720, "high"),
    DANGER(85, 100, 0xFF3333, "danger");

    private static final PistonPressure[] VALUES;
    private final int min;
    private final int max;
    private final int color;
    private final String translationKey;

    private PistonPressure(int min, int max, int color, String translationKey) {
        this.min = min;
        this.max = max;
        this.color = color;
        this.translationKey = "tibam.psi." + translationKey;
    }

    @Nullable
    public static PistonPressure getTier(int psi) {
        for (PistonPressure tier : VALUES) {
            if (psi < tier.min || psi > tier.max) continue;
            return tier;
        }
        return psi < 0 ? COLD : DANGER;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public int getColor() {
        return this.color;
    }

    public MutableComponent getDisplayName() {
        return Component.translatable((String)this.translationKey);
    }

    public boolean hasOutput() {
        return this != COLD;
    }

    public float getRodPopChance() {
        return this == DANGER ? 0.005f : 0.0f;
    }

    static {
        VALUES = PistonPressure.values();
    }
}

