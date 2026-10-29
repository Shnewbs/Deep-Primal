package com.shnewbs.deepprimal.lod.client;

/**
 * Optional CPU-only page broadphase admission. A skipped page test always falls
 * through to the original ordered per-part checks; it cannot hide geometry.
 * Sample an entire extraction after a short skip window so an offscreen camera
 * turn can restore effective coarse rejection without persisting any scene data.
 */
final class LodPageFrustumAdaptiveGate {
  static final boolean ENABLED = "true".equalsIgnoreCase(
      System.getProperty("deepprimal.lod.pageFrustumAutoSkip", "false"));
  private static final int MIN_SAMPLED_PAGES = 32;
  private static final int SKIP_EXTRACTIONS = 3;
  // Heuristic bounds, not gameplay visibility thresholds. A forced sample only
  // adds conservative coarse tests; the original per-part path is authoritative.
  private static final double RESAMPLE_TRAVEL_SQUARED = 16.0; // Four blocks.
  private static final float RESAMPLE_TURN_DEGREES = 8.0f;
  private int remainingSkips;
  private boolean hasSamplePose, stagedPose, stagedPoseFinite;
  private double sampledX, sampledY, sampledZ, nextX, nextY, nextZ;
  private float sampledYaw, sampledPitch, nextYaw, nextPitch;

  boolean shouldTestPages() {
    if (!ENABLED || remainingSkips == 0) return true;
    remainingSkips--;
    return false;
  }

  /**
   * Camera-aware overload used only by the renderer's opt-in adaptive path.
   * Compare against the last *completed* coarse sample, not the previous frame:
   * small repeated movements must eventually force an actual page test.
   * No camera history is published until completed(true, ...) succeeds.
   */
  boolean shouldTestPages(double x, double y, double z, float yaw, float pitch) {
    if (!ENABLED) return true;
    stagedPose = true;
    stagedPoseFinite = Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)
        && Float.isFinite(yaw) && Float.isFinite(pitch);
    nextX = x;
    nextY = y;
    nextZ = z;
    nextYaw = yaw;
    nextPitch = pitch;
    if (remainingSkips > 0 && (!hasSamplePose || !stagedPoseFinite
        || movedSinceSample(x, y, z, yaw, pitch))) remainingSkips = 0;
    return shouldTestPages();
  }

  private boolean movedSinceSample(double x, double y, double z, float yaw, float pitch) {
    double dx = x - sampledX, dy = y - sampledY, dz = z - sampledZ;
    // Invalid/overflowed differences force a sample rather than postponing it.
    if (!(dx * dx + dy * dy + dz * dz <= RESAMPLE_TRAVEL_SQUARED)) return true;
    return angleDifference(yaw, sampledYaw) > RESAMPLE_TURN_DEGREES
        || angleDifference(pitch, sampledPitch) > RESAMPLE_TURN_DEGREES;
  }

  private static float angleDifference(float a, float b) {
    // Normalize *before* subtracting. Extreme but finite headings can overflow
    // float subtraction, and even a finite huge difference can lose its low
    // turning bits. Each float is represented exactly as a double here; both
    // reduced operands lie within one turn so the subtraction is bounded.
    if (!Float.isFinite(a) || !Float.isFinite(b)) return Float.POSITIVE_INFINITY;
    // Real camera yaw/pitch normally stay within one turn. Preserve the
    // original cheap float path there; its difference cannot overflow.
    if (Math.abs(a) <= 360.0f && Math.abs(b) <= 360.0f) {
      float delta = Math.abs((a - b) % 360.0f);
      return Math.min(delta, 360.0f - delta);
    }
    double delta = Math.abs(((double) a % 360.0) - ((double) b % 360.0));
    if (delta >= 360.0) delta -= 360.0;
    return (float) Math.min(delta, 360.0 - delta);
  }

  /** Call only after a successful complete extraction, never on an exception. */
  void completed(boolean tested, int testedPages, int avoidedPartTests) {
    if (!ENABLED || !tested) return;
    // A failed extract never commits its staged camera pose. Old call sites
    // without a camera pose retain the original sampled-skip behavior.
    if (stagedPose) {
      hasSamplePose = stagedPoseFinite;
      if (stagedPoseFinite) {
        sampledX = nextX;
        sampledY = nextY;
        sampledZ = nextZ;
        sampledYaw = nextYaw;
        sampledPitch = nextPitch;
      }
      stagedPose = false;
    }
    if (testedPages < MIN_SAMPLED_PAGES) return;
    if (testedPages < 0 || avoidedPartTests < 0)
      throw new IllegalArgumentException("invalid coarse frustum counters");
    // If page rejection saves at most one per-part test for each coarse
    // test, bypass coarse for a few frames. Equal test counts are only a
    // heuristic CPU-work proxy, not measured GPU or driver performance.
    if (avoidedPartTests <= testedPages) remainingSkips = SKIP_EXTRACTIONS;
  }

  void reset() {
    remainingSkips = 0;
    hasSamplePose = stagedPose = stagedPoseFinite = false;
  }
}
