package com.shnewbs.deepprimal.core;

/** Checks unsupported requests and exact user-visible spatial sizes independently of GPU code. */
public final class UpscalingPolicyChecks {
  private static void require(boolean value, String reason) {
    if (!value) throw new AssertionError(reason);
  }

  public static void main(String[] args) {
    var none = UpscalingPolicy.Capabilities.NONE;
    var fsr = new UpscalingPolicy.Capabilities(true, true, false);
    for (var mode : UpscalingPolicy.Mode.values()) {
      require(UpscalingPolicy.effective(mode, none) == UpscalingPolicy.Mode.NONE,
          "Absent providers always retain native size");
      require(UpscalingPolicy.effective(mode, fsr)
          == (mode == UpscalingPolicy.Mode.DLSS ? UpscalingPolicy.Mode.NONE : mode),
          "Spatial/FSR capability never implies DLSS support");
    }
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
    System.out.println("UPSCALING_POLICY_PASS unsupportedFallback=true qualitySizes=true "
        + "oddDimensions=true finiteBounds=true independentFrameGeneration=true");
  }
}
