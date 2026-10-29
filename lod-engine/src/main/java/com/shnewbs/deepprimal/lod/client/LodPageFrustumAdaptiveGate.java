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
  private int remainingSkips;

  boolean shouldTestPages() {
    if (!ENABLED || remainingSkips == 0) return true;
    remainingSkips--;
    return false;
  }

  /** Call only after a successful complete extraction, never on an exception. */
  void completed(boolean tested, int testedPages, int avoidedPartTests) {
    if (!ENABLED || !tested || testedPages < MIN_SAMPLED_PAGES) return;
    if (testedPages < 0 || avoidedPartTests < 0)
      throw new IllegalArgumentException("invalid coarse frustum counters");
    // If page rejection saves at most one per-part test for each coarse
    // test, bypass coarse for a few frames. Equal test counts are only a
    // heuristic CPU-work proxy, not measured GPU or driver performance.
    if (avoidedPartTests <= testedPages) remainingSkips = SKIP_EXTRACTIONS;
  }

  void reset() {
    remainingSkips = 0;
  }
}
