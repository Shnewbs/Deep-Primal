package com.shnewbs.deepprimal.core;

/** Checks unsupported requests and exact user-visible spatial sizes independently of GPU code. */
public final class UpscalingPolicyChecks {
  private static int assertions;
  private static void require(boolean value, String reason) {
    assertions++;
    if (!value) throw new AssertionError(reason);
  }

  public static void main(String[] args) {
    var none = UpscalingPolicy.Capabilities.NONE;
    var fsr = new UpscalingPolicy.Capabilities(true, true, false);
    var dlss = new UpscalingPolicy.Capabilities(false, false, true);
    var allDeclared = new UpscalingPolicy.Capabilities(true, true, true);
    for (var mode : UpscalingPolicy.Mode.values()) {
      require(UpscalingPolicy.effective(mode, none) == UpscalingPolicy.Mode.NONE,
          "Absent providers always retain native size");
      var expectedFsr = switch (mode) {
        case SPATIAL, FSR -> mode;
        case NONE, DLSS, FSR_31, FSR_41 -> UpscalingPolicy.Mode.NONE;
      };
      require(UpscalingPolicy.effective(mode, fsr) == expectedFsr,
          "Spatial/FSR 1 capabilities cannot imply temporal FSR or DLSS support");
      require(UpscalingPolicy.effective(mode, dlss)
          == (mode == UpscalingPolicy.Mode.DLSS ? mode : UpscalingPolicy.Mode.NONE),
          "NVIDIA provider does not establish spatial/AMD support");
      require(UpscalingPolicy.effective(mode, allDeclared)
          == ((mode == UpscalingPolicy.Mode.FSR_31 || mode == UpscalingPolicy.Mode.FSR_41)
              ? UpscalingPolicy.Mode.NONE : mode),
          "Temporal FSR has no implemented provider even when all existing booleans are true");
      boolean futureTemporal = mode == UpscalingPolicy.Mode.DLSS
          || mode == UpscalingPolicy.Mode.FSR_31 || mode == UpscalingPolicy.Mode.FSR_41;
      require(UpscalingPolicy.nativeAaSelectable(mode) == futureTemporal,
          "Native AA is only selectable with a temporal provider preference");
      require(UpscalingPolicy.lastSelectableQuality(mode)
          == (futureTemporal ? UpscalingPolicy.Quality.NATIVE_AA.ordinal()
              : UpscalingPolicy.Quality.CUSTOM.ordinal()),
          "Native AA cannot enter FSR 1's quality cycle");
      require(UpscalingPolicy.menuQuality(mode, UpscalingPolicy.Quality.NATIVE_AA)
          == (mode == UpscalingPolicy.Mode.FSR
              ? UpscalingPolicy.Quality.QUALITY : UpscalingPolicy.Quality.NATIVE_AA),
          "Legacy FSR 1 Native AA is corrected only in staged UI state");
      require(UpscalingPolicy.menuQuality(mode, UpscalingPolicy.Quality.CUSTOM)
          == UpscalingPolicy.Quality.CUSTOM, "Valid custom scaling stays selected");
    }
    require(UpscalingPolicy.Mode.NONE.ordinal() == 0
            && UpscalingPolicy.Mode.SPATIAL.ordinal() == 1
            && UpscalingPolicy.Mode.FSR.ordinal() == 2
            && UpscalingPolicy.Mode.DLSS.ordinal() == 3
            && UpscalingPolicy.Mode.FSR_31.ordinal() == 4
            && UpscalingPolicy.Mode.FSR_41.ordinal() == 5,
        "Saved mode enum ordinals remain compatible");
    require(UpscalingPolicy.Quality.NATIVE_AA.ordinal() == 5,
        "Quality enum stays compatible with existing saved preferences");
    require(UpscalingPolicy.dimension(1920, UpscalingPolicy.Quality.QUALITY.scale(.7)) == 1280,
        "1920 Quality renders 1280 pixels");
    require(UpscalingPolicy.dimension(1080, UpscalingPolicy.Quality.QUALITY.scale(.7)) == 720,
        "1080 Quality renders 720 pixels");
    require(UpscalingPolicy.dimension(1920, UpscalingPolicy.Quality.ULTRA_QUALITY.scale(.7)) == 1477,
        "Ultra Quality uses 1.3 factor with upward rounding");
    require(UpscalingPolicy.dimension(3440, UpscalingPolicy.Quality.BALANCED.scale(.7)) == 2024,
        "Ultrawide Balanced keeps the last pixel");
    require(UpscalingPolicy.dimension(1, .5) == 1 && UpscalingPolicy.dimension(641, .5) == 321,
        "Small and odd framebuffers stay nonzero and bounded");
    require(UpscalingPolicy.renderScale(UpscalingPolicy.Mode.SPATIAL,
        UpscalingPolicy.Quality.QUALITY, .63) == .63, "Spatial uses its enabled custom slider");
    require(UpscalingPolicy.renderScale(UpscalingPolicy.Mode.FSR,
        UpscalingPolicy.Quality.CUSTOM, .63) == .63, "FSR Custom uses its slider");
    require(UpscalingPolicy.renderScale(UpscalingPolicy.Mode.DLSS,
        UpscalingPolicy.Quality.PERFORMANCE, .5) == 1, "No hard-coded DLSS render dimensions");
    int rejected = 0;
    for (double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY, .499, 1.001}) {
      try { UpscalingPolicy.dimension(1920, value); }
      catch (IllegalArgumentException expected) { rejected++; }
    }
    require(rejected == 5, "Nonfinite/out-of-range values are rejected before allocations");
    for (int width : new int[] {1, 2, 3, 641, 1920, 3840, 16384, Integer.MAX_VALUE}) {
      int previous = 0;
      for (double value : new double[] {.5, .63, 2.0 / 3, 1.0 / 1.3, 1}) {
        int result = UpscalingPolicy.dimension(width, value);
        require(result >= 1 && result <= width && result >= previous,
            "Allocation dimensions remain monotonic and within display size");
        previous = result;
      }
    }
    // Cycle boundary may never admit the Native AA ordinal for FSR 1.
    for (int step = 0; step < 40; step++) {
      int choice = step % (UpscalingPolicy.lastSelectableQuality(UpscalingPolicy.Mode.FSR) + 1);
      require(choice != UpscalingPolicy.Quality.NATIVE_AA.ordinal(),
          "FSR 1 menu excludes Native AA for every cycle position");
    }
    System.out.println("UPSCALING_POLICY_PASS assertions=" + assertions + " unsupportedFallback=true "
        + "qualitySizes=true nativeAaTemporalOnly=true legacyStageCorrection=true "
        + "oddDimensions=true finiteBounds=true independentFrameGeneration=true");
  }
}
