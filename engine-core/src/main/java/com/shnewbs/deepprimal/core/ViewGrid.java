package com.shnewbs.deepprimal.core;

/** Ring-grid relocation shared by all Y sections of a column. */
public final class ViewGrid {
    private ViewGrid() {}

    public static int coordinate(int camera, int radius, int index) {
        if (radius < 0) throw new IllegalArgumentException("Negative view radius");
        long low = (long) camera - radius;
        long high = (long) camera + radius;
        // An int section coordinate cannot represent a window that crosses either boundary.
        // Reject it instead of silently wrapping into the opposite side of the world.
        if (low < Integer.MIN_VALUE || high > Integer.MAX_VALUE)
            throw new IllegalArgumentException("View window exceeds signed section coordinates");
        long side = 2L * radius + 1;
        return (int) (low + Math.floorMod((long) index - low, side));
    }
}
