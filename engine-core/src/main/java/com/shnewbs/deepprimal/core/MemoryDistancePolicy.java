package com.shnewbs.deepprimal.core;

/** Conservative admission estimate, not a promise of whole-process memory usage. */
public final class MemoryDistancePolicy {
    public static int renderLimit(long maximumHeap, int worldSections) {
        // Reserve one quarter for other state; charge full-height columns even with a vertical render window.
        long columnBytes = Math.max(24, worldSections) * 128L * 1024;
        long columns = Math.max(25, maximumHeap / 4 * 3 / columnBytes);
        int radius = (int)((Math.sqrt(columns) - 1) / 2);
        return Math.clamp(radius, 2, DistancePolicy.MAX_RENDER);
    }
    private MemoryDistancePolicy() {}
}
