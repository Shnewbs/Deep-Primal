package com.shnewbs.deepprimal.core;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Terrain generation configuration.
 * All parameters are validated at startup.
 */
public final class TerrainConfig {
    public static final ModConfigSpec SPEC;

    // Revision
    public static final ModConfigSpec.IntValue REVISION;

    // Continental
    public static final ModConfigSpec.DoubleValue CONTINENT_SCALE;
    public static final ModConfigSpec.DoubleValue CONTINENT_OCTAVES;
    public static final ModConfigSpec.DoubleValue CONTINENT_PERSISTENCE;

    // Mountains
    public static final ModConfigSpec.DoubleValue MOUNTAIN_SCALE;
    public static final ModConfigSpec.DoubleValue MOUNTAIN_OCTAVES;
    public static final ModConfigSpec.DoubleValue MOUNTAIN_PERSISTENCE;
    public static final ModConfigSpec.BooleanValue ENABLE_MOUNTAINS;

    // Hills
    public static final ModConfigSpec.DoubleValue HILL_SCALE;
    public static final ModConfigSpec.DoubleValue HILL_OCTAVES;
    public static final ModConfigSpec.DoubleValue HILL_PERSISTENCE;

    // Rivers
    public static final ModConfigSpec.DoubleValue RIVER_DENSITY;
    public static final ModConfigSpec.DoubleValue RIVER_WIDTH_SCALE;
    public static final ModConfigSpec.BooleanValue RIVER_TUNNELS;
    public static final ModConfigSpec.IntValue RIVER_MIN_CATCHMENT;

    // Coast
    public static final ModConfigSpec.DoubleValue COAST_NOISE_SCALE;
    public static final ModConfigSpec.DoubleValue COAST_DETAIL_SCALE;

    // Caves
    public static final ModConfigSpec.BooleanValue ENABLE_CAVES;
    public static final ModConfigSpec.DoubleValue CAVE_FREQUENCY;
    public static final ModConfigSpec.DoubleValue CAVE_THICKNESS;
    public static final ModConfigSpec.IntValue CAVE_MIN_Y;
    public static final ModConfigSpec.IntValue CAVE_MAX_Y;

    // Biomes
    public static final ModConfigSpec.DoubleValue BIOME_TEMPERATURE_SCALE;
    public static final ModConfigSpec.DoubleValue BIOME_RAINFALL_SCALE;
    public static final ModConfigSpec.BooleanValue BIOME_3D;

    // Height
    public static final ModConfigSpec.IntValue SEA_LEVEL;
    public static final ModConfigSpec.IntValue MAX_HEIGHT;
    public static final ModConfigSpec.IntValue MIN_HEIGHT;

    static {
        var builder = new ModConfigSpec.Builder();

        builder.push("terrain");
        REVISION = builder
            .comment("Terrain generation revision. Changing this changes world geometry.")
            .defineInRange("revision", 7, 1, 7);

        builder.push("continent");
        CONTINENT_SCALE = builder
            .comment("Base scale of continental landmasses (blocks per noise unit)")
            .defineInRange("scale", 8192.0, 1000.0, 50000.0);
        CONTINENT_OCTAVES = builder
            .comment("Number of octaves for continental noise")
            .defineInRange("octaves", 6.0, 1.0, 10.0);
        CONTINENT_PERSISTENCE = builder
            .comment("Persistence of continental noise octaves")
            .defineInRange("persistence", 0.5, 0.1, 1.0);
        builder.pop();

        builder.push("mountains");
        ENABLE_MOUNTAINS = builder
            .comment("Enable mountain generation")
            .define("enabled", true);
        MOUNTAIN_SCALE = builder
            .comment("Scale of mountain noise")
            .defineInRange("scale", 780.0, 100.0, 5000.0);
        MOUNTAIN_OCTAVES = builder
            .comment("Mountain noise octaves")
            .defineInRange("octaves", 4.0, 1.0, 8.0);
        MOUNTAIN_PERSISTENCE = builder
            .comment("Mountain noise persistence")
            .defineInRange("persistence", 0.5, 0.1, 1.0);
        builder.pop();

        builder.push("hills");
        HILL_SCALE = builder
            .comment("Scale of hill noise")
            .defineInRange("scale", 65.0, 10.0, 500.0);
        HILL_OCTAVES = builder
            .comment("Hill noise octaves")
            .defineInRange("octaves", 3.0, 1.0, 6.0);
        HILL_PERSISTENCE = builder
            .comment("Hill noise persistence")
            .defineInRange("persistence", 0.5, 0.1, 1.0);
        builder.pop();

        builder.push("rivers");
        RIVER_DENSITY = builder
            .comment("River density (higher = more rivers)")
            .defineInRange("density", 1.0, 0.1, 10.0);
        RIVER_WIDTH_SCALE = builder
            .comment("River width multiplier")
            .defineInRange("widthScale", 1.0, 0.1, 5.0);
        RIVER_TUNNELS = builder
            .comment("Allow rivers to tunnel through mountains")
            .define("tunnels", true);
        RIVER_MIN_CATCHMENT = builder
            .comment("Minimum catchment area for river formation")
            .defineInRange("minCatchment", 18000, 1000, 100000);
        builder.pop();

        builder.push("coast");
        COAST_NOISE_SCALE = builder
            .comment("Coastline noise scale")
            .defineInRange("noiseScale", 1700.0, 100.0, 10000.0);
        COAST_DETAIL_SCALE = builder
            .comment("Coastline detail scale")
            .defineInRange("detailScale", 430.0, 50.0, 5000.0);
        builder.pop();

        builder.push("caves");
        ENABLE_CAVES = builder
            .comment("Enable cave generation")
            .define("enabled", true);
        CAVE_FREQUENCY = builder
            .comment("Cave frequency multiplier")
            .defineInRange("frequency", 1.0, 0.1, 5.0);
        CAVE_THICKNESS = builder
            .comment("Cave thickness multiplier")
            .defineInRange("thickness", 1.0, 0.1, 3.0);
        CAVE_MIN_Y = builder
            .comment("Minimum Y for cave generation")
            .defineInRange("minY", -64, -64, 256);
        CAVE_MAX_Y = builder
            .comment("Maximum Y for cave generation")
            .defineInRange("maxY", 180, 0, 320);
        builder.pop();

        builder.push("biomes");
        BIOME_TEMPERATURE_SCALE = builder
            .comment("Temperature noise scale")
            .defineInRange("temperatureScale", 6500.0, 1000.0, 50000.0);
        BIOME_RAINFALL_SCALE = builder
            .comment("Rainfall noise scale")
            .defineInRange("rainfallScale", 3100.0, 1000.0, 50000.0);
        BIOME_3D = builder
            .comment("Use 3D biome noise (includes height variation)")
            .define("biome3d", true);
        builder.pop();

        builder.push("height");
        SEA_LEVEL = builder
            .comment("Sea level (Y coordinate)")
            .defineInRange("seaLevel", 96, 0, 256);
        MAX_HEIGHT = builder
            .comment("Maximum terrain height")
            .defineInRange("maxHeight", 384, 128, 1024);
        MIN_HEIGHT = builder
            .comment("Minimum terrain height")
            .defineInRange("minHeight", -64, -128, 0);
        builder.pop();

        builder.pop(); // terrain

        SPEC = builder.build();
    }

    public static TerrainOptions fromConfig() {
        return new TerrainOptions(
            ENABLE_CAVES.get(),
            RIVER_TUNNELS.get(),
            true, // dunes
            true, // glaciers
            true, // debris
            MOUNTAIN_SCALE.get(),
            16000.0 // polarDistance
        );
    }

    private TerrainConfig() {}
}