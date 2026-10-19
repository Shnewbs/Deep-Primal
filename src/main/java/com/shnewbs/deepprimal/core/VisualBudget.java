package com.shnewbs.deepprimal.core;

/** Pure, inclusive visual limits. None of these policies stop gameplay ticks. */
public final class VisualBudget {
    public static boolean outside(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                  double x, double y, double z, int radius) {
        if (radius <= 0) return false;
        double dx = Math.max(Math.max(minX - x, x - maxX), 0);
        double dy = Math.max(Math.max(minY - y, y - maxY), 0);
        double dz = Math.max(Math.max(minZ - z, z - maxZ), 0);
        return dx * dx + dy * dy + dz * dz > (double)radius * radius;
    }
    public static int frameLimit(int nativeLimit, boolean focused, boolean minimized, int background, int hidden) {
        int cap = minimized ? hidden : !focused ? background : 0;
        return cap > 0 ? Math.min(nativeLimit, cap) : nativeLimit;
    }
    private VisualBudget() {}
}
