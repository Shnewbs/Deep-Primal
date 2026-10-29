package com.shnewbs.deepprimal.lod.client;

/**
 * Optional CPU-only page broadphase admission. A skipped page test always falls
 * through to the original ordered per-part checks; it cannot hide geometry.
 * Sample an entire extraction after a short skip window so an offscreen camera
 * turn can restore effective coarse rejection without persisting any scene data.
 */
final class LodPageFrustumAdaptiveGate {
  static final boolean ENABLED = !"false".equalsIgnoreCase(
      System.getProperty("deepprimal.lod.pageFrustumAutoSkip", "true"));
  private static final int MIN_SAMPLED_PAGES = 32;
  private static final int SKIP_EXTRACTIONS = 3;
  private int remainingSkips;

  boolean shouldTestPages() {
    if (!ENABLED || remainingSkips == 0) return true;
    remainingSkips--;
    return false;
  }

  /** Call only after a successful complete extraction, never on an exception. */
  void completed(boolean tested, int testedPages, int rejectedPages) {
    if (!ENABLED || !tested || testedPages < MIN_SAMPLED_PAGES) return;
    if (rejectedPages < 0 || rejectedPages > testedPages)
      throw new IllegalArgumentException("invalid coarse frustum counters");
    // With fewer than one rejection per eight tests, coarse broadphase is
    // likely less useful than fine culling. This is a heuristic, not an FPS claim.
    if (rejectedPages * 8L < testedPages) remainingSkips = SKIP_EXTRACTIONS;
  }

  void reset() {
    remainingSkips = 0;
  }
}
