package com.shnewbs.deepprimal.core;

import java.util.Objects;

/** Backend-independent choices. A saved preference never establishes provider support. */
public final class UpscalingPolicy {
  // Retain serialized names for existing upscaling.toml files.
  public enum Mode { NONE, SPATIAL, FSR, DLSS, FSR_31, FSR_41 }

  /** FSR 1's documented scale factors; DLSS obtains its sizes from NGX instead. */
  public enum Quality {
    ULTRA_QUALITY(1.0 / 1.3), QUALITY(1.0 / 1.5), BALANCED(1.0 / 1.7),
    PERFORMANCE(0.5), CUSTOM(1.0), NATIVE_AA(1.0);

    private final double scale;
    Quality(double scale) { this.scale = scale; }
    public double scale(double customScale) {
      validateScale(customScale);
      return this == CUSTOM ? customScale : scale;
    }
  }

  /** Values must come from initialized rendering providers, never a saved API/GPU name. */
  public record Capabilities(boolean spatial, boolean fsr1, boolean dlss) {
    public static final Capabilities NONE = new Capabilities(false, false, false);
    public boolean supports(Mode mode) {
      return switch (Objects.requireNonNull(mode)) {
        case NONE -> true;
        case SPATIAL -> spatial;
        case FSR -> fsr1;
        case FSR_31, FSR_41 -> false; // No verified temporal provider is installed.
        case DLSS -> dlss;
      };
    }
  }

  /** Native AA is a temporal-provider quality, never an FSR 1/spatial quality. */
  public static boolean nativeAaSelectable(Mode mode) {
    return switch (Objects.requireNonNull(mode)) {
      case DLSS, FSR_31, FSR_41 -> true;
      case NONE, SPATIAL, FSR -> false;
    };
  }

  /** Leave persisted legacy choices untouched; stage a truthful FSR 1 quality in the menu. */
  public static Quality menuQuality(Mode mode, Quality stored) {
    Objects.requireNonNull(mode);
    Objects.requireNonNull(stored);
    return mode == Mode.FSR && stored == Quality.NATIVE_AA ? Quality.QUALITY : stored;
  }

  public static int lastSelectableQuality(Mode mode) {
    return nativeAaSelectable(mode) ? Quality.NATIVE_AA.ordinal() : Quality.CUSTOM.ordinal();
  }

  public static Mode effective(Mode requested, Capabilities capabilities) {
    return Objects.requireNonNull(capabilities).supports(requested) ? requested : Mode.NONE;
  }

  public static double renderScale(Mode mode, Quality quality, double customScale) {
    Objects.requireNonNull(mode);
    Objects.requireNonNull(quality);
    validateScale(customScale);
    // Temporal providers must use their SDK's optimal dimensions, not this spatial policy.
    return mode == Mode.SPATIAL ? customScale : mode == Mode.FSR ? quality.scale(customScale) : 1.0;
  }

  public static int dimension(int display, double scale) {
    if (display < 1) throw new IllegalArgumentException("Positive display dimension required");
    validateScale(scale);
    return Math.max(1, (int) Math.ceil(display * scale));
  }

  private static void validateScale(double scale) {
    if (!Double.isFinite(scale) || scale < .5 || scale > 1)
      throw new IllegalArgumentException("Render scale must be finite and between 0.5 and 1");
  }

  /** Compatibility for the former Vulkan-only spatial caller, without vendor providers. */
  public static Mode effective(Mode requested, boolean vulkan, boolean frameGeneration) {
    return vulkan && !frameGeneration && requested == Mode.SPATIAL ? Mode.SPATIAL : Mode.NONE;
  }

  public static String unavailableReason(Mode requested, boolean frameGeneration) {
    if (frameGeneration) return "Frame generation has no installed provider";
    return switch (requested) {
      case FSR -> "FSR requires a validated FidelityFX provider";
      case FSR_31 -> "FSR 3.1 temporal integration is not installed (motion vectors, depth and frame history required)";
      case FSR_41 -> "FSR 4.1.1 requires a validated compatible runtime/backend and temporal inputs";
      case DLSS -> "DLSS requires a validated NVIDIA NGX provider";
      default -> "";
    };
  }

  private UpscalingPolicy() {}
}

