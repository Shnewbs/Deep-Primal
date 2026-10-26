package com.shnewbs.deepprimal.core;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Geological stability and hazards configuration.
 * Controls landslides, earthquakes, subsidence, and volcanic activity.
 */
public final class StabilityRuntime {
    public static final ModConfigSpec SPEC;

    // Landslides
    public static final ModConfigSpec.BooleanValue ENABLE_LANDSLIDES;
    public static final ModConfigSpec.DoubleValue LANDSLIDE_SLOPE_THRESHOLD;
    public static final ModConfigSpec.DoubleValue LANDSLIDE_PROBABILITY;
    public static final ModConfigSpec.DoubleValue LANDSLIDE_VOLUME;
    public static final ModConfigSpec.DoubleValue LANDSLIDE_RUNOUT;
    public static final ModConfigSpec.BooleanValue LANDSLIDE_WATER_TRIGGER;

    // Earthquakes
    public static final ModConfigSpec.BooleanValue ENABLE_EARTHQUAKES;
    public static final ModConfigSpec.DoubleValue EARTHQUAKE_FREQUENCY;
    public static final ModConfigSpec.DoubleValue EARTHQUAKE_MAGNITUDE_MIN;
    public static final ModConfigSpec.DoubleValue EARTHQUAKE_MAGNITUDE_MAX;
    public static final ModConfigSpec.DoubleValue EARTHQUAKE_DEPTH;
    public static final ModConfigSpec.DoubleValue EARTHQUAKE_DAMAGE_RADIUS;

    // Subsidence
    public static final ModConfigSpec.BooleanValue ENABLE_SUBSIDENCE;
    public static final ModConfigSpec.DoubleValue SUBSIDENCE_MINING_TRIGGER;
    public static final ModConfigSpec.DoubleValue SUBSIDENCE_GROUNDWATER_TRIGGER;
    public static final ModConfigSpec.DoubleValue SUBSIDENCE_RATE;
    public static final ModConfigSpec.IntValue SUBSIDENCE_MAX_DEPTH;

    // Volcanism
    public static final ModConfigSpec.BooleanValue ENABLE_VOLCANISM;
    public static final ModConfigSpec.DoubleValue VOLCANO_SPAWN_CHANCE;
    public static final ModConfigSpec.IntValue VOLCANO_MIN_HEIGHT;
    public static final ModConfigSpec.IntValue VOLCANO_MAX_HEIGHT;
    public static final ModConfigSpec.DoubleValue ERUPTION_FREQUENCY;
    public static final ModConfigSpec.DoubleValue LAVA_FLOW_SPEED;
    public static final ModConfigSpec.DoubleValue ASH_FALL_RADIUS;

    // General
    public static final ModConfigSpec.BooleanValue HAZARDS_AFFECT_PLAYERS;
    public static final ModConfigSpec.BooleanValue HAZARDS_AFFECT_STRUCTURES;
    public static final ModConfigSpec.IntValue HAZARD_COOLDOWN_TICKS;

    static {
        var builder = new ModConfigSpec.Builder();

        builder.push("stability");

        builder.push("landslides");
        ENABLE_LANDSLIDES = builder
            .comment("Enable landslide generation on steep slopes")
            .define("enabled", true);
        LANDSLIDE_SLOPE_THRESHOLD = builder
            .comment("Minimum slope angle for landslides (degrees)")
            .defineInRange("slopeThreshold", 35.0, 15.0, 60.0);
        LANDSLIDE_PROBABILITY = builder
            .comment("Per-chunk probability of landslide per tick (when conditions met)")
            .defineInRange("probability", 1e-6, 1e-9, 1e-3);
        LANDSLIDE_VOLUME = builder
            .comment("Typical landslide volume (cubic blocks)")
            .defineInRange("volume", 1000.0, 100.0, 100000.0);
        LANDSLIDE_RUNOUT = builder
            .comment("Landslide runout distance multiplier")
            .defineInRange("runout", 2.0, 0.5, 10.0);
        LANDSLIDE_WATER_TRIGGER = builder
            .comment("Water saturation can trigger landslides")
            .define("waterTrigger", true);
        builder.pop();

        builder.push("earthquakes");
        ENABLE_EARTHQUAKES = builder
            .comment("Enable earthquake events")
            .define("enabled", true);
        EARTHQUAKE_FREQUENCY = builder
            .comment("Earthquakes per day per region")
            .defineInRange("frequency", 0.01, 0.0, 1.0);
        EARTHQUAKE_MAGNITUDE_MIN = builder
            .comment("Minimum earthquake magnitude (Richter)")
            .defineInRange("magnitudeMin", 2.0, 1.0, 5.0);
        EARTHQUAKE_MAGNITUDE_MAX = builder
            .comment("Maximum earthquake magnitude (Richter)")
            .defineInRange("magnitudeMax", 7.0, 3.0, 9.0);
        EARTHQUAKE_DEPTH = builder
            .comment("Earthquake hypocenter depth (blocks)")
            .defineInRange("depth", 50.0, 10.0, 200.0);
        EARTHQUAKE_DAMAGE_RADIUS = builder
            .comment("Damage radius multiplier per magnitude")
            .defineInRange("damageRadius", 50.0, 10.0, 500.0);
        builder.pop();

        builder.push("subsidence");
        ENABLE_SUBSIDENCE = builder
            .comment("Enable ground subsidence from mining/groundwater")
            .define("enabled", true);
        SUBSIDENCE_MINING_TRIGGER = builder
            .comment("Mined blocks per chunk to trigger subsidence")
            .defineInRange("miningTrigger", 1000.0, 100.0, 10000.0);
        SUBSIDENCE_GROUNDWATER_TRIGGER = builder
            .comment("Groundwater depletion to trigger subsidence (m)")
            .defineInRange("groundwaterTrigger", 10.0, 1.0, 100.0);
        SUBSIDENCE_RATE = builder
            .comment("Subsidence rate (blocks per tick)")
            .defineInRange("rate", 0.001, 0.0, 0.1);
        SUBSIDENCE_MAX_DEPTH = builder
            .comment("Maximum subsidence depth (blocks)")
            .defineInRange("maxDepth", 10, 1, 100);
        builder.pop();

        builder.push("volcanism");
        ENABLE_VOLCANISM = builder
            .comment("Enable volcanic activity")
            .define("enabled", true);
        VOLCANO_SPAWN_CHANCE = builder
            .comment("Chance per chunk for volcano formation")
            .defineInRange("spawnChance", 1e-5, 1e-7, 1e-3);
        VOLCANO_MIN_HEIGHT = builder
            .comment("Minimum volcano height (blocks)")
            .defineInRange("minHeight", 50, 20, 200);
        VOLCANO_MAX_HEIGHT = builder
            .comment("Maximum volcano height (blocks)")
            .defineInRange("maxHeight", 200, 50, 500);
        ERUPTION_FREQUENCY = builder
            .comment("Eruptions per day per volcano")
            .defineInRange("eruptionFrequency", 0.1, 0.0, 10.0);
        LAVA_FLOW_SPEED = builder
            .comment("Lava flow speed (blocks/tick)")
            .defineInRange("lavaFlowSpeed", 0.5, 0.01, 5.0);
        ASH_FALL_RADIUS = builder
            .comment("Ash fall radius from eruption (chunks)")
            .defineInRange("ashFallRadius", 32.0, 1.0, 256.0);
        builder.pop();

        builder.push("general");
        HAZARDS_AFFECT_PLAYERS = builder
            .comment("Hazards can damage players")
            .define("affectPlayers", true);
        HAZARDS_AFFECT_STRUCTURES = builder
            .comment("Hazards can damage structures")
            .define("affectStructures", true);
        HAZARD_COOLDOWN_TICKS = builder
            .comment("Cooldown between hazard events in same area (ticks)")
            .defineInRange("cooldown", 24000, 1000, 1000000);
        builder.pop();

        builder.pop(); // stability

        SPEC = builder.build();
    }

    private StabilityRuntime() {}
}