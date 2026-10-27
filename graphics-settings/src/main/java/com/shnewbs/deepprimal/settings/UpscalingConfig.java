package com.shnewbs.deepprimal.settings;

import com.shnewbs.deepprimal.core.UpscalingPolicy;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Saved requests and verified runtime status are deliberately separate. */
public final class UpscalingConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.EnumValue<UpscalingPolicy.Mode> MODE;
  public static final ModConfigSpec.EnumValue<UpscalingPolicy.Quality> QUALITY;
  public static final ModConfigSpec.DoubleValue RENDER_SCALE;
  public static final ModConfigSpec.DoubleValue SHARPNESS;
  public static final ModConfigSpec.BooleanValue FRAME_GENERATION;

  static {
    var b = new ModConfigSpec.Builder();
    MODE = b.comment("NONE is native resolution. SPATIAL is linear scaling. FSR is AMD FSR 1 EASU/RCAS. "
        + "DLSS requires the NVIDIA NGX runtime and a complete temporal renderer; unavailable "
        + "requests retain native rendering. HUD and menus always remain at display resolution.")
        .defineEnum("mode", UpscalingPolicy.Mode.NONE);
    // SPATIAL still uses renderScale directly, preserving existing configurations.
    QUALITY = b.comment("Spatial quality preset; CUSTOM uses renderScale. DLSS sizes come from NGX.")
        .defineEnum("quality", UpscalingPolicy.Quality.QUALITY);
    RENDER_SCALE = b.comment("Custom world-render scale: 1 = native, 0.5 = half width/height.")
        .defineInRange("renderScale", 1.0, .5, 1.0);
    SHARPNESS = b.comment("FSR 1 RCAS strength: 0 disables sharpening, 1 is maximum. No DLSS sharpening.")
        .defineInRange("sharpness", .2, 0.0, 1.0);
    FRAME_GENERATION = b.comment("Reserved preference. No frame-generation provider is installed; "
        + "this does not enable generated frames or disable supported super resolution.")
        .define("frameGeneration", false);
    SPEC = b.build();
  }

  public static UpscalingPolicy.Mode requestedMode() {
    return SPEC.isLoaded() ? MODE.get() : UpscalingPolicy.Mode.NONE;
  }

  public static double requestedScale() {
    return SPEC.isLoaded()
        ? UpscalingPolicy.renderScale(requestedMode(), QUALITY.get(), RENDER_SCALE.get()) : 1;
  }

  public static float sharpness() { return SPEC.isLoaded() ? SHARPNESS.get().floatValue() : .2f; }

  public static UpscalingPolicy.Mode getActiveMode() { return UpscalingRuntime.activeMode(); }
  public static double activeScale() { return UpscalingRuntime.activeScale(); }
  public static String unavailableReason() { return unavailableReason(requestedMode()); }
  public static String unavailableReason(UpscalingPolicy.Mode requested) {
    return UpscalingRuntime.unavailableReason(requested);
  }

  /** Apply becomes effective on the next world frame; no window/API restart is needed. */
  public static void applyChanges() { UpscalingRuntime.requestReconfigure(); }

  private UpscalingConfig() {}
}

