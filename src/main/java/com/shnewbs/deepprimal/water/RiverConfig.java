package com.shnewbs.deepprimal.water;

import net.neoforged.neoforge.common.ModConfigSpec;

/** World-synced gameplay settings, independent of terrain selection and graphics. */
public final class RiverConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue CURRENTS;
  public static final ModConfigSpec.DoubleValue STRENGTH;

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
    SPEC = b.build();
  }

  private RiverConfig() {}
}
