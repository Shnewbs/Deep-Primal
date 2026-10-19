package com.shnewbs.deepprimal.core;

/** Ring-grid relocation shared by all Y sections of a column. */
public final class ViewGrid {
    private ViewGrid() {}
    public static int coordinate(int camera, int radius, int index) {
        int low = camera - radius;
        return low + Math.floorMod(index - low, radius * 2 + 1);
    }
}
