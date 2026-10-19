package com.shnewbs.deepprimal.core;

/** A fixed-size section window; slots wrap by absolute Y, including below zero. */
public final class VerticalRange {
    public static final int MAX_RADIUS = 96;
    private VerticalRange() {}

    public static int count(int worldSections, int radius) {
        return radius == 0 ? worldSections : Math.min(worldSections, radius * 2 + 1);
    }

    public static int low(int cameraY, int worldMin, int worldSections, int radius) {
        int size = count(worldSections, radius);
        return radius == 0 ? worldMin : Math.clamp(cameraY - radius, worldMin, worldMin + worldSections - size);
    }

    public static int coordinate(int low, int count, int slot) {
        return low + Math.floorMod(slot - low, count);
    }

    public static int visibilityRadius(int worldSections, int radius) {
        return radius == 0 ? worldSections : Math.min(worldSections, radius);
    }

    public static float fogScale(int horizontal, int worldSections, int radius) {
        return (float)horizontal / visibilityRadius(worldSections, radius);
    }
}
