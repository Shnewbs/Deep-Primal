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
    SPEC = b.build();
  }

  private RiverConfig() {}
}
