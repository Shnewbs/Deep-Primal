package com.shnewbs.deepprimal.settings;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Settings are applied live; limits describe this subsystem, not the whole game. */
public final class LodConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue ENABLED, PERSIST;
  public static final ModConfigSpec.IntValue RADIUS, MEMORY, DISK, CAPTURES, UPLOADS, ERROR;

  static {
    var b = new ModConfigSpec.Builder();
    ENABLED = b.define("enabled", true);
    PERSIST = b.define("persistentCache", true);
    RADIUS =
        b.comment(
                "Maximum radius in chunks for previously received terrain; does not request new"
                    + " full chunks.")
            .defineInRange("radius", 192, 16, 512);
    MEMORY =
        b.comment("Resident LOD GPU payload budget in MiB.")
            .defineInRange("meshMemoryMiB", 32, 4, 256);
    DISK =
        b.comment("Per world/dimension compressed disk budget in MiB.")
            .defineInRange("diskCacheMiB", 512, 16, 4096);
    CAPTURES =
        b.comment(
                "Tick interval between immutable chunk copies. One in-flight copy/reduction, with a"
                    + " 2 ms start budget.")
            .defineInRange("captureIntervalTicks", 1, 1, 8);
    UPLOADS = b.defineInRange("meshUploadsPerFrame", 1, 1, 4);
    ERROR =
        b.comment("Projected horizontal cell width in pixels before a coarser level is selected.")
            .defineInRange("detailPixels", 3, 1, 12);
    SPEC = b.build();
  }

  private LodConfig() {}
}
