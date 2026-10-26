package com.shnewbs.deepprimal.core;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Hydrology and water system configuration.
 */
public final class HydrologyConfig {
    public static final ModConfigSpec SPEC;

    // River generation
    public static final ModConfigSpec.DoubleValue RIVER_BASE_FLOW;
    public static final ModConfigSpec.DoubleValue RIVER_FLOW_VARIANCE;
    public static final ModConfigSpec.IntValue RIVER_MIN_LENGTH;
    public static final ModConfigSpec.IntValue RIVER_MAX_LENGTH;
    public static final ModConfigSpec.DoubleValue RIVER_MEANDER;
    public static final ModConfigSpec.BooleanValue RIVER_DELTAS;
    public static final ModConfigSpec.BooleanValue RIVER_LAKES;

    // Groundwater
    public static final ModConfigSpec.DoubleValue AQUIFER_RECHARGE_RATE;
    public static final ModConfigSpec.DoubleValue AQUIFER_DISCHARGE_RATE;
    public static final ModConfigSpec.DoubleValue GROUNDWATER_FLOW_SPEED;
    public static final ModConfigSpec.IntValue GROUNDWATER_DEPTH;

    // Precipitation
    public static final ModConfigSpec.DoubleValue BASE_PRECIPITATION;
    public static final ModConfigSpec.DoubleValue OROGRAPHIC_LIFT;
    public static final ModConfigSpec.DoubleValue EVAPOTRANSPIRATION;
    public static final ModConfigSpec.DoubleValue STORM_FREQUENCY;
    public static final ModConfigSpec.DoubleValue STORM_INTENSITY;

    // Water physics
    public static final ModConfigSpec.DoubleValue WATER_VISCOSITY;
    public static final ModConfigSpec.DoubleValue WATER_SURFACE_TENSION;
    public static final ModConfigSpec.DoubleValue SEDIMENT_TRANSPORT;
    public static final ModConfigSpec.DoubleValue EROSION_RATE;
    public static final ModConfigSpec.DoubleValue DEPOSITION_RATE;

    // Flooding
    public static final ModConfigSpec.BooleanValue ENABLE_FLOODING;
    public static final ModConfigSpec.DoubleValue FLOOD_THRESHOLD;
    public static final ModConfigSpec.DoubleValue FLOOD_DURATION;
    public static final ModConfigSpec.DoubleValue FLOOD_SPREAD_RATE;

    // Ice
    public static final ModConfigSpec.BooleanValue ENABLE_ICE;
    public static final ModConfigSpec.DoubleValue ICE_FORMATION_TEMP;
    public static final ModConfigSpec.DoubleValue ICE_MELT_TEMP;
    public static final ModConfigSpec.DoubleValue ICE_THICKNESS_MAX;

    static {
        var builder = new ModConfigSpec.Builder();

        builder.push("hydrology");

        builder.push("rivers");
        RIVER_BASE_FLOW = builder
            .comment("Base river flow rate (m^3/s per catchment unit)")
            .defineInRange("baseFlow", 0.1, 0.01, 10.0);
        RIVER_FLOW_VARIANCE = builder
            .comment("Random variance in river flow")
            .defineInRange("flowVariance", 0.3, 0.0, 1.0);
        RIVER_MIN_LENGTH = builder
            .comment("Minimum river length in chunks")
            .defineInRange("minLength", 10, 1, 100);
        RIVER_MAX_LENGTH = builder
            .comment("Maximum river length in chunks")
            .defineInRange("maxLength", 200, 10, 1000);
        RIVER_MEANDER = builder
            .comment("River meander factor")
            .defineInRange("meander", 0.5, 0.0, 1.0);
        RIVER_DELTAS = builder
            .comment("Enable river delta formation at coast")
            .define("deltas", true);
        RIVER_LAKES = builder
            .comment("Enable lake formation along rivers")
            .define("lakes", true);
        builder.pop();

        builder.push("groundwater");
        AQUIFER_RECHARGE_RATE = builder
            .comment("Aquifer recharge rate from precipitation (m/tick)")
            .defineInRange("rechargeRate", 0.001, 0.0, 0.1);
        AQUIFER_DISCHARGE_RATE = builder
            .comment("Aquifer discharge rate to surface (m/tick)")
            .defineInRange("dischargeRate", 0.0005, 0.0, 0.1);
        GROUNDWATER_FLOW_SPEED = builder
            .comment("Groundwater horizontal flow speed (m/tick)")
            .defineInRange("flowSpeed", 0.01, 0.001, 1.0);
        GROUNDWATER_DEPTH = builder
            .comment("Maximum aquifer depth (blocks)")
            .defineInRange("depth", 50, 10, 200);
        builder.pop();

        builder.push("precipitation");
        BASE_PRECIPITATION = builder
            .comment("Base precipitation rate (m/tick)")
            .defineInRange("baseRate", 0.0001, 0.0, 0.01);
        OROGRAPHIC_LIFT = builder
            .comment("Orographic precipitation multiplier")
            .defineInRange("orographicLift", 2.0, 0.5, 10.0);
        EVAPOTRANSPIRATION = builder
            .comment("Evapotranspiration rate (m/tick)")
            .defineInRange("evapotranspiration", 0.00005, 0.0, 0.001);
        STORM_FREQUENCY = builder
            .comment("Storm event frequency (per day)")
            .defineInRange("stormFrequency", 0.1, 0.0, 5.0);
        STORM_INTENSITY = builder
            .comment("Storm precipitation multiplier")
            .defineInRange("stormIntensity", 5.0, 1.0, 50.0);
        builder.pop();

        builder.push("physics");
        WATER_VISCOSITY = builder
            .comment("Water dynamic viscosity (Pa*s)")
            .defineInRange("viscosity", 0.001, 0.0001, 0.1);
        WATER_SURFACE_TENSION = builder
            .comment("Water surface tension (N/m)")
            .defineInRange("surfaceTension", 0.072, 0.01, 1.0);
        SEDIMENT_TRANSPORT = builder
            .comment("Sediment transport capacity multiplier")
            .defineInRange("sedimentTransport", 1.0, 0.0, 10.0);
        EROSION_RATE = builder
            .comment("Bedrock erosion rate (m/tick per Pa)")
            .defineInRange("erosionRate", 1e-6, 1e-9, 1e-3);
        DEPOSITION_RATE = builder
            .comment("Sediment deposition rate (m/tick)")
            .defineInRange("depositionRate", 0.001, 0.0, 0.1);
        builder.pop();

        builder.push("flooding");
        ENABLE_FLOODING = builder
            .comment("Enable dynamic flooding")
            .define("enabled", true);
        FLOOD_THRESHOLD = builder
            .comment("Water level above ground to trigger flood (blocks)")
            .defineInRange("threshold", 2.0, 0.5, 10.0);
        FLOOD_DURATION = builder
            .comment("Flood duration (ticks)")
            .defineInRange("duration", 24000, 1000, 1000000);
        FLOOD_SPREAD_RATE = builder
            .comment("Flood spread speed (blocks/tick)")
            .defineInRange("spreadRate", 0.1, 0.01, 1.0);
        builder.pop();

        builder.push("ice");
        ENABLE_ICE = builder
            .comment("Enable ice formation on water")
            .define("enabled", true);
        ICE_FORMATION_TEMP = builder
            .comment("Temperature for ice formation (0-1 scale)")
            .defineInRange("formationTemp", 0.2, 0.0, 0.5);
        ICE_MELT_TEMP = builder
            .comment("Temperature for ice melt (0-1 scale)")
            .defineInRange("meltTemp", 0.35, 0.1, 0.6);
        ICE_THICKNESS_MAX = builder
            .comment("Maximum ice thickness (blocks)")
            .defineInRange("maxThickness", 3.0, 0.5, 10.0);
        builder.pop();

        builder.pop(); // hydrology

        SPEC = builder.build();
    }

    private HydrologyConfig() {}
}