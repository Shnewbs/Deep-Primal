package tfcmt.tfcitembarrelsandmore.compat.create;

import net.minecraft.util.StringRepresentable;

public enum SpeedTelegraphOrder implements StringRepresentable
{
    FULL_ASTERN("full_astern", 1.0f, false),
    TWO_THIRDS_ASTERN("two_thirds_astern", 0.667f, false),
    HALF_ASTERN("half_astern", 0.5f, false),
    ONE_THIRD_ASTERN("one_third_astern", 0.333f, false),
    SLOW_ASTERN("slow_astern", 0.167f, false),
    STOP("stop", 0.0f, true),
    SLOW_AHEAD("slow_ahead", 0.167f, true),
    ONE_THIRD_AHEAD("one_third_ahead", 0.333f, true),
    HALF_AHEAD("half_ahead", 0.5f, true),
    TWO_THIRDS_AHEAD("two_thirds_ahead", 0.667f, true),
    FULL_AHEAD("full_ahead", 1.0f, true);

    private final String serialName;
    private final float rpmFraction;
    private final boolean forward;

    private SpeedTelegraphOrder(String serialName, float rpmFraction, boolean forward) {
        this.serialName = serialName;
        this.rpmFraction = rpmFraction;
        this.forward = forward;
    }

    public float getRpmFraction() {
        return this.rpmFraction;
    }

    public boolean isForward() {
        return this.forward;
    }

    public float toCreateRpm(float maxRpm) {
        if (this == STOP) {
            return 0.0f;
        }
        float abs = maxRpm * this.rpmFraction;
        return this.forward ? abs : -abs;
    }

    public SpeedTelegraphOrder stepAhead() {
        int next = Math.min(this.ordinal() + 1, SpeedTelegraphOrder.values().length - 1);
        return SpeedTelegraphOrder.values()[next];
    }

    public SpeedTelegraphOrder stepAstern() {
        int prev = Math.max(this.ordinal() - 1, 0);
        return SpeedTelegraphOrder.values()[prev];
    }

    public static SpeedTelegraphOrder fromOrdinal(int ordinal) {
        SpeedTelegraphOrder[] values = SpeedTelegraphOrder.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return STOP;
        }
        return values[ordinal];
    }

    public String getSerializedName() {
        return this.serialName;
    }
}

