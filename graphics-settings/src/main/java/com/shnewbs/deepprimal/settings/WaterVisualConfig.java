package com.shnewbs.deepprimal.settings;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Presentation settings remain independent of server water physics and the graphics backend. */
public final class WaterVisualConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue COLORS, FOG, WAVES, WAKES, CASCADES, SNOW, SMOOTH;
  public static final ModConfigSpec.IntValue PARTICLES, WAVE_RADIUS;

  static {
    var b = new ModConfigSpec.Builder();
    SMOOTH = b.define("smoothEngineWaterMesh", true);
    COLORS = b.define("blendedWaterColors", true);
    FOG = b.define("waterClarity", true);
    WAVES = b.define("oceanSurfaceWaves", true);
    WAKES = b.define("waterWakeParticles", true);
    CASCADES = b.define("cascadeSpray", true);
    SNOW = b.define("snowFoliageAndFlurries", true);
    PARTICLES =
        b.comment("Shared optional water/snow particle budget per client tick.")
            .defineInRange("particlesPerTick", 24, 0, 128);
    WAVE_RADIUS =
        b.comment("Loaded-ocean surface overlay radius in blocks.")
            .defineInRange("waveRadius", 32, 8, 64);
    SPEC = b.build();
  }

  private WaterVisualConfig() {}
}
