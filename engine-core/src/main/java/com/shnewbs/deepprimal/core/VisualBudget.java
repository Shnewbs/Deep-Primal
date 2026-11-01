package com.shnewbs.deepprimal.core;

/** Pure, inclusive visual limits. None of these policies stop gameplay ticks. */
public final class VisualBudget {
    public static boolean outside(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                  double x, double y, double z, int radius) {
        if (radius <= 0) return false;
        // Malformed native bounds/camera telemetry must never silently admit
        // visual work: NaN otherwise makes the squared-distance test false.
        if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(minZ)
                || !Double.isFinite(maxX) || !Double.isFinite(maxY) || !Double.isFinite(maxZ)
                || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || minX > maxX || minY > maxY || minZ > maxZ) return true;
        double dx = Math.max(Math.max(minX - x, x - maxX), 0);
        double dy = Math.max(Math.max(minY - y, y - maxY), 0);
        double dz = Math.max(Math.max(minZ - z, z - maxZ), 0);
        return dx * dx + dy * dy + dz * dz > (double)radius * radius;
    }
    public static int frameLimit(int nativeLimit, boolean focused, boolean minimized, int background, int hidden) {
        int cap = minimized ? hidden : !focused ? background : 0;
        // A native limit of zero means uncapped; do not let it suppress a
        // configured background or minimized ceiling.
        return cap > 0 ? (nativeLimit == 0 ? cap : Math.min(nativeLimit, cap)) : nativeLimit;
    }
    private VisualBudget() {}
}
