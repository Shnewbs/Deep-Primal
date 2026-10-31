package com.shnewbs.deepprimal.core;

/** A fixed-size section window; slots wrap by absolute Y, including below zero. */
public final class VerticalRange {
    public static final int MAX_RADIUS = 96;
    private VerticalRange() {}

    public static int count(int worldSections, int radius) {
        if (worldSections < 1 || radius < 0) throw new IllegalArgumentException("Invalid section window");
        // Radius is a user-facing setting. Use long arithmetic so even malformed
        // extreme values cannot wrap a valid section count negative.
        return radius == 0 ? worldSections : (int)Math.min((long)worldSections, 2L * radius + 1);
    }

    public static int low(int cameraY, int worldMin, int worldSections, int radius) {
        int size = count(worldSections, radius);
        long worldLast = (long)worldMin + worldSections - 1;
        if (worldLast > Integer.MAX_VALUE) throw new IllegalArgumentException("Unrepresentable world sections");
        if (radius == 0) return worldMin;
        // Camera positions may be outside the world. Clamp *before* narrowing
        // to int, avoiding underflow at MIN_VALUE and overflow near MAX_VALUE.
        long start = Math.max((long)worldMin,
            Math.min((long)cameraY - radius, (long)worldMin + worldSections - size));
        return (int)start;
    }

    public static int coordinate(int low, int count, int slot) {
        if (count < 1 || (long)low + count - 1 > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Invalid section ring");
        // slot-low may span 2^32-1; int subtraction aliases unrelated slots.
        return (int)((long)low + Math.floorMod((long)slot - low, count));
    }

    public static int visibilityRadius(int worldSections, int radius) {
        if (worldSections < 1 || radius < 0) throw new IllegalArgumentException("Invalid section window");
        return radius == 0 ? worldSections : Math.min(worldSections, radius);
    }

    public static float fogScale(int horizontal, int worldSections, int radius) {
        return (float)horizontal / visibilityRadius(worldSections, radius);
    }
}
