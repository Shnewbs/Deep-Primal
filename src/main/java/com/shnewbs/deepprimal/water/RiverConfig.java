package com.shnewbs.deepprimal.water;

import net.neoforged.neoforge.common.ModConfigSpec;

/** World-synced gameplay settings, independent of terrain selection and graphics. */
public final class RiverConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue CURRENTS;
  public static final ModConfigSpec.DoubleValue STRENGTH;

  public static final ModConfigSpec.BooleanValue VARIABLE_SPEED,
      FINITE_SOURCES,
      NATURAL_RENEWAL,
      RAIN_REFILL,
      SNOW_DRIFTS,
      HORSES,
      FARM_WALKING,
      PRESSURE,
      OCEAN_MOTION;
  public static final ModConfigSpec.IntValue SNOW_CAP, PRESSURE_DEPTH;

  public static final ModConfigSpec.BooleanValue FINITE_VOLUME, FLUID_PRESSURE, RAIN_RUNOFF,
      EVAPORATION, GLOBAL_WINTER, UNLIMITED_SNOW, SWIM_STAMINA, PRESSURE_EFFECTS;
  public static final ModConfigSpec.IntValue FLUID_WORK, PRESSURE_SEARCH, SWIM_SECONDS;
  public static final ModConfigSpec.DoubleValue TREADING_OFFSET, ARMOR_PROTECTION;

  static {
    var b = new ModConfigSpec.Builder();
    CURRENTS =
        b.comment(
                "Enable the stored downstream current in generated river water.",
                "Disabling leaves ordinary water physics; no terrain regeneration is required.")
            .define("currents", true);
    STRENGTH =
        b.comment("Scale river current force. Zero disables the added force.")
            .defineInRange("currentStrength", 1.0, 0.0, 2.0);
    VARIABLE_SPEED = b.define("variableCurrentSpeed", true);
    FINITE_SOURCES =
        b.comment("Disable ordinary source regeneration; not a finite-volume solver.")
            .define("finiteSources", false);
    NATURAL_RENEWAL = b.define("renewableNaturalWaters", true);
    RAIN_REFILL = b.define("rainRefill", true);
    SNOW_DRIFTS = b.define("snowDrifts", true);
    SNOW_CAP = b.defineInRange("snowCapLayers", 24, 1, 64);
    HORSES = b.define("swimmingHorses", true);
    FARM_WALKING = b.define("farmlandWaterWalking", false);
    PRESSURE = b.define("waterPressure", false);
    PRESSURE_DEPTH = b.defineInRange("pressureFreeDepth", 64, 16, 512);
    OCEAN_MOTION = b.define("oceanBoatMotion", true);
    b.push("hydrology");
    FINITE_VOLUME = b.comment("Conserve native water volume in loaded cells. Changes water gameplay; disabled by default. Waterlogged cells stay sealed; lava stays native.").define("finiteVolume", false);
    FLUID_PRESSURE = b.define("equalizeConnectedPools", true);
    FLUID_WORK = b.defineInRange("operationsPerTick", 2048, 64, 16384);
    PRESSURE_SEARCH = b.defineInRange("pressureSearchBlocks", 128, 16, 512);
    RAIN_RUNOFF = b.define("rainfallRunoff", true);
    EVAPORATION = b.define("evaporation", true);
    b.pop().push("swimming");
    SWIM_STAMINA = b.define("stamina", false);
    SWIM_SECONDS = b.defineInRange("maximumSeconds", 90, 10, 600);
    TREADING_OFFSET = b.defineInRange("treadingOffset", 0.0, 0.0, 0.04);
    b.pop().push("pressure");
    ARMOR_PROTECTION = b.defineInRange("protectionPerArmorPiece", 0.1, 0.0, 0.25);
    PRESSURE_EFFECTS = b.define("tieredEffects", true);
    b.pop().push("snow");
    GLOBAL_WINTER = b.comment("Snow replaces rain in precipitating biomes; does not replace desert climate or implement seasons.").define("globalWinter", false);
    UNLIMITED_SNOW = b.define("unlimitedAccumulation", false);
    b.pop();
    SPEC = b.build();
  }

  private RiverConfig() {}
}

