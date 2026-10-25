package com.shnewbs.deepprimal.engine;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class HydrologyConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue ENABLED, EXISTING_WORLDS;
  public static final ModConfigSpec.IntValue COLUMNS, SOURCE_RATE, RAIN_QUANTA;
  static {
    var b = new ModConfigSpec.Builder();
    ENABLED = b.comment("Finite environmental recharge, dynamic springs and accounted ocean boundaries.")
        .define("enabled", true);
    EXISTING_WORLDS = b.comment("Apply new hydrology to saved continental_v1/v2/v3 worlds without rebuilding terrain.")
        .define("existingEngineWorlds", true);
    COLUMNS = b.comment("Maximum loaded surface columns examined per server tick; no remote chunks are loaded.")
        .defineInRange("columnsPerTick", 64, 1, 256);
    SOURCE_RATE = b.comment("Maximum native sixteenth-block units delivered per dry-land outlet per hydrology visit; planned waterways are head-, stage- and regional-quota-limited.")
        .defineInRange("outletUnitsPerVisit", 4, 1, 16);
    RAIN_QUANTA = b.comment("Recharge per exposed column per second of active rain; 256 quanta = one native unit.")
        .defineInRange("rainQuantaPerColumnSecond", 64, 0, 4096);
    SPEC = b.build();
  }
  private HydrologyConfig() {}
}
